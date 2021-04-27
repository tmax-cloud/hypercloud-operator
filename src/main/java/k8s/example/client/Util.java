package k8s.example.client;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.joda.time.DateTime;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fi.iki.elonen.NanoHTTPD.Response;
import io.kubernetes.client.openapi.models.V1Namespace;
import k8s.example.client.k8s.HyperAuthCaller;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.metering.TimerMap;

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
    
    public static String makeK8sFieldValue(String name) { 
    	return name.replaceAll("@", "-").replaceAll("_", "-");
	}
    
	 public static String numberGen(int len, int dupCd ) {
	        
	        Random rand = new Random();
	        String numStr = ""; //난수가 저장될 변수
	        
	        for(int i=0;i<len;i++) {
	        	String ran = null;
	            //0~9 까지 난수 생성 ( 첫자리에 0 인 경우는 제외 )
	        	if (i == 0) {
	        		ran = Integer.toString(rand.nextInt(9)+1);
	        	}else {
		            ran = Integer.toString(rand.nextInt(10));
	        	}    
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
	 
	 public static void sendMail( String recipient, String subject, String body, String imgPath, String imgCid ) throws Throwable {	
		logger.info( " Send Mail to User [ " + recipient + "] Start");
		String host = "mail.tmax.co.kr";
		int port = 25;
		String sender = "no-reply-tc@tmax.co.kr";
		
		String charSetUtf = "UTF-8" ; 
		Properties props = System.getProperties();
		props.put( "mail.transport.protocol", "smtp" );
		props.put( "mail.smtp.host", host );
		props.put( "mail.smtp.port", port );
		props.put( "mail.smtp.ssl.trust", host );
		props.put( "mail.smtp.auth", "true" );
		props.put( "mail.smtp.starttls.enable", "true" );
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		
		Session session = Session.getDefaultInstance( props, new javax.mail.Authenticator() {
			String un = "no-reply-tc@tmax.co.kr";
			String pw = "!@tcdnsdudxla11";
//			String pw = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE, Constants.SECRET_MAIL_PASSWORD).getStringData().get("password");
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication( un, pw );
			}
		});		
		
		session.setDebug( true );
		MimeMessage mimeMessage = new MimeMessage(session);
		// Sender
		mimeMessage.setFrom( new InternetAddress(sender, sender, charSetUtf));
		// Receiver
		mimeMessage.setRecipient( Message.RecipientType.TO, new InternetAddress( recipient ) );
		// Make Subject
		mimeMessage.setSubject( MimeUtility.encodeText(subject,  charSetUtf, "B") );

//		Map<String, String> bodyMap = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE, "authenticate-html");  		
//		if( bodyMap != null ) {
//			body = bodyMap.get("body") + " \n AccessToken\n" + accessToken;
//			if( content != null) body = body + " \n Alter PassWord\n" + content; //TODO
//		}
		
		// Make Body ( text/html + img )
		MimeMultipart multiPart = new MimeMultipart();
		
		logger.debug( " Mail Body : "  + body );
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(body, "text/html; charset="+charSetUtf);
		multiPart.addBodyPart(messageBodyPart);

		BodyPart messageImgPart = new MimeBodyPart();
		DataSource ds = new FileDataSource(imgPath);
		messageImgPart.setDataHandler(new DataHandler(ds));
		messageImgPart.setHeader("Content-Type", "image/svg");
		messageImgPart.setHeader("Content-ID", "<"+imgCid+">");
		multiPart.addBodyPart(messageImgPart);

		mimeMessage.setContent(multiPart);
		
//		mimeMessage.setContent(body,"text/html; charset="+charSetUtf);
//		mimeMessage.setHeader("Content-Type", "text/html; charset="+charSetUtf);
		
		logger.info( " Ready to Send Mail to " + recipient);
		try {
			//Send Mail
			Transport.send( mimeMessage );
			logger.info( " Sent E-Mail to " + recipient);
		}catch (MessagingException e) {
            e.printStackTrace();
            logger.error( e.getMessage() + e.getStackTrace());
		} 
	}

    
    public static Response setCors( Response resp ) {
		resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Max-Age", "3628800");
        resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        resp.addHeader("Access-Control-Allow-Headers", "Content-Type, X-Requested-With, Accept, Authorization, Referer, User-Agent" );
		return resp;
    }
    
    /* Example
	 * {"op":"remove","path":"/apiVersion"}
	 * {"op":"replace","path":"/kind","value":"Registry3"}
	 * {"op":"add","path":"/kind2","value":"Registry"}
	 */
    public static JsonObject makePatchJsonObject(String op, String path, Object value) {
    	JsonObject res = new JsonObject();
    	
    	if(value != null) {
    		if(value instanceof JsonElement){
    			res.add("value", (JsonElement) value);
    		} else if(value instanceof String) {
    			res.addProperty("value", (String) value);
    		} else if(value instanceof Boolean) {
    			res.addProperty("value", (Boolean) value);
    		} else if(value instanceof Number) {
    			res.addProperty("value", (Number) value);
    		} else if(value instanceof Character) {
    			res.addProperty("value", (Character) value);
    		} else {
    			return res;
    		}
    	}
    	
    	res.addProperty("op", op);
    	res.addProperty("path", path);
    	
    	return res;
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
		json.remove("operatorStartTime");
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
    
    public static void setTrialNSTimer(V1Namespace nsResult) throws Exception  {
		logger.info(" [Trial Timer] TrialNSTimer for Trial NS[ " + nsResult.getMetadata().getName() + " ] Set Service Start ");
		DateTime createTime = nsResult.getMetadata().getCreationTimestamp();
		logger.info(" [Trial Timer] CreateTime : " + createTime);
		Map<String, String> labels = nsResult.getMetadata().getLabels();
		
		// Set Mail, Delete Time 
		DateTime currentTime = DateTime.now();
		DateTime mailTime = createTime.plusDays(23);
		DateTime deleteTime = createTime.plusDays(30);
		if ( nsResult.getMetadata().getLabels().get("period") != null ) {
			deleteTime = createTime.plusDays( Integer.parseInt(nsResult.getMetadata().getLabels().get("period")) * 30 );
			mailTime = deleteTime.minusDays(7);
		}

//		 For test Must Delete !!!!!!!!!!!!!!!!!!!!!!!!!!
//		mailTime = createTime.plusMinutes(3);
//		deleteTime = createTime.plusMinutes(6);
//
//		if ( nsResult.getMetadata().getLabels().get("period") != null ) {
//			deleteTime = createTime.plusMinutes( Integer.parseInt(nsResult.getMetadata().getLabels().get("period")) * 6 );
//			mailTime = createTime.plusMinutes( Integer.parseInt(nsResult.getMetadata().getLabels().get("period")) * 3 );
//		}
//		 For test Must Delete !!!!!!!!!!!!!!!!!!!!!!!!!!!


		Timer timer = new Timer(nsResult.getMetadata().getUid() + "#" + nsResult.getMetadata().getName() + "#" + nsResult.getMetadata().getAnnotations().get("owner") + "#" + deleteTime.toDateTime().toString("yyyy-MM-dd") );
		
		if( mailTime.isAfter(currentTime) ) {
			timer.schedule(new TimerTask() {
				public void run() {
					try {
						String nsId = Thread.currentThread().getName().split("#")[0];
						String nsName = Thread.currentThread().getName().split("#")[1];
						String userId = Thread.currentThread().getName().split("#")[2];
						String deleteTime = Thread.currentThread().getName().split("#")[3];
						logger.info(" [Trial Timer] Trial NameSpace [ " + nsName + " ] Mail Service before 1 weeks of deletion Start");
						logger.info(" [Trial Timer] User ID : " + userId );
						
						V1Namespace nameSpace = K8sApiCaller.getNameSpace(nsName);
						if ( nameSpace.getMetadata().getLabels() != null && nameSpace.getMetadata().getLabels().get("trial") != null
								&& nameSpace.getMetadata().getAnnotations()!= null && nameSpace.getMetadata().getAnnotations().get("owner") != null) {
							logger.info(" [Trial Timer] Still Trial NameSpace, Send Info Mail to User [ " + userId + " ]");
							String email = null;
							try{
								// Call hyperauth to get Email with userId
//								JsonObject userDetailJsonObject = HyperAuthCaller.getUserDetailWithoutToken( userId );
//								if ( userDetailJsonObject != null) {
//									email = userDetailJsonObject.get("email").toString().replaceAll("\"", "");
//								}
								email = userId;
								logger.info(" [Trial Timer] Email : " + email );
								String subject = " 신청해주신 Trial NameSpace [ " + nameSpace.getMetadata().getName() + " ] 만료 안내 ";
								String body = Constants.TRIAL_TIME_OUT_CONTENTS;
								body = body.replaceAll("%%TRIAL_END_TIME%%", deleteTime);
								Util.sendMail(email, subject, body, "/home/tmax/hypercloud4-operator/_html/img/service-timeout.png", "service-timeout");
							}catch (Exception e){
								logger.info("User [ " + userId + " ] not Exists in HyperAuth Server, Nothing to do");
							}
						} else {
							logger.info(" [Trial Timer] Paid NameSpace, Nothing to do ");
						}
					} catch (Exception e) {
						logger.error( " [Trial Timer] Exception : " + e.getMessage());
						e.printStackTrace();
					} catch (Throwable e) {
						logger.error( " [Trial Timer] Exception : " + e.getMessage());
						e.printStackTrace();
					}
				}
			}, mailTime.toDate());
			
			logger.info(" [Trial Timer] Set Trial NameSpace Sending Mail Timer Success ");
			logger.info(" [Trial Timer] MailSendTime for Trial NS[ " + nsResult.getMetadata().getName() + " ] : " + mailTime);
			
			// Replace Or Put Mail Time Label
			if ( labels.keySet().contains("mailSendDate")) {
				labels.replace("mailSendDate", mailTime.toString().replaceAll(":", "-").substring(0, 10));
			}else {
				labels.put("mailSendDate", mailTime.toString().replaceAll(":", "-").substring(0, 10));
			}
		} else {
			logger.info(" [Trial Timer] Mail for Alert Deletion for This Trial Namespace [" + nsResult.getMetadata().getName() + "] already Sent to " + nsResult.getMetadata().getAnnotations().get("owner") );
		}
		
		if( deleteTime.isAfter(currentTime) ) {
			timer.schedule(new TimerTask() {
				public void run() {
					try {
						String nsId = Thread.currentThread().getName().split("#")[0];
						String nsName = Thread.currentThread().getName().split("#")[1];
						String userId = Thread.currentThread().getName().split("#")[2];
						logger.info(" [Trial Timer] Trial NameSpace [ " + nsName + " ] deletion Start");
						logger.info(" [Trial Timer] User ID : " + userId );
						
						V1Namespace nameSpace = K8sApiCaller.getNameSpace(nsName);
						if ( nameSpace.getMetadata().getLabels() != null && nameSpace.getMetadata().getLabels().get("trial") != null 
								&& nameSpace.getMetadata().getAnnotations()!= null && nameSpace.getMetadata().getAnnotations().get("owner") != null) {
							logger.info(" [Trial Timer] Still Trial NameSpace, Delete Expired Namespace [ " + nsName + " ]");
							K8sApiCaller.deleteRoleBinding(nsName, "trial-" + nsName);
							// Delete ClusterRoleBinding for Trial New User
							K8sApiCaller.deleteClusterRoleBinding("CRB-" + nsName);
							K8sApiCaller.deleteNameSpace(nsName);
						} else {
							logger.info(" [Trial Timer] Paid NameSpace, Nothing to do ");
						}
					} catch (Exception e) {
						logger.error( "Exception : " + e.getMessage());
						e.printStackTrace();
					} catch (Throwable e) {
						logger.error( "Exception : " + e.getMessage());
						e.printStackTrace();
					}
				}
			}, deleteTime.toDate());
			
			logger.info(" [Trial Timer] Set Trial NameSpace Delete Timer Success ");
			logger.info(" [Trial Timer] Deletion Time for Trial NS[ " + nsResult.getMetadata().getName() + " ] : " + deleteTime);
			
			// Replace Or Put Deletion Time Label
			if ( labels.keySet().contains("deletionDate")) {
				labels.replace("deletionDate", deleteTime.toString().replaceAll(":", "-").substring(0, 10));
			}else {
				labels.put("deletionDate", deleteTime.toString().replaceAll(":", "-").substring(0, 10));
			}
			
			// patchNameSpace with new label
			try {
				K8sApiCaller.replaceNamespace(nsResult);
			} catch (Throwable e) {
				logger.error(" [Trial Timer] Replace NameSpace for new Label Failed ");
				logger.error(" Exception : " + e.getMessage());
				e.printStackTrace();
			}
			
			// Insert to TimerMap
			TimerMap.addTimer(nsResult.getMetadata().getName(), timer );
			for (String nsName : TimerMap.getTimerList()) {
				logger.info(" [Trial Timer] Registered NameSpace Timer in test : " + nsName );
			}	
		} else {
			logger.info(" [Trial Timer] This Trial Namespace [" + nsResult.getMetadata().getName() + "] has Already Expired, Check Why This NameSpace is Still Exists"  );
		}
	}
    
    public static void deleteTrialNSTimer( String nsName ) throws Exception  {
    	Timer timer = TimerMap.getTimer(nsName);
    	if ( timer != null ) {
    		timer.cancel();
        	TimerMap.removeTimer(nsName);	
    		logger.info(" [Trial Timer] Delete Trial NameSpace Timer Success ");
    	} else {
    		logger.info(" [Trial Timer] There was no Timer for Trial Namespace [ " + nsName + " ], Set New Timer Anyway");
    	}
    }

	public static String printExceptionError(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public static JsonObject yamlStringToJsonObject(String yamlString) throws JsonMappingException, JsonProcessingException {
		logger.debug("yamlString : " + yamlString );

		ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yamlString, Object.class);

        ObjectMapper jsonWriter = new ObjectMapper();
        String jsonString = jsonWriter.writeValueAsString(obj);	
		logger.debug("jsonString : " + jsonString );
        
        return new JsonParser().parse(jsonString).getAsJsonObject();		//FIXME
	}
    
}
