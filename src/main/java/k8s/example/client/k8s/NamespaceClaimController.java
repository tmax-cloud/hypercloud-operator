package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import io.kubernetes.client.openapi.models.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.DataObject.UserCR;
import k8s.example.client.Main;
import k8s.example.client.Util;
import k8s.example.client.metering.TimerMap;
import k8s.example.client.models.NamespaceClaim;
import k8s.example.client.models.RoleBindingClaim;
import k8s.example.client.models.StateCheckInfo;

public class NamespaceClaimController extends Thread {
	private Watch<NamespaceClaim> nscController;
	private static long latestResourceVersion = 0;
	private CustomObjectsApi api = null;
	ApiClient client = null;
	
    private Logger logger = Main.logger;
	StateCheckInfo sci = new StateCheckInfo();

	NamespaceClaimController(ApiClient client, CustomObjectsApi api, long resourceVersion) throws Exception {
		nscController = Watch.createWatch(client, api.listClusterCustomObjectCall("tmax.io", "v1", 
				Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, null, null, null, "handled=f", null, null, null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<NamespaceClaim>>() {}.getType());
		this.api = api;
		this.client = client;
		latestResourceVersion = resourceVersion;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				sci.checkThreadState();
				nscController.forEach(response -> {
					try {
						if (Thread.interrupted()) {
							logger.error("Interrupted!");
							nscController.close();
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
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
							logger.debug("[NamespaceClaim Controller] == NamespcaeClaim == \n" + claim.toString());
							claimName = claim.getMetadata().getName();
							resourceName = claim.getResourceName();
							
							switch( eventType ) {
								case Constants.EVENT_TYPE_ADDED : 
									if ( claim.getStatus() !=null && claim.getStatus().getStatus() != null ) { // Generated And Status has Changed when DownTime 
										logger.info("[NamespaceClaim Controller] Status of NamespaceClaim [ " + claim.getMetadata().getName() + " ] "
												+ "Already Exists as [ " + claim.getStatus().getStatus() + " ]" );
										if ( claim.getStatus().getStatus().equals(Constants.CLAIM_STATUS_SUCCESS) ) {
											// If Trial Type 
											if ( claim.getMetadata().getLabels() != null && claim.getMetadata().getLabels().get("trial") !=null 
													&& claim.getMetadata().getLabels().get("owner") !=null) {
												// give owner all verbs of NSC ( Except admin-tmax.co.kr)
												if ( !claim.getMetadata().getLabels().get("owner").equalsIgnoreCase( Constants.MASTER_USER_ID )) {
													patchUserRole ( claim.getMetadata().getLabels().get("owner"), claim.getMetadata().getName() );
												}
											}
										}
									}else {
										if ( K8sApiCaller.namespaceAlreadyExist( resourceName ) ) {
											replaceNscStatus( claimName, Constants.CLAIM_STATUS_REJECT, "Duplicated NameSpaceName" ); 
											K8sApiCaller.patchLabel(claimName, "handled" ,"t", Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM , false, null);// must be after replaceNscStatus for awake watcher once more
										} else {
											// Patch Status to Awaiting
											replaceNscStatus( claimName, Constants.CLAIM_STATUS_AWAITING, "wait for admin permission" );
											// If Trial Type 
											if ( claim.getMetadata().getLabels() != null && claim.getMetadata().getLabels().get("trial") !=null 
													&& claim.getMetadata().getLabels().get("owner") !=null) {
												// give owner all verbs of NSC ( Except admin-tmax.co.kr)
												if ( !claim.getMetadata().getLabels().get("owner").equalsIgnoreCase( Constants.MASTER_USER_ID )) {
													patchUserRole ( claim.getMetadata().getLabels().get("owner"), claim.getMetadata().getName() );
												}
											}
										}		
									}
									// Set Owner Label from Annotation 'Creator'
									if ( claim.getMetadata().getAnnotations() != null && claim.getMetadata().getAnnotations().get("creator") !=null
											&& !claim.getMetadata().getAnnotations().get("creator").contains(":")) { // 방어로직
										logger.info("[NamespaceClaim Controller] Set Owner Label from Annotation 'Creator'" );
										K8sApiCaller.patchLabel(claimName, "owner" ,claim.getMetadata().getAnnotations()
												.get("creator"), Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, false, null);// FIXME
									}								
									break;
									
								case Constants.EVENT_TYPE_MODIFIED : 
									String status = getClaimStatus( claimName );		
									if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && K8sApiCaller.namespaceAlreadyExist( resourceName ) ) {	
										K8sApiCaller.updateNamespace( claim );
										K8sApiCaller.patchLabel(claimName, "handled" ,"t", Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM , false, null);// FIXME
										replaceNscStatus( claimName, Constants.CLAIM_STATUS_SUCCESS, "namespace update success." );
										
									} else if ( status.equals( Constants.CLAIM_STATUS_SUCCESS ) && !K8sApiCaller.namespaceAlreadyExist( resourceName ) ) {
										V1Namespace nsResult = K8sApiCaller.createNamespace( claim );
										logger.info(" Create NameSpace [ " + nsResult.getMetadata().getName() + " ] Success");

										// clusterrole-NSC clusterrolebinding
										createNSCClusterRoleBinding ( claim );
										// ingress-nginx-shared namespace read role
										K8sApiCaller.createRoleBindingForIngressNginx(nsResult.getMetadata().getLabels().get("owner"));

										// If Trial Type 
										if ( nsResult.getMetadata().getLabels() != null && nsResult.getMetadata().getLabels().get("trial") !=null 
												&& nsResult.getMetadata().getLabels().get("owner") !=null) {
											// Make RoleBinding for Trial User
											try{ 
												// namspace-owner rolebinding
												createTrialRoleBinding ( nsResult );
											} catch (ApiException e) {
												logger.info(" TrialRoleBinding for Trial NameSpace [ " + nsResult.getMetadata().getName() + " ] Already Exists ");
											}
											// Set Timers to Send Mail (3 weeks later), Delete Trial NS (1 month later)
											if ( !TimerMap.isExists(nsResult.getMetadata().getName()) ) {
												Util.setTrialNSTimer( nsResult );
												for (String nsName : TimerMap.getTimerList()) {
													logger.info(" Registered NameSpace Timer in test : " + nsName );
												}
											} else {
												logger.info(" Timer for Trial NameSpace [ " + nsResult.getMetadata().getName() + " ] Already Exists ");
											}			
											
											// Send Success confirm Mail
											sendConfirmMail ( claim, nsResult.getMetadata().getCreationTimestamp(),  true );																			
										} else {
											// Make Namespaced RoleBinding for non-trial User
											try{
												// namspace-listget rolebinding
												createNSCRoleBinding ( nsResult );
											} catch (ApiException e) {
												logger.info(" RoleBinding for NameSpace [ " + nsResult.getMetadata().getName() + " ] Already Exists ");
											}
										}
										// Create Default NetWork Policy
										logger.info(" Create Network Policy for new Namespace ["+ nsResult.getMetadata().getName() +" ] Starts");
										K8sApiCaller.createDefaultNetPol(claim);
										replaceNscStatus( claimName, Constants.CLAIM_STATUS_SUCCESS, "namespace create success." );
										K8sApiCaller.patchLabel(claimName, "handled" ,"t", Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM , false, null);// FIXME

									} else if ( status.equals( Constants.CLAIM_STATUS_REJECT )) {
										if ( claim.getMetadata().getLabels() != null && claim.getMetadata().getLabels().get("trial") !=null 
												&& claim.getMetadata().getLabels().get("owner") !=null  ) {
											// Send Fail confirm Mail
											sendConfirmMail ( claim, null, false );										
										}
										K8sApiCaller.patchLabel(claimName, "handled" ,"t", Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM , false, null);// FIXME
									}				
									break;
									
								case Constants.EVENT_TYPE_DELETED : 
									// Delete ClusterRoleBinding for Trial New User
//									K8sApiCaller.deleteClusterRoleBinding("trial-" + claim.getMetadata().getName());
									break;
							}		
						}							
					} catch (Exception e) {
						logger.error("Exception: " + e.getMessage());
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						logger.error(sw.toString());
						try {
							replaceNscStatus( claimName, Constants.CLAIM_STATUS_ERROR, e.getMessage() );
						} catch (ApiException e1) {
							e1.printStackTrace();
							logger.error("Namespace Claim Controller Exception : Change Status 'Error' Fail ");
						}
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				logger.info("=============== NSC 'For Each' END ===============");
				nscController = Watch.createWatch(client,
						api.listClusterCustomObjectCall("tmax.io", "v1", Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, null, null, null, "handled=f", null, null, null, Boolean.TRUE, null),
						new TypeToken<Watch.Response<NamespaceClaim>>() {}.getType());
			}
		} catch (Exception e) {
			logger.error("Namespace Claim Controller Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			if( e.getMessage().equals("abnormal") ) {
				logger.error("Catch abnormal conditions!! Exit process");
				System.exit(1);
			}
		}
	}

	private void sendConfirmMail(NamespaceClaim claim, DateTime createTime, boolean flag ) throws Throwable {
		UserCR user = null;
		String subject = null;
		String body = null;
		String imgPath = null;
		String imgCid = null;
		String email = null;
		
		try {
			String accessToken = HyperAuthCaller.loginAsAdmin();
			JsonArray userListJsonArray = HyperAuthCaller.getUserList( accessToken.replaceAll("\"",""));
    		for(JsonElement userJson : userListJsonArray) {
    			if(userJson.getAsJsonObject().get("username").toString().equalsIgnoreCase("\"" + claim.getMetadata().getLabels().get("owner") + "\"" )) {
    				email = userJson.getAsJsonObject().get("email").toString().replaceAll("\"","");
    				break;
    			}
    		}
			logger.info("email : " + email);

			if (flag) {
				subject = " HyperCloud 서비스 신청 승인 완료 ";
				body = Constants.TRIAL_SUCCESS_CONFIRM_MAIL_CONTENTS;
				body = body.replaceAll("%%NAMESPACE_NAME%%", claim.getResourceName());
				body = body.replaceAll("%%TRIAL_START_TIME%%", createTime.toDateTime().toString("yyyy-MM-dd"));
				body = body.replaceAll("%%TRIAL_END_TIME%%", createTime.plusDays(30).toDateTime().toString("yyyy-MM-dd"));
//				body = body.replaceAll("%%SUCCESS_REASON%%", claim.getStatus().getReason());
				imgPath = "/home/tmax/hypercloud4-operator/_html/img/trial-approval.png";
				imgCid = "trial-approval";
			}else {
				subject = " HyperCloud 서비스 신청 승인 거절  ";
				body = Constants.TRIAL_FAIL_CONFIRM_MAIL_CONTENTS;
				if ( claim.getStatus()!= null && claim.getStatus().getReason() != null ) {
					body = body.replaceAll("%%FAIL_REASON%%", claim.getStatus().getReason());
				}else {
					body = body.replaceAll("%%FAIL_REASON%%", "Unknown Reason");
				}
				imgPath = "/home/tmax/hypercloud4-operator/_html/img/trial-disapproval.png";
				imgCid = "trial-disapproval";

			}
			Util.sendMail(email, subject, body, imgPath, imgCid);
		} catch (Throwable e) {
			if (e.getMessage().contains("Not Found") || e.getMessage().contains("404")) {
				logger.info("This Trial NSC was made by Unknown user or System admin, Will not Send Email");
			}else {
				e.printStackTrace();
				logger.error(e.getMessage());
				throw e;
			}
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
//		V1ClusterRole replaceResult = K8sApiCaller.replaceClusterRole( clusterRole );	
		logger.info("Add rules of NameSpace claim [ " + claimName + " ] to Trial Owner [ " + clusterRoleName + " ] Success");
	}

	private void createTrialRoleBinding(V1Namespace nsResult) throws ApiException {
		RoleBindingClaim rbcForTrial = new RoleBindingClaim();

		V1ObjectMeta RoleBindingMeta = new V1ObjectMeta();
		rbcForTrial.setResourceName("trial-" + nsResult.getMetadata().getName());
		RoleBindingMeta.setName("trial-" + nsResult.getMetadata().getName());
		RoleBindingMeta.setNamespace(nsResult.getMetadata().getName());
		RoleBindingMeta.setLabels(nsResult.getMetadata().getLabels()); // label 넘겨주기
		rbcForTrial.setMetadata(RoleBindingMeta);

		// RoleRef
		V1RoleRef roleRef = new V1RoleRef();
		roleRef.setApiGroup(Constants.RBAC_API_GROUP);
		roleRef.setKind("ClusterRole");
		roleRef.setName("namespace-owner");  //FIXME : policy 에 따라 변화해 줄 예정
		rbcForTrial.setRoleRef(roleRef);

		// subject
		List<V1Subject> subjectList = new ArrayList<>();
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

	private void createNSCRoleBinding(V1Namespace nsResult) throws ApiException {

		V1RoleBinding roleBinding = new V1RoleBinding();
		V1ObjectMeta RoleBindingMeta = new V1ObjectMeta();
		RoleBindingMeta.setName( nsResult.getMetadata().getName()+"-ns-listget");
		RoleBindingMeta.setNamespace(nsResult.getMetadata().getName());
		RoleBindingMeta.setLabels(nsResult.getMetadata().getLabels()); // label 넘겨주기
		roleBinding.setMetadata(RoleBindingMeta);

		// RoleRef
		V1RoleRef roleRef = new V1RoleRef();
		roleRef.setApiGroup(Constants.RBAC_API_GROUP);
		roleRef.setKind("ClusterRole");
		roleRef.setName("namespace-listget");  //FIXME : policy 에 따라 변화해 줄 예정
		roleBinding.setRoleRef(roleRef);

		// subject
		V1Subject subject = new V1Subject();
		subject.setApiGroup(Constants.RBAC_API_GROUP);
		subject.setKind("User");
		subject.setName(nsResult.getMetadata().getLabels().get("owner"));
		roleBinding.addSubjectsItem(subject);

		try {
			K8sApiCaller.createGeneralRoleBinding(roleBinding);
		} catch (ApiException e) {
			logger.info(e.getResponseBody());
			throw e;
		}
	}
	
	private void createNSCClusterRoleBinding(NamespaceClaim claim) throws ApiException {
		RoleBindingClaim rbcForNSC = new RoleBindingClaim();
		
		V1ObjectMeta RoleBindingMeta = new V1ObjectMeta();
		rbcForNSC.setResourceName("CRB-" + claim.getResourceName());
		RoleBindingMeta.setName( "CRB-" + claim.getResourceName());
		RoleBindingMeta.setLabels(claim.getMetadata().getLabels()); // label 넘겨주기
		rbcForNSC.setMetadata(RoleBindingMeta);

		// RoleRef
		V1RoleRef roleRef = new V1RoleRef();
		roleRef.setApiGroup(Constants.RBAC_API_GROUP);
		roleRef.setKind("ClusterRole");
		roleRef.setName("clusterrole-trial");  //FIXME : policy 에 따라 변화해 줄 예정
		rbcForNSC.setRoleRef(roleRef);

		// subject
		List< V1Subject > subjectList = new ArrayList<>();
		V1Subject subject = new V1Subject();
		subject.setApiGroup(Constants.RBAC_API_GROUP);
		subject.setKind("User");
		subject.setName(claim.getMetadata().getLabels().get("owner"));
		subjectList.add(subject);
		rbcForNSC.setSubjects(subjectList);
		
		try {
			K8sApiCaller.createClusterRoleBinding(rbcForNSC);
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
		
		logger.debug( "Patch Status Object : " + patchStatusArray );
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
					patchStatusArray);
		} catch (ApiException e) {
			logger.error(e.getResponseBody());
			logger.error("ApiException Code: " + e.getCode());
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
			logger.error(e.getResponseBody());
			logger.error("ApiException Code: " + e.getCode());
			throw e;
		}

		String objectStr = new Gson().toJson( claimJson );
		logger.debug( objectStr );
		
		JsonParser parser = new JsonParser();
		String status = parser.parse( objectStr ).getAsJsonObject().get( "status" ).getAsJsonObject().get( "status" ).getAsString();
		
		logger.info( "Claim Name [ " + name + " ] | Status : " + status );

		return status;
	}
	
	public static long getLatestResourceVersion() {
		return latestResourceVersion;
	}
}
