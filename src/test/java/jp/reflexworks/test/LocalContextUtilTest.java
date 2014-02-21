package jp.reflexworks.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import jp.sourceforge.reflex.util.FileUtil;
import jp.reflexworks.servlet.util.LocalContextUtil;

public class LocalContextUtilTest {
	
	@Test
	public void testLocalContext() throws IOException {

		String webXml = FileUtil.getResourceFilename("web.xml");
		
		File webXmlFile = new File(webXml);
		LocalContextUtil localContextUtil = new LocalContextUtil(webXmlFile);

		String val = localContextUtil.get("context.properties");
		System.out.println("context.properties=" + val);
		
		assertTrue("test.properties".equals(val));
	}
	
}
