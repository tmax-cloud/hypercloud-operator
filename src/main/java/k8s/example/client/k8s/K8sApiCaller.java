package k8s.example.client.k8s;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.ProgressResponseBody;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.AuthorizationV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.auth.ApiKeyAuth;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1HTTPIngressPath;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1HTTPIngressRuleValue;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1Ingress;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressBackend;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressRule;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressSpec;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressTLS;
import io.kubernetes.client.openapi.models.V1ClusterRole;
import io.kubernetes.client.openapi.models.V1ClusterRoleBinding;
import io.kubernetes.client.openapi.models.V1ClusterRoleBindingList;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapVolumeSource;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarSource;
import io.kubernetes.client.openapi.models.V1ExecAction;
import io.kubernetes.client.openapi.models.V1HTTPGetAction;
import io.kubernetes.client.openapi.models.V1HTTPHeader;
import io.kubernetes.client.openapi.models.V1Handler;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1LabelSelectorRequirement;
import io.kubernetes.client.openapi.models.V1Lifecycle;
import io.kubernetes.client.openapi.models.V1ListMeta;
import io.kubernetes.client.openapi.models.V1LoadBalancerIngress;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1NetworkPolicy;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimSpec;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1PolicyRule;
import io.kubernetes.client.openapi.models.V1Probe;
import io.kubernetes.client.openapi.models.V1ReplicaSet;
import io.kubernetes.client.openapi.models.V1ReplicaSetBuilder;
import io.kubernetes.client.openapi.models.V1ReplicaSetSpec;
import io.kubernetes.client.openapi.models.V1ResourceAttributes;
import io.kubernetes.client.openapi.models.V1ResourceQuota;
import io.kubernetes.client.openapi.models.V1ResourceQuotaSpec;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1Role;
import io.kubernetes.client.openapi.models.V1RoleBinding;
import io.kubernetes.client.openapi.models.V1RoleBindingList;
import io.kubernetes.client.openapi.models.V1RoleRef;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretKeySelector;
import io.kubernetes.client.openapi.models.V1SecretVolumeSource;
import io.kubernetes.client.openapi.models.V1SelfSubjectAccessReview;
import io.kubernetes.client.openapi.models.V1SelfSubjectAccessReviewSpec;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.openapi.models.V1Subject;
import io.kubernetes.client.openapi.models.V1Toleration;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeDevice;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.kubernetes.client.util.Config;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.RegistryEvent;
import k8s.example.client.DataObject.TokenCR;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.DataObject.UserSecurityPolicyCR;
import k8s.example.client.Main;
import k8s.example.client.StringUtil;
import k8s.example.client.Util;
import k8s.example.client.interceptor.ChunkedInterceptor;
import k8s.example.client.k8s.apis.CustomResourceApi;
import k8s.example.client.k8s.util.SecurityHelper;
import k8s.example.client.models.BindingInDO;
import k8s.example.client.models.BindingOutDO;
import k8s.example.client.models.CommandExecOut;
import k8s.example.client.models.Cost;
import k8s.example.client.models.Endpoint;
import k8s.example.client.models.GetPlanDO;
import k8s.example.client.models.Image;
import k8s.example.client.models.ImageSpec;
import k8s.example.client.models.InputParametersSchema;
import k8s.example.client.models.Metadata;
import k8s.example.client.models.NamespaceClaim;
import k8s.example.client.models.NamespaceClaimList;
import k8s.example.client.models.PlanMetadata;
import k8s.example.client.models.ProvisionInDO;
import k8s.example.client.models.Registry;
import k8s.example.client.models.RegistryCondition;
import k8s.example.client.models.RegistryPVC;
import k8s.example.client.models.RegistryPVC.CreatePvc;
import k8s.example.client.models.RegistryService;
import k8s.example.client.models.RegistryStatus;
import k8s.example.client.models.RoleBindingClaim;
import k8s.example.client.models.Schemas;
import k8s.example.client.models.ServiceInstanceSchema;
import k8s.example.client.models.ServiceMetadata;
import k8s.example.client.models.ServiceOffering;
import k8s.example.client.models.ServicePlan;
import k8s.example.client.models.Services;
import k8s.example.client.models.TemplateInstance;
import k8s.example.client.models.TemplateInstanceSpec;
import k8s.example.client.models.TemplateInstanceSpecTemplate;
import k8s.example.client.models.TemplateParameter;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

public class K8sApiCaller {
	private static ApiClient k8sClient;
	private static CoreV1Api api;
	private static AppsV1Api appApi;
	private static RbacAuthorizationV1Api rbacApi;
	private static AuthorizationV1Api authApi;
	private static NetworkingV1Api netApi;
	private static CustomObjectsApi customObjectApi;
	private static CustomResourceApi templateApi;
	private static ExtensionsV1beta1Api extentionApi;
	private static ObjectMapper mapper = new ObjectMapper();
	private static Gson gson = new GsonBuilder().create();

	private static long time = System.currentTimeMillis();

	private static Logger logger = Main.logger;

	public static void initK8SClient() throws Exception {
		k8sClient = Config.fromCluster();
		//k8sClient.setHttpClient(getHttpClient()); // set network interceptors
		k8sClient.setConnectTimeout(0);
		k8sClient.setReadTimeout(0);
		k8sClient.setWriteTimeout(0);
		Configuration.setDefaultApiClient(k8sClient);

		api = new CoreV1Api();
		appApi = new AppsV1Api();
		rbacApi = new RbacAuthorizationV1Api();
		authApi = new AuthorizationV1Api();
		customObjectApi = new CustomObjectsApi();
		templateApi = new CustomResourceApi();
		extentionApi = new ExtensionsV1beta1Api();
		netApi = new NetworkingV1Api();
	}

	public static void startWatcher() throws Exception {
		
		try {
			// Validate registry. if registry spec is not qualified, patch the registry spec.
			patchRegistrySpec();
		} catch(IOException e) {
			logger.info("patchRegistrySpec Exception: " + e.getMessage());
		}
		
		try {
			// Init Registry Image
			initializeImageList();
		} catch(Exception e) {
			logger.info("initializeImageList Exception: " + e.getMessage());
		}
				
		// Update ResourceVersion
		logger.info("Update ResourceVersion");
		long registryLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, true);
		logger.info("Registry Latest resource version: " + registryLatestResourceVersion);
		long imageLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_IMAGE, true);
		logger.info("Image Latest resource version: " + imageLatestResourceVersion);
		long templateLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, true);
		logger.info("Template Latest resource version: " + templateLatestResourceVersion);
		long instanceLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE,
				true);
		logger.info("Instance Latest resource version: " + instanceLatestResourceVersion);
		long nscLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, false);
		logger.info("Namespace Claim Latest resource version: " + nscLatestResourceVersion);
		long rqcLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_RESOURCEQUOTACLAIM,
				true);
		logger.info("ResourceQuota Claim Latest resource version: " + rqcLatestResourceVersion);
		long rbcLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM, true);
		logger.info("RoleBinding Claim Latest resource version: " + rbcLatestResourceVersion);
		
		long cscLatestResourceVersion = getLatestResourceVersion(Constants.CUSTOM_OBJECT_PLURAL_CATALOGSERVICECLAIM, true);
		logger.info("Catalog Service Claim Latest resource version: " + cscLatestResourceVersion);
		
		long jfLatestResourceVersion = 0;
		logger.info("joinFed Latest resource version: " + jfLatestResourceVersion);

		long ccLatestResourceVersion = 0;
		logger.info("CapiCluster Latest resource version: " + ccLatestResourceVersion);

		// Start registry watch
		logger.info("Start registry watcher");
		RegistryWatcher registryWatcher = new RegistryWatcher(k8sClient, customObjectApi, null);
		registryWatcher.start();

		// Start registry replicaSet watch
		logger.info("Start registry replica set watcher");
		RegistryReplicaSetWatcher registryReplicaSetWatcher = new RegistryReplicaSetWatcher(k8sClient, appApi, null);
		registryReplicaSetWatcher.start();

		// Start registry pod watch
		logger.info("Start registry pod watcher");
		RegistryPodWatcher registryPodWatcher = new RegistryPodWatcher(k8sClient, api, null);
		registryPodWatcher.start();

		// Start registry service watch
		logger.info("Start registry service watcher");
		RegistryServiceWatcher registryServiceWatcher = new RegistryServiceWatcher(k8sClient, api, null);
		registryServiceWatcher.start();

		// Start registry cert secret watch
		logger.info("Start registry cert secret watcher");
		RegistryCertSecretWatcher registryCertSecretWatcher = new RegistryCertSecretWatcher(k8sClient, api, null);
		registryCertSecretWatcher.start();

		// Start registry docker secret watch
		logger.info("Start registry docker secret watcher");
		RegistryDockerSecretWatcher registryDockerSecretWatcher = new RegistryDockerSecretWatcher(k8sClient, api, null);
		registryDockerSecretWatcher.start();

		// Start registry tls secret watch
		logger.info("Start registry tls secret watcher");
		RegistryTlsSecretWatcher registryTlsSecretWatcher = new RegistryTlsSecretWatcher(k8sClient, api, null);
		registryTlsSecretWatcher.start();

		// Start registry ingress watch
		logger.info("Start registry ingress watcher");
		RegistryIngressWatcher registryIngressWatcher = new RegistryIngressWatcher(k8sClient, extentionApi, null);
		registryIngressWatcher.start();
		
		// Start registry pvc watch
		logger.info("Start registry pvc watcher");
		RegistryPvcWatcher registryPvcWatcher = new RegistryPvcWatcher(k8sClient, api, null);
		registryPvcWatcher.start();

		// Start registry config map watch
		logger.info("Start registry config map watcher");
		RegistryConfigMapWatcher registryConfigMapWatcher = new RegistryConfigMapWatcher(k8sClient, api, null);
		registryConfigMapWatcher.start();

		// Start image watch
		logger.info("Start image watcher");
		ImageWatcher imageWatcher = new ImageWatcher(k8sClient, customObjectApi, null);
		imageWatcher.start();

		// Start Operator
		logger.info("Start Template Operator");
		TemplateOperator templateOperator = new TemplateOperator(k8sClient, templateApi, 0);
		templateOperator.start();

		logger.info("Start Instance Operator");
		InstanceOperator instanceOperator = new InstanceOperator(k8sClient, templateApi, 0);
		instanceOperator.start();

		// Start NamespaceClaim Controller
		logger.info("Start NamespaceClaim Controller");
		NamespaceClaimController nscOperator = new NamespaceClaimController(k8sClient, customObjectApi, 0);
		nscOperator.start();

		// Start ResourceQuotaClaim Controller
		logger.info("Start ResourceQuotaClaim Controller");
		ResourceQuotaClaimController rqcOperator = new ResourceQuotaClaimController(k8sClient, customObjectApi, 0);
		rqcOperator.start();

		// Start RoleBindingClaim Controller
		logger.info("Start RoleBindingClaim Controller");
		RoleBindingClaimController rbcOperator = new RoleBindingClaimController(k8sClient, customObjectApi, 0);
		rbcOperator.start();
		
		// Start CatalogServiceClaim Controller
		logger.info("Start CatalogServiceClaim Controller");
		CatalogServiceClaimController cscOperator = new CatalogServiceClaimController(k8sClient, customObjectApi, 0);
		cscOperator.start();

		// start JoinFed Controller
		logger.info("Start JoinFed Controller");
		JoinFedController jfOperator = new JoinFedController(k8sClient, customObjectApi, api, 0);
		jfOperator.start();

		// start CapiCluster Controller
		logger.info("Start CapiCluster Controller");
		CapiClusterController ccOperator = new CapiClusterController(k8sClient, customObjectApi, api, 0);
		ccOperator.start();

		// start FederatedService Controller
		logger.info("Start FederatedService Controller");
		FederatedServiceController fsOperator = new FederatedServiceController(k8sClient, customObjectApi, 0);
		fsOperator.start();

		while (true) { // Infinity Loop for keep alive Main Thread
			try {
				if (!registryWatcher.isAlive()) {
					String registryLatestResourceVersionStr = RegistryWatcher.getLatestResourceVersion();
					logger.info("Registry watcher is not alive. Restart registry watcher! (Latest resource version: "
							+ registryLatestResourceVersionStr + ")");
					registryWatcher.interrupt();
					registryWatcher = new RegistryWatcher(k8sClient, customObjectApi, registryLatestResourceVersionStr);
					registryWatcher.start();
				}

				if (!registryReplicaSetWatcher.isAlive()) {
					String registryReplicaSetLatestResourceVersionStr = RegistryReplicaSetWatcher
							.getLatestResourceVersion();
					logger.info(
							"Registry replicaSet watcher is not alive. Restart registry replica set watcher! (Latest resource version: "
									+ registryReplicaSetLatestResourceVersionStr + ")");
					registryReplicaSetWatcher.interrupt();
					registryReplicaSetWatcher = new RegistryReplicaSetWatcher(k8sClient, appApi,
							registryReplicaSetLatestResourceVersionStr);
					registryReplicaSetWatcher.start();
				}

				if (!registryPodWatcher.isAlive()) {
					String registryPodLatestResourceVersionStr = RegistryPodWatcher.getLatestResourceVersion();
					logger.info(
							"Registry pod watcher is not alive. Restart registry pod watcher! (Latest resource version: "
									+ registryPodLatestResourceVersionStr + ")");
					registryPodWatcher.interrupt();
					registryPodWatcher = new RegistryPodWatcher(k8sClient, api, registryPodLatestResourceVersionStr);
					registryPodWatcher.start();
				}

				if (!registryServiceWatcher.isAlive()) {
					String registryServiceLatestResourceVersionStr = RegistryServiceWatcher.getLatestResourceVersion();
					logger.info(
							"Registry service watcher is not alive. Restart registry service watcher! (Latest resource version: "
									+ registryServiceLatestResourceVersionStr + ")");
					registryServiceWatcher.interrupt();
					registryServiceWatcher = new RegistryServiceWatcher(k8sClient, api,
							registryServiceLatestResourceVersionStr);
					registryServiceWatcher.start();
				}

				if (!registryCertSecretWatcher.isAlive()) {
					String registryCertSecretLatestResourceVersionStr = RegistryCertSecretWatcher
							.getLatestResourceVersion();
					logger.info(
							"Registry cert secret watcher is not alive. Restart registry cert secret watcher! (Latest resource version: "
									+ registryCertSecretLatestResourceVersionStr + ")");
					registryCertSecretWatcher.interrupt();
					registryCertSecretWatcher = new RegistryCertSecretWatcher(k8sClient, api,
							registryCertSecretLatestResourceVersionStr);
					registryCertSecretWatcher.start();
				}

				if (!registryDockerSecretWatcher.isAlive()) {
					String registryDockerSecretLatestResourceVersionStr = RegistryDockerSecretWatcher
							.getLatestResourceVersion();
					logger.info(
							"Registry docker secret watcher is not alive. Restart registry docker secret watcher! (Latest resource version: "
									+ registryDockerSecretLatestResourceVersionStr + ")");
					registryDockerSecretWatcher.interrupt();
					registryDockerSecretWatcher = new RegistryDockerSecretWatcher(k8sClient, api,
							registryDockerSecretLatestResourceVersionStr);
					registryDockerSecretWatcher.start();
				}
				
				if (!registryTlsSecretWatcher.isAlive()) {
					String registryTlsSecretLatestResourceVersionStr = RegistryTlsSecretWatcher
							.getLatestResourceVersion();
					logger.info(
							"Registry tls secret watcher is not alive. Restart registry tls secret watcher! (Latest resource version: "
									+ registryTlsSecretLatestResourceVersionStr + ")");
					registryTlsSecretWatcher.interrupt();
					registryTlsSecretWatcher = new RegistryTlsSecretWatcher(k8sClient, api,
							registryTlsSecretLatestResourceVersionStr);
					registryTlsSecretWatcher.start();
				}
				
				if (!registryIngressWatcher.isAlive()) {
					String registryIngressLatestResourceVersionStr = RegistryIngressWatcher
							.getLatestResourceVersion();
					logger.info(
							"Registry ingress watcher is not alive. Restart registry ingress watcher! (Latest resource version: "
									+ registryIngressLatestResourceVersionStr + ")");
					registryIngressWatcher.interrupt();
					registryIngressWatcher = new RegistryIngressWatcher(k8sClient, extentionApi,
							registryIngressLatestResourceVersionStr);
					registryIngressWatcher.start();
				}
				
				if (!registryPvcWatcher.isAlive()) {
					String registryPvcLatestResourceVersionStr = RegistryPvcWatcher
							.getLatestResourceVersion();
					logger.info(
							"Registry pvc watcher is not alive. Restart registry pvc watcher! (Latest resource version: "
									+ registryPvcLatestResourceVersionStr + ")");
					registryPvcWatcher.interrupt();
					registryPvcWatcher = new RegistryPvcWatcher(k8sClient, api,
							registryPvcLatestResourceVersionStr);
					registryPvcWatcher.start();
				}
				
				if (!registryConfigMapWatcher.isAlive()) {
					String registryConfigMapLatestResourceVersionStr = RegistryConfigMapWatcher
							.getLatestResourceVersion();
					logger.info(
							"Registry configmap watcher is not alive. Restart registry configmap watcher! (Latest resource version: "
									+ registryConfigMapLatestResourceVersionStr + ")");
					registryConfigMapWatcher.interrupt();
					registryConfigMapWatcher = new RegistryConfigMapWatcher(k8sClient, api,
							registryConfigMapLatestResourceVersionStr);
					registryConfigMapWatcher.start();
				}

				if (!imageWatcher.isAlive()) {
					String imageLatestResourceVersionStr = ImageWatcher.getLatestResourceVersion();
					logger.info("Image watcher is not alive. Restart image watcher! (Latest resource version: "
							+ imageLatestResourceVersionStr + ")");
					imageWatcher.interrupt();
					imageWatcher = new ImageWatcher(k8sClient, customObjectApi, imageLatestResourceVersionStr);
					imageWatcher.start();
				}

				if (!templateOperator.isAlive()) {
					templateLatestResourceVersion = TemplateOperator.getLatestResourceVersion();
					logger.info(("Template Operator is not Alive. Restart Operator! (Latest Resource Version: "
							+ templateLatestResourceVersion + ")"));
					templateOperator.interrupt();
					templateOperator = new TemplateOperator(k8sClient, templateApi, templateLatestResourceVersion);
					templateOperator.start();
				}

				if (!instanceOperator.isAlive()) {
					instanceLatestResourceVersion = InstanceOperator.getLatestResourceVersion();
					logger.info(("Instance Operator is not Alive. Restart Operator! (Latest Resource Version: "
							+ instanceLatestResourceVersion + ")"));
					instanceOperator.interrupt();
					instanceOperator = new InstanceOperator(k8sClient, templateApi, instanceLatestResourceVersion);
					instanceOperator.start();
				}

				if (!nscOperator.isAlive()) {
					nscLatestResourceVersion = NamespaceClaimController.getLatestResourceVersion();
					logger.info(
							("Namespace Claim Controller is not Alive. Restart Controller! (Latest Resource Version: "
									+ nscLatestResourceVersion + ")"));
					nscOperator.interrupt();
					nscOperator = new NamespaceClaimController(k8sClient, customObjectApi, nscLatestResourceVersion);
					nscOperator.start();
				}

				if (!rqcOperator.isAlive()) {
					rqcLatestResourceVersion = ResourceQuotaClaimController.getLatestResourceVersion();
					logger.info(
							("ResourceQuota Claim Controller is not Alive. Restart Controller! (Latest Resource Version: "
									+ rqcLatestResourceVersion + ")"));
					rqcOperator.interrupt();
					rqcOperator = new ResourceQuotaClaimController(k8sClient, customObjectApi,
							rqcLatestResourceVersion);
					rqcOperator.start();
				}

				if (!rbcOperator.isAlive()) {
					rbcLatestResourceVersion = RoleBindingClaimController.getLatestResourceVersion();
					logger.info(
							("RoleBinding Claim Controller is not Alive. Restart Controller! (Latest Resource Version: "
									+ rbcLatestResourceVersion + ")"));
					rbcOperator.interrupt();
					rbcOperator = new RoleBindingClaimController(k8sClient, customObjectApi, rbcLatestResourceVersion);
					rbcOperator.start();
				}
				
				if (!cscOperator.isAlive()) {
					cscLatestResourceVersion = CatalogServiceClaimController.getLatestResourceVersion();
					logger.info(
							("CatalogService Claim Controller is not Alive. Restart Controller! (Latest Resource Version: "
									+ cscLatestResourceVersion + ")"));
					cscOperator.interrupt();
					cscOperator = new CatalogServiceClaimController(k8sClient, customObjectApi, cscLatestResourceVersion);
					cscOperator.start();
				}

				if (!jfOperator.isAlive()) {
					jfLatestResourceVersion = JoinFedController.getLatestResourceVersion();
					logger.info(
							("JoinFed Controller is not Alive. Restart Controller! (Latest Resource Version: "
									+ jfLatestResourceVersion + ")"));
					jfOperator.interrupt();
					jfOperator = new JoinFedController(k8sClient, customObjectApi, api, jfLatestResourceVersion);
					jfOperator.start();
				}

				if (!ccOperator.isAlive()) {
					ccLatestResourceVersion = CapiClusterController.getLatestResourceVersion();
					logger.info(
							("CapiCluster Controller is not Alive. Restart Controller! (Latest Resource Version: "
									+ ccLatestResourceVersion + ")"));
					ccOperator.interrupt();
					ccOperator = new CapiClusterController(k8sClient, customObjectApi, api, ccLatestResourceVersion);
					ccOperator.start();
				}

				if (!fsOperator.isAlive()) {
					logger.info("FederatedService Controller is not Alive. Restart Controller! (Latest Resource Version: 0)");
					fsOperator.interrupt();
					fsOperator = new FederatedServiceController(k8sClient, customObjectApi, 0);
					fsOperator.start();
				}
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.info(sw.toString());
			}
			
			Thread.sleep(10000); // Period: 10 sec
		}
	}
	

	public static UserCR getUser(String userName) throws Exception {
		UserCR user = new UserCR();

		try {
			Object response = customObjectApi.getClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_USER, userName);
			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));
