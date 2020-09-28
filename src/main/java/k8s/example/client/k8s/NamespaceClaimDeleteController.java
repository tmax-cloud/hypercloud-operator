package k8s.example.client.k8s;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.models.NamespaceClaim;
import k8s.example.client.models.StateCheckInfo;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class NamespaceClaimDeleteController extends Thread {

        private Watch<NamespaceClaim> nscDeleteController;
        private static long latestResourceVersion = 0;
        private CustomObjectsApi api = null;
        ApiClient client = null;

        private Logger logger = Main.logger;
        StateCheckInfo sci = new StateCheckInfo();

        NamespaceClaimDeleteController(ApiClient client, CustomObjectsApi api, long resourceVersion) throws Exception {
        nscDeleteController = Watch.createWatch(client, api.listClusterCustomObjectCall("tmax.io", "v1",
                Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, null, null, null, null, null, null, null, Boolean.TRUE, null),
                new TypeToken<Watch.Response<NamespaceClaim>>() {
                }.getType());
        this.api = api;
        this.client = client;
        latestResourceVersion = resourceVersion;
    }

        @Override
        public void run () {
        try {
            while (true) {
                sci.checkThreadState();
                nscDeleteController.forEach(response -> {
                    try {
                        if (Thread.interrupted()) {
                            logger.error("Interrupted!");
                            nscDeleteController.close();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }

                    // Logic here
                    try {
                        NamespaceClaim claim = response.object;

                        if (claim != null) {
                            latestResourceVersion = Long.parseLong(response.object.getMetadata().getResourceVersion());
                            String eventType = response.type; //ADDED, MODIFIED, DELETED

                            switch (eventType) {
                                case Constants.EVENT_TYPE_DELETED:
                                    logger.info("[NamespaceClaimDelete Controller] Event Type : " + eventType);
                                    logger.debug("[NamespaceClaimDelete Controller] == NamespcaeClaim == \n" + claim.toString());
                                    // Delete ClusterRole for NSC Claim User
                                    logger.info("[NamespaceClaimDelete Controller] Delete ClusterRole [ " + claim.getMetadata().getName() + " ]");
                                    try{
                                        K8sApiCaller.deleteClusterRole(claim.getMetadata().getName());
                                    } catch (Exception e) {
                                        if (e.getMessage().contains("Not Found")) {
                                            logger.info("[NamespaceClaimDelete Controller] ClusterRole [ " + claim.getMetadata().getName() + " ] " +
                                                    "is Already Deleted, Do nothing");
                                        } else {
                                            logger.error(e.getMessage());
                                            e.getStackTrace();
                                        }
                                    }

                                    logger.info("[NamespaceClaimDelete Controller] Delete ClusterRoleBinding [ " + claim.getMetadata().getName() + " ]");
                                    try{
                                        K8sApiCaller.deleteClusterRoleBinding(claim.getMetadata().getName());
                                    } catch (Exception e) {
                                        if (e.getMessage().contains("Not Found")) {
                                            logger.info("[NamespaceClaimDelete Controller] ClusterRoleBinding [ " + claim.getMetadata().getName() + " ] " +
                                                    "is Already Deleted, Do nothing");
                                        } else {
                                            logger.error(e.getMessage());
                                            e.getStackTrace();
                                        }
                                    }
                                    break;
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
                logger.info("=============== NSC 'For Each' END ===============");
                nscDeleteController = Watch.createWatch(client,
                        api.listClusterCustomObjectCall("tmax.io", "v1", Constants.CUSTOM_OBJECT_PLURAL_NAMESPACECLAIM, null, null, null, null, null, null, null, Boolean.TRUE, null),
                        new TypeToken<Watch.Response<NamespaceClaim>>() {
                        }.getType());
            }

        } catch (Exception e) {
            logger.error("Namespace Claim Delete Controller Exception: " + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(sw.toString());
            if (e.getMessage().equals("abnormal")) {
                logger.error("Catch abnormal conditions!! Exit process");
                System.exit(1);
            }
        }
    }

        public static long getLatestResourceVersion () {
        return latestResourceVersion;
    }

}
