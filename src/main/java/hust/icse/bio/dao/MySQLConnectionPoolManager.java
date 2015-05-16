package hust.icse.bio.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

public class MySQLConnectionPoolManager {
	private String databaseURL = "";
	private String username = "";
	private String password = "";
	private String driver = "";

	private Vector<Connection> connectionPool = new Vector<Connection>();

	public MySQLConnectionPoolManager() {
		this.databaseURL = MySQLconfig.DEFAULT_DATABASEURL;
		this.username = MySQLconfig.DEFAULT_USER;
		this.password = MySQLconfig.DEFAULT_PASSWORD;
		this.driver = MySQLconfig.DRIVER;
		initialize();
	}

	public MySQLConnectionPoolManager(String databaseURL, String username,
			String password, String driver) {
		this.databaseURL = databaseURL;
		this.username = username;
		this.password = password;
		this.driver = driver;
		initialize();
	}

	private void initialize() {
		initializeConnectionPool();
	}

	private void initializeConnectionPool() {
		while (isConnectionPoolFull() == false) {
			System.out.println("Pool not full. Add new connection to pool.");
			connectionPool.add(createNewConnectionForPool());
		}
	}

	private synchronized boolean isConnectionPoolFull() {

		if (connectionPool.size() < MySQLconfig.MAX_POOL_SIZE) {
			return false;
		} else {
			return true;
		}
	}

	private Connection createNewConnectionForPool() {
		Connection connection = null;

		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(databaseURL, username,
					password);
			System.out.println("New connection: " + connection);
		} catch (SQLException sqle) {
			System.err.println("SQLException: " + sqle);
			return null;
		} catch (ClassNotFoundException cnfe) {
			System.err.println("ClassNotFoundException: " + cnfe);
			return null;
		}
		return connection;
	}

	public synchronized Connection getConnectionFromPool() {
		Connection connection = null;

		if (connectionPool.size() > 0) {
			connection = (Connection) connectionPool.firstElement();
			connectionPool.removeElementAt(0);
		}
		return connection;
	}

	public synchronized void returnConnectionToPool(Connection connection) {
		// Adding the connection from the client back to the connection pool
		try {
			if (connection != null && connection.isClosed() == false) {
				connectionPool.addElement(connection);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
