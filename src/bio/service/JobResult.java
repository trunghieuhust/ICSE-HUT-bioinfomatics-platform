package bio.service;

public class JobResult {
	private JobState state;
	private String result;

	public JobResult() {
		state = new JobState();
		result = "";
	}

	public int getStateCode() {
		return state.getState();
	}

	public String getStateDescription() {
		return state.getDescription();
	}

	public String getResult() {
		return result;
	}

	public boolean updateState(int stateCode) {
		return state.updateState(stateCode);
	}

	public boolean setResult(String jobResult) {
		this.result = jobResult;
		return true;
	}
}
