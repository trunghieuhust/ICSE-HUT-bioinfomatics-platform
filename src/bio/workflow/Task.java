package bio.workflow;

public class Task {
	private String toolAlias;
	private String inputFile;
	private String outputFile;

	public Task() {

	}

	public Task(String toolAlias, String inputFile, String outputFile) {
		this.toolAlias = toolAlias;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	public String getToolAlias() {
		return toolAlias;
	}

	public String getInputFile() {
		return inputFile;
	}

	public String outputFile() {
		return outputFile;
	}

	public void setAlias(String alias) {
		this.toolAlias = alias;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public String toString() {
		return "Task:" + "\n\t Alias: " + toolAlias + "\n\t Input: "
				+ inputFile + "\n\tOutput: " + outputFile;
	}
}
