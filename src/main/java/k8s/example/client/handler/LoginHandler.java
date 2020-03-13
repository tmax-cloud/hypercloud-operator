package k8s.example.client.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
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
import k8s.example.client.DataObject.LoginInDO;
import k8s.example.client.DataObject.Token;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;

public class LoginHandler extends GeneralHandler {
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		System.out.println("***** POST /login");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		LoginInDO loginInDO = null;
		String outDO = null;
		IStatus status = null;
		try {
			// Read inDO
    		loginInDO = new ObjectMapper().readValue(body.get( "postData" ), LoginInDO.class);
    		System.out.println( "  User ID: " + loginInDO.getId() );
    		
    		// Get user info
    		String userId = loginInDO.getId().replace("@", "-");
    		UserCR user = K8sApiCaller.getUser(userId);
    		String encryptedPassword = Util.Crypto.encryptSHA256(loginInDO.getPassword() + loginInDO.getId() + user.getUserInfo().getPasswordSalt());
    		System.out.println("  DB password: " + user.getUserInfo().getPassword() + " / Input password: " + encryptedPassword);
    		
    		if(user.getUserInfo().getPassword().equals(encryptedPassword)) {
    			System.out.println("  Login success");
    			
    			status = Status.OK;
    			
    			// Make token & refresh token
    			String tokenId = UUID.randomUUID().toString();
    			
    			Builder tokenBuilder = JWT.create().withIssuer(Constants.ISSUER)
						.withExpiresAt(Util.getDateFromSecond(Constants.ACCESS_TOKEN_EXP_TIME))
						.withClaim(Constants.CLAIM_USER_ID, loginInDO.getId())
    					.withClaim(Constants.CLAIM_TOKEN_ID, tokenId) ;
    			String accessToken = tokenBuilder.sign(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY));
    			
    			tokenBuilder = JWT.create().withIssuer(Constants.ISSUER)
    					.withExpiresAt(Util.getDateFromSecond(Constants.REFRESH_TOKEN_EXP_TIME));
    			String refreshToken = tokenBuilder.sign(Algorithm.HMAC256(Constants.REFRESH_TOKEN_SECRET_KEY));

    			// Save tokens in token CR
            	K8sApiCaller.saveToken(userId, tokenId, Util.Crypto.encryptSHA256(accessToken), Util.Crypto.encryptSHA256(refreshToken));
    			
    			// Make outDO
    			Token loginOutDO = new Token();
    			loginOutDO.setAccessToken(accessToken);
    			loginOutDO.setRefreshToken(refreshToken);
    			System.out.println("  Access token: " + accessToken);
    			System.out.println("  Refresh token: " + refreshToken);
    			
    			Gson gson = new GsonBuilder().setPrettyPrinting().create();
    			outDO = gson.toJson(loginOutDO).toString();
    		} else {
    			System.out.println("  Login fail. Wrong password.");
    			
    			status = Status.UNAUTHORIZED;
    			outDO = "Login failed. Wrong password.";
    		}
		} catch (ApiException e) {
			System.out.println( "Exception message: " + e.getMessage() );
			
			if (e.getResponseBody().contains("NotFound")) {
				System.out.println( "  Login fail. User not exist." );
				status = Status.UNAUTHORIZED;
				outDO = "Login failed. User not exist.";
			} else {
				System.out.println( "Response body: " + e.getResponseBody() );
				e.printStackTrace();
				
				status = Status.UNAUTHORIZED;
				outDO = "Login failed. Exception occurs.";
			}
		} catch (Exception e) {
			System.out.println( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			
			status = Status.UNAUTHORIZED;
			outDO = "Login failed. Exception occurs.";
		}
		
		System.out.println();
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
    }
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		System.out.println("***** OPTIONS /login");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
}