package jp.reflexworks.servlet.util;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Properties;

import jp.sourceforge.reflex.IResourceMapper;
import jp.sourceforge.reflex.core.ResourceMapper;
import jp.sourceforge.reflex.util.StringUtils;
import jp.reflexworks.servlet.model.webxml.Context__param;
import jp.reflexworks.servlet.model.webxml.Web__app;

public class LocalContextUtil extends ServletContextUtil {

	private static final String WEBXML_PACKAGE = "jp.reflexworks.servlet.model.webxml";

	private Map<String, String> contextParamMap = new LinkedHashMap<String, String>();

	public LocalContextUtil(File webxml) throws IOException {
		IResourceMapper webxmlMapper = new ResourceMapper(WEBXML_PACKAGE);
		Reader reader = null;
		try {
			reader = new FileReader(webxml);
			Web__app webapp = (Web__app)webxmlMapper.fromXML(reader);
			if (webapp != null && webapp.context__param != null &&
					webapp.context__param.size() > 0) {
				for (Context__param contextParam : webapp.context__param) {
					contextParamMap.put(contextParam.param__name, contextParam.param__value);
				}
			}
			
		} finally {
			reader.close();
		}
		
		super.init();

		Properties props = getProperties();
		if (props != null) {
			Enumeration<?> enu = props.propertyNames();
			while (enu.hasMoreElements()) {
				String name = (String)enu.nextElement();
				String val = props.getProperty(name);
				contextParamMap.put(name, val);
			}
		}

	}
	
	public String get(String key) {
		if (key == null) {
			return null;
		}
		return contextParamMap.get(key);
	}
	
	public String getConv(String key) {
		String value = get(key);
		return convSystemProp(value);
	}
	
	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	public Map<String, String> getParamsConv(String prefix) {
		Map<String, String> params = new HashMap<String, String>();
		setMapConv(params, prefix);
		return params;
	}
	
	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 */
	public Map<String, String> getParams(String prefix) {
		Map<String, String> params = new HashMap<String, String>();
		setMap(params, prefix);
		return params;
	}
	
	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	private void setMapConv(Map<String, String> params, String prefix) {
		for (String name : contextParamMap.keySet()) {
			if (name.startsWith(prefix)) {
				String editKey = convSystemProp(name);
				if (!StringUtils.isBlank(editKey)) {
					params.put(editKey, getConv(editKey));
				}
			}
		}
	}
	
	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 */
	private void setMap(Map<String, String> params, String prefix) {
		for (String name : contextParamMap.keySet()) {
			if (name.startsWith(prefix)) {
				params.put(name, get(name));
			}
		}
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報をSortedMapにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	public SortedMap<String, String> getSortedParamsConv(String prefix) {
		SortedMap<String, String> params = new TreeMap<String, String>();
		setMapConv(params, prefix);
		return params;
	}
	
	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報をSortedMapにして返却します。
	 */
	public SortedMap<String, String> getSortedParams(String prefix) {
		SortedMap<String, String> params = new TreeMap<String, String>();
		setMap(params, prefix);
		return params;
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	public Set<String> getSetConv(String prefix) {
		Set<String> params = new HashSet<String>();
		setSetConv(params, prefix);
		return params;
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 */
	public Set<String> getSet(String prefix) {
		Set<String> params = new HashSet<String>();
		setSet(params, prefix);
		return params;
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	private void setSetConv(Set<String> params, String prefix) {
		if (prefix != null && prefix.length() > 0) {
			for (String name : contextParamMap.keySet()) {
				if (name.startsWith(prefix)) {
					params.add(getConv(name));
				}
			}
		}
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 */
	private void setSet(Set<String> params, String prefix) {
		if (prefix != null && prefix.length() > 0) {
			for (String name : contextParamMap.keySet()) {
				if (name.startsWith(prefix)) {
					params.add(get(name));
				}
			}
		}
	}
	
	public Properties getLocalProperties() {
		return getProperties();
	}

}
