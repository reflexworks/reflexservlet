package jp.reflexworks.servlet;

/**
 * ReflexServletで使用する定数
 */
public interface ReflexServletConst {

	/** エンコード*/
	public static final String ENCODING = "UTF-8";
	/** Content-Type : XML */
	public static final String CONTENT_TYPE_XML = "text/xml";
	public static final String CONTENT_TYPE_REFLEX_XML = 
			CONTENT_TYPE_XML + ";charset=" + ENCODING;
	/** Content-Type : JSON */
	public static final String CONTENT_TYPE_JSON = "text/javascript";
	public static final String CONTENT_TYPE_REFLEX_JSON = 
			CONTENT_TYPE_JSON + ";charset=" + ENCODING;
	/** Content-Type : MessagePack */
	public static final String CONTENT_TYPE_MESSAGEPACK = "application/x-msgpack";
	/** Content-Type : multipart/form-data */
	public static final String CONTENT_TYPE_MULTIPART_FORMDATA = "multipart/form-data";
	/** Content-Type : Text */
	public static final String CONTENT_TYPE_TEXT = "text/";
	/** XMLヘッダ */
	public static final String XMLHEAD = "<?xml version=\"1.0\" encoding=\"" + ENCODING + "\" ?>\n";
	/** json */
	public static final String JSON = "json";
	/** xml */
	public static final String XML = "xml";

	/** 改行コード */
	public static final String NEWLINE = "\n";
	/** HTMLの空白 */
	public static final String HTML_BLANK = "&nbsp;";
	/** Reflexロゴ */
	public static final String REFLEX_LOGOS = "http://reflex.sourceforge.jp/images/Reflex.gif";
	/** Reflex Signature */
	public static final String REFLEX_SIGNATURE = "　　ＶＩＲＴＵＡＬ ＴＥＣＨＮＯＬＯＧＹ ＩＮＣ.";
	/** デフォルトページ */
	public static final String DEFAULT_PAGE = "index.html";

	/** Method : GET */
	public static final String GET = "GET";
	/** Method : POST */
	public static final String POST = "POST";
	/** Method : PUT */
	public static final String PUT = "PUT";
	/** Method : DELETE */
	public static final String DELETE = "DELETE";

	/** Format : Text */
	public static final int FORMAT_TEXT = 0;
	/** Format : XML */
	public static final int FORMAT_XML = 1;
	/** Format : JSON */
	public static final int FORMAT_JSON = 2;
	/** Format : MessagePack */
	public static final int FORMAT_MESSAGEPACK = 3;
	/** Format : multipart/form-data */
	public static final int FORMAT_MULTIPART_FORMDATA = 4;

}
