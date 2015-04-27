package hust.icse.bio.utils;

public class LockObject {
	private static LockObject instance = new LockObject();

	public static LockObject getInstance() {
		return instance;
	}
}
