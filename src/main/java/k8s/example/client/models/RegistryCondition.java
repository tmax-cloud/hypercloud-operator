package k8s.example.client.models;

import org.joda.time.DateTime;

public class RegistryCondition {
	private DateTime lastProbeTime;
	private DateTime lastTransitionTime;
	private String message;
	private String reason;
	private String status;
	private String type;

	public DateTime getLastProbeTime() {
		return lastProbeTime;
	}
	public void setLastProbeTime(DateTime lastProbeTime) {
		this.lastProbeTime = lastProbeTime;
	}
	public DateTime getLastTransitionTime() {
		return lastTransitionTime;
	}
	public void setLastTransitionTime(DateTime lastTransitionTime) {
		this.lastTransitionTime = lastTransitionTime;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class V1PodCondition {\n");
		sb.append("    lastProbeTime: ").append(toIndentedString(lastProbeTime)).append("\n");
		sb.append("    lastTransitionTime: ").append(toIndentedString(lastTransitionTime)).append("\n");
		sb.append("    message: ").append(toIndentedString(message)).append("\n");
		sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
