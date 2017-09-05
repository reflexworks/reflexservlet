package jp.reflexworks.test;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.servlet.http.Part;

import org.junit.Test;

import jp.reflexworks.servlet.util.UrlUtil;
import jp.reflexworks.test.servlet.TestRequest;

/**
 * URLユーティリティのテスト.
 */
public class UrlUtilTest {
	
	/**
	 * テスト
	 */
	@Test
	public void test() {
		
		System.out.println("--- UrlUtilTest start ---");
		
		String method = "GET";
		String pathInfoQuery = "/mytest?title=test";
		Map<String, String> headers = null;
		byte[] payload = null;
		List<Part> parts = null;
		
		TestRequest req = new TestRequest(method, pathInfoQuery, headers, payload, parts);
		String urlToServletPath = UrlUtil.getFromSchemaToServletPath(req);
		System.out.println("url = " + req.getRequestURL() + " , pathInfoQuery = " + pathInfoQuery + " , urlToServletPath = " + urlToServletPath);
		String urlToContextPath = UrlUtil.getFromSchemaToContextPath(req);
		System.out.println("url = " + req.getRequestURL() + " , pathInfoQuery = " + pathInfoQuery + " , urlToContextPath = " + urlToContextPath);
		
	}

}
