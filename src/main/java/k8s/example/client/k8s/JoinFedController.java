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

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.custom.V1Patch;
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
import k8s.example.client.models.KubeFedCluster;
import k8s.example.client.models.KubeFedCluster.KubeFedClusterSpec;
import k8s.example.client.models.KubeFedCluster.SecretRef;
import k8s.example.client.models.StateCheckInfo;

public class JoinFedController extends Thread {
	private Watch<V1Secret> jfController;
	private ApiClient client;
	private CoreV1Api coreApi;
	private CoreV1Api coreApiMember;
	private CustomObjectsApi api = null;
	private RbacAuthorizationV1Api rbaMemberMember;

	private static long latestResourceVersion = 0;
	
	private static ApiClient MemberClient;
	private static Logger logger = Main.logger;

	private static String KUBECONFIG = "-kubeconfig";
	private static String FED_HOSTCLUSTER = "-hostcluster";
	private static String FED_NS = "kube-federation-system";
	private static String ENV_FED_NS = "FED_NS";
	private static String ENV_FED_HOSTCLUSTER = "HOSTCLUSTERNAME";
	private static String FED_CONTOLLER = "kubefed-controller-manager:";
	private static String HYPERCLOUD_OPERATOR = "-hypercloud4-operator";

	StateCheckInfo sci = new StateCheckInfo();
	
