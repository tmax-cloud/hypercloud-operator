package k8s.example.client.k8s;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;

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
import k8s.example.client.Main;
import k8s.example.client.k8s.apis.CustomResourceApi;
import okio.ByteString;

public class InstanceOperator extends Thread {
    private Logger logger = Main.logger;
	private final Watch<Object> watchInstance;
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
					}
				} catch(Exception e) {
					logger.info(e.getMessage());
				}
				
				try {
					JsonNode instanceObj = numberTypeConverter(objectToJsonNode(response.object));
					logger.info("[Instance Operator] Event Type : " + response.type.toString()); //ADDED, MODIFIED, DELETED
					logger.info("[Instance Operator] Object : " + instanceObj.toString());
					
	        		latestResourceVersion = instanceObj.get("metadata").get("resourceVersion").asInt();
	        		logger.info("[Instance Operator] Instance Name : " + instanceObj.get("metadata").get("name"));
	        		logger.info("[Instance Operator] ResourceVersion : " + latestResourceVersion);
	        		
	        		if(response.type.toString().equals("ADDED")) {
	        			String templateName = instanceObj.get("spec").get("template").get("metadata").get("name").asText();
	        			logger.info("[Instance Operator] Template Name : " + templateName);
	        			
	        			Object template = null;
	        			try {
	        				template = tpApi.getNamespacedCustomObject(
		        					Constants.CUSTOM_OBJECT_GROUP, 
		        					Constants.CUSTOM_OBJECT_VERSION, 
		        					Constants.TEMPLATE_NAMESPACE, 
		        					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, 
		        					templateName);
	        			} catch (Exception e) {
	        				throw new Exception("Template Not Found");
	        			}
	        			
	        			logger.info("[Instance Operator] Template : " + template.toString());
	        			
	        			JsonNode templateObjs = numberTypeConverter(objectToJsonNode(template).get("objects"));
	        			JsonNode parameters = instanceObj.get("spec").get("template").get("parameters");
	        			
	        			JSONObject specObj = new JSONObject();
    					JSONObject tpObj = new JSONObject();
    					JSONObject obj = new JSONObject();
    					
    					JSONArray objArr = new JSONArray();
	        			
	        			if(templateObjs.isArray()) {
	        				for(JsonNode object : templateObjs) {
		        				String objStr = object.toString();
		        				logger.info("[Instance Operator] Template Object : " + objStr);
		        				
		        				for(JsonNode parameter : parameters) {
			        				String paramName = null;
			        				String paramValue = null;
			        				if(parameter.has("name") && parameter.has("value")) {
			        					paramName = parameter.get("name").asText();
				        				paramValue = parameter.get("value").asText();
				        				
			        				}
			        				if(objStr.contains("${" + paramName + "}")) {
			        					logger.info("[Instance Operator] Parameter Name to be replaced : " + "${" + paramName + "}");
				        				logger.info("[Instance Operator] Parameter Value to be replaced : " + paramValue);
			        					objStr = objStr.replace("${" + paramName + "}", paramValue);
			        				}
			        			}

		        				JsonNode replacedObject = numberTypeConverter(mapper.readTree(objStr));
		        				logger.info("[Instance Operator] Replaced Template Object : " + replacedObject);
		        				
		        				if(!objStr.contains("${")) {
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
		        						logger.info(result.toString());
		        						patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_RUNNING);
		        					} catch (ApiException e) {
		        						logger.info("[Instance Operator] ApiException: " + e.getMessage());
		        						logger.info(e.getResponseBody());
		        						patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_ERROR, e.getResponseBody());
		        						throw e;
		        					} catch (Exception e) {
		        						logger.info("[Instance Operator] Exception: " + e.getMessage());
		        						StringWriter sw = new StringWriter();
		        						e.printStackTrace(new PrintWriter(sw));
		        						logger.info(sw.toString());
		        						patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_ERROR, e.getMessage());
		        						throw e;
		        					}
		        				} else {
		        					throw new Exception("Some non-replaced parameters or invaild values exist");
		        				}
		        			}
	        			}
	        			
	        			obj.put("objects", objArr);
    					tpObj.put("template", obj);
    					specObj.put("spec", tpObj);
    					logger.info("[Instance Operator] Object to be patched : " + specObj.toString());
    					
    					JSONObject patch = new JSONObject();
    					JSONArray patchArray = new JSONArray();
    					patch.put("op", "add");
    					patch.put("path", "/spec/template/objects");
    					patch.put("value", objArr);
    					patchArray.add(patch);
    					
    					try{
    						Object result = tpApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.TEMPLATE_NAMESPACE, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceObj.get("metadata").get("name").asText(), patchArray);
    						logger.info(result.toString());
    					} catch (ApiException e) {
    						throw new Exception(e.getResponseBody());
    					}
    					
	        		} else if(response.type.toString().equals("DELETED")) {
	        			V1DeleteOptions body = new V1DeleteOptions();
	        			logger.info("[Instance Operator] Template Instance " + instanceObj.get("metadata").get("name") + " is DELETED");
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
	        					
	        					logger.info(apiVersion + "/" + kind + " \"" + name + "\" deleted");
	        					try {
	        						Object result = tpApi.deleteNamespacedCustomObject(apiGroup, apiVersion, namespace, kind, name, body, 0, null, null);
	        						logger.info(result.toString());
	        					} catch (ApiException e) {
	        						throw new Exception(e.getResponseBody());
	        					}
	        				}
	        			}
	        		}
				} catch(Exception e) {
					logger.info("[Instance Operator] Instance Operator Exception: " + e.getMessage());
				}
        	});
		} catch (Exception e) {
			logger.info("[Instance Operator] Instance Operator Exception: " + e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void patchStatus(String instanceName, String phrase) throws Exception {
		JSONObject patchStatus = new JSONObject();
		JSONObject status = new JSONObject();
		JSONArray conditions = new JSONArray();
		JSONObject condition = new JSONObject();
		JSONArray patchStatusArray = new JSONArray();
		condition.put("type", "Phase");
		condition.put("status", phrase);
		conditions.add(condition);
		status.put("conditions", conditions);
		patchStatus.put("op", "add");
		patchStatus.put("path", "/status");
		patchStatus.put("value", status);
		patchStatusArray.add(patchStatus);
		
		try{
			tpApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.TEMPLATE_NAMESPACE, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceName, patchStatusArray);
		} catch (ApiException e) {
			throw new Exception(e.getResponseBody());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void patchStatus(String instanceName, String phrase, String message) throws Exception {
		JSONObject patchStatus = new JSONObject();
		JSONObject status = new JSONObject();
		JSONArray conditions = new JSONArray();
		JSONObject condition = new JSONObject();
		JSONArray patchStatusArray = new JSONArray();
		condition.put("type", "Phase");
		condition.put("status", phrase);
		condition.put("message", message);
		conditions.add(condition);
		status.put("conditions", conditions);
		patchStatus.put("op", "add");
		patchStatus.put("path", "/status");
		patchStatus.put("value", status);
		patchStatusArray.add(patchStatus);
		
		try{
			tpApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.TEMPLATE_NAMESPACE, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceName, patchStatusArray);
		} catch (ApiException e) {
			throw new Exception(e.getResponseBody());
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
			logger.info(e.getMessage());
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
