package jp.reflexworks.servlet.util;

import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import jp.sourceforge.reflex.util.StringUtils;

/**
 * URL編集ユーティリティ.
 */
public class UrlUtil {
	
	/**
	 * リクエストのスキーマからサーブレットパスまでを取得.
	 * @param req リクエスト
	 * @return リクエストのスキーマからサーブレットパスまで
	 */
	public static String getFromSchemaToServletPath(HttpServletRequest req) {
		if (req == null) {
			return null;
		}
		// getRequestURLはプロトコルからPathInfoまで。QueryStringは取得されない。
		String url = req.getRequestURL().toString();
		String pathInfo = req.getPathInfo();
		if (pathInfo != null) {
			int pathInfoLen = pathInfo.length();
			if (pathInfoLen > 0) {
				return url.substring(0, url.length() - pathInfoLen);
			}
		}
		return url;
	}
	
	/**
	 * リクエストのスキーマからコンテキストパスまで取得
	 * @param req リクエスト
	 * @return リクエストのスキーマからコンテキストパスまで
	 */
	public static String getFromSchemaToContextPath(HttpServletRequest req) {
		if (req == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(req.getScheme());
		sb.append("://");
		sb.append(getFromServerToContextPath(req));
		return sb.toString();
	}
	
	/**
	 * リクエストのサーバ名からコンテキストパスまで取得
	 * @param req リクエスト
	 * @return リクエストのサーバ名からコンテキストパスまで
	 */
	public static String getFromServerToContextPath(HttpServletRequest req) {
		if (req == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(req.getServerName());
		int port = req.getServerPort();
		if (port != 0 && port != 80) {
			sb.append(":");
			sb.append(port);
		}
		sb.append(req.getContextPath());
		return sb.toString();
	}

	/**
	 * PathInfo + QueryString文字列に、指定されたパラメータを設定します.
	 * @param pathInfoQuery PathInfo + Query
	 * @param key パラメータのキー
	 * @param val パラメータの値
	 * @return PathInfo + QueryString文字列に、指定されたパラメータを追加した文字列
	 */
	public static String addParam(String pathInfoQuery, String key, String val) {
		if (StringUtils.isBlank(key)) {
			return pathInfoQuery;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(pathInfoQuery);
		if (pathInfoQuery.indexOf("?") > -1) {
			sb.append("&");
		} else {
			sb.append("?");
		}
		sb.append(key);
		if (!StringUtils.isBlank(val)) {
			sb.append("=");
			sb.append(val);
		}
		return sb.toString();
	}
	
	/**
	 * リクエストパラメータを編集し、PathInfoとQueryString文字列を作成します.
	 * @param req リクエスト
	 * @param ignoreParams 除去するパラメータ
	 * @param addingParams 追加するパラメータ
	 * @return PathInfoとQueryString文字列
	 */
	public static String editPathInfoQuery(HttpServletRequest req, 
			Set<String> ignoreParams, Map<String, String> addingParams) {
		StringBuilder sb = new StringBuilder();
		sb.append(req.getPathInfo());
		sb.append(editQueryString(req, ignoreParams, addingParams));
		return sb.toString();
	}
	
	/**
	 * QueryStringを組み立てます.
	 * @param req リクエスト
	 * @param ignoreParams QueryStringから除去するキーリスト
	 * @param addingParams QueryStringに加えるキーと値のリスト
	 */
	public static String editQueryString(HttpServletRequest req, 
			Set<String> ignoreParams, Map<String, String> addingParams) {
		StringBuilder sb = new StringBuilder();
		if (ignoreParams == null || ignoreParams.size() == 0) {
			String queryString = req.getQueryString();
			if (queryString != null) {
				sb.append("?");
				sb.append(queryString);
			}
		} else {
			Enumeration<String> names = req.getParameterNames();
			boolean isFirst = true;
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				if (!ignoreParams.contains(name)) {
					if (isFirst) {
						sb.append("?");
						isFirst = false;
					} else {
						sb.append("&");
					}
					sb.append(name);
					sb.append("=");
					sb.append(req.getParameter(name));
				}
			}
		}
		
		if (addingParams != null) {
			boolean isFirst = false;
			if (sb.toString().indexOf("?") == -1) {
				isFirst = true;
			}
			for (Map.Entry<String, String> mapEntry : addingParams.entrySet()) {
				String key = mapEntry.getKey();
				String value = mapEntry.getValue();
				if (isFirst) {
					sb.append("?");
					isFirst = false;
				} else {
					sb.append("&");
				}
				sb.append(key);
				if (!StringUtils.isBlank(value)) {
					sb.append("=");
					sb.append(value);
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * リクエストの RequestURL + QueryString を取得.
	 * @param req リクエスト
	 * @return RequestURL + QueryString
	 */
	public static String getRequestURLWithQueryString(HttpServletRequest req) {
		StringBuilder url = new StringBuilder();
		url.append(req.getRequestURL());
		String queryString = req.getQueryString();
		if (!StringUtils.isBlank(queryString)) {
			url.append("?");
			url.append(queryString);
		}
		return url.toString();
	}
	
	/**
	 * リクエストの RequestURI + QueryString を取得.
	 * @param req リクエスト
	 * @return RequestURI + QueryString
	 */
	public static String getRequestURIWithQueryString(HttpServletRequest req) {
		StringBuilder url = new StringBuilder();
		url.append(req.getRequestURI());
		String queryString = req.getQueryString();
		if (!StringUtils.isBlank(queryString)) {
			url.append("?");
			url.append(queryString);
		}
		return url.toString();
	}
	
	/**
	 * PathInfo + QueryString 文字列から、PathInfo部分を抽出.
	 * 文字列の?以前の部分のみ返します。
	 * @param pathInfoQuery PathInfo + QueryString
	 * @return PathInfo
	 */
	public static String getPathInfo(String pathInfoQuery) {
		if (!StringUtils.isBlank(pathInfoQuery)) {
			int idx = pathInfoQuery.indexOf("?");
			if (idx > -1) {
				return pathInfoQuery.substring(0, idx);
			}
		}
		return pathInfoQuery;
	}
	
	/**
	 * URLからホスト名を取得.
	 * @param url URL
	 * @return ホスト名(ポート番号含む)
	 */
	public static String getHost(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		}
		int start = url.indexOf("://");
		if (start == -1) {
			start = 0;
		} else {
			start = start + 3;
		}
		int end = url.indexOf("/", start);
		if (end == -1) {
			end = url.length();
		}
		return url.substring(start, end);
	}

}
