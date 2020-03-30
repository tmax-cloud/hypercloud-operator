package k8s.example.client.k8s;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.models.V1ClusterRole;
import io.kubernetes.client.openapi.models.V1ClusterRoleBinding;
import io.kubernetes.client.openapi.models.V1ClusterRoleBindingList;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarSource;
import io.kubernetes.client.openapi.models.V1ExecAction;
import io.kubernetes.client.openapi.models.V1HTTPGetAction;
import io.kubernetes.client.openapi.models.V1HTTPHeader;
import io.kubernetes.client.openapi.models.V1Handler;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1Lifecycle;
import io.kubernetes.client.openapi.models.V1LoadBalancerIngress;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeAddress;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimSpec;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1PolicyRule;
import io.kubernetes.client.openapi.models.V1Probe;
import io.kubernetes.client.openapi.models.V1ReplicaSetBuilder;
import io.kubernetes.client.openapi.models.V1ReplicaSetSpec;
import io.kubernetes.client.openapi.models.V1ResourceQuota;
import io.kubernetes.client.openapi.models.V1ResourceQuotaSpec;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1Role;
import io.kubernetes.client.openapi.models.V1RoleBinding;
import io.kubernetes.client.openapi.models.V1RoleBindingList;
import io.kubernetes.client.openapi.models.V1RoleRef;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretKeySelector;
import io.kubernetes.client.openapi.models.V1SecretVolumeSource;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import io.kubernetes.client.openapi.models.V1Subject;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.kubernetes.client.proto.V1alpha1Rbac;
import io.kubernetes.client.proto.V1beta1Rbac;
import io.kubernetes.client.util.Config;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.Client;
import k8s.example.client.DataObject.ClientCR;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.DataObject.User;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.Main;
import k8s.example.client.StringUtil;
import k8s.example.client.Util;
import k8s.example.client.k8s.apis.CustomResourceApi;
import k8s.example.client.models.BindingInDO;
import k8s.example.client.models.BindingOutDO;
import k8s.example.client.models.CommandExecOut;
import k8s.example.client.models.Cost;
import k8s.example.client.models.Endpoint;
import k8s.example.client.models.GetPlanDO;
import k8s.example.client.models.InputParametersSchema;
import k8s.example.client.models.Metadata;
import k8s.example.client.models.NamespaceClaim;
import k8s.example.client.models.PlanMetadata;
import k8s.example.client.models.ProvisionInDO;
import k8s.example.client.models.Registry;
import k8s.example.client.models.RegistryCondition;
import k8s.example.client.models.RegistryPVC;
import k8s.example.client.models.RegistryService;
import k8s.example.client.models.RegistryStatus;
import k8s.example.client.models.RoleBindingClaim;
import k8s.example.client.models.Schemas;
import k8s.example.client.models.ServiceInstanceSchema;
import k8s.example.client.models.ServiceMetadata;
import k8s.example.client.models.ServiceOffering;
import k8s.example.client.models.ServicePlan;
import k8s.example.client.models.Services;
import k8s.example.client.models.TemplateInstance;
import k8s.example.client.models.TemplateInstanceSpec;
import k8s.example.client.models.TemplateInstanceSpecTemplate;
import k8s.example.client.models.TemplateParameter;

public class K8sApiCaller {	
	private static ApiClient k8sClient;
	private static CoreV1Api api;
	private static AppsV1Api appApi;
	private static RbacAuthorizationV1Api rbacApi;
	private static CustomObjectsApi customObjectApi;
	private static CustomResourceApi templateApi;
	private static ObjectMapper mapper = new ObjectMapper();
	private static Gson gson = new GsonBuilder().create();

    private static Logger logger = Main.logger;
    
	public static void initK8SClient() throws Exception {
		k8sClient = Config.fromCluster();
		k8sClient.setConnectTimeout(0);
		k8sClient.setReadTimeout(0);
		k8sClient.setWriteTimeout(0);		
		Configuration.setDefaultApiClient(k8sClient);

		api = new CoreV1Api();
		appApi = new AppsV1Api();
		rbacApi = new RbacAuthorizationV1Api();
		customObjectApi = new CustomObjectsApi();
		templateApi = new CustomResourceApi();
	}