	JoinFedController(ApiClient client, CustomObjectsApi api, CoreV1Api coreApi, long resourceVersion)
			throws Exception {
		jfController = Watch.createWatch(client, coreApi.listSecretForAllNamespacesCall(null, null, null, null, null, null,  null, null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<V1Secret>>() {
				}.getType());
		this.api = api;
		this.client = client;
		this.coreApi = coreApi;
		latestResourceVersion = resourceVersion;
		setEnv();
	}

	public void run() {
		try {
			while (true) {
				sci.checkThreadState();
				jfController.forEach(response -> {
					try {
						if (Thread.interrupted()) {
							logger.info("[JoinFed controller] Interrupted");
							jfController.close();
						}
					} catch (Exception e) {
						logger.info(e.getMessage());
					}
					
					// Logic here
					try {
						V1Secret secret = response.object;
						if (secret != null && secret.getMetadata().getName().contains(KUBECONFIG)) {
							logger.info("[JoinFed controller] Event Type : " + response.type.toString()); // ADDED, MODIFIED, DELETED
							
							if (secret.getMetadata().getAnnotations() != null && secret.getMetadata().getAnnotations().containsKey("federation")) {
								if (secret.getMetadata().getAnnotations().get("federation").toLowerCase().equals("join")) {
									deleteFed(secret);
									if (joinFed(secret))
										replaceCCAnnotate(secret.getMetadata().getName(), "joined");
									else {
										deleteFed(secret);
										replaceCCAnnotate(secret.getMetadata().getName(), "Error");
									}
								} else if (secret.getMetadata().getAnnotations().get("federation").toLowerCase()
										.equals("detach")) {
									deleteFed(secret);
									replaceCCAnnotate(secret.getMetadata().getName(), "detached");
								}
							}
						}						
					} catch (Exception e) {
						printException(e, "JoinFed handle");
					} catch (Throwable e) {
						e.printStackTrace();
					}
				});
				logger.info("=============== JoinFed 'For Each' END ===============");
                jfController = Watch.createWatch(client, coreApi.listSecretForAllNamespacesCall(null, null, null, null, null, null,  null, null, Boolean.TRUE, null),
                                new TypeToken<Watch.Response<V1Secret>>() {
                                }.getType());
			}
		} catch (Exception e) {
			printException(e, "JoinFed controller");
		}
	}

	private void setEnv() {
		if(System.getenv(ENV_FED_HOSTCLUSTER) != null) FED_HOSTCLUSTER = "-"+System.getenv(ENV_FED_HOSTCLUSTER);
		if(System.getenv(ENV_FED_NS) !=null) FED_NS = System.getenv(ENV_FED_NS);
	}
	
	private boolean joinFed(V1Secret kubeconfigSecret) {
		boolean result = false;
		Map<String, String> auth = null;
		String apiURL = null;
		String clusterName = kubeconfigSecret.getMetadata().getName().substring(0, kubeconfigSecret.getMetadata().getName().length() - KUBECONFIG.length());
		
		apiURL = setKubeConfig(kubeconfigSecret); // set kubeconfig to control join cluster.
		if (apiURL != null) auth = initMember(clusterName); // create necessary resources for fed-join to join cluster
		if (auth.size() == 2) result = initHost(clusterName, apiURL, auth); // set & create necessary resources in host cluster
		return result;
	}

	private String setKubeConfig(V1Secret kubeconfigSecret) {
		String apiURL = null;
		try {
			KubeConfig kubeconfig = KubeConfig
					.loadKubeConfig(new StringReader(new String(kubeconfigSecret.getData().get("value"))));
			apiURL = kubeconfig.getServer();
			MemberClient = Config.fromConfig(kubeconfig);
		} catch (IOException e) {
			logger.info("[JoinFed controller] setMemberConfig-setMemberClient Error");
			apiURL = null;
		}
		MemberClient.setConnectTimeout(0);
		MemberClient.setReadTimeout(0);
		MemberClient.setWriteTimeout(0);

		coreApiMember = new CoreV1Api(MemberClient);
		rbaMemberMember = new RbacAuthorizationV1Api(MemberClient);

		return apiURL;
	}

	private Map<String, String> initMember(String clusterName) {
		String saSecretName = null;
		Map<String, String> auth = new HashMap<String, String>();

		// create ns
		V1Namespace ns = new V1Namespace();
		V1ObjectMeta nsMeta = new V1ObjectMeta();
		nsMeta.setName(FED_NS);
		ns.setMetadata(nsMeta);
		try {
			ns = coreApiMember.createNamespace(ns, null, null, null);
			logger.info("[JoinFed controller] created namespace in member cluster");
		} catch (ApiException e) {
			printApiException(e, "initMember-createNamespace");
		}
		
		// create sa
		V1ServiceAccount sa = new V1ServiceAccount();
		V1ObjectMeta saMeta = new V1ObjectMeta();
		saMeta.setName(clusterName + FED_HOSTCLUSTER);
		saMeta.setNamespace(FED_NS);
		sa.setMetadata(saMeta);
		try {
			coreApiMember.createNamespacedServiceAccount(FED_NS, sa, null, null, null);
			logger.info("[JoinFed controller] created service account in member cluster ");
		} catch (ApiException e) {
			printApiException(e, "initMember-createServiceAccount");
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
			rbaMemberMember.createClusterRole(cr, null, null, null);
			logger.info("[JoinFed controller] created clusterrole in member cluster");
		} catch (ApiException e) {
			printApiException(e, "initMember-createClusterRole");
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
			rbaMemberMember.createClusterRoleBinding(crb, null, null, null);
			logger.info("[JoinFed controller] created clusterrolebinding in member cluster");
		} catch (ApiException e) {
			printApiException(e, "initMember-createClusterRoleBinding");
		}

		// get saSecret for caBundle & token
		try {
			sa = coreApiMember.readNamespacedServiceAccount(clusterName + FED_HOSTCLUSTER, FED_NS, null, null, null);
			saSecretName = sa.getSecrets().get(0).getName();
			logger.info("[JoinFed controller] get sa from join cluster");
		} catch (ApiException e) {
			printApiException(e, "initMember-getSa");
		}

		try {
			V1Secret saSecret = coreApiMember.readNamespacedSecret(saSecretName, FED_NS, null, false, false);
			String ca = new String(saSecret.getData().get("ca.crt"));
			String token = new String(saSecret.getData().get("token"));
			auth.put("ca", ca);
			auth.put("token", token);
			logger.info("[JoinFed controller] get secret from join cluster");
		} catch (ApiException e) {
			printApiException(e, "initMember-getSaSecret");
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
			logger.info("[JoinFed controller] secret created in host cluster");
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

	private void deleteFed(V1Secret kubeconfigSecret) {
		String clusterName = kubeconfigSecret.getMetadata().getName().substring(0, kubeconfigSecret.getMetadata().getName().length() - KUBECONFIG.length());
		
		setKubeConfig(kubeconfigSecret);
		delHost(clusterName);
		delMember(clusterName);
	}

	private void delMember(String clusterName) {		
		// delete ns
		try {
			coreApiMember.readNamespace(FED_NS, null, null, null);
			coreApiMember.deleteNamespace(FED_NS, null, null, 0, null, "Background", new V1DeleteOptions());
			logger.info("[JoinFed controller] namespace deleted in member cluster");
		} catch (IllegalStateException e) {
		} catch (ApiException e) {
			printApiException(e, "delMember-deleteNamespace");
		} catch (Exception e) {
			printException(e, "delMember-deleteNamespace");
		}

		// delete clusterrole
		String crName = FED_CONTOLLER + clusterName + FED_HOSTCLUSTER;
		try {
			rbaMemberMember.readClusterRole(crName, null);
			rbaMemberMember.deleteClusterRole(crName, null, null, null, null, null, null);
			logger.info("[JoinFed controller] clusterRole deleted in member cluster");
		} catch (ApiException e) {
			printApiException(e, "delMember-deleteClusterRole");
		}

		// delete clusterrolebinding
		String crbName = FED_CONTOLLER + clusterName + FED_HOSTCLUSTER;
		try {
			rbaMemberMember.readClusterRoleBinding(crbName, null);
			rbaMemberMember.deleteClusterRoleBinding(crbName, null, null, null, null, null, null);
			logger.info("[JoinFed controller] clusterRoleBinding deleted in member cluster");
		} catch (ApiException e) {
			printApiException(e, "delMember-deleteClusterRole");
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
			logger.info("[JoinFed controller] kubefedcluster deleted in host cluster");
		} catch (ApiException e) {
			printApiException(e, "delHost-deleteKubefedcluster");
		}

		// delete secret
		String secretName = clusterName + HYPERCLOUD_OPERATOR;
		try {
			coreApi.readNamespacedSecret(secretName, FED_NS, null, null, null);
			coreApi.deleteNamespacedSecret(secretName, FED_NS, null, null, null, null, null, null);
			logger.info("[JoinFed controller] secret deleted in host cluster");
		} catch (ApiException e) {
			printApiException(e, "delHost-deleteSecret");
		}
	}

	@SuppressWarnings("unchecked")
	private void replaceCCAnnotate(String name, String annotate) throws ApiException {
		String jsonPatchStr =
			      "[{\"op\":\"replace\",\"path\":\"/metadata/annotations/federation\",\"value\":\""+annotate+"\"}]";
				
		try {
			coreApi.patchNamespacedSecret(name, "default", new V1Patch(jsonPatchStr), null, null, null, null);
		} catch (ApiException e) {
			throw e;
		}
	}

	private static void printException(Exception e, String message) {
		logger.info("[JoinFed controller] " + message + " Error");
		logger.info("[JoinFed controller] Exception: " + e.getMessage());
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		logger.info(sw.toString());
	}

	public static void printApiException(ApiException e, String message) {
		if (e.getCode() == 404)
			return;

		logger.info("[JoinFed controller] " + message + " Error");
		logger.info("Status code: " + e.getCode());
		logger.info("Reason: " + e.getResponseBody());
		logger.info("Response headers: " + e.getResponseHeaders());
		e.printStackTrace();
	}

	public static long getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
