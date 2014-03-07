package jp.reflexworks.test;

import static org.junit.Assert.*;

import org.junit.Test;

import jp.reflexworks.servlet.util.AuthTokenUtil;
import jp.reflexworks.servlet.util.WsseUtil;
import jp.reflexworks.servlet.util.WsseAuth;

public class AuthTokenTest {

	@Test
	public void testCreateRxid() {
		
		String username = "vtec";
		String password = "38jv7j3u";
		String apiKey = "apikey9083ybhr98a3h2biop";
		
		// RXID作成
		String rxid = AuthTokenUtil.createRXIDString(username, password, apiKey);
		
		System.out.println(rxid);
		
		// 認証チェック
		WsseUtil wsseUtil = new WsseUtil();
		WsseAuth wsseAuth = wsseUtil.parseRXID(rxid);
		assertTrue(wsseUtil.checkAuth(wsseAuth, password, apiKey));
	}

}
