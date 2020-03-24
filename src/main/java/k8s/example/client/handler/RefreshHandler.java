package k8s.example.client.handler;

import java.util.HashMap;
import java.util.Map;

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
import k8s.example.client.DataObject.Token;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;

public class RefreshHandler extends GeneralHandler {
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		System.out.println("***** POST /refresh");
		
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
		try {
			// Read inDO
			refreshInDO = new ObjectMapper().readValue(body.get( "postData" ), Token.class);
			System.out.println( "  Access token: " + refreshInDO.getAccessToken() );
    		System.out.println( "  Refresh token: " + refreshInDO.getRefreshToken() );
    		
    		// Get token name
    		DecodedJWT jwt = JWT.decode(refreshInDO.getAccessToken());
    		String userId = jwt.getClaim(Constants.CLAIM_USER_ID).asString();
    		String tokenId = jwt.getClaim(Constants.CLAIM_TOKEN_ID).asString();
    		System.out.println( "  User ID: " + userId );
    		System.out.println( "  Token ID: " + tokenId );
    		String tokenName = userId.replace("@", "-") + "-" + tokenId;
    		
			// Verify refresh token	
			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(Constants.REFRESH_TOKEN_SECRET_KEY)).build();
			try {
				jwt = verifier.verify(refreshInDO.getRefreshToken());
			} catch (Exception e) {
				System.out.println( "Exception message: " + e.getMessage() );
				K8sApiCaller.deleteToken(tokenName);
			}

			String issuer = jwt.getIssuer();
			System.out.println( "  Issuer: " + issuer );
			
			if(verifyRefreshToken(refreshInDO.getAccessToken(), refreshInDO.getRefreshToken(), tokenName, issuer)) {
				System.out.println( "  Refresh success" );	
				status = Status.OK;
				
				// Make a new access token
				Builder tokenBuilder = JWT.create().withIssuer(Constants.ISSUER)
						.withExpiresAt(Util.getDateFromSecond(Constants.ACCESS_TOKEN_EXP_TIME))
						.withClaim(Constants.CLAIM_USER_ID, userId)
						.withClaim(Constants.CLAIM_TOKEN_ID, tokenId);
    			String newAccessToken = tokenBuilder.sign(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY));
    			System.out.println( "  New access token: " + newAccessToken );
    			
    			// Make outDO
    			refreshOutDO = new Token();
    			refreshOutDO.setAccessToken(newAccessToken);
    			Gson gson = new GsonBuilder().setPrettyPrinting().create();
    			outDO = gson.toJson(refreshOutDO).toString();
    			
    			// Update access token
    			K8sApiCaller.updateAccessToken(tokenName, Util.Crypto.encryptSHA256(newAccessToken));
			} else {
				System.out.println( "  Refresh fail" );
				status = Status.UNAUTHORIZED;
			}
		} catch (Exception e) {
			System.out.println( "  Refresh fail" );
			System.out.println( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			
			status = Status.UNAUTHORIZED;
		}
		
		System.out.println();
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
	}
	
	private boolean verifyRefreshToken(String accessToken, String refreshToken, String tokenName, String issuer) throws Exception {
		boolean result = false;		

		TokenCR token = K8sApiCaller.getToken(tokenName);
		
		accessToken = Util.Crypto.encryptSHA256(accessToken);
		refreshToken = Util.Crypto.encryptSHA256(refreshToken);
		
		if(issuer.equals(Constants.ISSUER) &&
				accessToken.equals(token.getAccessToken()) &&
				refreshToken.equals(token.getRefreshToken()))
			result = true;
		
		return result;
	}
	
	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		System.out.println("***** OPTIONS /refresh");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
}