//
//            V1ObjectMeta metadata = new ObjectMapper().readValue((new Gson()).toJson(respJson.get("metadata")), V1ObjectMeta.class);
//
//            User userInfo = new ObjectMapper().readValue((new Gson()).toJson(respJson.get("userInfo")), User.class);
//            user.setMetadata(metadata);
//
//            user.setUserInfo(userInfo);
//            user.setStatus(respJson.get("status").toString());

			mapper.registerModule(new JodaModule());
			user = mapper.readValue((new Gson()).toJson(respJson), new TypeReference<UserCR>() {
			});
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return user;
	}

	public static NamespaceClaimList listNamespaceClaim( String labelSelector, String _continue ) throws Exception {
		NamespaceClaimList nscList = null;
		try {
			Object response = customObjectApi.listClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, null, _continue, null, labelSelector, null,
					null, null, Boolean.FALSE);

			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));
			logger.debug("NamespaceClaim List respJson: " + respJson);

			mapper.registerModule(new JodaModule());
			if (respJson != null)
				nscList = mapper.readValue((new Gson()).toJson(respJson),
						new TypeReference< NamespaceClaimList >() {
						});

		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return nscList;
	}

	
	public static void deleteUserSecurityPolicy (String uspName) throws Exception {
		try {
			V1DeleteOptions body = new V1DeleteOptions();

			customObjectApi.deleteClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_USER_SECURITY_POLICY, uspName, 0, null, null, body);
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public static void initRegistry(String registryId, Registry registry) throws ApiException {
		logger.debug("[K8S ApiCaller] initRegistry(String, Registry) Start");

		String registryName = registry.getMetadata().getName();
		String namespace = registry.getMetadata().getNamespace();
		String serviceType = registry.getSpec().getService().getIngress() != null ? 
				RegistryService.SVC_TYPE_INGRESS : RegistryService.SVC_TYPE_LOAD_BALANCER;
		JsonObject status = new JsonObject();
		JsonArray conditions = new JsonArray();
		JsonObject condition1 = new JsonObject();
		JsonObject condition2 = new JsonObject();
		JsonObject condition3 = new JsonObject();
		JsonObject condition4 = new JsonObject();
		JsonObject condition5 = new JsonObject();
		JsonObject condition6 = new JsonObject();
		JsonObject condition7 = new JsonObject();
		JsonObject condition8 = new JsonObject();
		JsonObject condition9 = new JsonObject();
		JsonObject condition10 = new JsonObject();
		JsonArray patchStatusArray = new JsonArray();
		DateTime curTime = new DateTime();
		
		condition1.addProperty("type", RegistryCondition.Condition.REPLICA_SET.getType());
		condition1.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition1);

		condition2.addProperty("type", RegistryCondition.Condition.POD.getType());
		condition2.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition2);

		condition3.addProperty("type", RegistryCondition.Condition.CONTAINER.getType());
		condition3.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition3);

		condition4.addProperty("type", RegistryCondition.Condition.SERVICE.getType());
		condition4.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition4);

		condition5.addProperty("type", RegistryCondition.Condition.SECRET_OPAQUE.getType());
		condition5.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition5);

		condition6.addProperty("type", RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON.getType());
		condition6.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition6);
		
		condition7.addProperty("type", RegistryCondition.Condition.SECRET_TLS.getType());
		if(serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
			condition7.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
		} else {
			condition7.addProperty("status", RegistryStatus.Status.UNUSED_FIELD.getStatus());
		}
		conditions.add(condition7);
		
		condition8.addProperty("type", RegistryCondition.Condition.INGRESS.getType());
		if(serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
			condition8.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
		} else {
			condition8.addProperty("status", RegistryStatus.Status.UNUSED_FIELD.getStatus());
		}
		conditions.add(condition8);

		condition9.addProperty("type", RegistryCondition.Condition.PVC.getType());
		condition9.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition9);
		
		condition10.addProperty("type", RegistryCondition.Condition.CONFIG_MAP.getType());
		condition10.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
		conditions.add(condition10);

		String phase = RegistryStatus.StatusPhase.CREATING.getStatus();
		String message = "Registry is creating. All resources in registry has not yet been created.";
		String reason = "RegistryNotCreated";
		
		status.add("conditions", conditions);
		status.addProperty("phase", phase);
		status.addProperty("message", message);
		status.addProperty("reason", reason);
		status.addProperty("phaseChangedAt", curTime.toString());
		status.addProperty("capacity", "0");

		patchStatusArray.add( Util.makePatchJsonObject("add", "/status", status) );

		try {
			customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
					registry.getMetadata().getName(), patchStatusArray);
			
			logger.debug("CRD Status is patched: " + namespace + "/" + registryName);
			logger.debug("\tphase(" + phase + ")");
			logger.debug("\tmessage(" + message + ")");
			logger.debug("\treason(" + reason + ")\n");
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}
	
	public static void createRegistry(Registry registry) throws Exception {
		logger.debug("[K8S ApiCaller] createRegistry(Registry) Start");

		try {
			String serviceType = registry.getSpec().getService().getIngress() != null 
				? RegistryService.SVC_TYPE_INGRESS : RegistryService.SVC_TYPE_LOAD_BALANCER;

			createRegistryService(registry);
			createRegistryPvc(registry);
			createRegistryCertSecret(registry);
			createRegistryDcjSecret(registry);
			createRegistryConfigMap(registry);
			createRegistryReplicaSet(registry);
			
			if(serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
				createRegistryTlsSecret(registry);
				createRegistryIngress(registry);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}

	}
	
	public static void createRegistryService(Registry registry) throws Exception {
		logger.debug("[K8S ApiCaller] createRegistryService(Registry) Start");
		try {
			String registryName = registry.getMetadata().getName();
			String namespace = registry.getMetadata().getNamespace();
			RegistryService registryService = registry.getSpec().getService();
			String clusterIP = null;
			String lbIP = null;
			String serviceType 
			= registry.getSpec().getService().getIngress() != null 
			? RegistryService.SVC_TYPE_INGRESS : RegistryService.SVC_TYPE_LOAD_BALANCER;

			logger.info(namespace + "/" + registryName + " registry's service is creating...");
			
			// set default
			int registrySVCTargetPort = RegistryService.REGISTRY_TARGET_PORT;
			int registrySVCPort = registrySVCTargetPort;
			logger.debug("registrySVCPort: " + registrySVCPort);

			if( serviceType.equals(RegistryService.SVC_TYPE_INGRESS) ) {
				if( registryService.getIngress().getPort() != 0 ) {
					registrySVCPort = registryService.getIngress().getPort();
					logger.debug("[Ingress]registrySVCPort: " + registrySVCPort);
				}
			}
			else if( serviceType.equals(RegistryService.SVC_TYPE_LOAD_BALANCER) 
					&& registryService.getLoadBalancer().getPort() != 0) {
				registrySVCPort = registryService.getLoadBalancer().getPort();
				logger.debug("[LB]registrySVCPort: " + registrySVCPort);
			}

			// ----- Create Service
			V1Service lb = new V1Service();
			V1ObjectMeta lbMeta = new V1ObjectMeta();
			V1ServiceSpec lbSpec = new V1ServiceSpec();
			List<V1ServicePort> ports = new ArrayList<>();
			Map<String, String> lbSelector = new HashMap<String, String>();

			lbMeta.setName(Constants.K8S_PREFIX + registryName);

			logger.debug("<Service Label List>");
			Map<String, String> serviceLabels = new HashMap<String, String>();
			serviceLabels.put("app", "registry");
			serviceLabels.put("apps", lbMeta.getName());
			logger.debug("app: registry");
			logger.debug("apps: " + lbMeta.getName());
			lbMeta.setLabels(serviceLabels);

			List<V1OwnerReference> ownerRefs = new ArrayList<>();
			V1OwnerReference ownerRef = new V1OwnerReference();

			ownerRef.setApiVersion(registry.getApiVersion());
			ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
			ownerRef.setController(Boolean.TRUE);
			ownerRef.setKind(registry.getKind());
			ownerRef.setName(registry.getMetadata().getName());
			ownerRef.setUid(registry.getMetadata().getUid());
			ownerRefs.add(ownerRef);

			lbMeta.setOwnerReferences(ownerRefs);

			lb.setMetadata(lbMeta);

			V1ServicePort v1port = new V1ServicePort();
			v1port.setProtocol(RegistryService.REGISTRY_PORT_PROTOCOL);
			v1port.setPort(registrySVCPort);
			v1port.setName(RegistryService.REGISTRY_PORT_NAME);
			v1port.setTargetPort(new IntOrString(registrySVCTargetPort));

			ports.add(v1port);
			lbSpec.setPorts(ports);

			logger.debug("Selector: " + Constants.K8S_PREFIX + registryName + "=lb");
			lbSelector.put(Constants.K8S_PREFIX + registryName, "lb");
			lbSpec.setSelector(lbSelector);

			if(serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
				lbSpec.setType(RegistryService.SVC_TYPE_CLUSTER_IP);
			}
			else if(serviceType.equals(RegistryService.SVC_TYPE_LOAD_BALANCER)) {
				lbSpec.setType(RegistryService.SVC_TYPE_LOAD_BALANCER);
			}
			else {
				logger.debug("Service Type Is Invalid");
				throw new Exception("Service Type Is Invalid");
			}

			lb.setSpec(lbSpec);

			try {
				V1Service result = api.createNamespacedService(namespace, lb, null, null, null);
				logger.info(result.getMetadata().getNamespace() +"/"+result.getMetadata().getName() + " service created!!");
			} catch (ApiException e) {
				logger.error(e.getResponseBody());

				try {
					patchRegistryStatus(registry, RegistryCondition.Condition.SERVICE, 
							RegistryStatus.Status.FALSE.getStatus(), e.getResponseBody(), "CreateServiceFailed");
				} catch (ApiException e2) {
					logger.error(e2.getResponseBody());
					throw e2;
				}

				throw e;
			}

			int RETRY_CNT = 200;
			V1Service service = null;
			for (int i = 0; i < RETRY_CNT; i++) {
				Thread.sleep(500);
				service = api.readNamespacedService(Constants.K8S_PREFIX + registryName, namespace, null, null, null);

				clusterIP = service.getSpec().getClusterIP();
				logger.debug("[ClusterIP]:" + clusterIP);

				if (service.getSpec().getType().equals(RegistryService.SVC_TYPE_LOAD_BALANCER)
						&& service.getStatus().getLoadBalancer().getIngress() != null
						&& service.getStatus().getLoadBalancer().getIngress().size() == 1) {
					if (service.getStatus().getLoadBalancer().getIngress().get(0).getHostname() == null) {
						lbIP = service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
					} else {
						lbIP = service.getStatus().getLoadBalancer().getIngress().get(0).getHostname();
					}
					logger.debug("[LoadBalancerIP]:" + lbIP);
					break;
				}
				else if (service.getSpec().getType().equals(RegistryService.SVC_TYPE_CLUSTER_IP)) {
					logger.debug("Service type is ClusterIp");
					break;
				}

				if (i == RETRY_CNT - 1) {
					try {
						patchRegistryStatus(registry, RegistryCondition.Condition.SERVICE, 
								RegistryStatus.Status.FALSE.getStatus(), "Creating a registry is failed. Service(LB) is not found", "ServiceNotFound");
					} catch (ApiException e) {
						logger.error(e.getResponseBody());
						throw e;
					}

					return;
				}
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			throw e;
		}
	}

	public static void createRegistryPvc(Registry registry) throws Exception {
		logger.debug("[K8S ApiCaller] createRegistryPvc(Registry) Start");
		try {
			String registryName = registry.getMetadata().getName();
			String namespace = registry.getMetadata().getNamespace();
			RegistryPVC registryPVC = registry.getSpec().getPersistentVolumeClaim();
			boolean existPvcName = (registryPVC.getExist() != null);

			logger.info(namespace + "/" + registryName + " registry's pvc is creating...");
			if( existPvcName ) {
				V1PersistentVolumeClaim existPvc = null;
				try {
					existPvc = api.readNamespacedPersistentVolumeClaim(registryPVC.getExist().getPvcName(), namespace, null, null, null);
				}catch (ApiException e) {
					try {
						patchRegistryStatus(registry, RegistryCondition.Condition.PVC, 
								RegistryStatus.Status.FALSE.getStatus(), "Creating a registry is failed. " + registryPVC.getExist().getPvcName() + " PVC is not found", "PVCNotFound");
					} catch (ApiException e2) {
						logger.error(e2.getResponseBody());
						throw e2;
					}
					logger.error(e.getResponseBody());
					throw e;
				}

				JsonArray jArrayPatchPvc = new JsonArray();
				Map<String, String> labelsMap = null;

				// labels field is not exist
				if( (labelsMap = existPvc.getMetadata().getLabels()) == null ) {
					JsonArray labels = new JsonArray();
					JsonObject label1 = new JsonObject();
					JsonObject label2 = new JsonObject();
					JsonObject label3 = new JsonObject();

					label1.addProperty("registryUid", registry.getMetadata().getUid());
					labels.add(label1);

					label2.addProperty("app", "registry");
					labels.add(label2);

					label3.addProperty("apps", registry.getMetadata().getName());
					labels.add(label3);

					jArrayPatchPvc.add(Util.makePatchJsonObject("add", "/metadata", labels));
				}
				else {	// labels field is exist
					JsonObject label = new JsonObject();

					labelsMap.remove("registryUid");
					labelsMap.remove("app");
					labelsMap.remove("apps");

					for(String key : labelsMap.keySet()) {
						label.addProperty(key, labelsMap.get(key));
					}

					label.addProperty("registryUid", registry.getMetadata().getUid());
					label.addProperty("app", "registry");
					label.addProperty("apps", registry.getMetadata().getName());

					jArrayPatchPvc.add(Util.makePatchJsonObject("add", "/metadata/labels", label));
				}

				try {
					V1PersistentVolumeClaim  result = api.patchNamespacedPersistentVolumeClaim(existPvc.getMetadata().getName(), namespace, new V1Patch(jArrayPatchPvc.toString()), null, null, null, null);

					logger.info(result.getMetadata().getNamespace() + "/" + result.getMetadata().getName() + " pvc patched!!");
					logger.debug("\tmetadata:" + result.getMetadata().toString());
				} catch (ApiException e) {
					logger.error(e.getResponseBody());
				}

			} else {
				// ----- Create PVC
				CreatePvc createPvc = registryPVC.getCreate();
				V1PersistentVolumeClaim pvc = new V1PersistentVolumeClaim();
				V1ObjectMeta pvcMeta = new V1ObjectMeta();
				V1PersistentVolumeClaimSpec pvcSpec = new V1PersistentVolumeClaimSpec();
				V1ResourceRequirements pvcResource = new V1ResourceRequirements();
				Map<String, Quantity> limit = new HashMap<>();
				List<String> accessModes = new ArrayList<>();

				//			String storageClassName = StringUtil.isEmpty(registryPVC.getStorageClassName()) ? RegistryPVC.STORAGE_CLASS_DEFAULT : registryPVC.getStorageClassName();
				String storageClassName = createPvc.getStorageClassName();

				pvcMeta.setName(Constants.K8S_PREFIX + registryName);

				logger.debug("<Pvc Label List>");
				Map<String, String> pvcLabels = new HashMap<String, String>();
				pvcLabels.put("app", "registry");
				pvcLabels.put("apps", registry.getMetadata().getName());
				pvcLabels.put("registryUid", registry.getMetadata().getUid());
				logger.debug("app: registry");
				logger.debug("apps: " + registry.getMetadata().getName());
				logger.debug("registryUid: " + registry.getMetadata().getUid());
				pvcMeta.setLabels(pvcLabels);

				boolean deleteWitchPvc = (createPvc.getDeleteWithPvc() == null ? false : createPvc.getDeleteWithPvc());
				if( deleteWitchPvc ) {
					List<V1OwnerReference> ownerRefs = new ArrayList<>();
					V1OwnerReference ownerRef = new V1OwnerReference();

					ownerRef.setApiVersion(registry.getApiVersion());
					ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
					ownerRef.setController(Boolean.TRUE);
					ownerRef.setKind(registry.getKind());
					ownerRef.setName(registry.getMetadata().getName());
					ownerRef.setUid(registry.getMetadata().getUid());
					ownerRefs.add(ownerRef);

					pvcMeta.setOwnerReferences(ownerRefs);
				}

				// set storage quota.
				limit.put("storage", new Quantity(createPvc.getStorageSize()));
				pvcResource.setRequests(limit);
				pvcSpec.setResources(pvcResource);

				// set storage class name
				pvcSpec.setStorageClassName(storageClassName);

				for (String mode : createPvc.getAccessModes()) {
					accessModes.add(mode);
				}
				//			}
				pvcSpec.setAccessModes(accessModes);

				// set volume mode
				if (createPvc.getVolumeMode() != null) 
					pvcSpec.setVolumeMode(createPvc.getVolumeMode());

				pvc.setMetadata(pvcMeta);
				pvc.setSpec(pvcSpec);

				// create storage.
				try {
					V1PersistentVolumeClaim result = api.createNamespacedPersistentVolumeClaim(namespace, pvc, null, null, null);
					logger.info(result.getMetadata().getNamespace() +"/"+result.getMetadata().getName() + " pvc created!!");
				} catch (ApiException e) {
					logger.error(e.getResponseBody());

					try {
						patchRegistryStatus(registry, RegistryCondition.Condition.PVC, 
								RegistryStatus.Status.FALSE.getStatus(), e.getResponseBody(), "CreatePVCFailed");
					} catch (ApiException e2) {
						logger.error(e2.getResponseBody());
						logger.error("resCode: " + e2.getCode());
						throw e2;
					}

					if(e.getCode() != 409) // 409: Already exist
						throw e;
				}
			}
		}catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			throw e;
		}
	}

	public static void createRegistryCertSecret(Registry registry) throws Exception {
		logger.debug("[K8S ApiCaller] createRegistryCertSecret(Registry) Start");
		try {
			String registryName = registry.getMetadata().getName();
			String namespace = registry.getMetadata().getNamespace();
			RegistryService registryService = registry.getSpec().getService();
			String clusterIP = null;
			String lbIP = null;
			String ingressDomain = null;
			String registryDomain = null;
			String serviceType 
			= registry.getSpec().getService().getIngress() != null 
			? RegistryService.SVC_TYPE_INGRESS : RegistryService.SVC_TYPE_LOAD_BALANCER;

			logger.info(namespace + "/" + registryName + " registry's cert secret is creating...");

			// set default
			int registrySVCTargetPort = RegistryService.REGISTRY_TARGET_PORT;
			int registrySVCPort = registrySVCTargetPort;
			logger.debug("registrySVCPort: " + registrySVCPort);

			if( serviceType.equals(RegistryService.SVC_TYPE_INGRESS) ) {
				ingressDomain = registryService.getIngress().getDomainName();

				if( registryService.getIngress().getPort() != 0 ) {
					registrySVCPort = registryService.getIngress().getPort();
					logger.debug("[Ingress]registrySVCPort: " + registrySVCPort);
				}
			}
			else if( serviceType.equals(RegistryService.SVC_TYPE_LOAD_BALANCER) 
					&& registryService.getLoadBalancer().getPort() != 0) {
				registrySVCPort = registryService.getLoadBalancer().getPort();
				logger.debug("[LB]registrySVCPort: " + registrySVCPort);
			}

			if(serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {


				registryDomain = registryName + "." + ingressDomain;
				logger.debug("[registryDomain]:" + registryDomain);
			}

			List<V1OwnerReference> ownerRefs = new ArrayList<>();
			V1OwnerReference ownerRef = new V1OwnerReference();

			ownerRef.setApiVersion(registry.getApiVersion());
			ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
			ownerRef.setController(Boolean.TRUE);
			ownerRef.setKind(registry.getKind());
			ownerRef.setName(registry.getMetadata().getName());
			ownerRef.setUid(registry.getMetadata().getUid());
			ownerRefs.add(ownerRef);

			// Create Cert Directory
			logger.debug("Create Cert Directory");
			String registryDir = createDirectory(namespace, registryName);

			int RETRY_CNT = 200;
			V1Service service = null;
			for (int i = 0; i < RETRY_CNT; i++) {
				Thread.sleep(500);
				service = api.readNamespacedService(Constants.K8S_PREFIX + registryName, namespace, null, null, null);

				clusterIP = service.getSpec().getClusterIP();
				logger.debug("[ClusterIP]:" + clusterIP);

				if (service.getSpec().getType().equals(RegistryService.SVC_TYPE_LOAD_BALANCER)  
						&& service.getStatus().getLoadBalancer().getIngress() != null
						&& service.getStatus().getLoadBalancer().getIngress().size() == 1) {
					if (service.getStatus().getLoadBalancer().getIngress().get(0).getHostname() == null) {
						lbIP = service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
					} else {
						lbIP = service.getStatus().getLoadBalancer().getIngress().get(0).getHostname();
					}
					logger.debug("[LoadBalancerIP]:" + lbIP);
					break;
				} else if (service.getSpec().getType().equals(RegistryService.SVC_TYPE_CLUSTER_IP)) {
					logger.debug("Service type is ClusterIp");
					break;
				}

				if (i == RETRY_CNT - 1) {
					try {
						patchRegistryStatus(registry, RegistryCondition.Condition.SERVICE, 
								RegistryStatus.Status.FALSE.getStatus(), "Creating a registry is failed. Service(LB) is not found", "ServiceNotFound");
					} catch (ApiException e) {
						logger.error(e.getResponseBody());
						throw e;
					}

					return;
				}
			}
			
			// Create Certificates
			logger.debug("Create Certificates");
			List<String> commands = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			sb.append("openssl req -newkey rsa:4096 -nodes -sha256 -keyout ");
			sb.append(registryDir + "/" + Constants.CERT_KEY_FILE);
			sb.append(" -x509 -days 1000 -subj \"/C=KR/ST=Seoul/O=tmax/CN=");
			sb.append(clusterIP);
			sb.append("\" -config <(cat /etc/ssl/openssl.cnf <(printf \"[v3_ca]\\nsubjectAltName=IP:" + clusterIP);
			if(serviceType.equals(RegistryService.SVC_TYPE_LOAD_BALANCER)) {
				sb.append(",IP:" + lbIP);
			} else {
				sb.append(",DNS:" + registryDomain);
			}
			sb.append("\")) -out ");
			sb.append(registryDir + "/" + Constants.CERT_CRT_FILE);
			commands.clear();
			commands.add("/bin/bash"); // bash Required!!
			commands.add("-c");
			commands.add(sb.toString());

			try {
				commandExecute(commands.toArray(new String[commands.size()]));
			} catch (Exception e) {
				logger.error(e.getMessage());

				try {
					patchRegistryStatus(registry, RegistryCondition.Condition.SECRET_OPAQUE, 
							RegistryStatus.Status.FALSE.getStatus(), e.getMessage(), "CreateRegistryFailed");
				} catch (ApiException e2) {
					logger.error(e2.getResponseBody());
					throw e2;
				}

				throw e;
			}

			sb.setLength(0);
			sb.append("\'\'openssl x509 -inform PEM -in ");
			sb.append(registryDir + "/" + Constants.CERT_CRT_FILE);
			sb.append(" -out ");
			sb.append(registryDir + "/" + Constants.CERT_CERT_FILE);
			sb.append("\'\'");
			commands.clear();
			commands.add("/bin/sh");
			commands.add("-c");
			commands.add(sb.toString());
			try {
				commandExecute(commands.toArray(new String[commands.size()]));
			} catch (Exception e) {
				try {
					patchRegistryStatus(registry, RegistryCondition.Condition.SECRET_OPAQUE, 
							RegistryStatus.Status.FALSE.getStatus(), e.getMessage(), "CreateRegistryFailed");
				} catch (ApiException e2) {
					logger.error(e2.getResponseBody());
					throw e2;
				}

				throw e;
			}

			// Read cert files & Create Secret Object
			logger.debug("Read cert files & Create Secret Object"); 
			Map<String, String> secrets = new HashMap<>();

			secrets.put(Constants.CERT_KEY_FILE, readFile(registryDir + "/" + Constants.CERT_KEY_FILE));
			secrets.put(Constants.CERT_CRT_FILE, readFile(registryDir + "/" + Constants.CERT_CRT_FILE));
			secrets.put(Constants.CERT_CERT_FILE, readFile(registryDir + "/" + Constants.CERT_CERT_FILE));
			secrets.put("ID", registry.getSpec().getLoginId());
			secrets.put("PASSWD", registry.getSpec().getLoginPassword());
			secrets.put("CLUSTER_IP", clusterIP);

			if( lbIP != null ) 
				secrets.put("LB_IP", lbIP);

			if( registryDomain != null )
				secrets.put("DOMAIN_NAME", registryDomain);

			if( registryDomain != null) {
				secrets.put("REGISTRY_URL", registryDomain + ":" + registrySVCPort);
			} else if(lbIP != null) {
				secrets.put("REGISTRY_URL", lbIP + ":" + registrySVCPort);
			} else {
				secrets.put("REGISTRY_URL", clusterIP + ":" + registrySVCPort);
			}

			secrets.put("PORT", Integer.toString(registrySVCPort));

			Map<String, String> labels = new HashMap<>();
			labels.put("secret", "cert");

			try {
				// K8SApiCall createSecret
				logger.debug("K8SApiCall createSecret");
				String secretName = K8sApiCaller.createSecret(namespace, secrets, registryName, labels, null, ownerRefs);
				logger.info(namespace +"/"+ secretName + " cert secret created!!");
			} catch (ApiException e) {
				try {
					patchRegistryStatus(registry, RegistryCondition.Condition.SECRET_OPAQUE, 
							RegistryStatus.Status.FALSE.getStatus(), e.getResponseBody(), "CreateOpaqueSecretFailed");
				} catch (ApiException e2) {
					logger.error(e2.getResponseBody());
					throw e2;
				}

				throw e;
			}
		}catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			throw e;
		}
	}
	
	public static void createRegistryDcjSecret(Registry registry) throws Exception {
		try {
			// Create docker-config-json Secret Object
			String registryName = registry.getMetadata().getName();
			String namespace = registry.getMetadata().getNamespace();
			RegistryService registryService = registry.getSpec().getService();
			String clusterIP = null;
			String lbIP = null;
			String ingressDomain = null;
			String registryDomain = null;
			String serviceType 
			= registry.getSpec().getService().getIngress() != null 
			? RegistryService.SVC_TYPE_INGRESS : RegistryService.SVC_TYPE_LOAD_BALANCER;

			logger.info(namespace + "/" + registryName + "registry docker-config-json secret is creating...");

			// set default
			int registrySVCTargetPort = RegistryService.REGISTRY_TARGET_PORT;
			int registrySVCPort = registrySVCTargetPort;
			logger.debug("registrySVCPort: " + registrySVCPort);

			if( serviceType.equals(RegistryService.SVC_TYPE_INGRESS) ) {
				ingressDomain = registryService.getIngress().getDomainName();

				if( registryService.getIngress().getPort() != 0 ) {
					registrySVCPort = registryService.getIngress().getPort();
					logger.debug("[Ingress]registrySVCPort: " + registrySVCPort);
				}
			} else if( serviceType.equals(RegistryService.SVC_TYPE_LOAD_BALANCER) 
					&& registryService.getLoadBalancer().getPort() != 0) {
				registrySVCPort = registryService.getLoadBalancer().getPort();
				logger.debug("[LB]registrySVCPort: " + registrySVCPort);
			}

			if(serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
				registryDomain = registryName + "." + ingressDomain;
				logger.debug("[registryDomain]:" + registryDomain);
			}

			List<V1OwnerReference> ownerRefs = new ArrayList<>();
			V1OwnerReference ownerRef = new V1OwnerReference();

			ownerRef.setApiVersion(registry.getApiVersion());
			ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
			ownerRef.setController(Boolean.TRUE);
			ownerRef.setKind(registry.getKind());
			ownerRef.setName(registry.getMetadata().getName());
			ownerRef.setUid(registry.getMetadata().getUid());
			ownerRefs.add(ownerRef);

			int RETRY_CNT = 200;
			V1Service service = null;
			for (int i = 0; i < RETRY_CNT; i++) {
				Thread.sleep(500);
				service = api.readNamespacedService(Constants.K8S_PREFIX + registryName, namespace, null, null, null);

				clusterIP = service.getSpec().getClusterIP();
				logger.debug("[ClusterIP]:" + clusterIP);

				if (service.getSpec().getType().equals(RegistryService.SVC_TYPE_LOAD_BALANCER)  
						&& service.getStatus().getLoadBalancer().getIngress() != null
						&& service.getStatus().getLoadBalancer().getIngress().size() == 1) {
					if (service.getStatus().getLoadBalancer().getIngress().get(0).getHostname() == null) {
						lbIP = service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
					} else {
						lbIP = service.getStatus().getLoadBalancer().getIngress().get(0).getHostname();
					}
					logger.debug("[LoadBalancerIP]:" + lbIP);
					break;
				}
				else if (service.getSpec().getType().equals(RegistryService.SVC_TYPE_CLUSTER_IP)) {
					logger.debug("Service type is ClusterIp");
					break;
				}

				if (i == RETRY_CNT - 1) {
					try {
						patchRegistryStatus(registry, RegistryCondition.Condition.SERVICE, 
								RegistryStatus.Status.FALSE.getStatus(), "Creating a registry is failed. Service(LB) is not found", "ServiceNotFound");
					} catch (ApiException e) {
						logger.error(e.getResponseBody());
						throw e;
					}

					return;
				}
			}
			
			Map<String, String> secrets2 = new HashMap<>();
			List<String> domainList = new ArrayList<>();

			domainList.add(clusterIP + ":" + registrySVCPort);
			if(lbIP != null)
				domainList.add(lbIP + ":" + registrySVCPort);
			if(registryDomain != null)
				domainList.add(registryDomain + ":" + registrySVCPort);

			secrets2.put(Constants.DOCKER_CONFIG_JSON_FILE, createConfigJson(domainList,
					registry.getSpec().getLoginId(), registry.getSpec().getLoginPassword()));

			Map<String, String> labels2 = new HashMap<>();
			labels2.put("secret", "docker");
			try {
				String secretName = K8sApiCaller.createSecret(namespace, secrets2, registryName, labels2,
						Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON, ownerRefs);
				logger.info(namespace +"/"+ secretName + " docker-config-json secret created!!");
			} catch (ApiException e) {
				try {
					patchRegistryStatus(registry, RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON, 
							RegistryStatus.Status.FALSE.getStatus(), e.getResponseBody(), "CreateDockerConfigJsonSecretFailed");
				} catch (ApiException e2) {
					logger.error(e2.getResponseBody());
					throw e2;
				}

				throw e;
			}

		}catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			throw e;
		}
	}

	public static void createRegistryTlsSecret(Registry registry) throws Exception {
		logger.debug("[K8S ApiCaller] createRegistryTlsSecret(Registry) Start");
		try {
			String registryName = registry.getMetadata().getName();
			String namespace = registry.getMetadata().getNamespace();
			Map<String, String> tlsLabels = new HashMap<>();
			Map<String, String> tlsSecrets = new HashMap<>();
			String registryDir = Constants.OPENSSL_HOME_DIR + "/" + namespace + "/" + registryName;

			logger.info(namespace + "/" + registryName + " registry's tls secret is creating...");
			
			int MAX_RETRY_CNT=30;
			int tryCount = 0;
			
			for(; tryCount<MAX_RETRY_CNT; tryCount++) {
				try {
					tlsSecrets.put(Constants.K8S_SECRET_TLS_CRT, readFile(registryDir + "/" + Constants.CERT_CRT_FILE));
					tlsSecrets.put(Constants.K8S_SECRET_TLS_KEY, readFile(registryDir + "/" + Constants.CERT_KEY_FILE));
					break;
				} catch(FileNotFoundException e) {
					logger.debug("[retry " + (tryCount+1) + "/" + MAX_RETRY_CNT + "]" + e.getMessage());
					Thread.sleep(1000);
				}
			}
			
			if (tryCount == MAX_RETRY_CNT) {
				try {
					patchRegistryStatus(registry, RegistryCondition.Condition.SECRET_TLS, 
							RegistryStatus.Status.FALSE.getStatus(), "Can't read cert files.", "CreateTlsSecretFailed");
				} catch (ApiException e2) {
					logger.error(e2.getResponseBody());
					throw e2;
				}
				throw new Exception("Can't read cert files.");
			}
			tlsLabels.put("secret", "tls");
			
			List<V1OwnerReference> ownerRefs = new ArrayList<>();
			V1OwnerReference ownerRef = new V1OwnerReference();

			ownerRef.setApiVersion(registry.getApiVersion());
			ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
			ownerRef.setController(Boolean.TRUE);
			ownerRef.setKind(registry.getKind());
			ownerRef.setName(registry.getMetadata().getName());
			ownerRef.setUid(registry.getMetadata().getUid());
			ownerRefs.add(ownerRef);
			
			try {
				// K8SApiCall createSecret
				logger.debug("K8SApiCall createSecret");
				String secretName = K8sApiCaller.createSecret(namespace, tlsSecrets, registryName, tlsLabels, Constants.K8S_SECRET_TYPE_TLS, ownerRefs);
				logger.info(namespace +"/"+ secretName + " tls secret created!!");
			} catch (ApiException e) {
				try {
					patchRegistryStatus(registry, RegistryCondition.Condition.SECRET_TLS, 
							RegistryStatus.Status.FALSE.getStatus(), e.getResponseBody(), "CreateTlsSecretFailed");
				} catch (ApiException e2) {
					logger.error(e2.getResponseBody());
					throw e2;
				}

				throw e;
			}
		}catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			throw e;
		}
	}
	
	public static void createRegistryIngress(Registry registry) throws Exception {
		logger.debug("[K8S ApiCaller] createRegistryIngress(Registry) Start");
		try {
			String registryName = registry.getMetadata().getName();
			String namespace = registry.getMetadata().getNamespace();
			RegistryService registryService = registry.getSpec().getService();
			int registrySVCTargetPort = RegistryService.REGISTRY_TARGET_PORT;
			int registrySVCPort = registrySVCTargetPort;
			ExtensionsV1beta1Ingress ingress = new ExtensionsV1beta1Ingress();
			V1ObjectMeta metadata = new V1ObjectMeta();
			Map<String, String> annotations = new HashMap<>();

			logger.info(namespace + "/" + registryName + " registry's ingress is creating...");

			String ingressDomain = null;
			String registryDomain = null;

			ingressDomain = registryService.getIngress().getDomainName();
			registryDomain = registryName + "." + ingressDomain;
			logger.debug("[registryDomain]:" + registryDomain);

			if( registryService.getIngress().getPort() != 0 ) {
				registrySVCPort = registryService.getIngress().getPort();
				logger.debug("[Ingress]registrySVCPort: " + registrySVCPort);
			}

			metadata.setName(Constants.K8S_PREFIX + registryName);
			metadata.setNamespace(namespace);
			
			Map<String, String> ingressLabels = new HashMap<String, String>();
			ingressLabels.put("app", "registry");
			ingressLabels.put("apps", metadata.getName());
			logger.debug("app: registry");
			logger.debug("apps: " + metadata.getName());
			metadata.setLabels(ingressLabels);
			
//			if(registryService.getIngress().getIngressClass() != null) {
//				annotations.put("kubernetes.io/ingress.class", registryService.getIngress().getIngressClass()); 
//			} else {
//				try {
//					patchRegistryStatus(registry, RegistryCondition.Condition.INGRESS, 
//							RegistryStatus.Status.FALSE.getStatus(), "ingressClass field is null", "CreateIngressFailed");
//				} catch (ApiException e2) {
//					logger.error(e2.getResponseBody());
//					throw e2;
//				}
//				throw new Exception("ingressClass field is null");
//			}
			
			annotations.put("kubernetes.io/ingress.class", RegistryService.REGISTRY_DEFAULT_INGRESS_CLASS);
			annotations.put("nginx.ingress.kubernetes.io/proxy-connect-timeout", "3600");
			annotations.put("nginx.ingress.kubernetes.io/proxy-read-timeout", "3600");
			annotations.put("nginx.ingress.kubernetes.io/ssl-redirect", "true");
			annotations.put("nginx.ingress.kubernetes.io/backend-protocol", "HTTPS");
			annotations.put("nginx.ingress.kubernetes.io/proxy-body-size", "0");
			metadata.setAnnotations(annotations);
			
			List<V1OwnerReference> ownerRefs = new ArrayList<>();
			V1OwnerReference ownerRef = new V1OwnerReference();

			ownerRef.setApiVersion(registry.getApiVersion());
			ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
			ownerRef.setController(Boolean.TRUE);
			ownerRef.setKind(registry.getKind());
			ownerRef.setName(registry.getMetadata().getName());
			ownerRef.setUid(registry.getMetadata().getUid());
			ownerRefs.add(ownerRef);
			
			metadata.setOwnerReferences(ownerRefs);
			ingress.setMetadata(metadata);

			ExtensionsV1beta1IngressSpec spec = new ExtensionsV1beta1IngressSpec();
			List<ExtensionsV1beta1IngressTLS> tls = new ArrayList<>();
			ExtensionsV1beta1IngressTLS eTls = new ExtensionsV1beta1IngressTLS();
			String hostsItem = registryDomain;
			eTls.addHostsItem(hostsItem);
			eTls.setSecretName(Constants.K8S_PREFIX + Constants.K8S_TLS_PREFIX + registryName);

			tls.add(eTls);
			spec.setTls(tls);
			
			List<ExtensionsV1beta1IngressRule> rules = new ArrayList<>();
			ExtensionsV1beta1IngressRule eRule = new ExtensionsV1beta1IngressRule();
			ExtensionsV1beta1HTTPIngressRuleValue http = new ExtensionsV1beta1HTTPIngressRuleValue();
			List<ExtensionsV1beta1HTTPIngressPath> paths = new ArrayList<>();
			ExtensionsV1beta1HTTPIngressPath ePath = new ExtensionsV1beta1HTTPIngressPath();
			ExtensionsV1beta1IngressBackend backend = new ExtensionsV1beta1IngressBackend();
			
			backend.setServiceName(Constants.K8S_PREFIX + registryName);
			backend.setServicePort(new IntOrString(registrySVCPort));
			ePath.setBackend(backend);
			
			ePath.setPath("/");
			paths.add(ePath);
			http.setPaths(paths);
			eRule.setHttp(http);
			
			eRule.setHost(registryDomain);
			rules.add(eRule);
			spec.setRules(rules);

			ingress.setSpec(spec);

			try {
				extentionApi.createNamespacedIngress(namespace, ingress, null, null, null);

				logger.info(namespace +"/"+ ingress.getMetadata().getName() + " ingress created!!");
			} catch(ApiException e) {
				logger.error(e.getResponseBody());
				
				try {
					patchRegistryStatus(registry, RegistryCondition.Condition.INGRESS, 
							RegistryStatus.Status.FALSE.getStatus(), e.getResponseBody(), "CreateIngressFailed");
				} catch (ApiException e2) {
					logger.error(e2.getResponseBody());
					throw e2;
				}
			}
		}catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			throw e;
		}
	}
	
	public static V1ConfigMap createRegistryConfigMap(Registry registry) throws Exception {
		logger.debug("[K8S ApiCaller] createRegistryConfigMap(Registry) Start");
		V1ConfigMap cm = null;
		
		try {
			String configMapName = registry.getSpec().getCustomConfigYml();
			String registryName = registry.getMetadata().getName();
			String namespace = registry.getMetadata().getNamespace();

			logger.info(namespace + "/" + registryName + " registry's configmap is creating...");
			
			if (StringUtil.isNotEmpty(configMapName)) {
				try {
					cm = api.readNamespacedConfigMap(configMapName, namespace, null, null, null);

					logger.debug("== customConfigYaml ==\n" + cm.toString());
				} catch (ApiException e) {
					logger.error(e.getResponseBody());
					try {
						patchRegistryStatus(registry, RegistryCondition.Condition.CONFIG_MAP, 
								RegistryStatus.Status.FALSE.getStatus(), e.getResponseBody(), "ConfigMapNotExist");
					} catch (ApiException e2) {
						logger.error(e2.getResponseBody());
						throw e2;
					}
					throw e;
				}
				
				V1ObjectMeta metadata = cm.getMetadata();
				Map<String,String> labels = new HashMap<>();
				labels.put("registryUid", registry.getMetadata().getUid());
				labels.put("app", "registry");
				labels.put("apps", registry.getMetadata().getName());
				metadata.setLabels(labels);
				cm.setMetadata(metadata);
				
				try {
					// patch로 하면 data 내용 때문에 에러발생함
					V1ConfigMap result = api.replaceNamespacedConfigMap(cm.getMetadata().getName(), namespace, cm, null, null, null);

					logger.info(result.getMetadata().getNamespace() + "/" + result.getMetadata().getName() + " configmap patched!!");
					logger.debug("\tmetadata:" + result.getMetadata().toString());
					
					updateRegistryStatus(result, Constants.EVENT_TYPE_ADDED);
					
					return result;
				} catch (ApiException e) {
					logger.error(e.getResponseBody());
				}
			} else { // Create New ConfigMap
				configMapName = Constants.K8S_PREFIX + registry.getMetadata().getName();
				try {
					// get hypercloud4-system configmap
					V1ConfigMap regConfig = api.readNamespacedConfigMap(Constants.REGISTRY_CONFIG_MAP_NAME,
							Constants.REGISTRY_NAMESPACE, null, null, null);

					cm = new V1ConfigMap();
					V1ObjectMeta metadata = new V1ObjectMeta();

					metadata.setName(configMapName);
					metadata.setNamespace(namespace);
					
					logger.debug("<ConfigMap Label List>");
					Map<String, String> cmLabels = new HashMap<String, String>();
					cmLabels.put("app", "registry");
					cmLabels.put("apps", registry.getMetadata().getName());
					cmLabels.put("registryUid", registry.getMetadata().getUid());
					logger.debug("app: registry");
					logger.debug("apps: " + registry.getMetadata().getName());
					logger.debug("registryUid: " + registry.getMetadata().getUid());
					metadata.setLabels(cmLabels);
					
					List<V1OwnerReference> ownerRefs = new ArrayList<>();
					V1OwnerReference ownerRef = new V1OwnerReference();

					ownerRef.setApiVersion(registry.getApiVersion());
					ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
					ownerRef.setController(Boolean.TRUE);
					ownerRef.setKind(registry.getKind());
					ownerRef.setName(registry.getMetadata().getName());
					ownerRef.setUid(registry.getMetadata().getUid());
					ownerRefs.add(ownerRef);
					metadata.setOwnerReferences(ownerRefs);
					cm.setMetadata(metadata);
					cm.setData(regConfig.getData());

					V1ConfigMap result = api.createNamespacedConfigMap(namespace, cm, null, null, null);
					logger.info(namespace +"/"+ result.getMetadata().getName() + " configmap created!!");
					logger.debug("\tKey:" + result.getData().keySet() + "\n");
					logger.debug("\tOwnerReferences:" + result.getMetadata().getOwnerReferences());
					logger.debug("\tLabels:" + result.getMetadata().getLabels());
					
					return result;
				} catch (ApiException e) {
					logger.error(e.getResponseBody());
					try {
						patchRegistryStatus(registry, RegistryCondition.Condition.CONFIG_MAP, 
								RegistryStatus.Status.FALSE.getStatus(), e.getResponseBody(), "CreateConfigMapFailed");
					} catch (ApiException e2) {
						logger.error(e2.getResponseBody());
						throw e2;
					}
					throw e;
				}
			}
		}catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			throw e;
		}
		return cm;
	}

	public static void createRegistryReplicaSet(Registry registry) throws Exception {
		logger.debug("[K8S ApiCaller] createRegistryReplicaSet(Registry) Start");
		try {
			V1ReplicaSetBuilder rsBuilder = new V1ReplicaSetBuilder();
			String registryName = registry.getMetadata().getName();
			String namespace = registry.getMetadata().getNamespace();
			RegistryPVC registryPVC = registry.getSpec().getPersistentVolumeClaim();
			int registrySVCTargetPort = RegistryService.REGISTRY_TARGET_PORT;

			logger.info(namespace + "/" + registryName + " registry's replica set is creating...");
			
			// 1. metadata
			V1ObjectMeta rsMeta = new V1ObjectMeta();

			// 1-1. replica set name
			rsMeta.setName(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + registryName);
			logger.debug("RS Name: " + rsMeta.getName());

			// 1-2 replica set label
			logger.debug("<RS Label List>");
			Map<String, String> rsLabels = new HashMap<String, String>();
			rsLabels.put("app", "registry");
			rsLabels.put("apps", rsMeta.getName());
			logger.debug("app: registry");
			logger.debug("apps: " + rsMeta.getName());
			rsMeta.setLabels(rsLabels);

			// 1-3. replica set owner ref
			List<V1OwnerReference> ownerRefs = new ArrayList<>();
			V1OwnerReference ownerRef = new V1OwnerReference();

			ownerRef.setApiVersion(registry.getApiVersion());
			ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
			ownerRef.setController(Boolean.TRUE);
			ownerRef.setKind(registry.getKind());
			ownerRef.setName(registry.getMetadata().getName());
			ownerRef.setUid(registry.getMetadata().getUid());
			ownerRefs.add(ownerRef);
			rsMeta.setOwnerReferences(ownerRefs);

			rsBuilder.withMetadata(rsMeta);

			// 2. spec
			V1ReplicaSetSpec rsSpec = new V1ReplicaSetSpec();

			// 2-1. replicas
			rsSpec.setReplicas(1);

			// 2-2. pod template spec
			V1PodTemplateSpec podTemplateSpec = new V1PodTemplateSpec();

			// 2-2-1. pod metadata
			V1ObjectMeta podMeta = new V1ObjectMeta();

			// 2-2-1-1. pod label
			logger.debug("<Pod Label List>");
			Map<String, String> podLabels = new HashMap<String, String>();
			podLabels.put("app", "registry");
			podLabels.put("apps", rsMeta.getName());
			logger.debug("app: registry");
			logger.debug("apps: " + rsMeta.getName());

			podLabels.put(Constants.K8S_PREFIX + registryName, "lb");
			logger.debug(Constants.K8S_PREFIX + registryName + ": lb");

			// add user label
			if( registry.getSpec().getReplicaSet() != null) {
				Map<String, String> userLabels = null;

				if( (userLabels = registry.getSpec().getReplicaSet().getLabels()) != null) {
					for( String key : userLabels.keySet() ) {
						podLabels.put(key, userLabels.get(key));
					}
				}
			}

			podMeta.setLabels(podLabels);
			podTemplateSpec.setMetadata(podMeta);

			// 2-2-2. pod spec
			V1PodSpec podSpec = new V1PodSpec();

			V1Container container = new V1Container();

			// 2-2-2-2-1. container name
			container.setName(Constants.K8S_PREFIX + registryName.toLowerCase());
			logger.debug("<Container Name: " + container.getName() + ">");

			// 2-2-2-2-2. image
			container.setImage(registry.getSpec().getImage());
			logger.debug("- Image: " + container.getImage());
			container.setImagePullPolicy("IfNotPresent");

			// Set a Lifecycle
			V1Lifecycle lifecycle = new V1Lifecycle();
			V1Handler postStart = new V1Handler();
			V1ExecAction exec = new V1ExecAction();
			List<String> command = new ArrayList<>();
			command.add("/bin/sh");
			command.add("-c");
			command.add("mkdir /auth; htpasswd -Bbn $ID $PASSWD > /auth/htpasswd");

			exec.setCommand(command);
			postStart.setExec(exec);
			lifecycle.setPostStart(postStart);

			container.setLifecycle(lifecycle);

			// 2-2-2-2-3. spec
			V1ResourceRequirements resrcReq = new V1ResourceRequirements();
			Map<String, Quantity> limit = new HashMap<>();
			limit = new HashMap<String, Quantity>();
			limit.put("cpu", new Quantity(Constants.REGISTRY_CPU_STRING));
			limit.put("memory", new Quantity(String.valueOf(Constants.REGISTRY_MEMORY_STRING)));
			resrcReq.setLimits(limit);
			container.setResources(resrcReq);

			// 2-2-2-2-4. env
			// Default env
			V1EnvVar env1 = new V1EnvVar();
			env1.setName("REGISTRY_AUTH");
			env1.setValue("htpasswd");
			container.addEnvItem(env1);

			V1EnvVar env2 = new V1EnvVar();
			env2.setName("REGISTRY_AUTH_HTPASSWD_REALM");
			env2.setValue("Registry Realm");
			container.addEnvItem(env2);

			V1EnvVar env3 = new V1EnvVar();
			env3.setName("REGISTRY_AUTH_HTPASSWD_PATH");
			env3.setValue("/auth/htpasswd");
			container.addEnvItem(env3);

			V1EnvVar env4 = new V1EnvVar();
			env4.setName("REGISTRY_HTTP_ADDR");
			env4.setValue("0.0.0.0:" + registrySVCTargetPort);
			container.addEnvItem(env4);

			V1EnvVar env5 = new V1EnvVar();
			env5.setName("REGISTRY_HTTP_TLS_CERTIFICATE");
			env5.setValue("/certs/localhub.crt");
			container.addEnvItem(env5);

			V1EnvVar env6 = new V1EnvVar();
			env6.setName("REGISTRY_HTTP_TLS_KEY");
			env6.setValue("/certs/localhub.key");
			container.addEnvItem(env6);

			// env
			V1EnvVar secretEnv1 = new V1EnvVar();
			secretEnv1.setName("ID");
			V1EnvVarSource valueFrom = new V1EnvVarSource();
			V1SecretKeySelector secretKeyRef = new V1SecretKeySelector();
			secretKeyRef.setName(Constants.K8S_PREFIX + registryName);
			secretKeyRef.setKey("ID");
			valueFrom.setSecretKeyRef(secretKeyRef);
			secretEnv1.setValueFrom(valueFrom);
			container.addEnvItem(secretEnv1);

			V1EnvVar secretEnv2 = new V1EnvVar();
			secretEnv2.setName("PASSWD");
			V1EnvVarSource valueFrom2 = new V1EnvVarSource();
			V1SecretKeySelector secretKeyRef2 = new V1SecretKeySelector();
			secretKeyRef2.setName(Constants.K8S_PREFIX + registryName);
			secretKeyRef2.setKey("PASSWD");
			valueFrom2.setSecretKeyRef(secretKeyRef2);
			secretEnv2.setValueFrom(valueFrom2);
			container.addEnvItem(secretEnv2);

			// 2-2-2-2-5. port
			V1ContainerPort portsItem = new V1ContainerPort();
			portsItem.setContainerPort(registrySVCTargetPort);
			logger.debug("Container Port: " + portsItem.getContainerPort());

			portsItem.setName(RegistryService.REGISTRY_PORT_NAME);
			portsItem.setProtocol(RegistryService.REGISTRY_PORT_PROTOCOL);
			container.addPortsItem(portsItem);

			// Configmap Volume mount
			// config.yml:/etc/docker/registry/config.yml
			V1VolumeMount configMount = new V1VolumeMount();
			configMount.setName("config");
			configMount.setMountPath("/etc/docker/registry");
			container.addVolumeMountsItem(configMount);

			// Secret Volume mount
			V1VolumeMount certMount = new V1VolumeMount();
			certMount.setName("certs");
			certMount.setMountPath("/certs");
			container.addVolumeMountsItem(certMount);

			// Registry Volume mount
			String pvcMountPath = registryPVC.getMountPath() == null ? "/var/lib/registry" : registryPVC.getMountPath();
			boolean existPvcName = (registryPVC.getExist() != null);
			String volumeMode = null;
			
			if( existPvcName ) {
				V1PersistentVolumeClaim existPvc = null;
				try {
					existPvc = api.readNamespacedPersistentVolumeClaim(registryPVC.getExist().getPvcName(), namespace, null, null, null);
					volumeMode = existPvc.getSpec().getVolumeMode();
				} catch (ApiException e) {
					logger.error(e.getResponseBody());
					try {
						patchRegistryStatus(registry, RegistryCondition.Condition.PVC, 
								RegistryStatus.Status.FALSE.getStatus(), "Creating a registry is failed. " + registryPVC.getExist().getPvcName() + " PVC is not found", "PVCNotFound");
					} catch (ApiException e2) {
						logger.error(e2.getResponseBody());
						throw e2;
					}
					throw e;
				}
			}
			
			if( !existPvcName) {
				if (registry.getSpec().getPersistentVolumeClaim().getCreate().getVolumeMode() != null) {
					volumeMode = registry.getSpec().getPersistentVolumeClaim().getCreate().getVolumeMode();
				} else {
					volumeMode = "Filesystem";	// default value
				}
			} 

			if( volumeMode.equals("Block")) {
				V1VolumeDevice volumeDevicesItem = new V1VolumeDevice();

				volumeDevicesItem.setName("registry");
				volumeDevicesItem.setDevicePath(pvcMountPath);
				container.addVolumeDevicesItem(volumeDevicesItem);
			} else {
				V1VolumeMount registryMount = new V1VolumeMount();

				registryMount.setName("registry");
				registryMount.setMountPath(pvcMountPath);
				container.addVolumeMountsItem(registryMount);
			}

			// Get loginAuth For Readiness Probe
			String loginAuth = registry.getSpec().getLoginId() + ":" + registry.getSpec().getLoginPassword();
			loginAuth = new String(Base64.encodeBase64(loginAuth.getBytes()));

			// Set Readiness Probe
			V1Probe readinessProbe = new V1Probe();
			V1HTTPGetAction httpGet = new V1HTTPGetAction();
			List<V1HTTPHeader> headers = new ArrayList<V1HTTPHeader>();
			V1HTTPHeader authHeader = new V1HTTPHeader();
			authHeader.setName("authorization");
			authHeader.setValue("Basic " + loginAuth);
			headers.add(authHeader);

			httpGet.setPath("/v2/_catalog");
			httpGet.setPort(new IntOrString(registrySVCTargetPort));
			httpGet.setScheme("HTTPS");
			httpGet.setHttpHeaders(headers);
			readinessProbe.setHttpGet(httpGet);

			readinessProbe.setFailureThreshold(10);
			readinessProbe.setInitialDelaySeconds(5);
			readinessProbe.setPeriodSeconds(3);
			readinessProbe.setSuccessThreshold(1);
			readinessProbe.setTimeoutSeconds(1);
			container.setReadinessProbe(readinessProbe);

			// Set Liveness Probe
			V1Probe livenessProbe = new V1Probe();
			V1HTTPGetAction httpGet2 = new V1HTTPGetAction();
			List<V1HTTPHeader> headers2 = new ArrayList<V1HTTPHeader>();
			V1HTTPHeader authHeader2 = new V1HTTPHeader();
			authHeader2.setName("authorization");
			authHeader2.setValue("Basic " + loginAuth);
			headers2.add(authHeader2);

			httpGet2.setPath("/v2/_catalog");
			httpGet2.setPort(new IntOrString(registrySVCTargetPort));
			httpGet2.setScheme("HTTPS");
			httpGet2.setHttpHeaders(headers2);
			livenessProbe.setHttpGet(httpGet2);

			livenessProbe.setFailureThreshold(10);
			livenessProbe.setInitialDelaySeconds(5);
			livenessProbe.setPeriodSeconds(5);
			livenessProbe.setSuccessThreshold(1);
			livenessProbe.setTimeoutSeconds(30);
			container.setLivenessProbe(livenessProbe);

			podSpec.addContainersItem(container);

			List<V1Volume> volumes = new ArrayList<>();

			
			// Configmap Volume
			V1Volume configVolume = new V1Volume();
			V1ConfigMapVolumeSource configMap = new V1ConfigMapVolumeSource();
			String configMapName = registry.getSpec().getCustomConfigYml();
			if( configMapName == null ) {
				configMapName = Constants.K8S_PREFIX + registry.getMetadata().getName();
			}
			configMap.setName(configMapName);
			configVolume.setConfigMap(configMap);
			configVolume.setName("config");
			volumes.add(configVolume);


			// Secret Volume
			String secretName = Constants.K8S_PREFIX + registryName;
			V1Volume certVolume = new V1Volume();
			certVolume.setName("certs");
			V1SecretVolumeSource volSecret = new V1SecretVolumeSource();
			logger.debug("secret name: " + secretName);
			volSecret.setSecretName(secretName);
			certVolume.setSecret(volSecret);
			volumes.add(certVolume);

			// Registry Volume
			String pvcName = registryPVC.getExist() != null ? registryPVC.getExist().getPvcName() : Constants.K8S_PREFIX + registryName;
			V1Volume registryVolume = new V1Volume();
			registryVolume.setName("registry");
			V1PersistentVolumeClaimVolumeSource regPvc = new V1PersistentVolumeClaimVolumeSource();

			regPvc.setClaimName(pvcName);
			registryVolume.setPersistentVolumeClaim(regPvc);

			volumes.add(registryVolume);

			podSpec.setVolumes(volumes);

			// restart policy
			podSpec.setRestartPolicy("Always");
			logger.debug("Restart Policy: " + podSpec.getRestartPolicy());

			// node selector
			if( registry.getSpec().getReplicaSet() != null) {
				Map<String, String> nodeSelector = null;
				List<V1Toleration> tolerations = null;

				if( (nodeSelector = registry.getSpec().getReplicaSet().getNodeSelector()) != null) 
					podSpec.setNodeSelector(nodeSelector);

				if( (tolerations = registry.getSpec().getReplicaSet().getTolerations()) != null) 
					podSpec.setTolerations(tolerations);
			}

			podTemplateSpec.setSpec(podSpec);
			rsSpec.setTemplate(podTemplateSpec);

			// label selector
			V1LabelSelector labelSelector = new V1LabelSelector();
			logger.debug("<RS Label List>");
			Map<String, String> rsLabelSelector = new HashMap<String, String>();
			rsLabelSelector.put("app", "registry");
			rsLabelSelector.put("apps", rsMeta.getName());
			logger.debug("app: registry");
			logger.debug("apps: " + rsMeta.getName());

			// user label selector
			if( registry.getSpec().getReplicaSet() != null) {
				V1LabelSelector rsSelector = null;
				List<V1LabelSelectorRequirement> matchExpressions = null;
				Map<String, String> matchLabels = null;

				if( (rsSelector = registry.getSpec().getReplicaSet().getSelector()) != null) {
					matchLabels = rsSelector.getMatchLabels();
					if( matchLabels != null ) {
						for( String key : matchLabels.keySet()) {
							rsLabelSelector.put(key, matchLabels.get(key));
						}
					}

					matchExpressions = rsSelector.getMatchExpressions();
					if( matchExpressions != null ) {
						labelSelector.setMatchExpressions(matchExpressions);
					}
				}
			}

			labelSelector.setMatchLabels(rsLabelSelector);
			rsSpec.setSelector(labelSelector);

			rsBuilder.withSpec(rsSpec);

			try {
				logger.debug("Create ReplicaSet");
				V1ReplicaSet result = appApi.createNamespacedReplicaSet(namespace, rsBuilder.build(), null, null, null);
				logger.info(namespace +"/"+ result.getMetadata().getName() + " replica set created!!");
				logger.debug("\tOwnerReferences:" + result.getMetadata().getOwnerReferences());
				logger.debug("\tLabels:" + result.getMetadata().getLabels());
			} catch (ApiException e) {
				logger.error(e.getResponseBody());

				try {
					patchRegistryStatus(registry, RegistryCondition.Condition.REPLICA_SET, 
							RegistryStatus.Status.FALSE.getStatus(), e.getResponseBody(), "CreateReplicaSetFailed");
				} catch (ApiException e2) {
					logger.error(e2.getResponseBody());
					throw e2;
				}

				throw e;
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			throw e;
		}
	}
	
	public static Set<RegistryCondition.Condition> getRecreatedSubres(JsonNode diff) {
		Set<RegistryCondition.Condition> recreateSubresources = new HashSet<>();
		
		for (final JsonNode obj : diff) {
			String path = "";
			String op = "";

			logger.debug("update object: " + obj.toString());
			if (obj.get("path") != null) {
				path = obj.get("path").toString().split("\"")[1];
			}
			if (obj.get("op") != null) {
				op = obj.get("op").toString().split("\"")[1];
			}
			
			if (path.startsWith("/spec/replicaSet")) {
				recreateSubresources.add(RegistryCondition.Condition.REPLICA_SET);
			}
			if (path.startsWith("/spec/service")) {
				recreateSubresources.add(RegistryCondition.Condition.SERVICE);
				recreateSubresources.add(RegistryCondition.Condition.REPLICA_SET);
				recreateSubresources.add(RegistryCondition.Condition.SECRET_OPAQUE);
				recreateSubresources.add(RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON);
				if(path.equals("/spec/service/ingress") 
						&& op.equals("add")) {
					recreateSubresources.add(RegistryCondition.Condition.SECRET_TLS);
					recreateSubresources.add(RegistryCondition.Condition.INGRESS);
				}
			}
			if (path.startsWith("/spec/persistentVolumeClaim/exist")) {
				recreateSubresources.add(RegistryCondition.Condition.REPLICA_SET);
			}
			if (path.equals("/spec/image")) {
				recreateSubresources.add(RegistryCondition.Condition.REPLICA_SET);
			}
			if (path.equals("/spec/loginId")) {
				recreateSubresources.add(RegistryCondition.Condition.POD);
			}
			if (path.equals("/spec/loginPassword")) {
				recreateSubresources.add(RegistryCondition.Condition.POD);
			}
		}
		
		return recreateSubresources;
	}
	
	public static void updateRegistrySubresources(Registry registry, JsonNode diff, JsonNode beforeJson) throws ApiException {
		logger.debug("[K8S ApiCaller] updateRegistrySubresources(Registry, JsonNode) Start");
		String namespace = registry.getMetadata().getNamespace();
		String registryName = registry.getMetadata().getName();
		Set<String> updateSubResources = new HashSet<>();
		Set<RegistryCondition.Condition> recreateSubresources = new HashSet<>();
		boolean renewLoginAuthRequired = false;
		JsonArray jArrayPatchReplicaSet = new JsonArray();
		JsonArray jArrayPatchSecret = new JsonArray();

		for (final JsonNode obj : diff) {
			String path = "";
			String op = "";

			logger.debug("update object: " + obj.toString());
			if (obj.get("path") != null) {
				path = obj.get("path").toString().split("\"")[1];
			}
			if (obj.get("op") != null) {
				op = obj.get("op").toString().split("\"")[1];
			}
			
			if (path.startsWith("/spec/replicaSet")) {
				recreateSubresources.add(RegistryCondition.Condition.REPLICA_SET);
			}
			if (path.equals("/spec/customConfigYml")) {
				String configMapName = null;
				V1ConfigMap cm = null;
				if(op.equals("add")) {
					deleteRegistrySubresource(registry, RegistryCondition.Condition.CONFIG_MAP);
				} else if(op.equals("remove") || op.equals("replace")) {
					configMapName = beforeJson.get("spec").get("customConfigYml").asText();
					
					try {
						cm = api.readNamespacedConfigMap(configMapName, namespace, null, null, null);
						logger.debug(cm.toString());
					} catch (ApiException e) {
						logger.error(e.getResponseBody());
						try {
							patchRegistryStatus(registry, RegistryCondition.Condition.CONFIG_MAP, 
									RegistryStatus.Status.FALSE.getStatus(), e.getResponseBody(), "ConfigMapNotExist");
						} catch (ApiException e2) {
							logger.error(e2.getResponseBody());
						}
					}

					if (cm != null) {
						V1ObjectMeta metadata = cm.getMetadata();
						Map<String,String> labels = metadata.getLabels();
						if (labels != null) {
							labels.remove("registryUid");
							labels.remove("app");
							labels.remove("apps");
							metadata.setLabels(labels);
							cm.setMetadata(metadata);
						}

						try {
							// patch로 하면 data 내용 때문에 에러발생함
							V1ConfigMap result = api.replaceNamespacedConfigMap(cm.getMetadata().getName(), namespace, cm, null, null, null);

							logger.info(result.getMetadata().getNamespace() + "/" + result.getMetadata().getName() + " configmap patched!!");
							logger.debug("\tmetadata:" + result.getMetadata().toString());
						} catch (ApiException e) {
							logger.error(e.getResponseBody());
						}
					}

					try {
						createRegistryConfigMap(registry);
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				}
//				recreateSubresources.add(RegistryCondition.Condition.CONFIG_MAP);
				recreateSubresources.add(RegistryCondition.Condition.REPLICA_SET);
			}
			if (path.startsWith("/spec/service")) {
				recreateSubresources.add(RegistryCondition.Condition.SERVICE);
				recreateSubresources.add(RegistryCondition.Condition.REPLICA_SET);
				recreateSubresources.add(RegistryCondition.Condition.SECRET_OPAQUE);
				recreateSubresources.add(RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON);
				if(path.equals("/spec/service/ingress") 
						&& op.equals("add")) {
					try {
						createRegistryTlsSecret(registry);
						createRegistryIngress(registry);
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				} else {
					deleteRegistrySubresource(registry, RegistryCondition.Condition.SECRET_TLS);
					deleteRegistrySubresource(registry, RegistryCondition.Condition.INGRESS);
				}
			}
			if (path.startsWith("/spec/persistentVolumeClaim/exist")) {
				try {
					createRegistryPvc(registry);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				recreateSubresources.add(RegistryCondition.Condition.REPLICA_SET);
			}
			if (path.equals("/spec/image")) {
				recreateSubresources.add(RegistryCondition.Condition.REPLICA_SET);
			}
			if (path.equals("/spec/loginId")) {
				recreateSubresources.add(RegistryCondition.Condition.POD);
				renewLoginAuthRequired = true;
				String dataStr = registry.getSpec().getLoginId();
				byte[] encodeData = Base64.encodeBase64(dataStr.getBytes());

				jArrayPatchSecret.add(Util.makePatchJsonObject("replace", "/data/ID", new String(encodeData)));

				if (!updateSubResources.contains("ReplicaSet"))
					updateSubResources.add("ReplicaSet");
				if (!updateSubResources.contains("Secret"))
					updateSubResources.add("Secret");
			}
			if (path.equals("/spec/loginPassword")) {
				recreateSubresources.add(RegistryCondition.Condition.POD);
				renewLoginAuthRequired = true;
				String dataStr = registry.getSpec().getLoginPassword();
				byte[] encodeData = Base64.encodeBase64(dataStr.getBytes());

				jArrayPatchSecret.add(Util.makePatchJsonObject("replace", "/data/PASSWD", new String(encodeData)));

				if (!updateSubResources.contains("ReplicaSet"))
					updateSubResources.add("ReplicaSet");
				if (!updateSubResources.contains("Secret"))
					updateSubResources.add("Secret");
			}
			if(path.equals("/spec/persistentVolumeClaim/create/deleteWithPvc")) {
				if( registry.getSpec().getPersistentVolumeClaim().getCreate().getDeleteWithPvc() ) {
					JsonArray jArrayPatchPvc = new JsonArray();
					JsonArray ownerRefs = new JsonArray();
					JsonObject ownerRef = new JsonObject();
					
					ownerRef.addProperty("apiVersion", Constants.CUSTOM_OBJECT_GROUP + "/" + Constants.CUSTOM_OBJECT_VERSION);
					ownerRef.addProperty("blockOwnerDeletion", Boolean.TRUE);
					ownerRef.addProperty("controller", Boolean.TRUE);
					ownerRef.addProperty("kind", registry.getKind());
					ownerRef.addProperty("name", registry.getMetadata().getName());
					ownerRef.addProperty("uid", registry.getMetadata().getUid());
					
					ownerRefs.add(ownerRef);

					jArrayPatchPvc.add(Util.makePatchJsonObject("replace", "/metadata/ownerReferences", ownerRefs));
					
					try {
						V1PersistentVolumeClaim result = api.patchNamespacedPersistentVolumeClaim(Constants.K8S_PREFIX + registryName, namespace, new V1Patch(jArrayPatchPvc.toString()), null, null, null, null);
						logger.debug("PVC is patched: " + result.getMetadata().getNamespace() + "/" + result.getMetadata().getName());
						logger.debug("\tOwnerReferences:" + result.getMetadata().getOwnerReferences());
						logger.debug("\tLabels:" + result.getMetadata().getLabels());
					} catch (ApiException e) {
						logger.error(e.getResponseBody());
					}
				} else {
					try {
						V1PersistentVolumeClaim pvc = api.readNamespacedPersistentVolumeClaim(Constants.K8S_PREFIX + registryName, namespace, null, null, null);
						pvc.getMetadata().setOwnerReferences(null);
						
						V1PersistentVolumeClaim result = api.replaceNamespacedPersistentVolumeClaim(Constants.K8S_PREFIX + registryName, namespace, pvc, null, null, null);
						logger.debug("PVC is replaced: " + result.getMetadata().getNamespace() + "/" + result.getMetadata().getName());
						logger.debug("\tOwnerReferences:" + result.getMetadata().getOwnerReferences());
						logger.debug("\tLabels:" + result.getMetadata().getLabels());
					} catch (ApiException e) {
						logger.error(e.getResponseBody());
						throw e;
					}
				}
			}

			if (renewLoginAuthRequired) {
				String loginAuth = registry.getSpec().getLoginId() + ":" + registry.getSpec().getLoginPassword();
				loginAuth = new String(Base64.encodeBase64(loginAuth.getBytes()));

				jArrayPatchReplicaSet.add(Util.makePatchJsonObject("replace", 
						"/spec/template/spec/containers/0/readinessProbe/httpGet/httpHeaders/0/value", 
						"Basic " + loginAuth));

				jArrayPatchReplicaSet.add(Util.makePatchJsonObject("replace", 
						"/spec/template/spec/containers/0/livenessProbe/httpGet/httpHeaders/0/value", 
						"Basic " + loginAuth));
			}
		}

		for (String res : updateSubResources) {
			if (res.equals("ReplicaSet")) {
				updateRegistryReplicaSet(registry, jArrayPatchReplicaSet);
			}
			if (res.equals("Secret")) {
				updateRegistrySecret(registry, jArrayPatchSecret);
			}
		}
		
		if( recreateSubresources.contains(RegistryCondition.Condition.REPLICA_SET) 
				&& recreateSubresources.contains(RegistryCondition.Condition.POD) ) {
			recreateSubresources.remove(RegistryCondition.Condition.POD);
		}
		
		// delete subresource
		// required delete order: ConfigMap, Service >  Secret, Ingress > Replicaset(Pod)
		if(recreateSubresources.contains(RegistryCondition.Condition.CONFIG_MAP)) {
//			deleteRegistrySubresource(registry, RegistryCondition.Condition.CONFIG_MAP);
			try {
				V1ConfigMap cm = createRegistryConfigMap(registry);
//				if(cm != null) {
//					updateRegistryStatus(cm, Constants.EVENT_TYPE_ADDED);
//				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		if(recreateSubresources.contains(RegistryCondition.Condition.SERVICE)) {
			deleteRegistrySubresource(registry, RegistryCondition.Condition.SERVICE);
		}
		if(recreateSubresources.contains(RegistryCondition.Condition.SECRET_OPAQUE)) {
			deleteRegistrySubresource(registry, RegistryCondition.Condition.SECRET_OPAQUE);
		}
		if(recreateSubresources.contains(RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON)) {
			deleteRegistrySubresource(registry, RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON);
		}
		if(recreateSubresources.contains(RegistryCondition.Condition.SECRET_TLS)) {
			deleteRegistrySubresource(registry, RegistryCondition.Condition.SECRET_TLS);
		}
		if(recreateSubresources.contains(RegistryCondition.Condition.INGRESS)) {
			deleteRegistrySubresource(registry, RegistryCondition.Condition.INGRESS);
		}
		if(recreateSubresources.contains(RegistryCondition.Condition.REPLICA_SET)) {
			deleteRegistrySubresource(registry, RegistryCondition.Condition.REPLICA_SET);
		}
		if(recreateSubresources.contains(RegistryCondition.Condition.POD)) {
			deleteRegistrySubresource(registry, RegistryCondition.Condition.POD);
		}
	}

	public static void updateRegistryReplicaSet(Registry registry, JsonElement patchJson) throws ApiException {
		logger.debug("[K8S ApiCaller] updateRegistryReplicaSet(Registry, JsonElement) Start");
		String namespace = registry.getMetadata().getNamespace();
		String registryName = registry.getMetadata().getName();
		logger.debug("updateRegistryReplicaSet's Json: " + patchJson.toString());

		try {
			V1ReplicaSet result = appApi.patchNamespacedReplicaSet(
					Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + registryName, namespace, new V1Patch(patchJson.toString()), null, null,
					null, null);
			logger.debug("Secret is created: " + result.getMetadata().getNamespace() + "/" + result.getMetadata().getName());
			logger.debug("\tOwnerReferences:" + result.getMetadata().getOwnerReferences());
			logger.debug("\tLabels:" + result.getMetadata().getLabels());
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}

	public static void updateRegistrySecret(Registry registry, JsonElement patchJson) throws ApiException {
		logger.debug("[K8S ApiCaller] updateRegistrySecret(Registry, JsonElement) Start");
		String namespace = registry.getMetadata().getNamespace();
		String registryName = registry.getMetadata().getName();
		logger.debug("updateRegistrySecret's Json: " + patchJson.toString());

		try {
			V1Secret result = api.patchNamespacedSecret(Constants.K8S_PREFIX + registryName, namespace, new V1Patch(patchJson.toString()), null,
					null, null, null);
			logger.debug("Secret is patched: " + result.getMetadata().getNamespace() + "/" + result.getMetadata().getName());
			logger.debug("\tKey:" + result.getData().keySet() + "\n");
			logger.debug("\tOwnerReferences:" + result.getMetadata().getOwnerReferences());
			logger.debug("\tLabels:" + result.getMetadata().getLabels());
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}

	public static void updateRegistryAnnotationLastCR(Registry registry, JsonNode diff) throws ApiException {
		logger.debug("[K8S ApiCaller] updateRegistryAnnotationLastCR(Registry) Start");
		String namespace = registry.getMetadata().getNamespace();
		String registryName = registry.getMetadata().getName();

		// ------ Patch Registry
		Map<String, String> annotations = registry.getMetadata().getAnnotations() == null ? new HashMap<>()
				: registry.getMetadata().getAnnotations();
		JsonObject json = (JsonObject) Util.toJson(registry);

		annotations.put(Constants.LAST_CUSTOM_RESOURCE, json.toString());
		if(diff != null) {
			annotations.put(Constants.UPDATING_FIELDS, diff.toString());
		} else {
			annotations.remove(Constants.UPDATING_FIELDS);
		}
		
		registry.getMetadata().setAnnotations(annotations);

		try {
			Object result = customObjectApi.replaceNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
					registry);
			logger.debug("replaceNamespacedCustomObject result: " + result.toString() + "\n");

		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}

	public static void addRegistryAnnotation(Registry registry) throws ApiException {
		logger.debug("[K8S ApiCaller] addRegistryAnnotation(Registry) Start");
		String namespace = registry.getMetadata().getNamespace();
		String registryName = registry.getMetadata().getName();
		String registryIpPort = "";

		V1Secret secretRet = api.readNamespacedSecret(Constants.K8S_PREFIX + registryName, namespace, null, null, null);

		Map<String, byte[]> secretMap = secretRet.getData();
		registryIpPort = new String(secretMap.get("REGISTRY_URL"));
		logger.debug("REGISTRY_URL = " + registryIpPort);

		// ------ Patch Registry
		Map<String, String> annotations = registry.getMetadata().getAnnotations() == null ? new HashMap<>()
				: registry.getMetadata().getAnnotations();
		JsonObject json = (JsonObject) Util.toJson(registry);

		annotations.put(Constants.LAST_CUSTOM_RESOURCE, json.toString());
		annotations.put(Registry.REGISTRY_LOGIN_URL, "https://" + registryIpPort);
		registry.getMetadata().setAnnotations(annotations);

		try {
			Object result = customObjectApi.replaceNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
					registry);

			logger.debug("replaceNamespacedCustomObject result: " + result.toString() + "\n");
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}
	
	public static boolean updateRegistryAnnotation(Registry registry) throws ApiException {
		logger.debug("[K8S ApiCaller] updateRegistryAnnotation(Registry) Start");
		String namespace = registry.getMetadata().getNamespace();
		String registryName = registry.getMetadata().getName();
		String registryIpPort = "";

		V1Secret secretRet = api.readNamespacedSecret(Constants.K8S_PREFIX + registryName, namespace, null, null, null);

		Map<String, byte[]> secretMap = secretRet.getData();
		registryIpPort = new String(secretMap.get("REGISTRY_URL"));
		logger.debug("REGISTRY_URL = " + registryIpPort);
		
		// ------ Patch Registry
		Map<String, String> annotations = registry.getMetadata().getAnnotations() == null ? new HashMap<>()
				: registry.getMetadata().getAnnotations();
		
		String existUrl = annotations.get(Registry.REGISTRY_LOGIN_URL);
		String changeUrl = "https://" + registryIpPort;
		if( changeUrl.equals(existUrl) ) {
			logger.debug(Registry.REGISTRY_LOGIN_URL + " is not changed");
			return false;
		}
		
		JsonObject json = (JsonObject) Util.toJson(registry);

		annotations.put(Constants.LAST_CUSTOM_RESOURCE, json.toString());
		if(existUrl == null)
			annotations.put(Registry.REGISTRY_LOGIN_URL, changeUrl);
		else 
			annotations.replace(Registry.REGISTRY_LOGIN_URL, changeUrl);
		
		registry.getMetadata().setAnnotations(annotations);

		try {
			Object result = customObjectApi.replaceNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
					registry);

			logger.debug("replaceNamespacedCustomObject result: " + result.toString() + "\n");
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
		
		return true;
	}
	
	public static void deleteRegistryAnnotationUrl(Registry registry) throws ApiException {
		logger.debug("[K8S ApiCaller] deleteRegistryAnnotationUrl(Registry) Start");
		String namespace = registry.getMetadata().getNamespace();
		String registryName = registry.getMetadata().getName();

		// ------ Patch Registry
		Map<String, String> annotations = registry.getMetadata().getAnnotations() == null ? new HashMap<>()
				: registry.getMetadata().getAnnotations();

		annotations.remove(Registry.REGISTRY_LOGIN_URL);
		registry.getMetadata().setAnnotations(annotations);

		try {
			Object result = customObjectApi.replaceNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
					registry);

			logger.debug("replaceNamespacedCustomObject result: " + result.toString() + "\n");
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}
	
	public static void patchRegistrySpec() throws IOException {
		try {
			logger.debug("patchRegistrySpec");
			Object response = customObjectApi.listClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, null, null, null, null,
					null, null, null, Boolean.FALSE);

			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			mapper.registerModule(new JodaModule());
			List<Object> registryList = mapper.readValue((new Gson()).toJson(respJson.get("items")),
					new TypeReference<ArrayList<Object>>() {
					});

			if (registryList != null) {
				for (Object registryObj : registryList) {
					try {
						JsonArray patchArray = new JsonArray();
						Registry registry = mapper.treeToValue(mapper.valueToTree(registryObj), Registry.class);
						String registryName = registry.getMetadata().getName();
						String namespace = registry.getMetadata().getNamespace();
						Integer ingressPort = null;
						Integer lbPort = null;
						if(registry.getSpec().getService().getIngress() != null) {
							if(registry.getSpec().getService().getIngress().getPort() < 1
									|| registry.getSpec().getService().getIngress().getPort() > 65535)
								ingressPort = registry.getSpec().getService().getIngress().getPort();
							patchArray.add(Util.makePatchJsonObject("replace", "/spec/service/ingress/port", 443));
							
//							// Patch: 4.1.0.46- => 4.1.0.47+
//							if (registry.getSpec().getService().getIngress().getIngressClass() == null) {
//								patchArray.add(Util.makePatchJsonObject("add", "/spec/service/ingress/ingressClass", "nginx"));
//							}
						}

						if(registry.getSpec().getService().getLoadBalancer() != null) {
							if(registry.getSpec().getService().getLoadBalancer().getPort() < 1
									|| registry.getSpec().getService().getLoadBalancer().getPort() > 65535)
								lbPort = registry.getSpec().getService().getLoadBalancer().getPort();
							patchArray.add(Util.makePatchJsonObject("replace", "/spec/service/loadBalancer/port", 443));
						}
						
						if(patchArray.size() == 0) 
							continue;
						
						logger.debug(registry.getMetadata().getName() + "/" + registry.getMetadata().getNamespace()
								+ " registry spec is patched.");

						try { 
							customObjectApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
									Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
									patchArray);
							logger.debug("registry spec is patched: " + namespace + "/" + registryName);
							if( ingressPort != null )
								logger.debug("\tspec.service.ingress:" + ingressPort + " -> 443");
							if( lbPort != null )
								logger.debug("\tspec.service.loadBalancer:" + lbPort + " -> 443");
						} catch (ApiException e) {
							logger.error(e.getResponseBody());
//							throw e;
						}
//					} catch (ApiException e) {
//						logger.error(e.getResponseBody());
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				}
			}
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
		} catch (JsonParseException | JsonMappingException e) {
			logger.error(e.getMessage());
//			throw e;
		}
	}
	
	public static void patchRegistryStatus(Registry registry, String phase, String message, String reason, DateTime phaseChangedAt) throws ApiException{
		String registryName = registry.getMetadata().getName();
		String namespace = registry.getMetadata().getNamespace();
		JsonArray patchStatusArray = new JsonArray();

		if (phase != null && message != null && reason != null && phaseChangedAt != null) {
			patchStatusArray.add(Util.makePatchJsonObject("replace", "/status/phase", phase));
			patchStatusArray.add(Util.makePatchJsonObject("replace", "/status/message", message));
			patchStatusArray.add(Util.makePatchJsonObject("replace", "/status/reason", reason));
			if(registry.getStatus().getPhaseChangedAt() != null) {
				patchStatusArray.add(Util.makePatchJsonObject("replace", "/status/phaseChangedAt", phaseChangedAt.toString()));
			} else {
				patchStatusArray.add(Util.makePatchJsonObject("add", "/status/phaseChangedAt", phaseChangedAt.toString()));
			}
			
			try {
				customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
						Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
						registryName, patchStatusArray);
				logger.debug("CRD Status is patched: " + namespace + "/" + registryName);
				logger.debug("\tphase(" + phase + ")");
				logger.debug("\tmessage(" + message + ")");
				logger.debug("\treason(" + reason + ")\n");
			} catch (ApiException e) {
				logger.error(e.getResponseBody());
				throw e;
			}
		}
	}
	
	public static void patchRegistryStatus(String registryName, String namespace, String phase, String message, String reason, DateTime phaseChangedAt) throws ApiException{
		JsonArray patchStatusArray = new JsonArray();

		if (phase != null && message != null && reason != null && phaseChangedAt != null) {
			patchStatusArray.add(Util.makePatchJsonObject("replace", "/status/phase", phase));
			patchStatusArray.add(Util.makePatchJsonObject("replace", "/status/message", message));
			patchStatusArray.add(Util.makePatchJsonObject("replace", "/status/reason", reason));
			patchStatusArray.add(Util.makePatchJsonObject("replace", "/status/phaseChangedAt", phaseChangedAt));

			try {
				customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
						Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
						registryName, patchStatusArray);
				logger.debug("CRD Status is patched: " + namespace + "/" + registryName);
				logger.debug("\tphase(" + phase + ")");
				logger.debug("\tmessage(" + message + ")");
				logger.debug("\treason(" + reason + ")\n");
			} catch (ApiException e) {
				logger.error(e.getResponseBody());
				throw e;
			}
		}
	}
	
	public static void patchRegistryStatus(Registry registry, RegistryCondition.Condition cdt, String status, String message, String reason) throws ApiException{
		String registryName = registry.getMetadata().getName();
		String namespace = registry.getMetadata().getNamespace();
		JsonObject condition = new JsonObject();
		JsonArray patchStatusArray = new JsonArray();

		if( cdt != null )	{
			condition.addProperty("type", cdt.getType());
			
			if( status != null )		condition.addProperty("status", status);
			if( message != null )		condition.addProperty("message", message);
			if( reason != null )		condition.addProperty("reason", reason);
			
			patchStatusArray.add(Util.makePatchJsonObject("replace", cdt.getPath(), condition));
			
			try {
				customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
						Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
						registryName, patchStatusArray);
				logger.debug("CRD Status is Patched: " + namespace + "/" + registryName + " : " + condition.get("type").getAsString() + "(" + condition.get("status").getAsString() + ")\n");
			} catch (ApiException e) {
				logger.error(e.getResponseBody());
				throw e;
			}
		}
	}
	
	public static void updateRegistryStatus(V1ReplicaSet rs, String eventType)
			throws Exception {
		logger.debug("[K8S ApiCaller] updateRegistryStatus(V1ReplicaSet, String) Start");

		String registryName = "";
		String registryPrefix = Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX;
		String namespace = rs.getMetadata().getNamespace();
		boolean restoreRegistry = false;

		registryName = rs.getMetadata().getName();
		registryName = registryName.substring(registryPrefix.length());
		logger.debug("registry name: " + registryName);

		if (rs.getMetadata().getOwnerReferences() == null) {
			logger.debug(namespace + "/" + rs.getMetadata().getName() + " replicaset ownerReference is null");
			return;
		}

		try {
			if (!isCurrentRegistry(rs.getMetadata().getOwnerReferences().get(0).getUid(), registryName, namespace, "RegistryReplicaSet")) {
				logger.debug("This registry's event is not for current registry. So do not update registry status");
				return;
			}
		} catch(ApiException e) {
			if(e.getCode() == 404) {
				logger.info(namespace + "/" + rs.getMetadata().getName() + " replica set is deleted");
			}
			throw e;
		}
		
		JsonObject condition = new JsonObject();
		JsonArray patchStatusArray = new JsonArray();
		DateTime curTime = new DateTime();
		switch (eventType) {
		case Constants.EVENT_TYPE_ADDED:
			condition.addProperty("type", RegistryCondition.Condition.REPLICA_SET.getType());
			condition.addProperty("status", RegistryStatus.Status.TRUE.getStatus());
			condition.addProperty("lastTransitionTime", curTime.toString());

			patchStatusArray.add(Util.makePatchJsonObject("replace", 
					RegistryCondition.Condition.REPLICA_SET.getPath(), condition));
			
			break;
		case Constants.EVENT_TYPE_MODIFIED:

			return;
		case Constants.EVENT_TYPE_DELETED:
			condition.addProperty("type", RegistryCondition.Condition.REPLICA_SET.getType());
			condition.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
			condition.addProperty("lastTransitionTime", curTime.toString());

			patchStatusArray.add(Util.makePatchJsonObject("replace", 
					RegistryCondition.Condition.REPLICA_SET.getPath(), condition));

			restoreRegistry = true;
			
			break;
		}
		
		try {
			customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
					patchStatusArray);
			logger.debug("patchNamespacedCustomObjectStatus result: " + condition.get("type").getAsString() + "(" + condition.get("status").getAsString() + ")\n");
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.debug("[RegistryReplicaSet]" + namespace + "/" + registryName + " registry was deleted!!");
			} else {
				logger.error("[RegistryReplicaSet]" + e.getResponseBody());
			}
		}
		
		if(restoreRegistry) {
			Registry registry;
			
			try {
				registry = getRegistry(registryName, namespace);
				createRegistryReplicaSet(registry);
			} catch (Exception e) {
				logger.error(e.getMessage());
				throw e;
			}
		}
		
	}
	
	public static void updateRegistryStatus(V1Pod pod, String eventType)
			throws Exception {
		logger.debug("[K8S ApiCaller] updateRegistryStatus(V1Pod) Start");
		
		String registryPrefix = Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX;
		String registryName = pod.getMetadata().getLabels().get("apps").substring(registryPrefix.length());
		String namespace = pod.getMetadata().getNamespace();
		String reason = "";
		logger.debug("registry name: " + registryName);

		// Check if this pod event is exist registry's event.
		String verifyUid = null;
		
		try {
			V1ReplicaSet rs = appApi.readNamespacedReplicaSet(
					Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + registryName, namespace, null, null, null);

			if (pod.getMetadata().getOwnerReferences() != null) {
				if (pod.getMetadata().getOwnerReferences().get(0).getUid().equals(rs.getMetadata().getUid())) {
					if (rs.getMetadata().getOwnerReferences() != null) {
						verifyUid = rs.getMetadata().getOwnerReferences().get(0).getUid();
					}
				} else {
					logger.debug("This pod uid(" + pod.getMetadata().getOwnerReferences().get(0).getUid()
						+ ") is not for current replicaset(" + rs.getMetadata().getOwnerReferences().get(0).getUid() + ")");
				}
			}
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.debug(namespace + "/" + registryName + " registry was deleted!!");
			} else {
				logger.error(e.getResponseBody());
			}
			throw e;
		}
		
		try {
			if (!isCurrentRegistry(verifyUid, registryName, namespace, "RegistryPod")) {
				logger.debug("This registry's event is not for current registry. So do not update registry status");
				return;
			}
		} catch(ApiException e) {
			if(e.getCode() == 404) {
				logger.info(namespace + "/" + pod.getMetadata().getName() + " pod is deleted");
			}
			throw e;
		}

		JsonArray patchStatusArray = new JsonArray();
		DateTime curTime = new DateTime();
		JsonObject condition = new JsonObject();
		JsonObject condition2 = new JsonObject();
		
		condition.addProperty("type", RegistryCondition.Condition.POD.getType());
		condition2.addProperty("type", RegistryCondition.Condition.CONTAINER.getType());
		condition.addProperty("lastTransitionTime", curTime.toString());
		condition2.addProperty("lastTransitionTime", curTime.toString());
		
		switch (eventType) {
		case Constants.EVENT_TYPE_ADDED:
		case Constants.EVENT_TYPE_MODIFIED:
			if (pod.getStatus().getContainerStatuses() != null) {
				if (pod.getStatus().getContainerStatuses().get(0).getState().getWaiting() != null) {
					logger.debug(pod.getStatus().getContainerStatuses().get(0).getState().getWaiting().toString());
					reason = pod.getStatus().getContainerStatuses().get(0).getState().getWaiting().getReason();
				} else if (pod.getStatus().getContainerStatuses().get(0).getState().getRunning() != null) {
					logger.debug(pod.getStatus().getContainerStatuses().get(0).toString());
					if (pod.getStatus().getContainerStatuses().get(0).getReady())
						reason = "Running";
					else
						reason = "NotReady";
				} else if (pod.getStatus().getContainerStatuses().get(0).getState().getTerminated() != null) {
					logger.debug(pod.getStatus().getContainerStatuses().get(0).getState().getTerminated().toString());
					reason = pod.getStatus().getContainerStatuses().get(0).getState().getTerminated().getReason();
				} else
					reason = "Unknown";
				
				if (reason == null)		reason = "";
				logger.debug("registry pod state's reason: " + reason);

				Registry registry = null;
				try {
					registry = getRegistry(registryName, namespace);
				} catch (ApiException e) {
					if(e.getCode() == 404) {
						logger.debug("[RegistryPod]" + namespace + "/" + registryName + " registry was deleted!!");
					}
					else {
						logger.error("[RegistryPod]" + e.getResponseBody());
					}
					throw e;
				}

				if (registry.getStatus().getPhase() != null) {
					if (reason.equals("NotReady")) {
						condition.addProperty("status", RegistryStatus.Status.TRUE.getStatus());
						condition2.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
					} else if (reason.equals("Running")) {
						condition.addProperty("status", RegistryStatus.Status.TRUE.getStatus());
						condition2.addProperty("status", RegistryStatus.Status.TRUE.getStatus());
					} else {
						if(reason.equals("CrashLoopBackOff")) {
							deleteRegistrySubresource(registry, RegistryCondition.Condition.POD);
						}
						condition.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
						condition.addProperty("reason", reason);
						condition2.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
					}
				}
			} else {
				logger.debug("pod container status is not set.");
				return;
			}
			break;
		case Constants.EVENT_TYPE_DELETED:
			condition.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
			condition2.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
			break;
		}

		patchStatusArray.add(Util.makePatchJsonObject("replace", 
				RegistryCondition.Condition.POD.getPath(), condition));
		patchStatusArray.add(Util.makePatchJsonObject("replace", 
				RegistryCondition.Condition.CONTAINER.getPath(), condition2));
		
		try {
			customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
					registryName, patchStatusArray);
			logger.debug("patchNamespacedCustomObjectStatus result: "
					+ condition.get("type").getAsString() + "(" + condition.get("status").getAsString() + ") / "
					+ condition2.get("type").getAsString() + "(" + condition2.get("status").getAsString() + ")\n");
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.debug("[RegistryPod]" + namespace + "/" + registryName + " registry was deleted!!");
			} else {
				logger.error("[RegistryPod]" + e.getResponseBody());
			}
			throw e;
		}
	}

	public static void updateRegistryStatus(V1Service svc, String eventType) throws Exception {
		logger.debug("[K8S ApiCaller] updateRegistryStatus(V1Service, String) Start");
		String registryName = "";
		String registryPrefix = Constants.K8S_PREFIX;
		String namespace = svc.getMetadata().getNamespace();
		boolean restoreRegistry = false;

		registryName = svc.getMetadata().getName();
		registryName = registryName.substring(registryPrefix.length());
		logger.debug("registry name: " + registryName);

		if (svc.getMetadata().getOwnerReferences() == null) {
			logger.debug(namespace + "/" + svc.getMetadata().getName() + " service ownerReference is null");
			return;
		}

		try {
			if (!isCurrentRegistry(svc.getMetadata().getOwnerReferences().get(0).getUid(), registryName, namespace, "RegistryService")) {
				logger.debug("This registry's event is not for current registry. So do not update registry status");
				return;
			}
		}catch(ApiException e) {
			if(e.getCode() == 404) {
				logger.info(namespace + "/" + svc.getMetadata().getName() + " service is deleted");
			}
			throw e;
		}

		JsonArray patchStatusArray = new JsonArray();
		DateTime curTime = new DateTime();
		JsonObject condition = new JsonObject();

		condition.addProperty("type", RegistryCondition.Condition.SERVICE.getType());
		condition.addProperty("lastTransitionTime", curTime.toString());
		
		switch (eventType) {
		case Constants.EVENT_TYPE_ADDED:
			condition.addProperty("status", RegistryStatus.Status.TRUE.getStatus());

			break;
		case Constants.EVENT_TYPE_MODIFIED:

			return;
		case Constants.EVENT_TYPE_DELETED:
			condition.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
			restoreRegistry = true;
			
			break;
		}

		patchStatusArray.add(Util.makePatchJsonObject("replace", 
				RegistryCondition.Condition.SERVICE.getPath(), condition));
		
		try {
			customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
					patchStatusArray);
			logger.debug("patchNamespacedCustomObjectStatus result: " + condition.get("type").getAsString() + "(" + condition.get("status").getAsString() + ")\n");
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.debug("[RegistryService]" + namespace + "/" + registryName + " registry was deleted!!");
			}
			else {
				logger.error("[RegistryService]" + e.getResponseBody());
			}
		}
		
		if(restoreRegistry) {
			try {
				Registry registry = getRegistry(registryName, namespace);
				createRegistryService(registry);
				if(registry.getSpec().getService().getIngress() != null) {
					deleteRegistrySubresource(registry, RegistryCondition.Condition.SECRET_TLS);
				}
				deleteRegistrySubresource(registry, RegistryCondition.Condition.SECRET_OPAQUE);
				deleteRegistrySubresource(registry, RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON);
				deleteRegistrySubresource(registry, RegistryCondition.Condition.REPLICA_SET);
			} catch (Exception e) {
				logger.error(e.getMessage());
				throw e;
			}
		}

	}

	public static void updateRegistryStatus(V1Secret secret, String eventType) throws Exception {
		logger.debug("[K8S ApiCaller] updateRegistryStatus(V1Secret, String) Start");
		String registryName = "";
		String namespace = secret.getMetadata().getNamespace();
		String registryPrefix = null;
		boolean restoreRegistry = false;
		
		if( secret.getType().equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
			registryPrefix = Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX;
		} else if (secret.getType().equals(Constants.K8S_SECRET_TYPE_TLS)) {
			registryPrefix = Constants.K8S_PREFIX + Constants.K8S_TLS_PREFIX;
		}else {
			registryPrefix = Constants.K8S_PREFIX;
		}

		logger.debug("registry secret type: " + secret.getType());
		logger.debug("registry prefix: " + registryPrefix);
		registryName = secret.getMetadata().getName().substring(registryPrefix.length());
		logger.debug("registry name: " + registryName);

		if (secret.getMetadata().getOwnerReferences() == null) {
			logger.debug(namespace + "/" + secret.getMetadata().getName() + " secret ownerReference is null");
			return;
		}
		
		try {
			if (!isCurrentRegistry(secret.getMetadata().getOwnerReferences().get(0).getUid(), registryName, namespace, "RegistrySecret")) {
				logger.debug("This registry's event is not for current registry. So do not update registry status");
				return;
			}
		}catch(ApiException e) {
			if(e.getCode() == 404) {
				logger.info(namespace + "/" + secret.getMetadata().getName() + " secret is deleted");
			}
			throw e;
		}

		JsonArray patchStatusArray = new JsonArray();
		DateTime curTime = new DateTime();
		JsonObject condition = new JsonObject();
		
		if (secret.getType().equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
			// DOCKEER CONFIG JSON TYPE SECRET
			switch (eventType) {
			case Constants.EVENT_TYPE_ADDED:
				condition.addProperty("type", RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON.getType());
				condition.addProperty("status", RegistryStatus.Status.TRUE.getStatus());
				condition.addProperty("lastTransitionTime", curTime.toString());

				break;
			case Constants.EVENT_TYPE_MODIFIED:

				return;
			case Constants.EVENT_TYPE_DELETED:
				condition.addProperty("type", RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON.getType());
				condition.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
				condition.addProperty("lastTransitionTime", curTime.toString());
				
				restoreRegistry = true;
				
				break;
			}

			patchStatusArray.add(Util.makePatchJsonObject("replace",
					RegistryCondition.Condition.SECRET_DOCKER_CONFIG_JSON.getPath(), condition));
			
		} else if (secret.getType().equals(Constants.K8S_SECRET_TYPE_TLS)) {
			// TLS TYPE SECRET
			
			// Check if Service Type is Ingress
			Registry registry = getRegistry(registryName, namespace);
			
			String serviceType 
			= registry.getSpec().getService().getIngress() != null ? 
					RegistryService.SVC_TYPE_INGRESS : RegistryService.SVC_TYPE_LOAD_BALANCER;
			
			if( !serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
				logger.debug("tls-secret: Registry service type is not ingress.");
				condition.addProperty("type", RegistryCondition.Condition.SECRET_TLS.getType());
				condition.addProperty("status", RegistryStatus.Status.UNUSED_FIELD.getStatus());
			} else {
				switch (eventType) {
				case Constants.EVENT_TYPE_ADDED:
					condition.addProperty("type", RegistryCondition.Condition.SECRET_TLS.getType());
					condition.addProperty("status", RegistryStatus.Status.TRUE.getStatus());
					condition.addProperty("lastTransitionTime", curTime.toString());
					
					break;
				case Constants.EVENT_TYPE_MODIFIED:
					
					return;
				case Constants.EVENT_TYPE_DELETED:
					condition.addProperty("type", RegistryCondition.Condition.SECRET_TLS.getType());
					condition.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
					condition.addProperty("lastTransitionTime", curTime.toString());
					restoreRegistry = true;
					
					break;
				}
			}

			patchStatusArray.add(Util.makePatchJsonObject("replace", 
					RegistryCondition.Condition.SECRET_TLS.getPath(), condition));
			
		} else {
			// OPAQUE TYPE SECRET
			switch (eventType) {
			case Constants.EVENT_TYPE_ADDED:
				condition.addProperty("type", RegistryCondition.Condition.SECRET_OPAQUE.getType());
				condition.addProperty("status", RegistryStatus.Status.TRUE.getStatus());
				condition.addProperty("lastTransitionTime", curTime.toString());

				break;
			case Constants.EVENT_TYPE_MODIFIED:

				return;
			case Constants.EVENT_TYPE_DELETED:
				condition.addProperty("type", RegistryCondition.Condition.SECRET_OPAQUE.getType());
				condition.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
				condition.addProperty("lastTransitionTime", curTime.toString());
				restoreRegistry = true;
				
				break;
			}

			patchStatusArray.add(Util.makePatchJsonObject("replace", 
					RegistryCondition.Condition.SECRET_OPAQUE.getPath(), condition));
		}
		
		try {
			customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY,
					registryName, patchStatusArray);
			logger.debug("patchNamespacedCustomObjectStatus result: " + condition.get("type").getAsString() + "(" + condition.get("status").getAsString() + ")\n");
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.debug("[RegistrySecret]" + namespace + "/" + registryName + " registry was deleted!!");
			}
			else {
				logger.error("[RegistrySecret]" + e.getResponseBody());
			}
		}
		
		if(restoreRegistry) {
			Registry registry;
			registry = getRegistry(registryName, namespace);
			
			if (secret.getType().equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
				try {
					createRegistryDcjSecret(registry);
				} catch (Exception e) {
					logger.error(e.getMessage());
					throw e;
				}
			} else if (secret.getType().equals(Constants.K8S_SECRET_TYPE_TLS)) {
				try {
					createRegistryTlsSecret(registry);
				} catch (Exception e) {
					logger.error(e.getMessage());
					throw e;
				}
			} else {
				try {
					createRegistryCertSecret(registry);
					deleteRegistrySubresource(registry, RegistryCondition.Condition.REPLICA_SET);
					if(registry.getSpec().getService().getIngress() != null) {
						deleteRegistrySubresource(registry, RegistryCondition.Condition.SECRET_TLS);
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
					throw e;
				}
			}
		}
		
	}
	
	public static void updateRegistryStatus(V1PersistentVolumeClaim pvc, String eventType) throws Exception {
		logger.debug("[K8S ApiCaller] updateRegistryStatus(V1PersistentVolumeClaim, String) Start");
		String registryName = "";
		String namespace = pvc.getMetadata().getNamespace();
		boolean restoreRegistry = false;

		registryName = pvc.getMetadata().getLabels().get("apps");
		logger.debug("registry name: " + registryName);
		String registryUid = pvc.getMetadata().getLabels().get("registryUid");
		logger.debug("registry uid: " + registryUid);

		if ( registryUid == null) {
			logger.debug(namespace + "/" + pvc.getMetadata().getName() + " pvc's registryUid label is null");
			return;
		}

		try {
			if (!isCurrentRegistry(registryUid, registryName, namespace, "RegistryPvc")) {
				logger.debug("This registry's event is not for current registry. So do not update registry status");
				return;
			}
		} catch(ApiException e) {
			if(e.getCode() == 404) {
				logger.info(namespace + "/" + pvc.getMetadata().getName() + " pvc is deleted");
			}
			throw e;
		}

		JsonArray patchStatusArray = new JsonArray();
		DateTime curTime = new DateTime();
		JsonObject condition = new JsonObject();

		switch (eventType) {
		case Constants.EVENT_TYPE_ADDED:
		case Constants.EVENT_TYPE_MODIFIED:
			if( pvc.getStatus().getPhase().equals("Bound") ) {
				condition.addProperty("type", RegistryCondition.Condition.PVC.getType());
				condition.addProperty("status", RegistryStatus.Status.TRUE.getStatus());
				condition.addProperty("lastTransitionTime", curTime.toString());
			} else {
				condition.addProperty("type", RegistryCondition.Condition.PVC.getType());
				condition.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
				condition.addProperty("reason", pvc.getStatus().getPhase());
				condition.addProperty("lastTransitionTime", curTime.toString());
			}
			
			patchStatusArray.add(Util.makePatchJsonObject("replace", RegistryCondition.Condition.PVC.getPath(), condition));	

			Registry registry = getRegistry(registryName, namespace);
			if(pvc.getStatus() != null
					&& pvc.getStatus().getCapacity() != null
					&& pvc.getStatus().getCapacity().get("storage") != null) {
				if(registry.getStatus().getCapacity() == null) {
					patchStatusArray.add(Util.makePatchJsonObject("add", "/status/capacity", pvc.getStatus().getCapacity().get("storage").toSuffixedString()));	
				} else {
					patchStatusArray.add(Util.makePatchJsonObject("replace", "/status/capacity", pvc.getStatus().getCapacity().get("storage").toSuffixedString()));	
				}
			}
			
			
			break;
		case Constants.EVENT_TYPE_DELETED:
			condition.addProperty("type", RegistryCondition.Condition.PVC.getType());
			condition.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
			condition.addProperty("lastTransitionTime", curTime.toString());

			patchStatusArray.add(Util.makePatchJsonObject("replace", RegistryCondition.Condition.PVC.getPath(), condition));
			
			restoreRegistry = true;
			
			break;
		}

		try {
			customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
					patchStatusArray);
			logger.debug("patchNamespacedCustomObjectStatus result: " + condition.get("type").getAsString() + "(" + condition.get("status").getAsString() + ")\n");
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.debug("[RegistryService]" + namespace + "/" + registryName + " registry was deleted!!");
			}
			else {
				logger.error("[RegistryService]" + e.getResponseBody());
			}
		}
		
		if(restoreRegistry) {
			try {
				Registry registry = getRegistry(registryName, namespace);
				
				deleteImage(registry);
				createRegistryPvc(registry);
				deleteRegistrySubresource(registry, RegistryCondition.Condition.REPLICA_SET);
			} catch (Exception e) {
				logger.error(e.getMessage());
				throw e;
			}
		}
	}


	public static void updateRegistryStatus(ExtensionsV1beta1Ingress ingress, String eventType) throws Exception {
		logger.debug("[K8S ApiCaller] updateRegistryStatus(ExtensionsV1beta1Ingress, String) Start");
		String registryName = "";
		String registryPrefix = Constants.K8S_PREFIX;
		String namespace = ingress.getMetadata().getNamespace();
		boolean restoreRegistry = false;

		registryName = ingress.getMetadata().getName();
		registryName = registryName.substring(registryPrefix.length());
		logger.debug("registry name: " + registryName);

		if (ingress.getMetadata().getOwnerReferences() == null) {
			logger.debug(namespace + "/" + ingress.getMetadata().getName() + " ingress ownerReference is null");
			return;
		}

		try {
			if (!isCurrentRegistry(ingress.getMetadata().getOwnerReferences().get(0).getUid(), registryName, namespace, "RegistryIngress")) {
				logger.debug("This registry's event is not for current registry. So do not update registry status");
				return;
			}
		} catch(ApiException e) {
			if(e.getCode() == 404) {
				logger.info(namespace + "/" + ingress.getMetadata().getName() + " ingress is deleted");
			}
			throw e;
		}

		JsonArray patchStatusArray = new JsonArray();
		DateTime curTime = new DateTime();
		JsonObject condition = new JsonObject();
		
		// Check if Service Type is Ingress
		Registry registry = getRegistry(registryName, namespace);

		String serviceType 
		= registry.getSpec().getService().getIngress() != null ? 
				RegistryService.SVC_TYPE_INGRESS : RegistryService.SVC_TYPE_LOAD_BALANCER;

		if( !serviceType.equals(RegistryService.SVC_TYPE_INGRESS)) {
			logger.debug("ingress: Registry service type is not ingress.");
			condition.addProperty("type", RegistryCondition.Condition.INGRESS.getType());
			condition.addProperty("status", RegistryStatus.Status.UNUSED_FIELD.getStatus());
		} else {
			switch (eventType) {
			case Constants.EVENT_TYPE_ADDED:
				condition.addProperty("type", RegistryCondition.Condition.INGRESS.getType());
				condition.addProperty("status", RegistryStatus.Status.TRUE.getStatus());
				condition.addProperty("lastTransitionTime", curTime.toString());

				break;
			case Constants.EVENT_TYPE_MODIFIED:

				return;
			case Constants.EVENT_TYPE_DELETED:
				condition.addProperty("type", RegistryCondition.Condition.INGRESS.getType());
				condition.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
				condition.addProperty("lastTransitionTime", curTime.toString());
				restoreRegistry = true;
				
				break;
			}
		}
		
		patchStatusArray.add(Util.makePatchJsonObject("replace",
				RegistryCondition.Condition.INGRESS.getPath(), condition));

		try {
			customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
					patchStatusArray);
			logger.debug("patchNamespacedCustomObjectStatus result: " + condition.get("type").getAsString() + "(" + condition.get("status").getAsString() + ")\n");
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.debug("[RegistryIngress]" + namespace + "/" + registryName + " registry was deleted!!");
			}
			else {
				logger.error("[RegistryIngress]" + e.getResponseBody());
			}
		}
		
		if(restoreRegistry) {
			try {
				createRegistryIngress(registry);
			} catch (Exception e) {
				logger.error(e.getMessage());
				throw e;
			}
		}
	}
	
	public static void updateRegistryStatus(V1ConfigMap cm, String eventType) throws Exception {
		logger.debug("[K8S ApiCaller] updateRegistryStatus(V1ConfigMap, String) Start");
		String registryName = "";
		String namespace = cm.getMetadata().getNamespace();
		boolean restoreRegistry = false;

		registryName = cm.getMetadata().getLabels().get("apps");
		logger.debug("registry name: " + registryName);
		String registryUid = cm.getMetadata().getLabels().get("registryUid");
		logger.debug("registry uid: " + registryUid);

		if ( registryUid == null) {
			logger.debug(namespace + "/" + cm.getMetadata().getName() + " configMap's registryUid label is null");
			return;
		}

		try {
			if (!isCurrentRegistry(registryUid, registryName, namespace, "RegistryCm")) {
				logger.debug("This registry's event is not for current registry. So do not update registry status");
				return;
			}
		} catch(ApiException e) {
			if(e.getCode() == 404) {
				logger.info(namespace + "/" + cm.getMetadata().getName() + " cm is deleted");
			}
			throw e;
		}

		JsonArray patchStatusArray = new JsonArray();
		DateTime curTime = new DateTime();
		JsonObject condition = new JsonObject();

		switch (eventType) {
		case Constants.EVENT_TYPE_ADDED:
		case Constants.EVENT_TYPE_MODIFIED:
			condition.addProperty("type", RegistryCondition.Condition.CONFIG_MAP.getType());
			condition.addProperty("status", RegistryStatus.Status.TRUE.getStatus());
			condition.addProperty("lastTransitionTime", curTime.toString());
			
			break;
		case Constants.EVENT_TYPE_DELETED:
			condition.addProperty("type", RegistryCondition.Condition.CONFIG_MAP.getType());
			condition.addProperty("status", RegistryStatus.Status.FALSE.getStatus());
			condition.addProperty("lastTransitionTime", curTime.toString());
			
			restoreRegistry = true;
			
			break;
		}
		
		patchStatusArray.add(Util.makePatchJsonObject("replace", 
				RegistryCondition.Condition.CONFIG_MAP.getPath(), condition));

		try {
			customObjectApi.patchNamespacedCustomObjectStatus(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName,
					patchStatusArray);
			logger.debug("patchNamespacedCustomObjectStatus result: " + condition.get("type").getAsString() + "(" + condition.get("status").getAsString() + ")\n");
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.debug("[RegistryConfigMap]" + namespace + "/" + registryName + " registry was deleted!!");
			}
			else {
				logger.error("[RegistryConfigMap]" + e.getResponseBody());
			}
		}
		
		if(restoreRegistry) {
			try {
				Registry registry = getRegistry(registryName, namespace);
				
				createRegistryConfigMap(registry);
//				deleteRegistrySubresource(registry, RegistryCondition.Condition.REPLICA_SET);
			} catch (Exception e) {
				logger.error(e.getMessage());
				throw e;
			}
		}
	}
	
	public static void deleteRegistrySubresource(Registry registry, RegistryCondition.Condition condition) {
		String registryId = registry.getMetadata().getName();
		String namespace = registry.getMetadata().getNamespace();
		
		switch(condition) {
		case REPLICA_SET:
			try {
				appApi.deleteNamespacedReplicaSet(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + registryId, namespace, null, null, null, null, null, new V1DeleteOptions());
			} catch (ApiException e) {
				logger.error(e.getResponseBody());
			}
			break;
		case POD:
			String podName = "";
			V1PodList pods = null;
			String labelSelector = "apps=" + Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + registryId;
			
			try {
				pods = api.listNamespacedPod(namespace, null, null, null, null, labelSelector, null, null, null, false);

				if (pods.getItems().size() == 0) {
					logger.debug("[label: " + labelSelector + "] RegistryPod is not found");
					return;
				}

				for (V1Pod pod : pods.getItems()) {
					podName = pod.getMetadata().getName();
					break;
				}

				api.deleteNamespacedPod(podName, namespace, null, null, null, null, null, new V1DeleteOptions());

			} catch (ApiException e) {
				logger.error(e.getResponseBody());
			}
			break;
		case SERVICE:
			try {
				api.deleteNamespacedService(Constants.K8S_PREFIX + registryId, namespace, null, null, null, null, null, new V1DeleteOptions());
			} catch (ApiException e) {
				logger.error(e.getResponseBody());
			}
			break;
		case SECRET_OPAQUE:
			try {
				api.deleteNamespacedSecret(Constants.K8S_PREFIX + registryId, namespace, null, null, null, null, null, new V1DeleteOptions());
			} catch (ApiException e) {
				logger.error(e.getResponseBody());
			}
			break;
		case SECRET_DOCKER_CONFIG_JSON:
			try {
				api.deleteNamespacedSecret(Constants.K8S_PREFIX  + Constants.K8S_REGISTRY_PREFIX + registryId, namespace, null, null, null, null, null, new V1DeleteOptions());
			} catch (ApiException e) {
				logger.error(e.getResponseBody());
			}
			break;
		case SECRET_TLS:
			try {
				api.deleteNamespacedSecret(Constants.K8S_PREFIX  + Constants.K8S_TLS_PREFIX + registryId, namespace, null, null, null, null, null, new V1DeleteOptions());
			} catch (ApiException e) {
				logger.error(e.getResponseBody());
			}
			break;
		case INGRESS:
			try {
				extentionApi.deleteNamespacedIngress(Constants.K8S_PREFIX + registryId, namespace, null, null, null, null, null, new V1DeleteOptions());
			}catch(ApiException e) {
				logger.error(e.getResponseBody());
			}
			break;
		case PVC:
//			try {
//				api.deleteNamespacedPersistentVolumeClaim(Constants.K8S_PREFIX + registryId, namespace, null, null, null, null, null, new V1DeleteOptions());
//			}catch(ApiException e) {
//				logger.error(e.getResponseBody());
//			}
			break;
		case CONFIG_MAP:
			try {
				api.deleteNamespacedConfigMap(Constants.K8S_PREFIX + registryId, namespace, null, null, null, null, null, new V1DeleteOptions());
			}catch(ApiException e) {
				logger.error(e.getResponseBody());
			}
			break;
		default:
			logger.debug("Unknown RegistryCondition");
			break;
		}
	}

	public static boolean isCurrentRegistry(String verifyUid, String registryName, String namespace, String callFrom)
			throws ApiException, IOException {
		String existRegistryUID = null;
		Object response = null;

		if (verifyUid == null) {
			logger.debug("verifyUid is null!!");
			return false;
		}

		try {
			response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName);
		} catch (ApiException e) {
			if(e.getCode() == 404) {
				logger.debug("[" + callFrom + "]" + namespace + "/" + registryName + " registry was deleted!!");
			}
			else {
				logger.error("[" + callFrom + "]" + e.getResponseBody());
			}
			throw e;
		}

		try {
			Registry existRegistry = null;

			existRegistry = mapper.readValue(gson.toJson(response), Registry.class);
			existRegistryUID = existRegistry.getMetadata().getUid();
			logger.debug("VERIFY REGISTRY UID: " + verifyUid);
			logger.debug("EXIST REGISTRY UID: " + existRegistryUID);
		} catch (JsonParseException | JsonMappingException e) {
			logger.error(e.getMessage());
			throw e;
		}

		return verifyUid.equals(existRegistryUID);
	}
	
	public static Registry getRegistry(String registryName, String namespace) throws Exception {
		Object response = null;
		try {
			response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryName);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}

		Registry existRegistry = null;
		try {
			existRegistry = mapper.readValue(gson.toJson(response), Registry.class);
		} catch (JsonParseException | JsonMappingException e) {
			logger.error(e.getMessage());
			throw e;
		}
		
		return existRegistry;
	}

	public static List<Image> getAllImageData(Registry registry) throws ApiException, Exception {
		List<Image> imageList = null;

		try {
			Object response = customObjectApi.listNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, registry.getMetadata().getNamespace(),
					Constants.CUSTOM_OBJECT_PLURAL_IMAGE, null, null, null,
					"registry=" + registry.getMetadata().getName(), null, null, null, Boolean.FALSE);

			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			// Register Joda deserialization module because of creationTimestamp of k8s
			// object
			mapper.registerModule(new JodaModule());
			imageList = mapper.readValue((new Gson()).toJson(respJson.get("items")),
					new TypeReference<ArrayList<Image>>() {
					});

		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		} catch (JsonParseException | JsonMappingException e) {
			logger.error(e.getMessage());
			throw e;
		}

		return imageList;
	}

	public static void initializeImageList() throws ApiException, Exception {
		try {
			logger.debug("initializeImageList");
			Object response = customObjectApi.listClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, null, null, null, null,
					null, null, null, Boolean.FALSE);

			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			mapper.registerModule(new JodaModule());
			List<Object> registryList = mapper.readValue((new Gson()).toJson(respJson.get("items")),
					new TypeReference<ArrayList<Object>>() {
					});

			if (registryList != null) {
				for (Object registryObj : registryList) {
					try {
						Registry registry = mapper.treeToValue(mapper.valueToTree(registryObj), Registry.class);
						
						logger.info("<-- " + registry.getMetadata().getNamespace() + "/" + registry.getMetadata().getName()
							+ " registry sync start");
					
						syncImageList(registry);
						logger.info(registry.getMetadata().getNamespace() + "/" + registry.getMetadata().getName()
								+ " registry sync end -->");
					} catch (ApiException e) {
						logger.error(e.getResponseBody());
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				}
			}
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
		} catch (JsonParseException | JsonMappingException e) {
			logger.error(e.getMessage());
//			throw e;
		}
	}

	public static void syncImageList(Registry registry) throws ApiException, Exception {
		logger.debug("[K8S ApiCaller] syncImageList(Registry) Start");
		String namespace = registry.getMetadata().getNamespace();
		SSLSocketFactory sf = null;
		Map<String, String> header = new HashMap<>();
		Map<String, String> certMap = new HashMap<>();

		try {
			
			V1Secret secretReturn = api.readNamespacedSecret(Constants.K8S_PREFIX + registry.getMetadata().getName(), namespace, null, null, null);
			Map<String, byte[]> secretMap = new HashMap<>();
			secretMap = secretReturn.getData();
			
			for (String key : secretMap.keySet()) {
//				logger.debug("secret key: " + key);
				certMap.put(key, new String(secretMap.get(key)));
			}
			
			sf = SecurityHelper.createSocketFactory(certMap.get(Constants.CERT_CRT_FILE),
					certMap.get(Constants.CERT_CERT_FILE), certMap.get(Constants.CERT_KEY_FILE));
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}

		// Set authorization Header
		String auth = certMap.get("ID") + ":" + certMap.get("PASSWD");
		String encodedAuth = new String(Base64.encodeBase64(auth.getBytes()));
		String registryIpPort = certMap.get("REGISTRY_URL");
		if ( registryIpPort == null ) {	// 이전 버전 호환
			registryIpPort = certMap.get("REGISTRY_IP_PORT");
		}
		logger.debug("[encodedAuth]" + encodedAuth);
		header.put("authorization", "Basic " + encodedAuth);

		logger.debug("Image Registry [ " + registryIpPort + "] : Get Current Image List from Repository ");
		String imagelistResponse = httpsCommander(sf, header, null, registryIpPort, "v2/_catalog?n=10000", null);

		JsonObject imagelistJson = (JsonObject) new JsonParser().parse(imagelistResponse.toString());
		JsonArray repositories = imagelistJson.getAsJsonArray("repositories");
		List<String> imageList = new ArrayList<>();
		if (repositories != null) {
			for (JsonElement imageElement : repositories) {
				String imageName = imageElement.toString();
				imageName = imageName.replaceAll("\"", "");
				logger.debug("Repository Image : " + imageName);
				imageList.add(imageName);
			}
		}

		logger.debug("Previous Image List");
		List<Image> imageListDB = getAllImageData(registry);
		List<String> imageNameListDB = null;
		if (imageListDB != null) {
			for (Image imageDB : imageListDB) {
				if (imageNameListDB == null) {
					imageNameListDB = new ArrayList<>();
				}
				imageNameListDB.add(imageDB.getSpec().getName());
			}
		}

		logger.debug("Comparing ImageName and get New Images Name & Deleted Images Name");
		List<String> newImageList = new ArrayList<>();
		List<String> deletedImageList = new ArrayList<>();

		for (String imageName : imageList) {
			if (imageNameListDB == null) {
				imageNameListDB = new ArrayList<>();
			}
			if (!imageNameListDB.contains(imageName)) {
				if (newImageList == null) {
					newImageList = new ArrayList<>();
				}
				newImageList.add(imageName);
				logger.debug("new Image : " + imageName);
			}
		}

		if (imageNameListDB != null) {
			for (String imageName : imageNameListDB) {
				if (imageList == null) {
					imageList = new ArrayList<>();
				}
				if (!imageList.contains(imageName)) {
					if (deletedImageList == null) {
						deletedImageList = new ArrayList<>();
					}
					deletedImageList.add(imageName);
					logger.debug("deleted Image : " + imageName);
				}
			}
		}

		if (newImageList != null && newImageList.size() > 0) {
			logger.debug("For New Image, Insert Image and Versions Data from Repository");
			for (String newImage : newImageList) {
				logger.debug(
						"Image Registry [ " + registryIpPort + "] : Get Current Image [" + newImage + "] tags List ");
				String tagslistResponse = null;

				try {
					tagslistResponse = K8sApiCaller.httpsCommander(sf, header, null, registryIpPort,
							"v2/" + newImage + "/tags/list?n=10000", null);
					JsonObject tagsListJson = (JsonObject) new JsonParser().parse(tagslistResponse.toString());
					JsonArray tags = null;

					if (tagsListJson.get("errors") == null) {
						if (!tagsListJson.get("tags").isJsonNull()) {
							tags = tagsListJson.getAsJsonArray("tags");
						}
					} else {
						continue;
					}

					List<String> tagsList = null;

					if (tags != null) {
						tagsList = new ArrayList<>();
						for (JsonElement tagElement : tags) {
							String tag = tagElement.toString();
							tag = tag.replaceAll("\"", "");
							tagsList.add(tag);
						}
					}
					if (tagsList != null && tagsList.size() > 0) {
						createImage(registry, newImage, tagsList);
						logger.debug("Insert Image and Versions Data from Repository Success!");
					}
				} catch(Exception e) {}
			}
		}

		if (imageListDB != null && imageListDB.size() > 0) {
			logger.debug("For Exist Image, Compare tags List, Insert Version Data from Repository");
			for (Image imageDB : imageListDB) {

				List<String> tagsListDB = null;
				for (String imageVersion : imageDB.getSpec().getVersions()) {
					if (tagsListDB == null) {
						tagsListDB = new ArrayList<>();
					}
					tagsListDB.add(imageVersion);
				}

				String tagslistResponse = null;
				logger.debug("Image Registry [ " + registryIpPort + "] : Get Current Image ["
						+ imageDB.getSpec().getName() + "] tags List ");

				try {
					tagslistResponse = K8sApiCaller.httpsCommander(sf, header, null, registryIpPort,
							"v2/" + imageDB.getSpec().getName() + "/tags/list?n=10000", null);

					JsonObject tagsListJson = (JsonObject) new JsonParser().parse(tagslistResponse.toString());

					JsonArray tags = null;

					if (tagsListJson.get("errors") == null) {
						if (!tagsListJson.get("tags").isJsonNull()) {
							tags = tagsListJson.getAsJsonArray("tags");
						}
					} else {
						continue;
					}

					List<String> tagsList = null;

					if (tags != null) {
						tagsList = new ArrayList<>();
						for (JsonElement tagElement : tags) {
							String tag = tagElement.toString();
							tag = tag.replaceAll("\"", "");
							tagsList.add(tag);
						}
					}

					//				logger.debug("Comparing Tags and get New Tag");
					List<String> newTagsList = null;
					List<String> deletedTagsList = null;
					if (tagsList != null && tagsList.size() > 0) {
						for (String tag : tagsList) {
							if (tagsListDB != null && !tagsListDB.contains(tag)) {
								if (newTagsList == null) {
									newTagsList = new ArrayList<>();
								}
								newTagsList.add(tag);
								logger.debug("Image Name [" + imageDB.getSpec().getName() + "] New Tag : " + tag);
							}
						}
					}

					if (tagsListDB != null && tagsListDB.size() > 0) {
						for (String tag : tagsListDB) {
							if (tagsList != null && !tagsList.contains(tag)) {
								if (deletedTagsList == null) {
									deletedTagsList = new ArrayList<>();
								}
								deletedTagsList.add(tag);
								logger.debug("Image Name [" + imageDB.getSpec().getName() + "] Deleted Tag : " + tag);
							}
						}
					}

					if (newTagsList != null) {
						addImageVersions(registry, imageDB.getSpec().getName(), newTagsList);
						logger.debug("Insert Versions Data from Repository Success!");
					}

					if (deletedTagsList != null) {
						deleteImageVersions(registry, imageDB.getSpec().getName(), deletedTagsList);
						logger.debug("Delete Versions Data from Repository Success!");
					}
				} catch(Exception e) {}
			}
		}

		if (deletedImageList != null && deletedImageList.size() > 0) {
			logger.debug("For Deleted Image, Delete Image Data from Repository");
			for (String deletedImage : deletedImageList) {
				deleteImage(registry, deletedImage);
				logger.debug("Delete Image Data from Repository Success!");
			}
		}

	}

	public static void deleteImageVersions(Registry registry, String imageName, List<String> tagsList)
			throws ApiException {
		logger.debug("[K8S ApiCaller] deleteImageVersions(Registry, String, List<String>) Start");

		String namespace = registry.getMetadata().getNamespace();
		String imageRegistry = registry.getMetadata().getName();
		Image image = new Image();

		logger.debug("imageName: " + imageName);
		logger.debug("imageRegistry: " + imageRegistry);

		String imageCRName = Util.parseImageName(imageName) + "." + imageRegistry;

		try {
			Object response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName);

			image = mapper.readValue(gson.toJson(response), Image.class);

			logger.debug("IMAGE RESOURCE VERSION: " + image.getMetadata().getResourceVersion());
			logger.debug("IMAGE UID: " + image.getMetadata().getUid());
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		try {
			JsonArray patchArray = new JsonArray();
			JsonArray versions = new JsonArray();

			for (String version : image.getSpec().getVersions()) {
				logger.debug("Exist Image Version: " + version);

				if (!tagsList.contains(version)) {
					versions.add(version);
				} else {
					logger.debug("Deleted Image Version: " + version);
				}
			}

			patchArray.add(Util.makePatchJsonObject("replace", "/spec/versions", versions));

			try {
				Object result = customObjectApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
						Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName,
						patchArray);
				logger.debug("patchNamespacedCustomObject result: " + result.toString() + "\n");
			} catch (ApiException e) {
				logger.error(e.getResponseBody());
				throw e;
			}

		} catch (ApiException e) {
			logger.error(e.getResponseBody());
		}
	}

	public static void addImageVersions(Registry registry, String imageName, List<String> tagsList)
			throws ApiException {
		logger.debug("[K8S ApiCaller] addImageVersions(Registry, String, List<String>) Start");

		String namespace = registry.getMetadata().getNamespace();
		String imageRegistry = registry.getMetadata().getName();
		Image image = new Image();

		logger.debug("imageName: " + imageName);
		logger.debug("imageRegistry: " + imageRegistry);

		String imageCRName = Util.parseImageName(imageName) + "." + imageRegistry;

		try {
			Object response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName);

			image = mapper.readValue(gson.toJson(response), Image.class);

			logger.debug("IMAGE RESOURCE VERSION: " + image.getMetadata().getResourceVersion());
			logger.debug("IMAGE UID: " + image.getMetadata().getUid());
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		JsonArray patchArray = new JsonArray();
		JsonArray versions = new JsonArray();
		Set<String> versionSet = new HashSet<>();

		for (String version : image.getSpec().getVersions()) {
			logger.debug("Exist Image Version: " + version);
			versionSet.add(version);
		}

		for (String version : tagsList) {
			logger.debug("New Image Version: " + version);
			versionSet.add(version);
		}

		Iterator<String> iter = versionSet.iterator();
		while (iter.hasNext()) {
			String version = iter.next();
			versions.add(version);
		}

		patchArray.add(Util.makePatchJsonObject("replace", "/spec/versions", versions));

		try {
			Object result = customObjectApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName,
					patchArray);
			logger.debug("patchNamespacedCustomObject result: " + result.toString() + "\n");
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
		}

	}

	public static void deleteImage(Registry registry, String imageName) throws ApiException {
		logger.debug("[K8S ApiCaller] deleteImage(Registry, String, List<String>) Start");
		String namespace = registry.getMetadata().getNamespace();
		String imageRegistry = registry.getMetadata().getName();

		logger.debug("imageName: " + imageName);
		logger.debug("imageRegistry: " + imageRegistry);

		String imageCRName = Util.parseImageName(imageName) + "." + imageRegistry;
		try {
			Object result = customObjectApi.deleteNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName,
					0, null, null, null);
			logger.debug("deleteNamespacedCustomObject result: " + result.toString() + "\n");
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
		}
	}
	
	public static void deleteImage(Registry registry) throws ApiException {
		logger.debug("[K8S ApiCaller] deleteImage(Registry, String, List<String>) Start");
		String namespace = registry.getMetadata().getNamespace();
		String imageRegistry = registry.getMetadata().getName();

		logger.debug("imageRegistry: " + imageRegistry);

		try {
			Object response = customObjectApi.listNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, null, null, null, 
					"registry=" + imageRegistry, null, null, null, Boolean.FALSE);
			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			// Register Joda deserialization module because of creationTimestamp of k8s
			// object
			mapper.registerModule(new JodaModule());
			List<Image> imageList = mapper.readValue((new Gson()).toJson(respJson.get("items")),
					new TypeReference<ArrayList<Image>>() {
			});

			for(Image image : imageList) {
				String imageCRName = image.getMetadata().getName();
				try {
					Object result = customObjectApi.deleteNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
							Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName,
							0, null, null, null);
					logger.debug(imageCRName + " image is deleted!!");
				} catch (ApiException e) {
					logger.error("customObjectApi.deleteNamespacedCustomObject API error: " + e.getResponseBody());
				}
			}
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public static void createImage(Registry registry, String imageName, List<String> tagsList) throws ApiException {
		logger.debug("[K8S ApiCaller] createImage(Registry, String, List<String>) Start");

		String namespace = registry.getMetadata().getNamespace();
		String imageRegistry = registry.getMetadata().getName();
		Image image = null;

		try {
			image = new Image();
			String imageCRName = Util.parseImageName(imageName) + "." + imageRegistry;
			V1ObjectMeta metadata = new V1ObjectMeta();
			metadata.setName(imageCRName);
			metadata.setNamespace(namespace);

			Map<String, String> labels = new HashMap<>();
			labels.put("registry", imageRegistry);
			metadata.setLabels(labels);

			List<V1OwnerReference> ownerRefs = new ArrayList<>();
			V1OwnerReference ownerRef = new V1OwnerReference();

			ownerRef.setApiVersion(registry.getApiVersion());
			ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
			ownerRef.setController(Boolean.TRUE);
			ownerRef.setKind(registry.getKind());
			ownerRef.setName(registry.getMetadata().getName());
			ownerRef.setUid(registry.getMetadata().getUid());
			ownerRefs.add(ownerRef);

			metadata.setOwnerReferences(ownerRefs);
			image.setMetadata(metadata);

			ImageSpec spec = new ImageSpec();
			List<String> versions = new ArrayList<>();
			for (String imageVersion : tagsList) {
				versions.add(imageVersion);
			}

			spec.setName(imageName);
			spec.setVersions(versions);
			spec.setRegistry(imageRegistry);
			image.setSpec(spec);

			Object result = customObjectApi.createNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, image, null);
			logger.debug("createNamespacedCustomObject result: " + result.toString() + "\n");
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
		}

	}

	public static void createImage(RegistryEvent event) throws ApiException, IOException {
		logger.debug("[K8S ApiCaller] createImage(RegistryEvent) Start");
		boolean imageExist = true;

		Registry registry = getRegistry(event);
		String namespace = registry.getMetadata().getNamespace();

		Image image = null;
		String imageName = event.getTarget().getRepository();
		String imageVersion = event.getTarget().getTag();
		String imageRegistry = registry.getMetadata().getName();

		logger.debug("imageName: " + imageName);
		logger.debug("imageVersion: " + imageVersion);
		logger.debug("imageRegistry: " + imageRegistry);

		String imageCRName = Util.parseImageName(imageName) + "." + imageRegistry;

		try {
			Object response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName);

			image = mapper.readValue(gson.toJson(response), Image.class);

			logger.debug("IMAGE RESOURCE VERSION: " + image.getMetadata().getResourceVersion());
			logger.debug("IMAGE UID: " + image.getMetadata().getUid());
			imageExist = true;
		} catch (ApiException e) {
			if(e.getCode() != 404)
				logger.error(e.getResponseBody());
			else 
				logger.error(imageName + " image is not exist.");
			imageExist = false;
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw e;
		}

		if (imageExist) {
			try {
				JsonArray patchArray = new JsonArray();
				JsonObject patchContent = new JsonObject();
				JsonArray versions = new JsonArray();
				Set<String> versionSet = new HashSet<>();

				for (String version : image.getSpec().getVersions()) {
					logger.debug("Exist Image Version: " + version);
					versionSet.add(version);
				}

				logger.debug("New Image Version: " + imageVersion);
				versionSet.add(imageVersion);

				Iterator<String> iter = versionSet.iterator();
				while (iter.hasNext()) {
					String version = iter.next();
					versions.add(version);
				}

				patchArray.add(Util.makePatchJsonObject("replace", "/spec/versions", versions));

				try {
					Object result = customObjectApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
							Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE,
							imageCRName, patchArray);
					logger.debug("patchNamespacedCustomObject result: " + result.toString() + "\n");
				} catch (ApiException e) {
					logger.error(e.getResponseBody());
					throw e;
				}

				imageExist = true;
			} catch (ApiException e) {
				logger.error(e.getResponseBody());
			}
		} else {
			try {
				image = new Image();
				V1ObjectMeta metadata = new V1ObjectMeta();
				metadata.setName(imageCRName);
				metadata.setNamespace(namespace);

				Map<String, String> labels = new HashMap<>();
				labels.put("registry", imageRegistry);
				metadata.setLabels(labels);

				List<V1OwnerReference> ownerRefs = new ArrayList<>();
				V1OwnerReference ownerRef = new V1OwnerReference();

				ownerRef.setApiVersion(registry.getApiVersion());
				ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
				ownerRef.setController(Boolean.TRUE);
				ownerRef.setKind(registry.getKind());
				ownerRef.setName(registry.getMetadata().getName());
				ownerRef.setUid(registry.getMetadata().getUid());
				ownerRefs.add(ownerRef);

				metadata.setOwnerReferences(ownerRefs);

				image.setMetadata(metadata);

				ImageSpec spec = new ImageSpec();
				List<String> versions = new ArrayList<>();
				versions.add(imageVersion);
				spec.setName(imageName);
				spec.setVersions(versions);
				spec.setRegistry(imageRegistry);
				image.setSpec(spec);

				Object result = customObjectApi.createNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
						Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, image, null);
				
				logger.debug("createNamespacedCustomObject result: " + result.toString() + "\n");
			} catch (ApiException e) {
				logger.error(e.getResponseBody());
			}
		}

	}

	public static void deleteImage(RegistryEvent event) throws ApiException {
		logger.debug("[K8S ApiCaller] deleteImage(RegistryEvent) Start");

		Registry registry = getRegistry(event);
		String namespace = registry.getMetadata().getNamespace();

		String imageName = event.getTarget().getRepository();
		String imageVersion = event.getTarget().getTag();
		String imageRegistry = registry.getMetadata().getName();

		logger.debug("imageName: " + imageName);
		logger.debug("imageVersion: " + imageVersion);
		logger.debug("imageRegistry: " + imageRegistry);

		String imageCRName = Util.parseImageName(imageName) + "." + imageRegistry;

		try {
			Object result = customObjectApi.deleteNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_IMAGE, imageCRName,
					0, null, null, null);
			logger.debug("deleteNamespacedCustomObject result: " + result.toString() + "\n");

		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}

	public static Registry getRegistry(RegistryEvent event) throws ApiException {
		Registry registry = null;
		String namespace = null;
		String registryId = null;

		try {
			/*
			 * /events/0/source/addr: "hpcd-registry-t2-registry-46nfk:443"
			 */
			V1PodList pods = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, Boolean.FALSE);
			String searchName = null;

			if (event.getSource() == null || event.getSource().getAddr() == null) {
				return null;
			}

			searchName = event.getSource().getAddr().split(":")[0];
			logger.debug("RegistrySearchName: " + searchName);

			for (V1Pod pod : pods.getItems()) {
				if (pod.getMetadata().getName().equals(searchName)) {
					namespace = pod.getMetadata().getNamespace();
					logger.debug("RegistryNamespace: " + namespace);

					String registryPrefix = Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX;
					registryId = pod.getMetadata().getLabels().get("apps").substring(registryPrefix.length());

					break;
				}
			}

		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}

		try {
			Object response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, Constants.CUSTOM_OBJECT_PLURAL_REGISTRY, registryId);

			registry = mapper.readValue(gson.toJson(response), Registry.class);

			logger.debug("REGISTRY RESOURCE VERSION: " + registry.getMetadata().getResourceVersion());
			logger.debug("REGISTRY UID: " + registry.getMetadata().getUid());

		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return registry;
	}

	public static CommandExecOut commandExecute(String[] command) throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		CommandExecOut cmdOutDo = new CommandExecOut();

		// command exec.
		logger.debug("command: " + Arrays.asList(command));

		processBuilder.command(command);

		try {
			Process process = processBuilder.start();

			ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
			ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
			Thread outThread = new Thread(new Runnable() {
				public void run() {
					try {
						ByteStreams.copy(process.getInputStream(), baosOut);
					} catch (IOException ex) {
						ex.printStackTrace();
						logger.error(ex.getMessage());

						StringWriter sw = new StringWriter();
						ex.printStackTrace(new PrintWriter(sw));
						logger.error(sw.toString());
					} finally {
						logger.debug("out end");
					}
				}
			});
			outThread.start();

			Thread errThread = new Thread(new Runnable() {
				public void run() {
					try {
						ByteStreams.copy(process.getErrorStream(), baosErr);
					} catch (IOException ex) {
						ex.printStackTrace();
						logger.error(ex.getMessage());

						StringWriter sw = new StringWriter();
						ex.printStackTrace(new PrintWriter(sw));
						logger.error(sw.toString());
					} finally {
						logger.debug("err end");
					}
				}
			});
			errThread.start();

			// wait for current thread and return immediately if the subprocess has already
			// terminated.
			process.waitFor();

			// wait for any last output;
			outThread.join();
			errThread.join();

			process.getOutputStream().close();
			process.getErrorStream().close();
			process.getInputStream().close();

			// exit process
			process.destroy();

			String outString = new String(baosOut.toByteArray(), Charset.forName("UTF-8"));
			String errString = new String(baosErr.toByteArray(), Charset.forName("UTF-8"));

			logger.debug("out: " + outString);
			logger.debug("err: " + errString);

			cmdOutDo.setCmdStdOut(outString);
			cmdOutDo.setCmdStdErr(errString);

			logger.debug("exit code: " + process.exitValue());
			cmdOutDo.setCmdExitCode(process.exitValue());

		} catch (IOException e) {
			logger.error(e.getMessage());

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			throw e;
		} catch (InterruptedException e) {
			logger.error(e.getMessage());

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			throw e;
		}
		return cmdOutDo;
	}

	// type1: null => Opaque
	// type2: kubernetes.io/dockerconfigjson
	public static String createSecret(String namespace, Map<String, String> secrets, String secretName,
			Map<String, String> labels, String type, List<V1OwnerReference> ownerRefs) throws ApiException {
		logger.debug("[K8S ApiCaller] createSecret Service Start");

		V1Secret secret = new V1Secret();
		secret.setApiVersion("v1");
		secret.setKind("Secret");
		V1ObjectMeta metadata = new V1ObjectMeta();

		if (type != null && type.equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
			metadata.setName(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + secretName.toLowerCase());
		} else if( type != null && type.equals(Constants.K8S_SECRET_TYPE_TLS)) {
			metadata.setName(Constants.K8S_PREFIX + Constants.K8S_TLS_PREFIX + secretName.toLowerCase());
		}
		else {
			metadata.setName(Constants.K8S_PREFIX + secretName.toLowerCase());
		}

		if (ownerRefs != null) {
			metadata.setOwnerReferences(ownerRefs);
		}

		// logger.debug("== secret map == ");
		for (String key : secrets.keySet()) {
			// logger.debug("[secretMap]" + key + "=" + secrets.get(key));
			secret.putStringDataItem(key, secrets.get(key));
			// secret.putDataItem(key, secrets.get(key).getBytes(StandardCharsets.UTF_8));
		}

		// 2-2-1-1. pod label
		logger.debug("<Pod Label List>");
		Map<String, String> podLabels = new HashMap<String, String>();

		if (labels == null) {
			podLabels.put("secret", "obj");
			podLabels.put("apps", Constants.K8S_PREFIX + secretName);
			logger.debug("secret: obj");
			logger.debug("apps: " + Constants.K8S_PREFIX + secretName);
		} else {
			for (String key : labels.keySet()) {
				podLabels.put(key, labels.get(key));
			}
		}

		metadata.setLabels(podLabels);
		secret.setMetadata(metadata);
		if (type != null) {
			if (type.equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
				secret.setType(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON);
			}
			else if (type.equals(Constants.K8S_SECRET_TYPE_TLS)) {
				secret.setType(Constants.K8S_SECRET_TYPE_TLS);
			}
		}

		try {
			V1Secret result;
			Map<String, byte[]> secretMap = new HashMap<>();
			result = api.createNamespacedSecret(namespace, secret, "true", null, null);

			logger.debug("Secret is created: " + result.getMetadata().getNamespace() + "/" + result.getMetadata().getName());
			logger.debug("\tKey:" + result.getData().keySet() + "\n");
			// V1Secret secretRet = api.readNamespacedSecret(Constants.K8S_PREFIX +
			// secretName.toLowerCase(), Constants.K8S_PREFIX + domainId.toLowerCase(),
			// null, null, null);

			secretMap = result.getData();
			// logger.debug("== real secret data ==");
//			for( String key : secretMap.keySet()) {
//				//					logger.debug("[secret]" + key + "=" + new String(secretMap.get(key))); 
//			}

		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}

		return secret.getMetadata().getName();
	}
	
	public static void patchSecret(String namespace, Map<String, String> secrets, String secretName, Map<String, String> labels) throws Throwable {
		logger.debug("[K8S ApiCaller] patchSecret Service Start");

		V1Secret result;
		
		for( String key : secrets.keySet()) {
			String dataStr = secrets.get(key);
			byte[] encodeData = Base64.encodeBase64(dataStr.getBytes());
			String jsonPatchStr = "[{\"op\":\"replace\",\"path\":\"/data/" + key + "\",\"value\": \"" + new String(encodeData) + "\" }]";
			logger.debug("JsonPatchStr: " + jsonPatchStr);

			JsonElement jsonPatch = (JsonElement) new JsonParser().parse(jsonPatchStr);
			try {
				Map<String, byte[]> secretMap = new HashMap<>();
				result = api.patchNamespacedSecret(Constants.K8S_PREFIX + secretName.toLowerCase(), namespace, new V1Patch(jsonPatch.toString()), "true", null, null, null);
//				logger.debug("[result]" + result);
				
//				secretMap = result.getData();
//				logger.debug("== real secret data ==");
//				for( String key2 : secretMap.keySet()) {
//					logger.debug("[secret]" + key2 + "=" + new String(secretMap.get(key2)));
//				}
			
			} catch (ApiException e) {
				logger.error(e.getResponseBody());
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.error(sw.toString());
				throw e;
			}
		}
		
	}

	public static void deleteSecret(String namespace, String secretName, String type) throws ApiException {
		secretName = secretName.toLowerCase();

		try {
			if (type != null && type.equals(Constants.K8S_SECRET_TYPE_DOCKER_CONFIG_JSON)) {
				api.deleteNamespacedSecret(Constants.K8S_PREFIX + Constants.K8S_REGISTRY_PREFIX + secretName, namespace,
						null, null, 0, null, null, new V1DeleteOptions());

			} else {
				api.deleteNamespacedSecret(Constants.K8S_PREFIX + secretName, namespace, null, null, 0, null, null,
						new V1DeleteOptions());
			}
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}

	}

	public static V1Secret readSecret(String namespace, String secretName) throws ApiException {
		logger.debug(" [k8sApiCaller] Read Secret Service Start ");
		V1Secret secretReturn = null;
		try {
			logger.debug(" secretName.toLowerCase() :  " + secretName.toLowerCase());
			secretReturn = api.readNamespacedSecret(secretName.toLowerCase(), namespace, null, null, null);

		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
		return secretReturn;
	}

	private static String readFile(String filePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		StringBuilder stringBuilder = new StringBuilder();
		char[] buffer = new char[1];

		while (reader.read(buffer) != -1) {
			stringBuilder.append(new String(buffer));
			buffer = new char[1];
		}

		reader.close();

		String content = stringBuilder.toString();
		logger.debug("[Read " + filePath + "]"
//				+ ":" + content
				);

		return content;
	}

	private static String createConfigJson(List<String> domainList, String id, String password) {
		// $(echo "{\"auths\": {\"192.168.6.218:443\": {\"auth\": \"$(echo
		// "tmax:tmax123" | base64)\"}}}" | base64)
		StringBuilder configSb = new StringBuilder();
		String auth = id + ":" + password;
		
		configSb.append("{\"auths\": {");
		int i=0;
		for(String domain : domainList) {
			if(i++ > 0) {
				configSb.append(", ");
			}
			
			configSb.append("\"" + domain + "\": ");
			configSb.append("{");
			configSb.append("\"auth\": \""+ new String( Base64.encodeBase64( auth.getBytes() ) ) + "\"");
			configSb.append("}");
		}
		configSb.append("}}");

		return configSb.toString();
//		return new String( Base64.encodeBase64( configSb.toString().getBytes() ) );
	}

	private static String createDirectory(String domainId, String registryId) throws IOException {
		Path opensslHome = Paths.get(Constants.OPENSSL_HOME_DIR);
		if (!Files.exists(opensslHome)) {
			Files.createDirectory(opensslHome);
			logger.debug("Directory created: " + Constants.OPENSSL_HOME_DIR);
		}

		String domainDir = Constants.OPENSSL_HOME_DIR + "/" + domainId;
		if (!Files.exists(Paths.get(domainDir))) {
			Files.createDirectory(Paths.get(domainDir));
			logger.debug("Directory created: " + domainDir);
		}

		String registryDir = Constants.OPENSSL_HOME_DIR + "/" + domainId + "/" + registryId;
		if (!Files.exists(Paths.get(registryDir))) {
			Files.createDirectory(Paths.get(registryDir));
			logger.debug("Directory created: " + registryDir);
		}

		Path dockerLoginHome = Paths.get(Constants.DOCKER_LOGIN_HOME_DIR);
		if (!Files.exists(dockerLoginHome)) {
			Files.createDirectory(dockerLoginHome);
			logger.debug("Directory created: " + Constants.DOCKER_LOGIN_HOME_DIR);
		}

		return registryDir;
	}

	public static Services getCatalog() throws ApiException {
		Services catalog = new Services();
		List<ServiceOffering> serviceList = new ArrayList<ServiceOffering>();
		String catalogNamespace = Constants.DEFAULT_NAMESPACE;
		if (System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE) != null
				&& !System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE).isEmpty()) {
			catalogNamespace = System.getenv(Constants.SYSTEM_ENV_CATALOG_NAMESPACE);
		}
		Object templates = customObjectApi.listNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
				Constants.CUSTOM_OBJECT_VERSION, catalogNamespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, null, null,
				null, null, null, null, null, false);

		JsonNode templateList = numberTypeConverter(objectToJsonNode(templates).get("items"));


		if (templateList.isArray()) {
			for (JsonNode template : templateList) {
				ServiceOffering service = new ServiceOffering();
				ServiceMetadata serviceMeta = new ServiceMetadata();
				List<ServicePlan> planList = new ArrayList<ServicePlan>();

				service.setName(template.get("metadata").get("name").asText());
				service.setId(template.get("metadata").get("name").asText());

				if (template.get("shortDescription") == null) {
					service.setDescription(template.get("metadata").get("name").asText());
				} else {
					service.setDescription(template.get("shortDescription").asText());
				}
				if (template.get("imageUrl") == null) {
					serviceMeta.setImageUrl(Constants.DEFAULT_IMAGE_URL);
				} else {
					serviceMeta.setImageUrl(template.get("imageUrl").asText());
				}
				if (template.get("longDescription") == null) {
					serviceMeta.setLongDescription(template.get("metadata").get("name").asText());
				} else {
					serviceMeta.setLongDescription(template.get("longDescription").asText());
				}
				if (template.get("longDescription") == null) {
					serviceMeta.setLongDescription(template.get("metadata").get("name").asText());
				} else {
					serviceMeta.setLongDescription(template.get("longDescription").asText());
				}
				if (template.get("urlDescription") == null) {
					serviceMeta.setUrlDescription(template.get("metadata").get("name").asText());
				} else {
					serviceMeta.setUrlDescription(template.get("urlDescription").asText());
				}
				if (template.get("markdownDescription") == null) {
					serviceMeta.setMarkdownDescription(template.get("metadata").get("name").asText());
				} else {
					serviceMeta.setMarkdownDescription(template.get("markdownDescription").asText());
				}
				if (template.get("provider") == null) {
					serviceMeta.setProviderDisplayName(Constants.DEFAULT_PROVIDER);
				} else {
					serviceMeta.setProviderDisplayName(template.get("provider").asText());
				}
				if (template.get("recommend") != null) {
					serviceMeta.setRecommend(template.get("recommend").asBoolean());
				} else {
					serviceMeta.setRecommend(false);
				}

				List<String> tags = new ArrayList<String>();
				if (template.get("tags") != null) {
					for (JsonNode tag : template.get("tags")) {
						tags.add(tag.asText());
					}
				} else {
					tags.add(Constants.DEFAULT_TAGS);
				}
				service.setTags(tags);
				service.setMetadata(serviceMeta);


				if (template.get("objectKinds") != null) {
					JsonNode objectKinds = template.get("objectKinds");
					if (objectKinds.isArray()) {
						List<String> kinds = null;
						ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {
						});
						try {
							kinds = reader.readValue(objectKinds);
						} catch (IOException e) {
							logger.error(e.getMessage());
							;
						}

						if (kinds.contains("Secret") || kinds.contains("Service (LoadBalancer)")) {
							service.setBindable(true);
						}
					}
				} else {
					service.setBindable(false);
				}
				service.setBindings_retrievable(false);
				service.setInstances_retrievable(false);


				try {
					if (template.get("plans") != null) {
						JsonNode plans = template.get("plans");
						if (plans.isArray()) {
							int defaultPlaneId = 1;
							for (JsonNode plan : plans) {
								try {
									ServicePlan servicePlan = new ServicePlan();
									PlanMetadata planMeta = new PlanMetadata();
									List<String> bullets = new ArrayList<String>();
									Cost planCost = new Cost();
									Schemas planSchema = new Schemas();
									ServiceInstanceSchema instanceSchema = new ServiceInstanceSchema();
									InputParametersSchema create = new InputParametersSchema();
									Map<String, String> parameters = null;

									String uuid = UUID.randomUUID().toString();
									servicePlan.setId(uuid);
									if (plan.get("name") == null) {
										servicePlan.setName(template.get("metadata").get("name").asText() + "-plan"
												+ defaultPlaneId);
									} else {
										servicePlan.setName(plan.get("name").asText());
									}
									if (plan.get("description") == null) {
										servicePlan.setDescription(template.get("metadata").get("name").asText()
												+ "-plan" + defaultPlaneId);
									} else {
										servicePlan.setDescription(plan.get("description").asText());
									}
									if (plan.get("bindable") == null) {
										servicePlan.setBindable(false);
									} else {
										servicePlan.setBindable(plan.get("bindable").asBoolean());
									}
									defaultPlaneId++;


									try {
										if ( plan.get("metadata") != null ) {
											if (plan.get("metadata").get("bullets") != null) {
												for (JsonNode bullet : plan.get("metadata").get("bullets")) {
													bullets.add(bullet.asText());
												}
												planMeta.setBullets(bullets);
											}

											planCost.setAmount(plan.get("metadata").get("costs").get("amount").asText());
											planCost.setUnit(plan.get("metadata").get("costs").get("unit").asText());
											planMeta.setCosts(planCost);
										}
										servicePlan.setMetadata(planMeta);
										
										parameters = mapper
												.convertValue(
														plan.get("schemas").get("service_instance").get("create")
																.get("parameters"),
														new TypeReference<Map<String, String>>() {
														});
										create.setParameters(parameters);
									} catch (Exception e) {
										logger.error("This Plan is Error1");
									}

									instanceSchema.setCreate(create);
									planSchema.setService_instance(instanceSchema);
									servicePlan.setSchemas(planSchema);
									planList.add(servicePlan);
								} catch (Exception e) {
									logger.error("This Plan is Error2");
								}
							}
						}
					} else {
						ServicePlan servicePlan = new ServicePlan();
						servicePlan.setId(template.get("metadata").get("name").asText() + "-plan-default");
						servicePlan.setName(template.get("metadata").get("name").asText() + "-plan-default");
						servicePlan.setDescription(template.get("metadata").get("name").asText() + "-plan-default");
						servicePlan.setBindable(false);
						planList.add(servicePlan);
					}
				} catch (Exception e) {
					logger.error("This Plan is Empty");
				}

				service.setPlans(planList);
				serviceList.add(service);
			}
			catalog.setServices(serviceList);
		}
		return catalog;
	}

	public static Object createTemplateInstance(String instanceId, ProvisionInDO inDO, String instanceName,
			String instanceUid) throws Exception {
		Object response = null;
		TemplateInstance instance = new TemplateInstance();
		Metadata instanceMeta = new Metadata();
		Metadata templateMeta = new Metadata();
		TemplateInstanceSpec spec = new TemplateInstanceSpec();
		TemplateInstanceSpecTemplate template = new TemplateInstanceSpecTemplate();
		List<TemplateParameter> parameters = new ArrayList<TemplateParameter>();

		List<V1OwnerReference> ownerRefs = new ArrayList<>();
		V1OwnerReference ownerRef = new V1OwnerReference();

		ownerRef.setApiVersion(Constants.SERVICE_INSTANCE_API_VERSION);
		ownerRef.setBlockOwnerDeletion(Boolean.TRUE);
		ownerRef.setController(Boolean.TRUE);
		ownerRef.setKind(Constants.SERVICE_INSTANCE_KIND);
		ownerRef.setName(instanceName);
		ownerRef.setUid(instanceUid);
		ownerRefs.add(ownerRef);
		instanceMeta.setOwnerReferences(ownerRefs);

		logger.debug("Service Instance Namespace : " + inDO.getContext().getNamespace());

		try {
			instance.setApiVersion(Constants.CUSTOM_OBJECT_GROUP + "/" + Constants.CUSTOM_OBJECT_VERSION);
			instance.setKind(Constants.CUSTOM_OBJECT_KIND_TEMPLATE_INSTANCE);
			instanceMeta.setName(instanceName + "." + instanceId);
			instanceMeta.setNamespace(inDO.getContext().getNamespace());
			instance.setMetadata(instanceMeta);

			templateMeta.setName(inDO.getService_id());
			template.setMetadata(templateMeta);

			Map<String,String> inputParameters = new HashMap<>();
			
			String planName = inDO.getPlan_id(); 
			
			try {
				logger.debug("Get Plan Prameters : " + planName);
				
				Object planResponse = customObjectApi.getClusterCustomObject("servicecatalog.k8s.io", "v1beta1","clusterserviceplans", planName);
				GetPlanDO plan = mapper.readValue(gson.toJson(planResponse), GetPlanDO.class);
						  
				if(plan.getSpec().getInstanceCreateParameterSchema() != null) {
					for(String key : plan.getSpec().getInstanceCreateParameterSchema().keySet()) {
						
						logger.debug("Plan Prameter Key : " + key);
						logger.debug("Plan Prameter Value : " + plan.getSpec().getInstanceCreateParameterSchema().get(key));
						
						if (!inputParameters.containsKey(key)) {
							inputParameters.put(key, plan.getSpec().getInstanceCreateParameterSchema().get(key));
						}
					}
				}
			} catch (ApiException e) {
				logger.error("Response body: " + e.getResponseBody());
				e.printStackTrace();
				throw e;
			} catch (Exception e) {
				logger.error("Exception message: " + e.getMessage());
				e.printStackTrace();
				throw e;
			}
			
			if (inDO.getParameters() != null) {
				for (String key : inDO.getParameters().keySet()) {
					if (!inputParameters.containsKey(key)) {
						inputParameters.put(key, inDO.getParameters().get(key));
					}
				}
			}
			
			for (String key : inputParameters.keySet()) {
				logger.debug("Template Instance Prameter Key : " + key);
				logger.debug("Template Instance Prameter Value : " + inputParameters.get(key));
				
				TemplateParameter parameter = new TemplateParameter();
				parameter.setName(key);
				parameter.setValue(inputParameters.get(key));
				parameters.add(parameter);
			}
			template.setParameters(parameters);
			
			spec.setTemplate(template);
			instance.setSpec(spec);

			JSONParser parser = new JSONParser();
			JSONObject bodyObj = (JSONObject) parser.parse(new Gson().toJson(instance));

			response = customObjectApi.createNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, inDO.getContext().getNamespace(),
					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, bodyObj, null);
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return response;
	}

	public static BindingOutDO insertBindingSecret(String instanceId, String bindingId, BindingInDO inDO)
			throws Exception {
		BindingOutDO outDO = new BindingOutDO();
		Map<String, Object> secretMap = new HashMap<String, Object>();
		logger.debug(" Binding Namespace : " + inDO.getContext().getNamespace());
		try {
			Object response = customObjectApi.getNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, inDO.getContext().getNamespace(),
					Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE, instanceId);

			TemplateInstance templateInstance = mapper.readValue(gson.toJson(response), TemplateInstance.class);
			List<Object> objects = templateInstance.getSpec().getTemplate().getObjects();

			for (Object object : objects) {
				JSONObject objectJson = (JSONObject) JSONValue.parse(gson.toJson(object));
				JSONObject metadataJson = (JSONObject) JSONValue.parse(objectJson.get("metadata").toString());

				String name = metadataJson.get("name").toString();
				String namespace = inDO.getContext().getNamespace();
				if (metadataJson.get("namespace") != null) {
					namespace = metadataJson.get("namespace").toString();
				}

				if (objectJson.get("kind").toString().equals("Service")) {
					List<Endpoint> endPointList = new ArrayList<Endpoint>();
					V1Service service = api.readNamespacedService(name, namespace, null, null, null);
					if (service.getSpec().getType().equals("LoadBalancer")) {
						for (V1LoadBalancerIngress ip : service.getStatus().getLoadBalancer().getIngress()) {
							Endpoint endPoint = new Endpoint();
							List<String> ports = new ArrayList<String>();
							endPoint.setHost(ip.getIp());
							secretMap.put("instance-ip", ip.getIp());

							for (V1ServicePort port : service.getSpec().getPorts()) {
								ports.add(String.valueOf(port.getPort()));
							}
							endPoint.setPorts(ports);
							secretMap.put("instance-port", ports);
							endPointList.add(endPoint);
						}
						outDO.setEndpoints(endPointList);
					}
				} else if (objectJson.get("kind").toString().equals("Secret")) {
					V1Secret secret = api.readNamespacedSecret(name, namespace, null, null, null);
					Map<String, byte[]> data = secret.getData();
					for (String key : data.keySet()) {
						secretMap.put(key, new String(data.get(key)));
					}

					outDO.setCredentials(secretMap);
				}
			}
			return outDO;
		} catch (ApiException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private static JsonNode numberTypeConverter(JsonNode jsonNode) {
		if (jsonNode.isObject()) {
			ObjectNode objectNode = (ObjectNode) jsonNode;

			Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();

			while (iter.hasNext()) {
				Map.Entry<String, JsonNode> entry = iter.next();
				entry.setValue(numberTypeConverter(entry.getValue()));
			}
		} else if (jsonNode.isArray()) {
			ArrayNode arrayNode = (ArrayNode) jsonNode;
			for (int i = 0; i < arrayNode.size(); i++) {
				arrayNode.set(i, numberTypeConverter(arrayNode.get(i)));
			}
		} else if (jsonNode.isValueNode()) {
			if (jsonNode.isDouble() && jsonNode.canConvertToInt()) {
				IntNode intNode = new IntNode(jsonNode.asInt());
				jsonNode = intNode;
			}
		}
		return jsonNode;
	}

	private static JsonNode objectToJsonNode(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new GsonBuilder().create();
		String objectStr = gson.toJson(object);
		JsonNode resultNode = null;
		try {
			resultNode = mapper.readTree(objectStr);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return resultNode;
	}

	private static JsonNode objectToJsonNodeForBinding(Object object) throws IOException {
		JsonNode resultNode = mapper.valueToTree(object);
		return resultNode;
	}

	/**
	 * START method for Namespace Claim Controller by seonho_choi DO-NOT-DELETE
	 */
	private static long getLatestResourceVersion(String customResourceName, boolean isNamespaced) throws Exception {
		long latestResourceVersion = 0;
		try {

			Object result = customObjectApi.listClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, customResourceName, null, null, null, null, null, null, null,
					Boolean.FALSE);

			String JsonInString = gson.toJson(result);
			JsonFactory factory = mapper.getFactory();
			com.fasterxml.jackson.core.JsonParser parser = factory.createParser(JsonInString);
			JsonNode customObjectList = mapper.readTree(parser);

			if (customObjectList.get("items").isArray()) {
				for (JsonNode instance : customObjectList.get("items")) {
					long resourceVersion = instance.get("metadata").get("resourceVersion").asLong();
					if (isNamespaced) {
						latestResourceVersion = patchOperatorStartTime(customResourceName,
								instance.get("metadata").get("name").asText(),
								instance.get("metadata").get("namespace").asText(), resourceVersion);
					} else {
						latestResourceVersion = patchOperatorStartTime(customResourceName,
								instance.get("metadata").get("name").asText(), resourceVersion);
					}
				}
			}
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return latestResourceVersion;
	}
	
	private static long getLatestHandledResourceVersion(String customResourceName ) throws Exception { 
		long latestHandledResourceVersion = 0;
		try {
			V1ConfigMap configResult = api.readNamespacedConfigMap (Constants.PREFIX_RESOURCE_VERSION_CONFIGMAP + customResourceName, Constants.REGISTRY_NAMESPACE, null, null, null);
			if ( configResult.getData() != null && configResult.getData().get("latestHandledResourceVersion") != null) {
				latestHandledResourceVersion = Long.parseLong(configResult.getData().get("latestHandledResourceVersion"));
			}
		} catch (ApiException e) {
			if (e.getResponseBody().contains("NotFound") || e.getResponseBody().contains("404")) {
				// Make ConfigMap resourceversion-customResourceName in hypercloud4-system Namespace						
				V1ConfigMap configMap = new V1ConfigMap();
				V1ObjectMeta metadata = new V1ObjectMeta();

				metadata.setName(Constants.PREFIX_RESOURCE_VERSION_CONFIGMAP + customResourceName);
				metadata.setNamespace(Constants.REGISTRY_NAMESPACE);
				configMap.setMetadata(metadata);
				
				Map <String, String> dataMap = new HashMap<>();
				dataMap.put("latestHandledResourceVersion", "0");
				configMap.setData(dataMap);			
				try {
					api.createNamespacedConfigMap(Constants.REGISTRY_NAMESPACE, configMap, null, null, null);
				} catch (ApiException e1) {
					logger.error("Create " + customResourceName + " ConfigMap Failed");
					logger.error("Response body: " + e1.getResponseBody());
					e1.printStackTrace();
					throw e1;
				}
			} else {
				logger.error("Response body: " + e.getResponseBody());
				e.printStackTrace();
				throw e;
			}	
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return latestHandledResourceVersion;
	}
	

	public static V1Namespace createNamespace(NamespaceClaim claim) throws Throwable {
		logger.debug("[K8S ApiCaller] Create Namespace Start");

		V1Namespace namespace = new V1Namespace();
		V1ObjectMeta namespaceMeta = new V1ObjectMeta();
		Map<String, String> labels = new HashMap<>();
		labels.put("fromClaim", claim.getMetadata().getName());
		
		//Add Trial Label if exists
		if (claim.getMetadata().getLabels() != null && claim.getMetadata().getLabels().get("trial") != null
				 && claim.getMetadata().getLabels().get("owner") != null) {
			labels.put("trial", claim.getMetadata().getLabels().get("trial"));
			labels.put("owner", claim.getMetadata().getLabels().get("owner"));
		}
		
		//Add Trial Annotations if exists
		if (claim.getMetadata().getAnnotations() != null ) {
			namespaceMeta.setAnnotations(claim.getMetadata().getAnnotations());
		}
				
		namespaceMeta.setLabels(labels);
		namespaceMeta.setName(claim.getResourceName());
		namespace.setMetadata(namespaceMeta);

		V1Namespace namespaceResult;
		try {
			namespaceResult = api.createNamespace(namespace, null, null, null);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
		
		V1ResourceQuota quota = new V1ResourceQuota();
		V1ObjectMeta quotaMeta = new V1ObjectMeta();
		quotaMeta.setName(claim.getResourceName());
		quotaMeta.setNamespace(claim.getResourceName());
		Map<String, String> quotaLabel = new HashMap<>();
		quotaLabel.put("fromClaim", claim.getMetadata().getName());
		quotaMeta.setLabels(quotaLabel);
		V1ResourceQuotaSpec spec = claim.getSpec();
		quota.setMetadata(quotaMeta);
		quota.setSpec(spec);

		V1ResourceQuota quotaResult;
		try {
			quotaResult = api.createNamespacedResourceQuota(claim.getResourceName(), quota, null, null, null);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
		
		return namespaceResult;
	}

	public static void updateNamespace(NamespaceClaim claim) throws Throwable {
		logger.debug("[K8S ApiCaller] Update Namespace Start");

		V1Namespace namespace = new V1Namespace();
		V1ObjectMeta namespaceMeta = new V1ObjectMeta();
		Map<String, String> labels = new HashMap<>();
		labels.put("fromClaim", claim.getMetadata().getName());
		
		//Add Trial Label if exists
		if (claim.getMetadata().getLabels() != null && claim.getMetadata().getLabels().get("trial") != null 
				&& claim.getMetadata().getLabels().get("owner") !=null) {
			labels.put("trial", claim.getMetadata().getLabels().get("trial"));
			labels.put("owner", claim.getMetadata().getLabels().get("owner"));
		}
		
		//Add Trial Annotations if exists
		if (claim.getMetadata().getAnnotations() != null ) {
			namespaceMeta.setAnnotations(claim.getMetadata().getAnnotations());
		}
				
		namespaceMeta.setLabels(labels);
		namespaceMeta.setName(claim.getResourceName());
		namespace.setMetadata(namespaceMeta);

		V1Namespace namespaceResult;
		try {
			namespaceResult = api.replaceNamespace(claim.getResourceName(), namespace, null, null, null);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}

		V1ResourceQuota quota = new V1ResourceQuota();
		V1ObjectMeta quotaMeta = new V1ObjectMeta();
		quotaMeta.setName(claim.getResourceName());
		quotaMeta.setNamespace(claim.getResourceName());
		Map<String, String> quotaLabel = new HashMap<>();
		quotaLabel.put("fromClaim", claim.getMetadata().getName());
		quotaMeta.setLabels(quotaLabel);
		V1ResourceQuotaSpec spec = claim.getSpec();
		quota.setMetadata(quotaMeta);
		quota.setSpec(spec);

		V1ResourceQuota quotaResult;
		try {
			quotaResult = api.replaceNamespacedResourceQuota(claim.getResourceName(), claim.getResourceName(), quota,
					null, null, null);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}
	
	public static void replaceNamespace(V1Namespace namespace) throws Throwable {
		logger.debug("[K8S ApiCaller] Update Namespace [ " + namespace.getMetadata().getName() + " ] Start");

		V1Namespace namespaceResult;
		try {
			namespaceResult = api.replaceNamespace(namespace.getMetadata().getName(), namespace, null, null, null);
			logger.debug(" Update Namespace [ " + namespace.getMetadata().getName() + " ] Success");
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}
	
	public static void replaceNamespaceClaim(NamespaceClaim namespaceClaim) throws Throwable {
		logger.debug("[K8S ApiCaller] Update Namespace Claim [ " + namespaceClaim.getMetadata().getName() + " ] Start");

		try {
			customObjectApi.replaceClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, namespaceClaim.getMetadata().getName(), namespaceClaim);
			logger.debug(" Update Namespace [ " + namespaceClaim.getMetadata().getName() + " ] Success");
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}

	public static void createResourceQuota(NamespaceClaim claim) throws Throwable {
		logger.debug("[K8S ApiCaller] Create Resource Quota Start");

		V1ResourceQuota quota = new V1ResourceQuota();
		V1ObjectMeta quotaMeta = new V1ObjectMeta();
		quotaMeta.setName(claim.getResourceName());
		quotaMeta.setNamespace(claim.getMetadata().getNamespace());
		Map<String, String> quotaLabel = new HashMap<>();
		quotaLabel.put("fromClaim", claim.getMetadata().getName());
		quotaMeta.setLabels(quotaLabel);
		V1ResourceQuotaSpec spec = claim.getSpec();
		quota.setMetadata(quotaMeta);
		quota.setSpec(spec);

		V1ResourceQuota quotaResult;
		try {
			quotaResult = api.createNamespacedResourceQuota(claim.getMetadata().getNamespace(), quota, null, null,
					null);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}

	public static void updateResourceQuota(NamespaceClaim claim) throws Throwable {
		logger.debug("[K8S ApiCaller] Update Resource Quota Start");

		V1ResourceQuota quota = new V1ResourceQuota();
		V1ObjectMeta quotaMeta = new V1ObjectMeta();
		quotaMeta.setName(claim.getResourceName());
		quotaMeta.setNamespace(claim.getMetadata().getNamespace());
		Map<String, String> quotaLabel = new HashMap<>();
		quotaLabel.put("fromClaim", claim.getMetadata().getName());
		quotaMeta.setLabels(quotaLabel);
		V1ResourceQuotaSpec spec = claim.getSpec();
		quota.setMetadata(quotaMeta);
		quota.setSpec(spec);

		try {
			api.replaceNamespacedResourceQuota(claim.getResourceName(), claim.getMetadata().getNamespace(), quota, null,
					null, null);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}

	public static boolean namespaceAlreadyExist(String name) throws Throwable {
		logger.debug("[K8S ApiCaller] Get Namespace Start");

		V1Namespace namespaceResult;
		try {
			namespaceResult = api.readNamespace(name, null, null, null);
		} catch (ApiException e) {
			logger.debug("[K8S ApiCaller][Exception] Namespace-" + name + " is not Exist");
			return false;
		}

		if (namespaceResult == null) {
			logger.debug("[K8S ApiCaller] Namespace-" + name + " is not Exist");
			return false;
		} else {
			logger.info("[K8S ApiCaller] Namespace-" + name + " is already Exist");
			logger.debug(namespaceResult.toString());
			return true;
		}
	}

	public static boolean resourcequotaAlreadyExist(String name, String namespace) throws Throwable {
		logger.info("[K8S ApiCaller] Get Resource Quota Start");

		V1ResourceQuota resourceQuotaResult;
		try {
			resourceQuotaResult = api.readNamespacedResourceQuota(name, namespace, null, null, null);
		} catch (ApiException e) {
			logger.info("[K8S ApiCaller][Exception] ResourceQuota-" + name + " is not Exist");
			return false;
		}

		if (resourceQuotaResult == null) {
			logger.info("[K8S ApiCaller][Exception] ResourceQuota-" + name + " is not Exist");
			return false;
		} else {
			logger.info(resourceQuotaResult.toString());
			return true;
		}
	}

	public static boolean roleBindingAlreadyExist(String name, String namespace) throws Throwable {
		logger.info("[K8S ApiCaller] Get RoleBinding Start");

		V1RoleBinding roleBindingResult;
		try {
			roleBindingResult = rbacApi.readNamespacedRoleBinding(name, namespace, null);
		} catch (ApiException e) {
			logger.info("[K8S ApiCaller][Exception] RoleBinding-" + name + " is not Exist");
			return false;
		}

		if (roleBindingResult == null) {
			logger.info("[K8S ApiCaller][Exception] RoleBinding-" + name + " is not Exist");
			return false;
		} else {
			logger.info(roleBindingResult.toString());
			return true;
		}
	}
	
	public static boolean templateAlreadyExist(String name, String namespace) throws Throwable {
		logger.info("[K8S ApiCaller] Get Template Start");

		Object template;
		try {
			template = customObjectApi.getNamespacedCustomObject("tmax.io", "v1", namespace, Constants.CUSTOM_OBJECT_PLURAL_TEMPLATE, name);
		} catch (ApiException e) {
			logger.info("[K8S ApiCaller][Exception] Template-" + name + " is not Exist");
			return false;
		}

		if (template == null) {
			logger.info("[K8S ApiCaller][Exception] Template-" + name + " is not Exist");
			return false;
		} else {
			logger.info(objectToJsonNode(template).toString());
			return true;
		}
	}
	
	public static void createTemplate(JsonNode claim, String namespace) throws ApiException, ParseException {
		logger.info("[K8S ApiCaller] Create Template Start");
		logger.info("[K8S ApiCaller] Create Template Info : " + claim.get("spec").toString() );
		
		JSONParser parser = new JSONParser();
		JSONObject bodyObj = (JSONObject) parser.parse(claim.get("spec").toString());
		
		try {
			templateApi.createNamespacedCustomObject("tmax.io", "v1", namespace, "Template", bodyObj, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void createRoleBinding(RoleBindingClaim claim) throws ApiException {
		logger.info("[K8S ApiCaller] Create RoleBinding Start");

		V1RoleBinding roleBinding = new V1RoleBinding();
		V1ObjectMeta roleBindingMeta = new V1ObjectMeta();
		roleBindingMeta.setName(claim.getResourceName());
		roleBindingMeta.setNamespace(claim.getMetadata().getNamespace());
		roleBindingMeta.setLabels(claim.getMetadata().getLabels());
		roleBinding.setMetadata(roleBindingMeta);
		roleBinding.setSubjects(claim.getSubjects());
		roleBinding.setRoleRef(claim.getRoleRef());

		try {
			rbacApi.createNamespacedRoleBinding(claim.getMetadata().getNamespace(), roleBinding, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void createClusterRoleBinding(RoleBindingClaim claim) throws ApiException {
		logger.info("[K8S ApiCaller] Create ClusterRoleBinding Start");

		V1ClusterRoleBinding clusterRoleBinding = new V1ClusterRoleBinding();
		V1ObjectMeta clusterRoleBindingMeta = new V1ObjectMeta();
		clusterRoleBindingMeta.setName(claim.getResourceName());
		clusterRoleBindingMeta.setLabels(claim.getMetadata().getLabels());
		clusterRoleBinding.setMetadata(clusterRoleBindingMeta);
		clusterRoleBinding.setSubjects(claim.getSubjects());
		clusterRoleBinding.setRoleRef(claim.getRoleRef());

		try {
			rbacApi.createClusterRoleBinding( clusterRoleBinding, null, null, null);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			e.printStackTrace();
			throw e;
		}
	}
	

	public static void updateRoleBinding(RoleBindingClaim claim) throws Throwable {
		logger.debug("[K8S ApiCaller] Update Role Binding Start");

		V1RoleBinding roleBinding = new V1RoleBinding();
		V1ObjectMeta roleBindingMeta = new V1ObjectMeta();
		roleBindingMeta.setName(claim.getResourceName());
		roleBindingMeta.setNamespace(claim.getMetadata().getNamespace());
		roleBinding.setMetadata(roleBindingMeta);
		roleBinding.setSubjects(claim.getSubjects());
		roleBinding.setRoleRef(claim.getRoleRef());

		try {
			rbacApi.replaceNamespacedRoleBinding(claim.getResourceName(), claim.getMetadata().getNamespace(),
					roleBinding, null, null, null);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}

	public static void createClusterRoleForNewUser(String userId) throws ApiException {
		logger.info("[K8S ApiCaller] Create Temporary ClusterRole for New User Start");

		V1ClusterRole clusterRole = new V1ClusterRole();
		V1ObjectMeta clusterRoleMeta = new V1ObjectMeta();
		clusterRoleMeta.setName(userId);
		clusterRole.setMetadata(clusterRoleMeta);
		List<V1PolicyRule> rules = new ArrayList<>();

		V1PolicyRule rule = new V1PolicyRule();

		// ClusterRole & ClusterRoleBinding Rule
		rule = new V1PolicyRule();
		rule.addApiGroupsItem(Constants.RBAC_API_GROUP);
		rule.addResourcesItem("clusterroles");
		rule.addResourcesItem("clusterrolebindings");
		rule.addResourceNamesItem(userId);
		rule.addVerbsItem("*");
		rules.add(rule);
		
		// NameSpace Rule
		rule = new V1PolicyRule();
		rule.addApiGroupsItem(Constants.CORE_API_GROUP);
		rule.addResourcesItem("namespaces");
		rule.addVerbsItem("get");
		rules.add(rule);
	
		// Claims Rule
		rule = new V1PolicyRule();
		rule.addApiGroupsItem(Constants.CUSTOM_OBJECT_GROUP);
//		rule.addResourcesItem("rolebindingclaims");
//		rule.addResourcesItem("resourcequotaclaims");
		rule.addResourcesItem("namespaceclaims");
		rule.addVerbsItem("create");
		rule.addVerbsItem("get");		
		rules.add(rule);
		
		rule = new V1PolicyRule();
		rule.addApiGroupsItem(Constants.CUSTOM_OBJECT_GROUP);
		rule.addResourcesItem("rolebindingclaims");
		rule.addResourcesItem("resourcequotaclaims");
		rule.addResourcesItem("catalogserviceclaims");
		rule.addVerbsItem("create");
		rule.addVerbsItem("get");		
		rule.addVerbsItem("list");		
		rules.add(rule);
		
		// CMP Rule
		rule = new V1PolicyRule();
		rule.addApiGroupsItem(Constants.UI_CUSTOM_OBJECT_GROUP);
		rule.addResourcesItem("clustermenupolicies");
		rule.addVerbsItem("get");		
		rules.add(rule);
		
		clusterRole.setRules(rules);

		try {
			rbacApi.createClusterRole(clusterRole, null, null, null);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}

	public static void createClusterRoleBindingForNewUser(String userId) throws ApiException {
		logger.info("[K8S ApiCaller] Create Temporary ClusterRoleBinding for New User Start");

		V1ClusterRoleBinding clusterRoleBinding = new V1ClusterRoleBinding();
		V1ObjectMeta clusterRoleBindingMeta = new V1ObjectMeta();
		clusterRoleBindingMeta.setName(userId);
		clusterRoleBinding.setMetadata(clusterRoleBindingMeta);

		// RoleRef
		V1RoleRef roleRef = new V1RoleRef();
		roleRef.setApiGroup(Constants.RBAC_API_GROUP);
		roleRef.setKind("ClusterRole");
		roleRef.setName(userId);
		clusterRoleBinding.setRoleRef(roleRef);

		// subject
		V1Subject subject = new V1Subject();
		subject.setApiGroup(Constants.RBAC_API_GROUP);
		subject.setKind("User");
		subject.setName(userId);
		clusterRoleBinding.addSubjectsItem(subject);

		try {
			rbacApi.createClusterRoleBinding(clusterRoleBinding, null, null, null);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}
	}
	
	public static void createRoleBindingForIngressNginx(String userId) throws ApiException {
		logger.info("[K8S ApiCaller] Create roleBinding for New User Start");

		V1RoleBinding roleBinding = new V1RoleBinding();
		V1ObjectMeta roleBindingMeta = new V1ObjectMeta();
		roleBindingMeta.setName(Constants.INGRESS_NGINX_SHARED_READ_ROLE_BINDING + "-" + userId);
		roleBindingMeta.setNamespace(Constants.INGRESS_NGINX_SHARED_NAMESPACE);
		roleBinding.setMetadata(roleBindingMeta);

		// RoleRef
		V1RoleRef roleRef = new V1RoleRef();
		roleRef.setApiGroup(Constants.RBAC_API_GROUP);
		roleRef.setKind("ClusterRole");
		roleRef.setName(Constants.INGRESS_NGINX_SHARED_READ_CLUSTER_ROLE);
		roleBinding.setRoleRef(roleRef);

		// subject
		V1Subject subject = new V1Subject();
		subject.setApiGroup(Constants.RBAC_API_GROUP);
		subject.setKind("User");
		subject.setName(userId);
		roleBinding.addSubjectsItem(subject);

		try {
			rbacApi.createNamespacedRoleBinding(Constants.INGRESS_NGINX_SHARED_NAMESPACE, roleBinding, null, null, null);
		} catch (ApiException e) {
			if(e.getResponseBody().contains("Not Found") || e.getResponseBody().contains("404")) {
				logger.info(Constants.INGRESS_NGINX_SHARED_NAMESPACE + " does not exist, Do nothing ");
			} else {
				logger.error(e.getResponseBody());
				throw e;
			}
		}
	}

	public static void deleteClusterRole(String name) throws Exception {
		try {
			V1DeleteOptions body = new V1DeleteOptions();
			rbacApi.deleteClusterRole(name, null, null, null, null, null, body);
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void deleteRole(String name, String namespace) throws Exception {
		try {
			V1DeleteOptions body = new V1DeleteOptions();
			rbacApi.deleteNamespacedRole(name, namespace, null, null, null, null, null, body);
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public static void deleteClusterRoleBinding(String name) throws Exception {
		try {
			V1DeleteOptions body = new V1DeleteOptions();
			rbacApi.deleteClusterRoleBinding(name, null, null, null, null, null, body);
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void deleteRoleBinding(String nsName, String roleBindingName) throws Exception {
		try {
			V1DeleteOptions body = new V1DeleteOptions();
			rbacApi.deleteNamespacedRoleBinding(roleBindingName, nsName, null, null, 0, null, null, body);
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}		
	}
	
	public static V1ClusterRole readClusterRole(String clusterRoleName) {
		V1ClusterRole clusterRole = null;
		try {
			clusterRole = rbacApi.readClusterRole(clusterRoleName, "true"); 

		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return clusterRole;
	}
	
	public static V1ClusterRole replaceClusterRole(V1ClusterRole clusterRole) {
		V1ClusterRole replaceResult = null;
		try {
			replaceResult = rbacApi.replaceClusterRole(clusterRole.getMetadata().getName(), clusterRole, null, null, null);
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return replaceResult;
	}

	/**
	 * END method for Namespace Claim Controller by seonho_choi DO-NOT-DELETE
	 */

	/**
	 * START method for Namespace Claim Controller by seonho_choi DO-NOT-DELETE
	 */
	public static String getUid(String namespace, String name) {
		String uid = "";
		try {
			Object result = customObjectApi.getNamespacedCustomObject(Constants.SERVICE_INSTANCE_API_GROUP,
					Constants.SERVICE_INSTANCE_API_VERSION, namespace, Constants.SERVICE_INSTANCE_PLURAL, name);

			String jsonString = gson.toJson(result);
			logger.info(jsonString);
			JsonParser parser = new JsonParser();
			uid = parser.parse(jsonString).getAsJsonObject().get("metadata").getAsJsonObject().get("uid").getAsString();
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return uid;
	}

	public static V1NamespaceList getAccessibleNS(String userId, String labelSelector) throws Exception {
		V1NamespaceList nsList = new V1NamespaceList();
		V1ListMeta metadata = new V1ListMeta();
		nsList.setMetadata(metadata);
		List<String> nsNameList = null;
		List<String> userGroupList = null;
		
		V1ClusterRoleBindingList crbList = null;
		List<String> clusterRoleList = null;
		boolean clusterRoleFlag = false;
		try {
			// 1. Get UserGroup List if Exists
			logger.debug(" userId :" + userId);
			
			// TODO : keycloak에서의 user Group 고려 추가해야함
			// 2. List of ClusterRoleBinding
			crbList = rbacApi.listClusterRoleBinding("true", false, null, null, null, 1000, null, 60, false);
			for (V1ClusterRoleBinding item : crbList.getItems()) {
				List<V1Subject> subjects = item.getSubjects();
				V1RoleRef roleRef = item.getRoleRef();
				if (subjects != null) {
					for (V1Subject subject : subjects) {
						if (subject.getKind().equalsIgnoreCase("User")) {
							if (subject.getName().equalsIgnoreCase(userId)) {
								if (clusterRoleList == null)
									clusterRoleList = new ArrayList<>();
								clusterRoleList.add(roleRef.getName()); // get ClusterRole name
							}
						} else if (subject.getKind().equalsIgnoreCase("Group")) {
							if ( userGroupList != null ) {
//								logger.info(" subject.getName() " + subject.getName());
								if ( userGroupList.contains(subject.getName())) {
									if (clusterRoleList == null)
										clusterRoleList = new ArrayList<>();
									clusterRoleList.add(roleRef.getName()); // get ClusterRole name
								}
							}
						}
					}
				}
			}

			// 3. Check if ClusterRole has NameSpace GET rule
			if (clusterRoleList != null) {
				for (String clusterRoleName : clusterRoleList) {
					logger.info("User [ " + userId + " ] has ClusterRole [ " + clusterRoleName + " ]");
					V1ClusterRole clusterRole = null;
					List<V1PolicyRule> rules = null;
					try{
						clusterRole = rbacApi.readClusterRole(clusterRoleName, "true"); // rolebinding은 있는데 role을 지웠을 경우를 대비
						rules = clusterRole.getRules();
					} catch (ApiException e) {
						if( !e.getResponseBody().contains("NotFound") ) throw e;			
					}
					if (rules != null) {
						for (V1PolicyRule rule : rules) {
							if (rule.getResources() != null) {
								if (rule.getResources().contains("*") || rule.getResources().contains("namespaces")) {
									if ( rule.getApiGroups().contains("*") || rule.getApiGroups().contains("") || rule.getApiGroups().contains("core") ) {
										logger.info("clusterRoleName : " + clusterRoleName);
										if (rule.getVerbs() != null) {
											if (rule.getVerbs().contains("list") || rule.getVerbs().contains("*")) {
												if (rule.getResourceNames() == null
														|| rule.getResourceNames().size() == 0) {
													clusterRoleFlag = true;
												} else {
													for (String nsName : rule.getResourceNames()) {
														if (nsNameList == null)
															nsNameList = new ArrayList<>();
														nsNameList.add(nsName);
													}
												}
											}
										}
									}		
								}
							}
						}
					}
				}
			}
			logger.info("clusterRoleflag : " + clusterRoleFlag);
			// Get All NameSpace
			if (clusterRoleFlag) {
				nsList = api.listNamespace("true", false, null, null, labelSelector, null, null, 60, false);
				if(!(nsList.getItems().size() > 0)) {
					nsList.getMetadata().setContinue("wrongLabelorNoResource");  // for 권한 구분
				}
			} else {
				V1NamespaceList nsListK8S = api.listNamespace("true", false, null, null, labelSelector, null, null, 60, false);
				if(!(nsListK8S.getItems().size() > 0)) {
					nsList.getMetadata().setContinue("wrongLabelorNoResource");  // for 권한 구분
				}
				// 4. List of RoleBinding
				if (nsListK8S.getItems() != null && nsListK8S.getItems().size() > 0) {
					metadata.setContinue("get");  // for 권한 구분
					for (V1Namespace ns : nsListK8S.getItems()) {
						V1RoleBindingList rbList = rbacApi.listNamespacedRoleBinding(ns.getMetadata().getName(), "true",
								false, null, null, null, 100, null, 60, false);
						for (V1RoleBinding item : rbList.getItems()) {
							List<V1Subject> subjects = item.getSubjects();
							V1RoleRef roleRef = item.getRoleRef();
							for (V1Subject subject : subjects) {
								if (( subject.getKind().equalsIgnoreCase("User") && subject.getName().equalsIgnoreCase(userId) ) 
										|| ( userGroupList!= null && subject.getKind().equalsIgnoreCase("Group") && userGroupList.contains(subject.getName()) ) ) {
										
									// 5. Check if Role has NameSpace GET rule
									if (roleRef.getKind().equalsIgnoreCase("Role")) {
										logger.info(
												"User [ " + userId + " ] has Role [" + roleRef.getName() + " ]");
										V1Role role = null;
										List<V1PolicyRule> rules = null;
										try{
											role = rbacApi.readNamespacedRole(roleRef.getName(), ns.getMetadata().getName(), "true");  // rolebinding은 있는데 role을 지웠을 경우를 대비
											rules = role.getRules();
										} catch (ApiException e) {
											if( !e.getResponseBody().contains("NotFound") ) throw e;			
										}
										if (rules != null) {
											for (V1PolicyRule rule : rules) {
												if (rule.getResources() != null) {
													if (rule.getResources().contains("*") || rule.getResources().contains("namespaces")) {
														if ( rule.getApiGroups().contains("*") || rule.getApiGroups().contains("") 
																|| rule.getApiGroups().contains("core") ) {
															if (rule.getVerbs() != null) {
																if (rule.getVerbs().contains("list")
																		|| rule.getVerbs().contains("*")) {
																	if (nsNameList == null)
																		nsNameList = new ArrayList<>();
																	nsNameList.add(ns.getMetadata().getName());
																}
															}
														}	
													}
												}
											}
										}

										// 6. Check if ClusterRole has NameSpace GET rule
									} else if (roleRef.getKind().equalsIgnoreCase("ClusterRole")) {
										logger.info("User [ " + userId + " ] has ClusterRole [" + roleRef.getName() + " ]");
										
										V1ClusterRole role = null;
										List<V1PolicyRule> rules = null;
										try{
											role = rbacApi.readClusterRole(roleRef.getName(), "true"); // rolebinding은 있는데 role을 지웠을 경우를 대비
											rules = role.getRules();
										} catch (ApiException e) {
											if( !e.getResponseBody().contains("NotFound") ) throw e;			
										}
										if (rules != null) {
											for (V1PolicyRule rule : rules) {
												if (rule.getResources() != null) {
													if (rule.getResources().contains("*") || rule.getResources().contains("namespaces")) {
														if ( rule.getApiGroups().contains("*") || rule.getApiGroups().contains("") 
																|| rule.getApiGroups().contains("core") ) {
															if (rule.getVerbs() != null) {
																if (rule.getVerbs().contains("list")
																		|| rule.getVerbs().contains("*")) {
																	if (nsNameList == null)
																		nsNameList = new ArrayList<>();
																	nsNameList.add(ns.getMetadata().getName());
																}
															}
														}	
													}
												}
											}
										}
									}			
								}
							}
						}
					}
				} 
				
				if (nsNameList != null) {
					// Stream distinct (중복제거)
					nsNameList = nsNameList.stream().distinct().collect(Collectors.toList());

					for (String nsName : nsNameList) {
						nsList.setKind("NamespaceList");
						V1ListMeta nsListMeta = new V1ListMeta();
						nsListMeta.setSelfLink("/api/v1/namespaces");
						nsList.setMetadata(nsListMeta);
						V1Namespace ns = api.readNamespace(nsName, "true", false, false);
						ns.setKind(null);
						ns.setApiVersion(null);
						nsList.addItemsItem(ns);
					}
				}
			}
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			throw e;
		}

		if (nsList != null && nsList.getItems()!= null && nsList.getItems().size() > 0) {
			for (V1Namespace ns : nsList.getItems()) {
				logger.info(" [ Accessible NameSpace ] : " + ns.getMetadata().getName());
			}
		}
		return nsList;
	}
	
	public static NamespaceClaimList getAccessibleNSC(String token, String userId, String labelSelector, String _continue) throws Exception {
		NamespaceClaimList nscList = null;
		
		try {
			logger.debug(" user Id :" + userId);
			logger.debug(" user Token :" + token);
				
			// 1. Verify if user has NSC List role			
			ApiClient nscUser = Config.fromCluster();
			
		    // Configure API key authorization: BearerToken
		    ApiKeyAuth BearerToken = (ApiKeyAuth) nscUser.getAuthentication("BearerToken");
//		    logger.debug("BearerToken : " + BearerToken);

		    BearerToken.setApiKey(token);

		    AuthorizationV1Api authApi = new AuthorizationV1Api(nscUser);
		    V1SelfSubjectAccessReview body = new V1SelfSubjectAccessReview(); // V1SelfSubjectAccessReview | 
		    V1SelfSubjectAccessReviewSpec spec = new V1SelfSubjectAccessReviewSpec();
		    V1ResourceAttributes ra = new V1ResourceAttributes();
		    ra.setResource("namespaceclaims");
		    ra.setGroup("tmax.io");
		    ra.setVerb("list");
		    spec.setResourceAttributes(ra);
		    body.setSpec(spec);

		    V1SelfSubjectAccessReview result = null;
		    try {
		      result = authApi.createSelfSubjectAccessReview(body, null, null, null);    
			} catch (ApiException e) {
				logger.info(e.getResponseBody());
				throw e;
			}
		    
		    // 2. User has NSC List Permission
		    if (result.getStatus().getAllowed()) {
				logger.debug("2. User has NSC List Permission");
				nscList = listNamespaceClaim( labelSelector, _continue );
				if(!(nscList.getItems().size() > 0)) {  //label selector 잘못 입력 고려
					nscList.getMetadata().setContinue("wrongLabelorNoResource");
				}
		    } else {
		    	// 3. User has No NSC List Permission --> Check if there is owner NSC with label	
				logger.debug("3. User has No NSC List Permission --> Check if there is owner NSC with label");
				List < NamespaceClaim > nscItems = null;

				body = new V1SelfSubjectAccessReview(); // V1SelfSubjectAccessReview 
			    spec = new V1SelfSubjectAccessReviewSpec();
			    ra = new V1ResourceAttributes();
			    ra.setResource("namespaceclaims");
			    ra.setGroup("tmax.io");
			    ra.setVerb("get");
			    spec.setResourceAttributes(ra);
			    body.setSpec(spec);
			    
			    try {
			    	result = authApi.createSelfSubjectAccessReview(body, null, null, null);      
				} catch (ApiException e) {
					logger.error(e.getResponseBody());
					throw e;
				}
			    if (result.getStatus().getAllowed()) {
			    	//3-1. User has NSC Get Permission
					logger.debug("3-1. User has NSC Get Permission");
					NamespaceClaimList possibleNscList = null;		
					possibleNscList = listNamespaceClaim( labelSelector, _continue );
					if(!(possibleNscList.getItems().size() > 0)) {  //label selector 잘못 입력 고려
						possibleNscList.getMetadata().setContinue("wrongLabelorNoResource");  
					}
					if (possibleNscList != null && possibleNscList.getItems()!=null && possibleNscList.getItems().size() > 0) {
				    	for( NamespaceClaim possibleNsc :  possibleNscList.getItems()) {
							if ( possibleNsc.getMetadata().getLabels() != null && possibleNsc.getMetadata().getLabels().get("owner")!= null) {
								if ( possibleNsc.getMetadata().getLabels().get("owner").toString().equalsIgnoreCase(userId) ){
									if (nscItems == null) nscItems = new ArrayList<>();
									nscItems.add(possibleNsc);
					    		}
							}	
				    	}
				    	if ( nscItems == null ) possibleNscList.getMetadata().setContinue("wrongLabelorNoResource");  

					}
					possibleNscList.setItems(nscItems);
			    	nscList = possibleNscList;
			    } else {
					logger.debug("3-2. User has NO NSC Get Permission, User Cannot Access any NSC");
			    }
		    }
		    
		}catch( Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			throw e;
		}

		if (nscList != null && nscList.getItems() != null && nscList.getItems().size() > 0) {
			for (NamespaceClaim nsc : nscList.getItems()) {
				logger.info(" [ Accessible NameSpaceClaim ] : " + nsc.getMetadata().getName());
			}
		}
		return nscList;
	}

	private static long patchOperatorStartTime(String plural, String name, String namespace, long version) {

		JsonArray patchArray = new JsonArray();
		JsonObject patch = new JsonObject();
		patch.addProperty("op", "replace");
		patch.addProperty("path", "/operatorStartTime");
		patch.addProperty("value", Long.toString(time));
		patchArray.add(patch);

		logger.debug( "Patch Annotation Object : " + patchArray );

		Object response = null;
		try {
			response = customObjectApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, namespace, plural, name, patchArray);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			logger.error("ApiException Code: " + e.getCode());
		}

		long resourceVersion = version;
		if (response != null) {
			JsonObject json = (JsonObject) new JsonParser().parse(new Gson().toJson(response));
			resourceVersion = json.get("metadata").getAsJsonObject().get("resourceVersion").getAsLong();
		}

		return resourceVersion;
	}

	private static long patchOperatorStartTime(String plural, String name, long version) {

		JsonArray patchArray = new JsonArray();
		JsonObject patch = new JsonObject();
		patch.addProperty("op", "replace");
		patch.addProperty("path", "/operatorStartTime");
		patch.addProperty("value", Long.toString(time));
		patchArray.add(patch);

		// logger.info( "Patch Annotation Object : " + patchArray );

		Object response = null;
		try {
			response = customObjectApi.patchClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, plural, name, patchArray);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			logger.error("ApiException Code: " + e.getCode());
		}

		long resourceVersion = version;
		if (response != null) {
			JsonObject json = (JsonObject) new JsonParser().parse(new Gson().toJson(response));
			resourceVersion = json.get("metadata").getAsJsonObject().get("resourceVersion").getAsLong();
		}

		return resourceVersion;
	}

	private static OkHttpClient getHttpClient() {
		OkHttpClient httpClient;
		//List lists = Arrays.asList(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT);
		 ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
		            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
		            .cipherSuites(
		                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
		                    CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
		                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
		                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)
		            .build();

		//httpClient.connectionSpecs(Collections.singletonList(spec))

		
		
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.addNetworkInterceptor(getProgressInterceptor()); // K8S Interceptor
		builder.addNetworkInterceptor(new ChunkedInterceptor()); // HyperCloud Interceptor
		//builder.addInterceptor(new HttpLoggingInterceptor());
		builder.connectionSpecs(Collections.singletonList(spec));
		httpClient = builder.build();
		return httpClient;
	}

	/**
	 * Get network interceptor to add it to the httpClient to track download
	 * progress for async requests.
	 */
	private static Interceptor getProgressInterceptor() {
		return new Interceptor() {
			@Override
			public Response intercept(Interceptor.Chain chain) throws IOException {
				final Request request = chain.request();
				final Response originalResponse = chain.proceed(request);
				if (request.tag() instanceof ApiCallback) {
					final ApiCallback callback = (ApiCallback) request.tag();
					return originalResponse.newBuilder()
							.body(new ProgressResponseBody(originalResponse.body(), callback)).build();
				}
				return originalResponse;
			}
		};
	}

	public static String httpsCommander(SSLSocketFactory sf, Map<String, String> requestHeader,
			Map<String, String> responseHeader, String address, String command, String method) throws Exception {
		// Properties propertiesClone =
		// ProzoneConfig.makeClone(ServiceManager.getCurrentRequestContext().getRequest());

		StringBuilder textBuilder = new StringBuilder();

		logger.debug("[ InfraAPICaller-K8s ] httpsCommander ");
		StringBuilder sb = new StringBuilder();
		sb.append(Constants.HTTPS_SCHEME_PREFIX);
		sb.append(address);
		sb.append("/");
		sb.append(command);
		String serviceURL = sb.toString();
		logger.debug("Service URL : " + serviceURL);

		try {
			URL url = new URL(serviceURL);
			HttpsURLConnection client = (HttpsURLConnection) url.openConnection();
			client.setSSLSocketFactory(sf);
			client.setRequestMethod(method == null ? "GET" : method);
			client.setDoInput(true);
			client.setDoOutput(true);
			client.setUseCaches(false);
			client.setConnectTimeout(30 * 1000);
			client.setReadTimeout(60 * 1000);
			client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			// set HTTPS Header
			for (String key : requestHeader.keySet()) {
				logger.debug("[Request*Header]" + key + ": " + requestHeader.get(key));
				client.setRequestProperty(key, requestHeader.get(key));
			}

			InputStream is = client.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, Charset.forName(StandardCharsets.UTF_8.name()));
			Reader reader = new BufferedReader(isr);

			int c = 0;
			while ((c = reader.read()) != -1) {
				textBuilder.append((char) c);
			}

			reader.close();

			logger.debug("[https]: " + textBuilder.toString());

		} catch (Exception e) {
			logger.error("[httpsCommander Exception] " + e.getMessage());
			throw e;
		} finally {
		}
		return textBuilder.toString();

	}
	
	public static V1NamespaceList listNameSpace() throws Exception {
		V1NamespaceList nsList = null;
		try {
			nsList = api.listNamespace("true", false, null, null, null, null, null, 60, false);
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return nsList;
	}
	
	public static V1Namespace getNameSpace(String nsName) throws Exception {
		V1Namespace nameSpace = null;

		try {
			logger.debug("nameSpace [ " + nsName + " ] Get Service Start");
			nameSpace = api.readNamespace(nsName, "true", false, false);
			logger.debug("nameSpace [ " + nsName + " ] Get Service Success");

		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return nameSpace;
	}
	
	public static void deleteNameSpace(String nsName) throws Exception {
		try {
			logger.debug("nameSpace [ " + nsName + " ] Delete Service Start");
			V1Status deleteStatus = api.deleteNamespace(nsName, null, null, 0, null, "Background" , new V1DeleteOptions());
			logger.debug("delete Status : "  + deleteStatus.getStatus());
			logger.debug("delete message : "  + deleteStatus.getMessage());
			logger.debug("delete reason : "  + deleteStatus.getReason());
			logger.debug("delete whole : "  + deleteStatus.toString());

			logger.debug("nameSpace [ " + nsName + " ] Deleted");
		} catch (IllegalStateException e) {
			logger.debug("nameSpace [ " + nsName + " ] Delete Service Success");
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
		}
	}
	
	
	public static UserSecurityPolicyCR getUserSecurityPolicy(String uspName) throws Exception {
		UserSecurityPolicyCR uspCR = new UserSecurityPolicyCR();
		logger.debug("UserSecurityPolicy [ " + uspName + " ] Get Service Start");

		try {
			Object response = customObjectApi.getClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_USER_SECURITY_POLICY, uspName);
			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			mapper.registerModule(new JodaModule());
			uspCR = mapper.readValue((new Gson()).toJson(respJson), new TypeReference<UserSecurityPolicyCR>() {
			});
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return uspCR;
	}

	public static NamespaceClaim getNamespaceClaim(String nscName) throws Exception {
		NamespaceClaim nscCR = new NamespaceClaim();
		logger.debug("NamespaceClaim [ " + nscName + " ] Get Service Start");

		try {
			Object response = customObjectApi.getClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, nscName);
			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			mapper.registerModule(new JodaModule());
			nscCR = mapper.readValue((new Gson()).toJson(respJson), new TypeReference<NamespaceClaim>() {
			});
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return nscCR;
	}
	
	public static NamespaceClaim getResourceQuotaClaim(String rqcName) throws Exception {
		NamespaceClaim rqcCR = new NamespaceClaim();
		logger.debug("resourceQuotaClaim [ " + rqcName + " ] Get Service Start");

		try {
			Object response = customObjectApi.getClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_RESOURCEQUOTACLAIM, rqcName);
			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			mapper.registerModule(new JodaModule());
			rqcCR = mapper.readValue((new Gson()).toJson(respJson), new TypeReference<NamespaceClaim>() {
			});
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return rqcCR;
	}
	
	public static RoleBindingClaim getRoleBindingClaim(String rbcName) throws Exception {
		RoleBindingClaim rbcCR = new RoleBindingClaim();
		logger.debug("RoleBindingClaim [ " + rbcName + " ] Get Service Start");

		try {
			Object response = customObjectApi.getClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP,
					Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM, rbcName);
			JsonObject respJson = (JsonObject) new JsonParser().parse((new Gson()).toJson(response));

			mapper.registerModule(new JodaModule());
			rbcCR = mapper.readValue((new Gson()).toJson(respJson), new TypeReference<RoleBindingClaim>() {
			});
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return rbcCR;
	}
	
	
	public static void createUserSecurityPolicy(String uspName) throws Exception {
		try {
			UserSecurityPolicyCR uspCR = new UserSecurityPolicyCR();
			// Set name & label
			V1ObjectMeta metadata = new V1ObjectMeta();
			metadata.setName( uspName );
			uspCR.setMetadata( metadata );

			// Set otpEnable false
			uspCR.setOtpEnable("f");

			// Make body
			JSONParser parser = new JSONParser();
			JSONObject bodyObj = (JSONObject) parser.parse(new Gson().toJson(uspCR));

			customObjectApi.createClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION,
					Constants.CUSTOM_OBJECT_PLURAL_USER_SECURITY_POLICY, bodyObj, null);
		} catch (ApiException e) {
			logger.error("Response body: " + e.getResponseBody());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}		
	}
	
	public static void patchUserSecurityPolicy( String uspName, String value) throws Throwable {
		logger.debug("[K8S ApiCaller] patchUserSecurityPolicy Service Start");
		logger.debug("uspName : " + uspName);
		logger.debug("otp value : " + value);
		
		DateTime currentTime = new DateTime();
		logger.debug("Current Time : " + currentTime );
			
		try {
			String jsonPatchStr = "[{\"op\":\"replace\",\"path\":\"/otp\",\"value\": " + Integer.parseInt(value) + " },"
					+ "{\"op\":\"replace\",\"path\":\"/otpRegisterTime\",\"value\": \"" + currentTime.toString() + "\" }]";
			logger.debug("JsonPatchStr: " + jsonPatchStr);

			JsonElement jsonPatch = (JsonElement) new JsonParser().parse(jsonPatchStr);
			customObjectApi.patchClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_USER_SECURITY_POLICY, uspName, jsonPatch);
		
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			e.printStackTrace();
			throw e;
		}
		
		
	}
	
	public static void patchUserResourceVersionConfig( String userId, String value) throws Throwable {
		logger.debug("[K8S ApiCaller] patchUserResourceVersionConfig Service Start");
		logger.debug("UserId : " + userId);
		logger.debug("resourceVersion value : " + value);
		
		DateTime currentTime = new DateTime();
		logger.debug("Current Time : " + currentTime );
			
		try {
			String jsonPatchStr = "[{\"op\":\"replace\",\"path\":\"/metadata/resourceVersion\",\"value\": " + value + " }]";
			logger.debug("JsonPatchStr: " + jsonPatchStr);

			JsonElement jsonPatch = (JsonElement) new JsonParser().parse(jsonPatchStr);
			customObjectApi.patchClusterCustomObject(Constants.CUSTOM_OBJECT_GROUP, Constants.CUSTOM_OBJECT_VERSION, Constants.CUSTOM_OBJECT_PLURAL_USER, userId, jsonPatch);
		
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			e.printStackTrace();
			throw e;
		}
		
		
	}

	public static void createDefaultNetPol(NamespaceClaim claim) throws Exception {
		// Get Default Network Policy Yaml
		try {
			V1ConfigMap netPolConfig = api.readNamespacedConfigMap(Constants.DEFAULT_NETWORK_POLICY_CONFIG_MAP, Constants.TEMPLATE_NAMESPACE, null, null, null);
			if (netPolConfig != null && netPolConfig.getData() != null && netPolConfig.getData().get(Constants.NETWORK_POLICY_YAML) != null ) {
				String netPolYamlString = netPolConfig.getData().get(Constants.NETWORK_POLICY_YAML);
				JsonObject netPolJsonObject = Util.yamlStringToJsonObject (netPolYamlString);
		        mapper.registerModule(new JodaModule());
		        V1NetworkPolicy netPol = mapper.readValue((new Gson()).toJson(netPolJsonObject), new TypeReference<V1NetworkPolicy>() {
				});
		        
				logger.debug("netPol : " + netPol );
				
				netPol.getMetadata().setNamespace(claim.getResourceName());
				netApi.createNamespacedNetworkPolicy(claim.getResourceName(), netPol, null, null, null);						    
			} else {
				logger.debug("default networkPolicy is not set yet" );
			}
		} catch (ApiException e) {
			if (e.getResponseBody().contains("Not Found") || e.getResponseBody().contains("404")) {
				// Make ConfigMap default-networkpolicy-configmap in hypercloud4-system Namespace
				V1ConfigMap configMap = new V1ConfigMap();
				V1ObjectMeta metadata = new V1ObjectMeta();
				metadata.setName(Constants.DEFAULT_NETWORK_POLICY_CONFIG_MAP);
				metadata.setNamespace(Constants.TEMPLATE_NAMESPACE);
				configMap.setMetadata(metadata);
				Map<String, String> data = new HashMap<>();
				data.put(Constants.NETWORK_POLICY_YAML, null);
				configMap.setData(data);
				try {
					api.createNamespacedConfigMap(Constants.TEMPLATE_NAMESPACE, configMap, null, null, null);
				} catch (ApiException e1) {
					logger.error(e1.getResponseBody());
					e1.printStackTrace();
					throw e1;
				}
			} else {
				logger.error(e.getResponseBody());
				e.printStackTrace();
				throw e;
			}
		} catch (Exception e ) {
			logger.error(e.getStackTrace().toString());
			e.printStackTrace();
			throw e;
		}

	}

	@SuppressWarnings("unchecked")
	public static void patchLabel( String resourceName, String label, String value, String cumtomObjectResource, boolean isNamespaced, String namespace ) throws ApiException {
		JsonArray patchArray = new JsonArray();
		JsonObject patch = new JsonObject();
		patch.addProperty("op", "replace");
		patch.addProperty("path", "/metadata/labels/" + label);
		patch.addProperty("value", value);
		patchArray.add(patch);
		
		logger.debug( "Patch Object : " + patchArray );
		
		try {
			if ( !isNamespaced) {
				customObjectApi.patchClusterCustomObject(
						Constants.CUSTOM_OBJECT_GROUP, 
						Constants.CUSTOM_OBJECT_VERSION, 
						cumtomObjectResource, 
						resourceName, 
						patchArray );
			} else {
				customObjectApi.patchNamespacedCustomObject(Constants.CUSTOM_OBJECT_GROUP, 
						Constants.CUSTOM_OBJECT_VERSION, namespace, cumtomObjectResource, resourceName, patchArray);
			}		
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			logger.error("ApiException Code: " + e.getCode());
			throw e;
		}
	}


//	public static void updateClusterRoleBindingOfGroup(String userGroupName, String userId) throws Exception {
//		V1ClusterRoleBindingList crbList = null;
//		boolean flag = false;
//		try{
//			crbList = rbacApi.listClusterRoleBinding("true", false, null, null, null, 1000 , null, 60, false);
//			for (V1ClusterRoleBinding item : crbList.getItems()) {
//				List<V1Subject> subjects = item.getSubjects();
////				V1RoleRef roleRef = item.getRoleRef();
//				if (subjects != null) {
//					for( V1Subject subject : subjects ) {
//						if ( subject.getKind().equalsIgnoreCase("Group")) {
//							if( subject.getName().equalsIgnoreCase( userGroupName )) {
//								// 유저 이름으로 만들어진 해당 클러스터 롤이 존재하는지 파악
//								for( V1Subject subject2 : subjects ) {
//									if ( subject2.getKind().equalsIgnoreCase("User")) {
//										if( subject2.getName().equalsIgnoreCase( userId )) {
//											flag = true;  // 이미 만들어짐
//										}
//									}
//								}
//								if (!flag) {
//									// 존재하지 않으면 subject 추가해준다 만들어준다
//									V1Subject newSubject = new V1Subject();
//									newSubject.setKind("User");
//									newSubject.setApiGroup("rbac.authorization.k8s.io");
//									newSubject.setName( userId );
//									item.addSubjectsItem(newSubject);
//									rbacApi.replaceClusterRoleBinding(item.getMetadata().getName(), item, null, null, null);
//								}	
//							}
//						}
//					}	
//				}			
//			}
//		} catch (Exception e) {
//			logger.info(e.getMessage());
//			throw e;
//		} finally {
//		}
//		
//	}
//
//	public static void deleteClusterRoleBindingOfGroup(Set<String> keySet, String userId) throws Exception {
//		V1ClusterRoleBindingList crbList = null;
//		boolean flag = false;
//		try{
//			crbList = rbacApi.listClusterRoleBinding("true", false, null, null, null, 1000 , null, 60, false);
//			for (V1ClusterRoleBinding item : crbList.getItems()) {
//				List<V1Subject> subjects = item.getSubjects();
////				V1RoleRef roleRef = item.getRoleRef();
//				if (subjects != null) {
//					for( int i=0 ; i < subjects.size(); i++) {   
//						V1Subject subject = subjects.get(i);
//						if ( subject.getKind().equalsIgnoreCase("User")) {
//							if( subject.getName().equalsIgnoreCase( userId )) {
//								// user로 맵핑 된 clusterRoleBinding 중  Group을 역시나 가지고 있는애가 있는지 파악
//								for( V1Subject subject2 : subjects ) {
//									if ( subject2.getKind().equalsIgnoreCase("Group")) {
//										for (String userGroupName : keySet) {
//											if( subject2.getName().equalsIgnoreCase( userGroupName )) {
//												flag = true;  // 존재함
//											}
//										}
//										if (!flag) {
//											// 존재하지 subject를 지우고 replace를 해라
//											subjects.remove(i);
//											item.setSubjects(subjects);
//											rbacApi.replaceClusterRoleBinding(item.getMetadata().getName(), item, null, null, null);
//										}
//										
//										
//									}
//								}
//							}
//						}
//					}	
//				}			
//			}
//		} catch (Exception e) {
//			logger.info(e.getMessage());
//			throw e;
//		} finally {
//		}
//	}
}
