package jp.reflexworks.servlet.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jp.reflexworks.servlet.ReflexServletConst;
import jp.sourceforge.reflex.util.StringUtils;

/**
 * URL編集ユーティリティ.
 */
public class UrlUtil {

	/** リクエストヘッダ : X-Header-Forwarded-For */
	public static final String HEADER_FORWARDED_FOR = ReflexServletConst.HEADER_FORWARDED_FOR;
	/** リクエストヘッダ : x-header-forwarded-for */
	public static final String HEADER_FORWARDED_FOR_LOWER = HEADER_FORWARDED_FOR.toLowerCase(Locale.ENGLISH);

	/** ロガー. */
	private static Logger logger = LoggerFactory.getLogger(UrlUtil.class);

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
	 * @param isURLEncode QueryStringの値をURLエンコードする場合true
	 * @return PathInfoとQueryString文字列
	 */
	public static String editPathInfoQuery(HttpServletRequest req,
			Set<String> ignoreParams, Map<String, String> addingParams,
			boolean isURLEncode) {
		String pathInfo = req.getPathInfo();
		StringBuilder sb = new StringBuilder();
		if (!isURLEncode || StringUtils.isBlank(pathInfo) || "/".equals(pathInfo)) {
			sb.append(pathInfo);
		} else {
			String[] uriParts = pathInfo.split("\\/");
			int len = uriParts.length;
			for (int i = 1; i < len; i++) {	// 添字1から
				sb.append("/");
				sb.append(urlEncode(uriParts[i]));
			}
		}
		sb.append(editQueryString(req, ignoreParams, addingParams, isURLEncode));
		return sb.toString();
	}

