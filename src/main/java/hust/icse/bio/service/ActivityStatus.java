package hust.icse.bio.service;

import java.util.ArrayList;
import java.util.List;
import hust.icse.bio.service.State;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "name", "ID", "status", "statusCode", "taskStatusList" })
public class ActivityStatus {
	private List<TaskStatus> taskStatusList;
	private String name;
	private String ID;
	private String status;
	private int statusCode;

	public ActivityStatus(String name) {
		taskStatusList = new ArrayList<TaskStatus>();
		this.name = name;
		ID = "";
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

	@XmlElement
	public List<TaskStatus> getTaskStatusList() {
		return taskStatusList;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public void setStatusCode(int statusCode) {
		if (statusCode >= 0 && statusCode <= 5) {
			this.statusCode = statusCode;
			this.status = State.getDescription(statusCode);
		}

	}

	public void setTaskStatusList(List<TaskStatus> taskStatusList) {
		this.taskStatusList = taskStatusList;
	}

	public void addToTaskStatusList(TaskStatus taskStatus) {
		taskStatusList.add(taskStatus);
	}

	public void updateStatus() {
		int count = 0;
		for (TaskStatus taskStatus : taskStatusList) {
			if (taskStatus.getStatusCode() == State.STOP_WITH_ERROR) {
				setStatusCode(State.STOP_WITH_ERROR);
				break;
			} else if (taskStatus.getStatusCode() == State.COMPLETE_SUCCESSFULLY) {
				count++;
			}
		}
		if (count == 0) {
			setStatusCode(State.QUEUEING);
		} else {
			setStatusCode(State.COMPLETE_SUCCESSFULLY);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n\tActivity Status:\n\t\tName: " + name + "\n\t\tID: " + ID
				+ "\n\t\tStatus: " + status + "\n\t\tStatus Code: " + statusCode);
		sb.append(" \n\n\t\t Task status:");
		for (TaskStatus taskStatus : taskStatusList) {
			sb.append("\n\t\t" + taskStatus.toString());
		}
		return sb.toString();
	}
}
