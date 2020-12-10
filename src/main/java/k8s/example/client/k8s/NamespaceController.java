package k8s.example.client.k8s;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.models.NamespaceClaim;
import k8s.example.client.models.StateCheckInfo;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;

public class NamespaceController extends Thread {

        private Watch<V1Namespace> nsController;
        private static long latestResourceVersion = 0;
        private CustomObjectsApi customApi = null;
        private CoreV1Api api = null;
        ApiClient client = null;
        private Logger logger = Main.logger;
        StateCheckInfo sci = new StateCheckInfo();

        NamespaceController(ApiClient client, CoreV1Api api, CustomObjectsApi customApi,  long resourceVersion) throws Exception {
            nsController = Watch.createWatch(client, api.listNamespaceCall(null, null, null,null, null, null, "0", null, Boolean.TRUE,null ),
                new TypeToken<Watch.Response<V1Namespace>>() {}.getType());
            latestResourceVersion = resourceVersion;
            this.customApi = customApi;
            this.api = api;
            this.client = client;
        }

        @Override
        public void run () {
        try {
            while (true) {
                sci.checkThreadState();
                nsController.forEach(response -> {
                    try {
                        if (Thread.interrupted()) {
                            logger.error("Interrupted!");
                            nsController.close();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }

                    // Logic here
                    try {
                        V1Namespace ns = response.object;
                        if (ns != null) {
                            latestResourceVersion = Long.parseLong(response.object.getMetadata().getResourceVersion());
                            String eventType = response.type; //ADDED, MODIFIED, DELETED

                            if(eventType.equalsIgnoreCase(Constants.EVENT_TYPE_MODIFIED) || eventType.equalsIgnoreCase(Constants.EVENT_TYPE_ADDED) ){
                                if( ns.getStatus().getPhase().equalsIgnoreCase("Terminating") &&
                                        ns.getMetadata().getLabels()!=null && ns.getMetadata().getLabels().get("fromClaim")!=null){
                                    logger.info("[Namespace Controller] Event Type : " + eventType);
                                    logger.info("[Namespace Controller] Delete Finalizer");
                                    if ( ns.getMetadata().getFinalizers()!=null){
                                        ns.getMetadata().getFinalizers().remove("namespace/finalizers");
                                        K8sApiCaller.replaceNamespace(ns);
                                        logger.info("[Namespace Controller] Delete Finalizer Success");
                                    }

                                    logger.info("[Namespace Controller] Namespace [ " + ns.getMetadata().getName() + " ] from " +
                                            "NamespaceClaim [ " + ns.getMetadata().getLabels().get("fromClaim") +" ] Deleted");
                                    logger.debug("[Namespace Controller] == Namespace == " + ns.toString());

                                    // Delete ClusterRoleBinding for NSC Claim User
                                    logger.info("[Namespace Controller] Delete ClusterRoleBinding [ CRB-" + ns.getMetadata().getName() + " ]");
                                    try{
                                        K8sApiCaller.deleteClusterRoleBinding("CRB-" + ns.getMetadata().getName());
                                    } catch (Exception e) {
                                        if (e.getMessage().contains("Not Found")) {
                                            logger.info("[Namespace Controller] ClusterRoleBinding [ CRB-" + ns.getMetadata().getName() + " ] " +
                                                    "is Already Deleted, Do nothing");
                                        } else {
                                            logger.error(e.getMessage());
                                            e.getStackTrace();
                                        }
                                    }

                                    // Update NamespaceClaim Status to Deleted
                                    logger.info("[Namespace Controller] Update NamespaceClaim [ " + ns.getMetadata().getLabels().get("fromClaim") + " ] Status to Deleted");
                                    replaceNscStatus( ns.getMetadata().getLabels().get("fromClaim"), Constants.CLAIM_STATUS_NAMESPACE_DELETED,
                                            "Namespace " + ns.getMetadata().getName() + " Deleted" );
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Exception: " + e.getMessage());
                        e.getStackTrace().toString();
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        logger.error(sw.toString());
                    } catch (Throwable e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
                logger.info("=============== NS 'For Each' END ===============");
                nsController = Watch.createWatch(client, api.listNamespaceCall(null, null, null,null, null, null, "0", null, Boolean.TRUE,null ),
                        new TypeToken<Watch.Response<V1Namespace>>() {}.getType());

            }

        } catch (Exception e) {
            logger.error("Namespace Controller Exception: " + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(sw.toString());
            if (e.getMessage().equals("abnormal")) {
                logger.error("Catch abnormal conditions!! Exit process");
                System.exit(1);
            }
        }
    }

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
            customApi.patchClusterCustomObjectStatus(
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

    public static long getLatestResourceVersion () {
        return latestResourceVersion;
    }

}
