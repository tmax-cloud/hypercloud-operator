package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.models.CapiCluster;

public class CapiClusterController extends Thread {
	private Watch<CapiCluster> ccController;
	private CoreV1Api coreApi;
	private CustomObjectsApi api = null;

	private static Logger logger = Main.logger;
	private static long cclatestResourceVersion = 0;

	private static String KUBECONFIG = "-kubeconfig";

	CapiClusterController(ApiClient client, CustomObjectsApi api, CoreV1Api coreApi, long ccresourceVersion)
			throws Exception {
		ccController = Watch.createWatch(client,
				api.listClusterCustomObjectCall(Constants.CAPI_OBJECT_GROUP, Constants.CAPI_OBJECT_VERSION,
						Constants.CAPI_OBJECT_PLURAL_CAPICLUSTER, null, null, null, null, null, null, null,
						Boolean.TRUE, null),
				new TypeToken<Watch.Response<CapiCluster>>() {
				}.getType());
		this.api = api;
		this.coreApi = coreApi;
		cclatestResourceVersion = ccresourceVersion;
	}

	public void run() {
		try {
			while (true) {
				ccController.forEach(response -> {
					try {
						if (Thread.interrupted()) {
							logger.error("[CapiCluster controller] Interrupted");
							ccController.close();
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
					}

					// Logic here
					String clusterName = "unknown";
					try {
						CapiCluster cc = response.object;
						if (cc != null) {
							String eventType = response.type.toString(); // ADDED, MODIFIED, DELETED
							logger.info("[CapiCluster controller] Event Type : " + eventType);
							clusterName = cc.getMetadata().getName();

							if (cc.getMetadata().getAnnotations() != null
									&& cc.getStatus() != null
									&& cc.getStatus().getControlPlaneInitialized() != null
									&& cc.getMetadata().getAnnotations().containsKey("federation") //AFJ(Auto Fed Join)
									&& cc.getStatus().getControlPlaneInitialized().equals("true")
									&& cc.getMetadata().getAnnotations().get("federation").toLowerCase().equals("join")) {
								if(annotateKubeConfigSecret(clusterName+KUBECONFIG, "join")) replaceCCAnnotate(clusterName, "success");
								else replaceCCAnnotate(clusterName, "error");
							}
						}					
					} catch (Exception e) {
						printException(e, "CapiCluster handle");
					} catch (Throwable e) {
						e.printStackTrace();
					}
				});
			}
		} catch (Exception e) {
			printException(e, "CapiCluster Controller");
		}
	}

	private boolean annotateKubeConfigSecret(String name, String annotate) {
		boolean result = false;
		try {
			V1Secret temp = coreApi.readNamespacedSecret(name, "default", null, null, null);
			
			Map<String, String> temp2 = new HashMap();
			if(temp.getMetadata().getAnnotations() != null) temp2 = temp.getMetadata().getAnnotations();
			temp2.put("federation", "join");
			temp.getMetadata().setAnnotations(temp2);
			coreApi.replaceNamespacedSecret(name, "default", temp, null, null, null);
			result = true;
		} catch (ApiException e1) {
			printException(e1, "CapiCluster annotaubteKubeConfigSecret1");
		}
				
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private void replaceCCAnnotate(String name, String annotate) throws ApiException {
		JsonArray patchStatusArray = new JsonArray();
		JsonObject patchStatus = new JsonObject();
		JsonObject statusObject = new JsonObject();

		statusObject.addProperty("federation", annotate);

		patchStatus.addProperty("op", "replace");
		patchStatus.addProperty("path", "/metadata/annotations");
		patchStatus.add("value", statusObject);

		patchStatusArray.add(patchStatus);

		try {
			api.patchNamespacedCustomObject(Constants.CAPI_OBJECT_GROUP, Constants.CAPI_OBJECT_VERSION, "default",
					Constants.CAPI_OBJECT_PLURAL_CAPICLUSTER, name, patchStatusArray);
		} catch (ApiException e) {
			throw e;
		}
	}

	private static void printException(Exception e, String message) {
		logger.error("[CapiCluster controller] " + message + " Error");
		logger.error("[CapiCluster controller] Exception: " + e.getMessage());
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		logger.error(sw.toString());
	}

	public static void printApiException(ApiException e, String message) {
		if (e.getCode() == 404)
			return;

		logger.error("[CapiCluster controller] " + message + " Error");
		logger.error("Status code: " + e.getCode());
		logger.error("Reason: " + e.getResponseBody());
		logger.error("Response headers: " + e.getResponseHeaders());
		e.printStackTrace();
	}

	public static long getLatestResourceVersion() {
		return cclatestResourceVersion;
	}
}
