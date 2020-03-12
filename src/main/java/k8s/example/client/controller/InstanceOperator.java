package k8s.example.client.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.JSON.DateTimeTypeAdapter;
import io.kubernetes.client.openapi.JSON.DateTypeAdapter;
import io.kubernetes.client.openapi.JSON.SqlDateTypeAdapter;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.controller.apis.CustomResourceApi;
import okio.ByteString;

public class InstanceOperator extends Thread {
	private final Watch<Object> watchInstance;
	private ExecutorService executorService;
	private Logger logger = OperatorManager.logger;
	private Logger logger2 = LoggerFactory.getLogger("Operator");
	private static int latestResourceVersion = 0;
	
	ApiClient client = null;
	CustomResourceApi tpApi = null;
	ObjectMapper mapper = new ObjectMapper();
	Gson gson = new GsonBuilder().create();
	
	private DateTypeAdapter dateTypeAdapter = new DateTypeAdapter();
	private SqlDateTypeAdapter sqlDateTypeAdapter = new SqlDateTypeAdapter();
	private DateTimeTypeAdapter dateTimeTypeAdapter = new DateTimeTypeAdapter();
	private LocalDateTypeAdapter localDateTypeAdapter = new LocalDateTypeAdapter();
	private ByteArrayAdapter byteArrayTypeAdapter = new ByteArrayAdapter();
	
	public Gson kubeGson = new GsonBuilder()
            .registerTypeAdapter(Date.class, dateTypeAdapter)
            .registerTypeAdapter(java.sql.Date.class, sqlDateTypeAdapter)
            .registerTypeAdapter(DateTime.class, dateTimeTypeAdapter)
            .registerTypeAdapter(LocalDate.class, localDateTypeAdapter)
            .registerTypeAdapter(byte[].class, byteArrayTypeAdapter)
//            .registerTypeAdapter(new TypeToken<Watch.Response<Object>>(){}.getType(),  new MapDeserializerDoubleAsIntFix())
            .create();
	
	InstanceOperator(ApiClient client, CustomResourceApi api, int resourceVersion) throws Exception {
		JSON clientJson = client.getJSON();
		clientJson.setGson(kubeGson);
		client.setJSON(clientJson);
		
		ApiClient customClient = api.getApiClient();
		JSON customJson = api.getApiClient().getJSON();
		customJson.setGson(kubeGson);
		customClient.setJSON(customJson);
		api.setApiClient(customClient);
		
		watchInstance = Watch.createWatch(
		        client,
		        api.listNamespacedCustomObjectCall(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.TEMPLATE_NAMESPACE, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, null, null, null, null, null, String.valueOf(resourceVersion), null, Boolean.TRUE, null),
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
						logger.info("Interrupted!");
						watchInstance.close();
						executorService.shutdown();
					}
				} catch(Exception e) {
					logger.info(e.getMessage());
				}
				
