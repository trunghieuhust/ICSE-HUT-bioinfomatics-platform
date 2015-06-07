package hust.icse.bio.dao;

public abstract class DAOFactory {
	// List of DAO types supported by the factory
	public static final int MYSQL = 1;

	public static DAOFactory getDAOFactory(int whichFactory) {
		switch (whichFactory) {
		case 1:
			return new MySQLDAOFactory();
		default:
			return null;
		}
	}

	public abstract WorkflowDAO getWorkflowDAO();

	public abstract ActivityDAO getActivityDAO();

	public abstract TaskDAO getTaskDAO();

	public abstract ToolDAO getToolDAO();

	public abstract ResultDAO getResultDAO();

	public abstract PackageDAO getPackageDAO();
}
