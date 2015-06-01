package hust.icse.bio.dao;

import java.util.Collection;

import hust.icse.bio.service.Workflow;

public interface WorkflowDAO {
	int insertWorkflow(Workflow workflow);

	boolean deleteWorkflow(String ID);

	Workflow findWorkflow(String ID);

	boolean updateWorkflow(Workflow workflow);

	Collection<Workflow> selectAllWorkflowByUser(String userID);

	String getTemplate(String name, String userID);
	
	boolean insertTemplate(Workflow workflow);
}
