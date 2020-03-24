package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;

public class RegistryPodWatcher extends Thread {
	private final Watch<V1Pod> watchRegistryPod;
	private static String latestResourceVersion = "0";

	RegistryPodWatcher(ApiClient client, CoreV1Api api, String resourceVersion) throws Exception {
		watchRegistryPod = Watch.createWatch(
		        client,
		        api.listPodForAllNamespacesCall(null, null, null, "app=registry", null, null, null, null, Boolean.TRUE, null),
		        new TypeToken<Watch.Response<V1Pod>>(){}.getType()
        );
		
		latestResourceVersion = resourceVersion;	
	}
	
	@Override
	public void run() {
		try {
			watchRegistryPod.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						System.out.println("Interrupted!");
						watchRegistryPod.close();
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				
				
				// Logic here
				try {
					V1Pod pod = response.object;
					
					if( pod != null) {
						latestResourceVersion = response.object.getMetadata().getResourceVersion();
						String eventType = response.type.toString();
						System.out.println("[RegistryPodWatcher] ( Registry Pod " + eventType + " ) \n" + pod.toString());
						System.out.println("[RegistryPodWatcher] pod status: " + response.status.toString());
						System.out.println("[RegistryPodWatcher] pod: " + pod.toString());
						
						switch(eventType) {
						case Constants.EVENT_TYPE_ADDED : 
							
							break;
						case Constants.EVENT_TYPE_MODIFIED : 
							
							break;
						case Constants.EVENT_TYPE_DELETED : 
							
							break;
						}						
					}
//				} catch (ApiException e) {
//					System.out.println("ApiException: " + e.getMessage());
//					System.out.println(e.getResponseBody());
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
