package hust.icse.bio.utils;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class LockObject {
	private static LockObject instance = new LockObject();

	public static LockObject getInstance() {
		return instance;
	}
	public static void main(String[] args) {
		java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
		java.sql.Timestamp time = new Timestamp(Calendar.getInstance().getTimeInMillis());
		System.out.println(time.toString());
	}
}
