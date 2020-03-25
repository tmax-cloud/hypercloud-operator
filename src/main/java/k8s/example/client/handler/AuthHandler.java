package k8s.example.client.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.DataObject.TokenReview;
import k8s.example.client.DataObject.TokenReviewStatus;
import k8s.example.client.DataObject.TokenReviewUser;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;

public class AuthHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		//logger.info("***** POST /authenticate");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		String response = null;
		boolean authResult = false;
		try {			
			// Get token
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse( body.get( "postData" ) );
			String token = element.getAsJsonObject().get( "spec" ).getAsJsonObject().get( "token" ).getAsString();
			
			//logger.info( "  Token: " + token );
			if ( !token.isEmpty() && token.equals( Constants.MASTER_TOKEN )) return Util.setCors( NanoHTTPD.newFixedLengthResponse( createAuthResponse( true, Constants.MASTER_USER_ID ) ) );
			
			// Verify access token	
			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY)).build();
			DecodedJWT jwt = verifier.verify(token);
			
			String issuer = jwt.getIssuer();
			String userId = jwt.getClaims().get(Constants.CLAIM_USER_ID).asString();
			String tokenId = jwt.getClaims().get(Constants.CLAIM_TOKEN_ID).asString();
			logger.info( "  Issuer: " + issuer );
			logger.info( "  User ID: " + userId );
			logger.info( "  Token ID: " + tokenId );
			
			if(verifyAccessToken(token, userId, tokenId, issuer)) {
				logger.info( "  Authentication success" );
				authResult = true;
			} else {
				logger.info( "  Authentication fail" );
				authResult = false;
			}
			
			response = createAuthResponse( authResult, userId );
		} catch (Exception e) {
			//logger.info("Exception message: " + e.getMessage());
			e.printStackTrace();
			authResult = false;
		}
		
		//logger.info();
		return Util.setCors(NanoHTTPD.newFixedLengthResponse( response ));

    }
	
	private boolean verifyAccessToken (String accessToken, String userId, String tokenId, String issuer) throws Exception {
		boolean result = false;		

		String tokenName = userId.replace("@", "-") + "-" + tokenId;
		TokenCR token = K8sApiCaller.getToken(tokenName);
		
		accessToken = Util.Crypto.encryptSHA256(accessToken);
		
		if(issuer.equals(Constants.ISSUER) &&
				accessToken.equals(token.getAccessToken()))
			result = true;
		
		return result;
	}
	
	private String createAuthResponse( boolean authResult, String userId ) {
		
		/*
		 * RESPONSE TRUE EXAMPLE
		 * {
			  "apiVersion": "authentication.k8s.io/v1beta1",
			  "kind": "TokenReview",
			  "status": {
			    "authenticated": true,
			    "user": {
			      "username": "seonho",
			      "uid": "9999",
			      "groups": [
			        "developers",
			        "qa"
			      ],
			      "extra": {
			        "extrafield1": [
			          "extravalue1",
			          "extravalue2"
			        ]
			      }
			    }
			  }
			}
		 */
		
		/*
		 * RESPONSE FALSE EXAMPLE
		 * {
			  "apiVersion": "authentication.k8s.io/v1beta1",
			  "kind": "TokenReview",
			  "status": {
			    "authenticated": false
			  }
			}
		 */
		
		TokenReview tr = new TokenReview();
		
		TokenReviewStatus trStatus = new TokenReviewStatus();
		trStatus.setAuthenticated(authResult);
		
		if(authResult) {
			TokenReviewUser trUser = new TokenReviewUser();
			trUser.setUsername(userId);
//			trUser.setUid("uid-xxxx");
			
			trStatus.setUser(trUser);
		} 
		
		tr.setStatus(trStatus);
		
		Gson gson = new Gson();
		String response = gson.toJson( tr );
		
		logger.info( "  Response: " + response );
		
		return response;
	}
}