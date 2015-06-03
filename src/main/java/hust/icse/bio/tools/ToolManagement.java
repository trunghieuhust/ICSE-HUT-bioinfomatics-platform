package hust.icse.bio.tools;

import java.io.File;

import hust.icse.bio.infrastructure.VM;

public class ToolManagement {
	public Tool getTool(String alias) {
		Tool tool = new Tool();

		return tool;
	}

	public boolean isValidTool() {

		return false;
	}

	public String getImage(Tool tool) {

		return null;
	}

	public boolean deployToVM(Tool tool, VM vm) {
		return false;
	}

	public boolean addToolPackage(File toolPackage) {
		return false;
	}
}
