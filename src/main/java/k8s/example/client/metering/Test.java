package k8s.example.client.metering;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.User;

public class Test {
	public static CustomObjectsApi customObjectApi = new CustomObjectsApi();

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


		Object response = customObjectApi.getClusterCustomObject(
    			Constants.CUSTOM_OBJECT_GROUP,
				Constants.CUSTOM_OBJECT_VERSION, 
				Constants.CUSTOM_OBJECT_PLURAL_USER, 
				"test-tmax.co.kr");
		JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));
		System.out.println(respJson.toString());
        User userInfo = new ObjectMapper().readValue((new Gson()).toJson(respJson.get("userInfo")), User.class);
		
		
	}

}
