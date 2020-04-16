package k8s.example.client;

public class Constants {
	public static final String ISSUER = "Tmax-ProAuth-WebHook";
//	public static final String ACCESS_TOKEN_SECRET_KEY = "Access-Token-Secret-Key";
	public static final String ACCESS_TOKEN_SECRET_KEY = "ProAuth_Secret";
	public static final String REFRESH_TOKEN_SECRET_KEY = "Refresh-Token-Secret-Key";

	public static final String K8S_PREFIX = "hpcd-";
	
	public static final String TEMPLATE_NAMESPACE = "hypercloud4-system";
	public static final String DEFAULT_NAMESPACE = "default";
	public static final String SYSTEM_ENV_CATALOG_NAMESPACE = "CATALOG_NAMESPACE";
	public static final String REGISTRY_NAMESPACE = "hypercloud4-system";

	public static final String CUSTOM_OBJECT_GROUP = "tmax.io";
	public static final String CUSTOM_OBJECT_VERSION = "v1";
	public static final String CUSTOM_OBJECT_PLURAL_USER = "users";
	public static final String CUSTOM_OBJECT_PLURAL_TOKEN = "tokens";
	public static final String CUSTOM_OBJECT_PLURAL_TEMPLATE = "templates";
	public static final String CUSTOM_OBJECT_PLURAL_TEMPLATE_INSTANCE = "templateinstances";
	public static final String CUSTOM_OBJECT_PLURAL_CLIENT = "clients";
	public static final String CUSTOM_OBJECT_KIND_TEMPLATE_INSTANCE = "TemplateInstance";
	public static final String CUSTOM_OBJECT_PLURAL_REGISTRY = "registries";
	public static final String CUSTOM_OBJECT_PLURAL_IMAGE = "images";
	public static final String CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM = "namespaceclaims";
	public static final String CUSTOM_OBJECT_PLURAL_RESOURCEQUOTACLAIM = "resourcequotaclaims";
	public static final String CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM = "rolebindingclaims";
	
	public static final String SERVICE_INSTANCE_API_GROUP = "servicecatalog.k8s.io";
	public static final String SERVICE_INSTANCE_API_VERSION = "v1beta1";
	public static final String SERVICE_INSTANCE_PLURAL = "serviceinstances";
	public static final String SERVICE_INSTANCE_KIND = "ServiceInstance";

	public static final String RBAC_API_GROUP = "rbac.authorization.k8s.io";

	// HTTPS REQUEST URL
	public static final String HTTPS_SCHEME_PREFIX = "https://";
	
	public static final String CLUSTER_ROLE_NAMESPACE_OWNER = "namespace-owner";
	public static final String CLUSTER_ROLE_NAMESPACE_USER = "namespace-user";
	public static final String NAMESPACE_OWNER_LABEL = "ownerUserName";
	
	public static final String MASTER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiY2x1c3Rlci1hZG1pbiIsInRva2VuSWQiOiJ3b29AdG1heC5jby5rciIsImlzcyI6IlRtYXgtUHJvQXV0aC1XZWJIb29rIiwiaWQiOiJhZG1pbkB0bWF4LmNvLmtyIiwiZXhwIjoxNzQzMzAxNDM1fQ.ls9Cj1BX4NPJ3XxxHwcrGDzveaaqsauMo5L4e5BfUnE";
	public static final String MASTER_USER_ID = "admin@tmax.co.kr";
	public static final int ACCESS_TOKEN_EXP_TIME = 3600; // 1 hour
	public static final int REFRESH_TOKEN_EXP_TIME = 604800; // 7 days
	
	public static final String CLAIM_USER_ID = "id";
	public static final String CLAIM_USER_GROUP_NAME = "groupName";
	public static final String CLAIM_TOKEN_ID = "tokenId";
	public static final String CLAIM_ROLE = "role";
	
	public static final String ROLE_ADMIN = "cluster-admin";
	public static final String ROLE_OWNER = "namespace-owner";
	public static final String ROLE_USER = "namespace-user";

	// Registry
	public static final String REGISTRY_CONFIG_MAP_NAME = "registry-config";
	
	public static final String K8S_REGISTRY_PREFIX = "registry-";
	public static final String REGISTRY_CPU_STRING = "0.2";
	public static final String REGISTRY_MEMORY_STRING = "512Mi";

	// OpenSSL Certificate Home Directory
	public static final String OPENSSL_HOME_DIR = "/openssl";

	// OpenSSL Cert File Name
	public static final String GEN_CERT_SCRIPT_FILE = "genCert.sh";
	public static final String CERT_KEY_FILE = "localhub.key";
	public static final String CERT_CRT_FILE = "localhub.crt";
	public static final String CERT_CERT_FILE = "localhub.cert";
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
	public static final String K8S_SECRET_TYPE_OPAQUE = "opaque";
	
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
	
	// Custom Resource Annoatation To Know Modified Fields
	public static final String LAST_CUSTOM_RESOURCE = CUSTOM_OBJECT_GROUP + "/last-custom-resource";
	
