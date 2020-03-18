package k8s.example.client.models;

public class TemplateInstance {
	
	private String apiVersion = null;
	private String kind = null;
	private Metadata metadata = null;
	private TemplateInstanceSpec spec = null;
	private TemplateInstanceStatus status = null;
	
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
	public Object getMeatdata() {
		return metadata;
	}
	public void setMeatdata(Metadata metadata) {
		this.metadata = metadata;
	}
	public TemplateInstanceSpec getSpec() {
		return spec;
	}
	public void setSpec(TemplateInstanceSpec spec) {
		this.spec = spec;
	}
	public TemplateInstanceStatus getStatus() {
		return status;
	}
	public void setStatus(TemplateInstanceStatus status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "TemplateInstance [apiVersion=" + apiVersion + ", kind=" + kind + ", metadata=" + metadata + ", spec="
				+ spec + ", status=" + status + "]";
	}
}
