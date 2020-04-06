package k8s.example.client.handler;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import k8s.example.client.DataObject.User;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.ErrorCode;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.k8s.OAuthApiCaller;
import k8s.example.client.metering.util.SimpleUtil;

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
    		JsonArray userAuthList = OAuthApiCaller.ListUser();
    		if ( userAuthList != null ) {
    			for (JsonElement userAuth : userAuthList) {
    				if (userAuth.getAsJsonObject().get("user_id").toString().equalsIgnoreCase(userInDO.getId())) throw new Exception(ErrorCode.USER_ID_DUPLICATED);
    			}
    		}
    		
    		// UserCRD Create
    		String password = userInDO.getPassword();
    		K8sApiCaller.createUser(userInDO);
    		userInDO.setPassword(password);
    		
    		// Create Role & RoleBinding
    		K8sApiCaller.createClusterRoleForNewUser(userInDO);  		
    		K8sApiCaller.createClusterRoleBindingForNewUser(userInDO);  		

    		// Call UserCreate to ProAuth
    		OAuthApiCaller.createUser(userInDO);
    		
    		// Login to ProAuth & Get Token
    		JsonObject loginOut = OAuthApiCaller.AuthenticateCreate(userInDO.getId(), userInDO.getPassword());
//    		String refreshToken = loginOut.get("refresh_token").toString().replaceAll("\"", "");
    		String accessToken = loginOut.get("token").toString().replaceAll("\"", ""); //
    		logger.info( "  accessToken : " + accessToken );

    		// Send E-mail to User
    		sendMail(userInDO, accessToken);
			status = Status.CREATED;
    		outDO = "User Create Success";
    		  		
		} catch (ApiException e) {
			logger.info( "Exception message: " + e.getResponseBody() );
			e.printStackTrace();
			
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_CREATE_FAILED;
			
		} catch (Exception e) {
			logger.info( "Exception message: " + e.getMessage() );

			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_CREATE_FAILED;
			
		} catch (Throwable e) {
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_CREATE_FAILED;
		}
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
    }
	
	public Response get( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** GET /User Id Find");
		
		List < UserCR > userCRList = null;
		IStatus status = null;
		String outDO = null; 
		String userId = null;
		
		// Get Query Parameter
		String mode = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_MODE );
		String email = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_EMAIL );
		
		try {
			if (mode.equalsIgnoreCase("id")) {
				userCRList = K8sApiCaller.listUser();
				if ( userCRList!=null ) {
					for(UserCR userCR : userCRList) {
						if( userCR.getUserInfo().getEmail().equalsIgnoreCase(email) ) {
							userId = userCR.getMetadata().getName();
						}
					}
				}
				if (userId == null) {
					throw new Exception(ErrorCode.NO_MATCHING_USER);
				}
				else {
					logger.info( "User ID Found: " + userId );
					status = Status.OK; 
					outDO = userId;
				}
			}
			
		}catch (ApiException e) {
			logger.info( "Exception message: " + e.getResponseBody() );
			logger.info( "Exception message: " + e.getMessage() );
			status = Status.UNAUTHORIZED; 
			outDO = Constants.USER_ID_FIND_FAILED;
			
		} catch (Exception e) {
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_ID_FIND_FAILED;		
		}		
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
	}
	
	public Response put( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** PUT /User");
		
		IStatus status = null;
		String outDO = null; 
		String updateMode = null;
		UserCR userCR = null;
		User userInDO = null;
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		// if updateMode exists
		if(session.getParameters()!=null) {
			if( session.getParameters().get("mode")!= null) {
				updateMode = session.getParameters().get("mode").get(0);
				logger.info("updateMode : " + updateMode );			
			}
		}

		try {
			String bodyStr = readFile(body.get("content"), Integer.valueOf(session.getHeaders().get("content-length")));
			logger.info("Body: " + bodyStr);	
			userInDO = new ObjectMapper().readValue(bodyStr, User.class);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		switch(updateMode) {	
		case "meta":
			logger.info( "  User ID: " + userInDO.getId() );
			logger.info( "  User Name: " + userInDO.getName() );
			logger.info( "  User Description: " + userInDO.getDescription() );
			logger.info( "  User Department: " + userInDO.getDepartment() );
			logger.info( "  User Phone: " + userInDO.getPhone() );
			logger.info( "  User Position: " + userInDO.getPosition() );
			
			try {
				// Validate
	    		if (userInDO.getId() == null ) 	throw new Exception(ErrorCode.USER_ID_EMPTY);
	    		userCR = K8sApiCaller.getUser( userInDO.getId() );
	    		if (userCR.getMetadata().getName() == null) {
	    			throw new Exception(ErrorCode.NO_CORRESPONDING_USER);
	    		} else {
	    			K8sApiCaller.updateUserMeta( userInDO );
	    			status = Status.OK; 
					outDO = Constants.USER_UPDATE_SUCCESS;
	    		}
	    		
			} catch (ApiException e) {
				logger.info( "Exception message: " + e.getResponseBody() );
				logger.info( "Exception message: " + e.getMessage() );
				status = Status.UNAUTHORIZED; 
				outDO = Constants.USER_UPDATE_FAILED;
				
			} catch (Exception e) {
				logger.info( "Exception message: " + e.getMessage() );
				e.printStackTrace();
				status = Status.UNAUTHORIZED;
				outDO = Constants.USER_UPDATE_FAILED;		
			}		
			break;
			
		case "passwd":
			//TODO : 일해라 태건아
			break;
		}
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));

	}
	

	private void sendMail( User userInDO, String accessToken ) throws Throwable {	
		logger.info( " Send Verification Mail to New User ");

		String subject = "MailTest 메일테스트";	
		String charSetUtf = "UTF-8" ; //FIXME : 제목 한글 여전히 깨짐 ㅠㅠ
		Properties props = System.getProperties();
		String body = null;
		props.put( "mail.transport.protocol", "smtp" );
		props.put( "mail.smtp.host", HOST );
		props.put( "mail.smtp.port", PORT );
		props.put( "mail.smtp.ssl.trust", HOST );
		props.put( "mail.smtp.auth", "true" );
		props.put( "mail.smtp.starttls.enable", "true" );
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		
		Session session = Session.getDefaultInstance( props, new javax.mail.Authenticator() {
			String un = "taegeon_woo@tmax.co.kr";
			String pw = "tg540315";
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication( un, pw );
			}
		});		
		
		session.setDebug( true );

		MimeMessage mimeMessage = new MimeMessage(session);
		
		// Sender
		mimeMessage.setFrom( new InternetAddress(SEND_EMAIL, SEND_EMAIL, charSetUtf));
		
		// Receiver
		mimeMessage.setRecipient( Message.RecipientType.TO, new InternetAddress( SEND_EMAIL ) );
		
		// Make Subject
		mimeMessage.setSubject( subject, charSetUtf );
