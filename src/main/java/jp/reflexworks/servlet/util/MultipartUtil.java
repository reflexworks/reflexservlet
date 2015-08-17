package jp.reflexworks.servlet.util;

import javax.servlet.http.Part;

import jp.reflexworks.servlet.ReflexServletConst;

/**
 * Servlet 3.0のファイルアップロード機能をサポートするユーティリティです.
 */
public class MultipartUtil implements ReflexServletConst {

	public static final String HEADER_CONTENT_DISPOSITION_LOWER = 
			HEADER_CONTENT_DISPOSITION.toLowerCase();

	/**
	 * Content-Dispositionからファイル名を取得します.
	 * <p>
	 * Content-Disposition: form-data; name="content"; filename="FILE_NAME"
	 * </p>
	 * @param part Part
	 * @return アップロードファイル名
	 */
	public static String getFilename(Part part) {
		if (part != null) {
			String contentDisposition = part.getHeader(HEADER_CONTENT_DISPOSITION);
			if (contentDisposition == null) {
				contentDisposition = part.getHeader(HEADER_CONTENT_DISPOSITION_LOWER);
			}
			if (contentDisposition != null) {
				for (String cd : contentDisposition.split(HEADER_DISPOSITION_SPLIT)) {
					if (cd != null && cd.trim().startsWith(HEADER_VALUE_FILENAME)) {
						return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
					}
				}
			}
		}
		return null;
	}

}