	public static void startWatcher() throws Exception {    	
		// Get latest resource version
		logger.info("Get latest resource version");
		int userLatestResourceVersion = 0;
		try {
			Object response = customObjectApi.listClusterCustomObject(
					Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.CUSTOM_OBJECT_PLURAL_USER,
					null, null, null, null, null, null, null, Boolean.FALSE);
			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			// Register Joda deserialization module because of creationTimestamp of k8s object
			
			mapper.registerModule(new JodaModule());
			ArrayList<UserCR> userList = mapper.readValue((new Gson()).toJson(respJson.get("items")), new TypeReference<ArrayList<UserCR>>() {});

			for(UserCR user : userList) {
				int userResourceVersion = Integer.parseInt(user.getMetadata().getResourceVersion());
				userLatestResourceVersion = (userLatestResourceVersion >= userResourceVersion) ? userLatestResourceVersion : userResourceVersion;
			}
    	} catch (ApiException e) {
        	logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	logger.info("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
        }
    	
		logger.info("User Latest resource version: " + userLatestResourceVersion);

		// registry
		int registryLatestResourceVersion = 0;

		try {
			Object response = customObjectApi.listClusterCustomObject(
					Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, 
					//					"hypercloud-system",
					Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
					null, null, null, null, null, null, null, Boolean.FALSE);
			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			// Register Joda deserialization module because of creationTimestamp of k8s object
			mapper.registerModule(new JodaModule());
			ArrayList<Registry> registryList = mapper.readValue((new Gson()).toJson(respJson.get("items")), new TypeReference<ArrayList<Registry>>() {});

			for(Registry registry : registryList) {
				int registryResourceVersion = Integer.parseInt(registry.getMetadata().getResourceVersion());
				registryLatestResourceVersion = (registryLatestResourceVersion >= registryResourceVersion) ? registryLatestResourceVersion : registryResourceVersion;
			}
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Registry Latest resource version: " + registryLatestResourceVersion);

		// registry pod
		int registryPodLatestResourceVersion = 0;

		try {
			
			V1PodList registryPodList = api.listPodForAllNamespaces(null, null, null, "app=registry", null, null, null, null, Boolean.FALSE);
			for(V1Pod pod : registryPodList.getItems()) {
				int registryPodResourceVersion = Integer.parseInt(pod.getMetadata().getResourceVersion());
				registryPodLatestResourceVersion = (registryPodLatestResourceVersion >= registryPodResourceVersion) ? registryPodLatestResourceVersion : registryPodResourceVersion;
			}
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Registry Pod Latest resource version: " + registryPodLatestResourceVersion);

		// Operator
		int templateLatestResourceVersion = 0;
		try {
			Object result = templateApi.listClusterCustomObject(
					Constants.CUSTOM_OBJECT_GROUP, 
					Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, 
					null, null, null, null, null, null, null, Boolean.FALSE);
			
			String JsonInString = gson.toJson(result);
			JsonFactory factory = mapper.getFactory();
			com.fasterxml.jackson.core.JsonParser parser = factory.createParser(JsonInString);
			JsonNode customObjectList = mapper.readTree(parser);
			
			if(customObjectList.get("items").isArray()) {
				for(JsonNode instance : customObjectList.get("items")) {
					int templateResourceVersion = instance.get("metadata").get("resourceVersion").asInt();
					templateLatestResourceVersion = (templateLatestResourceVersion >= templateResourceVersion) ? templateLatestResourceVersion : templateResourceVersion;
				}
			}
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		
		logger.info("Template Latest resource version: " + templateLatestResourceVersion);
		
		int instanceLatestResourceVersion = 0;
		try {
			Object result = templateApi.listClusterCustomObject(
					Constants.CUSTOM_OBJECT_GROUP, 
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, 
					null, null, null, null, null, null, null, Boolean.FALSE);
			
			String JsonInString = gson.toJson(result);
			JsonFactory factory = mapper.getFactory();
			com.fasterxml.jackson.core.JsonParser parser = factory.createParser(JsonInString);
			JsonNode customObjectList = mapper.readTree(parser);
			
			if(customObjectList.get("items").isArray()) {
				for(JsonNode instance : customObjectList.get("items")) {
					int instanceResourceVersion = instance.get("metadata").get("resourceVersion").asInt();
					instanceLatestResourceVersion = (instanceLatestResourceVersion >= instanceResourceVersion) ? instanceLatestResourceVersion : instanceResourceVersion;
				}
			}
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		
		logger.info("Instance Latest resource version: " + instanceLatestResourceVersion);

		// Get NamespaceClaim LatestResourceVersion
		int nscLatestResourceVersion = getLatestResourceVersion( Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM );
		// Get NamespaceClaim LatestResourceVersion
		int rqcLatestResourceVersion = getLatestResourceVersion( Constants.CUSTOM_OBJECT_PLURAL_RESOURCEQUOTACLAIM );
		// Get RoleBindingClaim LatestResourceVersion
		int rbcLatestResourceVersion = getLatestResourceVersion( Constants.CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM );
		
		// Start user watch
		logger.info("Start user watcher");
		UserWatcher userWatcher = new UserWatcher(k8sClient, customObjectApi, String.valueOf(userLatestResourceVersion));
		userWatcher.start();

		// Start registry watch
		logger.info("Start registry watcher");
		RegistryWatcher registryWatcher = new RegistryWatcher(k8sClient, customObjectApi, String.valueOf(registryLatestResourceVersion));
		registryWatcher.start();

		// Start registry pod watch
		logger.info("Start registry pod watcher");
		RegistryPodWatcher registryPodWatcher = new RegistryPodWatcher(k8sClient, api, String.valueOf(registryPodLatestResourceVersion));
		registryPodWatcher.start();

		// Start Operator
		logger.info("Start Template Operator");
		TemplateOperator templateOperator = new TemplateOperator(k8sClient, templateApi, templateLatestResourceVersion);
		templateOperator.start();
		
		logger.info("Start Instance Operator");
		InstanceOperator instanceOperator = new InstanceOperator(k8sClient, templateApi, instanceLatestResourceVersion);
		instanceOperator.start();
		
		// Start NamespaceClaim Controller
		logger.info("Start NamespaceClaim Controller");
		NamespaceClaimController nscOperator = new NamespaceClaimController(k8sClient, customObjectApi, nscLatestResourceVersion);
		nscOperator.start();
		
		// Start ResourceQuotaClaim Controller
		logger.info("Start ResourceQuotaClaim Controller");
		ResourceQuotaClaimController rqcOperator = new ResourceQuotaClaimController(k8sClient, customObjectApi, rqcLatestResourceVersion);
		rqcOperator.start();
		
		// Start RoleBindingClaim Controller
		logger.info("Start RoleBindingClaim Controller");
		RoleBindingClaimController rbcOperator = new RoleBindingClaimController(k8sClient, customObjectApi, rbcLatestResourceVersion);
		rbcOperator.start();

		while(true) {
			if(!userWatcher.isAlive()) {
				String userLatestResourceVersionStr = UserWatcher.getLatestResourceVersion();
				logger.info("User watcher is not alive. Restart user watcher! (Latest resource version: " + userLatestResourceVersionStr + ")");
				userWatcher.interrupt();
				userWatcher = new UserWatcher(k8sClient, customObjectApi, userLatestResourceVersionStr);
				userWatcher.start();
			}

			if(!registryWatcher.isAlive()) {
				String registryLatestResourceVersionStr = RegistryWatcher.getLatestResourceVersion();
				logger.info("Registry watcher is not alive. Restart registry watcher! (Latest resource version: " + registryLatestResourceVersionStr + ")");
				registryWatcher.interrupt();
				registryWatcher = new RegistryWatcher(k8sClient, customObjectApi, registryLatestResourceVersionStr);
				registryWatcher.start();
			}

			
			if(!registryPodWatcher.isAlive()) {
				String registryPodLatestResourceVersionStr = RegistryPodWatcher.getLatestResourceVersion();
				logger.info("Registry pod watcher is not alive. Restart registry pod watcher! (Latest resource version: " + registryPodLatestResourceVersionStr + ")");
				registryPodWatcher.interrupt();
				registryPodWatcher = new RegistryPodWatcher(k8sClient, api, registryPodLatestResourceVersionStr);
				registryPodWatcher.start();
			}
			
			
			if(!templateOperator.isAlive()) {
				templateLatestResourceVersion = TemplateOperator.getLatestResourceVersion();
				logger.info(("Template Operator is not Alive. Restart Operator! (Latest Resource Version: " + templateLatestResourceVersion + ")"));
				templateOperator.interrupt();
				templateOperator = new TemplateOperator(k8sClient, templateApi, templateLatestResourceVersion);
				templateOperator.start();
			}
			
			if(!instanceOperator.isAlive()) {
				instanceLatestResourceVersion = InstanceOperator.getLatestResourceVersion();
				logger.info(("Instance Operator is not Alive. Restart Operator! (Latest Resource Version: " + instanceLatestResourceVersion + ")"));
				instanceOperator.interrupt();
				instanceOperator = new InstanceOperator(k8sClient, templateApi, instanceLatestResourceVersion);
				instanceOperator.start();
			}
			
			if(!nscOperator.isAlive()) {
				nscLatestResourceVersion = NamespaceClaimController.getLatestResourceVersion();
				logger.info(("Namespace Claim Controller is not Alive. Restart Controller! (Latest Resource Version: " + nscLatestResourceVersion + ")"));
				nscOperator.interrupt();
				nscOperator = new NamespaceClaimController(k8sClient, customObjectApi, nscLatestResourceVersion);
				nscOperator.start();
			}
			
			if(!rqcOperator.isAlive()) {
				rqcLatestResourceVersion = ResourceQuotaClaimController.getLatestResourceVersion();
				logger.info(("ResourceQuota Claim Controller is not Alive. Restart Controller! (Latest Resource Version: " + rqcLatestResourceVersion + ")"));
				rqcOperator.interrupt();
				rqcOperator = new ResourceQuotaClaimController(k8sClient, customObjectApi, rqcLatestResourceVersion);
				rqcOperator.start();
			}
			
			if(!rbcOperator.isAlive()) {
				rbcLatestResourceVersion = RoleBindingClaimController.getLatestResourceVersion();
				logger.info(("RoleBinding Claim Controller is not Alive. Restart Controller! (Latest Resource Version: " + rbcLatestResourceVersion + ")"));
				rbcOperator.interrupt();
				rbcOperator = new RoleBindingClaimController(k8sClient, customObjectApi, rbcLatestResourceVersion);
				rbcOperator.start();
			}

			Thread.sleep(10000); // Period: 10 sec
    	}
    }
    
    public static UserCR getUser(String userName) throws Exception {
    	UserCR user = new UserCR();
        
        try {
        	Object response = customObjectApi.getClusterCustomObject(
        			Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.CUSTOM_OBJECT_PLURAL_USER, 
					userName);
        	
            JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));
            User userInfo = new ObjectMapper().readValue((new Gson()).toJson(respJson.get("userInfo")), User.class);
            user.setUserInfo(userInfo);
            user.setStatus(respJson.get("status").toString());
        } catch (ApiException e) {
        	logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	logger.info("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
        }
    	
    	return user;
    }
    
    public static TokenCR getToken(String tokenName) throws Exception {
    	TokenCR token = new TokenCR();
        
        try {
        	Object response = customObjectApi.getClusterCustomObject(
        			Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.CUSTOM_OBJECT_PLURAL_TOKEN, 
					tokenName);
        	
            JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));
            String accessToken = new ObjectMapper().readValue((new Gson()).toJson(respJson.get("accessToken")), String.class);
            String refreshToken = new ObjectMapper().readValue((new Gson()).toJson(respJson.get("refreshToken")), String.class);
            
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
        } catch (ApiException e) {
        	logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	logger.info("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
        }
    	
    	return token;
    }
    
    public static Client getClient(Client clientInfo) throws Exception {
        Client dbClientInfo = new Client();
        
        try {
        	logger.info("Name of Client: " + clientInfo.getAppName() + clientInfo.getClientId());
        	
        	Object response = customObjectApi.getClusterCustomObject(
        			Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.CUSTOM_OBJECT_PLURAL_CLIENT, 
					clientInfo.getAppName() + clientInfo.getClientId());
        	
            JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));
            JsonObject clientInfoJson = respJson.get("clientInfo").getAsJsonObject();
            
            dbClientInfo.setAppName( new ObjectMapper().readValue((new Gson()).toJson(clientInfoJson.get("appName")), String.class) );       
            dbClientInfo.setClientId( new ObjectMapper().readValue((new Gson()).toJson(clientInfoJson.get("clientId")), String.class));           
            dbClientInfo.setClientSecret( new ObjectMapper().readValue((new Gson()).toJson(clientInfoJson.get("clientSecret")), String.class) );            
            dbClientInfo.setOriginUri( new ObjectMapper().readValue((new Gson()).toJson(clientInfoJson.get("originUri")), String.class) );
            dbClientInfo.setRedirectUri( new ObjectMapper().readValue((new Gson()).toJson(clientInfoJson.get("redirectUri")), String.class) );
            
        } catch (ApiException e) {
        	logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	logger.info("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
        }
    	
    	return dbClientInfo;
    }
    
    public static void deleteToken(String tokenName) throws Exception {
    	try {
    		V1DeleteOptions body = new V1DeleteOptions();
    		
        	customObjectApi.deleteClusterCustomObject(
        			Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_TOKEN,
					tokenName, 
					body, 0, null, null);
        } catch (ApiException e) {
        	logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	logger.info("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
        }
    }
    
    public static void saveClient(Client clientInfo) throws Exception {
    	
    	try {
    		ClientCR clientCR = new ClientCR();
    		
    		// Set name & label
        	V1ObjectMeta metadata = new V1ObjectMeta();
        	metadata.setName(clientInfo.getAppName() + clientInfo.getClientId());        	
//        	Map<String, String> label = new HashMap<>();   있어야할지 판단 안됨
//        	label.put("client", );
        	clientCR.setMetadata(metadata);
        	
        	// Set ClientInfo
        	clientCR.setClientInfo(clientInfo);
       	
        	// Make body
        	JSONParser parser = new JSONParser();        	
        	JSONObject bodyObj = (JSONObject) parser.parse(new Gson().toJson(clientCR));
        	
        	customObjectApi.createClusterCustomObject(
        			Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.CUSTOM_OBJECT_PLURAL_CLIENT,
					bodyObj, null);
        } catch (ApiException e) {
        	logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	logger.info("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
        }
    }
    
    public static void saveToken(String userId, String tokenId, String accessToken, String refreshToken) throws Exception {
    	String tokenName = userId + "-" + tokenId;
    	
    	try {
    		TokenCR token = new TokenCR();
    		
    		// Set name & label
        	V1ObjectMeta metadata = new V1ObjectMeta();
        	metadata.setName(tokenName);        	
        	Map<String, String> label = new HashMap<>();
        	label.put("user", userId);
        	metadata.setLabels(label);        	
        	token.setMetadata(metadata);
        	
        	// Set tokens
        	token.setAccessToken(accessToken);
        	token.setRefreshToken(refreshToken);
        	
        	// Make body
        	JSONParser parser = new JSONParser();        	
        	JSONObject bodyObj = (JSONObject) parser.parse(new Gson().toJson(token));
        	
        	customObjectApi.createClusterCustomObject(
        			Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.CUSTOM_OBJECT_PLURAL_TOKEN,
					bodyObj, null);
        } catch (ApiException e) {
        	logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	logger.info("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
        }
    }
    
    public static void updateAccessToken(String tokenName, String accessToken) throws Exception {
        try {
        	String jsonPatchStr = "[{\"op\":\"replace\",\"path\":\"/accessToken\",\"value\": " + accessToken + "}]";
			JsonElement jsonPatch = (JsonElement) new JsonParser().parse(jsonPatchStr);

			customObjectApi.patchClusterCustomObject(
					Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.CUSTOM_OBJECT_PLURAL_TOKEN,
					tokenName, jsonPatch);
        } catch (ApiException e) {
        	logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	logger.info("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
        }
    }
    
    public static void encryptUserPassword(String userId, String password, UserCR user) throws Exception {
        try {
        	// Encrypt password
        	String passwordSalt = UUID.randomUUID().toString();        	
        	String encryptedPassword = Util.Crypto.encryptSHA256(password + user.getUserInfo().getEmail() + passwordSalt);
        	
        	// Patch user CR
        	user.getUserInfo().setPassword(encryptedPassword);
        	user.getUserInfo().setPasswordSalt(passwordSalt);
        	Map<String, String> label = user.getMetadata().getLabels();
        	label.put("encrypted", "t");
        	user.getMetadata().setLabels(label);
        	
        	customObjectApi.replaceClusterCustomObject(
        			Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.CUSTOM_OBJECT_PLURAL_USER,
					userId, user);        	
        } catch (ApiException e) {
        	logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	logger.info("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
        }
    }
	@SuppressWarnings("unchecked")
	public static void initRegistry(String registryId, Registry registry) throws Throwable {
		
		String namespace = registry.getMetadata().getNamespace();

		JSONObject patchStatus = new JSONObject();
		JSONObject status = new JSONObject();
		JSONArray conditions = new JSONArray();
		JSONObject condition = new JSONObject();
		JSONArray patchStatusArray = new JSONArray();
		
		
		condition.put("type", "Phase");
		condition.put("status", RegistryStatus.REGISTRY_PHASE_CREATING);
		condition.put("message", "Registry is creating");
		condition.put("reason", "All resources in registry has not yet been created.");
		conditions.add(condition);
		status.put("conditions", conditions);
		status.put("phase", RegistryStatus.REGISTRY_PHASE_CREATING);

		patchStatus.put("op", "add");
		patchStatus.put("path", "/status");
		patchStatus.put("value", status);
		patchStatusArray.add(patchStatus);
		
		try{
			Object result = customObjectApi.patchNamespacedCustomObjectStatus(
					Constants.CUSTOM_OBJECT_GROUP, 
					Constants.CUSTOM_OBJECT_VERSION, 
					namespace, 
					Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
					registry.getMetadata().getName(), patchStatusArray);
			logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
		} catch (ApiException e) {
			throw new Exception(e.getResponseBody());
		}
		
	}

	public static void deleteRegistry(Registry registry) throws Throwable {
		try {
			logger.info("delete Registry Replica Set");
			appApi.deleteNamespacedReplicaSet(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + registry.getMetadata().getName(), registry.getMetadata().getNamespace(), null, null, null, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
		}
		
		try {
			logger.info("delete Secret");
			deleteSecret(registry.getMetadata().getNamespace(), registry.getMetadata().getName(), null);
			deleteSecret(registry.getMetadata().getNamespace(), registry.getMetadata().getName(), Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
		}
		
		try {
			logger.info("delete Service");
			api.deleteNamespacedService(Constants.K8S_PREFIX + registry.getMetadata().getName(), registry.getMetadata().getNamespace(), null, null, null, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
		}
		
		try {
			logger.info("delete PVC");
			api.deleteNamespacedPersistentVolumeClaim(Constants.K8S_PREFIX + registry.getMetadata().getName(), registry.getMetadata().getNamespace(), null, null, 0, null, null, new  V1DeleteOptions());
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
		}
	}

	@SuppressWarnings("unchecked")
	public static void createRegistry(Registry registry) throws Throwable {
		try {
			String namespace = registry.getMetadata().getNamespace();
			String registryId = registry.getMetadata().getName();
			RegistryService regService = registry.getSpec().getService();
			String registryIP = "";
			int registryPort = 0;
			
			// set default
			int registrySVCTargetPort = RegistryService.REGISTRY_TARGET_PORT;
			int registrySVCPort = registry.getSpec().getService().getPort() == 0 ? registrySVCTargetPort :  registry.getSpec().getService().getPort();
			int registrySVCNodePort = regService.getNodePort();
			
			if( regService.getType().equals(RegistryService.SVC_TYPE_NODE_PORT) ) {
				// If Registry Node IP is null
				if( StringUtil.isEmpty(regService.getNodeIP()) ) {
					V1NodeList nodes = api.listNode(null, null, null, null, null, null, null, null, null);
					for( V1Node node : nodes.getItems() ) {
						for( V1NodeAddress address : node.getStatus().getAddresses() ) {
							if( address.getType().equals("InternalIP") ) {
								registryIP = address.getAddress();
								logger.info("[registryIP]:" + registryIP);
								break;
							}
						}
						if( StringUtil.isNotEmpty(registryIP) ) {
							break;
						}
					}
				}
				else {
					registryIP = regService.getNodeIP();
				}
			}
			
			// ----- Create Loadbalancer
			V1Service lb = new V1Service();
			V1ObjectMeta lbMeta = new V1ObjectMeta();
			V1ServiceSpec lbSpec = new V1ServiceSpec();
			List<V1ServicePort> ports = new ArrayList<>();
			Map<String, String> selector = new HashMap<String, String>();

			lbMeta.setName(Constants.K8S_PREFIX + registryId);
			
			List<V1OwnerReference> ownerRefs = new ArrayList<>();
			V1OwnerReference ownerRef = new V1OwnerReference();
			
			ownerRef.setApiVersion(registry.getApiVersion());
			ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
			ownerRef.setController(Boolean.TRUE);
			ownerRef.setKind(registry.getKind());
			ownerRef.setName(registry.getMetadata().getName());
			ownerRef.setUid(registry.getMetadata().getUid());
			ownerRefs.add(ownerRef);
			
			lbMeta.setOwnerReferences(ownerRefs);
			
			lb.setMetadata(lbMeta);

			V1ServicePort v1port = new V1ServicePort();
			v1port.setProtocol(RegistryService.REGISTRY_PORT_PROTOCOL);
			v1port.setPort(registrySVCPort);
			v1port.setName(RegistryService.REGISTRY_PORT_NAME);
			v1port.setTargetPort(new IntOrString(registrySVCPort));
			if( regService.getType().equals(RegistryService.SVC_TYPE_NODE_PORT) ) {
				if( registrySVCNodePort != 0 )
					v1port.setNodePort(registrySVCNodePort);
			}
			
			ports.add(v1port);
			lbSpec.setPorts(ports);

			logger.info("Selector: " + Constants.K8S_PREFIX + registryId + "=lb");
			selector.put(Constants.K8S_PREFIX + registryId, "lb");
			lbSpec.setSelector(selector);

			lbSpec.setType(registry.getSpec().getService().getType());

			lb.setSpec(lbSpec);

			try {
				api.createNamespacedService(namespace, lb, null, null, null);
			} catch (ApiException e) {
				logger.info(e.getResponseBody());

				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_ERROR);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getResponseBody());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_ERROR);

				patchStatus.put("op", "replace");
				patchStatus.put("path", "/status");
				patchStatus.put("value", status);
				patchStatusArray.add(patchStatus);
				
				try{
					Object result = customObjectApi.patchNamespacedCustomObjectStatus(
							Constants.CUSTOM_OBJECT_GROUP, 
							Constants.CUSTOM_OBJECT_VERSION, 
							namespace, 
							Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
							registry.getMetadata().getName(), patchStatusArray);
					logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e2) {
					throw new Exception(e2.getResponseBody());
				}
				
				throw e;
			}

			// TYPE: ClusterIP or LoadBalancer => Get IP
			// TYPE: NodePort => Get Port
			registryPort = registrySVCPort;
			
			int RETRY_CNT = 200;
			V1Service service = null;
			for (int i = 0; i < RETRY_CNT; i++) {
				Thread.sleep(500);
				service = api.readNamespacedService(Constants.K8S_PREFIX + registryId,
						namespace, null, null, null);

				// GET IP
				if (service.getSpec().getType().equals("ClusterIP")) {
					registryIP = service.getSpec().getClusterIP();
					break;
				
				// GET IP
				} else if (service.getSpec().getType().equals("LoadBalancer")) {
					
					if (service.getStatus().getLoadBalancer().getIngress() != null
							&& service.getStatus().getLoadBalancer().getIngress().size() == 1) {
						if (service.getStatus().getLoadBalancer().getIngress().get(0).getHostname() == null) {
							registryIP = service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
						} else {
							registryIP = service.getStatus().getLoadBalancer().getIngress().get(0).getHostname();
						}
						logger.info("[registryIP]:" + registryIP);
						break;
					}
					
				// GET PORT
				} else if (service.getSpec().getType().equals("NodePort")) {
					
					for( V1ServicePort port : service.getSpec().getPorts() ) {
						if(port.getName().equals(RegistryService.REGISTRY_PORT_NAME)) {
							registrySVCNodePort = port.getNodePort();
							registryPort = registrySVCNodePort;
							logger.info("[registryNodePort]:" + registrySVCNodePort);
							break;
						}
					}
					if( registrySVCNodePort != 0) 
						break;
					
				}

				if (i == RETRY_CNT - 1) {
					JSONObject patchStatus = new JSONObject();
					JSONObject status = new JSONObject();
					JSONArray conditions = new JSONArray();
					JSONObject condition = new JSONObject();
					JSONArray patchStatusArray = new JSONArray();
					
					condition.put("type", "Phase");
					condition.put("status", RegistryStatus.REGISTRY_PHASE_ERROR);
					condition.put("message", "Creating a registry is failed");
					condition.put("reason", "Service(LB) is not found");
					conditions.add(condition);
					status.put("conditions", conditions);
					status.put("phase", RegistryStatus.REGISTRY_PHASE_ERROR);

					patchStatus.put("op", "replace");
					patchStatus.put("path", "/status");
					patchStatus.put("value", status);
					patchStatusArray.add(patchStatus);
					
					try{
						Object result = customObjectApi.patchNamespacedCustomObjectStatus(
								Constants.CUSTOM_OBJECT_GROUP, 
								Constants.CUSTOM_OBJECT_VERSION, 
								namespace, 
								Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
								registry.getMetadata().getName(), patchStatusArray);
						logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
					} catch (ApiException e) {
						throw new Exception(e.getResponseBody());
					}
					
					return;
				}
			}


			// ----- Create PVC
			V1PersistentVolumeClaim pvc = new V1PersistentVolumeClaim();
			V1ObjectMeta pvcMeta = new V1ObjectMeta();
			V1PersistentVolumeClaimSpec pvcSpec = new V1PersistentVolumeClaimSpec();
			V1ResourceRequirements pvcResource = new V1ResourceRequirements();
			Map<String, Quantity> limit = new HashMap<>();
			List<String> accessModes = new ArrayList<>();
			
			RegistryPVC registryPVC = registry.getSpec().getPersistentVolumeClaim();
//			String storageClassName = StringUtil.isEmpty(registryPVC.getStorageClassName()) ? RegistryPVC.STORAGE_CLASS_DEFAULT : registryPVC.getStorageClassName();
			String storageClassName = registryPVC.getStorageClassName();
			
			
			pvcMeta.setName(Constants.K8S_PREFIX + registryId);
			
			Map<String, String> pvcLabels = new HashMap<String, String>();
			pvcLabels.put("app", Constants.K8S_PREFIX.substring(0, Constants.K8S_PREFIX.length() - 1));
			pvcMeta.setLabels(pvcLabels);
			
			pvcMeta.setOwnerReferences(ownerRefs);
			
			// [2/5] set storage quota.
			limit.put("storage", new Quantity(registryPVC.getStorageSize()));
			pvcResource.setRequests(limit);
			pvcSpec.setResources(pvcResource);

			pvcSpec.setStorageClassName(storageClassName);
			if(registryPVC.getAccessModes() == null || registryPVC.getAccessModes().size() == 0) {
				accessModes.add(RegistryPVC.ACCESS_MODE_DEFAULT);
			}
			else {
				for(String mode : registryPVC.getAccessModes()) {
					accessModes.add(mode);
				}
			}
			pvcSpec.setAccessModes(accessModes);

			pvc.setMetadata(pvcMeta);
			pvc.setSpec(pvcSpec);

			// create storage.
			try {
				api.createNamespacedPersistentVolumeClaim(namespace, pvc, null, null, null);
			} catch (ApiException e) {
				logger.info(e.getResponseBody());

				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_ERROR);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getResponseBody());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_ERROR);

				patchStatus.put("op", "replace");
				patchStatus.put("path", "/status");
				patchStatus.put("value", status);
				patchStatusArray.add(patchStatus);
				
				try{
					Object result = customObjectApi.patchNamespacedCustomObjectStatus(
							Constants.CUSTOM_OBJECT_GROUP, 
							Constants.CUSTOM_OBJECT_VERSION, 
							namespace, 
							Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
							registry.getMetadata().getName(), patchStatusArray);
					logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e2) {
					throw new Exception(e2.getResponseBody());
				}
				
				throw e;
			}
			
			
			// ----- Create Secret
			// Create Cert Directory
			logger.info("Create Cert Directory");
			String registryDir = createDirectory(namespace, registryId);
			
			// Create Certificates
			logger.info("Create Certificates");
			List<String> commands = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			sb.append("openssl req -newkey rsa:4096 -nodes -sha256 -keyout ");
			sb.append(registryDir + "/" + Constants.CERT_KEY_FILE);
			sb.append(" -x509 -days 1000 -subj \"/C=KR/ST=Seoul/O=tmax/CN=");
			sb.append(registryIP + ":" + registryPort);
			sb.append("\" -config <(cat /etc/ssl/openssl.cnf <(printf \"[v3_ca]\\nsubjectAltName=IP:");
			sb.append(registryIP);
			sb.append("\")) -out ");
			sb.append(registryDir + "/" + Constants.CERT_CRT_FILE);
			commands.clear();
			commands.add("/bin/bash");		// bash Required!!
			commands.add("-c");
			commands.add(sb.toString());
			
			try {
				commandExecute(commands.toArray(new String[commands.size()]));
			}catch (Exception e) {
				logger.info(e.getMessage());
				
				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_ERROR);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getMessage());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_ERROR);

				patchStatus.put("op", "replace");
				patchStatus.put("path", "/status");
				patchStatus.put("value", status);
				patchStatusArray.add(patchStatus);
				
				try{
					Object result = customObjectApi.patchNamespacedCustomObjectStatus(
							Constants.CUSTOM_OBJECT_GROUP, 
							Constants.CUSTOM_OBJECT_VERSION, 
							namespace, 
							Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
							registry.getMetadata().getName(), patchStatusArray);
					logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e2) {
					throw new Exception(e2.getResponseBody());
				}
				
				throw e;
			}


			sb.setLength(0);
			sb.append("\'\'openssl x509 -inform PEM -in ");
			sb.append(registryDir + "/" + Constants.CERT_CRT_FILE);
			sb.append(" -out ");
			sb.append(registryDir + "/" + Constants.CERT_CERT_FILE);
			sb.append("\'\'");
			commands.clear();
			commands.add("/bin/sh");
			commands.add("-c");
			commands.add(sb.toString());
			try {
				commandExecute(commands.toArray(new String[commands.size()]));
			}catch (ApiException e) {
				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_ERROR);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getResponseBody());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_ERROR);

				patchStatus.put("op", "replace");
				patchStatus.put("path", "/status");
				patchStatus.put("value", status);
				patchStatusArray.add(patchStatus);
				
				try{
					Object result = customObjectApi.patchNamespacedCustomObjectStatus(
							Constants.CUSTOM_OBJECT_GROUP, 
							Constants.CUSTOM_OBJECT_VERSION, 
							namespace, 
							Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
							registry.getMetadata().getName(), patchStatusArray);
					logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e2) {
					throw new Exception(e2.getResponseBody());
				}
			
				throw e;
			}

