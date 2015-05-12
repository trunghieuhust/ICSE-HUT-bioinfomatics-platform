package hust.icse.bio.service;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class TaskStatus {
	private String name;
	private String ID;
	private String status;
	private int statusCode;

	public TaskStatus() {
		name = "unknown";
		ID = "unkown";
		statusCode = State.QUEUEING;
		status = State.getDescription(statusCode);
	}
@XmlElement
	public String getName() {
		return name;
	}
@XmlElement
	public String getID() {
		return ID;
	}
@XmlElement
	public String getStatus() {
		return status;
	}
@XmlElement
	public int getStatusCode() {
		return statusCode;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public boolean updateStatus(int statusCode) {
		if (statusCode >= 0 && statusCode <= 5) {
			this.statusCode = statusCode;
			this.status = State.getDescription(statusCode);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "\n\t\t\tName: " + name + "\n\t\t\tID: " + ID + "\n\t\t\tstatus: " + status
				+ "\n\t\t\tstatusCode: " + statusCode;
	}
}
