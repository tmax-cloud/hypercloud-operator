package k8s.example.client;

public class Constants {
	public static final String ISSUER = "Tmax-ProAuth";
	public static final String ACCESS_TOKEN_SECRET_KEY = "Access-Token-Secret-Key";
//	public static final String ACCESS_TOKEN_SECRET_KEY = "ProAuth_Secret";
	public static final String REFRESH_TOKEN_SECRET_KEY = "Refresh-Token-Secret-Key";

	public static final String K8S_PREFIX = "hpcd-";
	
	public static final String TEMPLATE_NAMESPACE = "hypercloud4-system";
	public static final String DEFAULT_NAMESPACE = "default";
	public static final String SYSTEM_ENV_CATALOG_NAMESPACE = "CATALOG_NAMESPACE";
	public static final String REGISTRY_NAMESPACE = "hypercloud4-system";
	public static final String PREFIX_RESOURCE_VERSION_CONFIGMAP = "resourceversion-";

	public static final String UI_CUSTOM_OBJECT_GROUP = "ui.tmax.io";
	public static final String CUSTOM_OBJECT_GROUP = "tmax.io";
	public static final String CUSTOM_OBJECT_VERSION = "v1";
	public static final String CUSTOM_OBJECT_PLURAL_USER = "users";
	public static final String CUSTOM_OBJECT_PLURAL_USER_SECURITY_POLICY = "usersecuritypolicies";
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
	public static final String CUSTOM_OBJECT_PLURAL_CATALOGSERVICECLAIM = "catalogserviceclaims";
	
	public static final String PLURAL_REGISTRY_REPLICASET = "registriesreplicasets";
	public static final String PLURAL_REGISTRY_POD = "registriespods";
	public static final String PLURAL_REGISTRY_SERVICE = "registriesservices";
	public static final String PLURAL_REGISTRY_CERT = "registriescerts";
	public static final String PLURAL_REGISTRY_DOCKER = "registriesdockers";
	public static final String PLURAL_REGISTRY_TLS = "registriestls";
	public static final String PLURAL_REGISTRY_INGRESS = "registriesingresses";
	public static final String PLURAL_REGISTRY_PVC = "registriespvcs";
	public static final String PLURAL_JOIN_FED = "joinfeds";
	public static final String PLURAL_CAPI_CLUSTER = "capiclusters";

	public static final String PLURAL_SECRET = "secrets";

	public static final String ISTIO_API_GROUP = "config.istio.io";
	public static final String SERVICE_INSTANCE_API_GROUP = "servicecatalog.k8s.io";
	public static final String SERVICE_INSTANCE_API_VERSION = "v1beta1";
	public static final String SERVICE_INSTANCE_PLURAL = "serviceinstances";
	public static final String SERVICE_INSTANCE_KIND = "ServiceInstance";

	public static final String RBAC_API_GROUP = "rbac.authorization.k8s.io";
	public static final String CORE_API_GROUP = "''";
	public static final String STORAGE_API_GROUP = "storage.k8s.io";

	//federation
	public static final String FED_OBJECT_GROUP = "core.kubefed.io";
	public static final String FED_OBJECT_VERSION =  "v1beta1";
	public static final String FED_OBJECT_FEDCLUSTER_PLURAL = "kubefedclusters";
	public static final String FED_OBJECT_RESOURCE_GROUP = "types.kubefed.io";
	public static final String FED_OBJECT_RESOURCE_VERSION = "v1beta1";
	public static final String FED_OBJECT_RESOURCE_SERVICE_PLURAL = "federatedservices";

	//capi
	public static final String CAPI_OBJECT_GROUP = "cluster.x-k8s.io";
	public static final String CAPI_OBJECT_VERSION =  "v1alpha3";
	public static final String CAPI_OBJECT_PLURAL_CAPICLUSTER = "clusters";

