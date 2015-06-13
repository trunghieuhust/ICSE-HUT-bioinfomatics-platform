package hust.icse.bio.dao;

import hust.icse.bio.infrastructure.User;
import hust.icse.bio.tools.Package;
import hust.icse.bio.utils.UUIDultis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MySQLPackageDAO implements PackageDAO {

	@Override
	public boolean addPackage(User user, Package toolPackage) {
		boolean result = false;
		if (getPackage(user, toolPackage.getPackageName(),
				toolPackage.getVersion()).size() != 0) {
			System.out.println("Package " + toolPackage.getPackageName()
					+ " exist.");
			result = false;
		} else {
			Connection conn = MySQLDAOFactory.createConnection();
			try {
				PreparedStatement preparedStatement = conn
						.prepareStatement("INSERT INTO package(package_name, version, provider, architecture, installed_size, flavor,userID, is_main_repo, depends) VALUES (?,?,?,?,?,?,?,?,?)");
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
				preparedStatement.setString(9, toolPackage.getDepends());
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
	public List<Package> getPackage(User user, String packageName,
			String version) {
		Connection conn = MySQLDAOFactory.createConnection();
		List<Package> result = new ArrayList<Package>();
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("SELECT * FROM package WHERE package_name=? AND version=?");
			preparedStatement.setString(1, packageName);
			preparedStatement.setString(2, version);
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();
			result = new ArrayList<Package>();
			while (resultSet.next()) {
				Package pack = new Package();
				pack.setArchitecture(resultSet.getString("architecture"));
				pack.setFlavor(resultSet.getString("flavor"));
				pack.setInstalledSize(resultSet.getLong("installed_size"));
				pack.setPackageName(resultSet.getString("package_name"));
				pack.setProviders(resultSet.getString("provider"));
				pack.setUserID(UUIDultis.UUIDFromByteArray(
						resultSet.getBytes("userID")).toString());
				pack.setVersion(resultSet.getString("version"));
				pack.setDepends(resultSet.getString("depends"));
				if (resultSet.getInt("is_main_repo") == 0) {
					pack.setMainRepo(false);
				} else {
					pack.setMainRepo(true);
				}
				result.add(pack);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			MySQLDAOFactory.returnConnection(conn);
		}
		return result;
	}

	@Override
	public boolean deletePackage(User user, String packageName, String version) {
		// TODO Auto-generated method stub
		return false;
	}

}
