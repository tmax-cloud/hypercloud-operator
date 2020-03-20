package k8s.example.client.k8s;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.k8s.apis.CustomResourceApi;

public class TemplateOperator extends Thread {
	private final Watch<Object> watchInstance;
	private ExecutorService executorService;
	private static int latestResourceVersion = 0;
	
	ApiClient client = null;
	CustomResourceApi tpApi = null;
	ObjectMapper mapper = new ObjectMapper();
	Gson gson = new GsonBuilder().create();

	TemplateOperator(ApiClient client, CustomResourceApi api, int resourceVersion) throws Exception {		
		watchInstance = Watch.createWatch(
		        client,
		        api.listNamespacedCustomObjectCall(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.TEMPLATE_NAMESPACE, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, null, null, null, null, null, null, null, Boolean.TRUE, null),
		        new TypeToken<Watch.Response<Object>>(){}.getType()
        );
		this.executorService = Executors.newCachedThreadPool();
		
		latestResourceVersion = resourceVersion;
		this.client = client;
		this.tpApi = api;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			watchInstance.forEach(response -> {
				try {
					if(Thread.interrupted()) {
						System.out.println("Interrupted!");
						watchInstance.close();
						executorService.shutdown();
					}
				} catch(Exception e) {
					System.out.println(e.getMessage());
				}
				
				try {
					JsonNode template = numberTypeConverter(objectToJsonNode(response.object));
					String templateName = template.get("metadata").get("name").asText();
					System.out.println("[Template Operator] Event Type : " + response.type.toString()); //ADDED, MODIFIED, DELETED
					System.out.println("[Template Operator] Template Name : " + templateName);
					
	        		if(template.get("metadata").get("resourceVersion").asInt() > latestResourceVersion) {
	        			latestResourceVersion = template.get("metadata").get("resourceVersion").asInt();
	        			System.out.println("[Template Operator] Custom LatestResourceVersion : " + latestResourceVersion);
	        		}
	        		
	        		if(response.type.toString().equals("ADDED")) {
	        			JsonNode templateObjs = numberTypeConverter(objectToJsonNode(template).get("objects"));
	        			JSONArray kindArr = new JSONArray();
    					
	        			if(templateObjs.isArray()) {
	        				for(JsonNode object : templateObjs) {
	        					String kind = object.get("kind").asText();
	        					if(kind.equals("Service")) {
	        						String type = object.get("spec").get("type").asText();
	        						kind += " (" + type + ")";
	        					}
	        					kindArr.add(kind);
		        			}
	        			}
	        			
    					System.out.println("[Template Operator] Object to be patched to objectKinds: " + kindArr.toString());
    					
    					JSONObject patch = new JSONObject();
    					JSONArray patchArray = new JSONArray();
    					patch.put("op", "add");
    					patch.put("path", "/objectKinds");
    					patch.put("value", kindArr);
    					patchArray.add(patch);
    					
    					try{
    						Object result = tpApi.patchNamespacedCustomObject(
    								Constants.CUSTOM_OBJECT_GROUP, 
    								Constants.CUSTOM_OBJECT_VERSION, 
    								Constants.TEMPLATE_NAMESPACE, 
    								Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, 
    								templateName, 
    								patchArray);
    						System.out.println(result.toString());
    					} catch (ApiException e) {
    						throw new Exception(e.getResponseBody());
    					}
    					
	        		}
				} catch(Exception e) {
					System.out.println(e.getMessage());
				} 
        				
//        				if(reason.equals("Running") || reason.equals("Completed") || reason.equals("Error")) {
//							Runnable runnable = new Runnable() {
//								@Override
//								public void run() {
//									try {
//										MainWatcher.callNotifiedStatusUpdate(response.object.getMetadata().getNamespace(), productId, reason, extraQueryParam);
//									} catch (Exception e) {
//										e.printStackTrace();
//									}
//								}
//							};
//							
//							executorService.execute(runnable);
//        				}
//	        		}
        		}
    		);
		} catch (Exception e) {
			System.out.println("[Template Operator] Template Operator Exception: " + e.getMessage());
			executorService.shutdown();
		}
	}
	
	private JsonNode numberTypeConverter(JsonNode jsonNode) {
		if (jsonNode.isObject()) {
			ObjectNode objectNode = (ObjectNode) jsonNode;
			
			Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
			
			while(iter.hasNext()) {
				Map.Entry<String, JsonNode> entry = iter.next();
				entry.setValue(numberTypeConverter(entry.getValue()));
			}
		} else if (jsonNode.isArray()) {
			ArrayNode arrayNode = (ArrayNode) jsonNode;
			for(int i = 0; i < arrayNode.size(); i++) {
				arrayNode.set(i, numberTypeConverter(arrayNode.get(i)));
			}
		} else if (jsonNode.isValueNode()) {
			if(jsonNode.isDouble() && jsonNode.canConvertToInt()) {
				IntNode intNode = new IntNode(jsonNode.asInt());
				jsonNode = intNode;
			}
		}
		return jsonNode;
	}

	private JsonNode objectToJsonNode(Object object) throws IOException {
		JsonNode resultNode = mapper.valueToTree(object);
		return resultNode;
	}

	public static int getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
