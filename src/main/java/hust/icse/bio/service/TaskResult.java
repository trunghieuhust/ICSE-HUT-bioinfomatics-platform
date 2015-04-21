package hust.icse.bio.service;

public class TaskResult {
	private State state;
	private String result;

	public TaskResult() {
		state = new State();
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
