package jp.reflexworks.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.reflexworks.servlet.ReflexServletUtil;
import jp.reflexworks.servlet.ReflexServletConst;
import jp.sourceforge.reflex.IResourceMapper;
import jp.sourceforge.reflex.core.ResourceMapper;
import jp.sourceforge.reflex.exception.JSONException;
import jp.sourceforge.reflex.util.DeflateUtil;

import org.eclipse.jetty.websocket.WebSocketServlet;

@SuppressWarnings("serial")
public abstract class ReflexSocketServlet extends WebSocketServlet implements ReflexServletConst {
	
	private ReflexServletUtil util = new ReflexServletUtil();

	/**
	 * リクエストデータ取得.
	 * <p>
	 * リクエストのPOSTデータに設定されたJSON文字列を、model_package配下のクラスのオブジェクトに変換して返却します。
	 * </p>
	 * @param req HttpServletRequest
	 * @param model_package modelのパッケージ
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getEntity(HttpServletRequest req, String model_package) 
	throws IOException, JSONException, ClassNotFoundException {
		return util.getEntity(req, model_package);
	}

	/**
	 * リクエストデータ取得.
	 * <p>
	 * リクエストのPOSTデータに設定されたJSON文字列を、model_package配下のクラスのオブジェクトに変換して返却します。
	 * </p>
	 * @param req HttpServletRequest
	 * @param model_package modelのパッケージ
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getEntity(HttpServletRequest req, Map<String, String> model_package) 
	throws IOException, JSONException, ClassNotFoundException {
		return util.getEntity(req, model_package);
	}

	/**
	 * リクエストデータ取得.
	 * <p>
	 * リクエストのPOSTデータに設定されたJSON文字列を、ResourceMapperを使用してオブジェクトに変換して返却します。
	 * </p>
	 * @param req HttpServletRequest
	 * @param rxmapper IResourceMapper
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getEntity(HttpServletRequest req, IResourceMapper rxmapper) 
	throws IOException, JSONException, ClassNotFoundException {
		return util.getEntity(req, rxmapper);
	}
	
	/**
	 * リクエストデータ取得.
	 * <p>
	 * 指定されたJSONまたはXML文字列を、model_package配下のクラスのオブジェクトに変換して返却します。
	 * </p>
	 * @param body XMLまたはJSON文字列
	 * @param model_package modelのパッケージ
	 * @param useJson true:JSON、false:XML
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getEntity(String body, String model_package, boolean useJson) 
	throws IOException, JSONException, ClassNotFoundException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		return util.getEntity(body, rxmapper, useJson);
	}

	/**
	 * リクエストデータ取得.
	 * <p>
	 * 指定されたJSONまたはXML文字列を、model_package配下のクラスのオブジェクトに変換して返却します。
	 * </p>
	 * @param body XMLまたはJSON文字列
	 * @param model_package modelのパッケージ
	 * @param useJson true:JSON、false:XML
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getEntity(String body, Map<String, String> model_package,
			boolean useJson) 
	throws IOException, JSONException, ClassNotFoundException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		return util.getEntity(body, rxmapper, useJson);
	}

	/**
	 * リクエストデータ取得.
	 * <p>
	 * 指定されたJSONまたはXML文字列を、ResourceMapperを使用してオブジェクトに変換して返却します。
	 * </p>
	 * @param body XMLまたはJSON文字列
	 * @param rxmapper IResourceMapper
	 * @param useJson true:JSON、false:XML
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getEntity(String body, IResourceMapper rxmapper, boolean useJson) 
	throws IOException, JSONException, ClassNotFoundException {
		return util.getEntity(body, rxmapper, useJson);
	}

	/**
	 * リクエストデータ取得.
	 * <p>
	 * リクエストのPOSTデータに設定されたXML文字列を、model_package配下のクラスのオブジェクトに変換して返却します。
	 * </p>
	 * @param req HttpServletRequest
	 * @param model_package modelのパッケージ
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getXmlEntity(HttpServletRequest req, String model_package) 
	throws IOException, JSONException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		return util.getXmlEntity(req, rxmapper);
	}

	/**
	 * リクエストデータ取得.
	 * <p>
	 * リクエストのPOSTデータに設定されたXML文字列を、model_package配下のクラスのオブジェクトに変換して返却します。
	 * </p>
	 * @param req HttpServletRequest
	 * @param model_package modelのパッケージ
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getXmlEntity(HttpServletRequest req, Map<String, String> model_package) 
	throws IOException, JSONException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		return util.getXmlEntity(req, rxmapper);
	}

	/**
	 * リクエストデータ取得.
	 * <p>
	 * リクエストのPOSTデータに設定されたXML文字列を、ResourceMapperを使用してオブジェクトに変換して返却します。
	 * </p>
	 * @param req HttpServletRequest
	 * @param rxmapper ResourceMapper
	 * @return POSTデータをオブジェクトに変換したもの
	 */
	public Object getXmlEntity(HttpServletRequest req, IResourceMapper rxmapper) 
	throws IOException, JSONException {
		return util.getXmlEntity(req, rxmapper);
	}

	/**
	 * リクエストデータの文字列を取得します
	 * @param req HttpServletRequest
	 * @return リクエストデータ文字列
	 */
	public String getBody(HttpServletRequest req) throws IOException, JSONException {
		return util.getBody(req);
	}