				try {
					String statusPhrase = null;
					JsonNode instanceObj = numberTypeConverter(objectToJsonNode(response.object));
					logger2.info("Event Type : " + response.type.toString()); //ADDED, MODIFIED, DELETED
					logger2.info("Object : " + instanceObj.toString());
					
	        		if(instanceObj.get("metadata").get("resourceVersion").asInt() > latestResourceVersion) {
	        			latestResourceVersion = instanceObj.get("metadata").get("resourceVersion").asInt();
	        			logger2.info("Instance Name : " + instanceObj.get("metadata").get("name"));
	        			logger2.info("Custom LatestResourceVersion : " + latestResourceVersion);
	        		}
	        		
	        		if(response.type.toString().equals("ADDED")) {
	        			String templateName = instanceObj.get("spec").get("template").get("metadata").get("name").asText();
	        			logger2.info("Template Instance " + instanceObj.get("metadata").get("name") + " is ADDED");
	        			logger2.info("Template Name : " + templateName);
	        			
	        			Object template = tpApi.getNamespacedCustomObject(
	        					Constants.CUSTOM_OBJECT_GROUP, 
	        					Constants.CUSTOM_OBJECT_VERSION, 
	        					Constants.TEMPLATE_NAMESPACE, 
	        					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, 
	        					templateName);
	        			logger2.info("Template : " + template.toString());
	        			
	        			JsonNode templateObjs = numberTypeConverter(objectToJsonNode(template).get("objects"));
	        			JsonNode parameters = instanceObj.get("spec").get("template").get("parameters");
	        			
	        			JSONObject specObj = new JSONObject();
    					JSONObject tpObj = new JSONObject();
    					JSONObject obj = new JSONObject();
    					
    					JSONArray objArr = new JSONArray();
	        			
	        			if(templateObjs.isArray()) {
	        				for(JsonNode object : templateObjs) {
		        				String objStr = object.toString();
		        				logger2.info("Template Object : " + objStr);
		        				
		        				for(JsonNode parameter : parameters) {
			        				String paramName = null;
			        				String paramValue = null;
			        				if(parameter.has("name") && parameter.has("value")) {
			        					paramName = parameter.get("name").asText();
				        				paramValue = parameter.get("value").asText();
				        				
			        				}
			        				if(objStr.contains("${" + paramName + "}")) {
			        					logger2.info("Parameter Name to be replaced : " + "${" + paramName + "}");
				        				logger2.info("Parameter Value to be replaced : " + paramValue);
			        					objStr = objStr.replace("${" + paramName + "}", paramValue);
			        				}
			        			}

		        				JsonNode replacedObject = numberTypeConverter(mapper.readTree(objStr));
		        				logger2.info("Replaced Template Object : " + replacedObject);
		        				
		        				if(!objStr.contains("$")) {
		        					String apiGroup = null;
		        					String apiVersion = null;
		        					String namespace = null;
		        					String kind = null;
		        					
		        					if(replacedObject.has("apiVersion")) {
		        						if(replacedObject.get("apiVersion").asText().contains("/")) {
		        							apiGroup = replacedObject.get("apiVersion").asText().split("/")[0];
		        							apiVersion = replacedObject.get("apiVersion").asText().split("/")[1];
		        						} else {
		        							apiGroup = "core";
		        							apiVersion = replacedObject.get("apiVersion").asText();
		        						}
		        					}
		        					
		        					if(replacedObject.get("metadata").has("namespace")) {
		        						namespace = replacedObject.get("metadata").get("namespace").asText();
		        					} else {
		        						namespace = "default";
		        					}
		        					
		        					if(replacedObject.has("kind")) {
		        						kind = replacedObject.get("kind").asText();
		        					}
		        					
		        					JSONParser parser = new JSONParser();
		        					JSONObject bodyObj = (JSONObject) parser.parse(replacedObject.toString());
		        					objArr.add(bodyObj);
		        							        							        					
		        					try {
		        						Object result = tpApi.createNamespacedCustomObject(apiGroup, apiVersion, namespace, kind, bodyObj, null);
		        						logger2.info(result.toString());
		        						statusPhrase = Constants.STATUS_RUNNING;
		        					} catch (ApiException e) {
		        						throw new Exception(e.getResponseBody());
		        					}
		        				} else {
		        					throw new Exception("Some non-replaced parameters or invaild values exist");
		        				}
		        			}
	        			}
	        			
	        			obj.put("objects", objArr);
    					tpObj.put("template", obj);
    					specObj.put("spec", tpObj);
    					logger2.info("Object to be patched : " + specObj.toString());
    					
    					JSONObject patch = new JSONObject();
    					JSONArray patchArray = new JSONArray();
    					patch.put("op", "add");
    					patch.put("path", "/spec/template/objects");
    					patch.put("value", objArr);
    					patchArray.add(patch);
    					
    					JSONObject patchStatus = new JSONObject();
    					JSONObject status = new JSONObject();
    					JSONArray conditions = new JSONArray();
    					JSONObject condition = new JSONObject();
    					JSONArray patchStatusArray = new JSONArray();
    					condition.put("type", "Phase");
    					condition.put("status", statusPhrase);
    					conditions.add(condition);
    					status.put("conditions", conditions);
    					patchStatus.put("op", "add");
    					patchStatus.put("path", "/status");
    					patchStatus.put("value", status);
    					patchStatusArray.add(patchStatus);
    					
    					try{
    						Object result = tpApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.TEMPLATE_NAMESPACE, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceObj.get("metadata").get("name").asText(), patchArray);
    						logger2.info(result.toString());
    						result = tpApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.TEMPLATE_NAMESPACE, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceObj.get("metadata").get("name").asText(), patchStatusArray);
    						logger2.info(result.toString());
    					} catch (ApiException e) {
    						throw new Exception(e.getResponseBody());
    					}
    					
	        		} else if(response.type.toString().equals("DELETED")) {
	        			V1DeleteOptions body = new V1DeleteOptions();
	        			logger2.info("Template Instance " + instanceObj.get("metadata").get("name") + " is DELETED");
	        			JsonNode instanceObjs = instanceObj.get("spec").get("template").get("objects");
	        			
	        			if(instanceObjs.isArray()) {
	        				for(JsonNode object : instanceObjs) {
	        					String apiGroup = null;
	        					String apiVersion = null;
	        					String kind = null;
	        					String namespace = null;
	        					String name = object.get("metadata").get("name").asText();
	        					
	        					if(object.has("apiVersion")) {
	        						if(object.get("apiVersion").asText().contains("/")) {
	        							apiGroup = object.get("apiVersion").asText().split("/")[0];
	        							apiVersion = object.get("apiVersion").asText().split("/")[1];
	        						} else {
	        							apiGroup = "core";
	        							apiVersion = object.get("apiVersion").asText();
	        						}
	        					}
	        					
	        					if(object.get("metadata").has("namespace")) {
	        						namespace = object.get("metadata").get("namespace").asText();
	        					} else {
	        						namespace = "default";
	        					}
	        					
	        					if(object.has("kind")) {
	        						kind = object.get("kind").asText();
	        					}
	        					
	        					logger2.info(apiVersion + "/" + kind + " \"" + name + "\" deleted");
	        					try {
	        						Object result = tpApi.deleteNamespacedCustomObject(apiGroup, apiVersion, namespace, kind, name, body, 0, null, null);
	        						logger2.info(result.toString());
	        					} catch (ApiException e) {
	        						throw new Exception(e.getResponseBody());
	        					}
	        				}
	        			}
	        		}
				} catch(Exception e) {
					logger2.info(e.getMessage());
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
			logger.info("Instance Operator Exception: " + e.getMessage());
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

	private JsonNode objectToJsonNode(Object object) {
		String objectStr = gson.toJson(object);
		JsonNode resultNode = null;
		try {
			resultNode = mapper.readTree(objectStr);
		} catch (IOException e) {
			logger2.info(e.getMessage());
		}
		return resultNode;
	}

	public static int getLatestResourceVersion() {
		return latestResourceVersion;
	}
	
	public class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {

        private DateTimeFormatter formatter;

        public LocalDateTypeAdapter() {
            this(ISODateTimeFormat.date());
        }

        public LocalDateTypeAdapter(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        public void setFormat(DateTimeFormatter dateFormat) {
            this.formatter = dateFormat;
        }

        @Override
        public void write(JsonWriter out, LocalDate date) throws IOException {
            if (date == null) {
                out.nullValue();
            } else {
                out.value(formatter.print(date));
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            switch (in.peek()) {
            case NULL:
                in.nextNull();
                return null;
            default:
                String date = in.nextString();
                return formatter.parseLocalDate(date);
            }
        }
    }
	
	public class ByteArrayAdapter extends TypeAdapter<byte[]> {

        @Override
        public void write(JsonWriter out, byte[] value) throws IOException {
            boolean oldHtmlSafe = out.isHtmlSafe();
            out.setHtmlSafe(false);
            if (value == null) {
                out.nullValue();
            } else {
                out.value(ByteString.of(value).base64());
            }
            out.setHtmlSafe(oldHtmlSafe);
        }

        @Override
        public byte[] read(JsonReader in) throws IOException {
            switch (in.peek()) {
            case NULL:
                in.nextNull();
                return null;
            default:
                String bytesAsBase64 = in.nextString();
                ByteString byteString = ByteString.decodeBase64(bytesAsBase64);
                return byteString.toByteArray();
            }
        }
    }
}
