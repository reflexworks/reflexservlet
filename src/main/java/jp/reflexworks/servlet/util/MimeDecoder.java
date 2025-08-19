package jp.reflexworks.servlet.util;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;

public class MimeDecoder {

	private static Logger logger = Logger.getLogger(MimeDecoder.class.getName());

	/**
	 * Mimeエンコードされた文字列をデコードします.
	 * <p>
	 * Mimeエンコードの例 : =?ISO-2022-JP?B?GyRCJUYlOSVIJWEhPCVrISobKEI=?= <br/>
	 * 手順<br/>
	 *   1. =? と ?= ではさまれた部分を ? で区切ります。<br/>
	 *   2. [1] : デコード形式<br/>
	 *   3. [2] : 'Q'なら quoted-printable, 'B'ならbase-64でデコードします。<br/>
	 *      (注)現在'Q'には対応していません。<br/>
	 * 変換できない文字列はそのまま返却します.
	 * </p>
	 * @param str Mimeエンコードされた文字列
	 * @return デコードした文字列
	 */
	public static String decode(String str) {
		if (str == null) {
			return str;
		}
		int len = str.length();
		String ret = null;
		if (len > 4 && str.startsWith("=?") && str.endsWith("?=")) {
			// Mimeエンコード
			String tmp = str.substring(2, len - 2);
			int idx = tmp.indexOf("?");
			if (idx > 0) {
				int idx1 = idx + 1;
				int idx2 = tmp.indexOf("?", idx1);
				if (idx2 > 0) {
					String format = tmp.substring(0, idx);
					String type = tmp.substring(idx1, idx2);
					if ("B".equals(type)) {
						String tmp2 = tmp.substring(idx2 + 1);
						try {
							ret = new String(Base64.decodeBase64(tmp2), format);
						} catch (UnsupportedEncodingException e) {
							if (logger.isLoggable(Level.INFO)) {
								logger.info("UnsupportedEncodingException: " + e.getMessage() +
										", format: " + format);
							}
						}
					}
				}
			}
		}
		if (ret == null) {
			// 引数をそのまま返す
			return str;
		}
		return ret;
	}

}
