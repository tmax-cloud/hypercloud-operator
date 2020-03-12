package k8s.example.client;

import k8s.example.client.k8s.K8sApiCaller;

public class Main {
	public static void main(String[] args) {
		try {
			// Start webhook server
			System.out.println("[Main] Start webhook server");
			new WebHookServer();
			
			// User watcher
			System.out.println("[Main] Init & start K8S watchers");
			K8sApiCaller.initK8SClient();
			K8sApiCaller.startWatcher(); // Infinite loop
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}