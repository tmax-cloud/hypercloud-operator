package k8s.example.client.handler;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import k8s.example.client.Constants;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.DataObject.User;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.metering.util.SimpleUtil;
import k8s.example.client.models.NamespaceClaim;
import k8s.example.client.models.NamespaceClaimList;

public class NameSpaceClaimHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response get(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** GET /nameSpaceClaim");
		
		IStatus status = null;
		List < NamespaceClaim > nscItems = null;
		NamespaceClaimList nscList = null;
		String outDO = null; 
		String accessToken = null;
		String userId = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_USER_ID );
		// if limit exists
		String limit = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_LIMIT );
		
		//if label selector exists
		String labelSelector = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_LABEL_SELECTOR );
		
		//if contunue exists
		String _continue = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_CONTINUE ); // 

		try {
			// Read AccessToken from Header
			if(!session.getHeaders().get("authorization").isEmpty()) {
				accessToken = session.getHeaders().get("authorization");
			} else {
				status = Status.BAD_REQUEST;
				throw new Exception(ErrorCode.TOKEN_EMPTY);
			}
    		logger.debug( "  Token: " + accessToken );
			nscList = K8sApiCaller.getAccessibleNSC(accessToken, userId, labelSelector, _continue);
			status = Status.OK;

			// Limit 
			if( nscList!= null && nscList.getItems() != null) {

				if( limit != null ) {
					nscItems = nscList.getItems();
					nscItems =  nscItems.stream().limit(Integer.parseInt(limit)).collect(Collectors.toList());		
					nscList.setItems(nscItems);
				}
			}	
    			
			// Make outDO
			if( (nscList!=null && nscList.getItems() != null && nscList.getItems().size() > 0)
					|| nscList.getMetadata().getContinue().equalsIgnoreCase("wrongLabelorNoResource")) {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				nscList.getMetadata().setContinue(null);
				outDO = gson.toJson( nscList ).toString();
			} else {
    			status = Status.FORBIDDEN;
    			JsonObject result = new JsonObject();
    			outDO = "Cannot Access Any NameSpaceClaim";
    			result.addProperty("message", outDO);
    			Gson gson = new Gson();		
    		    outDO = gson.toJson(result);		
    		}
			
		} catch (ApiException e) {
			logger.error( "Exception message: " + e.getMessage() );
			outDO = "Get NameSpaceClaim List failed.";
			status = Status.BAD_REQUEST;
			e.printStackTrace();

		} catch (Exception e) {
			logger.error( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			outDO = "Get NameSpaceClaim List failed.";
			status = Status.BAD_REQUEST;
		}
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, "application/json", outDO));
    }
	
	public Response put(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** PUT /nameSpaceClaim");

		IStatus status = null;
		String outDO = null;
		UserCR userCR = null;
		User userInDO = null;

		Map<String, String> body = new HashMap<String, String>();
		try {
			session.parseBody(body);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// if updateMode exists
		String mode = SimpleUtil.getQueryParameter(session.getParameters(), Constants.QUERY_PARAMETER_MODE);
		String nsName = SimpleUtil.getQueryParameter(session.getParameters(), Constants.QUERY_PARAMETER_NAMESPACE);

		try {
			String bodyStr = readFile(body.get("content"), Integer.valueOf(session.getHeaders().get("content-length")));
			logger.debug("Body: " + bodyStr);
			userInDO = new ObjectMapper().readValue(bodyStr, User.class);

		} catch (Exception e) {
			e.printStackTrace();
		}

		switch (mode) {
		case "namespace":
			logger.info("  Namespace Name Duplication Verify Service Start");
			logger.info("  Namespace: " + nsName);
			try {
				// Validate
				if (nsName == null)
					throw new Exception(ErrorCode.NAMESPACE_NAME_EMPTY);

				// Check ID, Email Duplication
				try {
					V1Namespace namespace = K8sApiCaller.getNameSpace(nsName);
					throw new Exception(ErrorCode.NAMESPACE_NAME_DUPLICATED);

				} catch( ApiException e) {
					if(e.getResponseBody().contains("NotFound")) {
						status = Status.OK;
						outDO = Constants.NAMESPACE_NAME_DUPLICATION_VERIFY_SUCCESS;
					}
				}

			} catch (ApiException e) {
				logger.error("Exception message: " + e.getResponseBody());
				logger.error("Exception message: " + e.getMessage());
				status = Status.BAD_REQUEST;
				outDO = Constants.NAMESPACE_NAME_DUPLICATION_VERIFY_FAILED;

			} catch (Exception e) {
				logger.error("Exception message: " + e.getMessage());
				e.printStackTrace();
				status = Status.BAD_REQUEST;
				outDO = Constants.NAMESPACE_NAME_DUPLICATION_VERIFY_FAILED;
				
				if(e.getMessage().contains(ErrorCode.NAMESPACE_NAME_DUPLICATED)) {
					status = Status.FORBIDDEN;
					outDO = ErrorCode.NAMESPACE_NAME_DUPLICATED;
				}
			}
			break;

		}

		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));

	}

	private String readFile(String path, Integer length) {
		Charset charset = Charset.defaultCharset();
		String bodyStr = "";
		int byteCount;
		try {
			ByteBuffer buf = ByteBuffer.allocate(Integer.valueOf(length));
			FileInputStream fis = new FileInputStream(path);
			FileChannel dest = fis.getChannel();

			while (true) {
				byteCount = dest.read(buf);
				if (byteCount == -1) {
					break;
				} else {
					buf.flip();
					bodyStr += charset.decode(buf).toString();
					buf.clear();
				}
			}
			dest.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bodyStr;
	}
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /authClient");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
	
}
