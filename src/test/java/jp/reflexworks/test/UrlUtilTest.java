package jp.reflexworks.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Part;

import static org.junit.Assert.*;
import org.junit.Test;

import jp.reflexworks.servlet.util.HeaderUtil;
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
		
		// editQueryString
		Set<String> ignoreParams = null;
		Map<String, String> addingParams = null;
		
		String editQueryString = UrlUtil.editQueryString(req, ignoreParams, addingParams);
		System.out.println("queryString = " + req.getQueryString() + " , edited = " + editQueryString);
		
		ignoreParams = new HashSet<String>();
		ignoreParams.add("_pagination");
		ignoreParams.add("x");
		ignoreParams.add("m");
		ignoreParams.add("p");
		ignoreParams.add("n");
		ignoreParams.add("_RXID");
		addingParams = new HashMap<String, String>();
		
		String queryString = "item.size=L&item.color=green&_pagination=5&m&l=200&p=74yf74hbuhfh";
		editQueryString = UrlUtil.editQueryString(queryString, ignoreParams, addingParams);
		System.out.println("queryString = " + queryString + " , edited = " + editQueryString);
		
		queryString = "_RXID=xxxxx&item.size=L&item.color=green&_pagination=5&m&l=200&p=74yf74hbuhfh";
		editQueryString = UrlUtil.editQueryString(queryString, ignoreParams, addingParams);
		System.out.println("queryString = " + queryString + " , edited = " + editQueryString);
		
		
	}
	
	/**
	 * テスト
	 */
	@Test
	public void testGetHost() {
		
		System.out.println("--- testGetHost start ---");
		
		String url = null;
		String host = null;
		
		url = "https://vte.cx";
		host = UrlUtil.getHost(url);
		System.out.println("url = " + url + ", host = " + host);
		System.out.println("url = " + url + ", urlHost = " + HeaderUtil.getURLHost(url));
		assertEquals(host, "vte.cx");
		
		url = "https://vte.cx/d";
		host = UrlUtil.getHost(url);
		System.out.println("url = " + url + ", host = " + host);
		System.out.println("url = " + url + ", urlHost = " + HeaderUtil.getURLHost(url));
		assertEquals(host, "vte.cx");
		
		url = "http://localhost:8080";
		host = UrlUtil.getHost(url);
		System.out.println("url = " + url + ", host = " + host);
		System.out.println("url = " + url + ", urlHost = " + HeaderUtil.getURLHost(url));
		assertEquals(host, "localhost:8080");
		
		url = "http://localhost:8080/index.html";
		host = UrlUtil.getHost(url);
		System.out.println("url = " + url + ", host = " + host);
		System.out.println("url = " + url + ", urlHost = " + HeaderUtil.getURLHost(url));
		assertEquals(host, "localhost:8080");
		
		url = "vte.cx/vt";
		host = UrlUtil.getHost(url);
		System.out.println("url = " + url + ", host = " + host);
		System.out.println("url = " + url + ", urlHost = " + HeaderUtil.getURLHost(url));
		assertEquals(host, "vte.cx");
		
		System.out.println("--- testGetHost end ---");
	}

}
