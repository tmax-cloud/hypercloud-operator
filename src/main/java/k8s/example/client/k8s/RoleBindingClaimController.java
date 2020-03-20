package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

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
import k8s.example.client.models.NamespaceClaim;
import k8s.example.client.models.RoleBindingClaim;

public class RoleBindingClaimController extends Thread {
	private final Watch<RoleBindingClaim> rbcController;
	private static int latestResourceVersion = 0;
	private CustomObjectsApi api = null;

	RoleBindingClaimController(ApiClient client, CustomObjectsApi api, int resourceVersion) throws Exception {
		rbcController = Watch.createWatch(client,
				api.listClusterCustomObjectCall("tmax.io", "v1", Constants.CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM, null, null, null, null, null, Integer.toString( resourceVersion ), null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<RoleBindingClaim>>() {}.getType());
		this.api = api;
		latestResourceVersion = resourceVersion;
	}
	
	@Override
	public void run() {
		try {
			rbcController.forEach(response -> {
				try {
					if (Thread.interrupted()) {
						System.out.println("Interrupted!");
						rbcController.close();
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				
				
				// Logic here
				String claimName = "unknown";
				String claimNamespace = "unknown";
				try {
					RoleBindingClaim claim = response.object;

					if( claim != null) {
						latestResourceVersion = Integer.parseInt( response.object.getMetadata().getResourceVersion() );
						String eventType = response.type.toString(); //ADDED, MODIFIED, DELETED
						System.out.println("[RoleBindingClaim Controller] Event Type : " + eventType );
						System.out.println("[RoleBindingClaim Controller] == ResourceQuotaClaim == \n" + claim.toString());
						claimName = claim.getMetadata().getName();
						claimNamespace = claim.getMetadata().getNamespace();
						switch( eventType ) {
							case Constants.EVENT_TYPE_ADDED : 
								// Patch Status to Awaiting
								replaceRbcStatus( claim.getMetadata().getName(), Constants.CLAIM_STATUS_AWAITING, "wait for admin permission", claimNamespace );
								break;
							case Constants.EVENT_TYPE_MODIFIED : 
								String status = getClaimStatus( claim.getMetadata().getName(), claimNamespace );
								if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && !K8sApiCaller.roleBindingAlreadyExist( claimName, claimNamespace ) ) {
									K8sApiCaller.createRoleBinding( claim );
									replaceRbcStatus( claim.getMetadata().getName(), Constants.CLAIM_STATUS_SUCCESS, "rolebinding create success.", claimNamespace );
								} else if ( ( status.equals( Constants.CLAIM_STATUS_AWAITING ) || status.equals( Constants.CLAIM_STATUS_REJECT ) ) && K8sApiCaller.namespaceAlreadyExist( claimName ) ) {
									replaceRbcStatus( claim.getMetadata().getName(), Constants.CLAIM_STATUS_SUCCESS, "rolebinding create success.", claimNamespace );
								}
								break;
							case Constants.EVENT_TYPE_DELETED : 
								// Nothing to do
								break;
						}
					}
					
				} catch (Exception e) {
					System.out.println("Exception: " + e.getMessage());
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					System.out.println(sw.toString());
					try {
						replaceRbcStatus( claimName, Constants.CLAIM_STATUS_ERROR, e.getMessage(), claimNamespace );
					} catch (ApiException e1) {
						e1.printStackTrace();
						System.out.println("Resource Quota Claim Controller Exception : Change Status 'Error' Fail ");
					}
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			System.out.println("Resource Quota Claim Controller Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			System.out.println(sw.toString());
		}
	}

	@SuppressWarnings("unchecked")
	private void replaceRbcStatus( String name, String status, String reason, String namespace ) throws ApiException {
		JsonArray patchStatusArray = new JsonArray();
		JsonObject patchStatus = new JsonObject();
		JsonObject statusObject = new JsonObject();
		patchStatus.addProperty("op", "replace");
		patchStatus.addProperty("path", "/status");
		statusObject.addProperty( "status", status );
		statusObject.addProperty( "reason", reason );
		patchStatus.add("value", statusObject);
		patchStatusArray.add( patchStatus );
		
		System.out.println( "Patch Status Object : " + patchStatusArray );
		/*[
		  "op" : "replace",
		  "path" : "/status",
		  "value" : {
		    "status" : "Awaiting"
		  }
		]*/
		try {
			api.patchNamespacedCustomObjectStatus(
					Constants.CUSTOM_OBJECT_GROUP, 
					Constants.CUSTOM_OBJECT_VERSION, 
					namespace, 
					Constants.CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM, 
					name, 
					patchStatusArray );
		} catch (ApiException e) {
			System.out.println(e.getResponseBody());
			System.out.println("ApiException Code: " + e.getCode());
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	private String getClaimStatus( String name, String namespace ) throws ApiException {
		Object claimJson = null;
		try {
			claimJson = api.getNamespacedCustomObject(
					Constants.CUSTOM_OBJECT_GROUP, 
					Constants.CUSTOM_OBJECT_VERSION, 
					namespace, 
					Constants.CUSTOM_OBJECT_PLURAL_ROLEBINDINGCLAIM,  
					name );
		} catch (ApiException e) {
			System.out.println(e.getResponseBody());
			System.out.println("ApiException Code: " + e.getCode());
			throw e;
		}

		String objectStr = new Gson().toJson( claimJson );
		System.out.println( objectStr );
		
		JsonParser parser = new JsonParser();
		String status = parser.parse( objectStr ).getAsJsonObject().get( "status" ).getAsJsonObject().get( "status" ).getAsString();
		
		System.out.println( "Status : " + status );

		return status;
	}
	
	public static int getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
