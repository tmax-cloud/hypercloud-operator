package k8s.example.client.metering;

import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import k8s.example.client.Constants;
import k8s.example.client.Util;

public class Test {

	public static void main(String[] args) throws Exception {
//		// Verify access token	
//		String accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiY2x1c3Rlci1hZG1pbiIsInRva2VuSWQiOiJ3b29AdG1heC5jby5rciIsImlzcyI6IlRtYXgtUHJvQXV0aC1XZWJIb29rIiwiaWQiOiJhZG1pbkB0bWF4LmNvLmtyIiwiZXhwIjoxNzQzMzAwODgzfQ.kCOP4IjbeHS53tTY7z55E2aUPkrpQjDFk-Qhnc6Rgeo";
//		JWTVerifier verifier = JWT.require(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY)).build();
//		DecodedJWT jwt = verifier.verify(accessToken);
//		
//		String issuer = jwt.getIssuer();
//		String userId = jwt.getClaims().get(Constants.CLAIM_USER_ID).asString();
//		String tokenId = jwt.getClaims().get(Constants.CLAIM_TOKEN_ID).asString();
//		System.out.println( "  Issuer: " + issuer );
//		System.out.println( "  User ID: " + userId );
//		System.out.println( "  Token ID: " + tokenId );
		
//		Builder tokenBuilder = JWT.create().withIssuer(Constants.ISSUER)
//				.withExpiresAt(Util.getDateFromSecond(157680000)).withClaim(Constants.CLAIM_TOKEN_ID, "woo@tmax.co.kr")
//				.withClaim(Constants.CLAIM_USER_ID,  "admin@tmax.co.kr").withClaim( Constants.CLAIM_ROLE, Constants.ROLE_ADMIN );
//		
//		System.out.println(tokenBuilder.sign(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY)));


//		Properties prop = System.getProperties();
//        String key;
//        for (Enumeration e = prop.propertyNames() ; e.hasMoreElements() ;) {
//         key = (String)e.nextElement();
//         System.out.println(key + "=" + prop.get(key));
//        }


		Properties properties = System.getProperties();

		for(Entry entry : properties.entrySet()) {

		System.out.println(entry.getKey()+"="+entry.getValue());

		}

		
		
	}

}
