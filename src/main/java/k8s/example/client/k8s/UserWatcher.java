package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import k8s.example.client.DataObject.UserCR;

public class UserWatcher extends Thread {
	private final Watch<UserCR> watchUser;
	private static String latestResourceVersion = "0";

	UserWatcher(ApiClient client, CustomObjectsApi api, String resourceVersion) throws Exception {
		watchUser = Watch.createWatch(client,
				//api.listClusterCustomObjectCall("tmax.co.kr", "v1", "users", null, null, null, "encrypted=f", null, resourceVersion, null, Boolean.TRUE, null),
				api.listClusterCustomObjectCall("tmax.co.kr", "v1", "users", null, null, null, "encrypted=f", null, null, null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<UserCR>>() {}.getType());

		latestResourceVersion = resourceVersion;
	}
	
	@Override
	public void run() {
		try {
			watchUser.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						System.out.println("Interrupted!");
						watchUser.close();
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				
				latestResourceVersion = response.object.getMetadata().getResourceVersion();
				
				// Logic here
				try {
					System.out.println("[UserWatcher] Encrypt password of " + response.object.getUserInfo().getEmail());

					K8sApiCaller.encryptUserPassword(
							response.object.getUserInfo().getEmail().replace("@", "-"), 
							response.object.getUserInfo().getPassword(),
							response.object);
				} catch (ApiException e) {
					System.out.println("ApiException: " + e.getMessage());
					System.out.println(e.getResponseBody());
				} catch (Exception e) {
					System.out.println("Exception: " + e.getMessage());
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					System.out.println(sw.toString());
				}
			});
		} catch (Exception e) {
			System.out.println("User Watcher Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			System.out.println(sw.toString());
		}
	}

	public static String getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
