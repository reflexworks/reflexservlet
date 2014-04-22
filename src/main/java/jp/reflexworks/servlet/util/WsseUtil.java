package jp.reflexworks.servlet.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import jp.sourceforge.reflex.util.DateUtil;
import jp.sourceforge.reflex.util.StringUtils;

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
		
		return auth;
	}

	/**
	 * リクエストヘッダからWSSE認証情報を取り出します
	 * @param header リクエストヘッダに指定されたWSSE文字列
	 * @return WSSE認証情報
	 */
	public WsseAuth getWsseAuthFromHeader(HttpServletRequest req) {
		String value = req.getHeader(WSSE);
		if (!StringUtils.isBlank(value)) {
			return parseWSSEheader(value);
		}
		//value = req.getHeader(HEADER_AUTHORIZATION);
		value = getTokenValue(req.getHeaders(HEADER_AUTHORIZATION));
		if (value != null) {
			String rxid = extractRXID(value);
			if (!StringUtils.isBlank(rxid)) {
				return parseRXID(rxid);
			}
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
			auth = parseRXID(rxid);
		}
		return auth;
	}
	
	/**
	 * RXID文字列からWSSE認証情報を作成します
	 * @param value RXID文字列
	 * @param isRxid ワンタイム利用かどうか
	 * @return WSSE認証情報
	 */
	public WsseAuth parseRXID(String value) {
		WsseAuth auth = null;
		if (value != null) {
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
					}
				} catch (ParseException e) {}	// Do nothing.
			}
		}
		
		if (auth != null) {
			auth.isRxid = true;
		}
		return auth;
	}

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
		if (rxids != null && rxids.size() > 0) {
			//String rxid = rxids.get(0);
			String rxid = getTokenValue(rxids);
			return extractRXID(rxid);
		}
		return null;
	}
	
	private String getTokenValue(Enumeration values) {
		if (values != null) {
			while (values.hasMoreElements()) {
				String value = (String)values.nextElement();
				String tmp = getTokenValue(value);
				if (tmp != null) {
					return tmp;
				}
			}
		}
		return null;
	}
	
	private String getTokenValue(List<String> values) {
		if (values != null) {
			for (String value : values) {
				String tmp = getTokenValue(value);
				if (tmp != null) {
					return tmp;
				}
			}
		}
		return null;
	}
	
	private String getTokenValue(String value) {
		if (value != null && value.startsWith(HEADER_AUTHORIZATION_TOKEN)) {
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
		String headerValue = editRXIDHeader(rxid);
		resp.addHeader(HEADER_AUTHORIZATION, headerValue);
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

}
