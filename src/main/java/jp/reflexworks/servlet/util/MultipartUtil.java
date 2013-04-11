package jp.reflexworks.servlet.util;

import javax.servlet.http.Part;

/**
 * Servlet 3.0のファイルアップロード機能をサポートするユーティリティです.
 */
public class MultipartUtil {
	
	public static final String CONTENT_DISPOSITION = "Content-Disposition";
	public static final String CONTENT_DISPOSITION_LOWER = CONTENT_DISPOSITION.toLowerCase();
	public static final String DISPOSITION_SPLIT = ";";
	public static final String FILENAME = "filename";

	/**
	 * Content-Dispositionからファイル名を取得します.
	 * <p>
	 * Content-Disposition: form-data; name="content"; filename="FILE_NAME"
	 * </p>
	 * @param part Part
	 * @return アップロードファイル名
	 */
	public String getFilename(Part part) {
		if (part != null) {
			String contentDisposition = part.getHeader(CONTENT_DISPOSITION);
			if (contentDisposition == null) {
				contentDisposition = part.getHeader(CONTENT_DISPOSITION_LOWER);
			}
			if (contentDisposition != null) {
		        for (String cd : contentDisposition.split(DISPOSITION_SPLIT)) {
		            if (cd != null && cd.trim().startsWith(FILENAME)) {
		                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
		            }
		        }
			}
		}
        return null;
    }

}
