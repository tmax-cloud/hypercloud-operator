package k8s.example.client.models;

import java.util.List;

import io.kubernetes.client.openapi.models.V1ListMeta;

public class NamespaceClaimList {
	private String apiVersion = "tmax.io/v1";
	private String kind = "NamespaceClaimList";
	private V1ListMeta metadata = null;
	private List < NamespaceClaim > items = null;
	private String operatorStartTime = null;
	
	public String getOperatorStartTime() {
		return operatorStartTime;
	}
	public void setOperatorStartTime(String operatorStartTime) {
		this.operatorStartTime = operatorStartTime;
	}
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
	public V1ListMeta getMetadata() {
		return metadata;
	}
	public void setMetadata(V1ListMeta metadata) {
		this.metadata = metadata;
	}
	public List < NamespaceClaim > getItems() {
		return items;
	}
	public void setItems(List < NamespaceClaim > items) {
		this.items = items;
	}
	public NamespaceClaimList addItemsItem(NamespaceClaim itemsItem) {
	    this.items.add(itemsItem);
	    return this;
	}
	public NamespaceClaimList items(List<NamespaceClaim> items) {    
	    this.items = items;
	    return this;
    }
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Namespace Claim {\n");
		sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
	    sb.append("    items: ").append(toIndentedString(items)).append("\n");
	    sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
		if(metadata != null ) 	sb.append("    metadata: ").append(toIndentedString(metadata.toString())).append("\n");
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
