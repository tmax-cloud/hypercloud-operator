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

public class RegistryTlsSecretWatcher extends Thread {
	private final Watch<V1Secret> watchRegistrySecret;
	private static String latestResourceVersion = "0";

    private Logger logger = Main.logger;
    
	RegistryTlsSecretWatcher(ApiClient client, CoreV1Api api, String resourceVersion) throws Exception {
		watchRegistrySecret = Watch.createWatch(
		        client,
		        api.listSecretForAllNamespacesCall(null, null, null, "secret=tls", null, null, null, null, Boolean.TRUE, null),
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
						logger.debug("Interrupted!");
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
						logger.debug("[RegistryTlsSecretWatcher] Registry Cert Secret " + eventType + "\n");

						K8sApiCaller.updateRegistryStatus(secret, eventType);
						
					}
//					logger.debug("[RegistryTlsSecretWatcher] Save latestHandledResourceVersion of RegistryTlsSecretWatcher [" + response.object.getMetadata().getName() + "]");
//					K8sApiCaller.updateLatestHandledResourceVersion(Constants.PLURAL_REGISTRY_TLS, response.object.getMetadata().getResourceVersion());
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
			logger.debug("@@@@@@@@@@@@@@@@@@@@ Registry Tls Secret 'For Each' END @@@@@@@@@@@@@@@@@@@@");
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
