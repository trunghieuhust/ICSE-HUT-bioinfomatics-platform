package hust.icse.bio.workflow;

import hust.icse.bio.service.ActivityStatus;
import hust.icse.bio.service.State;
import hust.icse.bio.service.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Activity {
	private String name;
	private ArrayList<Task> taskList;
	private User user;
	private ActivityStatus status;
	private UUID ID;

	public Activity(String name) {
		this.name = name;
		taskList = new ArrayList<Task>();
		status = new ActivityStatus(name);
	}

	public boolean insertTask(Task task) {
		if (task != null) {
			this.taskList.add(task);
			return true;
		} else {
			return false;
		}
	}

	public void setUser(User user) {
		this.user = user;
		for (Task task : taskList) {
			task.setUser(this.user);
		}
	}

	public void setID(UUID activityID) {
		ID = activityID;
		status.setID(ID.toString());
	}

	public UUID getID() {
		return ID;
	}

	public ActivityStatus getStatus() {
		return status;
	}

	public boolean insertMultipleTask(List<Task> taskList) {
		if (taskList != null && taskList.size() != 0) {
			this.taskList.addAll(taskList);
			for (Task task : taskList) {
				status.addToTaskStatusList(task.getStatus());
			}
			return true;
		} else {
			return false;
		}
	}

	public ArrayList<Task> getTaskList() {
		return taskList;
	}

	public int getNumberOfTask() {
		return taskList.size();
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (taskList.size() != 0) {
			sb.append("Activity Name: " + name + "\n");
			sb.append("Number of task: " + taskList.size() + "\n");
			sb.append("Task list:\n");
			for (Task task : taskList) {
				sb.append(task.toString());
			}
			return sb.toString();
		} else
			return "taskList empty.";
	}

	public void start() {
		status.setStatusCode(State.STILL_BEING_PROCESSED);
		for (Task task : taskList) {
			Thread taskThread = new Thread(task);
			taskThread.start();
			System.err.println("TaskThread: " + taskThread.getName());
		}
		boolean running = true;
		while (running) {
			for (Task task : taskList) {
				running = running
						&& (task.getStatus().getStatusCode() != State.COMPLETE_SUCCESSFULLY);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("All done.");
		status.setStatusCode(State.COMPLETE_SUCCESSFULLY);
	}
}
