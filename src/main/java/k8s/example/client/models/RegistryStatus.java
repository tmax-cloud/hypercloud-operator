package k8s.example.client.models;

import java.util.ArrayList;
import java.util.List;

public class RegistryStatus {
	public static final String REGISTRY_PHASE_CREATING = "Creating";
	public static final String REGISTRY_PHASE_RUNNING = "Running";
	public static final String REGISTRY_PHASE_NOT_READY = "NotReady";
	public static final String REGISTRY_PHASE_ERROR = "Error";

	private List<RegistryCondition> conditions = null;
	private String phase = null;

	public List<RegistryCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<RegistryCondition> conditions) {
		this.conditions = conditions;
	}

	public RegistryStatus conditions(List<RegistryCondition> conditions) {
		this.conditions = conditions;
		return this;
	}

	public RegistryStatus addConditionsItem(RegistryCondition conditionsItem) {
		if (this.conditions == null) {
			this.conditions = new ArrayList<RegistryCondition>();
		}
		this.conditions.add(conditionsItem);
		return this;
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class RegistryStatus {\n");
		if(conditions != null ) 
			for( RegistryCondition condition : conditions)
				sb.append("    conditions: ").append(toIndentedString(condition)).append("\n");
		sb.append("    phase: ").append(toIndentedString(phase)).append("\n");
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
