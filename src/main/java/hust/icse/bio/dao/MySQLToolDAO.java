package hust.icse.bio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import hust.icse.bio.infrastructure.User;
import hust.icse.bio.tools.Tool;
import hust.icse.bio.utils.UUIDultis;

public class MySQLToolDAO implements ToolDAO {

	@Override
	public boolean addTool(Tool tool, User user) {
		if (getTool(user, tool.getAlias()) != null) {
			// existed
			System.out.println("existed. Not add.");
			return false;
		} else {
			Connection conn = MySQLDAOFactory.createConnection();
			try {
				PreparedStatement preparedStatement = conn
						.prepareStatement("insert into tool values (?,?,?,?,?,?)");
				preparedStatement.setString(1, tool.getAlias());
				preparedStatement.setString(2, tool.getName());
				preparedStatement.setString(3, tool.getVersion());
				preparedStatement.setString(4, tool.getPackageName());
				preparedStatement.setString(5, tool.getCommand());
				preparedStatement
						.setBytes(6, UUIDultis.UUIDtoByteArray(UUIDultis
								.UUIDfromStringWithoutDashes(user.getUserID())));
				boolean result = preparedStatement.execute();
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				MySQLDAOFactory.returnConnection(conn);
			}
		}
		return false;
	}

	@Override
	public Tool getTool(User user, String alias) {
		Connection conn = MySQLDAOFactory.createConnection();
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select * from tool where tool_alias=? AND userID=?");
			preparedStatement.setString(1, alias);
			preparedStatement.setBytes(2, UUIDultis.UUIDtoByteArray(UUIDultis
					.UUIDfromStringWithoutDashes(user.getUserID())));
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();
			if (resultSet.next()) {
				Tool tool = new Tool();
				tool.setAlias(alias);
				tool.setCommand(resultSet.getString("command"));
				tool.setName(resultSet.getString("name"));
				tool.setPackageName(resultSet.getString("package_name"));
				tool.setVersion(resultSet.getString("version"));
				return tool;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			MySQLDAOFactory.returnConnection(conn);
		}
		return null;
	}

	@Override
	public boolean updateTool(Tool tool, User user) {
		if (getTool(user, tool.getAlias()) != null) {
			// existed, then UPDATE
			Connection conn = MySQLDAOFactory.createConnection();
			try {

				PreparedStatement preparedStatement = conn
						.prepareStatement("UPDATE tool WHERE tool_alias=? AND userID=?");
				preparedStatement.setString(1, tool.getAlias());
				preparedStatement
						.setBytes(2, UUIDultis.UUIDtoByteArray(UUIDultis
								.UUIDfromStringWithoutDashes(user.getUserID())));
				boolean result = preparedStatement.execute();
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				MySQLDAOFactory.returnConnection(conn);
			}

		}
		return false;
	}

	@Override
	public boolean deleteTool(Tool tool, User user) {
		Connection conn = MySQLDAOFactory.createConnection();
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("DELETE FROM tool WHERE tool_alias=? AND userID=?");
			preparedStatement.setString(1, tool.getAlias());
			preparedStatement.setBytes(2, UUIDultis.UUIDtoByteArray(UUIDultis
					.UUIDfromStringWithoutDashes(user.getUserID())));
			boolean result = preparedStatement.execute();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			MySQLDAOFactory.returnConnection(conn);
		}

		return false;
	}

}
