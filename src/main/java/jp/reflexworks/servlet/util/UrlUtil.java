package jp.reflexworks.servlet.util;

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

}