			// Read cert files & Create Secret Object
			logger.info("Read cert files & Create Secret Object");
			Map<String, String> secrets = new HashMap<>();

			secrets.put(Constants.CERT_KEY_FILE, readFile(registryDir + "/" + Constants.CERT_KEY_FILE));
			secrets.put(Constants.CERT_CRT_FILE, readFile(registryDir + "/" + Constants.CERT_CRT_FILE));
			secrets.put(Constants.CERT_CERT_FILE, readFile(registryDir + "/" + Constants.CERT_CERT_FILE));
			secrets.put("ID", registry.getSpec().getLoginId());
			secrets.put("PASSWD", registry.getSpec().getLoginPassword());
			secrets.put("REGISTRY_IP_PORT", registryIP + ":" + registryPort);

			Map<String, String> labels = new HashMap<>();
			labels.put("secret", "cert");
			String secretName;
			
			try {
				// K8SApiCall createSecret
				logger.info("K8SApiCall createSecret");
				secretName = K8sApiCaller.createSecret(namespace, secrets, registryId, labels, null, ownerRefs);
			}catch (ApiException e) {
				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_ERROR);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getResponseBody());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_ERROR);

				patchStatus.put("op", "replace");
				patchStatus.put("path", "/status");
				patchStatus.put("value", status);
				patchStatusArray.add(patchStatus);
				
				try{
					Object result = customObjectApi.patchNamespacedCustomObjectStatus(
							Constants.CUSTOM_OBJECT_GROUP, 
							Constants.CUSTOM_OBJECT_VERSION, 
							namespace, 
							Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
							registry.getMetadata().getName(), patchStatusArray);
					logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e2) {
					throw new Exception(e2.getResponseBody());
				}
				
				throw e;
			}
			
			
			
			// Create docker-config-json Secret Object
			Map<String, String> secrets2 = new HashMap<>();
			secrets2.put(Constants.DOCKER_CONFIG_JSON_FILE, createConfigJson(registryIP, registryPort, registry.getSpec().getLoginId(), registry.getSpec().getLoginPassword()));

			Map<String, String> labels2 = new HashMap<>();
			labels2.put("secret", "docker");
			try {
				K8sApiCaller.createSecret(namespace, secrets2, registryId, labels2, Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON, ownerRefs);
			}catch (ApiException e) {
				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_ERROR);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getResponseBody());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_ERROR);

				patchStatus.put("op", "replace");
				patchStatus.put("path", "/status");
				patchStatus.put("value", status);
				patchStatusArray.add(patchStatus);
				
				try{
					Object result = customObjectApi.patchNamespacedCustomObjectStatus(
							Constants.CUSTOM_OBJECT_GROUP, 
							Constants.CUSTOM_OBJECT_VERSION, 
							namespace, 
							Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
							registry.getMetadata().getName(), patchStatusArray);
					logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e2) {
					throw new Exception(e2.getResponseBody());
				}
				
				throw e;
			}
			

			// ----- Create Registry Replica Set
			V1ReplicaSetBuilder rsBuilder = new V1ReplicaSetBuilder();

			// 1. metadata
			V1ObjectMeta rsMeta = new V1ObjectMeta();

			// 1-1. replica set name
			rsMeta.setName(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + registryId);
			logger.info("RS Name: " + rsMeta.getName());
			
			// 1-2. replica set owner ref
			rsMeta.setOwnerReferences(ownerRefs);
			
			rsBuilder.withMetadata(rsMeta);
			
			// 2. spec
			V1ReplicaSetSpec rsSpec = new V1ReplicaSetSpec();

			// 2-1. replicas
			rsSpec.setReplicas(1);

			// 2-2. pod template spec
			V1PodTemplateSpec podTemplateSpec = new V1PodTemplateSpec();

			// 2-2-1. pod metadata
			V1ObjectMeta podMeta = new V1ObjectMeta();

			// 2-2-1-1. pod label
			logger.info("<Pod Label List>");
			Map<String, String> podLabels = new HashMap<String, String>();
			podLabels.put("app", "registry");
			podLabels.put("apps", rsMeta.getName());
			logger.info("app: registry" );
			logger.info("apps: " + rsMeta.getName());

			podLabels.put(Constants.K8S_PREFIX + registryId, "lb");
			logger.info(Constants.K8S_PREFIX + registryId + ": lb");

			podMeta.setLabels(podLabels);
			podTemplateSpec.setMetadata(podMeta);

			// 2-2-2. pod spec
			V1PodSpec podSpec = new V1PodSpec();

			V1Container container = new V1Container();

			// 2-2-2-2-1. container name
			container.setName(Constants.K8S_PREFIX + registryId.toLowerCase());
			logger.info("<Container Name: " + container.getName() + ">");

			// 2-2-2-2-2. image
			container.setImage(registry.getSpec().getImage());
			logger.info("- Image: " + container.getImage());
			container.setImagePullPolicy("IfNotPresent");

			// Set a Lifecycle 
			V1Lifecycle lifecycle = new V1Lifecycle();
			V1Handler postStart = new V1Handler();
			V1ExecAction exec = new V1ExecAction();
			List<String> command = new ArrayList<>();
			command.add("/bin/sh");
			command.add("-c");
			command.add("/auth.sh $ID $PASSWD");

			exec.setCommand(command);
			postStart.setExec(exec);
			lifecycle.setPostStart(postStart);

			container.setLifecycle(lifecycle);

			// 2-2-2-2-3. spec
			V1ResourceRequirements resrcReq = new V1ResourceRequirements();
			limit = new HashMap<String, Quantity>();
			limit.put("cpu", new Quantity(Constants.REGISTRY_CPU_STRING));
			limit.put("memory", new Quantity(String.valueOf(Constants.REGISTRY_MEMORY_STRING)));
			resrcReq.setLimits(limit);
			container.setResources(resrcReq);

			// 2-2-2-2-4. env
			// Default env
			V1EnvVar env1 = new V1EnvVar();
			env1.setName("REGISTRY_AUTH");
			env1.setValue("htpasswd");
			container.addEnvItem(env1);

			V1EnvVar env2 = new V1EnvVar();
			env2.setName("REGISTRY_AUTH_HTPASSWD_REALM");
			env2.setValue("Registry Realm");
			container.addEnvItem(env2);

			V1EnvVar env3 = new V1EnvVar();
			env3.setName("REGISTRY_AUTH_HTPASSWD_PATH");
			env3.setValue("/auth/htpasswd");
			container.addEnvItem(env3);

			V1EnvVar env4 = new V1EnvVar();
			env4.setName("REGISTRY_HTTP_ADDR");
			env4.setValue("0.0.0.0:" + registryPort);
			container.addEnvItem(env4);

			V1EnvVar env5 = new V1EnvVar();
			env5.setName("REGISTRY_HTTP_TLS_CERTIFICATE");
			env5.setValue("/certs/localhub.crt");
			container.addEnvItem(env5);

			V1EnvVar env6 = new V1EnvVar();
			env6.setName("REGISTRY_HTTP_TLS_KEY");
			env6.setValue("/certs/localhub.key");
			container.addEnvItem(env6);

			V1EnvVar env7 = new V1EnvVar();
			env7.setName("REGISTRY_IP_PORT");
			env7.setValue(registryIP + ":" + registryPort);
			container.addEnvItem(env7);

			// env
			V1EnvVar secretEnv1 = new V1EnvVar();
			secretEnv1.setName("ID");
			V1EnvVarSource valueFrom = new V1EnvVarSource();
			V1SecretKeySelector secretKeyRef = new V1SecretKeySelector ();
			secretKeyRef.setName(Constants.K8S_PREFIX + registryId);
			secretKeyRef.setKey("ID");
			valueFrom.setSecretKeyRef(secretKeyRef);
			secretEnv1.setValueFrom(valueFrom);
			container.addEnvItem(secretEnv1);

			V1EnvVar secretEnv2 = new V1EnvVar();
			secretEnv2.setName("PASSWD");
			V1EnvVarSource valueFrom2 = new V1EnvVarSource();
			V1SecretKeySelector secretKeyRef2 = new V1SecretKeySelector ();
			secretKeyRef2.setName(Constants.K8S_PREFIX + registryId);
			secretKeyRef2.setKey("PASSWD");
			valueFrom2.setSecretKeyRef(secretKeyRef2);
			secretEnv2.setValueFrom(valueFrom2);
			container.addEnvItem(secretEnv2);

			// 2-2-2-2-5. port
			V1ContainerPort portsItem = new V1ContainerPort();
			portsItem.setContainerPort(registrySVCTargetPort);
			logger.info("Container Port: " + portsItem.getContainerPort());

			portsItem.setName(RegistryService.REGISTRY_PORT_NAME);
			portsItem.setProtocol(RegistryService.REGISTRY_PORT_PROTOCOL);
			container.addPortsItem(portsItem);

			// Secret Volume mount
			V1VolumeMount certMount = new V1VolumeMount();
			certMount.setName("certs");
			certMount.setMountPath("/certs");
			container.addVolumeMountsItem(certMount);

			// Registry Volume mount
			V1VolumeMount registryMount = new V1VolumeMount();
			registryMount.setName("registry");
			registryMount.setMountPath("/var/lib/registry");
			container.addVolumeMountsItem(registryMount);

			// Get loginAuth For Readiness Probe
			String loginAuth = registry.getSpec().getLoginId() + ":" + registry.getSpec().getLoginPassword();
			loginAuth = new String( Base64.encodeBase64( loginAuth.getBytes() ) );
			
			// Set Readiness Probe
			V1Probe readinessProbe = new V1Probe();
			V1HTTPGetAction httpGet = new V1HTTPGetAction();
			List<V1HTTPHeader> headers = new ArrayList<V1HTTPHeader>();
			V1HTTPHeader authHeader = new V1HTTPHeader();
			authHeader.setName("authorization");
			authHeader.setValue("Basic " + loginAuth);
			headers.add(authHeader);
			
			httpGet.setPath("v2/_catalog");
			httpGet.setPort(new IntOrString(registryPort));
			httpGet.setScheme("HTTPS");
			httpGet.setHttpHeaders(headers);
			readinessProbe.setHttpGet(httpGet);
			
			readinessProbe.setFailureThreshold(10);
			readinessProbe.setInitialDelaySeconds(5);
			readinessProbe.setPeriodSeconds(3);
			readinessProbe.setSuccessThreshold(1);
			readinessProbe.setTimeoutSeconds(1);
			container.setReadinessProbe(readinessProbe);

			// Set Liveness Probe
			V1Probe livenessProbe = new V1Probe();
			V1HTTPGetAction httpGet2 = new V1HTTPGetAction();
			List<V1HTTPHeader> headers2 = new ArrayList<V1HTTPHeader>();
			V1HTTPHeader authHeader2 = new V1HTTPHeader();
			authHeader2.setName("authorization");
			authHeader2.setValue("Basic " + loginAuth);
			headers2.add(authHeader2);

			httpGet2.setPath("v2/_catalog");
			httpGet2.setPort(new IntOrString(registryPort));
			httpGet2.setScheme("HTTPS");
			httpGet2.setHttpHeaders(headers2);
			livenessProbe.setHttpGet(httpGet2);

			livenessProbe.setFailureThreshold(10);
			livenessProbe.setInitialDelaySeconds(5);
			livenessProbe.setPeriodSeconds(5);
			livenessProbe.setSuccessThreshold(1);
			livenessProbe.setTimeoutSeconds(30);
			container.setReadinessProbe(livenessProbe);

			podSpec.addContainersItem(container);

			// Secret Volume
			List<V1Volume> volumes = new ArrayList<>();
			V1Volume certVolume = new V1Volume();
			certVolume.setName("certs");
			V1SecretVolumeSource volSecret = new V1SecretVolumeSource();
			logger.info("secret name: " + secretName);
			volSecret.setSecretName(secretName);
			certVolume.setSecret(volSecret);
			volumes.add(certVolume);

			// Registry Volume
			V1Volume registryVolume = new V1Volume();
			registryVolume.setName("registry");
			V1PersistentVolumeClaimVolumeSource regPvc = new V1PersistentVolumeClaimVolumeSource();
			regPvc.setClaimName(Constants.K8S_PREFIX + registryId);
			registryVolume.setPersistentVolumeClaim(regPvc);

			volumes.add(registryVolume);

			podSpec.setVolumes(volumes);

			// 2-2-2-5. restart policy
			podSpec.setRestartPolicy("Always");
			logger.info("Restart Policy: " + podSpec.getRestartPolicy());

			podTemplateSpec.setSpec(podSpec);
			rsSpec.setTemplate(podTemplateSpec);

			// 2-3. label selector
			V1LabelSelector labelSelector = new V1LabelSelector();
			logger.info("<RS Label List>");
			Map<String, String> rsLabels = new HashMap<String, String>();
			rsLabels.put("app", "registry");
			rsLabels.put("apps", rsMeta.getName());
			logger.info("app: registry");
			logger.info("apps: " + rsMeta.getName());
			labelSelector.setMatchLabels(rsLabels);

			rsSpec.setSelector(labelSelector);

			rsBuilder.withSpec(rsSpec);

			try {
				logger.info("Create ReplicaSet");
				appApi.createNamespacedReplicaSet(namespace, rsBuilder.build(),
						null, null, null);
			} catch (ApiException e) {
				logger.info("Create Replicaset Failed");
				logger.info(e.getResponseBody());
				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_ERROR);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getResponseBody());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_ERROR);

				patchStatus.put("op", "replace");
				patchStatus.put("path", "/status");
				patchStatus.put("value", status);
				patchStatusArray.add(patchStatus);
				
				try{
					Object result = customObjectApi.patchNamespacedCustomObjectStatus(
							Constants.CUSTOM_OBJECT_GROUP, 
							Constants.CUSTOM_OBJECT_VERSION, 
							namespace, 
							Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
							registry.getMetadata().getName(), patchStatusArray);
					logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e2) {
					throw new Exception(e2.getResponseBody());
				}
				
				throw e;
			}
