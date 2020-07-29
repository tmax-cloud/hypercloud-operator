package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1ReplicaSet;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;

public class RegistryReplicaSetWatcher extends Thread {
	private final Watch<V1ReplicaSet> watchRegistryRs;
	private static String latestResourceVersion = "0";

    private Logger logger = Main.logger;
    
	RegistryReplicaSetWatcher(ApiClient client, AppsV1Api appApi, String resourceVersion) throws Exception {
		watchRegistryRs = Watch.createWatch(
		        client,
		        appApi.listReplicaSetForAllNamespacesCall(null, null, null, "app=registry", null, null, null, null, Boolean.TRUE, null),
		        new TypeToken<Watch.Response<V1ReplicaSet>>(){}.getType()
        );
		
		latestResourceVersion = resourceVersion;	
	}
	
	@Override
	public void run() {
		try {
			watchRegistryRs.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						logger.debug("Interrupted!");
						watchRegistryRs.close();
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				
				
				// Logic here
				try {
					V1ReplicaSet rs = response.object;
					
					if( rs != null ) {
						
						latestResourceVersion = response.object.getMetadata().getResourceVersion();
						String eventType = response.type.toString();
						logger.debug("[RegistryReplicaSetWatcher] Registry ReplicaSet " + eventType + "\n");

						K8sApiCaller.updateRegistryStatus(rs, eventType);
						
					}
//					logger.debug("[RegistryReplicaSetWatcher] Save latestHandledResourceVersion of RegistryReplicaSetWatcher [" + response.object.getMetadata().getName() + "]");
//					K8sApiCaller.updateLatestHandledResourceVersion(Constants.PLURAL_REGISTRY_REPLICASET, response.object.getMetadata().getResourceVersion());
				} catch (ApiException e) {
//					logger.debug("ApiException: " + e.getMessage());
//					logger.debug(e.getResponseBody());
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
			logger.debug("@@@@@@@@@@@@@@@@@@@@ Registry ReplicaSet 'For Each' END @@@@@@@@@@@@@@@@@@@@");
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
