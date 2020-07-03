package k8s.example.client.models;

import io.kubernetes.client.openapi.models.V1ObjectMeta;

public class KubeFedCluster {
	private V1ObjectMeta metadata = null;
	private KubeFedClusterSpec spec = null;
	private String apiVersion;
	private String kind;	
	
	public String getApiVersion() {
		return apiVersion;
	}
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public V1ObjectMeta getMetadata() {
		return metadata;
	}
	public void setMetadata(V1ObjectMeta metadata) {
		this.metadata = metadata;
	}
	public KubeFedClusterSpec getSpec() {
		return spec;
	}
	public void setSpec(KubeFedClusterSpec spec) {
		this.spec = spec;
	}
		
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class KubeKubeFedCluster {\n");
		sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
		if(metadata != null ) 	sb.append("    metadata: ").append(toIndentedString(metadata.toString())).append("\n");
		if(spec != null ) 	sb.append("    spec: ").append(toIndentedString(spec.toString())).append("\n");
		sb.append("}");
		return sb.toString();
	}

	public static class SecretRef {
		private String name;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("class SecretRef {\n");
			if(name != null ) 	sb.append("    Name: ").append(toIndentedString(name)).append("\n");
			sb.append("}");
			return sb.toString();
		}
		
		/**
		 * Convert the given object to string with each line indented by 4 spaces
		 * (except the first line).
		 */	
		private String toIndentedString(java.lang.Object o) {
			if (o == null) {
				return "null";
			}
			
			return o.toString().replace("\n", "\n    ");
		}
	}
	
	public static class KubeFedClusterSpec {
		private String apiEndpoint;
		private String caBundle;
		private SecretRef secretRef;

		public String getApiEndpoint() {
			return apiEndpoint;
		}

		public void setApiEndpoint(String APIEndpoint) {
			this.apiEndpoint = APIEndpoint;
		}
		
		public String getCaBundle() {
			return caBundle;
		}

		public void setCaBundle(String CABundle) {
			this.caBundle = CABundle;
		}
		
		public SecretRef getSecretRef() {
			return secretRef;
		}

		public void setSecretRef(SecretRef secretRef) {
			this.secretRef = secretRef;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("class Capi Machine Spec {\n");
			if(apiEndpoint != null ) 	sb.append("    APIEndpoint: ").append(toIndentedString(apiEndpoint)).append("\n");
			if(caBundle != null ) 	sb.append("    CABundle: ").append(toIndentedString(caBundle)).append("\n");
			if(secretRef != null ) 	sb.append("    secretRef: ").append(toIndentedString(secretRef)).append("\n");
			sb.append("}");
			return sb.toString();
		}
		
		/**
		 * Convert the given object to string with each line indented by 4 spaces
		 * (except the first line).
		 */	
		private String toIndentedString(java.lang.Object o) {
			if (o == null) {
				return "null";
			}
			return o.toString().replace("\n", "\n    ");
		}
	}	
	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */	
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
