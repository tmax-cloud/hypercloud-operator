package k8s.example.client.handler;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.metering.util.SimpleUtil;

public class NameSpaceHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response get(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** GET /nameSpace");
		
		IStatus status = null;
		V1NamespaceList nsList = null;
		String outDO = null; 
		String accessToken = null;
		String userId = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_USER_ID );
//		String userId = "admin-tmax.co.kr";
		// if limit exists
		String limit = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_LIMIT );
		
		//if label selector exists
		String labelSelector = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_LABEL_SELECTOR );
		
		try {			
			// Read AccessToken from Header
			if(!session.getHeaders().get("authorization").isEmpty()) {
				accessToken = session.getHeaders().get("authorization");
			} else {
				status = Status.BAD_REQUEST;
				throw new Exception(ErrorCode.TOKEN_EMPTY);
			}
    		logger.debug( "  Token: " + accessToken );	
    		
			nsList = K8sApiCaller.getAccessibleNS(userId, labelSelector);
			status = Status.OK;

			// Limit
			if( nsList!= null) {
				if( limit != null ) {
					nsList.setItems( nsList.getItems().stream().limit(Integer.parseInt(limit)).collect(Collectors.toList()));		
				}
			}			   	

			// Make outDO					
    		if( (nsList!=null && nsList.getItems() != null && nsList.getItems().size() > 0) || nsList.getMetadata().getContinue().equalsIgnoreCase("wrongLabelorNoResource")) {
    			nsList.getMetadata().setContinue(null);
    			Gson gson = new GsonBuilder().setPrettyPrinting().create();
    			outDO = gson.toJson( nsList ).toString();
    		} else {
    			status = Status.FORBIDDEN;	
    			JsonObject result = new JsonObject();
    			outDO = "Cannot Access Any NameSpace";
    			result.addProperty("message", outDO);
    			Gson gson = new Gson();		
    		    outDO = gson.toJson(result);			
    		}
			
		} catch (ApiException e) {
			logger.error( "Exception message: " + e.getMessage() );
			outDO = "Get NameSpace List failed.";
			status = Status.BAD_REQUEST;
			e.printStackTrace();

		} catch (Exception e) {
			logger.error( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			outDO = "Get NameSpace List failed.";
			status = Status.BAD_REQUEST;
		}
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, "application/json", outDO));
    }
	
	public Response put(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** PUT /NameSpace");
		logger.info(" Trial Namespace Period Extend Service Start");

		IStatus status = null;
		String outDO = null;
		
		String nsName = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_NAMESPACE );
		String period = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_PERIOD );

		try {			
			// Read NameSpace
    		V1Namespace namespace = K8sApiCaller.getNameSpace(nsName);

    		// Update Period Label
    		if ( namespace.getMetadata().getLabels() != null && namespace.getMetadata().getLabels().get("trial") != null
    				&& namespace.getMetadata().getLabels().get("owner") != null) {
    			Map<String, String> labels = namespace.getMetadata().getLabels();
        		if ( labels.keySet().contains("period")) {
        			labels.replace("period", period);
        		}else {
        			labels.put("period", period);
        		}
    		} else {			
    			status = Status.UNAUTHORIZED;
				throw new Exception(ErrorCode.NOT_TRIAL_NAMESPACE);
    		}
	
    		// Delete Exist Trial Timer with previous Period
    		Util.deleteTrialNSTimer ( nsName );
    		
    		// Set New Trial Timer 
    		Util.setTrialNSTimer(namespace);
    		    		
			// Make outDO
			outDO = "Trial NameSpace Period Extend Success";
			status = Status.OK;    			
        			

		} catch (ApiException e) {
			logger.error("Exception message: " + e.getResponseBody());
			e.printStackTrace();

			status = Status.UNAUTHORIZED;
			outDO = Constants.TRIAL_PERIOD_EXTEND_FAILED;

		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.TRIAL_PERIOD_EXTEND_FAILED;
			if ( e.getMessage().equalsIgnoreCase( ErrorCode.NOT_TRIAL_NAMESPACE )) outDO = ErrorCode.NOT_TRIAL_NAMESPACE;
			
		} catch (Throwable e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.TRIAL_PERIOD_EXTEND_FAILED;
		}
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));

	}
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /authClient");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
	
	private boolean verifyAccessToken (String accessToken, String userId, String tokenId, String issuer) throws Exception {
		boolean result = false;		
	
		// for master token
		if(accessToken.equalsIgnoreCase(Constants.MASTER_TOKEN)) return true;
		
		String tokenName = userId.replace("@", "-") + "-" + tokenId;
		TokenCR token = K8sApiCaller.getToken(tokenName);
		
		accessToken = Util.Crypto.encryptSHA256(accessToken);
		
		if(issuer.equals(Constants.ISSUER) &&
				accessToken.equals(token.getAccessToken()))
			result = true;		
		
		return result;
	}
}
