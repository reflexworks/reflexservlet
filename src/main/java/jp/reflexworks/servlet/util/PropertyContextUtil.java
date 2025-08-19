package jp.reflexworks.servlet.util;

import java.io.IOException;

public class PropertyContextUtil extends ServletContextUtil {

	public PropertyContextUtil(String propFile) throws IOException {
		super.init(propFile);
	}

}
