package k8s.example.client.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import io.kubernetes.client.openapi.ApiException;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.metering.util.SimpleUtil;

public class UserHandler extends GeneralHandler {

	private Logger logger = Main.logger;

	@Override
	public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** POST /User");

		Map<String, String> body = new HashMap<String, String>();
		try {
			session.parseBody(body);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String outDO = null;
		IStatus status = null;
		String userId = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_USER_ID );

		try {
    		// Create Role & RoleBinding 
    		K8sApiCaller.createClusterRoleForNewUser(userId);  		
    		K8sApiCaller.createClusterRoleBindingForNewUser(userId);
    		
			status = Status.OK;
			outDO = Constants.USER_NEW_ROLE_CREATE_SUCCESS;

		} catch (ApiException e) {
			logger.error("Exception message: " + e.getResponseBody());
			e.printStackTrace();

			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_NEW_ROLE_CREATE_FAILED;

		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());

			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_NEW_ROLE_CREATE_FAILED;

		} catch (Throwable e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_NEW_ROLE_CREATE_FAILED;
		}

		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));
	}
	
	public Response delete(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** DELETE /User");

		Map<String, String> body = new HashMap<String, String>();
		try {
			session.parseBody(body);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String outDO = null;
		IStatus status = null;
		String userId = SimpleUtil.getQueryParameter( session.getParameters(), Constants.QUERY_PARAMETER_USER_ID );

		try {
    		// Delete Role & RoleBinding 
			try {
				K8sApiCaller.deleteClusterRole(userId);
			} catch (ApiException e) {
				if ( e.getResponseBody().contains("404")) {
					logger.info("Nothing to do");
				}else {
					throw e;
				}
			}
			
			try {
				K8sApiCaller.deleteClusterRoleBinding(userId);
			} catch (ApiException e) {
				if ( e.getResponseBody().contains("404")) {
					logger.info("Nothing to do");
				}else {
					throw e;
				}
			}
    		
    		// Delete ingress-nginx-shared namespace read role
			try {
	    		K8sApiCaller.deleteRoleBinding(Constants.INGRESS_NGINX_SHARED_NAMESPACE,Constants.INGRESS_NGINX_SHARED_READ_ROLE_BINDING + "-" + userId);
			} catch (ApiException e) {
				if ( e.getResponseBody().contains("404")) {
					logger.info("Nothing to do");
				}else {
					throw e;
				}
			}  		
			status = Status.OK;
			outDO = Constants.USER_NEW_ROLE_DELETE_SUCCESS;

		} catch (ApiException e) {
			logger.error("Exception message: " + e.getResponseBody());
			e.printStackTrace();

			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_NEW_ROLE_DELETE_FAILED;
			
		} catch (Exception e) {
			logger.error("Exception message: " + e.getMessage());

			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_NEW_ROLE_DELETE_FAILED;

		} catch (Throwable e) {
			logger.error("Exception message: " + e.getMessage());
			e.printStackTrace();
			status = Status.UNAUTHORIZED;
			outDO = Constants.USER_NEW_ROLE_DELETE_FAILED;
		}

		return Util.setCors(NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO));	
	}

	@Override
	public Response other(String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		logger.info("***** OPTIONS /User");

		return Util.setCors(NanoHTTPD.newFixedLengthResponse(""));
	}
}