package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;

public class RegistryConfigMapWatcher extends Thread {
	private final Watch<V1ConfigMap> watchRegistryConfigMap;
	private static String latestResourceVersion = "0";

    private Logger logger = Main.logger;
    
	RegistryConfigMapWatcher(ApiClient client, CoreV1Api api, String resourceVersion) throws Exception {
		watchRegistryConfigMap = Watch.createWatch(
		        client,
		        api.listConfigMapForAllNamespacesCall(null, null, null, "app=registry", null, null, null, null, Boolean.TRUE, null),
		        new TypeToken<Watch.Response<V1ConfigMap>>(){}.getType()
        );
		
		latestResourceVersion = resourceVersion;	
	}
	
	@Override
	public void run() {
		try {
			watchRegistryConfigMap.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						logger.info("Interrupted!");
						watchRegistryConfigMap.close();
					}
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
				
				
				// Logic here
				try {
					V1ConfigMap cm = response.object;
					
					if( cm != null ) {
						
						latestResourceVersion = response.object.getMetadata().getResourceVersion();
						String eventType = response.type.toString();
						logger.info("[RegistryConfigMapWatcher] Registry ConfigMap " + eventType + "\n");

						K8sApiCaller.updateRegistryStatus(cm, eventType);
						
					}
//					logger.info("[RegistryConfigMapWatcher] Save latestHandledResourceVersion of RegistryConfigMapWatcher [" + response.object.getMetadata().getName() + "]");
//					K8sApiCaller.updateLatestHandledResourceVersion(Constants.PLURAL_REGISTRY_ConfigMap, response.object.getMetadata().getResourceVersion());
				} catch (ApiException e) {
//					logger.info("ApiException: " + e.getMessage());
//					logger.info(e.getResponseBody());
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
			logger.info("@@@@@@@@@@@@@@@@@@@@ Registry ConfigMap 'For Each' END @@@@@@@@@@@@@@@@@@@@");
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
