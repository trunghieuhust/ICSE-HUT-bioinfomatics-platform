package hust.icse.bio.dao;

import hust.icse.bio.utils.UUIDultis;
import hust.icse.bio.workflow.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLTaskDAO implements TaskDAO {

	@Override
	public boolean insertTask(Task task) {

		try {
			Connection conn = MySQLDAOFactory.createConnection();
			PreparedStatement preparedStatement = conn
					.prepareStatement("insert into task values (?,?,?,?,?,?,?,?,?,?)");
			preparedStatement.setBytes(1,
					UUIDultis.UUIDtoByteArray(task.getID()));
			preparedStatement.setBytes(2,
					UUIDultis.UUIDtoByteArray(task.getActivityID()));
			preparedStatement.setBytes(3,
					UUIDultis.UUIDtoByteArray(task.getWorkflowID()));
			preparedStatement.setString(4, task.getName());
			preparedStatement.setInt(5, task.getStatus().getStatusCode());
			preparedStatement.setString(6, task.getInput());
			preparedStatement.setString(7, task.getToolAlias());
			preparedStatement.setTimestamp(8, new java.sql.Timestamp(task.getCreated_at()
					.getTime()));
			preparedStatement.setLong(9, task.getDuration());
			preparedStatement.setTimestamp(10, new java.sql.Timestamp(task
					.getFinished_at().getTime()));
			preparedStatement.executeUpdate();
			MySQLDAOFactory.returnConnection(conn);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Task findTask(String ID) {
		// TODO Auto-generated method stub
		return null;
	}

}
