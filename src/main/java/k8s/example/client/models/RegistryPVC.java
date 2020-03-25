package k8s.example.client.models;

import java.util.ArrayList;
import java.util.List;

public class RegistryPVC {
//	public static final String STORAGE_CLASS_DEFAULT = "csi-cephfs-sc";
	public static final String ACCESS_MODE_DEFAULT = "ReadWriteMany";
	private List<String> accessModes = null;
	private String storageSize = null;
	private String storageClassName = null;
	
	public List<String> getAccessModes() {
		return accessModes;
	}
	public void setAccessModes(List<String> accessModes) {
		this.accessModes = accessModes;
	}
	public String getStorageSize() {
		return storageSize;
	}
	public void setStorageSize(String storageSize) {
		this.storageSize = storageSize;
	}
	public String getStorageClassName() {
		return storageClassName;
	}
	public void setStorageClassName(String storageClassName) {
		this.storageClassName = storageClassName;
	}
	
	public RegistryPVC addAccessModesItem(String accessMode) {
		if (this.accessModes == null) {
			this.accessModes = new ArrayList<String>();
		}
		this.accessModes.add(accessMode);
		return this;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class RegistryPVC {\n");
		if(accessModes != null ) 
			for( String accessMode : accessModes)
				sb.append("    accessMode: ").append(toIndentedString(accessMode)).append("\n");
		sb.append("    volumeSize: ").append(toIndentedString(storageSize)).append("\n");
		sb.append("    storageClassName: ").append(toIndentedString(storageClassName)).append("\n");
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
