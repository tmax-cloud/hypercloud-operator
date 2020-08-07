package k8s.example.client.models;

import io.kubernetes.client.openapi.models.V1ObjectMeta;

public class ServiceDNSRecord {
	private String apiVersion = null;
	private String kind = null;
	private V1ObjectMeta metadata = null;
	private ServiceDNSRecordSpec spec = null;
	
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
	public ServiceDNSRecordSpec getSpec() {
		return spec;
	}
	public void setSpec(ServiceDNSRecordSpec spec) {
		this.spec = spec;
	}
		
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ServiceDNSRecord {\n");
		if(apiVersion != null) sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
		if(kind != null) sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
		if(metadata != null ) 	sb.append("    metadata: ").append(toIndentedString(metadata.toString())).append("\n");
		if(spec != null ) 	sb.append("    spec: ").append(toIndentedString(spec.toString())).append("\n");
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
	
	public static class ServiceDNSRecordSpec {
		private String domainRef;
		private Integer recordTTL;
		
		public String getDomainRef() {
			return domainRef;
		}
		public void setDomainRef(String domainRef) {
			this.domainRef = domainRef;
		}
		public Integer getRecordTTL() {
			return recordTTL;
		}
		public void setRecordTTL(Integer recordTTL) {
			this.recordTTL = recordTTL;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("class ServiceDNSRecordSpec {\n");
			if(domainRef != null ) 	sb.append("    domainRef: ").append(toIndentedString(domainRef)).append("\n");
			if(recordTTL != null ) 	sb.append("    recordTTL: ").append(toIndentedString(recordTTL)).append("\n");
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
