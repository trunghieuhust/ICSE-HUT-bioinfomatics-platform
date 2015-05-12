package hust.icse.bio.service;

import hust.icse.bio.utils.UUIDGenerator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class WorkflowManagement {
	private static HashMap<UUID, Workflow> workflowManager = new HashMap<UUID, Workflow>();
	private static WorkflowManagement instance = new WorkflowManagement();

	public WorkflowManagement() {
	}

	public static WorkflowManagement getInstance() {
		return instance;
	}

	public String createWorkflow(User user, String workflow) {
		UUID workflowID = UUIDGenerator.nextUUID();
		// TODO validate workflow
		Workflow newWorkflow = new Workflow(user, workflow, workflowID);
		workflowManager.put(workflowID, newWorkflow);
		Thread thread = new Thread(newWorkflow);
		thread.start();
		try {
			thread.join(0);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public Status getStatus(String ID) {
		Workflow workflow = null;
		UUID uuid = UUIDGenerator.UUIDfromString(ID);
		if (workflowManager.containsKey(uuid)) {
			workflow = (Workflow) workflowManager.get(uuid);
			return workflow.getStatus();
		} else {
			Status status = new Status("Not found", ID,
					State.getDescription(State.NOT_FOUND), State.NOT_FOUND);
			System.err.println("Not found. ID: " + ID);
			System.err.println("WF size: " + workflowManager.size());
			Set<UUID> set = workflowManager.keySet();
			for (Iterator iterator = set.iterator(); iterator.hasNext();) {
				UUID uuid2 = (UUID) iterator.next();
				System.err.println(uuid2.toString());
			}
			return status;
		}
	}

	// public Job getTask(String jobID) {
	// Job job = null;
	// UUID uuid = UUIDGenerator.UUIDfromString(jobID);
	// if (workflowManager.containsKey(uuid)) {
	// // job = (Job) workflowManager.get(uuid);
	// }
	// return job;
	//
	// }
}
