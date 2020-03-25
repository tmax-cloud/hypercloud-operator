package k8s.example.client;

public class Constants {
	public static final String ISSUER = "Tmax-ProAuth-WebHook";
	public static final String ACCESS_TOKEN_SECRET_KEY = "Access-Token-Secret-Key";
	public static final String REFRESH_TOKEN_SECRET_KEY = "Refresh-Token-Secret-Key";
	public static final String TEMPLATE_NAMESPACE = "hypercloud4-system";
	
	public static final String CUSTOM_OBJECT_GROUP = "tmax.io";
	public static final String CUSTOM_OBJECT_VERSION = "v1";
	public static final String CUSTOM_OBJECT_PLURAL_USER = "users";
	public static final String CUSTOM_OBJECT_PLURAL_TOKEN = "tokens";
	public static final String CUSTOM_OBJECT_PLURAL_TEMPLATE = "templates";
	public static final String CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE = "templateinstances";
	public static final String CUSTOM_OBJECT_PLURAL_CLIENT = "clients";
	public static final String CUSTOM_OBJECT_KIND_TEMPLATE_INSTANCE = "TemplateInstance";
	public static final String CUSTOM_OBJECT_PLURAL_REGISTRY = "registries";
	public static final String CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM = "namespaceclaims";
	public static final String CUSTOM_OBJECT_PLURAL_RESOURCEQUOTACLAIM = "resourcequotaclaims";
	public static final String CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM = "rolebindingclaims";
	
	public static final String CLUSTER_ROLE_NAMESPACE_OWNER = "namespace-owner";
	public static final String CLUSTER_ROLE_NAMESPACE_USER = "namespace-user";
	public static final String NAMESPACE_OWNER_LABEL = "ownerUserName";
	
	public static final String MASTER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUbWF4LVByb0F1dGgtV2ViSG9vayIsImlkIjoid3ltaW4tdG1heC5jby5rciIsImV4cCI6MTU4MzEyMTQ5M30.hjvrlaLDFuSjchJKarGKbuWOuafhsuCQgBDo-pqsZvg";
	public static final String MASTER_USER_ID = "admin@tmax.co.kr";
	public static final int ACCESS_TOKEN_EXP_TIME = 3600; // 1 hour
	public static final int REFRESH_TOKEN_EXP_TIME = 604800; // 7 days
	
	public static final String CLAIM_USER_ID = "id";
	public static final String CLAIM_TOKEN_ID = "tokenId";
	

	public static final String K8S_PREFIX = "hpcd-";
	public static final String K8S_REGISTRY_PREFIX = "registry-";
	public static final String REGISTRY_CPU_STRING = "0.2";
	public static final String REGISTRY_MEMORY_STRING = "512Mi";

	// OpenSSL Certificate Home Directory
	public static final String OPENSSL_HOME_DIR = "/openssl";

	// OpenSSL Cert File Name
	public static final String GEN_CERT_SCRIPT_FILE = "genCert.sh";
	public static final String CERT_KEY_FILE = "tmax.key";
	public static final String CERT_CRT_FILE = "tmax.crt";
	public static final String CERT_CERT_FILE = "tmax.cert";
	public static final String DOCKER_DIR = "/etc/docker";
	public static final String DOCKER_CERT_DIR = "/etc/docker/certs.d";
	
	// Docker Login Config
	public static final String DOCKER_LOGIN_HOME_DIR = "/root/.docker";
	public static final String DOCKER_CONFIG_FILE = "config.json";
	public static final String DOCKER_CONFIG_JSON_FILE = ".dockerconfigjson";
	
	// Shared Option
	public static final int SHARE_ONLY_THIS_DOMAIN = 0;
	public static final int SHARE_OTHER_DOMAINS = 1;
	 
	// Secret Type
	public static final String K8S_SECRET_TYPE_DOCKER_CONFIG_JSON = "kubernetes.io/dockerconfigjson";
	
	// Status
	public static final String STATUS_RUNNING = "Running";
	public static final String STATUS_ERROR = "Error";
	
	// Custom Object Event Type
	public static final String EVENT_TYPE_ADDED = "ADDED";
	public static final String EVENT_TYPE_MODIFIED = "MODIFIED";
	public static final String EVENT_TYPE_DELETED = "DELETED";
	
	// Namespace Claim Status
	public static final String CLAIM_STATUS_AWAITING = "Awaiting";
	public static final String CLAIM_STATUS_SUCCESS = "Success";
	public static final String CLAIM_STATUS_REJECT = "Reject";
	public static final String CLAIM_STATUS_ERROR = "Error";
	
	// LoginPage of HyperCloud4
	public static final String LOGIN_PAGE_URI = "http://192.168.8.36/oauth/login.html";
	
	// Metering Cron Expression
	public static final String METERING_CRON_EXPRESSION = "0 0/5 * 1/1 * ? *";
	
	// Metering Get Query Parameters
	public static final String QUERY_PARAMETER_OFFSET = "offset";
	public static final String QUERY_PARAMETER_LIMIT = "limit";
	public static final String QUERY_PARAMETER_NAMESPACE = "namespace";
	public static final String QUERY_PARAMETER_TIMEUNIT = "timeUnit";
	public static final String QUERY_PARAMETER_STARTTIME = "startTime";
	public static final String QUERY_PARAMETER_ENDTIME = "endTime";
	public static final String QUERY_PARAMETER_SORT = "sort";
	
	// Mysql DB Connection
	public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	public static final String DB_URL = "jdbc:mysql://mysql-service.hypercloud4-system:3306/metering?useSSL=false";
	public static final String USERNAME = "root";
}
