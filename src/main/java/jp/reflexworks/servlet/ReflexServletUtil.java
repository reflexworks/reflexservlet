package jp.reflexworks.servlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.reflexworks.servlet.exception.InvokeException;
import jp.reflexworks.servlet.util.HeaderUtil;
import jp.sourceforge.reflex.IResourceMapper;
import jp.sourceforge.reflex.exception.JSONException;
import jp.sourceforge.reflex.exception.ReflexXMLException;
import jp.sourceforge.reflex.util.DeflateUtil;
import jp.sourceforge.reflex.util.FileUtil;
import jp.sourceforge.reflex.util.StringUtils;

/**
 * Reflex サーブレットユーティリティ.
 * <p>
 * 以下の機能を備えています。
 * <ul>
 * <li>JSONまたはXMLのPOSTデータをオブジェクトに変換 (getEntityメソッド)。</li>
 * <li>オブジェクトをJSONまたはXMLに変換してレスポンスデータに設定 (doResponseメソッド)</li>
 * </ul>
 * </p>
 */
public class ReflexServletUtil implements ReflexServletConst {

	/** Reqest Header : X-Requested-With */
	private static final String X_REQUESTED_WITH_LOWER =
			X_REQUESTED_WITH.toLowerCase(Locale.ENGLISH);

	/** ロガー */
	private static Logger logger = Logger.getLogger(ReflexServletUtil.class.getName());

