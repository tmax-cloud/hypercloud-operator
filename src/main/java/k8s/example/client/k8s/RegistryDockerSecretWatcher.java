package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;

public class RegistryDockerSecretWatcher extends Thread {
	private final Watch<V1Secret> watchRegistrySecret;
	private static String latestResourceVersion = "0";

    private Logger logger = Main.logger;
    
	RegistryDockerSecretWatcher(ApiClient client, CoreV1Api api, String resourceVersion) throws Exception {
		watchRegistrySecret = Watch.createWatch(
		        client,
		        api.listSecretForAllNamespacesCall(null, null, null, "secret=docker", null, null, null, null, Boolean.TRUE, null),
		        new TypeToken<Watch.Response<V1Secret>>(){}.getType()
        );
		
		latestResourceVersion = resourceVersion;	
	}
	
	@Override
	public void run() {
		try {
			watchRegistrySecret.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						logger.error("Interrupted!");
						watchRegistrySecret.close();
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				
				
				// Logic here
				try {
					V1Secret secret = response.object;
					
					if( secret != null ) {
						
						latestResourceVersion = response.object.getMetadata().getResourceVersion();
						String eventType = response.type.toString();
						logger.debug("[RegistryDockerSecretWatcher] Registry Docker Secret " + eventType + "\n");
						K8sApiCaller.updateRegistryStatus(secret, eventType);						
					}
//					logger.debug("[RegistryDockerSecretWatcher] Save latestHandledResourceVersion of RegistryDockerSecretWatcher [" + response.object.getMetadata().getName() + "]");
//					K8sApiCaller.updateLatestHandledResourceVersion(Constants.PLURAL_REGISTRY_DOCKER, response.object.getMetadata().getResourceVersion());
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
			logger.debug("@@@@@@@@@@@@@@@@@@@@ Registry Docker Secret 'For Each' END @@@@@@@@@@@@@@@@@@@@");
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
