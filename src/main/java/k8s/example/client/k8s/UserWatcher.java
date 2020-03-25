package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Main;
import k8s.example.client.DataObject.UserCR;

public class UserWatcher extends Thread {
	private final Watch<UserCR> watchUser;
	private static String latestResourceVersion = "0";
    private Logger logger = Main.logger;

	UserWatcher(ApiClient client, CustomObjectsApi api, String resourceVersion) throws Exception {
		watchUser = Watch.createWatch(client,
				//api.listClusterCustomObjectCall("tmax.io", "v1", "users", null, null, null, "encrypted=f", null, resourceVersion, null, Boolean.TRUE, null),
				api.listClusterCustomObjectCall("tmax.io", "v1", "users", null, null, null, "encrypted=f", null, null, null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<UserCR>>() {}.getType());

		latestResourceVersion = resourceVersion;
	}
	
	@Override
	public void run() {
		try {
			watchUser.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						logger.info("Interrupted!");
						watchUser.close();
					}
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
				
				latestResourceVersion = response.object.getMetadata().getResourceVersion();
				
				// Logic here
				try {
					logger.info("[UserWatcher] Encrypt password of " + response.object.getUserInfo().getEmail());

					K8sApiCaller.encryptUserPassword(
							response.object.getUserInfo().getEmail().replace("@", "-"), 
							response.object.getUserInfo().getPassword(),
							response.object);
				} catch (ApiException e) {
					logger.info("ApiException: " + e.getMessage());
					logger.info(e.getResponseBody());
				} catch (Exception e) {
					logger.info("Exception: " + e.getMessage());
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					logger.info(sw.toString());
				}
			});
		} catch (Exception e) {
			logger.info("User Watcher Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
		}
	}

	public static String getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
