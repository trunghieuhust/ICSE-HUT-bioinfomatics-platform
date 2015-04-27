package hust.icse.bio.service;

import hust.icse.bio.utils.UUIDGenerator;
import hust.icse.bio.workflow.Activity;
import hust.icse.bio.workflow.Task;
import hust.icse.bio.workflow.Tool;
import hust.icse.bio.workflow.WorkFlowUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Workflow implements Runnable {
	private String rawWorkflow;
	private ArrayList<Activity> activityList;
	private HashMap<UUID, Activity> activityManager;
	private HashMap<UUID, Task> taskManager;
	private UUID workflowID;
	private User user;
	private State state;

	private final static String TEST = "<workflow><activities><activity name='aligment'><task><tool-alias>clustal</tool-alias><input-files input='input'></input-files><output-files output='output'></output-files></task><task><tool-alias>clustalo2</tool-alias><input-files input='input'></input-files><output-files output='output3'></output-files></task><task><tool-alias>clustalo3</tool-alias><input-files input='input'></input-files><output-files output='output4'></output-files></task></activity><activity name='fasttree'><task><tool-alias>fasttree</tool-alias><input-files input='output'></input-files><output-files output='output-fasttree'></output-files></task></activity></activities></workflow><tools><tool><alias>clustal</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command='--infile=$input --outfile=$output -v'></execute></tool><tool><alias>clustalo2</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command='--infile=$input --outfile=$output --outfmt=clustal -v'></execute></tool><tool><alias>fasttree</alias><name>fasttree</name><version>2.1</version><package>fasttree</package><execute command='$input > $output'></execute></tool></tools>";

	public Workflow(User user, String workflow, UUID workflowID) {
		// TODO validate workflow
		this.rawWorkflow = workflow;
		this.workflowID = workflowID;
		this.user = user;
		activityList = WorkFlowUtils.getInstance().parse(workflow);
		activityManager = new HashMap<UUID, Activity>();
		taskManager = new HashMap<UUID, Task>();
		state = new State(State.UNKNOWN_STATE);
		assignID();
		assignTool();
	}

	private void assignTool() {
		ArrayList<Tool> tools = WorkFlowUtils.getInstance().parseTools(
				rawWorkflow);
		for (Activity activity : activityList) {
			ArrayList<Task> tasks = activity.getTaskList();
			for (Task task : tasks) {
				for (Tool tool : tools) {
					if (task.getToolAlias().equals(tool.getAlias())) {
						task.setTool(tool);
						break;
					}
				}
			}
		}
	}

	private void assignID() {
		for (Activity activity : activityList) {
			UUID activityID = UUIDGenerator.nextUUID();
			activity.setUser(user);
			activity.updateState(State.QUEUEING);
			activity.setID(activityID);
			activityManager.put(activityID, activity);
			ArrayList<Task> taskList = activity.getTaskList();
			for (Task task : taskList) {
				UUID taskID = UUIDGenerator.nextUUID();
				taskManager.put(taskID, task);
				task.setID(taskID);
				task.setWorkflowID(workflowID);
			}
		}
	}

	@Override
	public void run() {
		state.updateState(State.STILL_BEING_PROCESSED);
		user.getStorageManagement().createContainer(workflowID.toString());
		for (Task task : activityList.get(0).getTaskList()) {
			user.getStorageManagement().copyFileToOtherContainer(
					task.getInputFile(),
					user.getStorageManagement().getUploadContainer(),
					workflowID.toString());
		}
		for (Activity activity : activityList) {
			System.out.println("Activity " + activity.getName()
					+ "\nActivityID: "
					+ activityManager.containsValue(activity));
			activity.start();
		}
		System.out.println("Activity done.");
		state.updateState(State.COMPLETE_SUCCESSFULLY);
	}

	public int getStateCode() {
		return state.getState();
	}

	public String getFullState() {
		StringBuilder sb = new StringBuilder();
		for (Activity activity : activityList) {
			sb.append(activity.toString());
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		User user = UserManagement.getInstance().login("ducdmk55",
				"ducdmk55@123");
		Thread thread = new Thread(new Workflow(user, TEST,
				UUIDGenerator.nextUUID()));
		thread.start();
	}

}
