package hust.icse.bio.dao;

import hust.icse.bio.infrastructure.User;
import hust.icse.bio.tools.Package;
import hust.icse.bio.utils.UUIDultis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MySQLPackageDAO implements PackageDAO {

	@Override
	public boolean addPackage(User user, Package toolPackage) {
		boolean result = false;
		if (getPackage(user, toolPackage.getPackageName(),
				toolPackage.getVersion()) != null) {
			System.out.println("Package " + toolPackage.getPackageName()
					+ " exist.");
			result = false;
		} else {
			Connection conn = MySQLDAOFactory.createConnection();
			try {
				PreparedStatement preparedStatement = conn
						.prepareStatement("INSERT INTO package(package_name, version, provider, architecture, installed_size, flavor,userID, is_main_repo) VALUES (?,?,?,?,?,?,?,?)");
				preparedStatement.setString(1, toolPackage.getPackageName());
				preparedStatement.setString(2, toolPackage.getVersion());
				preparedStatement.setString(3, toolPackage.getProviders());
				preparedStatement.setString(4, toolPackage.getArchitecture());
				preparedStatement.setLong(5, toolPackage.getInstalledSize());
				preparedStatement.setString(6, toolPackage.getFlavor());
				preparedStatement
						.setBytes(7, UUIDultis.UUIDtoByteArray(UUIDultis
								.UUIDfromStringWithoutDashes(user.getUserID())));
				if (user.isAdmin()) {
					preparedStatement.setInt(8, 1);
				} else {
					preparedStatement.setInt(8, 0);
				}
				result = preparedStatement.execute();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				MySQLDAOFactory.returnConnection(conn);
			}
		}
		return result;
	}

	@Override
	public Package getPackage(User user, String packageName, String version) {
		Connection conn = MySQLDAOFactory.createConnection();
		Package pack = null;
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("SELECT * FROM package WHERE package_name=? AND version=? AND userID=?");
			preparedStatement.setString(1, packageName);
			preparedStatement.setString(2, version);
			preparedStatement.setBytes(3, UUIDultis.UUIDtoByteArray(UUIDultis
					.UUIDfromStringWithoutDashes(user.getUserID())));
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();
			if (resultSet.next()) {
				pack = new Package();
				pack.setArchitecture(resultSet.getString("architecture"));
				pack.setFlavor(resultSet.getString("flavor"));
				pack.setInstalledSize(resultSet.getLong("installed_size"));
				pack.setPackageName(resultSet.getString("package_name"));
				pack.setProviders(resultSet.getString("provider"));
				pack.setUserID(UUIDultis.UUIDFromByteArray(
						resultSet.getBytes("userID")).toString());
				pack.setVersion(resultSet.getString("version"));

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			MySQLDAOFactory.returnConnection(conn);
		}
		return pack;
	}

	@Override
	public boolean deletePackage(User user, String packageName, String version) {
		// TODO Auto-generated method stub
		return false;
	}

}
