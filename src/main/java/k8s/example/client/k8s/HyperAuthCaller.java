package k8s.example.client.k8s;

import java.io.IOException;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.StringUtil;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HyperAuthCaller {
	
	private static final String CALLER_NAME = "HyperAuthCaller";
    static OkHttpClient client = new OkHttpClient();	
	private static Logger logger = Main.logger;

	private static String setHyperAuthURL( String serviceName )  {
		int hyperauthHttpPort = 8080;
		if ( StringUtil.isNotEmpty(System.getenv( "HYPERAUTH_HTTP_PORT" ))){
			hyperauthHttpPort = Integer.parseInt( System.getenv( "HYPERAUTH_HTTP_PORT" ) );
		}
		return Constants.HYPERAUTH_URL + ":" + hyperauthHttpPort +"/" + serviceName;
	}
	
	public static String loginAsAdmin() throws IOException {
		logger.info(" [HyperAuth] Login as Admin Service");	    
		Request request = null;

	    //POST svc	      
	    HttpUrl.Builder urlBuilder = HttpUrl.parse(setHyperAuthURL( Constants.SERVICE_NAME_LOGIN_AS_ADMIN )).newBuilder();
	    String url = urlBuilder.build().toString();
	    RequestBody formBody = new FormBody.Builder().add("grant_type", "password")
	    		.add("username", "admin").add("password", "admin").
						add("client_id", "admin-cli").build();
	    request = new Request.Builder().header("Content-Type", "application/x-www-form-urlencoded").url(url).post(formBody).build(); 
	       
		Response response = client.newCall(request).execute();
		String result = response.body().string();
		logger.debug(" Login As Admin result : " + result);
		
		Gson gson = new Gson();
	    JsonObject resultJson = gson.fromJson(result, JsonObject.class);
	    
	    logger.debug(" accessToken : " + resultJson.get("access_token").toString());

	    return resultJson.get("access_token").toString();	
	}

	
	public static JsonObject getUser(String userId, String token) throws IOException {
		logger.info(" [HyperAuth] HyperAuth Get User Detail Service" );

	    Request request = null;

		 //GET svc
	    HttpUrl.Builder urlBuilder = HttpUrl.parse(setHyperAuthURL( Constants.SERVICE_NAME_USER_DETAIL ) + userId).newBuilder();

	    String url = urlBuilder.build().toString();
	    request = new Request.Builder().url(url).addHeader("Authorization", "Bearer " + token).get().build();
	    
	    logger.debug(" request" + request.toString() );

		Response response = client.newCall(request).execute();
		String result = response.body().string();
		logger.debug(" UserDetailResult : " + result);
		
		Gson gson = new Gson();
	    JsonObject resultJson = gson.fromJson(result, JsonObject.class);
	    
	    return resultJson; 
	}	
	
	public static JsonArray getUserList( String token) throws IOException {
		logger.info(" [HyperAuth] HyperAuth Get User List Service" );

	    Request request = null;

		 //GET svc
	    HttpUrl.Builder urlBuilder = HttpUrl.parse(setHyperAuthURL( Constants.SERVICE_NAME_USER_DETAIL )).newBuilder();

	    String url = urlBuilder.build().toString();
	    request = new Request.Builder().url(url).addHeader("Authorization", "Bearer " + token).get().build();
	    
	    logger.debug(" request" + request.toString() );

		Response response = client.newCall(request).execute();
		String result = response.body().string();
		logger.debug(" UserListResult : " + result);
		
		Gson gson = new Gson();
	    JsonArray resultJson = gson.fromJson(result, JsonArray.class);
	    
	    return resultJson; 
	}	
	
	public static JsonObject getUserDetailWithoutToken(String userId) throws Exception{
		logger.info(" [HyperAuth] HyperAuth Get User Detail Without Token Service" );

	    Request request = null;
		JsonObject resultJson = null;
		try{
			//GET svc
			HttpUrl.Builder urlBuilder = HttpUrl.parse(setHyperAuthURL( Constants.SERVICE_NAME_USER_DETAIL_WITHOUT_TOKEN ) + userId).newBuilder();

			String url = urlBuilder.build().toString();
			request = new Request.Builder().url(url).get().build();

			logger.debug(" request" + request.toString() );

			Response response = client.newCall(request).execute();
			String result = response.body().string();
			logger.debug(" UserDetailResult : " + result);

			Gson gson = new Gson();
			resultJson = gson.fromJson(result, JsonObject.class);
		} catch (Exception e){
			logger.debug("No Corresponding User" );
			e.printStackTrace();
			throw e;
		}
	    return resultJson; 
	}	
}
