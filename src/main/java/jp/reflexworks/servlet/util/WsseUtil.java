package jp.reflexworks.servlet.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;
import java.util.Map;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import jp.sourceforge.reflex.util.DateUtil;
import jp.sourceforge.reflex.util.StringUtils;

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
	/** RXIDヘッダのキー */
	public static final String HEADER_RXID = "X-RXID";
	/** ハッシュ関数 */
	//public static final String HASH_ALGORITHM = "SHA-1";
	public static final String HASH_ALGORITHM = "SHA-256";
	/** 乱数生成のための関数 */
	public static final String RANDOM_ALGORITHM = "SHA1PRNG";
	
	// URLパラメータの項目名
	/** URLパラメータのUsername　*/
	//public static final String PARAM_USER = "_user";
	/** URLパラメータのPasswordDigest　*/
	//public static final String PARAM_PASSWORDDIGEST = "_digest";
	/** URLパラメータのNonce　*/
	//public static final String PARAM_NONCE = "_nonce";
	/** URLパラメータのCreated　*/
	//public static final String PARAM_CREATED = "_created";
	
	// Cookieの項目名
	/** RXID */
	public static final String RXID = "_RXID";	// URLパラメータのみ
	/** SSID */
	//public static final String SSID = "SSID";	// CookieにセットするRXID
	
	/** エンコード */
	public static final String ENCODING = "UTF-8";
	
	/** Set-Cookie */
	//public static final String SET_COOKIE = "Set-Cookie";
	//public static final String SET_COOKIE_LOWER = SET_COOKIE.toLowerCase();
	/** Set-CookieされたRXIDの接頭辞 */
	//public static final String COOKIE_RXID_PREFIX = RXID + "=";
	//public static final int COOKIE_RXID_PREFIX_LEN = COOKIE_RXID_PREFIX.length();
	
	/** RXID Delimiter **/
	private static final String RXID_DELIMITER = "-";

	private Logger logger = Logger.getLogger(this.getClass().getName());
		
	/**
	 * WSSE文字列を作成します(RequestHeader用)
	 * @param username ユーザ名
	 * @param password パスワード
	 * @return RequestHeaderに設定するWSSE情報
	 */
	public String createWsseHeaderValue(String username, String password) {
		return getWsseHeaderValue(createWsse(username, password, null));
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
	/*
	public String getWsseUrlParam(String username, String password) {
		return getWsseUrlParam(getWsse(username, password));
	}
	*/

	/**
	 * WSSE文字列を作成します(URLパラメータ用)
	 * @param auth WSSE認証情報
	 * @return URLパラメータに設定するWSSE情報
	 */
	/*
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
	*/
	
	/*
	private String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, ENCODING);
		} catch (UnsupportedEncodingException e) {}
		return str;
	}
	*/

	/**
	 * WSSE認証情報を作成します
	 * @param username ユーザ名
	 * @param password パスワード
	 * @param apiKey APIKey (RXIDの場合APIKeyを指定します。)
	 */
	public WsseAuth createWsse(String username, String password, String apiKey) {
		WsseAuth auth = null;
		
		byte[] nonceB = new byte[8];
		try {
			SecureRandom.getInstance(RANDOM_ALGORITHM).nextBytes(nonceB);

			Date now = new Date();
			String created = DateUtil.getDateTime(now);

			byte[] createdB = created.getBytes(ENCODING);
			byte[] passwordB = password.getBytes(ENCODING);
			byte[] apiKeyB = null;
			if (apiKey != null) {
				apiKeyB = apiKey.getBytes(ENCODING);
			}

			int len = nonceB.length + createdB.length + passwordB.length;
			int apiKeyLen = 0;
			if (apiKey != null) {
				// APIKeyを含む
				apiKeyLen = apiKeyB.length;
				len += apiKeyLen;
			}
			byte[] v = new byte[len];
			if (apiKey != null) {
				// APIKeyを含む
				System.arraycopy(apiKeyB, 0, v, 0, apiKeyLen);
			}
			System.arraycopy(nonceB, 0, v, apiKeyLen, nonceB.length);
			System.arraycopy(createdB, 0, v, apiKeyLen + nonceB.length, createdB.length);
			System.arraycopy(passwordB, 0, v, apiKeyLen + nonceB.length + createdB.length, 
					passwordB.length);

			MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
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
	 * @param apiKey APIKey
	 * @return WSSE認証OKの場合true、エラーの場合false
	 */
	public boolean wsseAuthentication(HttpServletRequest req, String password,
			String apiKey) {
		// 認証情報の取り出し
		//WsseAuth auth = getWsseAuth(req, true);	// SSID有効
		WsseAuth auth = getWsseAuth(req);	// SSID有効
		
		// 認証チェック
		return checkAuth(auth, password, apiKey);
	}
	
	/**
	 * リクエストからWSSE認証情報を取り出します
	 * @param req リクエスト
	 * @return WSSE認証情報
	 */
	//public WsseAuth getWsseAuth(HttpServletRequest req, boolean enableSsid) {
	public WsseAuth getWsseAuth(HttpServletRequest req) {
		// ヘッダから認証情報を取得
		WsseAuth auth = getWsseAuthFromHeader(req);
		if (auth == null) {
			// URLパラメータから認証情報を取得
			auth = parseWSSEparam(req);
		}
		
		/*
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
		*/
		
		return auth;
	}

	/**
	 * リクエストヘッダからWSSE認証情報を取り出します
	 * @param header リクエストヘッダに指定されたWSSE文字列
	 * @return WSSE認証情報
	 */
	public WsseAuth getWsseAuthFromHeader(HttpServletRequest req) {
		String value = req.getHeader(WSSE);
		if (value != null) {
			return parseWSSEheader(value);
		}
		value = req.getHeader(HEADER_RXID);
		if (value != null) {
			return parseRXID(value);
		}
		return null;
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
			//auth.isOnetime = true;	// WSSE文字列もワンタイムチェックを実施する。
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
		WsseAuth auth = null;
		String rxid = req.getParameter(RXID);
		if (rxid != null) {
			//auth = parseRXID(rxid, true);
			auth = parseRXID(rxid);
		}
		return auth;
		
		/*
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
		*/
	}

	/**
	 * CookieからWSSE認証情報を取り出します
	 * @param req リクエスト
	 * @return WSSE認証情報
	 */
	/*
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
			auth.isCookie = true;
		}

		return auth;
	}
	*/
	
	/**
	 * リクエストの属性(Attribute)からWSSE認証情報を取り出します
	 * @param req リクエスト
	 * @return WSSE認証情報
	 */
	/*
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
	*/
	
	/**
	 * RXID文字列からWSSE認証情報を作成します
	 * @param value RXID文字列
	 * @return WSSE認証情報
	 */
	/*
	public WsseAuth parseRXID(String value) {
		return parseRXID(value, true);
	}
	*/
	
	/**
	 * RXID文字列からWSSE認証情報を作成します
	 * @param value RXID文字列
	 * @param isRxid ワンタイム利用かどうか
	 * @return WSSE認証情報
	 */
	//public WsseAuth parseRXID(String value, boolean isRxid) {
	public WsseAuth parseRXID(String value) {
		WsseAuth auth = null;
		if (value != null) {
			/*
			// 旧形式
			try {
				// Base64デコード
				byte[] rxidByte = Base64.decodeBase64(value.getBytes());
				String rxidStr = new String(rxidByte, ENCODING);
				auth = parseWSSEheader(rxidStr);
				if (auth != null) {
					auth.isOnetime = isRxid;
				}
				
			} catch (UnsupportedEncodingException e) {}	// Do nothing.
			*/
			// 短縮形式
			// 不正文字列の場合処理しないでnullを返す
			int p1 = value.indexOf(RXID_DELIMITER);
			int p2 = -1;
			int p3 = -1;
			if (p1 > 0) {
				p2 = value.substring(p1 + 1).indexOf(RXID_DELIMITER) + 1;
			}
			if (p2 > 0) {
				p2 += p1;
				p3 = value.substring(p2 + 1).indexOf(RXID_DELIMITER) + 1;
			}
			if (p3 > 0) {
				p3 += p2;
				try {
					String createdStr = getDateTimeOfWSSE(value.substring(0, p1));
					String nonceStr = rot13(value.substring(p1 + 1, p2));
					String passwordDigestStr = rot13(value.substring(p2 + 1, p3)); 
					String username = rot13(value.substring(p3 + 1)); 
					if (createdStr != null && nonceStr != null && 
							passwordDigestStr != null && username != null) {
						auth = new WsseAuth(username, passwordDigestStr, 
								nonceStr, createdStr);
						//auth.isOnetime = true;
					}
				} catch (ParseException e) {}	// Do nothing.
			}
		}
		
		//if (auth != null && isRxid) {
		if (auth != null) {
			auth.isRxid = true;
		}
		return auth;
	}
	
	/**
	 * rotate13(簡易暗号化)
	 * @param s
	 * @return 暗号化文字列
	 */
	public String rot13(String s) {
		if (s == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '@') c = '!';
			else if (c == '!') c = '@';
			else if (c == '/') c = '~';
			else if (c == '~') c = '/';
			else if (c == '+') c = '*';
			else if (c == '*') c = '+';
			else if (c >= 'a' && c <= 'm') c += 13;
			else if (c >= 'A' && c <= 'M') c += 13;
			else if (c >= 'n' && c <= 'z') c -= 13;
			else if (c >= 'N' && c <= 'Z') c -= 13;
			
			if (c != '=') {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * RXIDのcreatedをWSSE用("yyyy-MM-dd'T'HH:mm:ss+99:99"形式の文字列)に変換します
	 * @param rxidcreated RXIDのcreated
	 * @return dateの文字列
	 * @throws ParseException 
	 */
	private String getDateTimeOfWSSE(String rxidcreated) throws ParseException {
		String dateStr = rxidcreated.replace("P", "+").replace("M", "-");
		int idx = dateStr.lastIndexOf("+");
		if (idx < 0) {
			idx = dateStr.lastIndexOf("-");
		}
		String timeZoneId = null;
		if (idx > 0) {
			timeZoneId = "GMT" + dateStr.substring(idx) + ":00";
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssZ");
		if (timeZoneId != null) {
			dateStr += "00";
			TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
			format.setTimeZone(timeZone);
			Date date = format.parse(dateStr);
			return DateUtil.getDateTime(date, timeZoneId);
		}
		return null;
	}

	/**
	 * WSSEのcreatedをRXID用("yyyyMMddHHmmssP99"形式の文字列)に変換します
	 * @param wssecreated WSSEのcreated
	 * @return dateの文字列
	 * @throws ParseException 
	 */
	private String getDateTimeOfRXID(String wssecreated) throws ParseException {
		int idx = wssecreated.lastIndexOf("+");
		if (idx < 0) {
			idx = wssecreated.lastIndexOf("-");
		}
		TimeZone timeZone = null;
		if (idx > 0) {
			String id = "GMT" + wssecreated.substring(idx);
			timeZone = TimeZone.getTimeZone(id);
		}
		
		Date date = DateUtil.getDate(wssecreated);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		if (timeZone != null) {
			format.setTimeZone(timeZone);
		}
		String datestring = format.format(date);
		format = new SimpleDateFormat("Z");
		if (timeZone != null) {
			format.setTimeZone(timeZone);
		}
		String zone = format.format(date);
		datestring += zone.substring(0, 3).replace("+", "P").replace("-", "M");
		return datestring;
	}

	/**
	 * CookieからRXID文字列を取得します
	 * @param req リクエスト
	 * @return RXID文字列
	 */
	/*
	public String getRXIDString(HttpServletRequest req) {
		Cookie cookie = getRXID(req);
		String rxidStr = null;
		if (cookie != null) {
			rxidStr = cookie.getValue();
		}
		return rxidStr;
	}
	*/
	
	/**
	 * RXID(WSSE情報)が設定されているCookieを取得します
	 * @param req リクエスト
	 * @return RXID(WSSE情報)が設定されているCookie
	 */
	/*
	public Cookie getRXID(HttpServletRequest req) {
		return getCookie(req, RXID);
	}
	*/
	
	/**
	 * SSID(WSSE情報)が設定されているCookieを取得します
	 * @param req リクエスト
	 * @return SSID(WSSE情報)が設定されているCookie
	 */
	/*
	public Cookie getSSID(HttpServletRequest req) {
		return getCookie(req, SSID);
	}
	*/

	/**
	 * RXID文字列からCookieを作成します
	 * @param rxidStr RXID文字列
	 * @return RXIDを設定したCookie
	 */
	/*
	public Cookie getRXID(String rxidStr) {
		return new Cookie(RXID, rxidStr);
	}
	*/

	/**
	 * SSID文字列からCookieを作成します
	 * @param rxidStr SSID文字列
	 * @return SSIDを設定したCookie
	 */
	/*
	public Cookie getSSID(String rxidStr) {
		return new Cookie(SSID, rxidStr);
	}
	*/
	
	/**
	 * WSSE認証情報からRXIDを設定したCookieを作成します
	 * @param auth WSSE認証情報
	 * @return RXIDを設定したCookie
	 */
	/*
	public Cookie getRXID(WsseAuth auth) {
		// base64エンコード
		String rxidStr = getRXIDString(auth);
		return getRXID(rxidStr);
	}
	*/

	/**
	 * WSSE認証情報からSSIDを設定したCookieを作成します
	 * @param auth WSSE認証情報
	 * @return SSIDを設定したCookie
	 */
	/*
	public Cookie getSSID(WsseAuth auth) {
		// base64エンコード
		String rxidStr = getRXIDString(auth);
		return getSSID(rxidStr);
	}
	*/
	
	/**
	 * WSSE認証情報からRXID文字列を作成します
	 * @param auth WSSE認証情報
	 * @return RXID文字列
	 */
	public String getRXIDString(WsseAuth auth) {
		if (auth != null && auth.username != null && auth.passwordDigest != null &&
				auth.nonce != null && auth.created != null) {
			// 旧形式
			/*
			try {
				String rxidStr = getWsseHeaderValue(auth);
				byte[] rxidByte = rxidStr.getBytes(ENCODING);
				
				return new String(Base64.encodeBase64(rxidByte));
			} catch (UnsupportedEncodingException e) {}	// Do nothing.
			*/

			// 短縮形式
			try {
				StringBuilder buf = new StringBuilder();
				buf.append(getDateTimeOfRXID(auth.created));
				buf.append(RXID_DELIMITER);
				buf.append(rot13(auth.nonce));
				buf.append(RXID_DELIMITER);
				buf.append(rot13(auth.passwordDigest));
				buf.append(RXID_DELIMITER);
				buf.append(rot13(auth.username));
				return buf.toString();
				
			} catch (ParseException e) {}	// Do nothing.
		}

		return null;
	}
	
	/**
	 * ユーザ名とパスワードとAPIKeyからRXID文字列を作成します.
	 * @param username ユーザ名
	 * @param password パスワード
	 * @param apiKey APIKey
	 * @return RXID文字列
	 */
	public String createRXIDString(String username, String password, String apiKey) {
		WsseAuth auth = createWsse(username, password, apiKey);
		return getRXIDString(auth);
	}
	
	/**
	 * ユーザ名とパスワードからRXID Cookieオブジェクトを作成します.
	 * @param username ユーザ名
	 * @param password パスワード
	 * @return RXID Cookieオブジェクト
	 */
	/*
	public Cookie createRXID(String username, String password) {
		WsseAuth auth = getWsse(username, password);
		String rxidString = getRXIDString(auth);
		return getRXID(rxidString);
	}
	*/

	/**
	 * WSSE認証チェック
	 * @param auth WSSE認証情報
	 * @param password パスワード
	 * @param apiKey APIKey
	 * @return WSSE認証OKの場合true、エラーの場合false
	 */
	public boolean checkAuth(WsseAuth auth, String password, String apiKey) {
		if (auth == null) {
			return false;
		}
		if (auth.isRxid && apiKey == null) {
			return false;
		}

		try {
			//入力されたパラメータを取得
			byte[] digestB = Base64.decodeBase64(auth.passwordDigest.getBytes(ENCODING));
			byte[] nonceB = Base64.decodeBase64(auth.nonce.getBytes(ENCODING));
			byte[] createdB = auth.created.getBytes(ENCODING);
			byte[] apiKeyB = null;
			if (auth.isRxid) {
				apiKeyB = apiKey.getBytes(ENCODING);
			}

			//指定パスワードからdigestを生成
			byte[] passwordB = password.getBytes(ENCODING);
			
			int len = nonceB.length + createdB.length + passwordB.length;
			int apiKeyLen = 0;
			if (auth.isRxid) {
				// APIKeyを含む
				apiKeyLen = apiKeyB.length;
				len += apiKeyLen;
			}
			byte[] v = new byte[len];
			if (auth.isRxid) {
				// APIKeyを含む
				System.arraycopy(apiKeyB, 0, v, 0, apiKeyB.length);
			}
			System.arraycopy(nonceB, 0, v, apiKeyLen, nonceB.length);
			System.arraycopy(createdB, 0, v, apiKeyLen + nonceB.length, createdB.length);
			System.arraycopy(passwordB, 0, v, apiKeyLen + nonceB.length + createdB.length, 
					passwordB.length);

			//MessageDigest md = MessageDigest.getInstance("SHA1");
			MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
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
	
	/*
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
	*/
	
	/**
	 * URLフェッチ実行時、Set-CookieされたRXIDを取得します.
	 * @param conn HttpURLConnection
	 * @return RXID
	 */
	/*
	public String getRXIDFromConnection(HttpURLConnection conn) {
		if (conn == null) {
			return null;
		}
		Map<String, List<String>> headers = conn.getHeaderFields();
		return getRXIDFromHeaders(headers);
	}
	*/
	
	/**
	 * URLフェッチ実行時、レスポンスヘッダに設定されたRXIDを取得します.
	 * @param headers レスポンスヘッダ
	 * @return RXID
	 */
	public String getRXIDFromHeaders(Map<String, List<String>> headers) {
		if (headers == null) {
			return null;
		}
		List<String> rxids = headers.get(HEADER_RXID);
		if (rxids != null && rxids.size() > 0) {
			return rxids.get(0);
		}
		return null;
	}
	
	/*
	public String getRXIDFromHeaders(Map<String, List<String>> headers) {
		if (headers == null) {
			return null;
		}
		List<String> cookies = headers.get(SET_COOKIE);
		String rxid = getRXIDFromSetCookies(cookies);
		if (StringUtils.isBlank(rxid)) {
			cookies = headers.get(SET_COOKIE_LOWER);
			rxid = getRXIDFromSetCookies(cookies);
		}
		return rxid;
	}

	private String getRXIDFromSetCookies(List<String> cookies) {
		String rxid = null;
		if (cookies != null) {
			for (String cookie : cookies) {
				if (cookie.startsWith(COOKIE_RXID_PREFIX)) {
					int idx = cookie.indexOf(";");
					if (idx == -1) {
						idx = cookie.length();
					}
					rxid = cookie.substring(COOKIE_RXID_PREFIX_LEN, idx);
					// ダブルクォーテーションを除去
					rxid = StringUtils.trimDoubleQuotes(rxid);
					break;
				}
			}
		}
		return rxid;
	}
	*/

	/**
	 * created範囲チェック
	 * @param rxid RXID文字列
	 * @param beforeMinute 現在時刻から過去の有効範囲(分). 過去の有効範囲を指定しない場合はnull.
	 * @param afterMinute 現在時刻から未来の有効範囲(分). 未来の有効範囲を指定しない場合はnull.
	 * @return 指定された範囲内の場合true, 範囲外の場合false.
	 */
	public boolean checkCreatedFromRXID(String rxid, Integer beforeMinute, 
			Integer afterMinute) {
		return checkCreatedFromRXID(rxid, new Date(), beforeMinute, afterMinute);
	}

	/**
	 * created範囲チェック
	 * @param rxid RXID文字列
	 * @param now 現在時刻
	 * @param beforeMinute 現在時刻から過去の有効範囲(分). 過去の有効範囲を指定しない場合はnull.
	 * @param afterMinute 現在時刻から未来の有効範囲(分). 未来の有効範囲を指定しない場合はnull.
	 * @return 指定された範囲内の場合true, 範囲外の場合false.
	 */
	public boolean checkCreatedFromRXID(String rxid, Date now, Integer beforeMinute, 
			Integer afterMinute) {
		WsseAuth auth = parseRXID(rxid);
		return checkCreated(auth, now, beforeMinute, afterMinute);
	}

	/**
	 * created範囲チェック
	 * @param auth WsseAuth情報
	 * @param beforeMinute 現在時刻から過去の有効範囲(分). 過去の有効範囲を指定しない場合はnull.
	 * @param afterMinute 現在時刻から未来の有効範囲(分). 未来の有効範囲を指定しない場合はnull.
	 * @return 指定された範囲内の場合true, 範囲外の場合false.
	 */
	public boolean checkCreated(WsseAuth auth, Integer beforeMinute, 
			Integer afterMinute) {
		return checkCreated(auth, new Date(), beforeMinute, afterMinute);
	}

	/**
	 * created範囲チェック
	 * @param auth WsseAuth情報
	 * @param now 現在時刻
	 * @param beforeMinute 現在時刻から過去の有効範囲(分). 過去の有効範囲を指定しない場合はnull.
	 * @param afterMinute 現在時刻から未来の有効範囲(分). 未来の有効範囲を指定しない場合はnull.
	 * @return 指定された範囲内の場合true, 範囲外の場合false.
	 */
	public boolean checkCreated(WsseAuth auth, Date now, Integer beforeMinute, 
			Integer afterMinute) {
		if (auth == null) {
			return false;
		}
		return checkCreated(auth.created, now, beforeMinute, afterMinute);
	}
	
	/**
	 * created範囲チェック
	 * @param created created文字列
	 * @param beforeMinute 現在時刻から過去の有効範囲(分). 過去の有効範囲を指定しない場合はnull.
	 * @param afterMinute 現在時刻から未来の有効範囲(分). 未来の有効範囲を指定しない場合はnull.
	 * @return 指定された範囲内の場合true, 範囲外の場合false.
	 */
	public boolean checkCreated(String created, Integer beforeMinute, 
			Integer afterMinute) {
		return checkCreated(created, new Date(), beforeMinute, afterMinute);
	}

	/**
	 * created範囲チェック
	 * @param created created文字列
	 * @param now 現在時刻
	 * @param beforeMinute 現在時刻から過去の有効範囲(分). 過去の有効範囲を指定しない場合はnull.
	 * @param afterMinute 現在時刻から未来の有効範囲(分). 未来の有効範囲を指定しない場合はnull.
	 * @return 指定された範囲内の場合true, 範囲外の場合false.
	 */
	public boolean checkCreated(String created, Date now, Integer beforeMinute, 
			Integer afterMinute) {
		if (created == null) {
			return false;
		}
		try {
			// 範囲チェック
			Date createdDate = DateUtil.getDate(created);
			boolean checkBefore = true;
			boolean checkAfter = true;
			if (beforeMinute != null) {
				Date nowBefore5min = DateUtil.addTime(now, 0, 0, 0, 0, 0 - beforeMinute, 0, 0);
				checkBefore = createdDate.after(nowBefore5min);
			}
			if (afterMinute != null) {
				Date nowAfter5min = DateUtil.addTime(now, 0, 0, 0, 0, afterMinute, 0, 0);
				checkAfter = createdDate.before(nowAfter5min);
			}
			return checkBefore && checkAfter;
			
		} catch (ParseException e) {
			logger.info("ParseException : " + e.getMessage());
		}
		return false;
	}

}