	//externalDNS
	public static final String EXTERNAL_OBJECT_GROUP = "multiclusterdns.kubefed.io";
	public static final String EXTERNAL_OBJECT_VERSION = "v1alpha1";
	public static final String EXTERNAL_OBJECT_PLURAL_SERVICEDNSRECORD = "servicednsrecords";
	public static final String EXTERNAL_OBJECT_KIND_SERVICEDNSRECORD = "ServiceDNSRecord";
	public static final String EXTERNAL_OBJECT_PLURAL_DOMAIN = "domains";

	//role
	public static final String INGRESS_NGINX_SHARED_NAMESPACE = "ingress-nginx-shared";
	public static final String INGRESS_NGINX_SHARED_READ_ROLE_BINDING = "ingress-nginx-shared-read";
	public static final String INGRESS_NGINX_SHARED_READ_CLUSTER_ROLE = "ingress-nginx-shared-read-clusterrole";

	
	// HTTPS REQUEST URL
	public static final String HTTPS_SCHEME_PREFIX = "https://";
	
	public static final String CLUSTER_ROLE_NAMESPACE_OWNER = "namespace-owner";
	public static final String CLUSTER_ROLE_NAMESPACE_USER = "namespace-user";
	public static final String NAMESPACE_OWNER_LABEL = "ownerUserName";
	
	public static final String MASTER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiY2x1c3Rlci1hZG1pbiIsInRva2VuSWQiOiJ3b29AdG1heC5jby5rciIsImlzcyI6IlRtYXgtUHJvQXV0aC1XZWJIb29rIiwiaWQiOiJhZG1pbkB0bWF4LmNvLmtyIiwiZXhwIjoxNzQzMzAxNDM1fQ.ls9Cj1BX4NPJ3XxxHwcrGDzveaaqsauMo5L4e5BfUnE";
	public static final String MASTER_USER_ID = "admin-tmax.co.kr";
	public static final int ACCESS_TOKEN_EXP_TIME = 3600; // 1 hour
	public static final int REFRESH_TOKEN_EXP_TIME = 604800; // 7 days
	
	public static final String TOKEN_EXPIRED_TIME_KEY = "TokenExpiredTime";
	public static final String REFRESH_TOKEN_EXPIRED_TIME_KEY = "RefreshTokenExpiredTime";
	
	public static final String CLAIM_PREFERRED_USERNAME = "preferred_username";
	public static final String CLAIM_USER_ID = "id";
	public static final String CLAIM_ISSUER = "iss";
	public static final String CLAIM_USER_GROUP_NAME = "groupName";
	public static final String CLAIM_AUDIENCE = "aud";
	public static final String CLAIM_TOKEN_ID = "tokenId";
	public static final String CLAIM_ROLE = "role";
	
	public static final String ROLE_ADMIN = "cluster-admin";
	public static final String ROLE_OWNER = "namespace-owner";
	public static final String ROLE_USER = "namespace-user";

	// Registry
	public static final String REGISTRY_CONFIG_MAP_NAME = "registry-config";
	
	public static final String K8S_REGISTRY_PREFIX = "registry-";
	public static final String K8S_TLS_PREFIX = "tls-";
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
	public static final String K8S_SECRET_TYPE_TLS = "kubernetes.io/tls";
	
	// Secret TLS Data Key
	public static final String K8S_SECRET_TLS_CRT = "tls.crt";
	public static final String K8S_SECRET_TLS_KEY = "tls.key";
	
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
	public static final String CLAIM_STATUS_PENDING = "Pending";
	public static final String CLAIM_STATUS_REJECT = "Reject";
	public static final String CLAIM_STATUS_ERROR = "Error";
	public static final String CLAIM_STATUS_DELETED = "Deleted";

	// Custom Resource Annoatation To Know Modified Fields
	public static final String LAST_CUSTOM_RESOURCE = CUSTOM_OBJECT_GROUP + "/last-custom-resource";
	public static final String UPDATING_FIELDS = CUSTOM_OBJECT_GROUP + "/updating-fields";
	
	// LoginPage of HyperCloud4
	public static final String LOGIN_PAGE_URI = "http://192.168.8.36/oauth/login.html";
	
