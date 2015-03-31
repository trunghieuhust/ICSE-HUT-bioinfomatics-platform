package bio.service;

public class TestJob {

	public String submitJob(String tool, String[] data) {
		String jobID = "";

		jobID = HandlerRequest.getInstance().submit(tool, data);
		return jobID;
	}

	public int getStatus(String jobID) {
		System.out.println(jobID);
		return JobManagement.getInstance().getJobState(jobID);
	}

	public String[] getResult(String jobID) {
		String[] result = { "", "", "" };
		result[0] = getStatus(jobID) + "";
		result[1] = JobManagement.getInstance().getJob(jobID)
				.getStateDescription();
		result[2] = JobManagement.getInstance().getJobResult(jobID);

		return result;
	}
}