package jp.reflexworks.servlet.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.sourceforge.reflex.util.DateUtil;
import jp.sourceforge.reflex.util.SHA1;
import jp.sourceforge.reflex.util.SHA256;
import jp.sourceforge.reflex.util.StringUtils;
import jp.sourceforge.reflex.util.URLDecoderPlus;

/**
 * WSSE情報の取得、編集を行うクラス
 */
public class WsseUtil extends AuthTokenUtil {

	private Logger logger = Logger.getLogger(this.getClass().getName());

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
		WsseAuth auth = getWsseAuth(req);
		
		// 認証チェック
		return checkAuth(auth, password, apiKey);
	}
	
	/**
	 * リクエストからWSSE認証情報を取り出します
	 * @param req リクエスト
	 * @return WSSE認証情報
	 */
	public WsseAuth getWsseAuth(HttpServletRequest req) {
		// ヘッダから認証情報を取得
		WsseAuth auth = getWsseAuthFromHeader(req);
		if (auth == null) {
			// URLパラメータから認証情報を取得
			auth = parseWSSEparam(req);
		}
		if (auth == null) {
			// Cookieから認証情報を取得
			auth = parseWSSEcookie(req);
		}
		
		return auth;
	}

	/**
	 * リクエストヘッダからWSSE認証情報を取り出します
	 * @param header リクエストヘッダに指定されたWSSE文字列
	 * @return WSSE認証情報
	 */
	public WsseAuth getWsseAuthFromHeader(HttpServletRequest req) {
		String value = getWSSEValue(req);
		if (!StringUtils.isBlank(value)) {
			WsseAuth auth = parseWSSEheader(value);
			// 空の場合はブランクオブジェクトを作成して返却する。
			if (auth == null) {
				auth = createBlankWsseAuth(false);
			}
			return auth;
		}
		String rxid = getRXIDValueByHeader(req);
		if (!StringUtils.isBlank(rxid)) {
			WsseAuth auth = parseRXID(rxid);
			if (auth == null) {
				auth = createBlankWsseAuth(true);
			}
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
		String rxid = getRXIDValueByParam(req);
		if (rxid != null) {
			auth = parseRXID(rxid);
			if (auth == null) {
				// 空の場合はブランクオブジェクトを作成して返却する。
				auth = createBlankWsseAuth(true);
			}
			auth.isQueryString = true;
		}
		return auth;
	}

	/**
	 * CookieからWSSE認証情報を取り出します
	 * @param req リクエスト
	 * @return WSSE認証情報
	 */
	public WsseAuth parseWSSEcookie(HttpServletRequest req) {
		WsseAuth auth = null;
		Cookie cookie = null;
		
		// RXID
		if (auth == null) {
			cookie = getCookie(req, RXID_LEGACY);
			if (cookie != null) {
				String value = cookie.getValue();
				auth = parseRXID(value);
				if (auth == null) {
					// 空の場合はブランクオブジェクトを作成して返却する。
					auth = createBlankWsseAuth(true);
				}
				auth.isCookie = true;
			}
		}
		
		return auth;
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

	/**
	 * WSSE認証チェック.
	 * ハッシュ関数はSHA-256を使用します。
	 * @param auth WSSE認証情報
	 * @param password パスワード
	 * @param apiKey APIKey
	 * @return WSSE認証OKの場合true、エラーの場合false
	 */
	public boolean checkAuth(WsseAuth auth, String password, String apiKey) {
		return checkAuth(auth, password, apiKey, false);
	}

	/**
	 * WSSE認証チェック
	 * @param auth WSSE認証情報
	 * @param password パスワード
	 * @param apiKey APIKey
	 * @param isSha1 ハッシュ関数にSHA-1を使用する場合true
	 * @return WSSE認証OKの場合true、エラーの場合false
	 */
	public boolean checkAuth(WsseAuth auth, String password, String apiKey,
			boolean isSha1) {
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

			//MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
			//md.update(v);
			//
			//digestを比較
			//byte[] mdDigestB = md.digest();
			
			//digestを比較
			byte[] mdDigestB = null;
			if (isSha1) {
				mdDigestB = SHA1.hash(v);
			} else {
				mdDigestB = SHA256.hash(v);
			}
			boolean isEqual = MessageDigest.isEqual(mdDigestB, digestB);
			if (isEqual) {
				auth.password = password;	// Wsseオブジェクトにpasswordを設定
			}
			return isEqual;

		//} catch (NoSuchAlgorithmException e) {
		//	logger.log(Level.WARNING, e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		return false;
	}
	
	/**
	 * URLフェッチ実行時、レスポンスヘッダに設定されたRXIDを取得します.
	 * @param headers レスポンスヘッダ
	 * @return RXID
	 */
	public String getRXIDFromHeaders(Map<String, List<String>> headers) {
		if (headers == null) {
			return null;
		}
		List<String> rxids = headers.get(HEADER_AUTHORIZATION);
		if (rxids != null && !rxids.isEmpty()) {
			String rxid = getRXIDValue(rxids);
			return extractRXID(rxid);
		}
		return null;
	}
	
	/**
	 * リクエストからWSSE文字列を取得.
	 * @param req リクエスト
	 * @return WSSE文字列
	 */
	public String getWSSEValue(HttpServletRequest req) {
		String value = req.getHeader(WSSE);
		if (StringUtils.isBlank(value)) {
			value = req.getHeader(WSSE_UPPER_LOWER);
		}
		if (StringUtils.isBlank(value)) {
			value = req.getHeader(WSSE_LOWER);
		}
		return value;
	}
	
	/**
	 * リクエストヘッダからRXID文字列を取得.
	 * @param req リクエスト
	 * @return RXID
	 */
	public String getRXIDValueByHeader(HttpServletRequest req) {
		String value = getRXIDValue(req.getHeaders(HEADER_AUTHORIZATION));
		return extractRXID(value);
	}
	
	/**
	 * リクエストのURLパラメータからRXIDを取得.
	 * @param req リクエスト
	 * @return RXID
	 */
	public String getRXIDValueByParam(HttpServletRequest req) {
		String rxid = URLDecoderPlus.urlDecode(req.getParameter(RXID));
		if (rxid == null) {
			// 旧フォーマットも受け付ける。
			rxid = URLDecoderPlus.urlDecode(req.getParameter(RXID_LEGACY));
		}
		return rxid;
	}
	
	/**
	 * リクエストのCookieからRXIDを取得.
	 * @param req リクエスト
	 * @return RXID
	 */
	public String getRXIDValueByCookie(HttpServletRequest req) {
		Cookie cookie = getCookie(req, RXID_LEGACY);
		if (cookie != null) {
			return cookie.getValue();
		}
		return null;
	}
	
	private String getRXIDValue(Enumeration<String> values) {
		if (values != null) {
			while (values.hasMoreElements()) {
				String value = values.nextElement();
				String tmp = getRXIDValue(value);
				if (tmp != null) {
					return tmp;
				}
			}
		}
		return null;
	}
	
	private String getRXIDValue(List<String> values) {
		if (values != null) {
			for (String value : values) {
				String tmp = getRXIDValue(value);
				if (tmp != null) {
					return tmp;
				}
			}
		}
		return null;
	}
	
	private String getRXIDValue(String value) {
		if (value != null && value.startsWith(HEADER_AUTHORIZATION_RXID)) {
			return value;
		}
		return null;
	}
	
	/**
	 * レスポンスヘッダにRXIDを設定します.
	 * <p>
	 * Authorization: Token {RXID}
	 * </p>
	 * @param resp レスポンス
	 * @param rxid RXID
	 */
	public void addRXIDToHeader(HttpServletResponse resp, String rxid) {
		if (resp == null || StringUtils.isBlank(rxid)) {
			return;
		}
		resp.addHeader(HEADER_X_RXID, rxid);
	}

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
	
	/**
	 * WSSEおよびRXIDがパースできなかった場合に使用。
	 * @param isRxid RXIDの場合true
	 * @return 空のWSSE情報
	 */
	private WsseAuth createBlankWsseAuth(boolean isRxid) {
		WsseAuth auth = new WsseAuth(null, null, null, null);
		auth.isRxid = isRxid;
		return auth;
	}

}
