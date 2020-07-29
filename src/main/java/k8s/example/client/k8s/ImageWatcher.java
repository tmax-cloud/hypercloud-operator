package k8s.example.client.k8s;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import k8s.example.client.Constants;
import k8s.example.client.Main;
import k8s.example.client.models.Image;
import k8s.example.client.models.StateCheckInfo;

public class ImageWatcher extends Thread {
	private Watch<Image> watchImage;
	private static String latestResourceVersion = "0";
	private CustomObjectsApi api = null;
	ApiClient client;
    private Logger logger = Main.logger;
    StateCheckInfo sci = new StateCheckInfo();
	
	ImageWatcher(ApiClient client, CustomObjectsApi api, String resourceVersion) throws Exception {
		watchImage = Watch.createWatch(client,
				api.listClusterCustomObjectCall("tmax.io", "v1", "images", null, null, null, "handled=f", null, null, null, Boolean.TRUE, null),
				new TypeToken<Watch.Response<Image>>() {}.getType());

		this.api = api;
		this.client = client;
		latestResourceVersion = resourceVersion;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				sci.checkThreadState();
				watchImage.forEach(response -> {
					try {
						if (Thread.interrupted()) {
							logger.error("Interrupted!");
							watchImage.close();
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
					}


					// Logic here
					try {
						Image image = response.object;

						if( image != null ) {
							
							latestResourceVersion = response.object.getMetadata().getResourceVersion();
							String eventType = response.type.toString();
							logger.debug("====================== Image " + eventType + " ====================== \n");
							
							switch(eventType) {
							case Constants.EVENT_TYPE_ADDED: 
								
								break;
							case Constants.EVENT_TYPE_MODIFIED:
								
								break;
							case Constants.EVENT_TYPE_DELETED : 
								
								break;
							}						
						}


					} catch (Exception e) {
						logger.error("Exception: " + e.getMessage());
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						logger.error(sw.toString());
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				});
				
				logger.debug("=============== Image 'For Each' END ===============");
				watchImage = Watch.createWatch(client,
						api.listClusterCustomObjectCall("tmax.io", "v1", "images", null, null, null, "handled=f", null, null, null, Boolean.TRUE, null),
						new TypeToken<Watch.Response<Image>>() {}.getType());
			}
		} catch (Exception e) {
			logger.error("Image Watcher Exception: " + e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			if( e.getMessage().equals("abnormal") ) {
				logger.error("Catch abnormal conditions!! Exit process");
				System.exit(1);
			}
		}
	}

	public static String getLatestResourceVersion() {
		return latestResourceVersion;
	}
	
}
