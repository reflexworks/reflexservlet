package jp.reflexworks.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.reflexworks.servlet.util.HttpStatus;
import jp.sourceforge.reflex.IResourceMapper;
import jp.sourceforge.reflex.core.ResourceMapper;
import jp.sourceforge.reflex.util.DeflateUtil;

import org.json.JSONException;

/**
 * Reflex サーブレット.
 * <p>
 * 以下の機能を備えています。サーブレットの親クラスとして使用してください。
 * <ul>
 * <li>JSONまたはXMLのPOSTデータをオブジェクトに変換 (getEntityメソッド)。</li>
 * <li>オブジェクトをJSONまたはXMLに変換してレスポンスデータに設定 (doResponseメソッド)</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("serial")
public class ReflexServlet extends HttpServlet implements ReflexServletConst {
	
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
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param useJson true:JSON形式、false:XML形式
	 * @param model_package modelのパッケージ
	 */
	public void doResponse(HttpServletResponse resp, Object entities, boolean useJson, 
			String model_package) throws IOException {
		int statusCode = HttpStatus.SC_OK;
		//if (entities instanceof Status) {
		//	statusCode = ((Status)entities).getCode();  // とりあえずコメントアウト
		//}
		this.doResponse(resp, entities, useJson, model_package, statusCode);
	}

	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。<br>
	 * JSON形式指定でcallback関数の設定がある場合、JSONP形式で返却します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param useJson true:JSON形式、false:XML形式
	 * @param model_package modelのパッケージ
	 * @param callback callback関数
	 */
	public void doResponse(HttpServletResponse resp, Object entities, boolean useJson, 
			String model_package, String callback) throws IOException {
		int statusCode = HttpStatus.SC_OK;
		//if (entities instanceof Status) {
		//	statusCode = ((Status)entities).getCode();  // とりあえずコメントアウト
		//}
		this.doResponse(resp, entities, useJson, model_package, statusCode, callback);
	}

	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param useJson true:JSON形式、false:XML形式
	 * @param model_package modelのパッケージ
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 */
	public void doResponse(HttpServletResponse resp, Object entities, boolean useJson, 
			String model_package, int statusCode) throws IOException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		this.doResponse(resp, entities, useJson, rxmapper, null, statusCode);
	}

	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。<br>
	 * JSON形式指定でcallback関数の設定がある場合、JSONP形式で返却します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param useJson true:JSON形式、false:XML形式
	 * @param model_package modelのパッケージ
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param callback callback関数
	 */
	public void doResponse(HttpServletResponse resp, Object entities, boolean useJson, 
			String model_package, int statusCode, String callback) 
	throws IOException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		this.doResponse(resp, entities, useJson, rxmapper, null, statusCode, callback);
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
	 * @param useJson true:JSON形式、false:XML形式
	 * @param model_package modelのパッケージ
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param callback callback関数
	 * @param isGZip true:GZIP圧縮対応
	 */
	public void doResponse(HttpServletRequest req, HttpServletResponse resp, 
			Object entities, boolean useJson, String model_package, int statusCode, 
			String callback, boolean isGZip) 
	throws IOException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		this.doResponse(req, resp, entities, useJson, rxmapper, null, statusCode, 
				callback, isGZip);
	}

	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param useJson true:JSON形式、false:XML形式
	 * @param model_package modelのパッケージ
	 */
	public void doResponse(HttpServletResponse resp, Object entities, boolean useJson, 
			Map<String, String> model_package) throws IOException {
		int statusCode = HttpStatus.SC_OK;
		//if (entities instanceof Status) {
		//	statusCode = ((Status)entities).getCode();  // とりあえずコメントアウト
		//}
		this.doResponse(resp, entities, useJson, model_package, statusCode);
	}

	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。<br>
	 * JSON形式指定でcallback関数の設定がある場合、JSONP形式で返却します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param useJson true:JSON形式、false:XML形式
	 * @param model_package modelのパッケージ
	 * @param callback callback関数
	 */
	public void doResponse(HttpServletResponse resp, Object entities, boolean useJson, 
			Map<String, String> model_package, String callback) 
	throws IOException {
		int statusCode = HttpStatus.SC_OK;
		//if (entities instanceof Status) {
		//	statusCode = ((Status)entities).getCode();  // とりあえずコメントアウト
		//}
		this.doResponse(resp, entities, useJson, model_package, statusCode, callback);
	}

	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param useJson true:JSON形式、false:XML形式
	 * @param model_package modelのパッケージ
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 */
	public void doResponse(HttpServletResponse resp, Object entities, boolean useJson, 
			Map<String, String> model_package, int statusCode) throws IOException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		this.doResponse(resp, entities, useJson, rxmapper, null, statusCode);
	}
		
	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。<br>
	 * JSON形式指定でcallback関数の設定がある場合、JSONP形式で返却します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param useJson true:JSON形式、false:XML形式
	 * @param model_package modelのパッケージ
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param callback callback関数
	 */
	public void doResponse(HttpServletResponse resp, Object entities, boolean useJson, 
			Map<String, String> model_package, int statusCode, String callback) 
					throws IOException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		this.doResponse(resp, entities, useJson, rxmapper, null, statusCode, callback);
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
	 * @param useJson true:JSON形式、false:XML形式
	 * @param model_package modelのパッケージ
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param callback callback関数
	 * @param isGZip GZip圧縮対応の場合true
	 */
	public void doResponse(HttpServletRequest req, HttpServletResponse resp, 
			Object entities, boolean useJson, Map<String, String> model_package, 
			int statusCode, String callback, boolean isGZip) 
					throws IOException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		this.doResponse(req, resp, entities, useJson, rxmapper, null, statusCode, 
				callback, isGZip);
	}

	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param useJson true:JSON形式、false:XML形式
	 * @param rxmapper Resource Mapper
	 * @param deflateUtil DeflateUtil
	 */
	public void doResponse(HttpServletResponse resp, Object entities, boolean useJson, 
			IResourceMapper rxmapper, DeflateUtil deflateUtil) 
	throws IOException {
		int statusCode = HttpStatus.SC_OK;
		this.doResponse(resp, entities, useJson, rxmapper, deflateUtil, statusCode);
	}

	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。<br>
	 * JSON形式指定でcallback関数の設定がある場合、JSONP形式で返却します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param useJson true:JSON形式、false:XML形式
	 * @param rxmapper Resource Mapper
	 * @param callback callback関数
	 */
	public void doResponse(HttpServletResponse resp, Object entities, boolean useJson, 
			IResourceMapper rxmapper, DeflateUtil deflateUtil, String callback) 
	throws IOException {
		int statusCode = HttpStatus.SC_OK;
		this.doResponse(resp, entities, useJson, rxmapper, deflateUtil, statusCode, callback);
	}

	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param useJson true:JSON形式、false:XML形式
	 * @param rxmapper Resource Mapper
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 */
	public void doResponse(HttpServletResponse resp, Object entities, boolean useJson, 
			IResourceMapper rxmapper, DeflateUtil deflateUtil, int statusCode) 
	throws IOException {
		this.doResponse(resp, entities, useJson, rxmapper, deflateUtil, statusCode, null);
	}
		
	/**
	 * レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをXMLまたはJSONにシリアライズして、レスポンスデータに設定します。<br>
	 * JSON形式指定でcallback関数の設定がある場合、JSONP形式で返却します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities XMLまたはJSONにシリアライズするentity
	 * @param useJson true:JSON形式、false:XML形式
	 * @param rxmapper Resource Mapper
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param callback callback関数
	 */
	public void doResponse(HttpServletResponse resp, Object entities, boolean useJson, 
			IResourceMapper rxmapper, DeflateUtil deflateUtil,
			int statusCode, String callback) 
	throws IOException {
		doResponse(null, resp, entities, useJson, rxmapper, deflateUtil, statusCode, 
				callback, false);	// GZip圧縮しない
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
	 * @param useJson true:JSON形式、false:XML形式
	 * @param rxmapper Resource Mapper
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param isGZip GZIP圧縮する場合true
	 */
	public void doResponse(HttpServletRequest req, HttpServletResponse resp, 
			Object entities, boolean useJson, IResourceMapper rxmapper, 
			DeflateUtil deflateUtil, int statusCode, boolean isGZip) 
	throws IOException {
		doResponse(req, resp, entities, useJson, rxmapper, deflateUtil, 
				statusCode, null, isGZip);
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
	 * @param useJson true:JSON形式、false:XML形式
	 * @param rxmapper Resource Mapper
	 * @param deflateUtil DeflateUtil
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param callback callback関数
	 * @param isGZip GZIP圧縮する場合true
	 */
	public void doResponse(HttpServletRequest req, HttpServletResponse resp, 
			Object entities, boolean useJson, IResourceMapper rxmapper, 
			DeflateUtil deflateUtil, int statusCode, String callback, boolean isGZip) 
	throws IOException {
		int format = ReflexServletUtil.convertFormatType(useJson);
		doResponse(req, resp, entities, format, rxmapper, deflateUtil, 
				statusCode, callback, isGZip, true);	// 名前空間出力(旧バージョン)
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
	 * @param format 1:XML, 2:JSON, 3:MessagePack
	 * @param rxmapper Resource Mapper
	 * @param deflateUtil DeflateUtil
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param isGZip GZIP圧縮する場合true
	 */
	public void doResponse(HttpServletRequest req, HttpServletResponse resp, 
			Object entities, int format, IResourceMapper rxmapper, 
			DeflateUtil deflateUtil, int statusCode, boolean isGZip, boolean isStrict) 
	throws IOException {
		doResponse(req, resp, entities, format, rxmapper, deflateUtil, statusCode, null,
				isGZip, isStrict);
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
	 * @param format 1:XML, 2:JSON, 3:MessagePack
	 * @param rxmapper Resource Mapper
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param contentType Content-Type
	 * @param isGZip GZIP圧縮する場合true
	 */
	public void doResponse(HttpServletRequest req, HttpServletResponse resp, 
			Object entities, int format, IResourceMapper rxmapper, 
			DeflateUtil deflateUtil, int statusCode, String contentType, boolean isGZip, 
			boolean isStrict) 
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
	 * @param format 1:XML, 2:JSON, 3:MessagePack
	 * @param rxmapper Resource Mapper
	 * @param statusCode レスポンスのステータスに設定するコード。デフォルトはSC_OK(200)。
	 * @param contentType Content-Type
	 * @param callback callback関数
	 * @param isGZip GZIP圧縮する場合true
	 */
	public void doResponse(HttpServletRequest req, HttpServletResponse resp, 
			Object entities, int format, IResourceMapper rxmapper, 
			DeflateUtil deflateUtil, int statusCode, String contentType, 
			String callback, boolean isGZip, boolean isStrict) 
	throws IOException {
		// callbackは廃止
		util.doResponse(req, resp, entities, format, rxmapper, deflateUtil,
				statusCode, isGZip, isStrict, contentType);
	}

	/**
	 * コールバック形式レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをJSONにシリアライズしてcallback関数を付け、JSONP形式で返却します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities JSONにシリアライズするentity
	 * @param model_package modelのパッケージ
	 * @param callback コールバック関数名
	 */
	public void doCallback(HttpServletResponse resp, Object entities, String model_package, 
			String callback) throws IOException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		this.doResponse(resp, entities, true, rxmapper, null, HttpStatus.SC_OK, callback);
	}

	/**
	 * コールバック形式レスポンス出力.
	 * <p>
	 * 指定されたオブジェクトをJSONにシリアライズしてcallback関数を付け、JSONP形式で返却します。
	 * </p>
	 * @param resp HttpServletResponse
	 * @param entities JSONにシリアライズするentity
	 * @param model_package modelのパッケージ
	 * @param callback コールバック関数名
	 */
	public void doCallback(HttpServletResponse resp, Object entities, 
			Map<String, String> model_package, String callback) 
	throws IOException {
		IResourceMapper rxmapper = new ResourceMapper(model_package);
		this.doResponse(resp, entities, true, rxmapper, null, HttpStatus.SC_OK, callback);
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
