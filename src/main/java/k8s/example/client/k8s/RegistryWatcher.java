package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import k8s.example.client.models.StateCheckInfo;

public class RegistryWatcher extends Thread {
	private Watch<Object> watchRegistry;
	private static String latestResourceVersion = "0";
	private CustomObjectsApi api = null;
	ApiClient client;
    private static Logger logger = Main.logger;
	StateCheckInfo sci = new StateCheckInfo();

	private static ObjectMapper mapper = new ObjectMapper();
	
	RegistryWatcher(ApiClient client, CustomObjectsApi api, String resourceVersion) throws Exception {
		watchRegistry = Watch.createWatch(client,
				api.listClusterCustomObjectCall("tmax.io", "v1", "registries", null, null, null, null, null, resourceVersion, null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<Object>>() {}.getType());

		this.api = api;
		this.client = client;
		latestResourceVersion = resourceVersion;
		mapper.registerModule(new JodaModule());
	}
	
	
	@Override
	public void run() {
		try {
			while(true) {
				sci.checkThreadState();
				
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
						Registry registry = null;
						try {
							registry = mapper.treeToValue(mapper.valueToTree(response.object), Registry.class);
							logger.info("\t[" + registry.getMetadata().getResourceVersion() + "] " 
									+ registry.getMetadata().getName() + "/" 
									+ registry.getMetadata().getNamespace() 
									+ " Registry Data\n" + response.object.toString() + "\n");
						} catch(Exception e) {
							logger.info("[mapper error]: " + e.getMessage());
						}
						
						if( registry != null
								&& Integer.parseInt(registry.getMetadata().getResourceVersion()) > Integer.parseInt(latestResourceVersion)) {
							latestResourceVersion = registry.getMetadata().getResourceVersion();
							String eventType = response.type.toString();
							logger.info("====================== Registry " + eventType + " ====================== \n");
							
							String serviceType 
							= registry.getSpec().getService().getIngress() != null ? 
									RegistryService.SVC_TYPE_INGRESS : RegistryService.SVC_TYPE_LOAD_BALANCER;
							
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
								
								else if( registry.getStatus().getPhase() != null) {
									String phase = registry.getStatus().getPhase();
									String changePhase = null;
									String changeMessage = null;
									String changeReason = null;
									Map <RegistryCondition.Condition, Boolean> statusMap = getStatusMap(registry);
									
									logger.info("\t[" + registry.getMetadata().getResourceVersion() + "] " 
											+ registry.getMetadata().getName() + "/" 
											+ registry.getMetadata().getNamespace() 
											+ " Registry Status");
									for(RegistryCondition.Condition con : statusMap.keySet()) {
										logger.info("\t\t" + con.getType() + "(" + statusMap.get(con) +",  " + changeReason +",  " + changeMessage + ")");
									}
									
									// Registry Is Creating.
									if(phase.equals(RegistryStatus.StatusPhase.CREATING.getStatus()) ) {
										if ( !statusMap.get(RegistryCondition.Condition.REPLICA_SET)
												&& !statusMap.get(RegistryCondition.Condition.POD)
												&& !statusMap.get(RegistryCondition.Condition.CONTAINER)
												&& !statusMap.get(RegistryCondition.Condition.SERVICE)
												&& !statusMap.get(RegistryCondition.Condition.SECRET_OPAQUE)
												&& !statusMap.get(RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON)
												&& !statusMap.get(RegistryCondition.Condition.SECRET_TLS)
												&& !statusMap.get(RegistryCondition.Condition.INGRESS)
												&& !statusMap.get(RegistryCondition.Condition.PVC)) {

											K8sApiCaller.createRegistry(registry);
										} 
										// Registry Is NotReady
										else if(statusIsNotReady(statusMap, serviceType)) {
											changePhase = RegistryStatus.StatusPhase.NOT_READY.getStatus();
											changeMessage = "Registry is not ready.";
											changeReason = "NotReady";
										}
										else {
											for( RegistryCondition condition : registry.getStatus().getConditions() ) {
												if( condition.getReason() != null ) {
													changePhase = RegistryStatus.StatusPhase.ERROR.getStatus();
													changeMessage = "Registry's condtion is not satisfied.";
													changeReason = "Error";
													break;
												}
											}
										}
									}
									// Registry Is Updating.
									else if(phase.equals(RegistryStatus.StatusPhase.UPDATING.getStatus()) ) {
										// logger.info("afterJson = " + Util.toJson(registry).toString());
										JsonNode diff = Util.jsonDiff(beforeJson, Util.toJson(registry).toString());
										
										if(diff.size() > 0) {
											logger.info("[Updated Registry Spec]\ndiff: " + diff.toString() + "\n");
											K8sApiCaller.updateRegistryAnnotationLastCR(registry);
											K8sApiCaller.updateRegistrySubresources(registry, diff);
											break;
										} 

										Registry realRegistry = K8sApiCaller.getRegistry(registry.getMetadata().getName(), registry.getMetadata().getNamespace());
										Map <RegistryCondition.Condition, Boolean> realSatusMap = getStatusMap(realRegistry);
										
										if(statusIsNotReady(realSatusMap, serviceType)) {
											changePhase = RegistryStatus.StatusPhase.NOT_READY.getStatus();
											changeMessage = "Registry is not ready.";
											changeReason = "NotReady"; 
										}
										else {
											for( RegistryCondition condition : registry.getStatus().getConditions() ) {
												if( condition.getReason() != null ) {
													changePhase = RegistryStatus.StatusPhase.ERROR.getStatus();
													changeMessage = "Registry's condtion is not satisfied.";
													changeReason = "Error";
													break;
												}
											}
										}
									}
									// Registry Is Running.
									else if(statusIsRunning(statusMap, serviceType)) {
											// if last Phase is not Running, synchronize image list from registry.
											if(!phase.equals(RegistryStatus.StatusPhase.RUNNING.getStatus())) {
												boolean isSynced = false;
												int tryCount = 0;
												while(!isSynced) {
													if( ++tryCount > 10) {
														break;
													}
													try {
														K8sApiCaller.syncImageList(registry);
														isSynced = true;
													} catch (ApiException e) {
														logger.info(e.getResponseBody());
													} catch (Exception e) {
														logger.info(e.getMessage());
													}
													Thread.sleep(1000);
												}

												changePhase = RegistryStatus.StatusPhase.RUNNING.getStatus();
												changeMessage = "Registry is running. All registry resources are operating normally.";
												changeReason = "Running";
											}
											// Last Phase: Running / Current Phase: Running
											else { 
												if(K8sApiCaller.updateRegistryAnnotation(registry)) {
													logger.info("Update registry-login-url annotation");
													break;
												}
												// logger.info("afterJson = " + Util.toJson(registry).toString());
												JsonNode diff = Util.jsonDiff(beforeJson, Util.toJson(registry).toString());
												if(diff.size() > 0) {
													logger.info("[Updated Registry Spec]\ndiff: " + diff.toString() + "\n");
													logger.info("Change Status: Running -> Updating");
													changePhase = RegistryStatus.StatusPhase.UPDATING.getStatus();
													changeMessage = "Registry is Updating";
													changeReason = "Updating";
												}
											}
										}
									// Registry Is NotReady or Error.
									else {
										if(statusIsNotReady(statusMap, serviceType)) {
											changePhase = RegistryStatus.StatusPhase.NOT_READY.getStatus();
											changeMessage = "Registry is not ready.";
											changeReason = "NotReady";
										} else if(statusIsError(statusMap, serviceType)) {
											changePhase = RegistryStatus.StatusPhase.ERROR.getStatus();
											changeMessage = "Registry's condtion is not satisfied.";
											changeReason = "Error";
										}
									}
									
									K8sApiCaller.patchRegistryStatus(registry, changePhase, changeMessage, changeReason);
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
						new TypeToken<Watch.Response<Object>>() {}.getType());
			}
		} catch (Exception e) {
			logger.info("Registry Watcher Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
			if( e.getMessage().equals("abnormal") ) {
				logger.info("Catch abnormal conditions!! Exit process");
				System.exit(1);
			}
		}
	}

	public static String getLatestResourceVersion() {
		return latestResourceVersion;
	}
	
	public static Map <RegistryCondition.Condition, Boolean> getStatusMap(Registry registry) {
		Map <RegistryCondition.Condition, Boolean> statusMap = new HashMap<>();
		
		for(RegistryCondition.Condition con : RegistryCondition.Condition.values()) {
			try {
				statusMap.put(con,
						registry.getStatus().getConditions().get(con.ordinal()).getStatus().equals(RegistryStatus.Status.TRUE.getStatus()));
			} catch( IndexOutOfBoundsException e) {
				logger.info(con.getType() + " type condition is empty.");
				statusMap.put(con, Boolean.TRUE);
			}
		}
		return statusMap;
	}
	
	public static boolean statusIsNotReady(Map<RegistryCondition.Condition, Boolean> statusMap, String serviceType) {
		if(statusMap.get(RegistryCondition.Condition.REPLICA_SET)
				&& statusMap.get(RegistryCondition.Condition.POD)
				&& statusMap.get(RegistryCondition.Condition.SERVICE)
				&& statusMap.get(RegistryCondition.Condition.SECRET_OPAQUE)
				&& statusMap.get(RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON)
				&& statusMap.get(RegistryCondition.Condition.PVC)
				&& (
						(
							serviceType.equals(RegistryService.SVC_TYPE_INGRESS) 
							&& statusMap.get(RegistryCondition.Condition.SECRET_TLS)
							&& statusMap.get(RegistryCondition.Condition.INGRESS)
						)
						|| !serviceType.equals(RegistryService.SVC_TYPE_INGRESS)
					)
				) {
			return true;
		}
		
		return false;
	}
	
	public static boolean statusIsError(Map<RegistryCondition.Condition, Boolean> statusMap, String serviceType) {
		if(!statusMap.get(RegistryCondition.Condition.REPLICA_SET)
				|| !statusMap.get(RegistryCondition.Condition.POD)
				|| !statusMap.get(RegistryCondition.Condition.SERVICE)
				|| !statusMap.get(RegistryCondition.Condition.SECRET_OPAQUE)
				|| !statusMap.get(RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON)
				|| !statusMap.get(RegistryCondition.Condition.PVC)
				|| (
						serviceType.equals(RegistryService.SVC_TYPE_INGRESS) 
						&& (!statusMap.get(RegistryCondition.Condition.SECRET_TLS)
						|| !statusMap.get(RegistryCondition.Condition.INGRESS))
					)
				) {
			return true;
		}
		
		return false;
	}
	
	public static boolean statusIsRunning(Map<RegistryCondition.Condition, Boolean> statusMap, String serviceType) {
		if( statusMap.get(RegistryCondition.Condition.REPLICA_SET)
				&& statusMap.get(RegistryCondition.Condition.POD)
				&& statusMap.get(RegistryCondition.Condition.CONTAINER)
				&& statusMap.get(RegistryCondition.Condition.SERVICE)
				&& statusMap.get(RegistryCondition.Condition.SECRET_OPAQUE)
				&& statusMap.get(RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON)
				&& statusMap.get(RegistryCondition.Condition.PVC)
				&& (
						(
							serviceType.equals(RegistryService.SVC_TYPE_INGRESS) 
							&& statusMap.get(RegistryCondition.Condition.SECRET_TLS)
							&& statusMap.get(RegistryCondition.Condition.INGRESS)
						)
						|| !serviceType.equals(RegistryService.SVC_TYPE_INGRESS)
					)
			) {
			return true;
		}
		
		return false;
	}
}
