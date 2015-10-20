package hust.icse.bio.dao;

import hust.icse.bio.workflow.Task;

public interface TaskDAO {
	boolean insertTask(Task task);
	Task findTask(String ID);
}
