package k8s.example.client.k8s;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.models.Registry;

public class RegistryWatcher extends Thread {
	private final Watch<Registry> watchRegistry;
	private static String latestResourceVersion = "0";
	private CustomObjectsApi api = null;

	RegistryWatcher(ApiClient client, CustomObjectsApi api, String resourceVersion) throws Exception {
		watchRegistry = Watch.createWatch(client,
				api.listClusterCustomObjectCall("tmax.co.kr", "v1", "registries", null, null, null, "obj=registry", null, resourceVersion, null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<Registry>>() {}.getType());

		this.api = api;
		latestResourceVersion = resourceVersion;
	}
	
	@Override
	public void run() {
		try {
			watchRegistry.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						System.out.println("Interrupted!");
						watchRegistry.close();
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				
				
				// Logic here
				try {
					Registry registry = response.object;

					if( registry != null) {
						latestResourceVersion = response.object.getMetadata().getResourceVersion();

						System.out.println("[RegistryWatcher] == Registry == \n" + registry.toString());
						try {
							api.getNamespacedCustomObject(
									Constants.CUSTOM_OBJECT_GROUP, 
									Constants.CUSTOM_OBJECT_VERSION, 
									registry.getMetadata().getNamespace(), 
									Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, 
									registry.getMetadata().getName());
						} catch (ApiException e) {
							System.out.println(e.getResponseBody());
							System.out.println("ApiException Code: " + e.getCode());
							if( e.getCode() == 404 ) {
								try {
									K8sApiCaller.deleteRegistry(registry);
									System.out.println("Registry is deleted");
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}

						if(registry.getStatus() == null ) {
							K8sApiCaller.initRegistry(registry.getMetadata().getName(), registry);
							System.out.println("Creating registry");
						}
						else if(registry.getStatus().getPhase().equals("Creating")) {
							K8sApiCaller.createRegistry(registry.getMetadata().getName(), registry);
							System.out.println("Registry is running");
						}
						//					else if(registry.getStatus().getPhase().equals("Running")) {
						////						K8sApiCaller.createRegistry(registry.getMetadata().getName(), registry);
						//					}
					}
				} catch (ApiException e) {
					System.out.println("ApiException: " + e.getMessage());
					System.out.println(e.getResponseBody());
				} catch (Exception e) {
					System.out.println("Exception: " + e.getMessage());
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					System.out.println(sw.toString());
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			System.out.println("Registry Watcher Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			System.out.println(sw.toString());
		}
	}

	public static String getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
