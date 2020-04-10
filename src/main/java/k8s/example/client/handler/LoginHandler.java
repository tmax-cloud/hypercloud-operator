package k8s.example.client.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
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
import k8s.example.client.Constants;
import k8s.example.client.DataObject.Client;
import k8s.example.client.DataObject.CommonOutDO;
import k8s.example.client.DataObject.LoginInDO;
import k8s.example.client.DataObject.Token;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.k8s.OAuthApiCaller;

public class LoginHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /login");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
   
		LoginInDO loginInDO = null;
		String outDO = null;
		IStatus status = null;
		StringBuilder sb = null;
		String clientIdRequestParameter = null;
		String appNameRequestParameter = null;
		String accessToken = null;
		String refreshToken = null;

			try {
				// Read inDO
	    		loginInDO = new ObjectMapper().readValue(body.get( "postData" ), LoginInDO.class);
	    		logger.info( "  User ID: " + loginInDO.getId() );
	    		logger.info( "  User Password: " + loginInDO.getPassword() );
	    		
	    		// Validate
	    		if (loginInDO.getId() == null ) 	throw new Exception(ErrorCode.USER_ID_EMPTY);
	    		if (loginInDO.getPassword() == null ) 	throw new Exception(ErrorCode.USER_PASSWORD_EMPTY);
	    		
	    		// Get Client ID
	    		if (session.getParameters().get("clientId") != null ) clientIdRequestParameter = session.getParameters().get("clientId").get(0);
	    		if (session.getParameters().get("appName") != null ) appNameRequestParameter = session.getParameters().get("appName").get(0);
	        	if (session.getParameters().get("clientId") != null && session.getParameters().get("appName") != null) {
	        		logger.info( "  Client Id: " + clientIdRequestParameter );
		    		logger.info( "  App Name: " + appNameRequestParameter );  
	        	}
	    				   		
	        	if (System.getenv( "PROAUTH_EXIST" ) != null) {   // 그대로 아이디 비번 구분 X		//TODO
	        		if( System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1")) {
	        			
	    	    		logger.info( "  [[ Integrated OAuth System! ]] " );
	        			// Login to ProAuth & Get Token
	    	    		JsonObject loginOut = OAuthApiCaller.AuthenticateCreate(loginInDO.getId(), loginInDO.getPassword());
	    	    		logger.info( "  loginOut.get(\"result\") : " + loginOut.get("result").toString() );

	    	    		if ( loginOut.get("result").toString().equalsIgnoreCase("\"true\"") ){
	    	    			accessToken = loginOut.get("token").toString().replaceAll("\"", ""); 
		    	    		refreshToken = loginOut.get("refresh_token").toString().replaceAll("\"", "");
		    	    		logger.info( "  accessToken : " + accessToken );
		    	    		logger.info( "  refreshToken : " + refreshToken );	
		    	    		status = Status.OK; 
	    	    		} else {
	    	    			logger.info("  Login failed by ProAuth.");		    			
			    			status = Status.OK; //ui요청
			    			outDO = Constants.LOGIN_FAILED;
	    	    		}
	    	    		
	        		}
	    	    } 
	        	if (System.getenv( "PROAUTH_EXIST" ) == null || !System.getenv( "PROAUTH_EXIST" ).equalsIgnoreCase("1") ){	
	        		// 그대로 아이디 비번 오류 구분O  //TODO
	        		// TODO 추가 요건: 동일 계정에 대해서 10번 로그인 실패시, 계정 잠금 --> 무조건 비밀번호 재설정  ( 4/30 요건 )
	        		
	        		logger.info( "  [[ OpenAuth System! ]]" );  			
		    		// Get user info
		    		String userId = loginInDO.getId().replace("@", "-");
		    		UserCR user = K8sApiCaller.getUser(userId);
		    		String encryptedPassword = Util.Crypto.encryptSHA256(loginInDO.getPassword() + loginInDO.getId() + user.getUserInfo().getPasswordSalt());
		    		logger.info("  DB password: " + user.getUserInfo().getPassword() + " / Input password: " + encryptedPassword);
		    		
		    		if(user.getUserInfo().getPassword().equals(encryptedPassword)) {
		    			logger.info(" Login success ");
		    			
		    			status = Status.OK;
		    			
		    			// Make token & refresh token
		    			String tokenId = UUID.randomUUID().toString();
		    			
		    			Builder tokenBuilder = JWT.create().withIssuer(Constants.ISSUER)
								.withExpiresAt(Util.getDateFromSecond(Constants.ACCESS_TOKEN_EXP_TIME))
								.withClaim(Constants.CLAIM_USER_ID, loginInDO.getId())
		    					.withClaim(Constants.CLAIM_TOKEN_ID, tokenId);
		    			
		    			if ( K8sApiCaller.verifyAdmin(loginInDO.getId()) ) {
		    				logger.info("ADMIN!!!");
		    				tokenBuilder.withClaim( Constants.CLAIM_ROLE, Constants.ROLE_ADMIN );
		    			} else {
		    				logger.info("USER!!!");
		    				tokenBuilder.withClaim( Constants.CLAIM_ROLE, Constants.ROLE_USER );
		    			}
		    			
		    			accessToken = tokenBuilder.sign(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY));
		    			tokenBuilder = JWT.create().withIssuer(Constants.ISSUER)
		    					.withExpiresAt(Util.getDateFromSecond(Constants.REFRESH_TOKEN_EXP_TIME));
		    			refreshToken = tokenBuilder.sign(Algorithm.HMAC256(Constants.REFRESH_TOKEN_SECRET_KEY));
	
		    			// Save tokens in token CR
		            	K8sApiCaller.saveToken(userId, tokenId, Util.Crypto.encryptSHA256(accessToken), Util.Crypto.encryptSHA256(refreshToken));
		    		} else {
		    			logger.info("  Login fail. Wrong password.");
		    			
		    			status = Status.OK; //ui요청
		    			outDO = Constants.LOGIN_FAILED;
		    		}
	        	}
	    		
            	// Get Redirect URI if Exists
            	if ( clientIdRequestParameter != null && appNameRequestParameter != null ) {
        			logger.info(" Login from Client~! Return Redirect URI together ");
            		Client clientInfo  = new Client();
            		clientInfo.setAppName(appNameRequestParameter);
            		clientInfo.setClientId(clientIdRequestParameter); 		
            		Client dbClientInfo = K8sApiCaller.getClient(clientInfo);
            		
        			logger.info("  App Name : " + dbClientInfo.getAppName());
        			logger.info("  Origin URI : " + dbClientInfo.getOriginUri());
        			logger.info("  Redirect URI : " + dbClientInfo.getRedirectUri());
            		
        			// Validate
            		if( !clientInfo.getClientId().equalsIgnoreCase( dbClientInfo.getClientId()) ) throw new Exception( ErrorCode.CLIENT_ID_MISMATCH );

            		// Make Client outDO
            		sb = new StringBuilder();
            		sb.append( dbClientInfo.getRedirectUri() );
            		sb.append( "?at=" + accessToken );
            		sb.append( "&rt=" + refreshToken );
        			logger.info("Redirect URI : " + sb.toString());
        			outDO = sb.toString();
            	} else {
            		// Make outDO
        			Token loginOutDO = new Token();
        			loginOutDO.setAccessToken(accessToken);
        			loginOutDO.setRefreshToken(refreshToken);
        			logger.info("  Access token: " + accessToken);
        			logger.info("  Refresh token: " + refreshToken);
        			
        			Gson gson = new GsonBuilder().setPrettyPrinting().create();
        			outDO = gson.toJson(loginOutDO).toString();
            	}		   			
	    		 
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
				//Make OutDO
				CommonOutDO out = new CommonOutDO();
				out.setStatus(Constants.LOGIN_SUCCESS);
				out.setMsg(outDO);
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				outDO = gson.toJson(out).toString();
				
			} else if ( status.equals(Status.OK) && outDO.equals(Constants.LOGIN_FAILED)) { 
				CommonOutDO out = new CommonOutDO();
				out.setMsg(outDO);
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				outDO = gson.toJson(out).toString();
			}	
			
 		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
    }
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /login");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
}