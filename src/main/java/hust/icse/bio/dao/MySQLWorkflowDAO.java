package hust.icse.bio.dao;

import hust.icse.bio.service.Workflow;
import hust.icse.bio.utils.UUIDultis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public class MySQLWorkflowDAO implements WorkflowDAO {
	public MySQLWorkflowDAO() {
	}

	@Override
	public int insertWorkflow(Workflow workflow) {
		try {
			Connection conn = MySQLDAOFactory.createConnection();
			PreparedStatement preparedStatement = conn
					.prepareStatement("insert into workflow values (?,?,?,?,?,?,?)");
			preparedStatement.setBytes(1,
					UUIDultis.UUIDtoByteArray(workflow.getID()));
			preparedStatement.setString(2, workflow.getName());
			preparedStatement.setInt(3, workflow.getStatusCode());
			preparedStatement.setBytes(4,
					UUIDultis.UUIDtoByteArray(UUIDultis
							.UUIDfromStringWithoutDashes(workflow.getUser()
									.getUserID())));
			preparedStatement.setString(5, workflow.getRawWorkflow());
			preparedStatement.setTimestamp(6, new java.sql.Timestamp(workflow
					.getCreatedTime().getTime()));
			preparedStatement.setTimestamp(7, new java.sql.Timestamp(workflow
					.getFinishedTime().getTime()));
			preparedStatement.executeUpdate();
			MySQLDAOFactory.returnConnection(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean deleteWorkflow(String ID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Workflow findWorkflow(String ID) {
		return null;
	}

	@Override
	public boolean updateWorkflow(Workflow workflow) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Workflow> selectAllWorkflowByUser(String userID) {
		// TODO Auto-generated method stub
		return null;
	}
}
