package k8s.example.client.models;

public class RegistryService {
	public static final String SVC_TYPE_CLUSTER_IP = "ClusterIP";
	public static final String SVC_TYPE_NODE_PORT = "NodePort";
	public static final String SVC_TYPE_LOAD_BALANCER = "LoadBalancer";
	public static final String SVC_TYPE_INGRESS = "Ingress";
	public static final int REGISTRY_TARGET_PORT = 443;
	public static final String REGISTRY_PORT_NAME = "tls";
	public static final String REGISTRY_PORT_PROTOCOL = "TCP";
	private int port = 0;
	private String ingressDomain = null;
	private String type = null;
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getIngressDomain() {
		return ingressDomain;
	}
	public void setIngressDomain(String ingressDomain) {
		this.ingressDomain = ingressDomain;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class RegistryService {\n");
		sb.append("    port: ").append(toIndentedString(String.valueOf(port))).append("\n");
		sb.append("    ingressDomain: ").append(toIndentedString(ingressDomain)).append("\n");
		sb.append("    type: ").append(toIndentedString(type)).append("\n");
		sb.append("}");
		return sb.toString();
	}
	
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
