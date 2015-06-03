package hust.icse.bio.dao;

public interface MySQLconfig {
	public static final String DEFAULT_DATABASEURL = "jdbc:mysql://192.168.50.191:3306/bio_infomatics_service";
	public static final String DEFAULT_USER = "root";
	public static final String DEFAULT_PASSWORD = "bkcloud@123";
	public static final String DRIVER = "com.mysql.jdbc.Driver";
	public static final int MAX_POOL_SIZE = 10;
}