	// LoginPage of HyperCloud4
	public static final String LOGIN_PAGE_URI = "http://192.168.8.36/oauth/login.html";
	
	// User of HyperCloud4
	public static final String LOGIN_SUCCESS = "Login Success";
	public static final String LOGOUT_SUCCESS = "Logout Success";
	public static final String REFRESH_SUCCESS = "Refresh Success";
	public static final String PASSWORD_CHANGE_SUCCESS = "Password Change Success";
	public static final String LOGIN_FAILED = "Wrong ID or Password";
	public static final String LOGOUT_FAILED = "Log Out Failed, Token is not Exist or Valid";
	public static final String REFRESH_FAILED = "Refresh Failed, Refresh Token is not Valid";
	public static final String PASSWORD_CHANGE_FAILED = "Password Change Failed";
	public static final String EMPTY_PASSWORD = "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e";
	
	// User Status
	public static final String USER_STATUS_BLOCKED = "blocked";
	public static final String USER_STATUS_ACTIVE = "active";

	// User Delete Cron Expression
//	public static final String USER_DELETE_CRON_EXPRESSION = "0 0/5 * 1/1 * ? *"; // every 5 mins for test
	public static final String USER_DELETE_CRON_EXPRESSION = "0 0 4 ? * MON *"; // every monday 04:00am
	
	// Metering Cron Expression
	public static final String METERING_CRON_EXPRESSION = "0 0/5 * 1/1 * ? *"; // sec, mins, hrs, dom(day of month), month, dow(day of week)
	
	// Metering Get Query Parameters
	public static final String QUERY_PARAMETER_OFFSET = "offset";
	public static final String QUERY_PARAMETER_LIMIT = "limit";
	public static final String QUERY_PARAMETER_NAMESPACE = "namespace";
	public static final String QUERY_PARAMETER_TIMEUNIT = "timeUnit";
	public static final String QUERY_PARAMETER_STARTTIME = "startTime";
	public static final String QUERY_PARAMETER_ENDTIME = "endTime";
	public static final String QUERY_PARAMETER_SORT = "sort";
	
	// User Get Query Parameters
	public static final String QUERY_PARAMETER_ID = "id";
	public static final String QUERY_PARAMETER_MODE = "mode";
	public static final String QUERY_PARAMETER_EMAIL = "email";
	
	// Mysql DB Connection
	public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	public static final String DB_URL = "jdbc:mysql://mysql-service.hypercloud4-system:3306/metering?useSSL=false";
	public static final String USERNAME = "root";
	
	// Default Service Classes Values
	public static final String DEFAULT_IMAGE_URL = "https://folo.co.kr/img/gm_noimage.png";
	public static final String DEFAULT_PROVIDER = "tmax";
	public static final String DEFAULT_TAGS = "etc";
	
	// Oauth 
	public static final String OAUTH_URL = "http://proauth-server-service.proauth-system:8080/";
	public static final String USER_CREATE_SUCCESS = "User Create Success";
	public static final String USER_CREATE_FAILED = "User Create Failed";
	public static final String USER_UPDATE_SUCCESS = "User Update Success";
	public static final String USER_UPDATE_FAILED = "User Update Failed";
	public static final String USER_PASSWORD_FIND_SUCCESS = "User Password Change & Send Email Success";
	public static final String USER_PASSWORD_FIND_FAILED = "User Password Change & Send Email Failed";
	public static final String USER_ID_FIND_FAILED = "User ID Find Failed";
	public static final String SERVICE_NAME_OAUTH_USER_LIST = "proauth/oauth/usersList";
	public static final String SERVICE_NAME_OAUTH_USER_DETAIL = "proauth/oauth/user-details/";
	public static final String SERVICE_NAME_OAUTH_USER_CREATE = "proauth/oauth/users";
	public static final String SERVICE_NAME_OAUTH_USER_DELETE = "proauth/oauth/users/";
	public static final String SERVICE_NAME_OAUTH_USER_UPDATE = "proauth/oauth/users/";
	public static final String SERVICE_NAME_OAUTH_AUTHENTICATE_CREATE = "proauth/oauth/authenticate";
	public static final String SERVICE_NAME_OAUTH_AUTHENTICATE_DELETE = "proauth/oauth/authenticate";
	public static final String SERVICE_NAME_OAUTH_AUTHENTICATE_UPDATE = "proauth/oauth/authenticate";
	public static final String SERVICE_NAME_OAUTH_CONFIGURATION_UPDATE = "proauth/oauth/configuration";
	public static final String SERVICE_NAME_SET_PASSWORD_SERVICE = "proauth/oauth/setPassword";
	public static final String SERVICE_NAME_WEBHOOK_SAMPLE = "proauth/oauth/webhook-authenticate";
	
	// Template Parameter Data Type
	public static final String TEMPLATE_DATA_TYPE_NUMBER = "number";
	public static final String TEMPLATE_DATA_TYPE_STRING = "string";
	
	
}
