package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1ClusterRole;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PolicyRule;
import io.kubernetes.client.openapi.models.V1RoleRef;
import io.kubernetes.client.openapi.models.V1Subject;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.metering.TimerMap;
import k8s.example.client.models.NamespaceClaim;
import k8s.example.client.models.RoleBindingClaim;

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
									if ( K8sApiCaller.namespaceAlreadyExist( resourceName ) ) {
										replaceNscStatus( claimName, Constants.CLAIM_STATUS_REJECT, "Duplicated NameSpaceName" );
									} else {
										// Patch Status to Awaiting
										replaceNscStatus( claimName, Constants.CLAIM_STATUS_AWAITING, "wait for admin permission" );
										// If Trial Type 
										if ( claim.getMetadata().getLabels() != null && claim.getMetadata().getLabels().get("trial") !=null 
												&& claim.getMetadata().getLabels().get("owner") !=null) {
											// give owner all verbs of NSC of 
											patchUserRole ( claim.getMetadata().getLabels().get("owner"), claim.getMetadata().getName() );
											sendConfirmMail ( claim.getMetadata().getLabels().get("owner") );
										}
									}
									break;
								case Constants.EVENT_TYPE_MODIFIED : 
									String status = getClaimStatus( claimName );		
									if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && K8sApiCaller.namespaceAlreadyExist( resourceName ) ) {						
										K8sApiCaller.updateNamespace( claim );
										replaceNscStatus( claimName, Constants.CLAIM_STATUS_SUCCESS, "namespace update success." );
									} else if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && !K8sApiCaller.namespaceAlreadyExist( resourceName ) ) {
										V1Namespace nsResult = K8sApiCaller.createNamespace( claim );
										logger.info(" Create NameSpace [ " + nsResult.getMetadata().getName() + " ] Success");

										// If Trial Type 
										if ( nsResult.getMetadata().getLabels() != null && nsResult.getMetadata().getLabels().get("trial") !=null 
												&& nsResult.getMetadata().getLabels().get("owner") !=null) {
											// Make RoleBinding for Trial User
											try{ 
												createTrialRoleBinding ( nsResult );

											} catch (ApiException e) {
												logger.info(" TrialRoleBinding for Trial NameSpace [ " + nsResult.getMetadata().getName() + " ] Already Exists ");
											}
											
											// Set Timers to Send Mail (3 weeks later), Delete Trial NS (1 month later)
											if ( !TimerMap.isExists(nsResult.getMetadata().getName()) ) {
												Util.setTrialNSTimer( nsResult );
												for (String nsName : TimerMap.getTimerList()) {
													logger.info("   Registered NameSpace Timer in test : " + nsName );
												}
											} else {
												logger.info(" Timer for Trial NameSpace [ " + nsResult.getMetadata().getName() + " ] Already Exists ");
											}
										}
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


	private void sendConfirmMail(String userId) throws Throwable {
		UserCR user;
		try {
			user = K8sApiCaller.getUser(userId);
			String subject = " TmaxConsole Trial 서비스 구독이 신청되었습니다. ";
			String body = Constants.TRIAL_CONFIRM_MAIL_CONTENTS;
			Util.sendMail(user.getUserInfo().getEmail(), subject, body);
		} catch (Throwable e) {
			e.printStackTrace();
			logger.info(e.getMessage());
			throw e;
		}
		
		
	}

	private void patchUserRole(String clusterRoleName, String claimName ) {
		V1ClusterRole clusterRole = null;
		clusterRole = K8sApiCaller.readClusterRole(clusterRoleName);
		V1PolicyRule rule = new V1PolicyRule();
		rule.addApiGroupsItem(Constants.CUSTOM_OBJECT_GROUP);
		rule.addResourcesItem("namespaceclaims");
		rule.addVerbsItem("*");
		rule.addResourceNamesItem(claimName);
		clusterRole.addRulesItem(rule);
		V1ClusterRole replaceResult = K8sApiCaller.replaceClusterRole( clusterRole );	
		logger.info("Add rules of NameSpace claim [ " + claimName + " ] to Trial Owner [ " + clusterRoleName + " ] Success");
	}

	private void createTrialRoleBinding(V1Namespace nsResult) throws ApiException {
		RoleBindingClaim rbcForTrial = new RoleBindingClaim();
		
		V1ObjectMeta RoleBindingMeta = new V1ObjectMeta();
		rbcForTrial.setResourceName("trial-" + nsResult.getMetadata().getName());
		RoleBindingMeta.setName( "trial-" + nsResult.getMetadata().getName());
		RoleBindingMeta.setNamespace( nsResult.getMetadata().getName());
		RoleBindingMeta.setLabels(nsResult.getMetadata().getLabels()); // label 넘겨주기
		rbcForTrial.setMetadata(RoleBindingMeta);

		// RoleRef
		V1RoleRef roleRef = new V1RoleRef();
		roleRef.setApiGroup(Constants.RBAC_API_GROUP);
		roleRef.setKind("ClusterRole");
		roleRef.setName("namespace-owner");  //FIXME : policy 에 따라 변화해 줄 예정
		rbcForTrial.setRoleRef(roleRef);

		// subject
		List< V1Subject > subjectList = new ArrayList<>();
		V1Subject subject = new V1Subject();
		subject.setApiGroup(Constants.RBAC_API_GROUP);
		subject.setKind("User");
		subject.setName(nsResult.getMetadata().getLabels().get("owner"));
		subjectList.add(subject);
		rbcForTrial.setSubjects(subjectList);
		
		try {
			K8sApiCaller.createRoleBinding(rbcForTrial);	
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
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
