package jp.reflexworks.test;

import java.util.regex.Pattern;

public class HostnameValidator {
	
	private static final Pattern HOSTNAME_PATTERN = Pattern.compile(
			"^(?=.{1,254}$)" +
					"(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+" +
					"[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.?$"
			);

	public static boolean isHostname(String hostname) {
		return hostname != null
				&& HOSTNAME_PATTERN.matcher(hostname).matches();
	}

	public static void main(String[] args) {
		System.out.println(isHostname("localhost"));       // false
		System.out.println(isHostname("mydb-sv1"));        // false
		System.out.println(isHostname("999"));    // false
		System.out.println(isHostname("34.118.237.212"));  // true
		System.out.println(isHostname("mytest.vte.cx"));   // true
		System.out.println(isHostname("example.com"));     // true
		System.out.println(isHostname("example.com."));    // true
		System.out.println(isHostname("999.999"));    // true
		System.out.println(isHostname("9999.999"));    // true
	}

}
