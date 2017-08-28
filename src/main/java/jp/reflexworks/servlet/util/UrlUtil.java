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

}
