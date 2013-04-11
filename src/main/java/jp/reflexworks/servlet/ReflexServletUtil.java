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
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import jp.reflexworks.servlet.exception.InvokeException;
import jp.reflexworks.servlet.util.HttpStatus;
import jp.sourceforge.reflex.IResourceMapper;
import jp.sourceforge.reflex.core.ResourceMapper;
import jp.sourceforge.reflex.util.FileUtil;

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

	/**
	 * リクエストデータ取得
	 * @param req HttpServletRequest
	 * @param model_package modelのパッケージ
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getEntity(HttpServletRequest req, String model_package) 
	throws IOException, JSONException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		return this.getEntity(req, rxmapper);
	}
	
	/**
	 * リクエストデータ取得
	 * @param req HttpServletRequest
	 * @param model_package modelのパッケージ
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getEntity(HttpServletRequest req, Map<String, String> model_package) 
	throws IOException, JSONException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		return this.getEntity(req, rxmapper);
	}

	/**
	 * リクエストデータ取得
	 * @param req HttpServletRequest
	 * @param rxmapper IResourceMapper
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getEntity(HttpServletRequest req, IResourceMapper rxmapper) 
	throws IOException, JSONException {
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
				inputStream.close();
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
				// 何もしない
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
	public Object getEntity(String body, IResourceMapper rxmapper, boolean useJson) 
	throws IOException, JSONException {
		Object result = null;
		boolean changeObj = false;

		if (body != null) {
			int equalIdx = body.indexOf("=");
			if (equalIdx > 0 && equalIdx < body.length()) {
				String item = body.substring(0, equalIdx).toLowerCase();
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
					//if (contenttype.toLowerCase().indexOf("javascript") >= 0) {
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
	 * リクエストデータ取得
	 * ストリームから直接XMLを読み取り、オブジェクトに変換します
	 * @param req HttpServletRequest
	 * @param rxmapper ResourceMapper
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getXmlEntity(HttpServletRequest req, IResourceMapper rxmapper) 
	throws IOException, JSONException {
		BufferedReader in = null;
		Object result = null;
		try {
			in = new BufferedReader(new InputStreamReader(req.getInputStream()));
			result = rxmapper.fromXML(in);

		} finally {
			if (in != null) {
				in.close();
			}
		}
		return result;
	}

	/**
	 * リクエストデータの文字列を取得します
	 * @param req HttpServletRequest
	 * @return リクエストデータ文字列
	 */
	public String getBody(HttpServletRequest req) throws IOException, JSONException {
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
				// 何もしない
			}
		}

		return body;
	}

	/**
	 * BufferedReaderを読み、Stringにして返却します
	 * @param b BufferedReader
	 * @return Bufferから読み込んだ文字列
	 */
	public String getBody(BufferedReader b) throws IOException {
		StringBuffer sb = new StringBuffer();
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
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param callback callback関数
	 * @param isGZip GZIP形式にする場合true
	 * @param isStrict XMLの名前空間を出力する場合true
	 */
	public void doResponse(HttpServletRequest req, HttpServletResponse resp, 
			Object entities, int format, IResourceMapper rxmapper, 
			int statusCode, String callback, boolean isGZip, boolean isStrict) 
	throws IOException {
		OutputStream out = null;
		if (isGZip && isGZip(req)) {
			setGZipHeader(resp);
			out = new GZIPOutputStream(resp.getOutputStream());
		} else {
			out = resp.getOutputStream();
		}

		// ステータスコードの設定
		resp.setStatus(statusCode);

		// MessagePackの場合は、PrintWriterでなくOutputStreamを使用する。
		if (!(entities instanceof String) && format == FORMAT_MESSAGEPACK) {
			// MessagePack
			resp.setContentType(CONTENT_TYPE_MESSAGEPACK);
			try {
				rxmapper.toMessagePack(entities, out);
				
			} finally {
				try {
					out.close();
				} catch (IOException e) {}	// Do nothing.
			}
			
		} else {
			// レスポンスデータ出力
			PrintWriter prtout = null;
			try {
				prtout = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, ENCODING)));
				
				if (entities instanceof String) {
					// 文字列
					prtout.write((String)entities);
					
				} else if (format == FORMAT_JSON) {
					// JSON
					resp.setContentType(CONTENT_TYPE_REFLEX_JSON);
		
					// コールバック指定の場合は付加する
					if (callback != null && callback.length() > 0) {
						prtout.write(callback);
						prtout.write("(");
					}
					
					// JSON中身
					if (entities != null) {
						rxmapper.toJSON(entities, prtout);
					}
		
					// コールバック指定の場合は付加する
					if (callback != null && callback.length() > 0) {
						prtout.write(");");
					}
		
				} else {
					// XMLヘッダー出力
					resp.setContentType(CONTENT_TYPE_REFLEX_XML);
	
					// XML
					if (entities != null) {
						prtout.print(XMLHEAD);
						rxmapper.toXML(entities, prtout, isStrict);
					}
				}
	
			} finally {
				if (prtout != null) {
					try {
						prtout.close();
					} catch (Exception e) {}	// Do nothing.
				}
			}
		}
	}

	/**
	 * エラーページ出力
	 * @param resp HttpServletResponse
	 * @param exception 例外オブジェクト
	 */
	public void doErrorPage(HttpServletResponse resp, Throwable exception) throws IOException {

		int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
		//resp.setStatus(httpStatus);
		if (exception instanceof InvokeException) {
			httpStatus = ((InvokeException)exception).getHttpStatus();
		}

		if (resp.containsHeader("Content-Encoding")) {
			resp.setHeader("Content-Encoding", null);
		}

		OutputStream out = null;
		out = resp.getOutputStream();

		// レスポンスデータ出力
		PrintWriter prtout = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, ENCODING)));

		resp.setContentType("text/html;charset=" + ENCODING);

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

