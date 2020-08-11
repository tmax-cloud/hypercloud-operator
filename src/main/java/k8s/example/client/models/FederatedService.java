package k8s.example.client.models;

import io.kubernetes.client.openapi.models.V1ObjectMeta;

public class FederatedService {
	private String apiVersion = null;
	private String kind = null;
	private V1ObjectMeta metadata = null;
	
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
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class FederatedService {\n");
		if(apiVersion != null) sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
		if(kind != null) sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
		if(metadata != null ) 	sb.append("    metadata: ").append(toIndentedString(metadata.toString())).append("\n");
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
}
