package k8s.example.client.metering;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import k8s.example.client.Constants;
import k8s.example.client.Util;

public class Test {

	public static void main(String[] args) throws Exception {
//		// Verify access token	
//		String accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiY2x1c3Rlci1hZG1pbiIsInRva2VuSWQiOiJjM2NiNWM4Ny0wYjRkLTQyZGItYjg2OC0wZGY1NTlmOGRkYmEiLCJpc3MiOiJUbWF4LVByb0F1dGgtV2ViSG9vayIsImlkIjoiYWRtaW5AdG1heC5jby5rciIsImV4cCI6MTU4NTQ4NTY2NX0.QJcSq9TfyR-v28fDPJ3ZOfqv9jrt1bta6K1Bt797puQ";
//		JWTVerifier verifier = JWT.require(Algorithm.HMAC256(Constants.ACCESS_TOKEN_SECRET_KEY)).build();
//		DecodedJWT jwt = verifier.verify(accessToken);
//		
//		String issuer = jwt.getIssuer();
//		String userId = jwt.getClaims().get(Constants.CLAIM_USER_ID).asString();
//		String tokenId = jwt.getClaims().get(Constants.CLAIM_TOKEN_ID).asString();
//		System.out.println( "  Issuer: " + issuer );
//		System.out.println( "  User ID: " + userId );
//		System.out.println( "  Token ID: " + tokenId );
		
		List <String> aa = new ArrayList<>();
		aa.add("11");
		aa.add("22");
		aa.add("33");
		aa.add("11");
		aa = aa.stream().distinct().collect(Collectors.toList());
		for(String a : aa) {
			System.out.println(a);

		}

	}

}
