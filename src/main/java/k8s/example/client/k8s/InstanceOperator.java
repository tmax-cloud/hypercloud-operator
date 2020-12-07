package k8s.example.client.k8s;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import k8s.example.client.StringUtil;
import k8s.example.client.k8s.apis.CustomResourceApi;
import k8s.example.client.models.StateCheckInfo;
import okio.ByteString;
import io.kubernetes.client.openapi.models.V1OwnerReference;

public class InstanceOperator extends Thread {
	private final static Integer NO_RESOURCE = 0;
	private final static Integer BACK_UP_RESOURCE = 1;
	private final static Integer EXIST_RESOURCE = 2;

    private Logger logger = Main.logger;
	private Watch<Object> watchInstance;
	private static long latestResourceVersion = 0;
	
	ApiClient client = null;
	CustomResourceApi tpApi = null;
	ObjectMapper mapper = new ObjectMapper();
	Gson gson = new GsonBuilder().create();
	StateCheckInfo sci = new StateCheckInfo();
	
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
	
	InstanceOperator(ApiClient client, CustomResourceApi api, long resourceVersion) throws Exception {
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
		        api.listClusterCustomObjectCall(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, 
		        		null, null, null, null, null, null, null, Boolean.TRUE, null),
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
			while(true) {
				sci.checkThreadState();
				watchInstance.forEach(response -> {
					try {
						if(Thread.interrupted()) {
							logger.debug("Interrupted!");
							watchInstance.close();
						}
					} catch(Exception e) {
						logger.debug(e.getMessage());
					}
					
					//Logic here
					try {
						JsonNode instanceObj = numberTypeConverter(objectToJsonNode(response.object));
						logger.info("[Instance Operator] Event Type : " + response.type.toString()); //ADDED, MODIFIED, DELETED
						logger.debug("[Instance Operator] Object : " + instanceObj.toString());
						
		        		latestResourceVersion = instanceObj.get("metadata").get("resourceVersion").asLong();
		        		String instanceNamespace = instanceObj.get("metadata").get("namespace").asText();
		        		logger.debug("[Instance Operator] Instance Name : " + instanceObj.get("metadata").get("name").asText());
		        		logger.debug("[Instance Operator] Instance Namespace : " + instanceObj.get("metadata").get("namespace").asText());
		        		logger.debug("[Instance Operator] ResourceVersion : " + latestResourceVersion);
		        		
		        		if(response.type.toString().equals("ADDED")&&instanceObj.get("status")==null) {
		        			String templateName = instanceObj.get("spec").get("template").get("metadata").get("name").asText();
		        			
		        			logger.debug("[Instance Operator] Template Name : " + templateName);
						
							String templateNamespace = instanceObj.get("metadata").get("namespace").asText();
		        			if ( instanceObj.get("metadata").get("ownerReferences") != null ) {
		        				for(JsonNode owner : instanceObj.get("metadata").get("ownerReferences")) {
		        					if (owner.get("kind") != null && owner.get("kind").asText().equals(Constants.SERVICE_INSTANCE_KIND)) {
										Object serviceInstance = null;
										try {
											serviceInstance = tpApi.getNamespacedCustomObject(
												Constants.SERVICE_INSTANCE_API_GROUP,
												Constants.SERVICE_INSTANCE_API_VERSION,
												templateNamespace,
												Constants.SERVICE_INSTANCE_PLURAL,
												owner.get("name").asText());
											JsonNode serviceInstanceObj = numberTypeConverter(objectToJsonNode(serviceInstance));
											if(serviceInstanceObj.get("spec").get("clusterServiceClassName")!=null || serviceInstanceObj.get("spec").get("clusterServiceClassExternalName")!=null){
												templateNamespace = Constants.DEFAULT_NAMESPACE;
												if ( System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE) != null && !System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE).isEmpty() ) {
													templateNamespace = System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE);
												}
											}
										} catch (ApiException e) {
											logger.debug("Response body: " + e.getResponseBody());
											e.printStackTrace();
											throw e;
										} catch (Exception e) {
											logger.debug("Exception message: " + e.getMessage());
											e.printStackTrace();
											throw e;
										}
		        					}
								}
		        			}
		        			logger.debug("[Instance Operator] Template Namespace : " + templateNamespace);
		        			Object template = null;
		        			try {
		        				template = tpApi.getNamespacedCustomObject(
			        					Constants.CUSTOM_OBJECT_GROUP, 
			        					Constants.CUSTOM_OBJECT_VERSION, 
			        					templateNamespace, 
			        					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, 
			        					templateName);
		        			} catch (Exception e) {
		        				throw new Exception("Template Not Found");
		        			}
		        			
		        			//logger.debug("[Instance Operator] Template : " + template.toString());
		        			
		        			JsonNode templateObjs = numberTypeConverter(objectToJsonNode(template).get("objects"));
		        			JsonNode parameters = instanceObj.get("spec").get("template").get("parameters");
		        			
		        			JSONObject specObj = new JSONObject();
	    					JSONObject tpObj = new JSONObject();
	    					JSONObject obj = new JSONObject();
	    					
							JSONArray objArr = new JSONArray();
							
		        			
	    					JSONObject parmPatch = null;
	    					JSONArray parmPatchArray = null;
							List<String> existParm = new ArrayList<>();

							List<String> apiGroupList = new ArrayList<>();
							List<String> apiVersionList = new ArrayList<>();
							List<String> namespaceList = new ArrayList<>();
							List<String> kindList = new ArrayList<>();
							List<String> nameList = new ArrayList<>();
							List<Integer> resourceStatus = new ArrayList<>();
	    					
	    					Map<String,String> defaultValueMap = new HashMap<>();
	    					if ( objectToJsonNode(template).get("parameters") != null ) {
	        					for(JsonNode parameter : objectToJsonNode(template).get("parameters")) {
	        						String name = "";
	        						String defaultValue = "";
        							if( parameter.has("value") ) {
        								defaultValue = parameter.get("value").asText();
	        						}
        							if ( parameter.has("name") ) {
        								name = parameter.get("name").asText();
        							}
        							if ( !defaultValueMap.containsKey(name) ) {
        								defaultValueMap.put(name, defaultValue);
        							}
	        					}
	    					}
        							
		        			if(templateObjs.isArray()) {
								boolean checkMakeInstance = true;
		        				for(JsonNode object : templateObjs) {
			        				String objStr = object.toString();
			        				//logger.debug("[Instance Operator] Template Object : " + objStr);
			        				if ( parameters != null ) {
			        					
			        					for(JsonNode parameter : parameters) {
					        				String paramName = null;
					        				String paramValue = null;
					        				if(parameter.has("name") && parameter.has("value")) {
					        					paramName = parameter.get("name").asText();
						        				paramValue = parameter.get("value").asText();
						        				if ( StringUtil.isEmpty(paramValue) && defaultValueMap.containsKey(paramName) ) {
						        					paramValue = defaultValueMap.get(paramName);
						        				}
						        				if ( !existParm.contains( paramName ) ) {
						        					if (parmPatchArray == null) parmPatchArray = new JSONArray();
						        					parmPatch = new JSONObject();
						        					parmPatch.put("name", paramName);
						        					parmPatch.put("value", paramValue);
						        					parmPatchArray.add(parmPatch);
						        					existParm.add( paramName );
						        				}

						        				String dataType = existParameter( objectToJsonNode(template).get("parameters"), paramName );
						        				if ( objectToJsonNode(template).get("parameters") != null && dataType != null ) {
						        					
						        					if (dataType.equals(Constants.TEMPLATE_DATA_TYPE_NUMBER)) {
						        						String replaceString = "\"${" + paramName + "}\"";
						        						if( objStr.contains( replaceString ) ) {
								        					logger.debug("[Instance Operator] Parameter Number Name to be replaced : " + replaceString);
									        				logger.debug("[Instance Operator] Parameter Number Value to be replaced : " + paramValue);
								        					objStr = objStr.replace( replaceString, paramValue );
								        				}
						        					}
						        					
						        					String replaceString = "${" + paramName + "}";
						        					if( objStr.contains( replaceString ) ) {
							        					logger.debug("[Instance Operator] Parameter Name to be replaced : " + replaceString);
								        				logger.debug("[Instance Operator] Parameter Value to be replaced : " + paramValue);
							        					objStr = objStr.replace( replaceString, paramValue );
							        				}
						        				}
					        				}
					        				
			        					}
			        					
			        				}

			        				if ( objectToJsonNode(template).get("parameters") != null ) {
			        					for(JsonNode parameter : objectToJsonNode(template).get("parameters")) {
			        						String defaultValue = "";
		        							if( parameter.has("value") ) {
		        								defaultValue = parameter.get("value").asText();
			        						}
		        							if ( parameter.has("name") ) {
		        								String paramName = parameter.get("name").asText();
		        								if( parameter.has("valueType") && parameter.get("valueType").asText().equals( Constants.TEMPLATE_DATA_TYPE_NUMBER )) {
			        								String replaceString = "\"${" + paramName + "}\"";
					        						if( objStr.contains( replaceString ) ) {
							        					logger.debug("[Instance Operator] Default Parameter Number Name to be replaced : " + replaceString);
								        				logger.debug("[Instance Operator] Default Parameter Number Value to be replaced : " + defaultValue);
							        					objStr = objStr.replace( replaceString, defaultValue );
							        					
							        					if ( !existParm.contains( paramName ) ) {
								        					if (parmPatchArray == null) parmPatchArray = new JSONArray();
								        					parmPatch = new JSONObject();
								        					parmPatch.put("name", paramName);
								        					parmPatch.put("value", defaultValue);
								        					parmPatchArray.add(parmPatch);
								        					existParm.add( paramName );
								        				}
							        				}
				        						}
		        								String replaceString = "${" + paramName + "}";
					        					if( objStr.contains( replaceString ) ) {
						        					logger.debug("[Instance Operator] Default Parameter Name to be replaced : " + replaceString);
							        				logger.debug("[Instance Operator] Default Parameter Value to be replaced : " + defaultValue);
						        					objStr = objStr.replace( replaceString, defaultValue );
						        					
						        					if ( !existParm.contains( paramName ) ) {
							        					if (parmPatchArray == null) parmPatchArray = new JSONArray();
							        					parmPatch = new JSONObject();
							        					parmPatch.put("name", paramName);
							        					parmPatch.put("value", defaultValue);
							        					parmPatchArray.add(parmPatch);
							        					existParm.add( paramName );
							        				}
						        				}
		        							}
			        					}
			        				}
			        				
			        				String[] splitStr = objStr.split("\"metadata\":\\{");
			        				StringBuilder sb = new StringBuilder();
			        				sb.append("\"ownerReferences\": [{\"apiVersion\": \"v1\",\"blockOwnerDeletion\": true,\"controller\": false,\"kind\": \"TemplateInstance\",");
			        				sb.append("\"name\": \"");
			        				sb.append(instanceObj.get("metadata").get("name").asText());
			        				sb.append("\",\"uid\": \"");
			        				sb.append(instanceObj.get("metadata").get("uid").asText());
			        				sb.append("\"}],");
			        				
			        				StringBuilder objSb = new StringBuilder();
			        				objSb.append( splitStr[0] );
			        				for ( int i = 1; i < splitStr.length; i++ ) {
			        					objSb.append( "\"metadata\":{" );
			        					objSb.append( sb.toString() );
			        					objSb.append( splitStr[i] );
			        				}
			        				//objStr = splitStr[0] + "\"metadata\":{" + sb.toString() + splitStr[1];
			        				//logger.debug("[Instance Operator] @@@@@@@@@@@@@@@@@ Split Template Object[0] : " + splitStr[0]);
			        				//logger.debug("[Instance Operator] @@@@@@@@@@@@@@@@@ Split Template Object[1] : " + splitStr[1]);
			        				//logger.debug("[Instance Operator] Template Object : " + objStr);

									JsonNode replacedObject = numberTypeConverter(mapper.readTree(objSb.toString()));
									ObjectNode metadata = (ObjectNode) replacedObject.get("metadata");
									metadata.put("namespace",(instanceObj.get("metadata").get("namespace").asText()));

									//labels setting
									if(replacedObject.get("metadata").get("labels")==null){
										HashMap<String, String> map = new HashMap<String, String>();
										map.put("instance",instanceObj.get("metadata").get("name").asText());
										JsonNode node = mapper.valueToTree(map);
										metadata.put("labels",node);
									}
									else{
										ObjectNode labels = (ObjectNode) replacedObject.get("metadata").get("labels");
										labels.put("instance",instanceObj.get("metadata").get("name").asText());
									}

			        				logger.debug("[Instance Operator] Replaced Template Object : " + replacedObject);
			        				
			        				//if(!objStr.contains("${")) {
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
			        						if (instanceObj.get("metadata").has("namespace")) {
			        							namespace = instanceObj.get("metadata").get("namespace").asText();
			        						} else {
			        							namespace = "default";
			        						}
			        						
			        					}
			        					
			        					if(replacedObject.has("kind")) {
			        						kind = replacedObject.get("kind").asText();
			        					}
			        								        					
			        					JSONParser parser = new JSONParser();
										JSONObject bodyObj = (JSONObject) parser.parse(replacedObject.toString());
										
										//Resource check logic
										try {
                                            //Object objectResource;
                                            //JsonNode node;
                                            apiGroupList.add(apiGroup);
											apiVersionList.add(apiVersion);
											namespaceList.add(namespace);
											kindList.add(kind);
											nameList.add(replacedObject.get("metadata").get("name").asText());
                                            objArr.add(bodyObj);
                                            if (!existObject(apiGroup, apiVersion, namespace, kind.toLowerCase()+"s", replacedObject.get("metadata").get("name").asText())){
												logger.debug("resource apiGroup: " + apiGroup);
												logger.debug("resource apiVersion: " + apiVersion);
												logger.debug("resource kind: " + kind);
												logger.debug("resource namespace: " + namespace);
												logger.debug("resource name: " + replacedObject.get("metadata").get("name").asText());
												resourceStatus.add(NO_RESOURCE);
                                            }
                                            else {
												if(apiGroup.equals("core")){
													Map<String,String> map = K8sApiCaller.getCoreGroupMetadata(kind.toLowerCase()+"s",replacedObject.get("metadata").get("name").asText(),namespace).getLabels();
													if(map.containsKey("instance")&&map.get("instance").equals(instanceObj.get("metadata").get("name").asText())){
														logger.debug("backup resource core");
														resourceStatus.add(BACK_UP_RESOURCE);
													}
													else {
														logger.debug("exist resource2 core");
														resourceStatus.add(EXIST_RESOURCE);
														checkMakeInstance = false;
														break;
													}
												}
												else {
													Object objectResource = tpApi.getNamespacedCustomObject(apiGroup, apiVersion, namespace, kind.toLowerCase()+"s", replacedObject.get("metadata").get("name").asText());
													JsonNode node = numberTypeConverter(objectToJsonNode(objectResource));
													logger.debug("exist kind: " + kind);
													logger.debug("exist name: " + replacedObject.get("metadata").get("name").asText());
													if(node.get("metadata").get("labels") == null || node.get("metadata").get("labels").get("instance") == null){
														logger.debug("exist resource1");
														resourceStatus.add(EXIST_RESOURCE);
														checkMakeInstance = false;
														break;
													}
													else {
														String labelInstanceName = node.get("metadata").get("labels").get("instance").asText();
														if(labelInstanceName.equals(instanceObj.get("metadata").get("name").asText())) {
															logger.debug("backup resource");
															resourceStatus.add(BACK_UP_RESOURCE);
														}
														else {
															logger.debug("exist resource2");
															resourceStatus.add(EXIST_RESOURCE);
															checkMakeInstance = false;
															break;
														}
													}
												}
												
											}
										} catch (Exception e) {
                                            logger.debug("[Instance Operator] Exception: " + "check error");
                                            logger.debug("[Instance Operator] Exception: " + e.getMessage());
                                            
										} catch (Throwable e) {
											e.printStackTrace();
										}        							        					
			        				//} else {
			        				//	throw new Exception("Some non-replaced parameters or invaild values exist");
			        				//}
								}
								if(checkMakeInstance){
									for (int i=0; i<resourceStatus.size(); ++i){
										logger.debug("kind: " + kindList.get(i));
										logger.debug("resourceStatus: " + resourceStatus.get(i));
										if(resourceStatus.get(i)==NO_RESOURCE){
											try {
												Object result = tpApi.createNamespacedCustomObject(apiGroupList.get(i), apiVersionList.get(i), namespaceList.get(i), kindList.get(i), objArr.get(i), null);
												logger.debug(result.toString());
												patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_RUNNING, instanceNamespace);
											} catch (ApiException e) {
												logger.debug("[Instance Operator] ApiException: " + e.getMessage());
												logger.debug(e.getResponseBody());
												patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_ERROR, e.getResponseBody(), instanceNamespace);
												throw e;
											} catch (Exception e) {
                                                logger.debug("[Instance Operator] Exception: " + "create error");
												logger.debug("[Instance Operator] Exception: " + e.getMessage());
												StringWriter sw = new StringWriter();
												e.printStackTrace(new PrintWriter(sw));
												logger.debug(sw.toString());
												patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_ERROR, e.getMessage(), instanceNamespace);
												throw e;
											}
										}
										else if(resourceStatus.get(i)==BACK_UP_RESOURCE){
											if(apiGroupList.get(i).equals("core")){
												V1OwnerReference ownerRef = new V1OwnerReference();
												ownerRef.setApiVersion(apiVersionList.get(i));
												ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
												ownerRef.setController(Boolean.FALSE);
												ownerRef.setKind("TemplateInstance");
												ownerRef.setName(instanceObj.get("metadata").get("name").asText());
												ownerRef.setUid(instanceObj.get("metadata").get("uid").asText());
												try{
													K8sApiCaller.patchCoreGroupOwnerReferences(kindList.get(i).toLowerCase()+"s",nameList.get(i),namespaceList.get(i),ownerRef);
													patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_RUNNING, instanceNamespace);
												} catch (ApiException e) {
													logger.debug("[Instance Operator] ApiException: " + e.getMessage());
													logger.debug(e.getResponseBody());
													patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_ERROR, e.getResponseBody(), instanceNamespace);
													throw e;
												} catch (Exception e) {
													logger.debug("[Instance Operator] Exception: " + "create error");
													logger.debug("[Instance Operator] Exception: " + e.getMessage());
													StringWriter sw = new StringWriter();
													e.printStackTrace(new PrintWriter(sw));
													logger.debug(sw.toString());
													patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_ERROR, e.getMessage(), instanceNamespace);
													throw e;
												}
											}
											else{
												//Object objectResource = tpApi.getNamespacedCustomObject(apiGroupList.get(i), apiVersionList.get(i), namespaceList.get(i), kindList.get(i).toLowerCase()+"s", nameList.get(i));
												//JsonNode node = numberTypeConverter(objectToJsonNode(objectResource));
												JSONArray ownerArray = new JSONArray();

												JSONObject ownerReferences = new JSONObject();
												JSONObject patch = new JSONObject();
												JSONArray patchArray = new JSONArray();
											
												ownerReferences.put("apiVersion","v1");
												ownerReferences.put("blockOwnerDeletion",true);
												ownerReferences.put("controller",false);
												ownerReferences.put("kind","TemplateInstance");
												ownerReferences.put("name",instanceObj.get("metadata").get("name").asText());
												ownerReferences.put("uid",instanceObj.get("metadata").get("uid").asText());
	
												ownerArray.add(ownerReferences);
												patch.put("op", "replace");
												patch.put("path", "/metadata/ownerReferences");
												patch.put("value", ownerArray);
												patchArray.add(patch);
	
												try{
													Object result = tpApi.patchNamespacedCustomObject(apiGroupList.get(i), apiVersionList.get(i), namespaceList.get(i), kindList.get(i).toLowerCase()+"s", nameList.get(i), patchArray);
													patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_RUNNING, instanceNamespace);
													logger.debug(result.toString());
												} catch (ApiException e) {
													logger.debug("[Instance Operator] ApiException: " + e.getMessage());
													logger.debug(e.getResponseBody());
													patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_ERROR, e.getResponseBody(), instanceNamespace);
													throw e;
												} catch (Exception e) {
													logger.debug("[Instance Operator] Exception: " + "create error");
													logger.debug("[Instance Operator] Exception: " + e.getMessage());
													StringWriter sw = new StringWriter();
													e.printStackTrace(new PrintWriter(sw));
													logger.debug(sw.toString());
													patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_ERROR, e.getMessage(), instanceNamespace);
													throw e;
												}
											}
										}
									}
									if(instanceObj.get("spec").get("template").get("objects")==null){
										obj.put("objects", objArr);
										tpObj.put("template", obj);
										specObj.put("spec", tpObj);
										logger.debug("[Instance Operator] Object to be patched : " + specObj.toString());
										
										JSONObject patch = new JSONObject();
										JSONArray patchArray = new JSONArray();
										patch.put("op", "add");
										patch.put("path", "/spec/template/objects");
										patch.put("value", objArr);
										patchArray.add(patch);
										
										try{
											Object result = tpApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, instanceNamespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceObj.get("metadata").get("name").asText(), patchArray);
											logger.debug(result.toString());
										} catch (ApiException e) {
											throw new Exception(e.getResponseBody());
										}
									}
									if ( parmPatchArray != null ) {
										for ( int i = 0; i < parmPatchArray.size(); i++ ) {
											parmPatchArray.get(i).toString();
										}
										JSONObject parmPatchInput = new JSONObject();
										JSONArray parmPatchArrayInput = new JSONArray();
										parmPatchInput.put("op", "replace");
										parmPatchInput.put("path", "/spec/template/parameters");
										parmPatchInput.put("value", parmPatchArray);
										parmPatchArrayInput.add(parmPatchInput);
										
										try{
											Object result = tpApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, instanceNamespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceObj.get("metadata").get("name").asText(), parmPatchArrayInput);
											logger.debug(result.toString());
										} catch (ApiException e) {
											throw new Exception(e.getResponseBody());
										}
									}
								}
								else {
									patchStatus(instanceObj.get("metadata").get("name").asText(), Constants.STATUS_ERROR, "already exist resource in templateinstance", instanceNamespace);
								}
		        			}
	    					
		        		} else if(response.type.toString().equals("DELETED")) {
		        			V1DeleteOptions body = new V1DeleteOptions();
		        			logger.debug("[Instance Operator] Template Instance " + instanceObj.get("metadata").get("name") + " is DELETED");
		        			JsonNode instanceObjs = instanceObj.get("spec").get("template").get("objects");
		        		}
		        		
		        		
					} catch(Exception e) {
						logger.debug("[Instance Operator] Instance Operator Exception: " + e.getMessage());
					}
	        	});
				logger.debug("=============== Instance 'For Each' END ===============");
				watchInstance = Watch.createWatch(
				        client,
				        tpApi.listClusterCustomObjectCall(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, null, null, null, null, null, null, null, Boolean.TRUE, null),
				        new TypeToken<Watch.Response<Object>>(){}.getType());
			}

		} catch (Exception e) {
			logger.error("[Instance Operator] Instance Operator Exception: " + e.getMessage());
			if( e.getMessage().equals("abnormal") ) {
				logger.error("Catch abnormal conditions!! Exit process");
				System.exit(1);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void patchStatus(String instanceName, String phrase, String namespace) throws Exception {
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
			tpApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceName, patchStatusArray);
		} catch (ApiException e) {
			throw new Exception(e.getResponseBody());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void patchStatus(String instanceName, String phrase, String message, String namespace) throws Exception {
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
			tpApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceName, patchStatusArray);
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
			logger.error(e.getMessage());
		}
		return resultNode;
	}

	public static long getLatestResourceVersion() {
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
	
	private String existParameter( JsonNode parameters, String paramName ) {
		String dataType = null;
		for(JsonNode parameter : parameters) {
			if( parameter.has("name") && parameter.get("name").asText().toUpperCase().equals( paramName.toUpperCase() )) {
				if( parameter.has("valueType") && parameter.get("valueType").asText().equals( Constants.TEMPLATE_DATA_TYPE_NUMBER )) {
					dataType = Constants.TEMPLATE_DATA_TYPE_NUMBER;
				} else {
					dataType = Constants.TEMPLATE_DATA_TYPE_STRING;
				}
			}
		}
		return dataType;
	}

	private boolean existObject(String apiGroup, String apiVersion, String namespace, String plural, String name) throws Throwable{
		Object obj;
		try {
			if(apiGroup.equals("core")){
				String metaName = K8sApiCaller.getCoreGroupMetadata(plural,name,namespace).getName();
				if(metaName == null || metaName.isEmpty()) return false;
				return true;
			}
			else obj = tpApi.getNamespacedCustomObject(apiGroup, apiVersion, namespace, plural, name);
		} catch (ApiException e) {
			logger.debug("[Instance Operator] Instance Operator Resource exception: " + e.getMessage());
			logger.error("[Instance Operator] Instance Operator Resource exception: "+ e.getResponseBody());
			return false;
		}
		if (obj == null) {
			return false;
		} else {
			return true;
		}
	}
}
