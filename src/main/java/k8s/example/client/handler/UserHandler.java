package k8s.example.client.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import io.kubernetes.client.openapi.ApiException;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.CommonOutDO;
import k8s.example.client.DataObject.User;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.k8s.OAuthApiCaller;
import k8s.example.client.models.BrokerResponse;

public class UserHandler extends GeneralHandler {
	private final String HOST = "mail.tmax.co.kr";
	private final int PORT = 25;
//	private final String USERNAME = "taegeon_woo";
	private final String SEND_EMAIL = "taegeon_woo@tmax.co.kr";
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /User");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        List <UserCR> userCRList = null;
		User userInDO = null;
		String outDO = null;
		IStatus status = null;
		
		try {
			// Read inDO
			userInDO = new ObjectMapper().readValue(body.get( "postData" ), User.class);
    		logger.info( "  User ID: " + userInDO.getId() );
    		logger.info( "  User Name: " + userInDO.getName() );
    		logger.info( "  User E-Mail: " + userInDO.getEmail() );
    		logger.info( "  User PassWord: " + userInDO.getPassword() );
    		
    		// Validate
    		if (userInDO.getId() == null ) 	throw new Exception(ErrorCode.USER_ID_EMPTY);
    		if (userInDO.getEmail() == null ) 	throw new Exception(ErrorCode.USER_MAIL_EMPTY);
    		if (userInDO.getPassword() == null ) 	throw new Exception(ErrorCode.USER_PASSWORD_EMPTY);
    		
    		// Check ID, Email Duplication
    		userCRList = K8sApiCaller.listUser();
    		if ( userCRList != null ) {
        		for(UserCR userCR : userCRList) {
        			User user = userCR.getUserInfo();
        			if ( user.getName().equalsIgnoreCase(userInDO.getId())) throw new Exception(ErrorCode.USER_ID_DUPLICATED);  // 주의 : 회원가입 시 받은 ID를 k8s에는 Name으로 넣자
        			if ( user.getEmail().equalsIgnoreCase(userInDO.getEmail())) throw new Exception(ErrorCode.USER_MAIL_DUPLICATED);
        		}
    		}
    		logger.info( " 0000" );

    		JsonArray userAuthList = OAuthApiCaller.ListUser();
    		if ( userAuthList != null ) {
    			for (JsonElement userAuth : userAuthList) {
    				if (userAuth.getAsJsonObject().get("user_id").toString().equalsIgnoreCase(userInDO.getId())) throw new Exception(ErrorCode.USER_ID_DUPLICATED);
    			}
    		}
    		
    		// UserCRD Create
    		K8sApiCaller.createUser(userInDO);  		
    		
    		// Create Role & RoleBinding
    		K8sApiCaller.createClusterRoleForNewUser(userInDO);  		
    		K8sApiCaller.createClusterRoleBindingForNewUser(userInDO);  		

    		// Call UserCreate to ProAuth
    		OAuthApiCaller.createUser(userInDO);
    		
    		// Login to ProAuth & Get Token
    		JsonObject loginOut = OAuthApiCaller.AuthenticateCreate(userInDO);
//    		String refreshToken = loginOut.get("refresh_token").toString();
    		String accessToken = loginOut.get("token").toString();
    		
    		// Send E-mail to User
    		sendMail(userInDO, accessToken);
    		
    		
    		
		} catch (ApiException e) {
			logger.info( "Exception message: " + e.getResponseBody() );
			logger.info( "Exception message: " + e.getMessage() );
			
			if (e.getResponseBody().contains("NotFound")) {
				logger.info( "  Login fail. User not exist." );
				status = Status.OK; //ui요청
				outDO = Constants.LOGIN_FAILED;
			} else {
				logger.info( "Response body: " + e.getResponseBody() );
				e.printStackTrace();
				
				status = Status.UNAUTHORIZED;
				outDO = Constants.LOGIN_FAILED;
			}
		} catch (Exception e) {
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.LOGIN_FAILED;
			
		} catch (Throwable e) {
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
		}
		
		if (status.equals(Status.UNAUTHORIZED)) {
			CommonOutDO out = new CommonOutDO();
			out.setMsg(outDO);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			outDO = gson.toJson(out).toString();
		} else if ( status.equals(Status.OK) && outDO.equals(Constants.LOGIN_FAILED)) { //ui요청
			CommonOutDO out = new CommonOutDO();
			out.setMsg(outDO);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			outDO = gson.toJson(out).toString();
		}
 
//		logger.info();
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
    }
	

	private void sendMail( User userInDO, String accessToken ) throws Throwable {	
		
		String subject = "메일테스트";	
		Properties props = System.getProperties();
		String body = null;
		props.put( "mail.transport.protocol", "smtp" );
		props.put( "mail.smtp.host", HOST );
		props.put( "mail.smtp.port", PORT );
		props.put( "mail.smtp.ssl.trust", HOST );
		props.put( "mail.smtp.auth", "true" );
		props.put( "mail.smtp.starttls.enable", "true" );
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		
		Session session = Session.getDefaultInstance( props, new Authenticator() {
			String un = "taegeon_woo";
			String pw = "tg540315!";
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication( un, pw );
			}
		});		
		session.setDebug( true );

		Message mimeMessage = new MimeMessage(session);
		mimeMessage.setFrom( new InternetAddress( SEND_EMAIL ) );
		mimeMessage.setRecipient( Message.RecipientType.TO, new InternetAddress( SEND_EMAIL ) );

		mimeMessage.setSubject( subject );
		
		// Make Body
		Map<String, String> bodyMap = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE, "authenticate-html");  		
		if( bodyMap!=null ) {
			body = bodyMap.get("body");
		}

		if (body!=null) mimeMessage.setText( body );
		
		//Send Mail
		Transport.send( mimeMessage );		
	}
	
	public Response delete( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
		logger.info("***** DELETE /v2/service_instances/:instance_id/service_bindings/:binding_id");
		
		String serviceClassName = session.getParameters().get("service_id").get(0);
		String instanceId = urlParams.get("instance_id");
		String bindingId = urlParams.get("binding_id");
		logger.info("Instance ID: " + instanceId);
		logger.info("Binding ID: " + bindingId);
		
		BrokerResponse response = new BrokerResponse();
		String outDO = null;
		IStatus status = null;
		
		try {
			response.setOperation("");
			status = Status.OK;
		} catch (Exception e) {
			logger.info( "  Failed to unbind instance of service class \"" + serviceClassName + "\"");
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.BAD_REQUEST;
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		outDO = gson.toJson(response).toString();
		logger.info("Response : " + outDO);
		
//				logger.info();
		return NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO);
    }


	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /User");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
}