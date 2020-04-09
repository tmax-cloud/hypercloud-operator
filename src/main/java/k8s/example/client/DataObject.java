package k8s.example.client;

import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.proto.Meta.Time;

public class DataObject {
	
	public static class LoginInDO {
    	private String id;
    	private String password;
    	
    	public String getId() { return id; }    	
    	public String getPassword() { return password; }
    }
	
	@JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenCR {
    	private String apiVersion = "tmax.io/v1";
    	private String kind = "Token";
    	private V1ObjectMeta metadata;
    	private String accessToken;
    	private String refreshToken;
    	
    	public String getApiVersion() { return apiVersion; }
    	public String getKind() { return kind; }
    	public V1ObjectMeta getMetadata() { return metadata; }
    	public String getAccessToken() { return accessToken; }
    	public String getRefreshToken() { return refreshToken; }
    	
    	public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }
    	public void setKind(String kind) { this.kind = kind; }
    	public void setMetadata(V1ObjectMeta metadata) { this.metadata = metadata; }
    	public void setAccessToken(String accessToken) { this.accessToken = accessToken; }    	
    	public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
    
    public static class Token {
    	private String accessToken;
    	private String refreshToken;
    	private int atExpireTime;
    	
    	public String getAccessToken() { return accessToken; }    	
    	public String getRefreshToken() { return refreshToken; }
    	public int getAtExpireTime() { return atExpireTime; }
    	
    	public void setAccessToken(String accessToken) { this.accessToken = accessToken; }    	
    	public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    	public void setAtExpireTime(int atExpireTime) { this.atExpireTime = atExpireTime; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserCR {
    	private String apiVersion = "tmax.io/v1";
    	private String kind = "User";
    	private V1ObjectMeta metadata;
    	private User userInfo;
    	private String status;
    	
    	public String getApiVersion() { return apiVersion; }
    	public String getKind() { return kind; }
    	public V1ObjectMeta getMetadata() { return metadata; }
    	public User getUserInfo() { return userInfo; }
    	public String getStatus() { return status; }
    	
    	public void setMetadata(V1ObjectMeta metadata) { this.metadata = metadata; }
    	public void setUserInfo(User userInfo) { this.userInfo = userInfo; }
    	public void setStatus(String status) { this.status = status; }
    }
    
    public static class User {
    	private String name;
    	private String id;    	
    	private String password;
    	private String alterPassword;
    	private String passwordSalt;
    	private String email;
    	private String phone;
    	private String department;
    	private String position;
    	private String description;
    	
    	public String getId() { return id; }
    	public String getName() { return name; }
    	public String getPassword() { return password; }
    	public String getAlterPassword() { return alterPassword; }
    	public String getPasswordSalt() { return passwordSalt; }
    	public String getEmail() { return email; }
    	public String getPhone() { return phone; }
    	public String getDepartment() { return department; }
    	public String getPosition() { return position; }
    	public String getDescription() { return description; }
    	
    	public void setId(String id) { this.id = id; }
    	public void setName(String name) { this.name = name; }
    	public void setPassword(String password) { this.password = password; }
    	public void setAlterPassword(String alterPassword) { this.alterPassword = alterPassword; }
    	public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }
    	public void setEmail(String email) { this.email = email; }
    	public void setPhone(String phone) { this.phone = phone; }
    	public void setDepartment(String department) { this.department = department; }
    	public void setPosition(String position) { this.position = position; }
    	public void setDescription(String description) { this.description = description; }
    }
    
    public static class TokenReview {
    	private String apiVersion = "authentication.k8s.io/v1beta1";
    	private String kind = "TokenReview";
    	private TokenReviewStatus status;
    	
    	public void setStatus(TokenReviewStatus status) { this.status = status; }    	
    }
    
    public static class TokenReviewStatus {
    	private boolean authenticated;
    	private TokenReviewUser user;
    	
    	public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }    	
    	public void setUser(TokenReviewUser user) { this.user = user; }
    }
    
    public static class TokenReviewUser {
    	private String username;
    	private String uid;
    	
