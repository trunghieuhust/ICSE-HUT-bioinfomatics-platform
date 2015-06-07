package hust.icse.bio.dao;

import hust.icse.bio.infrastructure.User;
import hust.icse.bio.tools.Package;

public interface PackageDAO {
	public boolean addPackage(User user, Package toolPackage);

	public Package getPackage(User user, String packageName, String version);

	public boolean deletePackage(User user, String packageName, String version);
}
