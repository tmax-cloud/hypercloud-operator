package k8s.example.client.models;

import org.json.simple.JSONObject;

public class ProvisionInDO {

	private String service_id = null;
	private String plan_id = null;
	private Object context = null;
	private String organization_guid = null;
	private String space_guid = null;
	private Object parameters = null;
	private MaintenanceInfo maintenance_info = null;
	
	public String getService_id() {
		return service_id;
	}
	public void setService_id(String service_id) {
		this.service_id = service_id;
	}
	public String getPlan_id() {
		return plan_id;
	}
	public void setPlan_id(String plan_id) {
		this.plan_id = plan_id;
	}
	public Object getContext() {
		return context;
	}
	public void setContext(Object context) {
		this.context = context;
	}
	public String getOrganization_guid() {
		return organization_guid;
	}
	public void setOrganization_guid(String organization_guid) {
		this.organization_guid = organization_guid;
	}
	public String getSpace_guid() {
		return space_guid;
	}
	public void setSpace_guid(String space_guid) {
		this.space_guid = space_guid;
	}
	public Object getParameters() {
		return parameters;
	}
	public void setParameters(Object parameters) {
		this.parameters = parameters;
	}
	public MaintenanceInfo getMaintenance_info() {
		return maintenance_info;
	}
	public void setMaintenance_info(MaintenanceInfo maintenance_info) {
		this.maintenance_info = maintenance_info;
	}
	@Override
	public String toString() {
		return "ProvisionInDO [service_id=" + service_id + ", plan_id=" + plan_id + ", context=" + context
				+ ", organization_guid=" + organization_guid + ", space_guid=" + space_guid + ", parameters="
				+ parameters + ", maintenance_info=" + maintenance_info + "]";
	}
}
