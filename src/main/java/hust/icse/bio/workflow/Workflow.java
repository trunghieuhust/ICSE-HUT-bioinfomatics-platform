package hust.icse.bio.workflow;

import hust.icse.bio.dao.DAOFactory;
import hust.icse.bio.dao.MySQLDAOFactory;
import hust.icse.bio.dao.WorkflowDAO;
import hust.icse.bio.infrastructure.User;
import hust.icse.bio.infrastructure.UserManagement;
import hust.icse.bio.service.State;
import hust.icse.bio.service.Status;
import hust.icse.bio.tools.Tool;
import hust.icse.bio.utils.UUIDultis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import com.sun.jna.platform.win32.WinDef.WPARAM;

public class Workflow implements Runnable {
	private String rawWorkflow;
	private ArrayList<Activity> activityList;
	private HashMap<UUID, Activity> activityManager;
	private HashMap<UUID, Task> taskManager;
	private UUID workflowID;
	private User user;
	private Status status;
	private String name;
	private Date createdTime;
	private Date finishedTime;
	private WorkflowInfo workflowInfo;
	private final static String TEST = "<workflow name='2step' save-as-template='true'><activities><activity name='aligment'><task name='clustal1'><tool-alias>clustal</tool-alias><input-files input='actin'></input-files><output-files output='output1'></output-files><flavor>small</flavor></task><task name='clustal2'><tool-alias>clustalo2</tool-alias><input-files input='actin'></input-files><output-files output='output2'></output-files></task></activity><activity name='fasttree'><task name='fasttree'><tool-alias>fasttree</tool-alias><input-files input='output1'></input-files><output-files output='output-fasttree'></output-files><flavor>small</flavor></task></activity></activities></workflow><tools><tool><alias>clustal</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command='--infile=$input --outfile=$output -v'></execute></tool><tool><alias>clustalo2</alias><name>clustalo</name><version>1.2.1</version><package>clustalo</package><execute command='--infile=$input --outfile=$output --outfmt=clustal -v'></execute></tool><tool><alias>fasttree</alias><name>fasttree</name><version>2.1</version><package>fasttree</package><execute command='$input > $output'></execute></tool></tools>";
	private WorkflowDAO workflowDAO;

	public Workflow(User user, String workflow, UUID workflowID) {
		// TODO validate workflow
		this.rawWorkflow = workflow;
		this.workflowID = workflowID;
		this.user = user;
		this.status = new Status(name, workflowID.toString(),
				State.getDescription(State.QUEUEING), State.QUEUEING);
		workflowInfo = WorkFlowUtils.getInstance().parse(workflow);
		this.name = workflowInfo.getName();
		workflowDAO = DAOFactory.getDAOFactory(DAOFactory.MYSQL)
				.getWorkflowDAO();
		activityList = workflowInfo.getActivityList();
		activityManager = new HashMap<UUID, Activity>();
		taskManager = new HashMap<UUID, Task>();

		assignID();
		assignTool();
	}

	private void assignTool() {
		ArrayList<Tool> tools = workflowInfo.getToolList();
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
			UUID activityID = UUIDultis.nextUUID();
			activity.setUser(user);
			activity.setID(activityID);
			activityManager.put(activityID, activity);
			ArrayList<Task> taskList = activity.getTaskList();
			status.addToActivityStatusList(activity.getStatus());
			for (Task task : taskList) {
				UUID taskID = UUIDultis.nextUUID();
				taskManager.put(taskID, task);
				task.setID(taskID);
				task.setActivityID(activityID);
				task.setWorkflowID(workflowID);
				task.getStatus().setID(task.getID());
				task.getStatus().setName(task.getName());
			}
		}

	}

	@Override
	public void run() {
		if (workflowInfo.isSaveAsTemplate()) {
			this.rawWorkflow = WorkFlowUtils.getInstance().convertToTemplate(
					this.rawWorkflow);
			workflowDAO.insertTemplate(this);
		}
		createdTime = Calendar.getInstance().getTime();
		status.setStatusCode(State.STILL_BEING_PROCESSED);
		user.getStorageManagement().createContainer(workflowID.toString());
		for (Activity activity : activityList) {
			System.out.println("Activity " + activity.getName()
					+ "\nActivityID: "
					+ activityManager.containsValue(activity));
			activity.start();
		}
		System.out.println("Activity done.");
		finishedTime = Calendar.getInstance().getTime();
		System.err.println(createdTime.toString());
		System.err.println(finishedTime.toString());
		workflowDAO.insertWorkflow(this);
		status.setStatusCode(State.COMPLETE_SUCCESSFULLY);
	}

	public int getStatusCode() {
		return status.getStatusCode();
	}

	public Status getStatus() {
		return status;
	}

	public User getUser() {
		return user;
	}

	public String getRawWorkflow() {
		return rawWorkflow;
	}

	public String getName() {
		return name;
	}

	public UUID getID() {
		return workflowID;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public Date getFinishedTime() {
		return finishedTime;
	}

	// public String getFullState() {
	// StringBuilder sb = new StringBuilder();
	// for (Activity activity : activityList) {
	// sb.append(activity.toString());
	// }
	// return sb.toString();
	// }

	public static void main(String[] args) {
		// User user = UserManagement.getInstance().authenticate("ducdmk55",
		// "ducdmk55@123");
		User user = null;
		Workflow wf = new Workflow(user, TEST, UUIDultis.nextUUID());
		Thread thread = new Thread(wf);
		// thread.start();
	}
}
