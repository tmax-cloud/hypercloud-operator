package k8s.example.client.models;

import org.json.simple.JSONObject;

public class InputParametersSchema {

	private JSONObject parameters = null;

	public JSONObject getParameters() {
		return parameters;
	}

	public void setParameters(JSONObject parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "InputParametersSchema [parameters=" + parameters + "]";
	}
}
