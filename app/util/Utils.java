package util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Utils {

	public static String generateHash(){
		String hash = String.valueOf(System.currentTimeMillis() + Math.random()
				* (new Random().nextInt(10) * 10));
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(hash.getBytes());
			BigInteger bigInt = new BigInteger(1, thedigest);
			hash = bigInt.toString(16);
			while (hash.length() < 32) {
				hash = "0" + hash;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hash;
	}
}
