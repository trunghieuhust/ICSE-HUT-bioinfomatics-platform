package hust.icse.bio.service;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "name", "ID", "status", "statusCode",
		"activityStatusList" })
public class Status {
	private String name;
	private String id;
	private String status;
	private int statusCode;
	private List<ActivityStatus> activityStatusList;

	public Status() {
		activityStatusList = new ArrayList<ActivityStatus>();
		name = "";
		id = "";
		statusCode = -1;
		status = "";
	}

	public Status(String name, String ID, String status, int statusCode) {
		activityStatusList = new ArrayList<ActivityStatus>();
		this.name = name;
		this.id = ID;
		this.status = status;
		this.statusCode = statusCode;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setID(String ID) {
		this.id = ID;
	}

	public void setStatusCode(int statusCode) {
		if (statusCode >= 0 && statusCode <= 5) {
			this.statusCode = statusCode;
			this.status = State.getDescription(statusCode);
		}
	}

	@XmlElement
	public String getName() {
		return name;
	}

	@XmlElement
	public String getID() {
		return id;
	}

	@XmlElement
	public String getStatus() {
		return status;
	}

	@XmlElement
	public int getStatusCode() {
		return statusCode;
	}

	@XmlElement
	public List<ActivityStatus> getActivityStatusList() {
		return activityStatusList;
	}

	public void addToActivityStatusList(ActivityStatus activityStatus) {
		activityStatusList.add(activityStatus);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Workflow Status:" + "\n\tName: " + name + "\n\tID:" + id
				+ "\n\tStatus:" + status + "\n\tStatusCode:" + statusCode);
		for (ActivityStatus activityStatus : activityStatusList) {
			sb.append("\n\t" + activityStatus.toString());
		}
		return sb.toString();
	}
}
