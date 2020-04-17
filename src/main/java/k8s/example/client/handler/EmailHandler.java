package k8s.example.client.handler;

import java.util.HashMap;
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
    		
    		// Check ID, Email Duplication
    		userCRList = K8sApiCaller.listUser();
    		if ( userCRList != null ) {
        		for(UserCR userCR : userCRList) {
        			User user = userCR.getUserInfo();
        			if ( user.getEmail().equalsIgnoreCase(userInDO.getEmail())) throw new Exception(ErrorCode.USER_MAIL_DUPLICATED);
        		}
    		}		

    		// Issue VerifyCode
    		String verifyCode = Util.numberGen(4, 1);
    		logger.info( "  verifyCode: " + verifyCode );

    		// Send E-mail to User
    		Util.sendMail(userInDO.getEmail(), verifyCode); 
    		
    		// Insert VerifyCode into Secret
    		try {
    			Map<String, String> returnMap = K8sApiCaller.readSecret(Constants.TEMPLATE_NAMESPACE, Constants.K8S_PREFIX + Constants.SECRET_VERIFICATAION_CODE);
    			Map<String, String> patchMap = new HashMap<>();
    			patchMap.put(userInDO.getEmail(), verifyCode);
    			K8sApiCaller.patchSecret(Constants.TEMPLATE_NAMESPACE, patchMap, Constants.SECRET_VERIFICATAION_CODE, null);
    		} catch ( ApiException e) {
    			logger.info( "Exception message: " + e.getResponseBody() );
    			e.printStackTrace();
    			Map<String, String> patchMap = new HashMap<>();
    			patchMap.put(userInDO.getEmail(), verifyCode);
    			K8sApiCaller.createSecret(Constants.TEMPLATE_NAMESPACE, patchMap, Constants.SECRET_VERIFICATAION_CODE, null, null, null);
    		}
			status = Status.CREATED;
    		outDO = "User Email Authenticate Code Send Success";
    		  		
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
		logger.info("***** OPTIONS /User");
		
		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
    }
}