package hust.icse.bio.service;

public class TaskResult {
	private String outputConsole;
	private String outputURL[];

	public TaskResult() {
		outputConsole = "";
	}

	public String getOutputConsole() {
		return outputConsole;
	}

	public String[] getOutputFile() {
		return outputURL;
	}

	public boolean setOutputConsole(String OutputConsole) {
		this.outputConsole = OutputConsole;
		return true;
	}
}
