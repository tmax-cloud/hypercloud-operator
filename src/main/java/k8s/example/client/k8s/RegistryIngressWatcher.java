package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1Ingress;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;

public class RegistryIngressWatcher extends Thread {
	private final Watch<ExtensionsV1beta1Ingress> watchRegistryIngress;
	private static String latestResourceVersion = "0";

    private Logger logger = Main.logger;

	RegistryIngressWatcher(ApiClient client, ExtensionsV1beta1Api api, String resourceVersion) throws Exception {
		watchRegistryIngress = Watch.createWatch(
		        client,
		        api.listIngressForAllNamespacesCall(null, null, null, "app=registry", null, null, null, null, Boolean.TRUE, null),
		        new TypeToken<Watch.Response<ExtensionsV1beta1Ingress>>(){}.getType()
        );
		
		latestResourceVersion = resourceVersion;	
	}
	
	@Override
	public void run() {
		try {
			watchRegistryIngress.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						logger.debug("Interrupted!");
						watchRegistryIngress.close();
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				
				
				// Logic here
				try {
					ExtensionsV1beta1Ingress ingress = response.object;
					
					if( ingress != null ) {
						
						latestResourceVersion = response.object.getMetadata().getResourceVersion();
						String eventType = response.type.toString();
						logger.debug("[RegistryIngressWatcher] Registry Ingress " + eventType + "\n"
						);

						K8sApiCaller.updateRegistryStatus(ingress, eventType);
						
					}
//					logger.debug("[RegistryIngressWatcher] Save latestHandledResourceVersion of RegistryIngressWatcher [" + response.object.getMetadata().getName() + "]");
//					K8sApiCaller.updateLatestHandledResourceVersion(Constants.PLURAL_REGISTRY_INGRESS, response.object.getMetadata().getResourceVersion());
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
			logger.debug("@@@@@@@@@@@@@@@@@@@@ Registry Ingress 'For Each' END @@@@@@@@@@@@@@@@@@@@");
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