	// User of HyperCloud4
	public static final String LOGIN_SUCCESS = "Login Success";
	public static final String LOGOUT_SUCCESS = "Logout Success";
	public static final String REFRESH_SUCCESS = "Refresh Success";
	public static final String PASSWORD_CHANGE_SUCCESS = "Password Change Success";
	public static final String WRONG_OTP_NUMBER = "Wrong OTP Number";
	public static final String OTP_TIME_EXPIRED = "OTP Verification Time has Expired";
	public static final String LOGIN_FAILED = "Wrong ID or Password";
	public static final String OTP_ERROR = "OTP validation Error Occurred";
	public static final String WRONG_PASSWORD = "Wrong Password";
	public static final String LOGOUT_FAILED = "Log Out Failed, Token is not Exist or Valid";
	public static final String REFRESH_FAILED = "Refresh Failed, Refresh Token is not Valid";
	public static final String EXPIRE_TIME_UPDATE_FAILED = "Expire Time Update Failed";
	public static final String PASSWORD_CHANGE_FAILED = "Password Change Failed";
	public static final String EMPTY_PASSWORD = "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e";
	public static final String SECRET_VERIFICATAION_CODE = "verification-code-for-user";
	public static final int MAIL_VERIFICATAION_DURATION_MINUTES = 30;
	public static final int OTP_VERIFICATAION_DURATION_MINUTES = 10;
	public static final String OPERATOR_TOKEN_EXPIRE_TIME = "operator-token-expire-time";
	public static final String SECRET_MAIL_PASSWORD = "secret-mail-password";
	
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
	public static final String QUERY_PARAMETER_RESOURCE = "resource";
	public static final String QUERY_PARAMETER_CODE = "code";
	
	// Namespace 
	public static final String QUERY_PARAMETER_USER_ID = "userId";
	public static final String QUERY_PARAMETER_LABEL_SELECTOR = "labelSelector";
	public static final String QUERY_PARAMETER_CONTINUE = "continue";
	public static final String QUERY_PARAMETER_PERIOD = "period";
	public static final String TRIAL_PERIOD_EXTEND_FAILED = "Trial NameSpace Period Extend Failed";

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
	
	
	// hyperauth
	public static final String SERVICE_NAME_LOGIN_AS_ADMIN = "auth/realms/master/protocol/openid-connect/token";
	public static final String SERVICE_NAME_USER_DETAIL = "auth/admin/realms/tmax/users/";	
	public static final String SERVICE_NAME_USER_DETAIL_WITHOUT_TOKEN = "auth/realms/tmax/user/";	
	public static final String HYPERAUTH_URL = "http://hyperauth.hyperauth";
	
	// Oauth 
	public static final String OAUTH_URL = "http://proauth-server-service.proauth-system";
	public static final String USER_NEW_ROLE_CREATE_SUCCESS = "User New Role Create Success";
	public static final String USER_NEW_ROLE_CREATE_FAILED = "User New Role Create Failed";
	public static final String USER_NEW_ROLE_DELETE_SUCCESS = "User New Role Delete Success";
	public static final String USER_NEW_ROLE_DELETE_FAILED = "User New Role Delete Failed";
	public static final String USER_UPDATE_SUCCESS = "User Update Success";
	public static final String USER_UPDATE_FAILED = "User Update Failed";
	public static final String USER_EMAIL_DUPLICATION_VERIFY_SUCCESS = "User Email Duplication verify Success";
	public static final String USER_EMAIL_DUPLICATION_VERIFY_FAILED = "User EMAIL Duplication verify Failed";
	public static final String NAMESPACE_NAME_DUPLICATION_VERIFY_FAILED = "Namespace Duplication verify Failed";
	public static final String NAMESPACE_NAME_DUPLICATION_VERIFY_SUCCESS = "Namespace Duplication verify Success";
	public static final String USER_ID_DUPLICATION_VERIFY_SUCCESS = "User ID Duplication verify Success";
	public static final String USER_ID_DUPLICATION_VERIFY_FAILED = "User ID Duplication verify Failed";
	public static final String USER_PASSWORD_FIND_SUCCESS = "User Password Change & Send Email Success";
	public static final String USER_PASSWORD_FIND_FAILED = "User Password Change & Send Email Failed";
	public static final String USER_ID_FIND_FAILED = "User ID Find Failed";
	public static final String USER_EMAIL_VERIFICATION_NUMBER_SEND_FAIL = "User Email verification number send service fail";
	public static final String USER_EMAIL_VERIFY_FAIL = "User Email verify service fail";
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
	
