package hust.icse.bio.service;

import hust.icse.bio.utils.UUIDGenerator;

import java.util.HashMap;
import java.util.UUID;

public class WorkflowManagement {
	private static HashMap<UUID, Workflow> workflowManager;
	private static WorkflowManagement instance;

	public WorkflowManagement() {
		workflowManager = new HashMap<UUID, Workflow>();
	}

	public static WorkflowManagement getInstance() {
		if (instance == null) {
			instance = new WorkflowManagement();
			return instance;
		} else {
			return instance;
		}
	}

	public String createWorkflow(User user, String workflow) {
		UUID workflowID = UUIDGenerator.nextUUID();
		// TODO validate workflow
		Workflow newWorkflow = new Workflow(user, workflow, workflowID);
		workflowManager.put(workflowID, newWorkflow);
		Thread thread = new Thread(newWorkflow);
		thread.start();
		return workflowID.toString();

	}

	public String getTaskResult(String ID) {
		// Wor job = null;
		// UUID uuid = UUIDGenerator.UUIDfromString(jobID);
		// if (workflowManager.containsKey(uuid)) {
		// job = (Workflow) workflowManager.get(uuid);
		// return job.getResult();
		// } else
		return "Job not found";
	}

	public int getState(String ID) {
		Workflow workflow = null;
		UUID uuid = UUIDGenerator.UUIDfromString(ID);
		if (workflowManager.containsKey(uuid)) {
			workflow = (Workflow) workflowManager.get(uuid);
			return workflow.getStateCode();
		} else {
			return -1;
		}
	}

	public String getFullState(String ID) {
		Workflow workflow = null;
		UUID uuid = UUIDGenerator.UUIDfromString(ID);
		if (workflowManager.containsKey(uuid)) {
			workflow = (Workflow) workflowManager.get(uuid);
			return workflow.getFullState();
		} else {
			return "null";
		}

	}

//	public Job getTask(String jobID) {
//		Job job = null;
//		UUID uuid = UUIDGenerator.UUIDfromString(jobID);
//		if (workflowManager.containsKey(uuid)) {
//			// job = (Job) workflowManager.get(uuid);
//		}
//		return job;
//
//	}
}
