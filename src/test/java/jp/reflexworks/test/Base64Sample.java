package jp.reflexworks.test;

import jp.reflexworks.servlet.util.MimeDecoder;

import org.apache.commons.codec.binary.Base64;

public class Base64Sample {
	
	//private static final String NAME = "=?UTF-8?B?44GG44GV44GN44KZ5q2MLmpwZw==?=";
	//private static final String NAME = "=?ISO-2022-JP?B?GyRCJTUlcyVXJWslYSUkJWsbKEI=?=";
	private static final String NAME = "=?ISO-2022-JP?B?GyRCJUYlOSVIJWEhPCVrISobKEI=?=";
	private static final String BASE64NAME = "44GG44GV44GN44KZ5q2MLmpwZw==";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		try {
			String decodeStr = new String(Base64.decodeBase64(BASE64NAME), "UTF-8");
			System.out.println("base64decode : " + decodeStr);
			
			System.out.println("MimeDecoder : " + MimeDecoder.decode(NAME));
			System.out.println("MimeDecoder(2) : " + MimeDecoder.decode(null));
			System.out.println("MimeDecoder(3) : " + MimeDecoder.decode("適当な文字列"));
			System.out.println("MimeDecoder(4) : " + MimeDecoder.decode(""));
			System.out.println("MimeDecoder(5) : " + MimeDecoder.decode("="));
			System.out.println("MimeDecoder(6) : " + MimeDecoder.decode("=?"));
			System.out.println("MimeDecoder(7) : " + MimeDecoder.decode("=??"));
			System.out.println("MimeDecoder(8) : " + MimeDecoder.decode("=??="));
			System.out.println("MimeDecoder(9) : " + MimeDecoder.decode("=?UTF-8?="));
			System.out.println("MimeDecoder(10) : " + MimeDecoder.decode("=?UTF-8?Q?="));
			System.out.println("MimeDecoder(11) : " + MimeDecoder.decode("=?UTF-8?A?="));
			System.out.println("MimeDecoder(12) : " + MimeDecoder.decode("=?UTF-8?B?="));
			System.out.println("MimeDecoder(13) : " + MimeDecoder.decode("=?UTF-8?B?a="));
			System.out.println("MimeDecoder(14) : " + MimeDecoder.decode("=?UTF-8?B?a?="));
			System.out.println("MimeDecoder(15) : " + MimeDecoder.decode("=?UTF-8?B?44GG?="));
			System.out.println("MimeDecoder(16) : " + MimeDecoder.decode("=?UTF-8?B?44GV?="));
			
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

}
