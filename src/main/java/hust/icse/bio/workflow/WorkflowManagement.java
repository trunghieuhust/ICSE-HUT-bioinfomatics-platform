package hust.icse.bio.workflow;

import hust.icse.bio.dao.DAOFactory;
import hust.icse.bio.dao.WorkflowDAO;
import hust.icse.bio.infrastructure.User;
import hust.icse.bio.service.State;
import hust.icse.bio.service.Status;
import hust.icse.bio.service.TaskResult;
import hust.icse.bio.utils.UUIDultis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class WorkflowManagement {
	private static HashMap<UUID, Workflow> workflowManager = new HashMap<UUID, Workflow>();
	private static HashMap<UUID, TaskResult> resultSet = new HashMap<UUID, TaskResult>();
	private static ArrayList<UUID> resultSetIndex = new ArrayList<UUID>();
	private static WorkflowManagement instance = new WorkflowManagement();

	private static int MAX_RESULT_SET_SIZE = 20;

	public WorkflowManagement() {
	}

	public static WorkflowManagement getInstance() {
		return instance;
	}

	public String createWorkflow(User user, String workflow) {
		UUID workflowID = UUIDultis.nextUUID();
		// TODO validate workflow
		Workflow newWorkflow = new Workflow(user, workflow, workflowID);
		workflowManager.put(workflowID, newWorkflow);
		Thread thread = new Thread(newWorkflow);
		thread.start();
		return workflowID.toString();
	}

	public String createWorkflowFromTemplate(User user, String workflowName) {
		WorkflowDAO workflowDAO = DAOFactory.getDAOFactory(DAOFactory.MYSQL)
				.getWorkflowDAO();
		String rawXML = workflowDAO.getTemplate(workflowName, user.getUserID());
		if (rawXML != null) {
			return createWorkflow(user, rawXML);
		} else {
			return null;
		}
	}

	public TaskResult getTaskResult(String ID) {
		UUID taskID = UUIDultis.UUIDfromString(ID);
		if (resultSet.containsKey(taskID)) {
			TaskResult result = resultSet.get(taskID);
			return result;
		} else {
			return null;
		}
	}

	public Status getStatus(String ID) {
		Workflow workflow = null;
		UUID uuid = UUIDultis.UUIDfromString(ID);
		if (workflowManager.containsKey(uuid)) {
			workflow = (Workflow) workflowManager.get(uuid);
			return workflow.getStatus();
		} else {
			Status status = new Status("Not found", ID,
					State.getDescription(State.NOT_FOUND), State.NOT_FOUND);
			System.err.println("Not found. ID: " + ID);
			System.err.println("WF size: " + workflowManager.size());
			Set<UUID> set = workflowManager.keySet();
			for (Iterator<UUID> iterator = set.iterator(); iterator.hasNext();) {
				UUID uuid2 = (UUID) iterator.next();
				System.err.println(uuid2.toString());
			}
			return status;
		}
	}

	public void addTaskResult(TaskResult result, UUID taskID) {
		if (result != null && taskID != null) {
			resultSetIndex.add(taskID);
			resultSet.put(taskID, result);
			if (resultSetIndex.size() > MAX_RESULT_SET_SIZE) {
				// TODO: Save to db and clear
				UUID firstResultID = resultSetIndex.remove(0);
				resultSet.remove(firstResultID);
			}
		}
	}
}
