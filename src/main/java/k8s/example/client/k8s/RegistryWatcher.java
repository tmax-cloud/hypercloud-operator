package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.models.Registry;
import k8s.example.client.models.RegistryCondition;
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
						
						if( registry != null) {
							latestResourceVersion = response.object.getMetadata().getResourceVersion();
							String eventType = response.type.toString();
							logger.info("====================== Registry " + eventType + " ====================== \n" + registry.toString());
							
							switch(eventType) {
							case Constants.EVENT_TYPE_ADDED : 
								if(registry.getStatus() == null ) {
									K8sApiCaller.initRegistry(registry.getMetadata().getName(), registry);
									logger.info("Creating registry");
								}
								
								break;
							case Constants.EVENT_TYPE_MODIFIED : 
								if( registry.getStatus().getConditions() != null) {
									for( RegistryCondition registryCondition : registry.getStatus().getConditions()) {
										if( registryCondition.getType().equals("Phase")) {
											if (registryCondition.getStatus().equals(RegistryStatus.REGISTRY_PHASE_CREATING)) {
												K8sApiCaller.createRegistry(registry);
												logger.info("Registry is running");
											}
											else if (registryCondition.getStatus().equals(RegistryStatus.REGISTRY_PHASE_RUNNING)) {
												if( registry.getMetadata().getAnnotations().get(Registry.REGISTRY_LOGIN_URL) == null) {
													K8sApiCaller.addRegistryAnnotation(registry);
													logger.info("Update registry-login-url annotation");
												}
											}
										}
									}
								}
								
								break;
							case Constants.EVENT_TYPE_DELETED : 
//								K8sApiCaller.deleteRegistry(registry);
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
}
