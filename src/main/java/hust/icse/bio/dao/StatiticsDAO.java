package hust.icse.bio.dao;

import java.util.List;

import hust.icse.bio.infrastructure.User;
import hust.icse.bio.service.Statitics;

public interface StatiticsDAO {
	public List<Statitics> getStatitics(User user);
}
