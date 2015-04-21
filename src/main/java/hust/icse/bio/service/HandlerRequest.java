package hust.icse.bio.service;

public class HandlerRequest {
	private static HandlerRequest instance;

	public static HandlerRequest getInstance() {
		if (instance == null) {
			instance = new HandlerRequest();
			return instance;
		} else {
			return instance;
		}
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
