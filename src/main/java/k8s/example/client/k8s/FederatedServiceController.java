package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.models.FederatedService;
import k8s.example.client.models.ServiceDNSRecord;
import k8s.example.client.models.ServiceDNSRecord.ServiceDNSRecordSpec;
import k8s.example.client.models.StateCheckInfo;

public class FederatedServiceController extends Thread {
	private Watch<FederatedService> fsController;
	private ApiClient client;
	private CustomObjectsApi api = null;

	private static Logger logger = Main.logger;
	private static long fslatestResourceVersion = 0;
	
	private static String FED_NS = "kube-federation-system";
	private static String ENV_FED_NS = "FED_NS";

	StateCheckInfo sci = new StateCheckInfo();
	
	FederatedServiceController(ApiClient client, CustomObjectsApi api, long fsresourceVersion)
			throws Exception {
		fsController = Watch.createWatch(client,
				api.listClusterCustomObjectCall(Constants.FED_OBJECT_RESOURCE_GROUP, Constants.FED_OBJECT_RESOURCE_VERSION,
						Constants.FED_OBJECT_RESOURCE_SERVICE_PLURAL, null, null, null, null, null, null, null,
						Boolean.TRUE, null),
				new TypeToken<Watch.Response<FederatedService>>() {
				}.getType());
		this.api = api;
		this.client = client;
		fslatestResourceVersion = fsresourceVersion;
		setEnv();
	}

	public void run() {
		try {
			while (true) {
				sci.checkThreadState();
				
				fsController.forEach(response -> {
					try {
						if (Thread.interrupted()) {
							logger.error("[FederatedService controller] Interrupted");
							fsController.close();
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
					}

					// Logic here
					try {
						FederatedService fs = response.object;
						if (fs != null) {
							String eventType = response.type.toString(); // ADDED, MODIFIED, DELETED
							logger.info("[FederatedService controller] Event Type : " + eventType);
							
							if (fs.getMetadata().getAnnotations() != null
									&& fs.getMetadata().getAnnotations().containsKey("isExternalDNS") 
									&& fs.getMetadata().getAnnotations().get("isExternalDNS").toLowerCase().equals("true")) {
								if(createServiceDNSRecord(fs.getMetadata().getName(), fs.getMetadata().getNamespace())) {
									annotateFederatedService(fs.getMetadata().getName(), fs.getMetadata().getNamespace(), "success");
									logger.info("[FederatedService controller] "+fs.getMetadata().getName()+" is successed");
								}
								else {
									annotateFederatedService(fs.getMetadata().getName(), fs.getMetadata().getNamespace(), "fail");
									logger.info("[FederatedService controller] "+fs.getMetadata().getName()+" is failed");
								}
							}							
						}					
					} catch (Exception e) {
						printException(e, "FederatedService handle");
					} catch (Throwable e) {
						e.printStackTrace();
					}
				});
				logger.info("=============== FederatedService 'For Each' END ===============");
				fsController = Watch.createWatch(client,
						api.listClusterCustomObjectCall(Constants.FED_OBJECT_RESOURCE_GROUP, Constants.FED_OBJECT_RESOURCE_VERSION,
								Constants.FED_OBJECT_RESOURCE_SERVICE_PLURAL, null, null, null, null, null, null, null,
								Boolean.TRUE, null),
						new TypeToken<Watch.Response<FederatedService>>() {
						}.getType());
			}
		} catch (Exception e) {
			printException(e, "FederatedService Controller");
		}
	}

	private String getDomainName(String string) {
		String result = null;
		String[] split = string.split(",");
		for(int i = 0; i < split.length; i++) {
			if(split[i].contains("name=")) {
				result = split[i].split("=")[1];
			}
		}
		return result;
	}
	
	private boolean createServiceDNSRecord(String name, String namespace) {
		boolean result = true;
		ServiceDNSRecord sdr = new ServiceDNSRecord();
		V1ObjectMeta meta = new V1ObjectMeta();
		ServiceDNSRecordSpec sdrSpec = new ServiceDNSRecordSpec();
		Object listDomain = null;
		
		meta.setName(name);
		meta.setNamespace(namespace);
		try {
			listDomain = api.listNamespacedCustomObject(Constants.EXTERNAL_OBJECT_GROUP, Constants.EXTERNAL_OBJECT_VERSION, FED_NS, Constants.EXTERNAL_OBJECT_PLURAL_DOMAIN, null, null, null, null, null, null, null, false);
			sdrSpec.setDomainRef(getDomainName(listDomain.toString()));
			sdrSpec.setRecordTTL(300);
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			printException(e, "Get Domain");
			result = false;
		}
		sdr.setApiVersion(Constants.EXTERNAL_OBJECT_GROUP+"/"+Constants.EXTERNAL_OBJECT_VERSION);
		sdr.setKind(Constants.EXTERNAL_OBJECT_KIND_SERVICEDNSRECORD);
		sdr.setMetadata(meta);
		sdr.setSpec(sdrSpec);
		
		try {
			api.createNamespacedCustomObject(Constants.EXTERNAL_OBJECT_GROUP, Constants.EXTERNAL_OBJECT_VERSION, namespace, Constants.EXTERNAL_OBJECT_PLURAL_SERVICEDNSRECORD, sdr, null);
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			printException(e, "Create ServiceDNSRecord");
			result = false;
		}

		return result;
	}
	
	@SuppressWarnings("unchecked")
	private void annotateFederatedService(String name, String namespace, String annotate) throws ApiException {
		JsonArray patchStatusArray = new JsonArray();
		JsonObject patchStatus = new JsonObject();
		JsonObject statusObject = new JsonObject();

		statusObject.addProperty("isExternalDNS", annotate);

		patchStatus.addProperty("op", "replace");
		patchStatus.addProperty("path", "/metadata/annotations");
		patchStatus.add("value", statusObject);

		patchStatusArray.add(patchStatus);

		try {
			api.patchNamespacedCustomObject(
					Constants.FED_OBJECT_RESOURCE_GROUP, Constants.FED_OBJECT_RESOURCE_VERSION, namespace, 
					Constants.FED_OBJECT_RESOURCE_SERVICE_PLURAL, name, patchStatusArray);
		} catch (ApiException e) {
			throw e;
		}
	}

	private static void printException(Exception e, String message) {
		logger.error("[FederatedService controller] " + message + " Error");
		logger.error("[FederatedService controller] Exception: " + e.getMessage());
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		logger.error(sw.toString());
	}

	public static void printException(ApiException e, String message) {
		if (e.getCode() == 404)
			return;

		logger.error("[FederatedService controller] " + message + " Error");
		logger.error("Status code: " + e.getCode());
		logger.error("Reason: " + e.getResponseBody());
		logger.error("Response headers: " + e.getResponseHeaders());
		e.printStackTrace();
	}

	public static long getLatestResourceVersion() {
		return fslatestResourceVersion;
	}
	
	private void setEnv() {
		if(System.getenv(ENV_FED_NS) !=null) FED_NS = System.getenv(ENV_FED_NS);
	}
}
