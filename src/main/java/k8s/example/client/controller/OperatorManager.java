package k8s.example.client.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;
import k8s.example.client.controller.apis.CustomResourceApi;

public class OperatorManager {
	
	private static final String SERVER_IP = "0.0.0.0";
	private static final String SERVER_HTTP_PORT = "8088";
	private static final String apiGroup = "tmax.co.kr";
	private static final String apiVersion = "v1";
	private static final String hyperCloudNamespace = "hypercloud-system";
	private static final String templateInstanceCrdName = "templateinstances";
	
	public static Logger logger = LoggerFactory.getLogger("OM");
	
	public static void main (String...args) {
		logger.info("HyperCloud Operator Manager Start");
		
		try { 
			ApiClient client = Config.fromCluster();			
			client.setConnectTimeout(0);
	    	client.setReadTimeout(0);
	    	client.setWriteTimeout(0);
			Configuration.setDefaultApiClient(client);
			
			CustomResourceApi customApi = new CustomResourceApi();

			// Get Latest Resource Version
			Object result = customApi.listNamespacedCustomObject(apiGroup, apiVersion, hyperCloudNamespace, templateInstanceCrdName, null, null, null, null, null, null, null, Boolean.FALSE);
			Gson gson = new GsonBuilder().create();
			String JsonInString = gson.toJson(result);
			ObjectMapper mapper = new ObjectMapper();
			JsonFactory factory = mapper.getFactory();
			JsonParser parser = factory.createParser(JsonInString);
			JsonNode customObjectList = mapper.readTree(parser);
			
			int instanceLatestResourceVersion = 0;
			if(customObjectList.get("items").isArray()) {
				for(JsonNode instance : customObjectList.get("items")) {
					int instanceResourceVersion = instance.get("metadata").get("resourceVersion").asInt();
					instanceLatestResourceVersion = (instanceLatestResourceVersion >= instanceResourceVersion) ? instanceLatestResourceVersion : instanceResourceVersion;
				}
			}
			
			logger.info("Template Instance Operator Run (Latest Resource Version: " + instanceLatestResourceVersion + ")");
			InstanceOperator instanceOperator = new InstanceOperator(client, customApi, instanceLatestResourceVersion);
			instanceOperator.start();
						
			// Check Watcher Threads are Alive every 10sec
			while(true) {
				if(!instanceOperator.isAlive()) {
					instanceLatestResourceVersion = InstanceOperator.getLatestResourceVersion();
					logger.info("Template Instance Operator is not Alive. Restart Operator! (Latest Resource Version: " + instanceLatestResourceVersion + ")");
					instanceOperator.interrupt();
					instanceOperator = new InstanceOperator(client, customApi, instanceLatestResourceVersion);
					instanceOperator.start();
				}
				
				Thread.sleep(10000);
			}
		} catch (Exception e) {
			logger.info("Operator Manager Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
		}
	}
	
	public static void callNotifiedStatusUpdate(String namespace, String podName, String status, Map<String, String> extraQueryParam) throws Exception {
		String pathParam = null;
		String queryParam = null;
		
		if(podName != null) {
			String scaleGroupId = podName.split("-")[1]; // 0: prefix, 1: replica set name, 2: pod ID
			pathParam = "namespaces/" + namespace + "/scaleGroups/" + scaleGroupId + "/pods/" + podName;
		}
		
		queryParam = "status=" + status;
		
		if(extraQueryParam != null) {
			Iterator<String> keyIterator = extraQueryParam.keySet().iterator();
			while(keyIterator.hasNext()) {
				String key = keyIterator.next();
				queryParam += "&" + key + "=" + extraQueryParam.get(key);
			}
		}
		
		serviceCall("PUT", "v3", "_api", "NotifiedStatus", pathParam, null, queryParam, null);
	}
	   
	public static void serviceCall(String Method, String application, String serviceGroup, String serviceName, String prePathParam, String postPathParam, String queryParam, String inDO) throws Exception {		
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(SERVER_IP);
		sb.append(":");
		sb.append(SERVER_HTTP_PORT);
		sb.append("/"+ application + "/" + serviceGroup + "/");
		if(prePathParam != null) sb.append(prePathParam + "/");
		sb.append(serviceName);
		if(postPathParam != null) sb.append("/" + postPathParam);
		if(queryParam != null) sb.append("?" + queryParam);
		
		String serviceURL = sb.toString();

		CloseableHttpClient client = null;
		try {
			// Make HTTP Connection
			client = HttpClientBuilder.create().build();
			
			HttpRequestBase request = null;
			switch(Method) {
			case("POST"):
				request = new HttpPost(serviceURL);
				break;
			case("PUT"):
				request = new HttpPut(serviceURL);
				break;
			case("GET"):
				request = new HttpGet(serviceURL);
				break;
			case("DELETE"):
				request = new HttpDelete(serviceURL);
				break;
			}
			
			request.addHeader("Content-Type", "application/json; charset=UTF-8");

			if(inDO != null) {
				HttpEntity entity = new StringEntity( new String( inDO ), ContentType.APPLICATION_JSON );
				((HttpEntityEnclosingRequestBase) request).setEntity( entity );
			}
			
			// Send Request
			logger.info("Service URL : " + Method + " " + serviceURL);
			client.execute(request);			
		} catch (Exception e) {
			System.out.println("HTTP Request Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
		} finally {
			client.close();
		}
	}
}