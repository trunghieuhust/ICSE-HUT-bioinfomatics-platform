package hust.icse.bio.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDultis {
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

	public static UUID UUIDfromStringWithoutDashes(String id) {
		String uuid = id.replaceAll(
				"(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
		return UUIDfromString(uuid);
	}

	public static byte[] UUIDtoByteArray(UUID id) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(id.getMostSignificantBits());
		bb.putLong(id.getLeastSignificantBits());
		return bb.array();
	}

	public static byte[] UUIDtoByteArray(String id) {
		return UUIDtoByteArray(UUIDfromString(id));
	}

	public static UUID UUIDFromByteArray(byte[] byteArray) {
		ByteBuffer bb = ByteBuffer.wrap(byteArray);
		long msb = bb.getLong();
		long lsb = bb.getLong();
		return new UUID(msb, lsb);
	}
//TEST 
	public static void main(String[] args) {
		 UUID id = nextUUID();
		 System.out.println(id.toString());
		 byte[] b = UUIDtoByteArray(id);
		 UUID recID = UUIDFromByteArray(b);
		 System.out.println(recID.toString());
		 System.out.println(id.compareTo(recID));
		 System.out.println("5509321b783747e587044ff7ec4a50e9");
		 System.out
		 .println(UUIDfromStringWithoutDashes("5509321b783747e587044ff7ec4a50e9"));
	}
}