	/**
	 * BufferedReaderを読み、Stringにして返却します
	 * @param b BufferedReader
	 * @return Bufferから読み込んだ文字列
	 */
	public String getBody(BufferedReader b) throws IOException {
		return util.getBody(b);
	}

	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。<br>
	 * JSON形式指定でcallback関数の設定がある場合、JSONP形式で返却します。
	 * </p>
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param format 0:XML, 1:JSON, 2:MessagePack
	 * @param rxmapper Resource Mapper
	 * @param deflateUtil DeflateUtil
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param isGZip GZIP圧縮する場合true
	 * @param isStrict 名前空間を出力する場合true
	 */
	public void doResponse(HttpServletRequest req, HttpServletResponse resp, 
			Object entities, int format, IResourceMapper rxmapper, 
			DeflateUtil deflateUtil, int statusCode, boolean isGZip, boolean isStrict) 
	throws IOException {
		doResponse(req, resp, entities, format, rxmapper, deflateUtil, statusCode, 
				isGZip, isStrict, null);
	}

	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。<br>
	 * JSON形式指定でcallback関数の設定がある場合、JSONP形式で返却します。
	 * </p>
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param format 0:XML, 1:JSON, 2:MessagePack
	 * @param rxmapper Resource Mapper
	 * @param deflateUtil DeflateUtil
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param contentType Content-Type
	 * @param isGZip GZIP圧縮する場合true
	 * @param isStrict 名前空間を出力する場合true
	 */
	public void doResponse(HttpServletRequest req, HttpServletResponse resp, 
			Object entities, int format, IResourceMapper rxmapper, 
			DeflateUtil deflateUtil, int statusCode, boolean isGZip, boolean isStrict, 
			String contentType) 
	throws IOException {
		util.doResponse(req, resp, entities, format, rxmapper, deflateUtil, 
				statusCode, isGZip, isStrict, contentType);
	}

	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。<br>
	 * JSON形式指定でcallback関数の設定がある場合、JSONP形式で返却します。
	 * </p>
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param format 0:XML, 1:JSON, 2:MessagePack
	 * @param rxmapper Resource Mapper
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param contentType Content-Type
	 * @param callback callback関数
	 * @param isGZip GZIP圧縮する場合true
	 * @param isStrict 名前空間を出力する場合true
	 */
	public void doResponse(HttpServletRequest req, HttpServletResponse resp, 
			Object entities, int format, IResourceMapper rxmapper, 
			DeflateUtil deflateUtil,
			int statusCode, String contentType, String callback, boolean isGZip, 
			boolean isStrict) 
	throws IOException {
		// callbackは廃止
		util.doResponse(req, resp, entities, format, rxmapper, deflateUtil,
				statusCode, isGZip, isStrict, contentType);
	}

	/**
	 * エラーページ出力
	 * @param resp HttpServletResponse
	 * @param exception 例外オブジェクト
	 */
	public void doErrorPage(HttpServletResponse resp, Throwable exception) throws IOException {
		util.doErrorPage(resp, exception);
	}

	/**
	 * レスポンス出力（ファイル用）
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 */
	public void doResponseFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		util.doResponseFile(req, resp);
	}

	/**
	 * InputStreamから読み込んだデータをResponseに設定します
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param in InputStream
	 * @param contentType content-type
	 */
	public void setResponseFile(HttpServletRequest req, HttpServletResponse resp, 
			InputStream in, String contentType)
	throws IOException {
		util.setResponseFile(req, resp, in, contentType);
	}

	/**
	 * Requestがgzip対応かどうかを判定します
	 * @param req
	 * @return true:gzip対応、false:gzip対応でない
	 */
	public boolean isGZip(HttpServletRequest req) {
		return util.isGZip(req);
	}
	
	/**
	 * GZip圧縮のレスポンスヘッダを設定します
	 * @param resp
	 */
	public void setGZipHeader(HttpServletResponse resp) {
		util.setGZipHeader(resp);
	}

	/**
	 * ExceptionのStackTraceをカンマでつなげます
	 * @param exception
	 * @return 編集した文字列
	 */
	public static String errorString(Throwable exception) {
		return ReflexServletUtil.errorString(exception);
	}

	/**
	 * ExceptionのStackTraceを指定文字でつなげます
	 * @param exception
	 * @param s 指定文字
	 * @return 編集した文字列
	 */
	public static String errorString(Throwable exception, String s) {
		return ReflexServletUtil.errorString(exception, s);
	}

	/**
	 * URIに指定された拡張子を取得します
	 * @param req HttpServletRequest
	 * @return 拡張子。ない場合はnull。
	 */
	public String getSuffix(HttpServletRequest req) {
		return util.getSuffix(req);
	}
	
	/**
	 * Content-Typeからformat区分を生成します.
	 * @param req リクエスト
	 * @return 0:String, 1:XML, 2:JSON, 3:MessagePack, 4:multipart/form-data, -1:ContentTypeからでは判定不能
	 */
	public int getFormat(HttpServletRequest req) {
		return ReflexServletUtil.getFormat(req);
	}
	
	/**
	 * リクエストがmultipart/form-data形式かどうかチェックする.
	 * @param req リクエスト
	 * @return multipart/form-data形式の場合true
	 */
	public boolean isFileUpload(HttpServletRequest req) {
		int format = getFormat(req);
		return ReflexServletUtil.isFileUpload(format);
	}

}