//		mimeMessage.setSubject( subject, "text/plain; charset=UTF-8" );

		// Make Body
		Map<String, String> bodyMap = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE, "authenticate-html");  		
		if( bodyMap != null ) {
			body = bodyMap.get("body") + accessToken; //TODO
		}
		logger.info( " Mail Body : "  + body );
		if (body!=null) mimeMessage.setText( MimeUtility.encodeText(body,  charSetUtf, "B") );
		logger.info( " Ready to Send Mail to " + SEND_EMAIL);
		try {
			//Send Mail
			Transport.send( mimeMessage );
			logger.info( " Sent E-Mail to " + SEND_EMAIL);
		}catch (MessagingException e) {
            e.printStackTrace();
            logger.info( e.getMessage() + e.getStackTrace());
		} 
	}
	
	private String readFile(String path, Integer length) {
		Charset charset = Charset.defaultCharset();
		String bodyStr = "";
		int byteCount;
		try {
			ByteBuffer buf = ByteBuffer.allocate(Integer.valueOf(length));
			FileInputStream fis = new FileInputStream(path);
			FileChannel dest = fis.getChannel();
			
			while(true) {
				byteCount = dest.read(buf);
				if(byteCount == -1) {
					break;
				} else {
					buf.flip();
					bodyStr += charset.decode(buf).toString();
					buf.clear();
				}
			}
			dest.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return bodyStr;
	}

	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /User");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
}