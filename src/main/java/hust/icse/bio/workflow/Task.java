package hust.icse.bio.workflow;

import hust.icse.bio.dao.DAOFactory;
import hust.icse.bio.infrastructure.User;
import hust.icse.bio.infrastructure.VM;
import hust.icse.bio.service.State;
import hust.icse.bio.service.TaskResult;
import hust.icse.bio.service.TaskStatus;
import hust.icse.bio.tools.Tool;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jclouds.openstack.glance.v1_0.domain.Image.Status;

public class Task implements Runnable {
	private String toolAlias;
	private String[] input;
	private String[] inputValue;
	private String[] output;
	private String[] outputValue;
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

	public String getInput() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < input.length; i++) {
			sb.append(input[i]);
			if (i != input.length) {
				sb.append(",");
			}
		}
		return sb.toString();
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

	public String[] getOutput() {
		return output;
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

	public void setInput(String[] input) {
		this.input = input;
	}

	public void setOutput(String[] output) {
		this.output = output;
	}

	public void setOutputValue(String[] outputValue) {
		this.outputValue = outputValue;
	}

	public void setInputValue(String[] inputValue) {
		this.inputValue = inputValue;
	}

	public String[] getInputValue() {
		return inputValue;
	}

	public String[] getOutputValue() {
		return outputValue;
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
		StringBuilder sb = new StringBuilder();
		sb.append("Task:" + "\n\t Name: " + name + "\n\t Alias: " + toolAlias
				+ "\n\t Input: ");
		for (int i = 0; i < input.length; i++) {
			sb.append("\n\t\t" + input[i] + ": " + inputValue[i]);
		}
		sb.append("\n\tOutput: ");
		for (int i = 0; i < output.length; i++) {
			sb.append("\n\t\t" + output[i] + ": " + outputValue[i]);
		}

		return sb.toString();
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
		List<String> outputLink = new ArrayList<String>();
		for (int i = 0; i < output.length; i++) {
			outputLink.add(user.getStorageManagement().getFileLink(output[i],
					workflowID.toString()));
		}
		result.setOutputFile(outputLink.toArray(new String[outputLink.size()]));
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
		for (int i = 0; i < input.length; i++) {
			if (user.getStorageManagement().getFileLink(inputValue[i],
					user.getStorageManagement().getUploadContainer()) == null) {
				// input file from previous activity
				command = command.replace("$" + input[i], folder
						+ inputValue[i]);
			} else {
				// input file from upload folder
				command = command.replace("$input", user.getStorageManagement()
						.getUploadFolder() + "/" + inputValue[i]);
			}
		}
		for (int i = 0; i < output.length; i++) {
			command = command.replace("$" + output[i], folder + outputValue[i]);
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

	public static void main(String[] args) {
		Task task = new Task();
		String[] input = { "in1", "in2" };
		String[] inputValue = { "input1", "input2" };
		String[] output = { "o1", "o2" };
		String[] outputValue = { "output1", "output2" };
		Tool tool = new Tool("alias", "name", "1.0", "package",
				"-i={$in1,$in2} -o={$o1,$o2}");
		task.setUser(new User("ducdmk55", "ducdmk55@123"));
		task.setTool(tool);
		task.setInput(input);
		task.setOutput(output);
		task.setOutputValue(outputValue);
		task.setInputValue(inputValue);
		task.setWorkflowID(UUID.randomUUID());
		task.getCommand();
	}
}
