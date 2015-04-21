package hust.icse.bio.service;

public class TestJob {

	public String submitJob(String username, String password, String workflow) {
		String jobID = "";
		User user = UserManagement.getInstance().login(username, password);
		if (user == null)
			return "Invalid user name or password.";
		else {
			jobID = HandlerRequest.getInstance().submit(user, workflow);
			return jobID;
		}
	}

	public String getStatus(String ID, String username, String password) {
		System.out.println(ID);
		return WorkflowManagement.getInstance().getFullState(ID);
	}

	public String[] getResult(String jobID, String username, String password) {
		String[] result = { "", "", "" };
		// result[0] = getStatus(jobID, username, password) + "";
		// result[1] = WorkflowManagement.getInstance().getJob(jobID)
		// .getStateDescription();
		// result[2] = WorkflowManagement.getInstance().getTaskResult(jobID);
		System.out.println(result[2]);
		// Result res = new Result();
		// res.Status = getStatus(jobID) + "";
		// res.Description = JobManagement.getInstance().getJob(jobID)
		// .getStateDescription();
		// res.Result = JobManagement.getInstance().getJobResult(jobID);
		// HashMap<String, String> hash = new HashMap<String, String>();
		// hash.put("Status", result[0]);
		// hash.put("desc", result[1]);
		return result;
	}

	public String submitWorkflow(String workflow) {

		return workflow;

	}
}