package bio.service;

public class TestJob {

	public String submitJob(String tool, String[] data, String username,
			String password) {
		String jobID = "";
		User user = UserManagement.getInstance().login(username, password);
		if (user == null)
			return "Invalid user name or password.";
		else {
			jobID = HandlerRequest.getInstance().submit(user, tool, data);
			return jobID;
		}
	}

	public int getStatus(String jobID, String username, String password) {
		System.out.println(jobID);
		return JobManagement.getInstance().getJobState(jobID);
	}

	public String[] getResult(String jobID, String username, String password) {
		String[] result = { "", "", "" };
		result[0] = getStatus(jobID, username, password) + "";
		result[1] = JobManagement.getInstance().getJob(jobID)
				.getStateDescription();
		result[2] = JobManagement.getInstance().getJobResult(jobID);
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
}