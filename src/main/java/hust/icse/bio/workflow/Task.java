package hust.icse.bio.workflow;

import hust.icse.bio.dao.DAOFactory;
import hust.icse.bio.infrastructure.User;
import hust.icse.bio.infrastructure.VM;
import hust.icse.bio.service.State;
import hust.icse.bio.service.TaskResult;
import hust.icse.bio.service.TaskStatus;
import hust.icse.bio.service.WorkflowManagement;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.jclouds.openstack.glance.v1_0.domain.Image.Status;

public class Task implements Runnable {
	private String toolAlias;
	private String inputFile;
	private String outputFile;
	private TaskResult result;
	private String name;
	private TaskStatus status;
	private UUID taskID;
	private UUID workflowID;
	private UUID activityID;
	private VM vm;
	private static String image = "cloud-bio-v2";
	private static String flavor = "m1.small";
	private User user;
	private Tool tool;
	private Date created_at;
	private Date finished_at;
	private long duration;
	private static final int MAX_RETRY = 50;

	// TODO input output nhieu file.
	public Task() {
		result = new TaskResult();
		status = new TaskStatus();
	}

	public String getToolAlias() {
		return toolAlias;
	}

	public String getInputFile() {
		return inputFile;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public Date getFinished_at() {
		return finished_at;
	}

	public long getDuration() {
		return duration;
	}

	public String getID() {
		return taskID.toString();
	}

	public String outputFile() {
		return outputFile;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public boolean updateStatus(int statusCode) {
		return status.updateStatus(statusCode);
	}

	public void setAlias(String alias) {
		this.toolAlias = alias;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setWorkflowID(UUID workflowID) {
		this.workflowID = workflowID;
	}

	public void setActivityID(UUID activityID) {
		this.activityID = activityID;
	}

	public UUID getActivityID() {
		return activityID;
	}

	public UUID getWorkflowID() {
		return workflowID;
	}

	public String getName() {
		return name;
	}

	public Tool getTool() {
		return tool;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setID(UUID taskID) {
		this.taskID = taskID;
	}

	public void setTool(Tool tool) {
		this.tool = tool;
	}

	public TaskResult getResult() {
		return result;
	}

	@Override
	public String toString() {
		return "Task:" + "\n\t Name: " + name + "\n\t Alias: " + toolAlias
				+ "\n\t Input: " + inputFile + "\n\tOutput: " + outputFile;
		// return "\n\tTask : " + toolAlias + "\n\tTaskID: " +
		// taskID.toString();
	}

	@Override
	public void run() {
		created_at = Calendar.getInstance().getTime();
		for (int i = 0; i < MAX_RETRY; i++) {
			vm = user.getManager().launchInstance(taskID.toString(), image,
					flavor);
			if (vm == null) {
				status.updateStatus(State.QUEUEING);
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				break;
			}
		}
		status.updateStatus(State.STILL_BEING_PROCESSED);
		vm.executeCommand(getCommand());
		result.setOutputConsole(user.getStorageManagement().getFileLink(
				name + "_output_console.txt", workflowID.toString()));
		result.setOutputFile(user.getStorageManagement().getFileLink(
				outputFile, workflowID.toString()));
		user.getManager().terminateInstance(vm);
		status.updateStatus(State.COMPLETE_SUCCESSFULLY);
		WorkflowManagement.getInstance().addTaskResult(result, taskID);
		finished_at = Calendar.getInstance().getTime();
		result.setDurationTime(vm.getUpTime());
		duration = vm.getUpTime();
		DAOFactory.getDAOFactory(DAOFactory.MYSQL).getTaskDAO()
				.insertTask(this);
		System.out.println("VM terminated");
	}

	public String getCommand() {
		String folder = getSwiftFolder();
		String command = tool.getName() + " " + tool.getCommand();
		command = command.replace("$output", folder + outputFile);
		if (user.getStorageManagement().getFileLink(inputFile,
				user.getStorageManagement().getUploadContainer()) == null) {
			command = command.replace("$input", folder + inputFile);
		} else {
			command = command.replace("$input", user.getStorageManagement()
					.getUploadFolder() + "/" + inputFile);
		}
		command = command + " &> " + folder + name + "_output_console.txt";
		System.out.println(command);

		return command;
	}

	private String getSwiftFolder() {
		String swiftFolder = "swift-folder/";
		String folder = swiftFolder + workflowID.toString() + "/";
		return folder;
	}

}