    	public void setUsername(String username) { this.username = username; }    	
    	public void setUid(String uid)	{ this.uid = uid; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClientCR {
    	private String apiVersion = "tmax.io/v1";
    	private String kind = "Client";
    	private V1ObjectMeta metadata;
    	private Client clientInfo;
    	
    	public String getApiVersion() { return apiVersion; }
    	public String getKind() { return kind; }
    	public V1ObjectMeta getMetadata() { return metadata; }
    	public Client getClientInfo() { return clientInfo; }
    	
    	public void setClientInfo(Client clientInfo) { this.clientInfo = clientInfo; }
    	public void setMetadata(V1ObjectMeta metadata) { this.metadata = metadata; }
			
    }
    
    public static class Client {
    	private String appName;
    	private String originUri;
    	private String redirectUri;
    	private String clientId;
    	private String clientSecret;
    	
    	public String getAppName() { return appName; }
    	public String getOriginUri() { return originUri; }
    	public String getRedirectUri() { return redirectUri; }
    	public String getClientId() { return clientId; }
    	public String getClientSecret() { return clientSecret; }
    	
    	public void setAppName(String appName) { this.appName = appName; }
    	public void setOriginUri(String originUri) { this.originUri = originUri; }
    	public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
    	public void setClientId(String clientId) { this.clientId = clientId; }
    	public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    }
    
    
    public static class CommonOutDO {
    	private String msg;
    	private String status;

    	public String getMsg() { return msg; }
    	public String getStatus() { return status; }
    	
    	public void setMsg(String msg) { this.msg = msg; }
    	public void setStatus(String status) { this.status = status; }
    }
    
    public static class RegistryEventDO {
    	private List<RegistryEvent> events;

		public List<RegistryEvent> getEvents() {
			return events;
		}
		public void setEvents(List<RegistryEvent> events) {
			this.events = events;
		}
    }
    
    public static class RegistryEvent {
    	private String id;
    	private String timestamp;
    	private String action;
    	private RegistryDescriptor target;
    	private int length;
    	private String repository;
    	private String fromRepository;
    	private String url;
    	private String tag;
    	private RequestRecord request;
    	private ActorRecord actor;
    	private SourceRecord source;
    	
		public String getId() { return id; }
		public void setId(String id) { this.id = id; }
		
		public String getTimestamp() { return timestamp; }
		public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
		
		public String getAction() { return action; }
		public void setAction(String action) { this.action = action; }
		
		public RegistryDescriptor getTarget() { return target; }
		public void setTarget(RegistryDescriptor target) { this.target = target; }
		
		public int getLength() { return length; }
		public void setLength(int length) { this.length = length; }
		
		public String getRepository() { return repository; }
		public void setRepository(String repository) { this.repository = repository; }
		
		public String getFromRepository() { return fromRepository; }
		public void setFromRepository(String fromRepository) { this.fromRepository = fromRepository; }
		
		public String getUrl() { return url; }
		public void setUrl(String url) { this.url = url; }
		
		public String getTag() { return tag; }
		public void setTag(String tag) { this.tag = tag; }
		
		public RequestRecord getRequest() { return request; }
		public void setRequest(RequestRecord request) { this.request = request; }
		
		public ActorRecord getActor() { return actor; }
		public void setActor(ActorRecord actor) { this.actor = actor; }
		
		public SourceRecord getSource() { return source; }
		public void setSource(SourceRecord source) { this.source = source; }
    }
    
    public static class RegistryDescriptor {
    	private String mediaType;
    	private int size;
    	private String digest;
    	private int length;
    	private String repository; 
    	private String url;
    	private String tag;
		public String getMediaType() {
			return mediaType;
		}
		public void setMediaType(String mediaType) {
			this.mediaType = mediaType;
		}
		public int getSize() {
			return size;
		}
		public void setSize(int size) {
			this.size = size;
		}
		public String getDigest() {
			return digest;
		}
		public void setDigest(String digest) {
			this.digest = digest;
		}
		public int getLength() {
			return length;
		}
		public void setLength(int length) {
			this.length = length;
		}
		public String getRepository() {
			return repository;
		}
		public void setRepository(String repository) {
			this.repository = repository;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getTag() {
			return tag;
		}
		public void setTag(String tag) {
			this.tag = tag;
		}
    	
    	
    }
    
    public static class RequestRecord {
    	private String id;
    	private String addr;
    	private String host;
    	private String method;
    	private String useragent;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getAddr() {
			return addr;
		}
		public void setAddr(String addr) {
			this.addr = addr;
		}
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public String getMethod() {
			return method;
		}
		public void setMethod(String method) {
			this.method = method;
		}
		public String getUseragent() {
			return useragent;
		}
		public void setUseragent(String useragent) {
			this.useragent = useragent;
		}
    	
    	
    }

    public static class ActorRecord {
    	private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
    	
    	
    }
    
    public static class SourceRecord {
    	private String addr;
    	private String instanceID;
    	
		public String getAddr() {
			return addr;
		}
		public void setAddr(String addr) {
			this.addr = addr;
		}
		public String getInstanceID() {
			return instanceID;
		}
		public void setInstanceID(String instanceID) {
			this.instanceID = instanceID;
		}
    	
    	
    }
}
