package bio.utils;

import java.util.UUID;

public class UUIDGenerator {
	public static UUID nextUUID() {
		return UUID.randomUUID();
	}

	public static String nextUUIDString() {
		return UUID.randomUUID().toString();
	}

	public static UUID UUIDfromString(String id) {
		UUID uuid = null;
		try {
			uuid = UUID.fromString(id);
		} catch (IllegalArgumentException e) {
			System.out
					.println(id
							+ " does not conform to the string representation as described in toString()");
		}
		return uuid;
	}
}
