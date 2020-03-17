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

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarSource;
import io.kubernetes.client.openapi.models.V1ExecAction;
import io.kubernetes.client.openapi.models.V1Handler;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1Lifecycle;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeAddress;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimSpec;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1ReplicaSetBuilder;
import io.kubernetes.client.openapi.models.V1ReplicaSetSpec;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretKeySelector;
import io.kubernetes.client.openapi.models.V1SecretVolumeSource;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.kubernetes.client.util.Config;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.DataObject.User;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.k8s.apis.CustomResourceApi;
import k8s.example.client.StringUtil;
import k8s.example.client.Util;
import k8s.example.client.models.CommandExecOut;
import k8s.example.client.models.Metadata;
import k8s.example.client.models.ProvisionInDO;
import k8s.example.client.models.Registry;
import k8s.example.client.models.RegistryPVC;
import k8s.example.client.models.RegistryService;
import k8s.example.client.models.RegistryStatus;
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
	private static CustomObjectsApi customObjectApi;
	private static CustomResourceApi templateApi;
	private static ObjectMapper mapper = new ObjectMapper();
	private static Gson gson = new GsonBuilder().create();

	public static void initK8SClient() throws Exception {
		k8sClient = Config.fromCluster();
		k8sClient.setConnectTimeout(5000);
		k8sClient.setReadTimeout(0);
		k8sClient.setWriteTimeout(0);		
		Configuration.setDefaultApiClient(k8sClient);

		api = new CoreV1Api();
		appApi = new AppsV1Api();
		customObjectApi = new CustomObjectsApi();
		templateApi = new CustomResourceApi();
	}

	public static void startWatcher() throws Exception {    	
		// Get latest resource version
		System.out.println("Get latest resource version");

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
        	System.out.println("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	System.out.println("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
        }
    	
		System.out.println("User Latest resource version: " + userLatestResourceVersion);

		// registry
		int registryLatestResourceVersion = 0;

		try {
			Object response = customObjectApi.listClusterCustomObject(
					Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, 
					//					"hypercloud-system",
					Constants.CUSTOM_OBJECT_PLURAL_USER,
					null, null, null, "obj=registry", null, null, null, Boolean.FALSE);
			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			// Register Joda deserialization module because of creationTimestamp of k8s object
			mapper.registerModule(new JodaModule());
			ArrayList<Registry> registryList = mapper.readValue((new Gson()).toJson(respJson.get("items")), new TypeReference<ArrayList<Registry>>() {});

			for(Registry user : registryList) {
				int registryResourceVersion = Integer.parseInt(user.getMetadata().getResourceVersion());
				registryLatestResourceVersion = (registryLatestResourceVersion >= registryResourceVersion) ? registryLatestResourceVersion : registryResourceVersion;
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("Registry Latest resource version: " + registryLatestResourceVersion);
		
		// Operator
		int instanceLatestResourceVersion = 0;
		try {
			Object result = templateApi.listNamespacedCustomObject(
					Constants.CUSTOM_OBJECT_GROUP, 
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.TEMPLATE_NAMESPACE, 
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
			System.out.println("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		
		System.out.println("Instance Latest resource version: " + instanceLatestResourceVersion);

		// Start user watch
		System.out.println("Start user watcher");
		UserWatcher userWatcher = new UserWatcher(k8sClient, customObjectApi, String.valueOf(userLatestResourceVersion));
		userWatcher.start();

		// Start registry watch
		System.out.println("Start registry watcher");
		RegistryWatcher registryWatcher = new RegistryWatcher(k8sClient, customObjectApi, String.valueOf(registryLatestResourceVersion));
		registryWatcher.start();
		
		// Start Operator
		System.out.println("Start Template Instance Operator");
		InstanceOperator instanceOperator = new InstanceOperator(k8sClient, templateApi, instanceLatestResourceVersion);
		instanceOperator.start();

		while(true) {
			if(!userWatcher.isAlive()) {
				String userLatestResourceVersionStr = UserWatcher.getLatestResourceVersion();
				System.out.println("User watcher is not alive. Restart user watcher! (Latest resource version: " + userLatestResourceVersionStr + ")");
				userWatcher.interrupt();
				userWatcher = new UserWatcher(k8sClient, customObjectApi, userLatestResourceVersionStr);
				userWatcher.start();
			}

			if(!registryWatcher.isAlive()) {
				String registryLatestResourceVersionStr = RegistryWatcher.getLatestResourceVersion();
				System.out.println("Registry watcher is not alive. Restart registry watcher! (Latest resource version: " + registryLatestResourceVersionStr + ")");
				registryWatcher.interrupt();
				registryWatcher = new RegistryWatcher(k8sClient, customObjectApi, registryLatestResourceVersionStr);
				registryWatcher.start();
			}
			
			if(!instanceOperator.isAlive()) {
				instanceLatestResourceVersion = InstanceOperator.getLatestResourceVersion();
				System.out.println(("Template Instance Operator is not Alive. Restart Operator! (Latest Resource Version: " + instanceLatestResourceVersion + ")"));
				instanceOperator.interrupt();
				instanceOperator = new InstanceOperator(k8sClient, templateApi, instanceLatestResourceVersion);
				instanceOperator.start();
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
        	System.out.println("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	System.out.println("Exception message: " + e.getMessage());
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
        	System.out.println("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	System.out.println("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
        }
    	
    	return token;
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
        	System.out.println("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	System.out.println("Exception message: " + e.getMessage());
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
        	System.out.println("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	System.out.println("Exception message: " + e.getMessage());
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
        	System.out.println("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	System.out.println("Exception message: " + e.getMessage());
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
        	System.out.println("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
        } catch (Exception e) {
        	System.out.println("Exception message: " + e.getMessage());
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
		condition.put("message", "Registry Is Creating");
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
			System.out.println("patchNamespacedCustomObjectStatus result: " + result.toString());
		} catch (ApiException e) {
			throw new Exception(e.getResponseBody());
		}
		
	}

	public static void deleteRegistry(Registry registry) throws Throwable {
		try {
			System.out.println("delete Registry Replica Set");
			appApi.deleteNamespacedReplicaSet(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + registry.getMetadata().getName(), registry.getMetadata().getNamespace(), null, null, null, null, null, null);
		} catch (ApiException e) {
			System.out.println(e.getResponseBody());
		}
		
		try {
			System.out.println("delete Secret");
			deleteSecret(registry.getMetadata().getNamespace(), registry.getMetadata().getName(), null);
			deleteSecret(registry.getMetadata().getNamespace(), registry.getMetadata().getName(), Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON);
		} catch (ApiException e) {
			System.out.println(e.getResponseBody());
		}
		
		try {
			System.out.println("delete Service");
			api.deleteNamespacedService(Constants.K8S_PREFIX + registry.getMetadata().getName(), registry.getMetadata().getNamespace(), null, null, null, null, null, null);
		} catch (ApiException e) {
			System.out.println(e.getResponseBody());
		}
		
		try {
			System.out.println("delete PVC");
			api.deleteNamespacedPersistentVolumeClaim(Constants.K8S_PREFIX + registry.getMetadata().getName(), registry.getMetadata().getNamespace(), null, null, 0, null, null, new  V1DeleteOptions());
		} catch (ApiException e) {
			System.out.println(e.getResponseBody());
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
								System.out.println("[registryIP]:" + registryIP);
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
			lb.setMetadata(lbMeta);

			V1ServicePort v1port = new V1ServicePort();
			v1port.setProtocol(RegistryService.REGISTRY_PORT_PROTOCOL);
			v1port.setPort(registrySVCPort);
			v1port.setName(RegistryService.REGISTRY_PORT_NAME);
			v1port.setTargetPort(new IntOrString(registrySVCTargetPort));
			if( regService.getType().equals(RegistryService.SVC_TYPE_NODE_PORT) ) {
				if( registrySVCNodePort != 0 )
					v1port.setNodePort(registrySVCNodePort);
			}
			
			ports.add(v1port);
			lbSpec.setPorts(ports);

			System.out.println("Selector: " + Constants.K8S_PREFIX + registryId + "=lb");
			selector.put(Constants.K8S_PREFIX + registryId, "lb");
			lbSpec.setSelector(selector);

			lbSpec.setType(registry.getSpec().getService().getType());

			lb.setSpec(lbSpec);

			try {
				api.createNamespacedService(namespace, lb, null, null, null);
			} catch (ApiException e) {
				System.out.println(e.getResponseBody());

				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_FAILED);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getResponseBody());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_FAILED);

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
					System.out.println("patchNamespacedCustomObjectStatus result: " + result.toString());
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
						System.out.println("[registryIP]:" + registryIP);
						break;
					}
					
				// GET PORT
				} else if (service.getSpec().getType().equals("NodePort")) {
					
					for( V1ServicePort port : service.getSpec().getPorts() ) {
						if(port.getName().equals(RegistryService.REGISTRY_PORT_NAME)) {
							registrySVCNodePort = port.getNodePort();
							registryPort = registrySVCNodePort;
							System.out.println("[registryNodePort]:" + registrySVCNodePort);
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
					condition.put("status", RegistryStatus.REGISTRY_PHASE_FAILED);
					condition.put("message", "Creating a registry is failed");
					condition.put("reason", "Service(LB) is not found");
					conditions.add(condition);
					status.put("conditions", conditions);
					status.put("phase", RegistryStatus.REGISTRY_PHASE_FAILED);

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
						System.out.println("patchNamespacedCustomObjectStatus result: " + result.toString());
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
			String storageClassName = StringUtil.isEmpty(registryPVC.getStorageClassName()) ? RegistryPVC.STORAGE_CLASS_DEFAULT : registryPVC.getStorageClassName();
			
			
			pvcMeta.setName(Constants.K8S_PREFIX + registryId);
			
			Map<String, String> pvcLabels = new HashMap<String, String>();
			pvcLabels.put("app", Constants.K8S_PREFIX.substring(0, Constants.K8S_PREFIX.length() - 1));
			pvcMeta.setLabels(pvcLabels);

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
				System.out.println(e.getResponseBody());

				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_FAILED);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getResponseBody());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_FAILED);

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
					System.out.println("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e2) {
					throw new Exception(e2.getResponseBody());
				}
				
				throw e;
			}
			
			
			// ----- Create Secret
			// Create Cert Directory
			System.out.println("Create Cert Directory");
			String registryDir = createDirectory(namespace, registryId);
			
			// Create Certificates
			System.out.println("Create Certificates");
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
				System.out.println(e.getMessage());
				
				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_FAILED);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getMessage());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_FAILED);

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
					System.out.println("patchNamespacedCustomObjectStatus result: " + result.toString());
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
				condition.put("status", RegistryStatus.REGISTRY_PHASE_FAILED);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getResponseBody());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_FAILED);

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
					System.out.println("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e2) {
					throw new Exception(e2.getResponseBody());
				}
			
				throw e;
			}

			// Read cert files & Create Secret Object
			System.out.println("Read cert files & Create Secret Object");
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
				System.out.println("K8SApiCall createSecret");
				secretName = K8sApiCaller.createSecret(namespace, secrets, registryId, labels, null);
			}catch (ApiException e) {
				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_FAILED);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getResponseBody());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_FAILED);

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
					System.out.println("patchNamespacedCustomObjectStatus result: " + result.toString());
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
				K8sApiCaller.createSecret(namespace, secrets2, registryId, labels2, Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON);
			}catch (ApiException e) {
				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_FAILED);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getResponseBody());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_FAILED);

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
					System.out.println("patchNamespacedCustomObjectStatus result: " + result.toString());
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
			System.out.println("RS Name: " + rsMeta.getName());

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
			System.out.println("<Pod Label List>");
			Map<String, String> podLabels = new HashMap<String, String>();
			podLabels.put("app", "registry");
			podLabels.put("apps", rsMeta.getName());
			System.out.println("app: registry" );
			System.out.println("apps: " + rsMeta.getName());

			podLabels.put(Constants.K8S_PREFIX + registryId, "lb");
			System.out.println(Constants.K8S_PREFIX + registryId + ": lb");

			podMeta.setLabels(podLabels);
			podTemplateSpec.setMetadata(podMeta);

			// 2-2-2. pod spec
			V1PodSpec podSpec = new V1PodSpec();

			V1Container container = new V1Container();

			// 2-2-2-2-1. container name
			container.setName(Constants.K8S_PREFIX + registryId.toLowerCase());
			System.out.println("<Container Name: " + container.getName() + ">");

			// 2-2-2-2-2. image
			container.setImage(registry.getSpec().getImage());
			System.out.println("- Image: " + container.getImage());
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
			env4.setValue("0.0.0.0:443");
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
			System.out.println("Container Port: " + portsItem.getContainerPort());

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

			podSpec.addContainersItem(container);

			// Secret Volume
			List<V1Volume> volumes = new ArrayList<>();
			V1Volume certVolume = new V1Volume();
			certVolume.setName("certs");
			V1SecretVolumeSource volSecret = new V1SecretVolumeSource();
			System.out.println("secret name: " + secretName);
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
			System.out.println("Restart Policy: " + podSpec.getRestartPolicy());

			podTemplateSpec.setSpec(podSpec);
			rsSpec.setTemplate(podTemplateSpec);

			// 2-3. label selector
			V1LabelSelector labelSelector = new V1LabelSelector();
			System.out.println("<RS Label List>");
			Map<String, String> rsLabels = new HashMap<String, String>();
			rsLabels.put("app", "registry");
			rsLabels.put("apps", rsMeta.getName());
			System.out.println("app: registry");
			System.out.println("apps: " + rsMeta.getName());
			labelSelector.setMatchLabels(rsLabels);

			rsSpec.setSelector(labelSelector);

			rsBuilder.withSpec(rsSpec);

			try {
				System.out.println("Create ReplicaSet");
				appApi.createNamespacedReplicaSet(namespace, rsBuilder.build(),
						null, null, null);
			} catch (ApiException e) {
				System.out.println("Create Replicaset Failed");
				System.out.println(e.getResponseBody());
				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_FAILED);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", e.getResponseBody());
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_FAILED);

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
					System.out.println("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e2) {
					throw new Exception(e2.getResponseBody());
				}
				
				throw e;
			}
			
			// Check if Registry Pod is Running
			int retryCount = 0;
			RETRY_CNT = 1000;
			V1Pod pod = null;
			V1PodList pods = null;
			while (++retryCount <= RETRY_CNT) {
				System.out.println("Pod is not Running... Retry Count [" + retryCount + "/" + RETRY_CNT + "]");
				try {
					pods = api.listNamespacedPod(namespace, null, null, null, null,
							"apps=" + rsMeta.getName(), null, null, null, false);
				} catch (ApiException e) {
					System.out.println("Create Replicaset Failed");
					System.out.println(e.getResponseBody());
					
					JSONObject patchStatus = new JSONObject();
					JSONObject status = new JSONObject();
					JSONArray conditions = new JSONArray();
					JSONObject condition = new JSONObject();
					JSONArray patchStatusArray = new JSONArray();
					
					condition.put("type", "Phase");
					condition.put("status", RegistryStatus.REGISTRY_PHASE_FAILED);
					condition.put("message", "Creating a registry is failed");
					condition.put("reason", e.getResponseBody());
					conditions.add(condition);
					status.put("conditions", conditions);
					status.put("phase", RegistryStatus.REGISTRY_PHASE_FAILED);

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
						System.out.println("patchNamespacedCustomObjectStatus result: " + result.toString());
					} catch (ApiException e2) {
						throw new Exception(e2.getResponseBody());
					}
					
					throw e;
				}

				if (pods.getItems() != null && !pods.getItems().isEmpty()) {
					pod = pods.getItems().get(0);
					if (pod.getStatus().getPhase().equals("Running")) {
						System.out.println("Pod is Running !!!!!!");
						System.out.println("Pod Name: " + pod.getMetadata().getName());
						break;
					}
				}

				Thread.sleep(500);
			}

			if (retryCount > RETRY_CNT) {
				System.out.println("Pod Running is Fail");
				System.out.println("Create Replicaset Failed");
				JSONObject patchStatus = new JSONObject();
				JSONObject status = new JSONObject();
				JSONArray conditions = new JSONArray();
				JSONObject condition = new JSONObject();
				JSONArray patchStatusArray = new JSONArray();
				
				condition.put("type", "Phase");
				condition.put("status", RegistryStatus.REGISTRY_PHASE_FAILED);
				condition.put("message", "Creating a registry is failed");
				condition.put("reason", "Pod is not running");
				conditions.add(condition);
				status.put("conditions", conditions);
				status.put("phase", RegistryStatus.REGISTRY_PHASE_FAILED);

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
					System.out.println("patchNamespacedCustomObjectStatus result: " + result.toString());
				} catch (ApiException e2) {
					throw new Exception(e2.getResponseBody());
				}
			}
			
			JSONObject patchStatus = new JSONObject();
			JSONObject status = new JSONObject();
			JSONArray conditions = new JSONArray();
			JSONObject condition = new JSONObject();
			JSONArray patchStatusArray = new JSONArray();
			
			condition.put("type", "Phase");
			condition.put("status", RegistryStatus.REGISTRY_PHASE_RUNNING);
			condition.put("message", "Registry Is Running");
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
				System.out.println("patchNamespacedCustomObjectStatus result: " + result.toString());
			} catch (ApiException e2) {
				throw new Exception(e2.getResponseBody());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}
	
	public static CommandExecOut commandExecute(String[] command) throws Throwable {
		ProcessBuilder processBuilder = new ProcessBuilder();
		CommandExecOut cmdOutDo = new CommandExecOut();
		
		// command exec.
		System.out.println("command: " + Arrays.asList(command));
		
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
						System.out.println(ex.getMessage());
						
						StringWriter sw = new StringWriter();
						ex.printStackTrace(new PrintWriter(sw));
						System.out.println(sw.toString());
					} finally {
						System.out.println("out end");
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
						System.out.println(ex.getMessage());
						
						StringWriter sw = new StringWriter();
						ex.printStackTrace(new PrintWriter(sw));
						System.out.println(sw.toString());
					} finally {
						System.out.println("err end");
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

			System.out.println("out: " + outString);
			System.out.println("err: " + errString);

			cmdOutDo.setCmdStdOut(outString);
			cmdOutDo.setCmdStdErr(errString);

			System.out.println("exit code: " + process.exitValue());
			cmdOutDo.setCmdExitCode(process.exitValue());
            
        } catch (IOException e) {
			System.out.println(e.getMessage());
			
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			System.out.println(sw.toString());
			throw e;
        } catch (InterruptedException e) {
			System.out.println(e.getMessage());
			
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			System.out.println(sw.toString());
			throw e;
        }
		return cmdOutDo;
	}

	// type1: null => Opaque
	// type2: kubernetes.io/dockerconfigjson 
	public static String createSecret(String namespace, Map<String, String> secrets, String secretName, Map<String, String> labels, String type) throws Throwable {
		V1Secret secret = new V1Secret();
		secret.setApiVersion("v1");
		secret.setKind("Secret");
		V1ObjectMeta metadata = new V1ObjectMeta();
		
		if( type != null && type.equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
			metadata.setName(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + secretName.toLowerCase());
		}else {
			metadata.setName(Constants.K8S_PREFIX + secretName.toLowerCase());
		}
		

		//			System.out.println("== secret map == ");
		for( String key : secrets.keySet()) {
			//				System.out.println("[secretMap]" + key + "=" + secrets.get(key));
			secret.putStringDataItem(key, secrets.get(key));
			//				secret.putDataItem(key, secrets.get(key).getBytes(StandardCharsets.UTF_8));
		}

		// 2-2-1-1. pod label
		System.out.println("<Pod Label List>");
		Map<String, String> podLabels = new HashMap<String, String>();

		if(labels == null) {
			podLabels.put("secret", "obj");
			podLabels.put("apps", Constants.K8S_PREFIX + secretName);
			System.out.println("secret: obj");
			System.out.println("apps: " + Constants.K8S_PREFIX + secretName);
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
			
			System.out.println("[result]" + result);

			//				V1Secret secretRet = api.readNamespacedSecret(Constants.K8S_PREFIX + secretName.toLowerCase(), Constants.K8S_PREFIX + domainId.toLowerCase(), null, null, null);

			secretMap = result.getData();
			//				System.out.println("== real secret data ==");
			for( String key : secretMap.keySet()) {
				//					System.out.println("[secret]" + key + "=" + new String(secretMap.get(key)));
			}

		} catch (ApiException e) {
			System.out.println(e.getResponseBody());
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
			System.out.println(e.getResponseBody());
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
		System.out.println("[" + filePath + "]:" + content);

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
		System.out.println("configStr: " + configSb.toString());
		
		return configSb.toString();
//		return new String( Base64.encodeBase64( configSb.toString().getBytes() ) );
	}
	
	private static String createDirectory(String domainId, String registryId) throws IOException {
		Path opensslHome = Paths.get(Constants.OPENSSL_HOME_DIR);
		if (!Files.exists(opensslHome)) {
			Files.createDirectory(opensslHome);
			System.out.println("Directory created: " + Constants.OPENSSL_HOME_DIR);
		}

		String domainDir = Constants.OPENSSL_HOME_DIR + "/" + domainId;
		if (!Files.exists(Paths.get(domainDir))) {
			Files.createDirectory(Paths.get(domainDir));
			System.out.println("Directory created: " + domainDir);
		}

		String registryDir = Constants.OPENSSL_HOME_DIR + "/" + domainId + "/" + registryId;
		if (!Files.exists(Paths.get(registryDir))) {
			Files.createDirectory(Paths.get(registryDir));
			System.out.println("Directory created: " + registryDir);
		}

		Path dockerLoginHome = Paths.get(Constants.DOCKER_LOGIN_HOME_DIR);
		if (!Files.exists(dockerLoginHome)) {
			Files.createDirectory(dockerLoginHome);
			System.out.println("Directory created: " + Constants.DOCKER_LOGIN_HOME_DIR);
		}

		return registryDir;
	}
	
	public static Services getCatalog() throws ApiException {
		Services catalog = new Services();
		List<ServiceOffering> serviceList = new ArrayList<ServiceOffering>();
		Object templates = customObjectApi.listNamespacedCustomObject(
				Constants.CUSTOM_OBJECT_GROUP, 
				Constants.CUSTOM_OBJECT_VERSION, 
				Constants.TEMPLATE_NAMESPACE, 
				Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, 
				null, null, null, null, null, null, null, false);
		
		JsonNode templateList = numberTypeConverter(objectToJsonNode(templates).get("items"));
		if(templateList.isArray()) {
			for(JsonNode template : templateList) {
				ServiceOffering service = new ServiceOffering();
				List<ServicePlan> planList = new ArrayList<ServicePlan>();
				ServicePlan servicePlan = new ServicePlan();
				
				service.setName(template.get("metadata").get("name").asText());
				service.setId(template.get("metadata").get("name").asText());
				service.setDescription(template.get("metadata").get("name").asText());
				service.setBindable(false);
				
				servicePlan.setId(service.getId() + "-" + "plan1");
				servicePlan.setName("example-plan");
				servicePlan.setDescription("Example Plan");
				planList.add(servicePlan);
				service.setPlans(planList);
				serviceList.add(service);
			}
			
			catalog.setServices(serviceList);
		}
		return catalog;
	}
	
	public static Object createServiceInstance(String instanceId, ProvisionInDO inDO) throws Exception {
		Object response = null;
		TemplateInstance instance = new TemplateInstance();
		Metadata instanceMeta = new Metadata();
		Metadata templateMeta = new Metadata();
		TemplateInstanceSpec spec = new TemplateInstanceSpec();
		TemplateInstanceSpecTemplate template = new TemplateInstanceSpecTemplate();
		List<TemplateParameter> parameters = new ArrayList<TemplateParameter>();
		
		try {
			instance.setApiVersion(Constants.CUSTOM_OBJECT_GROUP + "/" + Constants.CUSTOM_OBJECT_VERSION);
			instance.setKind(Constants.CUSTOM_OBJECT_KIND_TEMPLATE_INSTANCE);
			instanceMeta.setName(instanceId);
			instanceMeta.setNamespace(Constants.TEMPLATE_NAMESPACE);
			instance.setMeatdata(instanceMeta);
			
			templateMeta.setName(inDO.getService_id());
			template.setMetadata(templateMeta);
			
			for(String key : inDO.getParameters().keySet()) {
				TemplateParameter parameter = new TemplateParameter();
				parameter.setName(key);
				parameter.setValue(inDO.getParameters().get(key));
				parameters.add(parameter);
			}
			template.setParameters(parameters);
			spec.setTemplate(template);
			instance.setSpec(spec);
			
			JSONParser parser = new JSONParser();        	
	    	JSONObject bodyObj = (JSONObject) parser.parse(new Gson().toJson(instance));
	    	
	    	response = customObjectApi.createNamespacedCustomObject(
	    			Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.TEMPLATE_NAMESPACE,
					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE,
					bodyObj, null);
		} catch(ApiException e) {
			System.out.println("Response body: " + e.getResponseBody());
        	e.printStackTrace();
        	throw e;
		} catch(Exception e) {
			System.out.println("Exception message: " + e.getMessage());
        	e.printStackTrace();
        	throw e;
		}
		
		return response;
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
			System.out.println(e.getMessage());
		}
		return resultNode;
	}
}
