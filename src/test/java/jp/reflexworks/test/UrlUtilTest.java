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
	public void test1() {

		System.out.println("--- UrlUtilTest (1) start ---");

		String method = "GET";
		String pathInfoQuery = "/mytestマイテスト?titleタイトル=testテスト";
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

		String editQueryString = UrlUtil.editQueryString(req, ignoreParams, addingParams, false);
		System.out.println("queryString = " + req.getQueryString() + " , edited = " + editQueryString);

		ignoreParams = new HashSet<String>();
		ignoreParams.add("_pagination");
		ignoreParams.add("x");
		ignoreParams.add("m");
		ignoreParams.add("p");
		ignoreParams.add("n");
		ignoreParams.add("_RXID");
		addingParams = new HashMap<String, String>();

		String queryString = "item.size=L&item.color=greenグリーン&_pagination=5&m&l=200&p=74yf74hbuhfh";
		editQueryString = UrlUtil.editQueryString(queryString, ignoreParams, addingParams, false);
		System.out.println("queryString = " + queryString + " , edited = " + editQueryString);

		queryString = "_RXID=xxxxx&item.size=L&item.color=greenグリーン&_pagination=5&m&l=200&p=74yf74hbuhfh";
		editQueryString = UrlUtil.editQueryString(queryString, ignoreParams, addingParams, false);
		System.out.println("queryString = " + queryString + " , edited = " + editQueryString);

		System.out.println("--- UrlUtilTest (1) end ---");

	}

	/**
	 * テスト
	 */
	@Test
	public void test2() {

		System.out.println("--- UrlUtilTest (2) start ---");

		String method = "GET";
		String pathInfoQuery = "/mytestマイテスト/ＡＢＣ?titleタイトル=testテスト";
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

		String editQueryString = UrlUtil.editQueryString(req, ignoreParams, addingParams, true);
		System.out.println("queryString = " + req.getQueryString() + " , edited = " + editQueryString);

		// editPathInfoQuery
		String editPathInfoQuery = UrlUtil.editPathInfoQuery(req, ignoreParams, addingParams, true);
		System.out.println("pathInfoQuery = " + pathInfoQuery + " , edited = " + editPathInfoQuery);

		ignoreParams = new HashSet<String>();
		ignoreParams.add("_pagination");
		ignoreParams.add("x");
		ignoreParams.add("m");
		ignoreParams.add("p");
		ignoreParams.add("n");
		ignoreParams.add("_RXID");
		addingParams = new HashMap<String, String>();

		String queryString = "item.size=L&item.color=greenグリーン&_pagination=5&m&l=200&p=74yf74hbuhfh";
		editQueryString = UrlUtil.editQueryString(queryString, ignoreParams, addingParams, true);
		System.out.println("queryString = " + queryString + " , edited = " + editQueryString);

		queryString = "_RXID=xxxxx&item.size=L&item.color=greenグリーン&_pagination=5&m&l=200&p=74yf74hbuhfh";
		editQueryString = UrlUtil.editQueryString(queryString, ignoreParams, addingParams, true);
		System.out.println("queryString = " + queryString + " , edited = " + editQueryString);

		System.out.println("--- UrlUtilTest (2) end ---");

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
		String hostWithoutPort = UrlUtil.getHostWithoutPort(url);
		System.out.println("url = " + url + ", hostWithoutPort = " + hostWithoutPort);
		assertEquals(hostWithoutPort, "localhost");

		url = "vte.cx/vt";
		host = UrlUtil.getHost(url);
		System.out.println("url = " + url + ", host = " + host);
		System.out.println("url = " + url + ", urlHost = " + HeaderUtil.getURLHost(url));
		assertEquals(host, "vte.cx");

		url = "localhost:8080";
		host = UrlUtil.getHost(url);
		System.out.println("url = " + url + ", host = " + host);
		assertEquals(host, "localhost:8080");
		hostWithoutPort = UrlUtil.getHostWithoutPort(url);
		System.out.println("url = " + url + ", hostWithoutPort = " + hostWithoutPort);
		assertEquals(hostWithoutPort, "localhost");

		System.out.println("--- testGetHost end ---");
	}

	/**
	 * テスト
	 */
	@Test
	public void testEditQuery() {

		System.out.println("--- UrlUtilTest (testEditQuery) start ---");

		Set<String> ignoreParams = new HashSet<>();
		ignoreParams.add("s");

		String pathInfoQuery = "/今日のnews?article.text-ft-天気&s=date";
		System.out.println("before = " + pathInfoQuery);
		String result = UrlUtil.editPathInfoQuery(pathInfoQuery, ignoreParams, null, false);
		System.out.println(" after = " + result);
		System.out.println("------");

		pathInfoQuery = "/昨日ののnews?s=date";
		System.out.println("before = " + pathInfoQuery);
		result = UrlUtil.editPathInfoQuery(pathInfoQuery, ignoreParams, null, false);
		System.out.println(" after = " + result);
		System.out.println("------");

		pathInfoQuery = "?article.text-ft-天気&s=date";
		System.out.println("before = " + pathInfoQuery);
		result = UrlUtil.editPathInfoQuery(pathInfoQuery, ignoreParams, null, false);
		System.out.println(" after = " + result);
		System.out.println("------");

		pathInfoQuery = "?article.text-ft-天気&s=date&article.tag-eq-晴れ";
		System.out.println("before = " + pathInfoQuery);
		result = UrlUtil.editPathInfoQuery(pathInfoQuery, ignoreParams, null, false);
		System.out.println(" after = " + result);
		System.out.println("------");

		pathInfoQuery = "?s=date";
		System.out.println("before = " + pathInfoQuery);
		result = UrlUtil.editPathInfoQuery(pathInfoQuery, ignoreParams, null, false);
		System.out.println(" after = " + result);
		System.out.println("------");

		pathInfoQuery = "/昨日ののnews";
		System.out.println("before = " + pathInfoQuery);
		result = UrlUtil.editPathInfoQuery(pathInfoQuery, ignoreParams, null, false);
		System.out.println(" after = " + result);
		System.out.println("------");

		pathInfoQuery = null;
		System.out.println("before = " + pathInfoQuery);
		result = UrlUtil.editPathInfoQuery(pathInfoQuery, ignoreParams, null, false);
		System.out.println(" after = " + result);
		System.out.println("------");

		System.out.println("--- UrlUtilTest (testEditQuery) end ---");

	}

}