//			
//			// Check if Registry Pod is Running
//			int retryCount = 0;
//			RETRY_CNT = 200;
//			V1Pod pod = null;
//			V1PodList pods = null;
//			while (++retryCount <= RETRY_CNT) {
//				logger.info("Pod is not Running... Retry Count [" + retryCount + "/" + RETRY_CNT + "]");
//				try {
//					pods = api.listNamespacedPod(namespace, null, null, null, null,
//							"apps=" + rsMeta.getName(), null, null, null, false);
//				} catch (ApiException e) {
//					logger.info("Create Replicaset Failed");
//					logger.info(e.getResponseBody());
//					
//					JSONObject patchStatus = new JSONObject();
//					JSONObject status = new JSONObject();
//					JSONArray conditions = new JSONArray();
//					JSONObject condition = new JSONObject();
//					JSONArray patchStatusArray = new JSONArray();
//					
//					condition.put("type", "Phase");
//					condition.put("status", RegistryStatus.REGISTRY_PHASE_ERROR);
//					condition.put("message", "Creating a registry is failed");
//					condition.put("reason", e.getResponseBody());
//					conditions.add(condition);
//					status.put("conditions", conditions);
//					status.put("phase", RegistryStatus.REGISTRY_PHASE_ERROR);
//
//					patchStatus.put("op", "replace");
//					patchStatus.put("path", "/status");
//					patchStatus.put("value", status);
//					patchStatusArray.add(patchStatus);
//					
//					try{
//						Object result = customObjectApi.patchNamespacedCustomObjectStatus(
//								Constants.CUSTOM_OBJECT_GROUP, 
//								Constants.CUSTOM_OBJECT_VERSION, 
//								namespace, 
//								Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
//								registry.getMetadata().getName(), patchStatusArray);
//						logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
//					} catch (ApiException e2) {
//						throw new Exception(e2.getResponseBody());
//					}
//					
//					throw e;
//				}
//
//				if (pods.getItems() != null && !pods.getItems().isEmpty()) {
//					pod = pods.getItems().get(0);
//					if (pod.getStatus().getPhase().equals("Running")) {
//						logger.info("Pod is Running !!!!!!");
//						logger.info("Pod Name: " + pod.getMetadata().getName());
//						break;
//					}
//				}
//
//				Thread.sleep(500);
//			}
//
//			if (retryCount > RETRY_CNT) {
//				logger.info("Pod Running is Fail");
//				logger.info("Create Replicaset Failed");
//				JSONObject patchStatus = new JSONObject();
//				JSONObject status = new JSONObject();
//				JSONArray conditions = new JSONArray();
//				JSONObject condition = new JSONObject();
//				JSONArray patchStatusArray = new JSONArray();
//				
//				condition.put("type", "Phase");
//				condition.put("status", RegistryStatus.REGISTRY_PHASE_ERROR);
//				condition.put("message", "Creating a registry is failed");
//				condition.put("reason", "Pod is not running");
//				conditions.add(condition);
//				status.put("conditions", conditions);
//				status.put("phase", RegistryStatus.REGISTRY_PHASE_ERROR);
//
//				patchStatus.put("op", "replace");
//				patchStatus.put("path", "/status");
//				patchStatus.put("value", status);
//				patchStatusArray.add(patchStatus);
//				
//				try{
//					Object result = customObjectApi.patchNamespacedCustomObjectStatus(
//							Constants.CUSTOM_OBJECT_GROUP, 
//							Constants.CUSTOM_OBJECT_VERSION, 
//							namespace, 
//							Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
//							registry.getMetadata().getName(), patchStatusArray);
//					logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
//				} catch (ApiException e2) {
//					throw new Exception(e2.getResponseBody());
//				}
//			}
//			
//			JSONObject patchStatus = new JSONObject();
//			JSONObject status = new JSONObject();
//			JSONArray conditions = new JSONArray();
//			JSONObject condition = new JSONObject();
//			JSONArray patchStatusArray = new JSONArray();
//			
//			condition.put("type", "Phase");
//			condition.put("status", RegistryStatus.REGISTRY_PHASE_RUNNING);
//			condition.put("message", "Registry Is Running");
//			condition.put("reason", "All registry resources are operating normally.");
//			conditions.add(condition);
//			status.put("conditions", conditions);
//			status.put("phase", RegistryStatus.REGISTRY_PHASE_RUNNING);
//
//			patchStatus.put("op", "replace");
//			patchStatus.put("path", "/status");
//			patchStatus.put("value", status);
//			patchStatusArray.add(patchStatus);
//			
//			try{
//				Object result = customObjectApi.patchNamespacedCustomObjectStatus(
//						Constants.CUSTOM_OBJECT_GROUP, 
//						Constants.CUSTOM_OBJECT_VERSION, 
//						namespace, 
//						Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
//						registry.getMetadata().getName(), patchStatusArray);
//				logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
//			} catch (ApiException e2) {
//				throw new Exception(e2.getResponseBody());
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}
	
	public static void updateRegistryStatus(V1Pod pod) throws Throwable {
		String registryName = "";
		String registryPrefix = Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX;
		String namespace = pod.getMetadata().getNamespace();
		String reason = "";
		
		registryName = pod.getMetadata().getLabels().get("apps");
		registryName = registryName.substring(registryPrefix.length());
		logger.info("registry name: " + registryName);
		
		if( pod.getStatus().getContainerStatuses() != null ) {
			if(pod.getStatus().getContainerStatuses().get(0).getState().getWaiting() != null) {
				logger.info(pod.getStatus().getContainerStatuses().get(0).getState().getWaiting().toString());
				reason = pod.getStatus().getContainerStatuses().get(0).getState().getWaiting().getReason();
			}
			else if(pod.getStatus().getContainerStatuses().get(0).getState().getRunning() != null) {
				logger.info(pod.getStatus().getContainerStatuses().get(0).toString());
				if(pod.getStatus().getContainerStatuses().get(0).getReady()) 
					reason = "Running";
				else 
					reason = "NotReady";
			}
			else if(pod.getStatus().getContainerStatuses().get(0).getState().getTerminated() != null) {
				logger.info(pod.getStatus().getContainerStatuses().get(0).getState().getTerminated().toString());
				reason = pod.getStatus().getContainerStatuses().get(0).getState().getTerminated().getReason();
			}
			else reason = "Unknown";
			
			if(reason == null) reason = "";
			logger.info("registry pod state's reason: " + reason);

			Object response = customObjectApi.getNamespacedCustomObject(
					Constants.CUSTOM_OBJECT_GROUP, 
					Constants.CUSTOM_OBJECT_VERSION, 
					namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName);

			Registry registry = mapper.readValue(gson.toJson(response), Registry.class);
			
			logger.info("REGISTRY RESOURCE VERSION: " + registry.getMetadata().getResourceVersion());
			logger.info("REGISTRY UID: " + registry.getMetadata().getUid());
			
			if( registry.getStatus().getConditions() != null) {
				for( RegistryCondition registryCondition : registry.getStatus().getConditions()) {
					if( registryCondition.getType().equals("Phase")) {

						if( reason.equals("Running") ) {
							JSONObject patchStatus = new JSONObject();
							JSONObject status = new JSONObject();
							JSONArray conditions = new JSONArray();
							JSONObject condition = new JSONObject();
							JSONArray patchStatusArray = new JSONArray();

							condition.put("type", "Phase");
							condition.put("status", RegistryStatus.REGISTRY_PHASE_RUNNING);
							condition.put("message", "Registry is running");
							condition.put("reason", "All registry resources are operating normally.");
							conditions.add(condition);
							status.put("conditions", conditions);
							status.put("phase", RegistryStatus.REGISTRY_PHASE_RUNNING);

							patchStatus.put("op", "replace");
							patchStatus.put("path", "/status");
							patchStatus.put("value", status);
							patchStatusArray.add(patchStatus);

							try{
								Object result = customObjectApi.patchNamespacedCustomObjectStatus(
										Constants.CUSTOM_OBJECT_GROUP, 
										Constants.CUSTOM_OBJECT_VERSION, 
										namespace, 
										Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
										registry.getMetadata().getName(), patchStatusArray);
								logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
							} catch (ApiException e2) {
								throw new Exception(e2.getResponseBody());
							}
						}
						else if(!registryCondition.getStatus().equals(RegistryStatus.REGISTRY_PHASE_CREATING) 
								&& reason.equals("ContainerCreating")) {
							JSONObject patchStatus = new JSONObject();
							JSONObject status = new JSONObject();
							JSONArray conditions = new JSONArray();
							JSONObject condition = new JSONObject();
							JSONArray patchStatusArray = new JSONArray();

							condition.put("type", "Phase");
							condition.put("status", RegistryStatus.REGISTRY_PHASE_NOT_READY);
							condition.put("message", "Reigstry is not ready. Recreate registry if 30 seconds have passed in NotReady state and the registry state has not changed to Running.");
							condition.put("reason", "Registry container is checking for Readiness or container is creating");
							conditions.add(condition);
							status.put("conditions", conditions);
							status.put("phase", RegistryStatus.REGISTRY_PHASE_NOT_READY);

							patchStatus.put("op", "replace");
							patchStatus.put("path", "/status");
							patchStatus.put("value", status);
							patchStatusArray.add(patchStatus);

							try{
								Object result = customObjectApi.patchNamespacedCustomObjectStatus(
										Constants.CUSTOM_OBJECT_GROUP, 
										Constants.CUSTOM_OBJECT_VERSION, 
										namespace, 
										Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
										registry.getMetadata().getName(), patchStatusArray);
								logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
							} catch (ApiException e2) {
								throw new Exception(e2.getResponseBody());
							}
						}
						else if( reason.equals("NotReady") ) {
							JSONObject patchStatus = new JSONObject();
							JSONObject status = new JSONObject();
							JSONArray conditions = new JSONArray();
							JSONObject condition = new JSONObject();
							JSONArray patchStatusArray = new JSONArray();

							condition.put("type", "Phase");
							condition.put("status", RegistryStatus.REGISTRY_PHASE_NOT_READY);
							condition.put("message", "Reigstry is not ready. Recreate registry if 30 seconds have passed in NotReady state and the registry state has not changed to Running.");
							condition.put("reason", "Registry container is checking for Readiness");
							conditions.add(condition);
							status.put("conditions", conditions);
							status.put("phase", RegistryStatus.REGISTRY_PHASE_NOT_READY);

							patchStatus.put("op", "replace");
							patchStatus.put("path", "/status");
							patchStatus.put("value", status);
							patchStatusArray.add(patchStatus);

							try{
								Object result = customObjectApi.patchNamespacedCustomObjectStatus(
										Constants.CUSTOM_OBJECT_GROUP, 
										Constants.CUSTOM_OBJECT_VERSION, 
										namespace, 
										Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
										registry.getMetadata().getName(), patchStatusArray);
								logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
							} catch (ApiException e2) {
								throw new Exception(e2.getResponseBody());
							}
						}
						else if( reason.equals("Error") ) {
							JSONObject patchStatus = new JSONObject();
							JSONObject status = new JSONObject();
							JSONArray conditions = new JSONArray();
							JSONObject condition = new JSONObject();
							JSONArray patchStatusArray = new JSONArray();

							condition.put("type", "Phase");
							condition.put("status", RegistryStatus.REGISTRY_PHASE_ERROR);
							condition.put("message", "Creating a registry is failed");
							condition.put("reason", "Registry pod is error!");
							conditions.add(condition);
							status.put("conditions", conditions);
							status.put("phase", RegistryStatus.REGISTRY_PHASE_ERROR);

							patchStatus.put("op", "replace");
							patchStatus.put("path", "/status");
							patchStatus.put("value", status);
							patchStatusArray.add(patchStatus);

							try{
								Object result = customObjectApi.patchNamespacedCustomObjectStatus(
										Constants.CUSTOM_OBJECT_GROUP, 
										Constants.CUSTOM_OBJECT_VERSION, 
										namespace, 
										Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
										registry.getMetadata().getName(), patchStatusArray);
								logger.info("patchNamespacedCustomObjectStatus result: " + result.toString());
							} catch (ApiException e2) {
								throw new Exception(e2.getResponseBody());
							}
						}

					}


				}
			}

		}

	}
	
	public static CommandExecOut commandExecute(String[] command) throws Throwable {
		ProcessBuilder processBuilder = new ProcessBuilder();
		CommandExecOut cmdOutDo = new CommandExecOut();
		
		// command exec.
		logger.info("command: " + Arrays.asList(command));
		
        processBuilder.command(command);

        try {
            Process process = processBuilder.start();

            ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
			ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
			Thread outThread = new Thread(new Runnable() {
				public void run() {
					try {
						ByteStreams.copy(process.getInputStream(), baosOut);
					} catch (IOException ex) {
						ex.printStackTrace();
						logger.info(ex.getMessage());
						
						StringWriter sw = new StringWriter();
						ex.printStackTrace(new PrintWriter(sw));
						logger.info(sw.toString());
					} finally {
						logger.info("out end");
					}
				}
			});
			outThread.start();

			Thread errThread = new Thread(new Runnable() {
				public void run() {
					try {
						ByteStreams.copy(process.getErrorStream(), baosErr);
					} catch (IOException ex) {
						ex.printStackTrace();
						logger.info(ex.getMessage());
						
						StringWriter sw = new StringWriter();
						ex.printStackTrace(new PrintWriter(sw));
						logger.info(sw.toString());
					} finally {
						logger.info("err end");
					}
				}
			});
			errThread.start();

			// wait for current thread and return immediately if the subprocess has already
			// terminated.
			process.waitFor();

			// wait for any last output;
			outThread.join();
			errThread.join();

			process.getOutputStream().close();
			process.getErrorStream().close();
			process.getInputStream().close();

			// exit process
			process.destroy();

			String outString = new String(baosOut.toByteArray(), Charset.forName("UTF-8"));
			String errString = new String(baosErr.toByteArray(), Charset.forName("UTF-8"));

			logger.info("out: " + outString);
			logger.info("err: " + errString);

			cmdOutDo.setCmdStdOut(outString);
			cmdOutDo.setCmdStdErr(errString);

			logger.info("exit code: " + process.exitValue());
			cmdOutDo.setCmdExitCode(process.exitValue());
            
        } catch (IOException e) {
			logger.info(e.getMessage());
			
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
			throw e;
        } catch (InterruptedException e) {
			logger.info(e.getMessage());
			
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
			throw e;
        }
		return cmdOutDo;
	}

	// type1: null => Opaque
	// type2: kubernetes.io/dockerconfigjson 
	public static String createSecret(String namespace, Map<String, String> secrets, String secretName, Map<String, String> labels, String type, List<V1OwnerReference> ownerRefs) throws Throwable {
		V1Secret secret = new V1Secret();
		secret.setApiVersion("v1");
		secret.setKind("Secret");
		V1ObjectMeta metadata = new V1ObjectMeta();
		
		if( type != null && type.equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
			metadata.setName(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + secretName.toLowerCase());
		} else {
			metadata.setName(Constants.K8S_PREFIX + secretName.toLowerCase());
		}
		
		if( ownerRefs != null) {
			metadata.setOwnerReferences(ownerRefs);
		}

		//			logger.info("== secret map == ");
		for( String key : secrets.keySet()) {
			//				logger.info("[secretMap]" + key + "=" + secrets.get(key));
			secret.putStringDataItem(key, secrets.get(key));
			//				secret.putDataItem(key, secrets.get(key).getBytes(StandardCharsets.UTF_8));
		}

		// 2-2-1-1. pod label
		logger.info("<Pod Label List>");
		Map<String, String> podLabels = new HashMap<String, String>();

		if(labels == null) {
			podLabels.put("secret", "obj");
			podLabels.put("apps", Constants.K8S_PREFIX + secretName);
			logger.info("secret: obj");
			logger.info("apps: " + Constants.K8S_PREFIX + secretName);
		}
		else {
			for(String key : labels.keySet()) {
				podLabels.put(key, labels.get(key));
			}
		}

		metadata.setLabels(podLabels);
		secret.setMetadata(metadata);
		if(type != null) {
			if(type.equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
				secret.setType(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON);
			}
		}

		try {
			V1Secret result;
			Map<String, byte[]> secretMap = new HashMap<>();
			result = api.createNamespacedSecret(namespace, secret, "true", null, null);
			
			logger.info("[result]" + result);

			//				V1Secret secretRet = api.readNamespacedSecret(Constants.K8S_PREFIX + secretName.toLowerCase(), Constants.K8S_PREFIX + domainId.toLowerCase(), null, null, null);

			secretMap = result.getData();
			//				logger.info("== real secret data ==");
			for( String key : secretMap.keySet()) {
				//					logger.info("[secret]" + key + "=" + new String(secretMap.get(key)));
			}

		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}

		return secret.getMetadata().getName();
	}
	
	public static void deleteSecret(String namespace, String secretName, String type) throws Throwable {
		secretName = secretName.toLowerCase();
		
		try {
			if( type != null && type.equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
				api.deleteNamespacedSecret(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + secretName, namespace, null, null, 0, null, null, new V1DeleteOptions());

			}else {
				api.deleteNamespacedSecret(Constants.K8S_PREFIX + secretName, namespace, null, null, 0, null, null, new V1DeleteOptions());
			}
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
		
	}

	private static String readFile(String filePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		StringBuilder stringBuilder = new StringBuilder();
		char[] buffer = new char[1];

		while (reader.read(buffer) != -1) {
			stringBuilder.append(new String(buffer));
			buffer = new char[1];
		}

		reader.close();

		String content = stringBuilder.toString();
		logger.info("[" + filePath + "]:" + content);

		return content;
	}
	
	private static String createConfigJson(String ip, int port, String id, String password) {
		// $(echo "{\"auths\": {\"192.168.6.218:443\": {\"auth\": \"$(echo "tmax:tmax123" | base64)\"}}}" | base64)
		StringBuilder configSb = new StringBuilder();
		configSb.append("{\"auths\": {\"");
		configSb.append(ip + ":" + port);
		configSb.append("\": {\"auth\": \"");
		String auth = id + ":" + password;
		configSb.append( new String( Base64.encodeBase64( auth.getBytes() ) ) );
		configSb.append("\"}}}");
		logger.info("configStr: " + configSb.toString());
		
		return configSb.toString();
//		return new String( Base64.encodeBase64( configSb.toString().getBytes() ) );
	}
	
	private static String createDirectory(String domainId, String registryId) throws IOException {
		Path opensslHome = Paths.get(Constants.OPENSSL_HOME_DIR);
		if (!Files.exists(opensslHome)) {
			Files.createDirectory(opensslHome);
			logger.info("Directory created: " + Constants.OPENSSL_HOME_DIR);
		}

		String domainDir = Constants.OPENSSL_HOME_DIR + "/" + domainId;
		if (!Files.exists(Paths.get(domainDir))) {
			Files.createDirectory(Paths.get(domainDir));
			logger.info("Directory created: " + domainDir);
		}

		String registryDir = Constants.OPENSSL_HOME_DIR + "/" + domainId + "/" + registryId;
		if (!Files.exists(Paths.get(registryDir))) {
			Files.createDirectory(Paths.get(registryDir));
			logger.info("Directory created: " + registryDir);
		}

		Path dockerLoginHome = Paths.get(Constants.DOCKER_LOGIN_HOME_DIR);
		if (!Files.exists(dockerLoginHome)) {
			Files.createDirectory(dockerLoginHome);
			logger.info("Directory created: " + Constants.DOCKER_LOGIN_HOME_DIR);
		}

		return registryDir;
	}
	
	public static Services getCatalog() throws ApiException {
		Services catalog = new Services();
		List<ServiceOffering> serviceList = new ArrayList<ServiceOffering>();
		Object templates = customObjectApi.listNamespacedCustomObject(
				Constants.CUSTOM_OBJECT_GROUP, 
				Constants.CUSTOM_OBJECT_VERSION, 
				Constants.DEFAULT_NAMESPACE,
				Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, 
				null, null, null, null, null, null, null, false);
		
		JsonNode templateList = numberTypeConverter(objectToJsonNode(templates).get("items"));
		logger.info("Catalog Debug 1");

		if(templateList.isArray()) {
			for(JsonNode template : templateList) {
				ServiceOffering service = new ServiceOffering();
				ServiceMetadata serviceMeta = new ServiceMetadata();
				List<ServicePlan> planList = new ArrayList<ServicePlan>();
				
				service.setName(template.get("metadata").get("name").asText());
				service.setId(template.get("metadata").get("name").asText());
				
				if ( template.get("shortDescription") == null ) {
					service.setDescription(template.get("metadata").get("name").asText()); 
				} else {
					service.setDescription(template.get("shortDescription").asText());
				}
				if ( template.get("imageUrl") == null ) {
					serviceMeta.setImageUrl("none"); 
				} else {
					serviceMeta.setImageUrl(template.get("imageUrl").asText());
				}
				if ( template.get("longDescription") == null ) {
					serviceMeta.setLongDescription(template.get("metadata").get("name").asText()); 
				} else {
					serviceMeta.setLongDescription(template.get("longDescription").asText());
				}
				if ( template.get("provider") == null ) {
					serviceMeta.setProviderDisplayName(template.get("metadata").get("name").asText()); 
				} else {
					serviceMeta.setProviderDisplayName(template.get("provider").asText());
				}
				
				if ( template.get("recommend") != null ) {
					serviceMeta.setRecommend( template.get("recommend").asBoolean() );
				} else {
					serviceMeta.setRecommend( false );
				}
				
				List<String> tags = new ArrayList<String>();
				for(JsonNode tag : template.get("tags")) {
					tags.add(tag.asText());
				}
				service.setTags(tags);
				
				
				service.setMetadata(serviceMeta);
				logger.info("Catalog Debug 2");
				JsonNode objectKinds = template.get("objectKinds");
				if(objectKinds.isArray()) {
					List<String> kinds = null;
					ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {});
					try {
						kinds = reader.readValue(objectKinds);
					} catch (IOException e) {
						logger.info(e.getMessage());;
					}
					
					if(kinds.contains("Secret") || kinds.contains("Service (LoadBalancer)")) {
						service.setBindable(true);
					}
				}
				
				service.setBindings_retrievable(false);
				service.setInstances_retrievable(false);
				logger.info("Catalog Debug 3");
				try {
					JsonNode plans = template.get("plans");
					if(plans.isArray()) {
						for(JsonNode plan : plans) {
							try {
								ServicePlan servicePlan = new ServicePlan();
								PlanMetadata planMeta = new PlanMetadata();
								List<String> bullets = new ArrayList<String>();
								Cost planCost = new Cost();
								Schemas planSchema = new Schemas();
								ServiceInstanceSchema instanceSchema = new ServiceInstanceSchema();
								InputParametersSchema create = new InputParametersSchema();
								Map<String, String> parameters = null;
								
								servicePlan.setId(plan.get("name").asText());
								servicePlan.setName(plan.get("name").asText());
								servicePlan.setDescription(plan.get("description").asText());
								servicePlan.setBindable(plan.get("bindable").asBoolean());
								logger.info("Catalog Debug 4");
								
								try {
									for(JsonNode bullet : plan.get("metadata").get("bullets")) {
										bullets.add(bullet.asText());
									}
									planMeta.setBullets(bullets);
									
									planCost.setAmount(plan.get("metadata").get("costs").get("amount").asText());
									planCost.setUnit(plan.get("metadata").get("costs").get("unit").asText());
									planMeta.setCosts(planCost);
									servicePlan.setMetadata(planMeta);
									
									parameters = mapper.convertValue(plan.get("schemas").get("service_instance").get("create").get("parameters"), new TypeReference<Map<String, String>>(){});
									create.setParameters(parameters);
								} catch ( Exception e ) {
									logger.info("This Plan is Error1");
								}
								
								instanceSchema.setCreate(create);
								planSchema.setService_instance(instanceSchema);
								servicePlan.setSchemas(planSchema);
								planList.add(servicePlan);
							} catch( Exception e) {
								logger.info("This Plan is Error2");
							}
						}
					}
				} catch( Exception e) {
					logger.info("This Plan is Empty");
				}

				logger.info("Catalog Debug 5");
				service.setPlans(planList);
				serviceList.add(service);
			}
			catalog.setServices(serviceList);
		}
		return catalog;
	}
	
	public static Object createTemplateInstance(String instanceId, ProvisionInDO inDO, String instanceName, String instanceUid) throws Exception {
		Object response = null;
		TemplateInstance instance = new TemplateInstance();
		Metadata instanceMeta = new Metadata();
		Metadata templateMeta = new Metadata();
		TemplateInstanceSpec spec = new TemplateInstanceSpec();
		TemplateInstanceSpecTemplate template = new TemplateInstanceSpecTemplate();
		List<TemplateParameter> parameters = new ArrayList<TemplateParameter>();
		
		List<V1OwnerReference> ownerRefs = new ArrayList<>();
		V1OwnerReference ownerRef = new V1OwnerReference();
		
		ownerRef.setApiVersion(Constants.SERVICE_INSTANCE_API_VERSION);
		ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
		ownerRef.setController(Boolean.TRUE);
		ownerRef.setKind(Constants.SERVICE_INSTANCE_KIND);
		ownerRef.setName(instanceName);
		ownerRef.setUid(instanceUid);
		ownerRefs.add(ownerRef);
		instanceMeta.setOwnerReferences(ownerRefs);
		
		logger.info("Service Instance Namespace : " + inDO.getContext().getNamespace());
		
		try {
			instance.setApiVersion(Constants.CUSTOM_OBJECT_GROUP + "/" + Constants.CUSTOM_OBJECT_VERSION);
			instance.setKind(Constants.CUSTOM_OBJECT_KIND_TEMPLATE_INSTANCE);
			instanceMeta.setName(instanceId);
			instanceMeta.setNamespace(inDO.getContext().getNamespace());
			instance.setMetadata(instanceMeta);
			
			templateMeta.setName(inDO.getService_id());
			template.setMetadata(templateMeta);
			
			if(inDO.getParameters() != null) {
				for(String key : inDO.getParameters().keySet()) {
					TemplateParameter parameter = new TemplateParameter();
					parameter.setName(key);
					parameter.setValue(inDO.getParameters().get(key));
					parameters.add(parameter);
				}
				template.setParameters(parameters);
			} else {
				String planName = inDO.getPlan_id();
				Object planResponse = customObjectApi.getNamespacedCustomObject("servicecatalog.k8s.io", "v1beta1", inDO.getContext().getNamespace(), "serviceplans", planName);
				GetPlanDO plan = mapper.readValue(gson.toJson(planResponse), GetPlanDO.class);
				if(plan.getSpec().getInstanceCreateParameterSchema() != null) {
					for(String key : plan.getSpec().getInstanceCreateParameterSchema().keySet()) {
						TemplateParameter parameter = new TemplateParameter();
						parameter.setName(key);
						parameter.setValue(inDO.getParameters().get(key));
						parameters.add(parameter);
					}
				}
				template.setParameters(parameters);
			}
			
			spec.setTemplate(template);
			instance.setSpec(spec);
			
			JSONParser parser = new JSONParser();        	
	    	JSONObject bodyObj = (JSONObject) parser.parse(new Gson().toJson(instance));
	    	
	    	response = customObjectApi.createNamespacedCustomObject(
	    			Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, 
					inDO.getContext().getNamespace(),
					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE,
					bodyObj, null);
		} catch(ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
		} catch(Exception e) {
			logger.info("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
		}
		
		return response;
	}
	
	public static Object deleteTemplateInstance(String instanceId) throws Exception {
		Object response = null;
		
		try {
    		V1DeleteOptions body = new V1DeleteOptions();
    		
        	response = customObjectApi.deleteNamespacedCustomObject(
        			Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION,
					Constants.DEFAULT_NAMESPACE,
					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE,
					instanceId, 
					body, 0, null, null);
        } catch (ApiException e) {
        	logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	logger.info("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
        }
		
		return response;
	}
	
	public static BindingOutDO insertBindingSecret(String instanceId, String bindingId, BindingInDO inDO) throws Exception {
		BindingOutDO outDO = new BindingOutDO();
		Map<String, Object> secretMap = new HashMap<String, Object>();
		logger.info(" Binding Namespace : " + inDO.getContext().getNamespace());
		try {
			Object response = customObjectApi.getNamespacedCustomObject(
					Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION,
					inDO.getContext().getNamespace(), 
					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, 
					instanceId);
			
			TemplateInstance templateInstance = mapper.readValue(gson.toJson(response), TemplateInstance.class);
			List<Object> objects = templateInstance.getSpec().getTemplate().getObjects();
					
			for(Object object : objects) {
				JSONObject objectJson = (JSONObject) JSONValue.parse(gson.toJson(object));
				JSONObject metadataJson = (JSONObject) JSONValue.parse(objectJson.get("metadata").toString());
				
				String name = metadataJson.get("name").toString();
				String namespace = "default";
				if(metadataJson.get("namespace") != null) {
					namespace = metadataJson.get("namespace").toString();
				}
				
				if(objectJson.get("kind").toString().equals("Service")) {
					List<Endpoint> endPointList = new ArrayList<Endpoint>();
					V1Service service = api.readNamespacedService(name, namespace, null, null, null);
					if(service.getSpec().getType().equals("LoadBalancer")) {
						for(V1LoadBalancerIngress ip : service.getStatus().getLoadBalancer().getIngress()) {
							Endpoint endPoint = new Endpoint();
							List<String> ports = new ArrayList<String>();
							endPoint.setHost(ip.getIp());
							secretMap.put("instance-ip", ip.getIp());
							
							for(V1ServicePort port : service.getSpec().getPorts()) {
								ports.add(String.valueOf(port.getPort()));
							}
							endPoint.setPorts(ports);
							secretMap.put("instance-port", ports);
							endPointList.add(endPoint);
						}
						outDO.setEndpoints(endPointList);
					}
				} else if(objectJson.get("kind").toString().equals("Secret")) {
					V1Secret secret = api.readNamespacedSecret(name, namespace, null, null, null);
					Map<String, byte[]> data = secret.getData();
					for(String key : data.keySet()) {
						secretMap.put(key, new String(data.get(key)));
					}
					
					outDO.setCredentials(secretMap);
				}
			}
			return outDO;
		} catch (ApiException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static JsonNode numberTypeConverter(JsonNode jsonNode) {
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

	private static JsonNode objectToJsonNode(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new GsonBuilder().create();
		String objectStr = gson.toJson(object);
		JsonNode resultNode = null;
		try {
			resultNode = mapper.readTree(objectStr);
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		return resultNode;
	}
	
	private static JsonNode objectToJsonNodeForBinding(Object object) throws IOException {
		JsonNode resultNode = mapper.valueToTree(object);
		return resultNode;
	}

	/**
	 * START method for Namespace Claim Controller by seonho_choi
	 * DO-NOT-DELETE
	 */
	private static int getLatestResourceVersion( String customResourceName ) throws Exception {
		int latestResourceVersion = 0;
		try {
			
			Object result = customObjectApi.listClusterCustomObject(
					Constants.CUSTOM_OBJECT_GROUP, 
					Constants.CUSTOM_OBJECT_VERSION, 
					customResourceName, 
					null, null, null, null, null, null, null, Boolean.FALSE);
			
			String JsonInString = gson.toJson(result);
			JsonFactory factory = mapper.getFactory();
			com.fasterxml.jackson.core.JsonParser parser = factory.createParser(JsonInString);
			JsonNode customObjectList = mapper.readTree(parser);
			
			if(customObjectList.get("items").isArray()) {
				for(JsonNode instance : customObjectList.get("items")) {
					int resourceVersion = instance.get("metadata").get("resourceVersion").asInt();
					latestResourceVersion = (latestResourceVersion >= resourceVersion) ? latestResourceVersion : resourceVersion;
				}
			}
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return latestResourceVersion;
	}
	
	public static void createNamespace( NamespaceClaim claim ) throws Throwable {
		logger.info( "[K8S ApiCaller] Create Namespace Start" );
		
		V1Namespace namespace = new V1Namespace();
		V1ObjectMeta namespaceMeta = new V1ObjectMeta();
		Map<String,String> label = new HashMap<>();
		label.put( "fromClaim", claim.getMetadata().getName() );
		namespaceMeta.setLabels( label );
		namespaceMeta.setName( claim.getMetadata().getName() );
		namespace.setMetadata( namespaceMeta );

		V1Namespace namespaceResult;
		try {
			namespaceResult = api.createNamespace( namespace, null, null, null );
		} catch (ApiException e) {
			logger.info( e.getResponseBody() );
			throw e;
		}
		
		V1ResourceQuota quota = new V1ResourceQuota();
		V1ObjectMeta quotaMeta = new V1ObjectMeta();
		quotaMeta.setName( claim.getMetadata().getName()  );
		quotaMeta.setNamespace( claim.getMetadata().getName() );
		Map<String,String> quotaLabel = new HashMap<>();
		quotaLabel.put( "fromClaim", claim.getMetadata().getName() );
		quotaMeta.setLabels( quotaLabel );
		V1ResourceQuotaSpec spec = claim.getSpec();
		quota.setMetadata( quotaMeta );
		quota.setSpec( spec );
		
		V1ResourceQuota quotaResult;
		try {
			quotaResult = api.createNamespacedResourceQuota( claim.getMetadata().getName(), quota, null, null, null );
		} catch (ApiException e) {
			logger.info( e.getResponseBody() );
			throw e;
		}
	}
	
	public static void createResourceQuota( NamespaceClaim claim ) throws Throwable {
		logger.info( "[K8S ApiCaller] Create Resource Quota Start" );
		
		V1ResourceQuota quota = new V1ResourceQuota();
		V1ObjectMeta quotaMeta = new V1ObjectMeta();
		quotaMeta.setName( claim.getMetadata().getNamespace() );
		quotaMeta.setNamespace( claim.getMetadata().getNamespace() );
		Map<String,String> quotaLabel = new HashMap<>();
		quotaLabel.put( "fromClaim", claim.getMetadata().getName() );
		quotaMeta.setLabels( quotaLabel );
		V1ResourceQuotaSpec spec = claim.getSpec();
		quota.setMetadata( quotaMeta );
		quota.setSpec( spec );
		
		V1ResourceQuota quotaResult;
		try {
			quotaResult = api.createNamespacedResourceQuota( claim.getMetadata().getNamespace(), quota, null, null, null );
		} catch (ApiException e) {
			logger.info( e.getResponseBody() );
			throw e;
		}
	}
	
	public static void updateResourceQuota( NamespaceClaim claim ) throws Throwable {
		logger.info( "[K8S ApiCaller] Update Resource Quota Start" );
				
		V1ResourceQuota quota = new V1ResourceQuota();
		V1ObjectMeta quotaMeta = new V1ObjectMeta();
		quotaMeta.setName( claim.getMetadata().getNamespace() );
		quotaMeta.setNamespace( claim.getMetadata().getNamespace() );
		V1ResourceQuotaSpec spec = claim.getSpec();
		quota.setMetadata( quotaMeta );
		quota.setSpec( spec );
		
		try {
			api.replaceNamespacedResourceQuota( claim.getMetadata().getNamespace(), claim.getMetadata().getNamespace(), quota, null, null, null);
		} catch (ApiException e) {
			logger.info( e.getResponseBody() );
			throw e;
		}
	}
	
	public static boolean namespaceAlreadyExist( String name ) throws Throwable {
		logger.info( "[K8S ApiCaller] Get Namespace Start" );

		V1Namespace namespaceResult;
		try {
			namespaceResult = api.readNamespace( name, null, null, null);
		} catch (ApiException e) {
			logger.info( "[K8S ApiCaller][Exception] Namespace-" + name + " is not Exist" );
			return false;
		}
		
		if ( namespaceResult == null ) {
			logger.info( "[K8S ApiCaller] Namespace-" + name + " is not Exist" );
			return false;
		} else {
			logger.info( namespaceResult.toString() );
			return true;
		}
	}
	
	public static boolean resourcequotaAlreadyExist( String namespace ) throws Throwable {
		logger.info( "[K8S ApiCaller] Get Resource Quota Start" );

		V1ResourceQuota resourceQuotaResult;
		try {
			resourceQuotaResult = api.readNamespacedResourceQuota(namespace, namespace, null, null, null);
		} catch (ApiException e) {
			logger.info( "[K8S ApiCaller][Exception] ResourceQuota-" + namespace + " is not Exist" );
			return false;
		}
		
		if ( resourceQuotaResult == null ) {
			logger.info( "[K8S ApiCaller][Exception] ResourceQuota-" + namespace + " is not Exist" );
			return false;
		} else {
			logger.info( resourceQuotaResult.toString() );
			return true;
		}
	}
	
	public static boolean roleBindingAlreadyExist( String name, String namespace ) throws Throwable {
		logger.info( "[K8S ApiCaller] Get RoleBinding Start" );

		V1RoleBinding roleBindingResult;
		try {
			roleBindingResult = rbacApi.readNamespacedRoleBinding(name, namespace, null);
		} catch (ApiException e) {
			logger.info( "[K8S ApiCaller][Exception] RoleBinding-" + name + " is not Exist" );
			return false;
		}
		
		if ( roleBindingResult == null ) {
			logger.info( "[K8S ApiCaller][Exception] RoleBinding-" + name + " is not Exist" );
			return false;
		} else {
			logger.info( roleBindingResult.toString() );
			return true;
		}
	}
	
	public static void createRoleBinding( RoleBindingClaim claim ) throws ApiException {
		logger.info( "[K8S ApiCaller] Create RoleBinding Start" );

		V1RoleBinding roleBinding = new V1RoleBinding();
		V1ObjectMeta roleBindingMeta = new V1ObjectMeta();
		roleBindingMeta.setName( claim.getMetadata().getName() );
		roleBindingMeta.setNamespace( claim.getMetadata().getNamespace() );
		roleBinding.setMetadata( roleBindingMeta );
		roleBinding.setSubjects( claim.getSubjects() );
		roleBinding.setRoleRef( claim.getRoleRef() );
		
		try {
			rbacApi.createNamespacedRoleBinding( claim.getMetadata().getNamespace(), roleBinding, null, null, null);
		} catch (ApiException e) {
			logger.info( e.getResponseBody() );
			throw e;
		}
	}
	/**
	 * END method for Namespace Claim Controller by seonho_choi
	 * DO-NOT-DELETE
	 */
	
	/**
	 * START method for Namespace Claim Controller by seonho_choi
	 * DO-NOT-DELETE
	 */
	public static String getUid( String namespace, String name ) {
		String uid = "";
		try {
			Object result = customObjectApi.getNamespacedCustomObject(
					Constants.SERVICE_INSTANCE_API_GROUP, 
					Constants.SERVICE_INSTANCE_API_VERSION, 
					namespace, 
					Constants.SERVICE_INSTANCE_PLURAL, 
					name );
			
			String jsonString = gson.toJson(result);
			logger.info( jsonString );
			JsonParser parser = new JsonParser();
			uid = parser.parse( jsonString ).getAsJsonObject().get( "metadata" ).getAsJsonObject().get( "uid" ).getAsString();
		} catch (ApiException e) {
			logger.info("Response body: " + e.getResponseBody());
        	e.printStackTrace();
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return uid;
	}

	public static V1NamespaceList getAccessibleNS(String userId) throws ApiException {
		V1NamespaceList nsList = null;
		List <String> nsNameList = null;
		//1. List of ClusterRoleBinding 
		V1ClusterRoleBindingList crbList = null;
		List < String > clusterRoleList = null;
		boolean clusterRoleFlag = false;
		try {
			crbList = rbacApi.listClusterRoleBinding("true", false, null, null, null, 1000 , null, 60, false);
			for (V1ClusterRoleBinding item : crbList.getItems()) {

				List<V1Subject> subjects = item.getSubjects();
				V1RoleRef roleRef = item.getRoleRef();
				if (subjects != null) {
					for( V1Subject subject : subjects ) {

						if ( subject.getKind().equalsIgnoreCase("User")) {
							if( subject.getName().equalsIgnoreCase(userId)) {
								if (clusterRoleList == null ) clusterRoleList = new ArrayList<>();
								clusterRoleList.add(roleRef.getName());   // get ClusterRole name
							}
						}
					}	
				}			
			}
			
			//2. Check if ClusterRole has NameSpace GET rule 
			if (clusterRoleList != null) {
				for ( String clusterRoleName : clusterRoleList ) {
					V1ClusterRole clusterRole = rbacApi.readClusterRole(clusterRoleName, "true");
					List<V1PolicyRule> rules = clusterRole.getRules();
					if ( rules != null) {
						for ( V1PolicyRule rule : rules ) {
							if (rule.getResources().contains("*") || rule.getResources().contains("namespaces")) {
								if (rule.getVerbs().contains("list")){
									clusterRoleFlag = true;
								}
							}
						}
					}	
				}
			}
			
			// Get All NameSpace
			if (clusterRoleFlag) {
				nsList = api.listNamespace("true", false, null, null, null, 100 , null, 60, false);				
			} else {
				V1NamespaceList nsListK8S = api.listNamespace("true", false, null, null, null, 100 , null, 60, false);	
				
				//3. List of RoleBinding
				for ( V1Namespace ns : nsListK8S.getItems()) {
					V1RoleBindingList rbList = rbacApi.listNamespacedRoleBinding(ns.getMetadata().getName(), "true", false, null, null, null, 100, null, 60, false);
					for (V1RoleBinding item : rbList.getItems()) {
						List<V1Subject> subjects = item.getSubjects();
						V1RoleRef roleRef = item.getRoleRef();
						for( V1Subject subject : subjects ) {
							if ( subject.getKind().equalsIgnoreCase("User")) {
								
								//4. Check if Role has NameSpace GET rule 
								if( subject.getName().equalsIgnoreCase(userId)) {  // Found Matching Role
									V1Role role = rbacApi.readNamespacedRole(roleRef.getName(), ns.getMetadata().getName(), "true");
									List<V1PolicyRule> rules = role.getRules();							
									if ( rules != null) {
										for ( V1PolicyRule rule : rules ) {
											if (rule.getResources().contains("*") || rule.getResources().contains("namespaces")) {
												if (rule.getVerbs().contains("list")){
													if(nsNameList == null) nsNameList = new ArrayList<>();
													nsNameList.add(ns.getMetadata().getName());
												}
											}
										}
									}
								}
							}
						}				
					}
				} 					
				if (nsNameList!=null) {
					// Stream distinct (중복제거)
					nsNameList = nsNameList.stream().distinct().collect(Collectors.toList());				
					
					for (String nsName : nsNameList) {
						if(nsList == null) nsList = new V1NamespaceList();
						nsList.addItemsItem( api.readNamespace(nsName, "true", false, false) );
					}
				}
				
			}			
		} catch (ApiException e) {			
			logger.info(e.getResponseBody());
			throw e;
		}		
		for (V1Namespace ns : nsList.getItems()) {
			logger.info(" [ Accessible NameSpace ] : " + ns.getMetadata().getName() );		
		}
		return nsList;
	}

	public static boolean verifyAdmin(String userId) throws ApiException {
		V1ClusterRoleBindingList crbList = null;
		boolean isAdmin = false;
		try {
			crbList = rbacApi.listClusterRoleBinding("true", false, null, null, null, 1000 , null, 60, false);
			for (V1ClusterRoleBinding item : crbList.getItems()) {
				List<V1Subject> subjects = item.getSubjects();
				V1RoleRef roleRef = item.getRoleRef();
				if (subjects != null) {
					for( V1Subject subject : subjects ) {
						if ( subject.getKind().equalsIgnoreCase("User")) {
							if( subject.getName().equalsIgnoreCase(userId)) {
								V1ClusterRole clusterRole = rbacApi.readClusterRole(roleRef.getName(), "true");
								List<V1PolicyRule> rules = clusterRole.getRules();
								if ( rules != null) {
									for ( V1PolicyRule rule : rules ) {									
										if ( rule.getApiGroups().contains("*") && rule.getResources().contains("*") && rule.getVerbs().contains("*") ) {  // check admin rule 
											isAdmin = true;
										}
									}
								}
							}
						}
					}	
				}			
			}
		}catch (ApiException e) {			
			logger.info(e.getResponseBody());
			throw e;
		} catch(Exception e2) {
			logger.info(e2.getStackTrace().toString());
		}
		return isAdmin;		
	}
}
