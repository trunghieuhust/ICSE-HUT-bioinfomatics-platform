package hust.icse.bio.service;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class TaskResult {
	private String outputConsole;
	private String[] outputFile;
	private long durationTime;

	public TaskResult() {
		outputConsole = "";
	}

	@XmlElement
	public String getOutputConsole() {
		return outputConsole;
	}

	@XmlElement
	public String[] getOutputFile() {
		return outputFile;
	}

	public long getDurationTime() {
		return durationTime;
	}

	public void setDurationTime(long durationTime) {
		this.durationTime = durationTime;
	}

	public boolean setOutputConsole(String OutputConsole) {
		this.outputConsole = OutputConsole;
		return true;
	}

	public void setOutputFile(String[] outputFile) {
		this.outputFile = outputFile;
	}
}
