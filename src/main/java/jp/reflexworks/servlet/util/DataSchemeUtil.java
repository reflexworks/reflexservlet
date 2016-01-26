package jp.reflexworks.servlet.util;

import java.io.OutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

public class DataSchemeUtil {
	
	public static final String PREFIX_1 = "data:";
	public static final String PREFIX_2 = ";base64,";
	public static final String CHARSET = "UTF-8";
	public static final String MEDIATYPE_DEFAULT = "text/plain;charset=" + CHARSET;
	
	/**
	 * byte配列をBase64に変換し、Data URL スキーム形式でストリームに出力する。
	 * 例) data:image/gif;base64,xxxxxxx
	 * @param data バイト配列のデータ
	 * @param mediatype Content-Type
	 * @param out 出力ストリーム
	 * @throws IOException
	 */
	public void generate(byte[] data, String mediatype, OutputStream out) 
	throws IOException {

		byte[] base64data = Base64.encodeBase64(data);
		
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX_1);
		if (mediatype != null && mediatype.length() > 0) {
			sb.append(mediatype);
		} else {
			sb.append(MEDIATYPE_DEFAULT);
		}
		sb.append(PREFIX_2);
		out.write(sb.toString().getBytes(CHARSET));
		out.write(base64data);
	}

}