	//html
	public static final String OTP_VERIFICATION_CONTENTS = "<!DOCTYPE html>\r\n" + 
			"<html lang=\"en\">\r\n" + 
			"<head>\r\n" + 
			"    <meta charset=\"UTF-8\">\r\n" + 
			"    <title>[인증번호 : %%otpCode%%] 인증 번호를 알려드립니다.</title>\r\n" +
			"</head>\r\n" + 
			"<body>\r\n" + 
			"<div style=\"border: #c5c5c8 0.06rem solid; border-bottom: 0; width: 42.5rem; height: 50.94rem; padding: 0 1.25rem\">\r\n" + 
			"    <header>\r\n" + 
			"        <div style=\"margin: 0;\">\r\n" + 
			"            <p style=\"font-size: 1rem; font-weight: bold; color: #333333; line-height: 3rem; letter-spacing: 0; border-bottom: #c5c5c8 0.06rem solid;\">\r\n" + 
			"                인증번호 : %%otpCode%%\r\n" +
			"            </p>\r\n" + 
			"        </div>\r\n" + 
			"    </header>\r\n" + 
			"    <section>\r\n" + 
			"        <figure style=\"text-align: center;\">\r\n" + 
			"            <img style=\"margin: 2.38rem 0;\"\r\n" + 
			"                 src=\"cid:email-authentication\">\r\n" + 
			"        </figure>\r\n" + 
			"        <div style=\"width: 27.06rem; margin: 0 7.70rem;\">\r\n" + 
			"            <p style=\"font-size: 1.25rem; font-weight: bold; line-height: 3rem;\">\r\n" + 
			"                인증번호 %%otpCode%%\r\n" +
			"            </p>\r\n" + 
			"            <p style=\"line-height: 1.38rem;\">\r\n" +
			"                안녕하세요? <br>\r\n" +
			"                HyperCloud를 이용해 주셔서 감사합니다. <br>\r\n" +
			"                로그인 화면에서 인증번호를 입력해 주세요. <br>\r\n" +
			"                감사합니다. <br>\r\n" +
			"            </p>\r\n" + 
			"        </div>\r\n" + 
			"    </section>\r\n" + 
			"</div>\r\n" + 
			"<footer style=\"background-color: #3669B3; width: 45.12rem; height: 1.88rem; font-size: 0.75rem; color: #FFFFFF; display: flex;\r\n" + 
			"    align-items: center; justify-content: center;\">\r\n" + 
			"    <div>\r\n" + 
			"        COPYRIGHT2020. TMAX A&C., LTD. ALL RIGHTS RESERVED\r\n" + 
			"    </div>\r\n" + 
			"</footer>\r\n" + 
			"</body>\r\n" + 
			"</html>";
	public static final String TRIAL_TIME_OUT_CONTENTS = "<!DOCTYPE html>\r\n" + 
			"<html lang=\"en\">\r\n" + 
			"<head>\r\n" + 
			"    <meta charset=\"UTF-8\">\r\n" + 
			"    <title>HyperCloud 서비스 기간 만료 안내 알림</title>\r\n" +
			"</head>\r\n" + 
			"<body>\r\n" + 
			"<div style=\"border: #c5c5c8 0.06rem solid; border-bottom: 0; width: 42.5rem; height: 43.19rem; padding: 0 1.25rem\">\r\n" + 
			"    <header>\r\n" + 
			"        <div style=\"margin: 0;\">\r\n" + 
			"            <p style=\"font-size: 1rem; font-weight: bold; color: #333333; line-height: 3rem; letter-spacing: 0; border-bottom: #c5c5c8 0.06rem solid;\">\r\n" + 
			"                HyperCloud 서비스 기간 만료 안내 알림\r\n" +
			"            </p>\r\n" + 
			"        </div>\r\n" + 
			"    </header>\r\n" + 
			"    <section>\r\n" + 
			"        <figure style=\"text-align: center;\">\r\n" + 
			"            <img style=\"margin: 2.38rem 0;\"\r\n" + 
			"                 src=\"cid:service-timeout\">\r\n" + 
			"        </figure>\r\n" + 
			"        <div style=\"width: 34.44rem; margin: 0 4rem;\">\r\n" + 
			"<!--            <p style=\"font-size: 1.25rem; font-weight: bold; line-height: 3rem;\">-->\r\n" + 
			"<!--                인증번호 1256-->\r\n" +
			"<!--            </p>-->\r\n" + 
			"            <p style=\"line-height: 1.38rem;\">\r\n" +
			"                안녕하세요? <br>\r\n" +
			"                TmaxCloud를 이용해 주셔서 감사합니다. <br>\r\n" +
			"                고객님께서 사용중인 Trial 서비스가 <span style=\"color: #F26868;\">%%TRIAL_END_TIME%%</span>에 만료됩니다. <br>\r\n" +
			"                Trial 서비스 이용 만료 시 사용중인 네임스페이스의 리소스는 모두 삭제됩니다. <br>\r\n" +
			"                Trial 서비스 만료 기간 이전에 <span style=\"color: #187EE3;\">유료 서비스로 전환</span> 혹은 리소스를 백업해주시기 바랍니다. <br>\r\n" +
			"                <br>\r\n" +
			"                감사합니다. <br>\r\n" +
			"                TmaxCloud 드림.\r\n" +
			"            </p>\r\n" + 
			"        </div>\r\n" + 
			"    </section>\r\n" + 
			"</div>\r\n" + 
			"<footer style=\"background-color: #3669B3; width: 45.12rem; height: 1.88rem; font-size: 0.75rem; color: #FFFFFF; display: flex;\r\n" + 
			"    align-items: center; justify-content: center;\">\r\n" + 
			"    <div>\r\n" + 
			"        COPYRIGHT2020. TMAX A&C., LTD. ALL RIGHTS RESERVED\r\n" + 
			"    </div>\r\n" + 
			"</footer>\r\n" + 
			"</body>\r\n" + 
			"</html>";
	public static final String TRIAL_SUCCESS_CONFIRM_MAIL_CONTENTS = "<!DOCTYPE html>\r\n" + 
			"<html lang=\"en\">\r\n" + 
			"<head>\r\n" + 
			"    <meta charset=\"UTF-8\">\r\n" + 
			"    <title>HyperCloud 서비스 신청 승인 완료</title>\r\n" +
			"</head>\r\n" + 
			"<body>\r\n" + 
			"<div style=\"border: #c5c5c8 0.06rem solid; border-bottom: 0; width: 42.5rem; height: 53.82rem; padding: 0 1.25rem\">\r\n" + 
			"    <header>\r\n" + 
			"        <div style=\"margin: 0;\">\r\n" + 
			"            <p style=\"font-size: 1rem; font-weight: bold; color: #333333; line-height: 3rem; letter-spacing: 0; border-bottom: #c5c5c8 0.06rem solid;\">\r\n" + 
			"                HyperCloud 서비스 신청 승인 완료\r\n" +
			"            </p>\r\n" + 
			"        </div>\r\n" + 
			"    </header>\r\n" + 
			"    <section>\r\n" + 
			"        <figure style=\"text-align: center;\">\r\n" + 
			"            <img style=\"margin: 0.94rem 0;\"\r\n" + 
			"                 src=\"cid:trial-approval\">\r\n" + 
			"        </figure>\r\n" + 
			"        <div style=\"width: 35.70rem; margin: 0 2.75rem;\">\r\n" +
			"            <p style=\"font-size: 1.5rem; font-weight: bold; line-height: 3rem;\">\r\n" +
			"                축하합니다.\r\n" +
			"            </p>\r\n" +
			"            <p style=\"line-height: 1.38rem;\">\r\n" +
			"                고객님의 Trial 서비스 신청이 성공적으로 승인되었습니다. <br>\r\n" +
			"                지금 바로 티맥스의 소프트웨어와 검증을 거친 오픈소스 서비스를 결합한 클라우드 플랫폼, <br>\r\n" +
			"                HyperCloud를 이용해 보세요. <br>\r\n" +
			"                <br>\r\n" +
			"                네임스페이스 이름 : <span style=\"font-weight: 600;\">%%NAMESPACE_NAME%%</span> <br>\r\n" +
			"                Trial 기한 : %%TRIAL_START_TIME%% ~ %%TRIAL_END_TIME%% <br>\r\n" +
			"                <br>\r\n" +
			"                리소스 정보 <br>\r\n" +
			"                -CPU : 1 Core <br>\r\n" +
			"                -Memory : 4 GIB <br>\r\n" +
			"                -Storage : 4 GIB <br>\r\n" +
			"                <br>\r\n" +
			"<!--                <span style=\"font-weight: 600;\">승인사유</span> <br>-->\r\n" +
			"                <br>\r\n" +
			"\r\n" +
			"                감사합니다. <br>\r\n" +
			"                TmaxCloud 드림.\r\n" +
			"            </p>\r\n" +
			"            <p style=\"margin: 3rem 0;\">\r\n" +
			"                <a href=\"https://console.tmaxcloud.com\">Tmax Console 바로가기 ></a>\r\n" +
			"            </p>\r\n" + 
			"        </div>\r\n" + 
			"    </section>\r\n" + 
			"</div>\r\n" + 
			"<footer style=\"background-color: #3669B3; width: 45.12rem; height: 1.88rem; font-size: 0.75rem; color: #FFFFFF; display: flex;\r\n" + 
			"    align-items: center; justify-content: center;\">\r\n" + 
			"    <div>\r\n" + 
			"        COPYRIGHT2020. TMAX A&C., LTD. ALL RIGHTS RESERVED\r\n" + 
			"    </div>\r\n" + 
			"</footer>\r\n" + 
			"</body>\r\n" + 
			"</html>";
	public static final String TRIAL_FAIL_CONFIRM_MAIL_CONTENTS = "<!DOCTYPE html>\r\n" + 
			"<html lang=\"en\">\r\n" + 
			"<head>\r\n" + 
			"    <meta charset=\"UTF-8\">\r\n" + 
			"    <title>HyperCloud 서비스 신청 결과 알림</title>\r\n" +
			"</head>\r\n" + 
			"<body>\r\n" + 
			"<div style=\"border: #c5c5c8 0.06rem solid; border-bottom: 0; width: 42.5rem; height: 43.19rem; padding: 0 1.25rem\">\r\n" + 
			"    <header>\r\n" + 
			"        <div style=\"margin: 0;\">\r\n" + 
			"            <p style=\"font-size: 1rem; font-weight: bold; color: #333333; line-height: 3rem; letter-spacing: 0; border-bottom: #c5c5c8 0.06rem solid;\">\r\n" + 
			"                HyperCloud 서비스 신청 결과 알림\r\n" +
			"            </p>\r\n" + 
			"        </div>\r\n" + 
			"    </header>\r\n" + 
			"    <section>\r\n" + 
			"        <figure style=\"text-align: center;\">\r\n" + 
			"            <img style=\"margin: 0.94rem 0;\"\r\n" + 
			"                 src=\"cid:trial-disapproval\">\r\n" + 
			"        </figure>\r\n" + 
			"        <div style=\"width: 35.70rem; margin: 2rem 2.27rem;\">\r\n" + 
			"            <p style=\"line-height: 1.38rem;\">\r\n" + 
			"                안녕하세요? TmaxCloud 입니다. <br>\r\n" +
			"                TmaxCloud에 관심을 가져 주셔서 감사합니다. 고객님의 서비스 신청을 검토하였으며, <br>\r\n" +
			"                그 결과로 고객님의 서비스 신청이 승인되지 않았음을 알려드립니다. <br>\r\n" +
			"                비승인 사유는 아래와 같습니다. <br>\r\n" +
			"                <br>\r\n" +
			"                <span style=\"font-weight: 600;\">비승인 사유</span>\r\n" +
			"                <p>%%FAIL_REASON%%</p>\r\n" +
			"                <br>\r\n" +
			"                <br>\r\n" +
			"                감사합니다. <br>\r\n" +
			"                TmaxCloud 드림.\r\n" +
			"            </p>\r\n" + 
			"        </div>\r\n" + 
			"    </section>\r\n" + 
			"</div>\r\n" + 
			"<footer style=\"background-color: #3669B3; width: 45.12rem; height: 1.88rem; font-size: 0.75rem; color: #FFFFFF; display: flex;\r\n" + 
			"    align-items: center; justify-content: center;\">\r\n" + 
			"    <div>\r\n" + 
			"        COPYRIGHT2020. TMAX A&C., LTD. ALL RIGHTS RESERVED\r\n" + 
			"    </div>\r\n" + 
			"</footer>\r\n" + 
			"</body>\r\n" + 
			"</html>" ;
	public static final String VERIFY_MAIL_CONTENTS = "<!DOCTYPE html>\r\n" + 
			"<html lang=\"en\">\r\n" + 
			"<head>\r\n" + 
			"    <meta charset=\"UTF-8\">\r\n" + 
			"    <title>이메일을 인증해주세요.</title>\r\n" +
			"</head>\r\n" + 
			"<body>\r\n" + 
			"<div style=\"border: #c5c5c8 0.06rem solid; border-bottom: 0; width: 42.5rem; height: 50.94rem; padding: 0 1.25rem\">\r\n" + 
			"    <header>\r\n" + 
			"        <div style=\"margin: 0;\">\r\n" + 
			"            <p style=\"font-size: 1rem; font-weight: bold; color: #333333; line-height: 3rem; letter-spacing: 0; border-bottom: #c5c5c8 0.06rem solid;\">\r\n" + 
			"                [인증번호 : @@verifyNumber@@] 이메일을 인증해 주세요.\r\n" +
			"            </p>\r\n" + 
			"        </div>\r\n" + 
			"    </header>\r\n" + 
			"    <section>\r\n" + 
			"        <figure style=\"text-align: center;\">\r\n" + 
			"            <img style=\"margin: 2.38rem 0;\"\r\n" + 
			"                 src=\"cid:index\">\r\n" + 
			"        </figure>\r\n" + 
			"        <div style=\"width: 27.06rem; margin: 0 7.70rem;\">\r\n" + 
			"            <p style=\"font-size: 1.25rem; font-weight: bold; line-height: 3rem;\">\r\n" + 
			"                인증번호 @@verifyNumber@@\r\n" +
			"            </p>\r\n" +
			"            <p style=\"line-height: 1.38rem;\">\r\n" +
			"                안녕하세요? <br>\r\n" +
			"                TmaxCloud를 이용해 주셔서 감사합니다. <br>\r\n" +
			"                가입 화면에서 인증번호를 입력해 주세요. <br>\r\n" +
			"                감사합니다. <br>\r\n" +
			"            </p>\r\n" + 
			"        </div>\r\n" + 
			"    </section>\r\n" + 
			"</div>\r\n" + 
			"<footer style=\"background-color: #3669B3; width: 45.12rem; height: 1.88rem; font-size: 0.75rem; color: #FFFFFF; display: flex;\r\n" + 
			"    align-items: center; justify-content: center;\">\r\n" + 
			"    <div>\r\n" + 
			"        COPYRIGHT2020. TMAX A&C., LTD. ALL RIGHTS RESERVED\r\n" + 
			"    </div>\r\n" + 
			"</footer>\r\n" + 
			"</body>\r\n" + 
			"</html>";
	public static final String DEFAULT_NETWORK_POLICY_CONFIG_MAP = "default-networkpolicy-configmap";
	public static final String NETWORK_POLICY_YAML = "networkpolicies.yaml";
	
}
