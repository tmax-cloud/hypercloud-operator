package k8s.example.client.handler;

import java.util.Map;

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
import k8s.example.client.k8s.K8sApiCaller;
import k8s.example.client.models.Services;

public class CatalogHandler extends GeneralHandler {
	@Override
    public Response get(
      UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		System.out.println("***** GET /v2/catalog");
		
		Services catalog = null;
		IStatus status = null;
		
		try {
			catalog = K8sApiCaller.getCatalog();
			status = Status.OK;
		} catch (ApiException e) {
			System.out.println( "  Get Catalog fail" );
			System.out.println( "Exception message: " + e.getMessage() );
			e.printStackTrace();
			status = Status.NOT_FOUND;
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String outDO = gson.toJson(catalog).toString();
		System.out.println("Catalog : " + outDO);
		
		System.out.println();
		Response resp = NanoHTTPD.newFixedLengthResponse(status, NanoHTTPD.MIME_HTML, outDO);
		resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Max-Age", "3628800");
        resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        resp.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
        resp.addHeader("Access-Control-Allow-Headers", "Authorization");
		return resp;
		
	}
}