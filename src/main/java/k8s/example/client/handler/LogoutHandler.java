package k8s.example.client.handler;

import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import io.kubernetes.client.openapi.ApiException;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.Token;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;

public class LogoutHandler extends GeneralHandler {
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		System.out.println("***** POST /logout");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        Token logoutInDO = null;
		String outDO = null;
		IStatus status = null;
		try {
			// Read inDO
			logoutInDO = new ObjectMapper().readValue(body.get( "postData" ), Token.class);
			String accessToken = logoutInDO.getAccessToken();
    		System.out.println( "  Token: " + accessToken );
    		
    		// Verify access token	
			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY)).build();
			DecodedJWT jwt = verifier.verify(accessToken);
			
			String issuer = jwt.getIssuer();
			String userId = jwt.getClaims().get(Constants.CLAIM_USER_ID).asString();
			String tokenId = jwt.getClaims().get(Constants.CLAIM_TOKEN_ID).asString();
			System.out.println( "  Issuer: " + issuer );
			System.out.println( "  User ID: " + userId );
			System.out.println( "  Token ID: " + tokenId );
			
			if(verifyAccessToken(accessToken, userId, tokenId, issuer)) {
				status = Status.OK;
				
				String tokenName = userId.replace("@", "-") + "-" + tokenId;
				System.out.println( "  Logout success." );
				K8sApiCaller.deleteToken(tokenName);
				outDO = "Logout success.";
			} else {
				System.out.println( "  Token is not valid" );
				status = Status.UNAUTHORIZED;
				outDO = "Logout fail. Token is not valid.";
			}
		} catch (ApiException e) {
			System.out.println( "Exception message: " + e.getMessage() );
			
			if (e.getResponseBody().contains("NotFound")) {
				System.out.println( "  Logout fail. Token not exist." );
				status = Status.UNAUTHORIZED;
				outDO = "Logout failed. Token not exist.";
			} else {
				System.out.println( "Response body: " + e.getResponseBody() );
				e.printStackTrace();
				
				status = Status.UNAUTHORIZED;
				outDO = "Logout failed. Exception occurs.";
			}
		} catch (Exception e) {
			System.out.println( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			
			status = Status.UNAUTHORIZED;
			outDO = "Logout failed. Exception occurs.";
		}
		
		System.out.println();
		Response resp = NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO);
		resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Max-Age", "3628800");
        resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        resp.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
        resp.addHeader("Access-Control-Allow-Headers", "Authorization");
		return resp;
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
}