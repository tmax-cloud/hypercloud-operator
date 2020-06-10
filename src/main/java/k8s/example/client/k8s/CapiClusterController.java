package k8s.example.client.k8s;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64.Encoder;

import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.models.V1ClusterRole;
import io.kubernetes.client.openapi.models.V1ClusterRoleBinding;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PolicyRule;
import io.kubernetes.client.openapi.models.V1RoleRef;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1ServiceAccount;
import io.kubernetes.client.openapi.models.V1Subject;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.models.CapiCluster;
import k8s.example.client.models.KubeFedCluster;
import k8s.example.client.models.KubeFedCluster.KubeFedClusterSpec;
import k8s.example.client.models.KubeFedCluster.SecretRef;

public class CapiClusterController extends Thread {
	private Watch<CapiCluster> ccController;
	private CoreV1Api coreApi;
	private CoreV1Api coreApiCapi;
	private CustomObjectsApi api = null;
	private RbacAuthorizationV1Api rbacApiCapi;

	private static ApiClient CapiClient;
	private static Logger logger = Main.logger;
	private static long cclatestResourceVersion = 0;

	private static String KUBECONFIG = "-kubeconfig";
	private static String FED_HOSTCLUSTER = "-hostcluster";
	private static String FED_NS = "kube-federation-system";
	private static String ENV_FED_NS = "FED_NS";
	private static String ENV_FED_HOSTCLUSTER = "HOST_CLUSTER_NAME";
	private static String FED_CONTOLLER = "kubefed-controller-manager:";
	private static String HYPERCLOUD_OPERATOR = "-hypercloud4-operator";

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
		setEnv();
	}

	public void run() {
		try {
			while (true) {
				ccController.forEach(response -> {
					try {
						if (Thread.interrupted()) {
							logger.info("[CapiCluster controller] Interrupted");
							ccController.close();
						}
					} catch (Exception e) {
						logger.info(e.getMessage());
					}

					// Logic here
					String clusterName = "unknown";
					try {
						CapiCluster cc = response.object;
						if (cc != null) {
							cclatestResourceVersion = Long
									.parseLong(response.object.getMetadata().getResourceVersion());
							String eventType = response.type.toString(); // ADDED, MODIFIED, DELETED
							logger.info("[CapiCluster controller] Event Type : " + eventType);
							clusterName = cc.getMetadata().getName();

							if (cc.getMetadata().getAnnotations() != null
									&& cc.getMetadata().getAnnotations().containsKey("federation")
									&& cc.getStatus().getControlPlaneInitialized().equals("true")) {
								if (cc.getMetadata().getAnnotations().get("federation").toLowerCase().equals("join")) {
									deleteFed(clusterName);
									if (joinFed(clusterName))
										replaceCCAnnotate(cc.getMetadata().getName(), "joined");
									else {
										deleteFed(clusterName);
										replaceCCAnnotate(cc.getMetadata().getName(), "Error");
									}
								} else if (cc.getMetadata().getAnnotations().get("federation").toLowerCase()
										.equals("detach")) {
									deleteFed(clusterName);
									replaceCCAnnotate(cc.getMetadata().getName(), "detached");
								}
							}
						}
					} catch (Exception e) {
						printException(e, "CapiCluster handle");
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			}
		} catch (Exception e) {
			printException(e, "CapiCluster Controller");
		}
	}

	private void setEnv() {
		if(System.getenv(ENV_FED_HOSTCLUSTER) != null) FED_HOSTCLUSTER = "-"+System.getenv(ENV_FED_HOSTCLUSTER);
		if(System.getenv(ENV_FED_NS) !=null) FED_NS = System.getenv(ENV_FED_NS);
	}
	
	private boolean joinFed(String clusterName) {
		boolean result = false;
		Map<String, String> auth = null;
		String apiURL = null;

		apiURL = setCapiConfig(clusterName); // set kubeconfig to control join cluster.
		if (apiURL != null) auth = initCapi(clusterName); // create necessary resources for fed-join to join cluster
		if (auth.size() == 2) result = initHost(clusterName, apiURL, auth); // set & create necessary resources in host cluster
		return result;
	}

	private String setCapiConfig(String Cfname) {
		String apiURL = null;
		try {
			V1Secret secret = coreApi.readNamespacedSecret(Cfname + KUBECONFIG, "default", null, false, false);
			try {
				KubeConfig kubeconfig = KubeConfig
						.loadKubeConfig(new StringReader(new String(secret.getData().get("value"))));
				apiURL = kubeconfig.getServer();
				CapiClient = Config.fromConfig(kubeconfig);
			} catch (IOException e) {
				logger.info("[CapiCluster controller] setCapiConfig-setCapiClient Error");
				apiURL = null;
			}
			CapiClient.setConnectTimeout(0);
			CapiClient.setReadTimeout(0);
			CapiClient.setWriteTimeout(0);

			coreApiCapi = new CoreV1Api(CapiClient);
			rbacApiCapi = new RbacAuthorizationV1Api(CapiClient);
		} catch (ApiException e) {
			printApiException(e, "setCapiConfig-getSecret");
			apiURL = null;
		}

		return apiURL;
	}

	private Map<String, String> initCapi(String clusterName) {
		String saSecretName = null;
		Map<String, String> auth = new HashMap<String, String>();

		// create ns
		V1Namespace ns = new V1Namespace();
		V1ObjectMeta nsMeta = new V1ObjectMeta();
		nsMeta.setName(FED_NS);
		ns.setMetadata(nsMeta);
		try {
			ns = coreApiCapi.createNamespace(ns, null, null, null);
			logger.info("[CapiCluster controller] created namespace in joning cluster");
		} catch (ApiException e) {
			printApiException(e, "initCapi-createNamespace");
		}
		// create sa
		V1ServiceAccount sa = new V1ServiceAccount();
		V1ObjectMeta saMeta = new V1ObjectMeta();
		saMeta.setName(clusterName + FED_HOSTCLUSTER);
		saMeta.setNamespace(FED_NS);
		sa.setMetadata(saMeta);
		try {
			coreApiCapi.createNamespacedServiceAccount(FED_NS, sa, null, null, null);
			logger.info("[CapiCluster controller] created service account in joning cluster ");
		} catch (ApiException e) {
			printApiException(e, "initCapi-createServiceAccount");
		}
		// create cluster role
		V1ClusterRole cr = new V1ClusterRole();
		V1ObjectMeta crMeta = new V1ObjectMeta();
		crMeta.setName(FED_CONTOLLER + clusterName + FED_HOSTCLUSTER);
		cr.setMetadata(crMeta);
		List<V1PolicyRule> rules = new ArrayList<>();

		V1PolicyRule rule = new V1PolicyRule();
		rule.addApiGroupsItem("*");
		rule.addResourcesItem("*");
		rule.addVerbsItem("*");
		rules.add(rule);

		rule = new V1PolicyRule();
		rule.addNonResourceURLsItem("*");
		rule.addVerbsItem("*");
		rules.add(rule);

		cr.setRules(rules);

		try {
			rbacApiCapi.createClusterRole(cr, null, null, null);
			logger.info("[CapiCluster controller] created clusterrole in joning cluster");
		} catch (ApiException e) {
			printApiException(e, "initCapi-createClusterRole");
		}

		// create cluster role binding
		V1ClusterRoleBinding crb = new V1ClusterRoleBinding();
		V1ObjectMeta crbMeta = new V1ObjectMeta();
		crbMeta.setName(FED_CONTOLLER + clusterName + FED_HOSTCLUSTER);
		crb.setMetadata(crbMeta);
		V1RoleRef rr = new V1RoleRef();
		rr.setApiGroup("rbac.authorization.k8s.io");
		rr.setKind("ClusterRole");
		rr.setName(FED_CONTOLLER + clusterName + FED_HOSTCLUSTER);
		crb.setRoleRef(rr);
		List<V1Subject> sl = new ArrayList<>();
		V1Subject s = new V1Subject();
		s.setKind("ServiceAccount");
		s.setName(clusterName + FED_HOSTCLUSTER);
		s.setNamespace(FED_NS);
		sl.add(s);
		crb.setSubjects(sl);

		try {
			rbacApiCapi.createClusterRoleBinding(crb, null, null, null);
			logger.info("[CapiCluster controller] created clusterrolebinding in joning cluster");
		} catch (ApiException e) {
			printApiException(e, "initCapi-createClusterRoleBinding");
		}

		// get saSecret for caBundle & token
		try {
			sa = coreApiCapi.readNamespacedServiceAccount(clusterName + FED_HOSTCLUSTER, FED_NS, null, null, null);
			saSecretName = sa.getSecrets().get(0).getName();
			logger.info("[CapiCluster controller] get sa from join cluster");
		} catch (ApiException e) {
			printApiException(e, "initCapi-getSa");
		}

		try {
			V1Secret saSecret = coreApiCapi.readNamespacedSecret(saSecretName, FED_NS, null, false, false);
			String ca = new String(saSecret.getData().get("ca.crt"));
			String token = new String(saSecret.getData().get("token"));
			auth.put("ca", ca);
			auth.put("token", token);
			logger.info("[CapiCluster controller] get secret from join cluster");
		} catch (ApiException e) {
			printApiException(e, "initCapi-getSaSecret");

		}

		return auth;
	}

	private boolean initHost(String clusterName, String apiURL, Map<String, String> auth) {
		// create secret in host cluster
		V1Secret secret = new V1Secret();
		V1ObjectMeta secretMeta = new V1ObjectMeta();
		secretMeta.setName(clusterName + HYPERCLOUD_OPERATOR);
		secretMeta.setNamespace(FED_NS);
		secret.setMetadata(secretMeta);

		Map<String, byte[]> data = new HashMap<>();
		data.put("token", auth.get("token").getBytes());
		secret.setData(data);

		try {
			coreApi.createNamespacedSecret(FED_NS, secret, null, null, null);
			logger.info("[CapiCluster controller] secret created in host cluster");
		} catch (ApiException e) {
			printApiException(e, "initHost-createSecret");
			return false;
		}

		// create kubefedcluster crd
		KubeFedCluster kfc = new KubeFedCluster();
		V1ObjectMeta kfcMeta = new V1ObjectMeta();
		Encoder encoder = Base64.getEncoder();
		kfc.setApiVersion("core.kubefed.io/v1beta1");
		kfc.setKind("KubeFedCluster");
		kfcMeta.setName(clusterName);
		kfcMeta.setNamespace(FED_NS);
		kfc.setMetadata(kfcMeta);
		KubeFedClusterSpec kfcs = new KubeFedClusterSpec();
		kfcs.setApiEndpoint(apiURL);
		kfcs.setCaBundle(new String(encoder.encode(auth.get("ca").getBytes())));
		SecretRef sr = new SecretRef();
		sr.setName(clusterName + HYPERCLOUD_OPERATOR);
		kfcs.setSecretRef(sr);
		kfc.setSpec(kfcs);

		try {
			api.createNamespacedCustomObject(Constants.FED_OBJECT_GROUP, Constants.FED_OBJECT_VERSION, FED_NS,
					Constants.FED_OBJECT_FEDCLUSTER_PLURAL, kfc, null);
		} catch (ApiException e) {
			printApiException(e, "initHost-createKubefedcluster");
			return false;
		}

		return true;
	}

	private void deleteFed(String clusterName) {
		setCapiConfig(clusterName);
		delHost(clusterName);
		delCapi(clusterName);
	}

	private void delCapi(String clusterName) {
		// delete ns
		try {
			coreApiCapi.readNamespace(FED_NS, null, null, null);
			coreApiCapi.deleteNamespace(FED_NS, null, null, 0, null, "Background", new V1DeleteOptions());
			logger.info("[CapiCluster controller] namespace deleted in joining cluster");
		} catch (IllegalStateException e) {
		} catch (ApiException e) {
			printApiException(e, "delCapi-deleteNamespace");
		} catch (Exception e) {
			printException(e, "delCapi-deleteNamespace");
		}

		// delete clusterrole
		String crName = FED_CONTOLLER + clusterName + FED_HOSTCLUSTER;
		try {
			rbacApiCapi.readClusterRole(crName, null);
			rbacApiCapi.deleteClusterRole(crName, null, null, null, null, null, null);
			logger.info("[CapiCluster controller] clusterRole deleted in joining cluster");
		} catch (ApiException e) {
			printApiException(e, "delCapi-deleteClusterRole");
		}

		// delete clusterrolebinding
		String crbName = FED_CONTOLLER + clusterName + FED_HOSTCLUSTER;
		try {
			rbacApiCapi.readClusterRoleBinding(crbName, null);
			rbacApiCapi.deleteClusterRoleBinding(crbName, null, null, null, null, null, null);
			logger.info("[CapiCluster controller] clusterRoleBinding deleted in joining cluster");
		} catch (ApiException e) {
			printApiException(e, "delCapi-deleteClusterRole");
		}

	}

	private void delHost(String clusterName) {
		// delete kubefedcluster crd
		String kfcName = clusterName;
		try {
			api.getNamespacedCustomObject(Constants.FED_OBJECT_GROUP, Constants.FED_OBJECT_VERSION, FED_NS,
					Constants.FED_OBJECT_FEDCLUSTER_PLURAL, kfcName);
			api.deleteNamespacedCustomObject(Constants.FED_OBJECT_GROUP, Constants.FED_OBJECT_VERSION, FED_NS,
					Constants.FED_OBJECT_FEDCLUSTER_PLURAL, kfcName, null, null, null, null);
			logger.info("[CapiCluster controller] kubefedcluster deleted in host cluster");
		} catch (ApiException e) {
			printApiException(e, "delHost-deleteKubefedcluster");
		}

		// delete secret
		String secretName = clusterName + HYPERCLOUD_OPERATOR;
		try {
			coreApi.readNamespacedSecret(secretName, FED_NS, null, null, null);
			coreApi.deleteNamespacedSecret(secretName, FED_NS, null, null, null, null, null, null);
			logger.info("[CapiCluster controller] secret deleted in host cluster");
		} catch (ApiException e) {
			printApiException(e, "delHost-deleteSecret");
		}
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
		logger.info("[CapiCluster controller] " + message + " Error");
		logger.info("[CapiCluster controller] Exception: " + e.getMessage());
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		logger.info(sw.toString());
	}

	public static void printApiException(ApiException e, String message) {
		if (e.getCode() == 404)
			return;

		logger.info("[CapiCluster controller] " + message + " Error");
		logger.info("Status code: " + e.getCode());
		logger.info("Reason: " + e.getResponseBody());
		logger.info("Response headers: " + e.getResponseHeaders());
		e.printStackTrace();
	}

	public static long getLatestResourceVersion() {
		return cclatestResourceVersion;
	}
}
