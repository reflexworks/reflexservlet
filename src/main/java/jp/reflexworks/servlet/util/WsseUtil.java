package jp.reflexworks.servlet.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;

import jp.sourceforge.reflex.util.DateUtil;

import org.apache.commons.codec.binary.Base64;

/**
 * WSSE情報の取得、編集を行うクラス
 */
public class WsseUtil {

	// リクエストヘッダの項目名
	/** WSSEヘッダのキー */
	public static final String WSSE = "X-WSSE";
	/** トークン */
	public static final String TOKEN = "UsernameToken";
	/** Username */
	public static final String USER = "Username";
	/** PasswordDigest */
	public static final String PASSWORDDIGEST = "PasswordDigest";
	/** Nonce */
	public static final String NONCE = "Nonce";
	/** Created */
	public static final String CREATED = "Created";
	
	// URLパラメータの項目名
	/** URLパラメータのUsername　*/
	public static final String PARAM_USER = "user";
	/** URLパラメータのPasswordDigest　*/
	public static final String PARAM_PASSWORDDIGEST = "digest";
	/** URLパラメータのNonce　*/
	public static final String PARAM_NONCE = "nonce";
	/** URLパラメータのCreated　*/
	public static final String PARAM_CREATED = "created";
	
	// Cookieの項目名
	/** RXID */
	public static final String RXID = "RXID";	// URLパラメータのみ
	/** SSID */
	public static final String SSID = "SSID";	// CookieにセットするRXID

	/*
	private static final String SESSIONID_SEPARATOR = ",";
	private static final int IDX_USERNAME = 0;
	private static final int IDX_PASSWORDDIGEST = 1;
	private static final int IDX_NONCE = 2;
	private static final int IDX_CREATED = 3;
	*/
	
	/** エンコード */
	public static final String ENCODING = "UTF-8";

	private Logger logger = Logger.getLogger(this.getClass().getName());
		
	/**
	 * WSSE文字列を作成します(RequestHeader用)
	 * @param username ユーザ名
	 * @param password パスワード
	 * @return RequestHeaderに設定するWSSE情報
	 */
	public String getWsseHeaderValue(String username, String password) {
		return getWsseHeaderValue(getWsse(username, password));
	}

	/**
	 * WSSE文字列を作成します(RequestHeader用)
	 * @param auth WSSE認証情報
	 * @return RequestHeaderに設定するWSSE情報
	 */
	public String getWsseHeaderValue(WsseAuth auth) {
		if (auth == null) {
			return "";
		}
		
		StringBuffer buf = new StringBuffer();
		buf.append(TOKEN);
		buf.append(" ");
		buf.append(USER);
		buf.append("=\"");
		buf.append(auth.username);
		buf.append("\", ");
		buf.append(PASSWORDDIGEST);
		buf.append("=\"");
		buf.append(auth.passwordDigest);
		buf.append("\", ");
		buf.append(NONCE);
		buf.append("=\"");
		buf.append(auth.nonce);
		buf.append("\", ");
		buf.append(CREATED);
		buf.append("=\"");
		buf.append(auth.created);
		buf.append('"');
		
		return buf.toString();
	}
		
	/**
	 * WSSE文字列を作成します(URLパラメータ用)
	 * @param username ユーザ名
	 * @param password パスワード
	 * @return URLパラメータに設定するWSSE情報
	 */
	public String getWsseUrlParam(String username, String password) {
		return getWsseUrlParam(getWsse(username, password));
	}

	/**
	 * WSSE文字列を作成します(URLパラメータ用)
	 * @param auth WSSE認証情報
	 * @return URLパラメータに設定するWSSE情報
	 */
	public String getWsseUrlParam(WsseAuth auth) {
		if (auth == null) {
			return "";
		}
		
		StringBuffer buf = new StringBuffer();
		buf.append(PARAM_USER);
		buf.append("=");
		buf.append(urlEncode(auth.username));
		buf.append("&");
		buf.append(PARAM_PASSWORDDIGEST);
		buf.append("=");
		buf.append(urlEncode(auth.passwordDigest));
		buf.append("&");
		buf.append(PARAM_NONCE);
		buf.append("=");
		buf.append(urlEncode(auth.nonce));
		buf.append("&");
		buf.append(PARAM_CREATED);
		buf.append("=");
		buf.append(urlEncode(auth.created));
		
		return buf.toString();
	}
	
