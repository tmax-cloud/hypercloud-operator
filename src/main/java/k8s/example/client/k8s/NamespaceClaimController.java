package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.models.NamespaceClaim;

public class NamespaceClaimController extends Thread {
	private Watch<NamespaceClaim> nscController;
	private static long latestResourceVersion = 0;
	private CustomObjectsApi api = null;
	ApiClient client = null;
	
    private Logger logger = Main.logger;

	NamespaceClaimController(ApiClient client, CustomObjectsApi api, long resourceVersion) throws Exception {
		nscController = Watch.createWatch(client,
				api.listClusterCustomObjectCall("tmax.io", "v1", Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, null, null, null, null, null, Long.toString( resourceVersion ), null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<NamespaceClaim>>() {}.getType());
		this.api = api;
		this.client = client;
		latestResourceVersion = resourceVersion;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				nscController.forEach(response -> {
					try {
						if (Thread.interrupted()) {
							logger.info("Interrupted!");
							nscController.close();
						}
					} catch (Exception e) {
						logger.info(e.getMessage());
					}
										
					// Logic here
					String claimName = "unknown";
					String resourceName = "unknown";
					try {
						NamespaceClaim claim = response.object;

						if( claim != null) {
							latestResourceVersion = Long.parseLong( response.object.getMetadata().getResourceVersion() );
							String eventType = response.type.toString(); //ADDED, MODIFIED, DELETED
							logger.info("[NamespaceClaim Controller] Event Type : " + eventType );
							logger.info("[NamespaceClaim Controller] == NamespcaeClaim == \n" + claim.toString());
							claimName = claim.getMetadata().getName();
							resourceName = claim.getResourceName();
							
							switch( eventType ) {
								case Constants.EVENT_TYPE_ADDED : 
									// Patch Status to Awaiting
									replaceNscStatus( claimName, Constants.CLAIM_STATUS_AWAITING, "wait for admin permission" );
									break;
								case Constants.EVENT_TYPE_MODIFIED : 
									String status = getClaimStatus( claimName );		
									if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && K8sApiCaller.namespaceAlreadyExist( resourceName ) ) {						
										K8sApiCaller.updateNamespace( claim );
										replaceNscStatus( claimName, Constants.CLAIM_STATUS_SUCCESS, "namespace update success." );
									} else if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && !K8sApiCaller.namespaceAlreadyExist( resourceName ) ) {
										K8sApiCaller.createNamespace( claim );
										replaceNscStatus( claimName, Constants.CLAIM_STATUS_SUCCESS, "namespace create success." );
									}
									break;
								case Constants.EVENT_TYPE_DELETED : 
									// Nothing to do
									break;
							}
						}					
					} catch (Exception e) {
						logger.info("Exception: " + e.getMessage());
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						logger.info(sw.toString());
						try {
							replaceNscStatus( claimName, Constants.CLAIM_STATUS_ERROR, e.getMessage() );
						} catch (ApiException e1) {
							e1.printStackTrace();
							logger.info("Namespace Claim Controller Exception : Change Status 'Error' Fail ");
						}
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				logger.info("=============== NSC 'For Each' END ===============");
				nscController = Watch.createWatch(client,
						api.listClusterCustomObjectCall("tmax.io", "v1", Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, null, null, null, null, null, Long.toString( latestResourceVersion ), null, Boolean.TRUE, null),
						new TypeToken<Watch.Response<NamespaceClaim>>() {}.getType());
			}
		} catch (Exception e) {
			logger.info("Namespace Claim Controller Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.info(sw.toString());
		}
	}

	@SuppressWarnings("unchecked")
	private void replaceNscStatus( String name, String status, String reason ) throws ApiException {
		JsonArray patchStatusArray = new JsonArray();
		JsonObject patchStatus = new JsonObject();
		JsonObject statusObject = new JsonObject();
		patchStatus.addProperty("op", "replace");
		patchStatus.addProperty("path", "/status");
		statusObject.addProperty( "status", status );
		statusObject.addProperty( "reason", reason );
		patchStatus.add("value", statusObject);
		patchStatusArray.add( patchStatus );
		
		logger.info( "Patch Status Object : " + patchStatusArray );
		/*[
		  "op" : "replace",
		  "path" : "/status",
		  "value" : {
		    "status" : "Awaiting"
		  }
		]*/
		try {
			api.patchClusterCustomObjectStatus(
					Constants.CUSTOM_OBJECT_GROUP, 
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, 
					name, 
					patchStatusArray );
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			logger.info("ApiException Code: " + e.getCode());
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	private String getClaimStatus( String name ) throws ApiException {
		Object claimJson = null;
		try {
			claimJson = api.getClusterCustomObject(
					Constants.CUSTOM_OBJECT_GROUP, 
					Constants.CUSTOM_OBJECT_VERSION, 
					Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, 
					name );
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			logger.info("ApiException Code: " + e.getCode());
			throw e;
		}

		String objectStr = new Gson().toJson( claimJson );
		logger.info( objectStr );
		
		JsonParser parser = new JsonParser();
		String status = parser.parse( objectStr ).getAsJsonObject().get( "status" ).getAsJsonObject().get( "status" ).getAsString();
		
		logger.info( "Status : " + status );

		return status;
	}
	
	public static long getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
