package hust.icse.bio.dao;

import hust.icse.bio.infrastructure.User;
import hust.icse.bio.tools.Tool;

public interface ToolDAO {
	public boolean addTool(Tool tool, User user);

	public Tool getTool(User user, String alias);

	public boolean updateTool(Tool tool, User user);

	public boolean deleteTool(Tool tool, User user);
}
