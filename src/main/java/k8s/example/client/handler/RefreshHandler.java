package k8s.example.client.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
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
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.DataObject.Token;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;

public class RefreshHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /refresh");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Token refreshInDO = null;
		Token refreshOutDO = null;
		String outDO = null;
		IStatus status = null;
		int atExpireTimeSec = 0;
		try {
			// Read inDO
			refreshInDO = new ObjectMapper().readValue(body.get( "postData" ), Token.class);
			logger.info( "  Access token: " + refreshInDO.getAccessToken() );
    		logger.info( "  Refresh token: " + refreshInDO.getRefreshToken() );
    		
    		// Get token name
    		DecodedJWT jwt = JWT.decode(refreshInDO.getAccessToken());
    		String userId = jwt.getClaim(Constants.CLAIM_USER_ID).asString();
    		String tokenId = jwt.getClaim(Constants.CLAIM_TOKEN_ID).asString();
    		logger.info( "  User ID: " + userId );
    		logger.info( "  Token ID: " + tokenId );
    		String tokenName = userId.replace("@", "-") + "-" + tokenId;
    		
			// Verify refresh token	
			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(Constants.REFRESH_TOKEN_SECRET_KEY)).build();
			try {
				jwt = verifier.verify(refreshInDO.getRefreshToken());
			} catch (Exception e) {
				logger.info( "Exception message: " + e.getMessage() );
				K8sApiCaller.deleteToken(tokenName);
			}

			String issuer = jwt.getIssuer();
			logger.info( "  Issuer: " + issuer );
			
			if(verifyRefreshToken(refreshInDO.getAccessToken(), refreshInDO.getRefreshToken(), tokenName, issuer)) {
				logger.info( "  Refresh success" );	
				status = Status.OK;
				
				// Make a new access token
				atExpireTimeSec = (refreshInDO.getAtExpireTime() == 0)?  Constants.ACCESS_TOKEN_EXP_TIME : refreshInDO.getAtExpireTime() * 60;
				logger.info( "  AT Expire Time : " +  atExpireTimeSec/60  + " min");	
				
				Builder tokenBuilder = JWT.create().withIssuer(Constants.ISSUER)
						.withExpiresAt(Util.getDateFromSecond(atExpireTimeSec))
						.withClaim(Constants.CLAIM_USER_ID, userId)
						.withClaim(Constants.CLAIM_TOKEN_ID, tokenId);
				
				// TODO
    			if ( userId.equals( Constants.MASTER_USER_ID ) ) {
    				tokenBuilder.withClaim( Constants.CLAIM_ROLE, Constants.ROLE_ADMIN );
    			} else {
    				tokenBuilder.withClaim( Constants.CLAIM_ROLE, Constants.ROLE_USER );
    			}
    			
    			String newAccessToken = tokenBuilder.sign(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY));
    			logger.info( "  New access token: " + newAccessToken );
    			
    			// Make outDO
    			refreshOutDO = new Token();
    			refreshOutDO.setAccessToken(newAccessToken);
    			refreshOutDO.setAtExpireTime(refreshInDO.getAtExpireTime());
    			Gson gson = new GsonBuilder().setPrettyPrinting().create();
    			outDO = gson.toJson(refreshOutDO).toString();
    			
    			// Update access token
    			K8sApiCaller.updateAccessToken(tokenName, Util.Crypto.encryptSHA256(newAccessToken));
			} else {
				logger.info( "  Refresh fail" );
				status = Status.UNAUTHORIZED;
			}
		} catch (Exception e) {
			logger.info( "  Refresh fail" );
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			
			status = Status.UNAUTHORIZED;
		}
		
//		logger.info();
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
	}
	
	private boolean verifyRefreshToken(String accessToken, String refreshToken, String tokenName, String issuer) throws Exception {
		boolean result = false;		

		TokenCR token = K8sApiCaller.getToken(tokenName);
		
		accessToken = Util.Crypto.encryptSHA256(accessToken);
		refreshToken = Util.Crypto.encryptSHA256(refreshToken);
		
		logger.info("  In AccessToken : " + accessToken);
		logger.info("  DB AccessToken : " + token.getAccessToken());
		
		logger.info("  In RefreshToken : " + refreshToken);
		logger.info("  DB RefreshToken : " + token.getRefreshToken());
		
		if(issuer.equals(Constants.ISSUER) &&
				accessToken.equals(token.getAccessToken()) &&
				refreshToken.equals(token.getRefreshToken()))
			result = true;
		
		return result;
	}
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /refresh");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
}