	/**
	 * リクエストパラメータを編集し、PathInfoとQueryString文字列を作成します.
	 * @param pathInfoQuery PathInfoとQueryString
	 * @param ignoreParams 除去するパラメータ
	 * @param addingParams 追加するパラメータ
	 * @param isURLEncode QueryStringの値をURLエンコードする場合true
	 * @return PathInfoとQueryString文字列
	 */
	public static String editPathInfoQuery(String pathInfoQuery,
			Set<String> ignoreParams, Map<String, String> addingParams,
			boolean isURLEncode) {
		// pathInfoとQueryStringに分ける
		String pathInfo = "";
		String tmpQueryString = "";
		if (pathInfoQuery != null) {
			int idx = pathInfoQuery.indexOf("?");
			if (idx > -1) {
				pathInfo = pathInfoQuery.substring(0, idx);
				tmpQueryString = pathInfoQuery.substring(idx + 1);
			} else {
				pathInfo = pathInfoQuery;
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append(pathInfo);
		sb.append(editQueryString(tmpQueryString, ignoreParams, addingParams, isURLEncode));
		return sb.toString();
	}

	/**
	 * QueryStringを組み立てます.
	 * @param req リクエスト
	 * @param ignoreParams QueryStringから除去するキーリスト
	 * @param addingParams QueryStringに加えるキーと値のリスト
	 * @param isURLEncode QueryStringの値をURLエンコードする場合true
	 */
	public static String editQueryString(HttpServletRequest req,
			Set<String> ignoreParams, Map<String, String> addingParams,
			boolean isURLEncode) {
		if (req == null) {
			return null;
		}
		return editQueryString(req.getQueryString(), ignoreParams, addingParams, isURLEncode);
	}

	/**
	 * QueryStringを組み立てます.
	 * @param queryString クエリ文字列
	 * @param ignoreParams QueryStringから除去するキーリスト
	 * @param addingParams QueryStringに加えるキーと値のリスト
	 * @param isURLEncode QueryStringの値をURLエンコードする場合true
	 */
	public static String editQueryString(String queryString,
			Set<String> ignoreParams, Map<String, String> addingParams,
			boolean isURLEncode) {
		boolean isFirst = true;
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isBlank(queryString)) {
			String[] queryStringParts = null;
			if (!StringUtils.isBlank(queryString)) {
				queryStringParts = queryString.split("&");
				for (String queryStringPart : queryStringParts) {
					int idx = queryStringPart.indexOf("=");
					String name = null;
					String value = null;
					if (idx > 0) {
						name = queryStringPart.substring(0, idx);
						value = queryStringPart.substring(idx + 1);
					} else {
						name = queryStringPart;
					}
					if (ignoreParams == null || !ignoreParams.contains(name)) {
						if (isFirst) {
							sb.append("?");
							isFirst = false;
						} else {
							sb.append("&");
						}
						sb.append(urlEncode(name, isURLEncode));
						if (value != null) {
							sb.append("=");
							sb.append(urlEncode(value, isURLEncode));
						}
					}
				}
			}
		}

		if (addingParams != null && !addingParams.isEmpty()) {
			for (Map.Entry<String, String> mapEntry : addingParams.entrySet()) {
				String key = mapEntry.getKey();
				String value = mapEntry.getValue();
				if (isFirst) {
					sb.append("?");
					isFirst = false;
				} else {
					sb.append("&");
				}
				sb.append(urlEncode(key, isURLEncode));
				if (!StringUtils.isBlank(value)) {
					sb.append("=");
					sb.append(urlEncode(value, isURLEncode));
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

	/**
	 * URLからホスト名を取得.
	 * ポート番号がついている場合は除く。
	 * @param url URL
	 * @return ホスト名(ポート番号除く)
	 */
	public static String getHostWithoutPort(String url) {
		String urlAndPort = getHost(url);
		if (StringUtils.isBlank(urlAndPort)) {
			return urlAndPort;
		}
		int idx = urlAndPort.indexOf(":");
		if (idx > -1) {
			return urlAndPort.substring(0, idx);
		} else {
			return urlAndPort;
		}
	}

	/**
	 * X-Forwarded-Forリクエストヘッダから、LastForwarded IPを取得.
	 * @param forwardedFor X-Forwarded-Forの値
	 * @return LastForwarded IP
	 */
	public static String getLastForwarded(HttpServletRequest req) {
		String forwardedFor1 = req.getHeader(HEADER_FORWARDED_FOR);
		String forwardedFor2 = req.getHeader(HEADER_FORWARDED_FOR_LOWER);
		String ip = null;
		if (forwardedFor1 != null) {
			ip = getLastForwarded(forwardedFor1);
		} else if (forwardedFor2 != null) {
			ip = getLastForwarded(forwardedFor2);
		} else {
			ip = req.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * X-Forwarded-Forリクエストヘッダから、LastForwarded IPを取得.
	 * @param forwardedFor X-Forwarded-Forの値
	 * @return LastForwarded IP
	 */
	public static String getLastForwarded(String forwardedFor) {
		if (StringUtils.isBlank(forwardedFor)) {
			return null;
		}
		String[] parts = forwardedFor.split(",");
		return parts[parts.length - 1].trim();
	}

	/**
	 * URLエンコード
	 * @param str 文字列
	 * @return URLエンコードした文字列
	 */
	public static String urlEncode(String str) {
		if (StringUtils.isBlank(str)) {
			return str;
		}
		try {
			return URLEncoder.encode(str, ReflexServletConst.ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.warn("[urlEncode] UnsupportedEncodingException: " + e.getMessage());
		}
		return str;	// そのまま返す
	}

	/**
	 * URLデコード
	 * @param str 文字列
	 * @return URLデコードした文字列
	 */
	public static String urlDecode(String str) {
		if (StringUtils.isBlank(str)) {
			return str;
		}
		try {
			return URLDecoder.decode(str, ReflexServletConst.ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.warn("[urlDecode] UnsupportedEncodingException: " + e.getMessage());
		}
		return str;	// そのまま返す
	}

	/**
	 * URLエンコード指定がある場合、URLエンコードを行い返却する.
	 * @param value 値
	 * @param isURIEncode URLエンコードを行う場合true
	 * @return 編集した値
	 */
	private static String urlEncode(String value, boolean isURIEncode) {
		if (isURIEncode) {
			return urlEncode(value);
		} else {
			return value;
		}
	}

	/**
	 * PathInfo + QueryString文字列をURLエンコードする.
	 * PathInfoの/、QueryStringの?,&,=はエンコードせず、その他の値をエンコードする。
	 * @param pathInfoQuery PathInfo + QueryString
	 * @return URLエンコードしたPathInfo + QueryString
	 */
	public static String urlEncodePathInfoQuery(String pathInfoQuery) {
		if (StringUtils.isBlank(pathInfoQuery)) {
			return pathInfoQuery;
		}
		// まずはPathInfoとQueryStringを分割
		int len = pathInfoQuery.length();
		int idxPathInfoEnd = pathInfoQuery.indexOf("?");
		if (idxPathInfoEnd == -1) {
			idxPathInfoEnd = len;
		}
		String pathInfo = pathInfoQuery.substring(0, idxPathInfoEnd);
		String queryString = null;
		int idxPathInfoEnd1 = idxPathInfoEnd + 1;
		if (len > idxPathInfoEnd1) {
			queryString = pathInfoQuery.substring(idxPathInfoEnd1);
		}

		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isBlank(pathInfo)) {
			// PathInfoをURLエンコード
			// PathInfoが"/"のみの場合、そのままとする。
			if ("/".equals(pathInfo)) {
				sb.append(pathInfo);
			} else {
				String[] pathInfoParts = pathInfo.split("\\/");
				boolean isFirst = true;
				for (String pathInfoPart : pathInfoParts) {
					if (isFirst) {
						isFirst = false;
					} else {
						sb.append("/");
					}
					sb.append(urlEncode(pathInfoPart));
				}
			}
		}
		if (!StringUtils.isBlank(queryString)) {
			sb.append("?");
			// QueryStringをURLエンコード
			String[] paramParts = queryString.split("&");
			boolean isFirst = true;
			for (String paramPart : paramParts) {
				if (isFirst) {
					isFirst = false;
				} else {
					sb.append("&");
				}
				int idx = paramPart.indexOf("=");
				String key = null;
				String val = null;
				if (idx == -1) {
					key = paramPart;
				} else {
					key = paramPart.substring(0, idx);
					val = paramPart.substring(idx + 1);
				}
				sb.append(urlEncode(key));
				if (!StringUtils.isBlank(val)) {
					sb.append("=");
					sb.append(urlEncode(val));
				}
			}
		}
		return sb.toString();
	}

}
