package hust.icse.bio.dao;

import java.sql.Connection;

public class MySQLDAOFactory extends DAOFactory {
	private static MySQLConnectionPoolManager connectionPoolManager = new MySQLConnectionPoolManager();

	public static Connection createConnection() {
		Connection conn = null;
		conn = connectionPoolManager.getConnectionFromPool();
		return conn;
	}

	public static void returnConnection(Connection connection) {
		connectionPoolManager.returnConnectionToPool(connection);
	}

	@Override
	public WorkflowDAO getWorkflowDAO() {
		return new MySQLWorkflowDAO();
	}

	@Override
	public ActivityDAO getActivityDAO() {
		return new MySQLActivityDAO();
	}

	@Override
	public TaskDAO getTaskDAO() {
		return new MySQLTaskDAO();
	}

	@Override
	public ToolDAO getToolDAO() {
		return new MySQLToolDAO();
	}

	@Override
	public ResultDAO getResultDAO() {
		return new MySQLResultDAO();
	}

	@Override
	public PackageDAO getPackageDAO() {
		return new MySQLPackageDAO();
	}

}
