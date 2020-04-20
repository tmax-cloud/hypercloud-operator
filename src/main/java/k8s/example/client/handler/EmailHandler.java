package k8s.example.client.handler;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

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

public class EmailHandler extends GeneralHandler {
    private Logger logger = Main.logger;
	@Override
    public Response post(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /Email");
		logger.info(" User Email Authenticate Code Send Service Start ");

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
    		logger.info( "  User E-Mail: " + userInDO.getEmail() );
    		
    		// Validate
    		if (userInDO.getEmail() == null ) 	throw new Exception(ErrorCode.USER_MAIL_EMPTY);		

    		// Issue VerifyCode
    		String verifyCode = Util.numberGen(4, 1);
    		logger.info( " verifyCode: " + verifyCode );

    		// Send E-mail to User
    		String subject = "[인증번호 : " + verifyCode + " ] 이메일을 인증해 주세요";
    		String content = "인증번호 " + verifyCode + "\n\n 안녕하세요? \n TmaxCloud를 이용해 주셔서 감사합니다. \n 인증번호를 입력해 주세요. \n 감사합니다.";
    		Util.sendMail( userInDO.getEmail(), subject, content ); //FIXME
    		
    		// Insert VerifyCode into Secret
    		try {
    			Map<String, String> returnMap = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE, Constants.K8S_PREFIX + Constants.SECRET_VERIFICATAION_CODE);
    			Map<String, String> patchMap = new HashMap<>();
    			patchMap.put(userInDO.getEmail().replaceAll("@", "-"), verifyCode);
    			K8sApiCaller.patchSecret(Constants.TEMPLATE_NAMESPACE, patchMap, Constants.SECRET_VERIFICATAION_CODE, null);
    		} catch ( ApiException e) {
    			logger.info( "Exception message: " + e.getResponseBody() );
    			e.printStackTrace();
    			Map<String, String> patchMap = new HashMap<>();
    			patchMap.put(userInDO.getEmail().replaceAll("@", "-"), verifyCode);
    			K8sApiCaller.createSecret(Constants.TEMPLATE_NAMESPACE, patchMap, Constants.SECRET_VERIFICATAION_CODE, null, null, null);
    		}
			status = Status.CREATED;
    		outDO = "User Email Authenticate Code Send Success";
    		  		
		} catch (ApiException e) {
			logger.info( "Exception message: " + e.getResponseBody() );
			e.printStackTrace();
			
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_EMAIL_VERIFICATION_NUMBER_SEND_FAIL;
			
		} catch (Exception e) {
			logger.info( "Exception message: " + e.getMessage() );

			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_EMAIL_VERIFICATION_NUMBER_SEND_FAIL;
			
		} catch (Throwable e) {
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_EMAIL_VERIFICATION_NUMBER_SEND_FAIL;
		}
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
    }
	
	public Response put( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
		logger.info("***** put/Email");
		logger.info(" User Email Verify Service Start ");

		IStatus status = null;
		String outDO = null; 
		User userInDO = null;

		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        try {
			String bodyStr = readFile(body.get("content"), Integer.valueOf(session.getHeaders().get("content-length")));
			logger.info("Body: " + bodyStr);	
			userInDO = new ObjectMapper().readValue(bodyStr, User.class);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		try {
			// Read inDO
    		logger.info( "  User E-Mail: " + userInDO.getEmail() );
    		logger.info( "  User VerifyCode: " + userInDO.getVerifyCode() );
			boolean flag = false;
    		Map<String, String> returnMap = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE, Constants.K8S_PREFIX + Constants.SECRET_VERIFICATAION_CODE);
    		if (returnMap.size() != 0) {
    			Iterator<String> keyset = returnMap.keySet().iterator();
        		while( keyset.hasNext() ) {
        			String key = keyset.next().toString();
        			logger.info("key: " + key);	
        			if ( key.equalsIgnoreCase(userInDO.getEmail().replaceAll("@", "-"))) {
            			logger.info("userInDO.getEmail(): " + userInDO.getEmail());	
            			logger.info("userInDO.getVerifyCode(): " + userInDO.getVerifyCode());	
            			logger.info("returnMap.get(key)): " + returnMap.get(key));	
        				if (returnMap.get(key).equalsIgnoreCase(userInDO.getVerifyCode())) {
        					flag = true;
        				}
        			}
        		}
    		}
    		
    		if (flag) {
    			status = Status.OK;
        		outDO = "User Email Verify Success";
        		// Delete Secret Key value
        		Map<String, String> patchMap = new HashMap<>();
    			patchMap.put(userInDO.getEmail().replaceAll("@", "-"), "");
        		K8sApiCaller.patchSecret(Constants.TEMPLATE_NAMESPACE, patchMap, Constants.SECRET_VERIFICATAION_CODE, null);
    		}else {
    			status = Status.UNAUTHORIZED;
    			outDO = "Verification Number is Wrong";
    		}
    		
		} catch (ApiException e) {
			logger.info( "Exception message: " + e.getResponseBody() );
			e.printStackTrace();
			
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_EMAIL_VERIFY_FAIL;
			
		} catch (Exception e) {
			logger.info( "Exception message: " + e.getMessage() );

			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_EMAIL_VERIFY_FAIL;
			
		} catch (Throwable e) {
			logger.info( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_EMAIL_VERIFY_FAIL;
		}	
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));

	}

	@Override
    public Response other(
      String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /Email");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
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
}