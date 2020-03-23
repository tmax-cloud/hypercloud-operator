package k8s.example.client.handler;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import io.kubernetes.client.openapi.ApiException;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.Client;
import k8s.example.client.ErrorCode;
import k8s.example.client.StringUtil;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.models.BrokerResponse;

public class LoginPageHandler extends GeneralHandler {
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		System.out.println("***** POST /loginPage");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        Client clientInfo = null;
		String outMsg = null;
		IStatus status = null;
		try {
			// Read inDO
			clientInfo = new ObjectMapper().readValue(body.get( "postData" ), Client.class);
			
			// Verify Input Values
			if( StringUtil.isEmpty(clientInfo.getAppName()))	throw new Exception(ErrorCode.APP_NAME_EMPTY);
			if( StringUtil.isEmpty(clientInfo.getClientId()))	throw new Exception(ErrorCode.CLIENT_ID_EMPTY);
			if( StringUtil.isEmpty(clientInfo.getClientSecret()))	throw new Exception(ErrorCode.CLIENT_SECRET_EMPTY);
			
    		System.out.println( "  Client Id: " + clientInfo.getClientId() );
    		System.out.println( "  Client Secret: " + clientInfo.getClientSecret() );

			// Get clients info from K8S 		
    		Client dbClientInfo = K8sApiCaller.getClient(clientInfo);
    		
    		// Validate
    		System.out.println( "  clientInfo.getClientSecret(): " + clientInfo.getClientSecret() );
    		System.out.println( "  dbClientInfo.getClientSecret(): " + dbClientInfo.getClientSecret() );
    		if( !clientInfo.getClientId().equalsIgnoreCase( dbClientInfo.getClientId()) ) throw new Exception( ErrorCode.CLIENT_ID_MISMATCH );
    		if( !clientInfo.getClientSecret().equalsIgnoreCase( dbClientInfo.getClientSecret()) ) throw new Exception( ErrorCode.CLIENT_SECRET_MISMATCH );
    		

			// Make outDO					
			StringBuilder sb = new StringBuilder();
			sb.append( Constants.LOGIN_PAGE_URI );
			sb.append( "?clientId=" + clientInfo.getClientId() );
			sb.append( "&clientSecret=" + clientInfo.getClientSecret() );
    		System.out.println( "Login Page URI : " + sb.toString() );
			status = Status.OK;
			outMsg = sb.toString();			

		} catch (ApiException e) {
			System.out.println( "Exception message: " + e.getMessage() );
			outMsg = "LogIn page Create failed.";
			status = Status.BAD_REQUEST;

		} catch (Exception e) {
			System.out.println( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			outMsg = "LogIn page Create failed.";
			status = Status.BAD_REQUEST;
		}
		
		System.out.println();
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outMsg));
    }
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		System.out.println("***** OPTIONS /authClient");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }

}
