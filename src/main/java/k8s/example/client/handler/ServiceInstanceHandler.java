package k8s.example.client.handler;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.GeneralHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import io.kubernetes.client.openapi.ApiException;
import k8s.example.client.DataObject.LoginInDO;
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.models.ProvisionInDO;
import k8s.example.client.models.Services;

public class ServiceInstanceHandler extends GeneralHandler {
	@Override
    public Response put(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		System.out.println("***** PUT /v2/service_instances");
		
		Map<String, String> body = new HashMap<String, String>();
        try {
			session.parseBody( body );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        ProvisionInDO inDO = null;
        String outDO = null;
		IStatus status = null;
		String instanceId = session.getParameters().get("service_instances").get(0);
		
		try {
			inDO = new ObjectMapper().readValue(body.get( "postData" ), ProvisionInDO.class);
			System.out.println("Instance ID: " + instanceId);
			System.out.println("Service ID: " + inDO.getService_id());
			System.out.println("Service Plan ID: " + inDO.getPlan_id());
			System.out.println("Context: " + inDO.getContext().toString());
			
//			K8sApiCaller.createServiceInstance(instanceId, inDO);
			
			status = Status.OK;
		} catch (Exception e) {
			System.out.println( "  Get Catalog fail" );
			System.out.println( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.NOT_FOUND;
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		outDO = gson.toJson(inDO).toString();
		System.out.println("Catalog : " + outDO);
		
		System.out.println();
		return NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO);
    }

}