	/**
	 * リクエストデータ取得
	 * @param req HttpServletRequest
	 * @param rxmapper IResourceMapper
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public static Object getEntity(HttpServletRequest req, IResourceMapper rxmapper)
	throws IOException, JSONException, ReflexXMLException, ClassNotFoundException {
		// リクエストデータ受信
		InputStream inputStream = req.getInputStream();
		Object result = null;

		// Content-typeで判断
		String contentType = req.getContentType();
		if (contentType != null && contentType.startsWith(CONTENT_TYPE_MESSAGEPACK)) {
			// MessagePack形式
			try {
				result = rxmapper.fromMessagePack(inputStream);
			} finally {
				try {
					inputStream.close();
				} catch (Exception re) {
					logger.log(Level.WARNING, "[close error] " + re.getClass().getName(), re);
				}
			}
			return result;
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING));

			// Content-typeで判断
			if (contentType != null) {
				if (contentType.startsWith(CONTENT_TYPE_JSON)) {
					result = rxmapper.fromJSON(reader);
				} else if (contentType.startsWith(CONTENT_TYPE_XML)) {
					result = rxmapper.fromXML(reader);
				}
			}

			if (result == null) {
				String body = getBody(reader);

				if (body != null && body.length() > 0) {
					boolean useJson = true;
					if (body.startsWith("<")) {
						useJson = false;
					}
					result = getEntity(body, rxmapper, useJson);
				}
			}

		} finally {
			try {
				reader.close();
			} catch (Exception re) {
				logger.log(Level.WARNING, "[close error] " + re.getClass().getName(), re);
			}
		}

		return result;
	}

	/**
	 * リクエストデータ取得
	 * @param body XMLまたはJSON文字列
	 * @param rxmapper IResourceMapper
	 * @param useJson true:JSON、false:XML
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public static Object getEntity(String body, IResourceMapper rxmapper, boolean useJson)
	throws IOException, JSONException, ReflexXMLException, ClassNotFoundException {
		Object result = null;
		boolean changeObj = false;

		if (body != null) {
			int equalIdx = body.indexOf("=");
			if (equalIdx > 0 && equalIdx < body.length()) {
				String item = body.substring(0, equalIdx).toLowerCase(Locale.ENGLISH);
				if (JSON.equals(item) && useJson) {
					result = rxmapper.fromJSON(body.substring(equalIdx + 1));
					changeObj = true;
				} else if (XML.equals(item)) {
					result = rxmapper.fromXML(body.substring(equalIdx + 1));
					changeObj = true;
				}
			}
			if (!changeObj) {
				if (body.length() > 0) {
					char firstChar = body.charAt(0);
					if (firstChar == '{' && useJson) {
						result = rxmapper.fromJSON(body);
					} else {
						result = rxmapper.fromXML(body);
					}
				}
			}
		}

		return result;
	}

	/**
	 * リクエストデータの文字列を取得します
	 * @param req HttpServletRequest
	 * @return リクエストデータ文字列
	 */
	public static String getBody(HttpServletRequest req) throws IOException {
		// リクエストデータ受信
		InputStream inputStream = req.getInputStream();
		String body = null;

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING));
			body = getBody(reader);

		} finally {
			try {
				reader.close();
			} catch (Exception re) {
				logger.log(Level.WARNING, "[close error] " + re.getClass().getName(), re);
			}
		}

		return body;
	}

	/**
	 * BufferedReaderを読み、Stringにして返却します
	 * @param b BufferedReader
	 * @return Bufferから読み込んだ文字列
	 */
	public static String getBody(BufferedReader b) throws IOException {
		StringBuilder sb = new StringBuilder();
		String str;
		boolean isFirst = true;
		while ((str = b.readLine()) != null) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(NEWLINE);
			}
			sb.append(str);
		}
		return sb.toString();
	}

	/**
	 * レスポンス出力
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param format 1:XML, 2:JSON, 3:MessagePack
	 * @param rxmapper ResourceMapper
	 * @param deflateUtil DeflateUtil
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param isGZip GZIP形式にする場合true
	 * @param isStrict XMLの名前空間を出力する場合true
	 * @param isDisableDeflate MessagePackをDeflate圧縮しない場合true
	 * @param isNoCache ブラウザにキャッシュしない設定をレスポンスヘッダに指定する場合true
	 * @param isSameOrigin SameOrigin指定をする場合true
	 */
	public static void doResponse(HttpServletRequest req, HttpServletResponse resp,
			Object entities, int format, IResourceMapper rxmapper,
			DeflateUtil deflateUtil, int statusCode, boolean isGZip, boolean isStrict,
			boolean isNoCache, boolean isSameOrigin)
	throws IOException {
		doResponse(req, resp, entities, format, rxmapper, deflateUtil, statusCode,
				isGZip, isStrict, isNoCache, isSameOrigin, null);
	}

	/**
	 * レスポンス出力
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param format 1:XML, 2:JSON, 3:MessagePack
	 * @param rxmapper ResourceMapper
	 * @param deflateUtil DeflateUtil
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param isGZip GZIP形式にする場合true
	 * @param isStrict XMLの名前空間を出力する場合true
	 * @param isNoCache ブラウザにキャッシュしない設定をレスポンスヘッダに指定する場合true
	 * @param isSameOrigin 「X-Frame-Options: SAMEORIGIN」レスポンスヘッダを指定する場合true
	 * @param contentType Content-Type
	 */
	public static void doResponse(HttpServletRequest req, HttpServletResponse resp,
			Object entities, int format, IResourceMapper rxmapper,
			DeflateUtil deflateUtil, int statusCode, boolean isGZip, boolean isStrict,
			boolean isNoCache, boolean isSameOrigin, String contentType)
	throws IOException {
		boolean isRespGZip = isGZip && isGZip(req);

		boolean isDeflate = isSetHeader(req, HEADER_ACCEPT_ENCODING,
				HEADER_VALUE_DEFLATE);

		// ステータスコードの設定
		resp.setStatus(statusCode);
		// Content-Typeが指定されている場合は設定
		if (!StringUtils.isBlank(contentType)) {
			resp.setContentType(contentType);
		}

		// ブラウザにキャッシュしない場合
		if (isNoCache) {
			resp.addHeader(PRAGMA, NO_CACHE);
			resp.addHeader(CACHE_CONTROL, CACHE_CONTROL_VALUE);
			resp.addHeader(EXPIRES, PAST_DATE);
		}
		// SAMEORIGIN指定 (クリックジャッキング対策)
		if (isSameOrigin) {
			resp.addHeader(HEADER_FRAME_OPTIONS, SAMEORIGIN);
		}
		// ブラウザのクロスサイトスクリプティングのフィルタ機能を使用
		resp.addHeader(HEADER_XSS_PROTECTION, HEADER_XSS_PROTECTION_MODEBLOCK);
		// HTTPレスポンス全体を検査（sniffing）してコンテンツ タイプを判断し、「Content-Type」を無視した動作を行うことを防止する。(IE対策)
		resp.addHeader(HEADER_CONTENT_TYPE_OPTIONS, HEADER_CONTENT_TYPE_OPTIONS_NOSNIFF);

		// status=204 の場合はコンテントを返却しない。
		if (entities == null || statusCode == HttpStatus.SC_NO_CONTENT) {
			return;
		}

		// MessagePackまたはバイト配列の場合は、PrintWriterでなくOutputStreamを使用する。
		if ((!(entities instanceof String) && format == FORMAT_MESSAGEPACK) ||
				entities instanceof byte[]) {
			// MessagePack
			if (format == FORMAT_MESSAGEPACK && StringUtils.isBlank(contentType)) {
				resp.setContentType(CONTENT_TYPE_MESSAGEPACK);
			}

			OutputStream out = null;
			try {
				// Deflateの場合はGZip圧縮しない。
				if (isDeflate) {
					resp.setHeader(HEADER_CONTENT_ENCODING, HEADER_VALUE_DEFLATE);
					out = resp.getOutputStream();
				} else if (isRespGZip) {
					setGZipHeader(resp);
					out = new GZIPOutputStream(resp.getOutputStream());
				} else {
					out = resp.getOutputStream();
				}

				byte[] respData = null;
				if (entities instanceof byte[]) {
					// バイト配列データ
					respData = (byte[])entities;
				} else {
					// 一旦MessagePack形式にし、deflate圧縮したものをレスポンスする。
					byte[] msgData = rxmapper.toMessagePack(entities);
					if (isDeflate) {
						// Deflate圧縮
						if (deflateUtil != null) {
							respData = deflateUtil.deflate(msgData);
						} else {
							respData = DeflateUtil.deflateOneTime(msgData);
						}
					} else {
						// Deflateなし
						respData = msgData;
					}
				}

				if (respData != null && respData.length > 0) {
					out.write(respData);
				}

			} finally {
				try {
					out.close();
				} catch (Exception e) {
					logger.log(Level.WARNING, "[close error] " + e.getClass().getName(), e);
				}
			}

		} else {
			// レスポンスデータ出力
			PrintWriter prtout = null;
			try {
				OutputStream out = null;
				if (isRespGZip) {
					setGZipHeader(resp);
					out = new GZIPOutputStream(resp.getOutputStream());
				} else {
					out = resp.getOutputStream();
				}
				prtout = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
						out, ENCODING)));

				if (entities instanceof String) {
					// 文字列
					prtout.write((String)entities);

				} else if (format == FORMAT_JSON) {
					// JSON
					if (StringUtils.isBlank(contentType)) {
						resp.setContentType(CONTENT_TYPE_REFLEX_JSON);
						//resp.addHeader(HEADER_CONTENT_TYPE_OPTIONS,
						//		HEADER_CONTENT_TYPE_OPTIONS_NOSNIFF);	// 全体に指定
					}

					// JSON中身
					if (entities != null) {
						rxmapper.toJSON(entities, prtout);
					}

				} else {
					// XMLヘッダー出力
					if (StringUtils.isBlank(contentType)) {
						resp.setContentType(CONTENT_TYPE_REFLEX_XML);
					}

					// XML
					if (entities != null) {
						prtout.print(XMLHEAD);
						rxmapper.toXML(entities, prtout);
					}
				}

			} finally {
				if (prtout != null) {
					try {
						prtout.close();
					} catch (Exception e) {
						logger.log(Level.WARNING, "[close error] " + e.getClass().getName(), e);
					}
				}
			}
		}
	}

	/**
	 * HTMLレスポンス出力
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param html HTML
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param isGZip GZIP形式にする場合true
	 * @param isNoCache ブラウザにキャッシュしない場合true
	 * @param isSameOrigin SameOrigin指定をする場合true
	 */
	public static void doHtmlPage(HttpServletRequest req, HttpServletResponse resp,
			String html, int statusCode, boolean isGZip, boolean isNoCache,
			boolean isSameOrigin)
	throws IOException {
		doResponse(req, resp, html, 0, null, null, statusCode, isGZip,
				false, isNoCache, isSameOrigin, CONTENT_TYPE_HTML_CHARSET);
	}

	/**
	 * エラーページ出力.
	 * <p>
	 * ReflexWorksのデフォルトエラーページを出力します.
	 * </p>
	 * @param resp HttpServletResponse
	 * @param exception 例外オブジェクト
	 */
	public static void doErrorPage(HttpServletResponse resp, Throwable exception)
	throws IOException {

		int httpStatus = SC_INTERNAL_SERVER_ERROR;
		if (exception instanceof InvokeException) {
			httpStatus = ((InvokeException)exception).getHttpStatus();
		}

		if (resp.containsHeader(HEADER_CONTENT_ENCODING)) {
			resp.setHeader(HEADER_CONTENT_ENCODING, null);
		}

		OutputStream out = null;
		out = resp.getOutputStream();

		// レスポンスデータ出力
		PrintWriter prtout = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, ENCODING)));

		resp.setContentType(CONTENT_TYPE_HTML_CHARSET);

		prtout.print("<html>");
		prtout.print(NEWLINE);
		prtout.print("<head>");
		prtout.print(NEWLINE);
		prtout.print("<title>");
		prtout.print(NEWLINE);
		prtout.print("[ReflexContainer] Error Report ");
		prtout.print("</title>");
		prtout.print(NEWLINE);
		prtout.print("</head>");
		prtout.print(NEWLINE);
		prtout.print("<body>");
		prtout.print(NEWLINE);

		prtout.print("<p align=\"center\"><a href=\"http://www.virtual-tech.net/\"><img src=\"");
		prtout.print(REFLEX_LOGOS);
		prtout.print("\"></img></a></p>");

		prtout.print("<hr/>");

		prtout.print("<font size=\"5\">");
		prtout.print("<b>");

		prtout.print("HTTP Status : ");
		prtout.print(httpStatus);

		prtout.print("</b>");
		prtout.print("</font>");

		if (exception.getMessage() != null) {
			prtout.print("<br>");
			prtout.print("<br>");
			prtout.print(NEWLINE);
			prtout.print("Message : ");
			prtout.print(exception.getMessage());
		}

		prtout.print("<br>");
		prtout.print("<br>");
		prtout.print(NEWLINE);

		prtout.print("Exception Detail : ");
		prtout.print("<br>");
		prtout.print("<br>");
		prtout.print(NEWLINE);

		prtout.print(HTML_BLANK);
		prtout.print(HTML_BLANK);
		prtout.print(HTML_BLANK);
		prtout.print(HTML_BLANK);
		prtout.print(exception.toString());

		StackTraceElement[] stackTraceElement = exception.getStackTrace();
		if (stackTraceElement != null) {
			for (int i = 0; i < stackTraceElement.length; i++) {
				prtout.print("<br>");
				prtout.print(NEWLINE);
				prtout.print(HTML_BLANK);
				prtout.print(HTML_BLANK);
				prtout.print(HTML_BLANK);
				prtout.print(HTML_BLANK);
				prtout.print(HTML_BLANK);
				prtout.print(HTML_BLANK);
				prtout.print(HTML_BLANK);
				prtout.print(HTML_BLANK);
				prtout.print(stackTraceElement[i].toString());
			}
		}

		prtout.print("<br>");
		prtout.print("<br>");
		prtout.print(NEWLINE);

		prtout.print("<hr/>");
		prtout.print(NEWLINE);

		prtout.print("<font size=\"5\">");
		prtout.print("<SPAN style='Arial;font-size:38%;vertical;color:#B2B2B2'>");
		prtout.print("<b>");
		prtout.print(REFLEX_SIGNATURE);
		prtout.print("</b>");
		prtout.print("</font>");

		prtout.print("<br>");
		prtout.print("<br>");
		prtout.print(NEWLINE);

		prtout.print("</body>");
		prtout.print(NEWLINE);
		prtout.print("</html>");

		try {
			prtout.flush();
			out.flush();
			out.close();
		} catch (Exception re) {
			logger.log(Level.WARNING, "[close error] " + re.getClass().getName(), re);
		}
	}

	/**
	 * レスポンス出力（ファイル用）
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 */
	public static void doResponseFile(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {

		String reqFileTemp = "";

		reqFileTemp = req.getPathInfo();

		if (reqFileTemp.indexOf(REFLEX_LOGOS) >= 0) {
			reqFileTemp = REFLEX_LOGOS;
		}

		if ("".equals(reqFileTemp)) {
			reqFileTemp = DEFAULT_PAGE;
		}

		String reqFilePath = null;
		reqFilePath = FileUtil.getResourceUrl(reqFileTemp);
		URL url = new URL(reqFilePath);

		InputStream in = url.openStream();

		try {
			String contentType = null;
			// Content-Typeの設定
			String type = reqFileTemp.substring(reqFileTemp.indexOf(".") + 1).toLowerCase(Locale.ENGLISH);
			if ("html".equals(type) || "htm".equals(type)) {
				contentType = "text/html;charset=" + ENCODING;
			} else if ("xml".equals(type)) {
				contentType = "application/xml;charset=" + ENCODING;
			} else if ("xsl".equals(type)) {
				contentType = "text/xsl;charset=" + ENCODING;
			} else if ("css".equals(type)) {
				contentType = "text/css;charset=" + ENCODING;
			} else if ("js".equals(type)) {
				contentType = "application/javascript;charset=" + ENCODING;
			} else if ("png".equals(type)) {
				contentType = "image/png";
			} else if ("jpeg".equals(type)) {
				contentType = "image/jpeg";
			} else if ("jpg".equals(type)) {
				contentType = "image/jpeg";
			} else if ("gif".equals(type)) {
				contentType = "image/gif";
			}

			setResponseFile(req, resp, in, contentType);

		} finally {
			try {
				in.close();
			} catch (Exception e) {
				logger.log(Level.WARNING, "[close error] " + e.getClass().getName(), e);
			}
		}
	}

	/**
	 * InputStreamから読み込んだデータをResponseに設定
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param in InputStream
	 * @param contentType content-type
	 */
	public static void setResponseFile(HttpServletRequest req, HttpServletResponse resp,
			InputStream in, String contentType)
	throws IOException {
		OutputStream out = null;

		try {
			if (contentType != null) {
				resp.setContentType(contentType);
			}

			if (isGZip(req)) {
				setGZipHeader(resp);
				out = new GZIPOutputStream(resp.getOutputStream());
			} else {
				out = resp.getOutputStream();
			}

			byte[] buffer = new byte[4096];

			int size;
			while ((size = in.read(buffer)) != -1) {
				out.write(buffer, 0, size);
			}
		} finally {
			try {
				out.close();
			} catch (Exception e) {
				logger.log(Level.WARNING, "[close error] " + e.getClass().getName(), e);
			}
		}
	}

	/**
	 * Requestがgzip対応かどうかを判定します
	 * @param req
	 * @return true:gzip対応、false:gzip対応でない
	 */
	public static boolean isGZip(HttpServletRequest req) {
		boolean ret = false;
		if (req != null) {
			String acceptedEncodings = req.getHeader(HEADER_ACCEPT_ENCODING);	//クライアントの受理可能エンコーディング
			if (acceptedEncodings == null || acceptedEncodings.length() == 0) {
				acceptedEncodings = req.getHeader(HEADER_ACCEPT_ENCODING_LOWERCASE);
			}
			return HeaderUtil.containsHeader(acceptedEncodings, HEADER_VALUE_GZIP);
		}
		return ret;
	}

	/**
	 * GZip圧縮のレスポンスヘッダを設定します
	 * @param resp
	 */
	public static void setGZipHeader(HttpServletResponse resp) {
		resp.setHeader(HEADER_CONTENT_ENCODING, HEADER_VALUE_GZIP);
	}

	/**
	 * ExceptionのStackTraceをカンマでつなげます
	 * @param exception
	 * @return 編集した文字列
	 */
	public static String errorString(Throwable exception) {
		return errorString(exception, "  ;  ");
	}

	/**
	 * ExceptionのStackTraceを指定文字でつなげます
	 * @param exception
	 * @param s 指定文字
	 * @return 編集した文字列
	 */
	public static String errorString(Throwable exception, String s) {
		StringBuilder eStr = new StringBuilder();
		StackTraceElement[] stackTraceElement = exception.getStackTrace();
		if (stackTraceElement != null && stackTraceElement.length > 0) {
			eStr.append(stackTraceElement[0].toString());
			for (int i = 1; i < stackTraceElement.length; i++) {
				eStr.append(s);
				eStr.append(stackTraceElement[i].toString());
			}
		}
		return eStr.toString();
	}

	/**
	 * URIに指定された拡張子を取得します
	 * @param req HttpServletRequest
	 * @return 拡張子。ない場合はnull。
	 */
	public static String getSuffix(HttpServletRequest req) {
		String suffix = null;
		String uri = req.getRequestURI();
		int suffixIdx = uri.lastIndexOf(".");
		if (suffixIdx > 0) {
			int slashIdx = uri.lastIndexOf("/");
			if (suffixIdx > slashIdx) {
				suffix = uri.substring(suffixIdx + 1).toLowerCase(Locale.ENGLISH);
			}
		}
		return suffix;
	}

	/**
	 * useJsonフラグをformat区分に変換します.
	 * @param useJson JSON形式を使用する場合true
	 * @return 1:XML, 2:JSON, 3:MessagePack
	 */
	public static int convertFormatType(boolean useJson) {
		if (useJson) {
			return FORMAT_JSON;
		} else {
			return FORMAT_XML;
		}
	}

	/**
	 * Content-Typeからformat区分を生成します.
	 * @param req リクエスト
	 * @return 0:String, 1:XML, 2:JSON, 3:MessagePack, 4:multipart/form-data, -1:ContentTypeからでは判定不能
	 */
	public static int getFormat(HttpServletRequest req) {
		String contentType = req.getContentType();
		if (contentType != null) {
			if (contentType.startsWith(CONTENT_TYPE_MESSAGEPACK)) {
				return FORMAT_MESSAGEPACK;
			} else if (contentType.startsWith(CONTENT_TYPE_JSON)) {
				return FORMAT_JSON;
			} else if (contentType.startsWith(CONTENT_TYPE_XML)) {
				return FORMAT_XML;
			} else if (contentType.startsWith(CONTENT_TYPE_MULTIPART_FORMDATA)) {
				return FORMAT_MULTIPART_FORMDATA;
			} else if (contentType.startsWith(CONTENT_TYPE_TEXT)) {
				return FORMAT_TEXT;
			}
		}
		return -1;
	}

	/**
	 * formatがMessagePack形式かどうかチェックする.
	 * @param format 0:String, 1:XML, 2:JSON, 3:MessagePack
	 * @return MessagePack形式の場合true
	 */
	public static boolean isMessagePack(int format) {
		if (format == FORMAT_MESSAGEPACK) {
			return true;
		}
		return false;
	}

	/**
	 * formatがXML形式かどうかチェックする.
	 * @param format 0:String, 1:XML, 2:JSON, 3:MessagePack
	 * @return XML形式の場合true
	 */
	public static boolean isXML(int format) {
		if (format == FORMAT_XML) {
			return true;
		}
		return false;
	}

	/**
	 * formatがJSON形式かどうかチェックする.
	 * @param format 0:String, 1:XML, 2:JSON, 3:MessagePack
	 * @return JSON形式の場合true
	 */
	public static boolean isJSON(int format) {
		if (format == FORMAT_JSON) {
			return true;
		}
		return false;
	}

	/**
	 * formatがファイルアップロードを表すかどうかチェックする.
	 * @param format 0:String, 1:XML, 2:JSON, 3:MessagePack, 4:multipart/form-data
	 * @return ファイルアップロードの場合true
	 */
	public static boolean isFileUpload(int format) {
		if (format == FORMAT_MULTIPART_FORMDATA) {
			return true;
		}
		return false;
	}

	/**
	 * X-Requested-WithヘッダがXMLHttpRequestとなっているかどうかチェックする.
	 * @param req リクエスト
	 * @return X-Requested-WithヘッダがXMLHttpRequestとなっている場合true
	 */
	public static boolean isXMLHttpRequest(HttpServletRequest req) {
		if (req != null) {
			String requestedWith = req.getHeader(X_REQUESTED_WITH);
			if (requestedWith == null) {
				requestedWith = req.getHeader(X_REQUESTED_WITH_LOWER);
			}
			if (X_REQUESTED_WITH_WHR.equals(requestedWith)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * X-Requested-Withヘッダに値が設定されているかどうかチェックする.
	 * @param req リクエスト
	 * @return X-Requested-Withヘッダに値が設定されている場合true
	 */
	public static boolean hasXRequestedWith(HttpServletRequest req) {
		if (req != null) {
			String requestedWith = req.getHeader(X_REQUESTED_WITH);
			if (requestedWith == null) {
				requestedWith = req.getHeader(X_REQUESTED_WITH_LOWER);
			}
			return !StringUtils.isBlank(requestedWith);
		}
		return false;
	}

	/**
	 * リクエストヘッダから指定されたキーの値のうち、先頭が指定された文字列と等しい値を返却します。
	 * @param req リクエスト
	 * @param key リクエストヘッダのキー
	 * @param valuePrefix リクエストヘッダの値の先頭文字列
	 * @return リクエストヘッダのうち、指定されたキー・先頭文字列に合致した値。
	 *         複数存在する場合は最初の1件を返却します。
	 */
	public static String getHeaderValue(HttpServletRequest req,
			String key, String valuePrefix) {
		if (req == null || StringUtils.isBlank(key)) {
			return null;
		}
		valuePrefix = StringUtils.null2blank(valuePrefix);
		Enumeration<String> enu = req.getHeaders(key);
		if (enu != null) {
			while (enu.hasMoreElements()) {
				String value = enu.nextElement();
				if (value != null && value.startsWith(valuePrefix)) {
					return value;
				}
			}
		}
		return null;
	}

	/**
	 * リクエストヘッダのうち、指定されたキーの値に指定された値が含まれているかどうかチェックする.
	 * @param req リクエスト
	 * @param key キー
	 * @param val 値
	 * @return リクエストヘッダに指定されたキーの値がある場合true
	 */
	public static boolean isSetHeader(HttpServletRequest req, String key, String val) {
		if (req == null || StringUtils.isBlank(key) || StringUtils.isBlank(val)) {
			return false;
		}
		Enumeration<String> enu = req.getHeaders(key);
		if (enu != null) {
			while (enu.hasMoreElements()) {
				String tmpValStr = enu.nextElement();
				String[] tmpVals = tmpValStr.split(HEADER_VALUE_DELIMITER);
				for (String tmpVal : tmpVals) {
					if (val.equals(StringUtils.trim(tmpVal))) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
