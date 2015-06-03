package hust.icse.bio.dao;

import hust.icse.bio.utils.UUIDultis;
import hust.icse.bio.workflow.Workflow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

	@Override
	public String getTemplate(String name, String userID) {
		Connection conn = MySQLDAOFactory.createConnection();
		String rawXML = null;
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select rawXML from workflow_template where userID=? AND name=?");
			preparedStatement.setBytes(1, UUIDultis.UUIDtoByteArray(UUIDultis
					.UUIDfromStringWithoutDashes(userID)));
			preparedStatement.setString(2, name);
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();
			if (resultSet.next()) {
				rawXML = resultSet.getString("rawXML");
			}
			System.out.println("rawXML:"+ rawXML);
			MySQLDAOFactory.returnConnection(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rawXML;
	}

	@Override
	public boolean insertTemplate(Workflow workflow) {
		String rawXML = getTemplate(workflow.getName(), workflow.getUser()
				.getUserID());
		boolean result = false;
		Connection conn = MySQLDAOFactory.createConnection();
		// exist == null -> this template not exist in database -> INSERT INTO
		if (rawXML == null) {
			try {
				System.out.println("Template not exist. INSERT INTO");
				PreparedStatement preparedStatement = conn
						.prepareStatement("insert into workflow_template(rawXML, userID, name) values (?,?,?)");
				preparedStatement.setString(1, workflow.getRawWorkflow());
				preparedStatement.setBytes(2, UUIDultis
						.UUIDtoByteArray(UUIDultis
								.UUIDfromStringWithoutDashes(workflow.getUser()
										.getUserID())));
				preparedStatement.setString(3, workflow.getName());
				result = preparedStatement.execute();
				MySQLDAOFactory.returnConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} else {
			// UPDATE existed template.
			System.out.println("Template exist. UPDATE");
			try {
				PreparedStatement preparedStatement = conn
						.prepareStatement("update workflow_template set rawXML=? where name=? AND userID=?");
				preparedStatement.setString(1, workflow.getRawWorkflow());
				preparedStatement.setString(2, workflow.getName());
				preparedStatement.setBytes(3, UUIDultis
						.UUIDtoByteArray(UUIDultis
								.UUIDfromStringWithoutDashes(workflow.getUser()
										.getUserID())));
				result = preparedStatement.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		return result;
	}
}
