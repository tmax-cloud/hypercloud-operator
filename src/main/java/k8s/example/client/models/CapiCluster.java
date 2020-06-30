package k8s.example.client.models;

import io.kubernetes.client.openapi.models.V1ObjectMeta;

public class CapiCluster {
	private String resourceName = null;	
	private String apiVersion = "cluster.x-k8s.io";
	private String kind = null;
	private V1ObjectMeta metadata = null;
	private CapiClusterStatus status = null;
	
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
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public V1ObjectMeta getMetadata() {
		return metadata;
	}
	public void setMetadata(V1ObjectMeta metadata) {
		this.metadata = metadata;
	}
	public CapiClusterStatus getStatus() {
		return status;
	}
	public void setStatus(CapiClusterStatus status) {
		this.status = status;
	}
		
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class CapiMachine {\n");
		sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
		if(metadata != null ) 	sb.append("    metadata: ").append(toIndentedString(metadata.toString())).append("\n");
		if(resourceName != null ) 	sb.append("    resourceName: ").append(toIndentedString(resourceName.toString())).append("\n");
		if(status != null ) 	sb.append("    status: ").append(toIndentedString(status.toString())).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	public String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		
		return o.toString().replace("\n", "\n    ");
	}
	
	public class CapiClusterStatus {
		private String controlPlaneInitialized;

		public String getControlPlaneInitialized() {
			return controlPlaneInitialized;
		}

		public void setControlPlaneInitialized(String controlPlaneInitialized) {
			this.controlPlaneInitialized = controlPlaneInitialized;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("class Capi Machine Status {\n");
			if(controlPlaneInitialized != null ) 	sb.append("    phase: ").append(toIndentedString(controlPlaneInitialized)).append("\n");
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
}
