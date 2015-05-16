package hust.icse.bio.service;

public class HandlerRequest {
	private static HandlerRequest instance = new HandlerRequest();

	public static HandlerRequest getInstance() {
		return instance;
	}

	public String submit(User user, String workflow) {
		String workflowID = null;
		workflowID = WorkflowManagement.getInstance().createWorkflow(user,
				workflow);
		return workflowID;
	}

	public Status getStatus(String ID) {
		Status status = WorkflowManagement.getInstance().getStatus(ID);
		return status;
	}

	public TaskResult getTaskResult(String ID) {
		TaskResult result = WorkflowManagement.getInstance().getTaskResult(ID);
		return result;
	}
}
