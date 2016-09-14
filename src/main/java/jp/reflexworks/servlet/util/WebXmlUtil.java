package jp.reflexworks.servlet.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.reflexworks.servlet.model.webxml.Web__app;
import jp.sourceforge.reflex.IResourceMapper;
import jp.sourceforge.reflex.core.ResourceMapper;
import jp.sourceforge.reflex.util.FileUtil;

public class WebXmlUtil {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	public Web__app getWebXml() {
		Web__app webxml = null;
		Reader reader = null;
		try {
			String webxmlFile = FileUtil.getResourceFilename("web.xml");
			IResourceMapper mapper = new ResourceMapper("jp.reflexworks.servlet.model.webxml");
			reader = new InputStreamReader(new FileInputStream(webxmlFile), "UTF-8");
			webxml = (Web__app)mapper.fromXML(reader);
			
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getClass().getName(), e);

		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.log(Level.WARNING, e.getClass().getName(), e);
				}
			}
		}
		
		return webxml;
	}

}
