package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;

public class RegistryPvcWatcher extends Thread {
	private final Watch<V1PersistentVolumeClaim> watchRegistryPvc;
	private static String latestResourceVersion = "0";

    private Logger logger = Main.logger;
    
	RegistryPvcWatcher(ApiClient client, CoreV1Api api, String resourceVersion) throws Exception {
		watchRegistryPvc = Watch.createWatch(
		        client,
		        api.listPersistentVolumeClaimForAllNamespacesCall(null, null, null, "app=registry", null, null, null, null, Boolean.TRUE, null),
		        new TypeToken<Watch.Response<V1PersistentVolumeClaim>>(){}.getType()
        );
		
		latestResourceVersion = resourceVersion;	
	}
	
	@Override
	public void run() {
		try {
			watchRegistryPvc.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						logger.debug("Interrupted!");
						watchRegistryPvc.close();
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				
				
				// Logic here
				try {
					V1PersistentVolumeClaim pvc = response.object;
					
					if( pvc != null ) {
						
						latestResourceVersion = response.object.getMetadata().getResourceVersion();
						String eventType = response.type.toString();
						logger.debug("[RegistryPvcWatcher] Registry PVC " + eventType + "\n");

						K8sApiCaller.updateRegistryStatus(pvc, eventType);
						
					}
//					logger.debug("[RegistryPvcWatcher] Save latestHandledResourceVersion of RegistryPvcWatcher [" + response.object.getMetadata().getName() + "]");
//					K8sApiCaller.updateLatestHandledResourceVersion(Constants.PLURAL_REGISTRY_PVC, response.object.getMetadata().getResourceVersion());
				} catch (ApiException e) {
//					logger.error("ApiException: " + e.getMessage());
//					logger.error(e.getResponseBody());
				} catch (Exception e) {
					logger.error("Exception: " + e.getMessage());
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					logger.error(sw.toString());
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			logger.debug("@@@@@@@@@@@@@@@@@@@@ Registry PVC 'For Each' END @@@@@@@@@@@@@@@@@@@@");
		} catch (Exception e) {
			logger.error("Registry Watcher Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
		}
	}

	public static String getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
