package hust.icse.bio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import hust.icse.bio.infrastructure.User;
import hust.icse.bio.service.Statitics;

public class MySQLStatiticsDAO implements StatiticsDAO {

	@Override
	public List<Statitics> getStatitics(User user) {
		Connection conn = MySQLDAOFactory.createConnection();
		List<Statitics> statiticsList = new ArrayList<Statitics>();
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("SELECT flavor, duration FROM task");
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();
			while (resultSet.next()) {
				Statitics statitics = new Statitics();
				statitics.setFlavor(resultSet.getString("flavor"));
				statitics.setTime(resultSet.getLong("duration"));
				statiticsList.add(statitics);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statiticsList;
	}

}
