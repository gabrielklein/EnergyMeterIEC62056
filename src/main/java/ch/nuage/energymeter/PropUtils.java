package ch.nuage.energymeter;

import java.util.Properties;

public class PropUtils {

	/**
	 * Get a boolean from the properties or the default value. 1 or true are
	 * considered as "true", otherwise "false"
	 *
	 * @param prop
	 * @param key
	 * @param val
	 * @return
	 */
	public static boolean getBooleanFromProperty(Properties prop, String key, boolean val) {
		String s = prop.getProperty(key, null);
		if (s == null) {
			return val;
		}

		s = s.trim().toLowerCase();

		return s.startsWith("t") || s.startsWith("1");
	}

	/**
	 * Get an int from the properties or the default value.
	 *
	 * @param prop
	 * @param key
	 * @param val
	 * @return
	 */
	public static int getIntFromProperty(Properties prop, String key, int val) {
		String s = prop.getProperty(key, null);
		if (s == null) {
			return val;
		}

		s = s.trim().toLowerCase();
		try {
			val = Integer.parseInt(s);
		} catch (Throwable t) {
			System.out.println("Cannot parse from the property file key as int: " + key + ", value: " + s);
		}

		return val;
	}

	/**
	 * Get a string from the properties or the default value.
	 *
	 * @param prop
	 * @param key
	 * @param val
	 * @return
	 */
	public static String getStringFromProperty(Properties prop, String key, String val) {
		String s = prop.getProperty(key, null);
		if (s == null) {
			return val;
		}
		s = s.trim();

		return s;
	}

}
