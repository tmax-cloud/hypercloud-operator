package k8s.example.client.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import io.kubernetes.client.openapi.ApiException;
import k8s.example.client.Constants;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.DataObject.Client;
import k8s.example.client.DataObject.CommonOutDO;
import k8s.example.client.DataObject.LoginInDO;
import k8s.example.client.DataObject.Token;
import k8s.example.client.DataObject.User;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.k8s.OAuthApiCaller;

public class UserHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /User");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        List <UserCR> userCRList = null;
		User userInDO = null;
		String outDO = null;
		IStatus status = null;
		StringBuilder sb = null;
		
		try {
			// Read inDO
			userInDO = new ObjectMapper().readValue(body.get( "postData" ), User.class);
    		logger.info( "  User ID: " + userInDO.getId() );
    		logger.info( "  User Name: " + userInDO.getName() );
    		logger.info( "  User E-Mail: " + userInDO.getEmail() );
    		logger.info( "  User PassWord: " + userInDO.getPassword() );
    		
    		// Validate
    		if (userInDO.getId() == null ) 	throw new Exception(ErrorCode.USER_ID_EMPTY);
    		if (userInDO.getEmail() == null ) 	throw new Exception(ErrorCode.USER_MAIL_EMPTY);
    		if (userInDO.getPassword() == null ) 	throw new Exception(ErrorCode.USER_PASSWORD_EMPTY);
    		
    		// Check ID, Email Duplication
    		userCRList = K8sApiCaller.listUser();
    		if ( userCRList != null ) {
        		for(UserCR userCR : userCRList) {
        			User user = userCR.getUserInfo();
        			if ( user.getName().equalsIgnoreCase(userInDO.getId())) throw new Exception(ErrorCode.USER_ID_DUPLICATED);  // 주의 : 회원가입 시 받은 ID를 k8s에는 Name으로 넣자
        			if ( user.getEmail().equalsIgnoreCase(userInDO.getEmail())) throw new Exception(ErrorCode.USER_MAIL_DUPLICATED);
        		}
    		}
    		
    		JsonArray userAuthList = OAuthApiCaller.ListUser();
    		if ( userAuthList != null ) {
    			for (JsonElement userAuth : userAuthList) {
    				if (userAuth.getAsJsonObject().get("user_id").toString().equalsIgnoreCase(userInDO.getId())) throw new Exception(ErrorCode.USER_ID_DUPLICATED);
    			}
    		}
    		
    		// UserCRD Create
    		K8sApiCaller.createUser(userInDO);
    		
    		// Call UserCreate to ProAuth
    		OAuthApiCaller.createUser(userInDO);
    		
    		// Send E-mail to User
    		//TODO
    		
    		
		} catch (ApiException e) {
			logger.info( "Exception message: " + e.getResponseBody() );
			logger.info( "Exception message: " + e.getMessage() );
			
			if (e.getResponseBody().contains("NotFound")) {
				logger.info( "  Login fail. User not exist." );
				status = Status.OK; //ui요청
				outDO = Constants.LOGIN_FAILED;
			} else {
				logger.info( "Response body: " + e.getResponseBody() );
				e.printStackTrace();
				
				status = Status.UNAUTHORIZED;
				outDO = Constants.LOGIN_FAILED;
			}
		} catch (Exception e) {
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			
			status = Status.UNAUTHORIZED;
			outDO = Constants.LOGIN_FAILED;
		}
		
		if (status.equals(Status.UNAUTHORIZED)) {
			CommonOutDO out = new CommonOutDO();
			out.setMsg(outDO);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			outDO = gson.toJson(out).toString();
		} else if ( status.equals(Status.OK) && outDO.equals(Constants.LOGIN_FAILED)) { //ui요청
			CommonOutDO out = new CommonOutDO();
			out.setMsg(outDO);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			outDO = gson.toJson(out).toString();
		}
 
//		logger.info();
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
    }
	

	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /login");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
}