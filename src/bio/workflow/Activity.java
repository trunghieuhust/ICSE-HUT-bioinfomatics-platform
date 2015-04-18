package bio.workflow;

import java.util.ArrayList;
import java.util.List;

public class Activity {
	private String name;
	private ArrayList<Task> taskList;

	public Activity(String name) {
		this.name = name;
		taskList = new ArrayList<Task>();
	}

	public boolean insertTask(Task task) {
		if (task != null) {
			this.taskList.add(task);
			return true;
		} else {
			return false;
		}
	}

	public boolean insertMultipleTask(List<Task> taskList) {
		if (taskList != null && taskList.size() != 0) {
			this.taskList.addAll(taskList);
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

	public void print() {
		if (taskList.size() != 0) {
			System.out.println("Activity Name: " + name);
			System.out.println("Number of task: " + taskList.size());
			for (Task task : taskList) {
				System.out.println(task.toString());
			}

		}else
			System.out.println("taskList empty.");
	}
}
