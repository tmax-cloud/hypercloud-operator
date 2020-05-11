package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.models.Registry;
import k8s.example.client.models.RegistryCondition;
import k8s.example.client.models.RegistryService;
import k8s.example.client.models.RegistryStatus;

public class RegistryWatcher extends Thread {
	private Watch<Registry> watchRegistry;
	private static String latestResourceVersion = "0";
	private CustomObjectsApi api = null;
	ApiClient client;
    private Logger logger = Main.logger;
	
	RegistryWatcher(ApiClient client, CustomObjectsApi api, String resourceVersion) throws Exception {
		watchRegistry = Watch.createWatch(client,
				api.listClusterCustomObjectCall("tmax.io", "v1", "registries", null, null, null, null, null, resourceVersion, null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<Registry>>() {}.getType());

		this.api = api;
		this.client = client;
		latestResourceVersion = resourceVersion;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				watchRegistry.forEach(response -> {
					try {
						if (Thread.interrupted()) {
							logger.info("Interrupted!");
							watchRegistry.close();
						}
					} catch (Exception e) {
						logger.info(e.getMessage());
					}
					
					
					// Logic here
					try {
						Registry registry = response.object;
						
						if( registry != null
								&& Integer.parseInt(registry.getMetadata().getResourceVersion()) > Integer.parseInt(latestResourceVersion)) {
							latestResourceVersion = response.object.getMetadata().getResourceVersion();
							String eventType = response.type.toString();
							logger.info("====================== Registry " + eventType + " ====================== \n");
							
							String serviceType = registry.getSpec().getService().getType();
							
							switch(eventType) {
							case Constants.EVENT_TYPE_ADDED: 
								if(registry.getStatus() == null ) {
									K8sApiCaller.initRegistry(registry.getMetadata().getName(), registry);
									logger.info("Creating registry");
								}
								
								break;
							case Constants.EVENT_TYPE_MODIFIED:
								if (registry.getMetadata().getAnnotations() == null) {
									K8sApiCaller.updateRegistryAnnotationLastCR(registry);
									break;
								}
								
								String beforeJson = registry.getMetadata().getAnnotations().get(Constants.LAST_CUSTOM_RESOURCE);
//								logger.info("beforeJson = " + beforeJson);
								if( beforeJson == null) {
									K8sApiCaller.updateRegistryAnnotationLastCR(registry);
									break;
								}
								
								if( registry.getStatus().getPhase() != null) {
									String phase = registry.getStatus().getPhase();
									String changePhase = null;
									String changeMessage = null;
									String changeReason = null;
									Map <RegistryCondition.Condition, Boolean> statusMap = getStatusMap(registry);
									
									// Registry Is Running.
									if( statusMap.get(RegistryCondition.Condition.REPLICA_SET)
											&& statusMap.get(RegistryCondition.Condition.POD)
											&& statusMap.get(RegistryCondition.Condition.CONTAINER)
											&& statusMap.get(RegistryCondition.Condition.SERVICE)
											&& statusMap.get(RegistryCondition.Condition.SECRET_OPAQUE)
											&& statusMap.get(RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON) ) {
										
										if(serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
											if( !statusMap.get(RegistryCondition.Condition.SECRET_TLS)
													|| !statusMap.get(RegistryCondition.Condition.INGRESS) ) {
												break;
											}
										}
										
										changePhase = RegistryStatus.StatusPhase.RUNNING.getStatus();
										changeMessage = "Registry is running. All registry resources are operating normally.";
										changeReason = "Running";
									}
									// Registry Is Creating.
									else if(phase.equals(RegistryStatus.StatusPhase.CREATING.getStatus()) ) {
										if ( !statusMap.get(RegistryCondition.Condition.REPLICA_SET)
												&& !statusMap.get(RegistryCondition.Condition.POD)
												&& !statusMap.get(RegistryCondition.Condition.CONTAINER)
												&& !statusMap.get(RegistryCondition.Condition.SERVICE)
												&& !statusMap.get(RegistryCondition.Condition.SECRET_OPAQUE)
												&& !statusMap.get(RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON)
												&& !statusMap.get(RegistryCondition.Condition.SECRET_TLS)
												&& !statusMap.get(RegistryCondition.Condition.INGRESS) ) {

											K8sApiCaller.createRegistry(registry);
											break;
										} 
										else if(statusMap.get(RegistryCondition.Condition.REPLICA_SET)
												&& statusMap.get(RegistryCondition.Condition.POD)
												&& statusMap.get(RegistryCondition.Condition.SERVICE)
												&& statusMap.get(RegistryCondition.Condition.SECRET_OPAQUE)
												&& statusMap.get(RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON)) {

											if(serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
												if( !statusMap.get(RegistryCondition.Condition.SECRET_TLS)
														|| !statusMap.get(RegistryCondition.Condition.INGRESS) ) {
													break;
												}
											}
											
											changePhase = RegistryStatus.StatusPhase.NOT_READY.getStatus();
											changeMessage = "Registry is not ready.";
											changeReason = "NotReady";
										}

									}
									// Registry Is NotReady or Error.
									else {
										if(!statusMap.get(RegistryCondition.Condition.SERVICE)) {
											changePhase = RegistryStatus.StatusPhase.ERROR.getStatus();
											changeMessage = "Registry service is not exist.";
											changeReason = "ServiceNotFound";
										}
										else if(!statusMap.get(RegistryCondition.Condition.SECRET_OPAQUE)) {
											changePhase = RegistryStatus.StatusPhase.ERROR.getStatus();
											changeMessage = "Registry opaque type secret is not exist.";
											changeReason = "SecretNotFound";
										}
										else if(!statusMap.get(RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON)) {
											changePhase = RegistryStatus.StatusPhase.ERROR.getStatus();
											changeMessage = "Registry docker config json type secret is not exist.";
											changeReason = "SecretNotFound";
										}
										else if(!statusMap.get(RegistryCondition.Condition.CONTAINER)) {
											changePhase = RegistryStatus.StatusPhase.NOT_READY.getStatus();
											changeMessage = "Registry is not ready.";
											changeReason = "NotReady";
										}
										else if(serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
											if(!statusMap.get(RegistryCondition.Condition.SECRET_TLS)) {
												changePhase = RegistryStatus.StatusPhase.ERROR.getStatus();
												changeMessage = "Registry tls type secret is not exist.";
												changeReason = "SecretNotFound";
											} else if(!statusMap.get(RegistryCondition.Condition.INGRESS)) {
												changePhase = RegistryStatus.StatusPhase.ERROR.getStatus();
												changeMessage = "Registry ingress is not exist.";
												changeReason = "IngressNotFound";
											}
										}
									}
									
									K8sApiCaller.patchRegistryStatus(registry, changePhase, changeMessage, changeReason);
									
									if(phase.equals(RegistryStatus.StatusPhase.RUNNING.getStatus())) {
										if( registry.getMetadata().getAnnotations().get(Constants.CUSTOM_OBJECT_GROUP + "/" + Registry.REGISTRY_LOGIN_URL) == null) {
											K8sApiCaller.addRegistryAnnotation(registry);
											logger.info("Update registry-login-url annotation");
											break;
										}
									}

								}

//								logger.info("afterJson = " + Util.toJson(registry).toString());
								JsonNode diff = Util.jsonDiff(beforeJson, Util.toJson(registry).toString());
								logger.info("diff: " + diff.toString());

								if(diff.size() > 0 ) {
									K8sApiCaller.updateRegistryAnnotationLastCR(registry);
									K8sApiCaller.updateRegistrySubresources(registry, diff);
								}

								break;
							case Constants.EVENT_TYPE_DELETED : 
								logger.info("Registry is deleted");
								
								break;
							}						
						}
					} catch (ApiException e) {
						logger.info("ApiException: " + e.getMessage());
						logger.info(e.getResponseBody());
					} catch (Exception e) {
						logger.info("Exception: " + e.getMessage());
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						logger.info(sw.toString());
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				});
				
				logger.info("=============== Registry 'For Each' END ===============");
				watchRegistry = Watch.createWatch(client,
						api.listClusterCustomObjectCall("tmax.io", "v1", "registries", null, null, null, null, null, latestResourceVersion, null, Boolean.TRUE, null),
						new TypeToken<Watch.Response<Registry>>() {}.getType());
			}
		} catch (Exception e) {
			logger.info("Registry Watcher Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
		}
	}

	public static String getLatestResourceVersion() {
		return latestResourceVersion;
	}
	
	public static Map <RegistryCondition.Condition, Boolean> getStatusMap(Registry registry) {
		Map <RegistryCondition.Condition, Boolean> statusMap = new HashMap<>();
		
		RegistryCondition replicasetCondtion
		= registry.getStatus().getConditions().get(RegistryCondition.Condition.INDEX_REPLICA_SET); 
		statusMap.put(RegistryCondition.Condition.REPLICA_SET, 
				replicasetCondtion.getStatus().equals(RegistryStatus.Status.TRUE.getStatus()));
		
		RegistryCondition podCondtion
		= registry.getStatus().getConditions().get(RegistryCondition.Condition.INDEX_POD); 
		statusMap.put(RegistryCondition.Condition.POD, 
				podCondtion.getStatus().equals(RegistryStatus.Status.TRUE.getStatus()));
		
		RegistryCondition containerCondtion
		= registry.getStatus().getConditions().get(RegistryCondition.Condition.INDEX_CONTAINER); 
		statusMap.put(RegistryCondition.Condition.CONTAINER, 
				containerCondtion.getStatus().equals(RegistryStatus.Status.TRUE.getStatus()));
		
		RegistryCondition serviceCondtion
		= registry.getStatus().getConditions().get(RegistryCondition.Condition.INDEX_SERVICE);  
		statusMap.put(RegistryCondition.Condition.SERVICE, 
				serviceCondtion.getStatus().equals(RegistryStatus.Status.TRUE.getStatus()));
		
		RegistryCondition secretOpqueCondtion
		= registry.getStatus().getConditions().get(RegistryCondition.Condition.INDEX_SECRET_OPAQUE); 
		statusMap.put(RegistryCondition.Condition.SECRET_OPAQUE, 
				secretOpqueCondtion.getStatus().equals(RegistryStatus.Status.TRUE.getStatus()));
		
		RegistryCondition secretDockerCondtion
		= registry.getStatus().getConditions().get(RegistryCondition.Condition.INDEX_SECRET_DOCKER_CONFIG_JSON); 
		statusMap.put(RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON, 
				secretDockerCondtion.getStatus().equals(RegistryStatus.Status.TRUE.getStatus()));
		
		RegistryCondition secretTlsCondtion
		= registry.getStatus().getConditions().get(RegistryCondition.Condition.INDEX_SECRET_TLS); 
		statusMap.put(RegistryCondition.Condition.SECRET_TLS, 
				secretTlsCondtion.getStatus().equals(RegistryStatus.Status.TRUE.getStatus()));
		
		RegistryCondition ingressCondtion
		= registry.getStatus().getConditions().get(RegistryCondition.Condition.INDEX_INGRESS); 
		statusMap.put(RegistryCondition.Condition.INGRESS, 
				ingressCondtion.getStatus().equals(RegistryStatus.Status.TRUE.getStatus()));
		
		return statusMap;
	}
	
	
}