/*
		prtout.print("<p align=\"center\"><font size=\"6\"><b>Error Report");
		prtout.print("</b></font></p>");
*/
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

/*		prtout.print("<a href=\"http://www.virtual-tech.net/\"><img src=\"");
		prtout.print(REFLEX_LOGOS);
		prtout.print("\"></img></a>");
*/
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


		prtout.flush();
		out.flush();
		out.close();
	}

	/**
	 * レスポンス出力（ファイル用）
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 */
	public void doResponseFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {

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
			String type = reqFileTemp.substring(reqFileTemp.indexOf(".") + 1).toLowerCase();
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
			} catch (Exception e) {}
		}
	}

	/**
	 * InputStreamから読み込んだデータをResponseに設定
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param in InputStream
	 * @param contentType content-type
	 */
	public void setResponseFile(HttpServletRequest req, HttpServletResponse resp, 
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
			} catch (Exception e) {}
		}
	}

	/**
	 * Requestがgzip対応かどうかを判定します
	 * @param req
	 * @return true:gzip対応、false:gzip対応でない
	 */
	public boolean isGZip(HttpServletRequest req) {
		boolean ret = false;
		if (req != null) {
			String acceptedEncodings = req.getHeader("accept-encoding");	//クライアントの受理可能エンコーディング
			if (acceptedEncodings == null || acceptedEncodings.length() == 0) {
				acceptedEncodings = req.getHeader("Accept-Encoding");
			}
			if (acceptedEncodings != null && acceptedEncodings.indexOf("gzip") != -1) {
				ret = true;
			}
		}
		return ret;
	}
	
	/**
	 * GZip圧縮のレスポンスヘッダを設定します
	 * @param resp
	 */
	public void setGZipHeader(HttpServletResponse resp) {
		resp.setHeader("Content-Encoding", "gzip");
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
		StringBuffer eStr = new StringBuffer();
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
	public String getSuffix(HttpServletRequest req) {
		String suffix = null;
		String uri = req.getRequestURI();
		int suffixIdx = uri.lastIndexOf(".");
		if (suffixIdx > 0) {
			int slashIdx = uri.lastIndexOf("/");
			if (suffixIdx > slashIdx) {
				suffix = uri.substring(suffixIdx + 1).toLowerCase();
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
	public boolean isFileUpload(int format) {
		if (format == FORMAT_MULTIPART_FORMDATA) {
			return true;
		}
		return false;
	}

}
