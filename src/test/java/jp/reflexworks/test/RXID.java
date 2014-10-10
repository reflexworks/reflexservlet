package jp.reflexworks.test;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.reflexworks.servlet.util.WsseAuth;
import jp.reflexworks.servlet.util.WsseUtil;
import jp.sourceforge.reflex.util.DateUtil;

public class RXID {

	public static void main(String[] args) {

		String username = "testuser-x-y-z@example-mail.com";
		//String username = "super@$%-_.z";
		//String username = "";
		//String username = "noriko.terada@gmail.com";
		String password = "passwordhashXXXXX";
		//String password = "";
		//String password = "noriko2010";
		String apiKey = "ApiKey12345X";
		String serviceName = "testservice";
		
		WsseUtil wsseUtil = new WsseUtil();

		try {
			String wsse = wsseUtil.createWsseHeaderValue(username, password);
	
			System.out.println("wsse=" + wsse);
	
			// 新RXIDのチェック
			WsseAuth auth = wsseUtil.parseWSSEheader(wsse);
			String rxid = getRXIDString(auth);
			WsseAuth wsseauth = parseRXID(rxid);
			
			boolean checkauth = wsseUtil.checkAuth(wsseauth, password, apiKey);
			
			boolean checkauth256 = wsseUtil.checkAuth(auth, password, apiKey);
			
			System.out.println("rxid=" + rxid);
			System.out.println(wsseauth.toString());
			System.out.println("checkauth=" + checkauth);
			System.out.println("checkauth-256=" + checkauth256);
	
			// 旧RXIDのチェック -> 旧で無くなった
			// APIKey対応のためcheck=falseになる。
			String rxid2 = WsseUtil.getRXIDString(wsseauth);
			WsseAuth wsseauth2 = WsseUtil.parseRXID(rxid2);
			boolean checkauth2 = wsseUtil.checkAuth(wsseauth2, password, apiKey);

			System.out.println("[WsseUtil]");
			System.out.println("rxid=" + rxid2);
			System.out.println(wsseauth2.toString());
			System.out.println("checkauth=" + checkauth2);
			
			// RXIDはAPIKey付きで作成する必要がある。
			System.out.println("--- RXID with APIKey ---");
			System.out.println("username=" + username + ", password=" + password + ", apiKey=" + apiKey);
			String rxid3 = wsseUtil.createRXIDString(username, password, serviceName, apiKey);
			System.out.println("rxid=" + rxid3);
			WsseAuth wsseauth3 = wsseUtil.parseRXID(rxid3);
			System.out.println(wsseauth3.toString());
			boolean checkauth3 = wsseUtil.checkAuth(wsseauth3, password, apiKey);
			System.out.println("checkauth=" + checkauth3);
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * WSSEからRXIDに変換
	 * @param wsseauth
	 * @return RXID
	 * @throws ParseException
	 * @throws UnsupportedEncodingException
	 */
	public static String getRXIDString(WsseAuth wsseauth) throws ParseException, UnsupportedEncodingException {
		return getDateTimeOfRXID(wsseauth.created)+"-"+wsseauth.nonce.replace("=","")+"-"+wsseauth.passwordDigest.replace("=","")+"-"+rot13(wsseauth.username);
	}
	
	/**
	 * RXIDからWsseAuthに変換
	 * @param value
	 * @return WsseAuth
	 * @throws UnsupportedEncodingException
	 * @throws ParseException
	 */
	public static WsseAuth parseRXID(String value) throws UnsupportedEncodingException, ParseException {

		int p1 = value.indexOf("-");
		if (p1<0) {
			// 旧rxid
			return WsseUtil.parseRXID(value);
		}else {
		
		int p2 = p1+value.substring(p1+1).indexOf("-")+1;
		int p3 = p2+value.substring(p2+1).indexOf("-")+1;

		String createdStr = getDateTimeOfWSSE(value.substring(0,p1));
		String nonceStr = value.substring(p1+1,p2);
		String passwordDigestStr = value.substring(p2+1,p3); 
		String username = rot13(value.substring(p3+1)); 

		WsseAuth wsseauth = new WsseAuth(username, passwordDigestStr, nonceStr, createdStr);
		//wsseauth.isOnetime = true;
		return wsseauth;
		}
	}

	/**
	 * RXIDのcreatedをWSSE用("yyyy-MM-dd'T'HH:mm:ss+99:99"形式の文字列)に変換します
	 * @param rxidcreated
	 * @return dateの文字列
	 * @throws ParseException 
	 */
	private static String getDateTimeOfWSSE(String rxidcreated) throws ParseException {
		/*
		String dateStr = rxidcreated.replace("P", "+").replace("M", "-")+"00";
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssZ");
		Date date = format.parse(dateStr);
		return DateUtil.getDateTime(date);
		*/
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
			//TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
			//format.setTimeZone(timeZone);
			dateStr += "00";
			Date date = format.parse(dateStr);
			return DateUtil.getDateTime(date, timeZoneId);
		}
		return null;
	}

	/**
	 * WSSEのcreatedをRXID用("yyyyMMddHHmmssP99"形式の文字列)に変換します
	 * @param wssecreated
	 * @return dateの文字列
	 * @throws ParseException 
	 */
	private static String getDateTimeOfRXID(String wssecreated) throws ParseException {
		Date date = DateUtil.getDate(wssecreated);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String datestring = format.format(date);
		format = new SimpleDateFormat("Z");
		String zone = format.format(date);
		datestring += zone.substring(0, 3).replace("+", "P").replace("-", "M");
		return datestring;
	}

	/**
	 * rotate13(簡易暗号化)
	 * @param s
	 * @return 暗号化文字列
	 */
	private static String rot13(String s) {

		StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if       (c >= 'a' && c <= 'm') c += 13;
            else if  (c >= 'A' && c <= 'M') c += 13;
            else if  (c >= 'n' && c <= 'z') c -= 13;
            else if  (c >= 'N' && c <= 'Z') c -= 13;
            sb.append(c);
        }
        return sb.toString();
	}
}
