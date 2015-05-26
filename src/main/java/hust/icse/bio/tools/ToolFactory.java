package hust.icse.bio.tools;

import hust.icse.bio.infrastructure.VM;

public interface ToolFactory {
	public ToolFactory getToolFactory();

	public VM setupTool();
}
