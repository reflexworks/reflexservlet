package jp.reflexworks.test;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;

import jp.reflexworks.servlet.util.WsseUtil;
import jp.reflexworks.servlet.util.WsseAuth;
import jp.sourceforge.reflex.util.DateUtil;

public class WsseGMT {
	
	public static final String ENCODING = WsseUtil.ENCODING;

	/**
	 * WSSE生成ツール.
	 * <p>
	 * WSSE文字列を標準出力に書き出します。
	 * WsseUtil#getWsseHeaderValueの引数にユーザ名とパスワードを設定してください。
	 * </p>
	 */
	public static void main(String[] args) {
		WsseGMT wsseTool = new WsseGMT();
		wsseTool.run();
	}
	
	private void run() {
		WsseUtil wsseUtil = new WsseUtil();

		WsseAuth wsseAuth = getWsseGMT("super", "superpass");
		System.out.println(wsseUtil.getWsseHeaderValue(wsseAuth));

		//WsseAuth wsseAuth = wsseUtil.getWsse("noriko.terada@gmail.com", "noriko2010");
		//wsseAuth = getWsseGMT("noriko.terada@gmail.com", "LTzqGL6XCkPUyaadLnSBFnz60Ko=");
		wsseAuth = getWsseGMT("noriko.terada@gmail.com", "Mj5efqvXxSXCjgZ9vlPPvv3oDVI=");
		System.out.println(wsseUtil.getWsseHeaderValue(wsseAuth));
		System.out.println(wsseUtil.getRXIDString(wsseAuth));

		//wsseAuth = wsseUtil.getWsse("terada@virtual-tech.net", "terada2010");
		wsseAuth = getWsseGMT("terada@virtual-tech.net", "Mj5efqvXxSXCjgZ9vlPPvv3oDVI=");
		System.out.println(wsseUtil.getWsseHeaderValue(wsseAuth));
		System.out.println(wsseUtil.getRXIDString(wsseAuth));

		wsseAuth = getWsseGMT("pdcadmin", "pwd4253103499");
		System.out.println(wsseUtil.getWsseHeaderValue(wsseAuth));
		System.out.println(wsseUtil.getRXIDString(wsseAuth));

		wsseAuth = getWsseGMT("test@virtual-tech.net", "aaa");
		System.out.println(wsseUtil.getWsseHeaderValue(wsseAuth));
		System.out.println(wsseUtil.getRXIDString(wsseAuth));
	}

	/**
	 * WSSE認証情報を作成します
	 * @param username ユーザ名
	 * @param password パスワード
	 */
	public WsseAuth getWsseGMT(String username, String password) {
		WsseAuth auth = null;
		
		byte[] nonceB = new byte[8];
		try {
			SecureRandom.getInstance("SHA1PRNG").nextBytes(nonceB);

			Date now = new Date();
			String created = DateUtil.getDateTime(now, "GMT+00:00");

			byte[] createdB = created.getBytes(ENCODING);
			byte[] passwordB = password.getBytes(ENCODING);

			byte[] v = new byte[nonceB.length + createdB.length + passwordB.length];
			System.arraycopy(nonceB, 0, v, 0, nonceB.length);
			System.arraycopy(createdB, 0, v, nonceB.length, createdB.length);
			System.arraycopy(passwordB, 0, v, nonceB.length + createdB.length, 
					passwordB.length);

			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(v);
			byte[] digest = md.digest();
			
			String passwordDigestStr = new String(Base64.encodeBase64(digest), ENCODING);
			String nonceStr = new String(Base64.encodeBase64(nonceB), ENCODING);

			auth = new WsseAuth(username, passwordDigestStr, nonceStr, created);
			auth.password = password;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return auth;
	}

}
