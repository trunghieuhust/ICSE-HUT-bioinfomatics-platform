package bio.service;

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

	public String submit(String tool, String[] data) {
		String jobID;
		jobID = JobManagement.getInstance().createJob();
		return jobID;
	}
}
