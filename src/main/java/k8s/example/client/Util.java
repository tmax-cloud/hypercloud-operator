package k8s.example.client;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fi.iki.elonen.NanoHTTPD.Response;
import k8s.example.client.k8s.K8sApiCaller;

public class Util {	
	public static Logger logger = Main.logger;

    public static Date getDateFromSecond(long seconds) {
		return Date.from(LocalDateTime.now().plusSeconds(seconds).atZone(ZoneId.systemDefault()).toInstant());
	}
    
    public static class Crypto {
	    public static String encryptSHA256(String input) throws Exception{
			String ret = "";
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(input.getBytes("UTF-8"));
				StringBuffer hexString = new StringBuffer();
	
				for (int i = 0; i < hash.length; i++) {
					String hex = Integer.toHexString(0xff & hash[i]);
					if (hex.length() == 1) hexString.append('0');
					hexString.append(hex);
				}
				ret = hexString.toString();	
			} catch (Exception e) {
				throw e;
			}
			return ret;
		}
    }
    
    public static String getRamdomPassword(int len) { 
    	char[] charSet = new char[] { 
    			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
    			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' 
    			}; 
    	
    	int idx = 0; 
    	StringBuffer sb = new StringBuffer(); 
    	
    	for (int i = 0; i < len; i++) { 
    		idx = (int) (charSet.length * Math.random()); // 36 * 생성된 난수를 Int로 추출 (소숫점제거) 
    		sb.append(charSet[idx]); 
    	}
    	return sb.toString();
	}
    
	 public static String numberGen(int len, int dupCd ) {
	        
	        Random rand = new Random();
	        String numStr = ""; //난수가 저장될 변수
	        
	        for(int i=0;i<len;i++) {
	            
	            //0~9 까지 난수 생성
	            String ran = Integer.toString(rand.nextInt(10));
	            
	            if(dupCd==1) {
	                //중복 허용시 numStr에 append
	                numStr += ran;
	            }else if(dupCd==2) {
	                //중복을 허용하지 않을시 중복된 값이 있는지 검사한다
	                if(!numStr.contains(ran)) {
	                    //중복된 값이 없으면 numStr에 append
	                    numStr += ran;
	                }else {
	                    //생성된 난수가 중복되면 루틴을 다시 실행한다
	                    i-=1;
	                }
	            }
	        }
        return numStr;
	}
	 
	 public static void sendMail( String email, String content ) throws Throwable {	
		logger.info( " Send Verification Mail User ");
		String host = "mail.tmax.co.kr";
		int port = 25;
		String recipient = "taegeon_woo@tmax.co.kr";
//		String recipient = email;  //FIXME

		String subject = "MailTest 메일테스트";	
		String charSetUtf = "UTF-8" ; //FIXME : 제목 한글 여전히 깨짐 ㅠㅠ
		Properties props = System.getProperties();
		String body = null;
		props.put( "mail.transport.protocol", "smtp" );
		props.put( "mail.smtp.host", host );
		props.put( "mail.smtp.port", port );
		props.put( "mail.smtp.ssl.trust", host );
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
		mimeMessage.setFrom( new InternetAddress(recipient, recipient, charSetUtf));
		
		// Receiver
		mimeMessage.setRecipient( Message.RecipientType.TO, new InternetAddress( recipient ) );
		
		// Make Subject
		mimeMessage.setSubject( subject, charSetUtf );
//			mimeMessage.setSubject( subject, "text/plain; charset=UTF-8" );

		// Make Body
//		Map<String, String> bodyMap = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE, "authenticate-html");  		
//		if( bodyMap != null ) {
//			body = bodyMap.get("body") + " \n AccessToken\n" + accessToken;
//			if( content != null) body = body + " \n Alter PassWord\n" + content; //TODO
//		}
		logger.info( " Mail Body : "  + content );
		if (body!=null) mimeMessage.setText( MimeUtility.encodeText(content,  charSetUtf, "B") );
		logger.info( " Ready to Send Mail to " + recipient);
		try {
			//Send Mail
			Transport.send( mimeMessage );
			logger.info( " Sent E-Mail to " + recipient);
		}catch (MessagingException e) {
            e.printStackTrace();
            logger.info( e.getMessage() + e.getStackTrace());
		} 
	}

    
    public static Response setCors( Response resp ) {
		resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Max-Age", "3628800");
        resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        resp.addHeader("Access-Control-Allow-Headers", "Content-Type, X-Requested-With, Accept, Authorization, Referer, User-Agent" );
		return resp;
    }
    
    public static JsonNode jsonDiff(String beforeJson, String afterJson) throws Exception{
    	try {
    		ObjectMapper jackson = new ObjectMapper(); 
    		JsonNode beforeNode = jackson.readTree(beforeJson); 
    		JsonNode afterNode = jackson.readTree(afterJson); 
    		return JsonDiff.asJson(beforeNode, afterNode);
    	}catch(Exception e) {
    		
    		throw e;
    	}
    }
    
    public static JsonElement toJson(Object o) {
		JsonObject json = (JsonObject) new JsonParser().parse(new Gson().toJson(o));
		json.remove("status");
		JsonObject metadata = json.getAsJsonObject("metadata");
		if( metadata != null ) {
			metadata.remove("annotations");
			metadata.remove("creationTimestamp");
			metadata.remove("generation");
			metadata.remove("resourceVersion");
			metadata.remove("selfLink");
			metadata.remove("uid");
		}
		
		return json;
	}
    
    public static String parseImageName(String imageName) {
    	return imageName.replaceAll("[/]", "-s-").replaceAll("[_]", "-u-");
    }
    
}