	private String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, ENCODING);
		} catch (UnsupportedEncodingException e) {}
		return str;
	}

	/**
	 * WSSE認証情報を作成します
	 * @param username ユーザ名
	 * @param password パスワード
	 */
	public WsseAuth getWsse(String username, String password) {
		WsseAuth auth = null;
		
		byte[] nonceB = new byte[8];
		try {
			SecureRandom.getInstance("SHA1PRNG").nextBytes(nonceB);

			Date now = new Date();
			String created = DateUtil.getDateTime(now);

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
			logger.log(Level.WARNING, e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return auth;
	}

	/**
	 * リクエストからWSSE認証を行います
	 * @param req リクエスト
	 * @param password パスワード
	 * @return WSSE認証OKの場合true、エラーの場合false
	 */
	public boolean wsseAuthentication(HttpServletRequest req, String password) {
		// 認証情報の取り出し
		WsseAuth auth = getWsseAuth(req, true);	// SSID有効
		
		// 認証チェック
		return checkAuth(auth, password);
	}
	
	/**
	 * リクエストからWSSE認証情報を取り出します
	 * @param req リクエスト
	 * @return WSSE認証情報
	 */
	public WsseAuth getWsseAuth(HttpServletRequest req, boolean enableSsid) {
		WsseAuth auth = null;
		String wsse = req.getHeader(WSSE);
		if (wsse != null) {
			// 認証情報はヘッダに指定
			auth = parseWSSEheader(wsse);
		} else {
			// 認証情報はURLパラメータに指定
			auth = parseWSSEparam(req);
		}
		if (auth == null) {
			// リクエストの属性から認証情報を取得
			auth = parseWSSEAttribute(req, enableSsid);
		}
		if (auth == null) {
			// Cookieから認証情報を取得
			auth = parseWSSEcookie(req, enableSsid);
		}
		
		return auth;
	}

	/**
	 * リクエストヘッダからWSSE認証情報を取り出します
	 * @param header リクエストヘッダに指定されたWSSE文字列
	 * @return WSSE認証情報
	 */
	public WsseAuth parseWSSEheader(String header) {
		String authUsername = null;
		String authPassworddigest = null;
		String authNonce = null;
		String authCreated = null;

		String[] words = header.split(",");
		int idx, i;

		for (i = 0; i < words.length; i++) {
			String rec = words[i];
			int len = rec.length();

			if (((idx = rec.indexOf('=')) > 0) && (idx < (len-1)))  {
				String key = rec.substring(0, idx).trim();
				String val = rec.substring(idx+1, len).trim();
				int	stx = 0;
				int edx = val.length() - 1;
				char	c;
				if (((c = val.charAt(stx)) == '"') || (c == '\''))	stx++;
				if (((c = val.charAt(edx)) == '"') || (c == '\''))	edx--;
				val = val.substring(stx, edx+1);

				if (key.indexOf(USER) >= 0) {
					authUsername = val;
				}
				else if (key.equals(PASSWORDDIGEST)) {
					authPassworddigest = val;
				}
				else if (key.equals(NONCE)) {
					authNonce = val;
				}
				else if (key.equals(CREATED)) {
					authCreated = val;
				}
			}
		}

		//認証パラメータの取り出せない場合は終了
		if ((authUsername != null) &&
				(authPassworddigest != null) &&
				(authNonce != null) &&
				(authCreated != null)) {
			WsseAuth auth = new WsseAuth(authUsername, authPassworddigest, 
					authNonce, authCreated);
			auth.isOnetime = true;	// WSSE文字列もワンタイムチェックを実施する。
			return auth;
		}
		
		return null;
	}

	/**
	 * URLパラメータからWSSE認証情報を取り出します
	 * @param req リクエスト
	 * @return WSSE認証情報
	 */
	public WsseAuth parseWSSEparam(HttpServletRequest req) {
		String authUsername = req.getParameter(PARAM_USER);
		String authPassworddigest = req.getParameter(PARAM_PASSWORDDIGEST);
		String authNonce = req.getParameter(PARAM_NONCE);
		String authCreated = req.getParameter(PARAM_CREATED);

		WsseAuth auth = null;

		//認証パラメータの取り出せない場合は終了
		if ((authUsername != null) &&
				(authPassworddigest != null) &&
				(authNonce != null) &&
				(authCreated != null)) {
			auth = new WsseAuth(authUsername, authPassworddigest, authNonce, authCreated);
			auth.isOnetime = true;	// WSSE文字列もワンタイムチェックを実施する。
		}
		
		if (auth == null) {
			String rxid = req.getParameter(RXID);
			auth = parseRXID(rxid, true);
			if (auth != null) {
				auth.isOnetime = true;
			}
		}
		
		return auth;
	}

	/**
	 * CookieからWSSE認証情報を取り出します
	 * @param req リクエスト
	 * @return WSSE認証情報
	 */
	public WsseAuth parseWSSEcookie(HttpServletRequest req, boolean enableSsid) {
		WsseAuth auth = null;
		Cookie cookie = null;

		// SSID
		if (enableSsid) {
			cookie = getSSID(req);
			if (cookie != null) {
				String value = cookie.getValue();
				auth = parseRXID(value, false);
			}
		}

		// RXID (ワンタイムチェック実施)
		if (auth == null) {
			cookie = getRXID(req);
			if (cookie != null) {
				String value = cookie.getValue();
				auth = parseRXID(value, true);
			}
		}

		if (auth != null) {
			auth.isSession = true;
		}

		return auth;
	}
	
	/**
	 * リクエストの属性(Attribute)からWSSE認証情報を取り出します
	 * @param req リクエスト
	 * @return WSSE認証情報
	 */
	public WsseAuth parseWSSEAttribute(HttpServletRequest req, boolean enableSsid) {
		Object rxidObj = null;
		if (enableSsid) {
			rxidObj = req.getAttribute(SSID);
			if (rxidObj != null && rxidObj instanceof String) {
				return parseRXID((String)rxidObj, false);
			}
		}
		rxidObj = req.getAttribute(RXID);
		if (rxidObj != null && rxidObj instanceof String) {
			return parseRXID((String)rxidObj, true);
		}
		return null;
	}
	
	/**
	 * RXID文字列からWSSE認証情報を作成します
	 * @param value RXID文字列
	 * @return WSSE認証情報
	 */
	public WsseAuth parseRXID(String value) {
		return parseRXID(value, true);
	}
	
	/**
	 * RXID文字列からWSSE認証情報を作成します
	 * @param value RXID文字列
	 * @param isRxid ワンタイム利用かどうか
	 * @return WSSE認証情報
	 */
	public WsseAuth parseRXID(String value, boolean isRxid) {
		WsseAuth auth = null;
		if (value != null) {
			try {
				// Base64デコード
				byte[] rxidByte = Base64.decodeBase64(value.getBytes());
				String rxidStr = new String(rxidByte, ENCODING);
				auth = parseWSSEheader(rxidStr);
				if (auth != null) {
					auth.isOnetime = isRxid;
				}
				
			} catch (UnsupportedEncodingException e) {}	// Do nothing.
		}
		
		return auth;
	}
	
	/**
	 * CookieからRXID文字列を取得します
	 * @param req リクエスト
	 * @return RXID文字列
	 */
	public String getRXIDString(HttpServletRequest req) {
		Cookie cookie = getRXID(req);
		String rxidStr = null;
		if (cookie != null) {
			rxidStr = cookie.getValue();
		}
		return rxidStr;
	}
	
	/**
	 * RXID(WSSE情報)が設定されているCookieを取得します
	 * @param req リクエスト
	 * @return RXID(WSSE情報)が設定されているCookie
	 */
	public Cookie getRXID(HttpServletRequest req) {
		return getCookie(req, RXID);
	}
	
	/**
	 * SSID(WSSE情報)が設定されているCookieを取得します
	 * @param req リクエスト
	 * @return SSID(WSSE情報)が設定されているCookie
	 */
	public Cookie getSSID(HttpServletRequest req) {
		return getCookie(req, SSID);
	}

	/**
	 * RXID文字列からCookieを作成します
	 * @param rxidStr RXID文字列
	 * @return RXIDを設定したCookie
	 */
	public Cookie getRXID(String rxidStr) {
		return new Cookie(RXID, rxidStr);
	}

	/**
	 * SSID文字列からCookieを作成します
	 * @param rxidStr SSID文字列
	 * @return SSIDを設定したCookie
	 */
	public Cookie getSSID(String rxidStr) {
		return new Cookie(SSID, rxidStr);
	}
	
	/**
	 * WSSE認証情報からRXIDを設定したCookieを作成します
	 * @param auth WSSE認証情報
	 * @return RXIDを設定したCookie
	 */
	public Cookie getRXID(WsseAuth auth) {
		// base64エンコード
		String rxidStr = getRXIDString(auth);
		return getRXID(rxidStr);
	}

	/**
	 * WSSE認証情報からSSIDを設定したCookieを作成します
	 * @param auth WSSE認証情報
	 * @return SSIDを設定したCookie
	 */
	public Cookie getSSID(WsseAuth auth) {
		// base64エンコード
		String rxidStr = getRXIDString(auth);
		return getSSID(rxidStr);
	}
	
	/**
	 * WSSE認証情報からRXID文字列を作成します
	 * @param auth WSSE認証情報
	 * @return RXID文字列
	 */
	public String getRXIDString(WsseAuth auth) {
		if (auth != null && auth.username != null && auth.passwordDigest != null &&
				auth.nonce != null && auth.created != null) {
			try {
				String rxidStr = getWsseHeaderValue(auth);
				byte[] rxidByte = rxidStr.getBytes(ENCODING);
				
				return new String(Base64.encodeBase64(rxidByte));
			} catch (UnsupportedEncodingException e) {}	// Do nothing.
		}

		return null;
	}

	/**
	 * WSSE認証チェック
	 * @param auth WSSE認証情報
	 * @param password パスワード
	 * @return WSSE認証OKの場合true、エラーの場合false
	 */
	public boolean checkAuth(WsseAuth auth, String password) {
		if (auth == null) {
			return false;
		}

		try {
			//入力されたパラメータを取得
			byte[] digestB = Base64.decodeBase64(auth.passwordDigest.getBytes());
			byte[] nonceB = Base64.decodeBase64(auth.nonce.getBytes());
			byte[] createdB = auth.created.getBytes(ENCODING);

			//指定パスワードからdigestを生成        	
			byte[] passwordB = password.getBytes(ENCODING);
			byte[] v = new byte[nonceB.length + createdB.length + passwordB.length];
			System.arraycopy(nonceB, 0, v, 0, nonceB.length);
			System.arraycopy(createdB, 0, v, nonceB.length, createdB.length);
			System.arraycopy(passwordB, 0, v, nonceB.length + createdB.length, 
					passwordB.length);

			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(v);

			//digestを比較
			byte[] mdDigestB = md.digest();
			boolean isEqual = MessageDigest.isEqual(mdDigestB, digestB);
			if (isEqual) {
				auth.password = password;	// Wsseオブジェクトにpasswordを設定
			}
			return isEqual;

		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		return false;
	}
		
	private Cookie getCookie(HttpServletRequest req, String key) {
		if (key == null) {
			return null;
		}
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (key.equals(cookie.getName())) {
					return cookie;
				}
			}
		}
		return null;
	}
	
	/*
	 * WSSEのusername = [認可ID]認証IDとし、パスワードは認証IDに対してチェックする。ACLは認可IDに対してチェックする。
	 */
	/*
	public String getUsername(String certificationId, String permissionId) {
		StringBuffer buf = new StringBuffer();
		if (permissionId != null) {
			buf.append("[");
			buf.append(permissionId);
			buf.append("]");
		}
		buf.append(certificationId);
		return buf.toString();
	}
	
	public String getCertificationId(String username) {
		if (username != null) {
			int idx1 = username.indexOf("[");
			int idx2 = username.indexOf("]");
			if (idx1 > -1 && idx2 > 0) {
				if (idx2 < username.length()) {
					return username.substring(idx2 + 1);
				} else {
					return null;
				}
			}
		}
		return username;
	}
	
	public String getPermissionId(String username) {
		if (username != null) {
			int idx1 = username.indexOf("[");
			int idx2 = username.indexOf("]");
			if (idx1 > -1 && idx2 > 0) {
				return username.substring(idx1 + 1, idx2);
			}
		}
		return username;
	}
	*/

	/**
	 * WSSE認証情報
	 */
	public class WsseAuth {
		/** ユーザ名 */
		public String username;
		/** PasswordDigest */
		public String passwordDigest;
		/** Nonce */
		public String nonce;
		/** Created */
		public String created;
		/** パスワード */
		public String password;
		public boolean isSession;
		/** ワンタイムかどうか */
		public boolean isOnetime;
		
		public WsseAuth(String username, String passwordDigest, String nonce, 
				String created) {
			this.username = username;
			this.passwordDigest = passwordDigest;
			this.nonce = nonce;
			this.created = created;
		}
		
		@Override
		public String toString() {
			return "WsseAuth [username=" + username + ", passwordDigest="
					+ passwordDigest + ", nonce=" + nonce + ", created="
					+ created + ", isRxid=" + isOnetime + ", isSession=" + isSession
					+ "]";
		}

	}

}
