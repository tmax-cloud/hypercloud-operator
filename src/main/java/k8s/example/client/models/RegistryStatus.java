package k8s.example.client.models;

public class RegistryStatus {
	public static final String REGISTRY_PHASE_CREATING = "Creating";
	public static final String REGISTRY_PHASE_RUNNING = "Running";
	public static final String REGISTRY_PHASE_FAILED = "Failed";
	
	private String message = null;
	private String phase = null;
	private String reason = null;
//	private DateTime startTime = null;
	
	
	public String getMessage() {
		return message;
	}

	public RegistryStatus() {
		super();
	}
	
	public RegistryStatus(String message, String phase, String reason) {
		super();
		this.message = message;
		this.phase = phase;
		this.reason = reason;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getPhase() {
		return phase;
	}
	public void setPhase(String phase) {
		this.phase = phase;
	}
	
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
//	public DateTime getStartTime() {
//		return startTime;
//	}
//	public void setStartTime(DateTime startTime) {
//		this.startTime = startTime;
//	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class RegistryStatus {\n");
		if(message != null ) sb.append("    message: ").append(toIndentedString(message)).append("\n");
		if(phase != null ) sb.append("    phase: ").append(toIndentedString(phase)).append("\n");
		if(reason != null ) sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
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
