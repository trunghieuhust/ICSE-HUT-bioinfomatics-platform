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

	// public String getState(User user, String ID) {
	//
	// }
}
