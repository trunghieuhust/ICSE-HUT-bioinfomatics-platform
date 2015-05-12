package hust.icse.bio.workflow;

import hust.icse.bio.service.State;
import hust.icse.bio.service.TaskResult;
import hust.icse.bio.service.TaskStatus;
import hust.icse.bio.service.User;
import hust.icse.bio.vm.VM;

import java.util.UUID;

public class Task implements Runnable {
	private String toolAlias;
	private String inputFile;
	private String outputFile;
	private TaskResult result;
	private String name;
	private TaskStatus status;
	private UUID taskID;
	private UUID workflowID;
	private VM vm;
	private static String image = "cloud-bio-v2";
	private static String flavor = "m1.small";
	private User user;
	private Tool tool;

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

	public String getName() {
		return name;
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

	@Override
	public String toString() {
		return "Task:" + "\n\t Name: " + name + "\n\t Alias: " + toolAlias
				+ "\n\t Input: " + inputFile + "\n\tOutput: " + outputFile;
		// return "\n\tTask : " + toolAlias + "\n\tTaskID: " +
		// taskID.toString();
	}

	@Override
	public void run() {
		status.updateStatus(State.STILL_BEING_PROCESSED);
		vm = user.getManager().launchInstance(taskID.toString(), image, flavor);
		System.out.println("VM ready. IP " + vm.getFloatingIP());
		// vm.executeCommand("echo '" + taskID.toString() + "' >> id.txt");
		result.setOutputConsole(vm.executeCommand(getCommand()));
		System.out.println(result.getOutputConsole());
		user.getManager().terminateInstance(vm);
		status.updateStatus(State.COMPLETE_SUCCESSFULLY);
		System.out.println("VM terminated");
	}

	public String getCommand() {
		String swiftFolder = "swift-folder/";
		String folder = swiftFolder + workflowID.toString() + "/";
		String command = tool.getName() + " " + tool.getCommand();
		command = command.replace("$output", folder + outputFile);
		command = command.replace("$input", folder + inputFile);
		System.out.println(command);

		return command;
	}
}
