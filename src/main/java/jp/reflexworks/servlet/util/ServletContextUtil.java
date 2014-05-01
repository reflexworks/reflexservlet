package jp.reflexworks.servlet.util;

import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.sourceforge.reflex.util.FileUtil;
import jp.sourceforge.reflex.util.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextUtil implements ServletContextListener {
	
	public static final String REGEX_SYSTEM_PARAM_START = "\\$\\{";
	public static final String REGEX_SYSTEM_PARAM_END = "\\}";
	public static final String REGEX_SYSTEM_PARAM = 
		REGEX_SYSTEM_PARAM_START + "(.*?)" + REGEX_SYSTEM_PARAM_END;
	public static final String URL_CLASSPATH = "classpath:///";
	
	/** 
	 * context-paramをプロパティファイルに設定する場合、
	 * プロパティファイルのパスを以下のnameでweb.xmlに設定してください。
	 * web.xmlのcontext-paramより、プロパティファイルの値が優先されます。
	 **/
	public static final String PARAM_PROPERTY = "_context.properties";
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private ServletContext servletContext;
	private Properties properties;
	private Pattern pattern;
	private static ServletContextUtil plugin = null;
	private FileUtil fileUtil = new FileUtil();

	public void contextInitialized(ServletContextEvent sce) {
		this.servletContext = sce.getServletContext();
		init();
		String appName = sce.getServletContext().getServletContextName();
		plugin = this;
		logger.info("Web Application '" + appName + "' has been started.");
	}
	
	protected void init() {
		this.pattern = Pattern.compile(REGEX_SYSTEM_PARAM);
		String propertyPath = getConv(PARAM_PROPERTY);
		if (propertyPath != null && propertyPath.length() > 0) {
			/*
			InputStream in = null;
			File propertyFile = new File(propertyPath);
			if (propertyFile.exists()) {
				try {
					in = new FileInputStream(propertyFile);
				} catch (IOException e) {
					logger.warning(e.getMessage());
				}
			}
			
			if (in == null) {
				ClassLoader loader = this.getClass().getClassLoader();
				URL propertyURL = loader.getResource(propertyPath);
				if (propertyURL != null) {
					try {
						in = propertyURL.openStream();
					} catch (IOException e) {
						logger.warning(e.getMessage());
					}
				}
			}
			*/
			InputStream in = fileUtil.getInputStreamFromFile(propertyPath);
			
			if (in != null) {
				try {
					this.properties = new Properties();
					this.properties.load(in);
					
				} catch (IOException e) {
					logger.warning(e.getMessage());
				}
			}
		}

	}

	public void contextDestroyed(ServletContextEvent sce) {
		String appName = sce.getServletContext().getServletContextName();
		logger.info("Stopping Web Application '" + appName + "'...");

		plugin = null;
		this.servletContext = null;

		logger.info("Web Application '" + appName + "' has been stopped.");
	}
	
	/**
	 * 指定されたキーに対する値を取得します.
	 * <p>
	 * プロパティファイル、web.xmlの順で参照し、値があれば返却します。
	 * </p>
	 * @param key キー
	 * @return キーに対する値
	 */
	public String get(String key) {
		if (key == null) {
			return null;
		}
		String val = getProperty(key);
		if (val == null) {
			val = getContext(key);
		}
		return val;
	}
	
	/**
	 * 指定されたキーに対する値を取得します.
	 * <p>
	 * プロパティファイル、web.xmlの順で参照し、値があれば返却します。<br>
	 * 値に環境変数が指定されている場合は置換して返却します。
	 * </p>
	 * @param key キー
	 * @return キーに対する値
	 */
	public String getConv(String key) {
		String value = get(key);
		return convSystemProp(value);
	}

	/**
	 * web.xmlのinit-parameter、context-parameterの値を取得します.
	 * @param key キー
	 * @return web.xmlのinit-parameter、context-parameterの値
	 */
	public String getContext(String key) {
		if (servletContext != null) {
			return servletContext.getInitParameter(key);
		}
		return null;
	}
	
	/**
	 * web.xmlのinit-parameter、context-parameterの値を取得します.
	 * <p>
	 * 値に環境変数が指定されている場合は置換して返却します。
	 * </p>
	 * @param key キー
	 * @return web.xmlのinit-parameter、context-parameterの値
	 */
	public String getContextConv(String key) {
		String value = getContext(key);
		return convSystemProp(value);
	}

	/**
	 * プロパティファイルの値を取得します.
	 * @param key キー
	 * @return 値
	 */
	public String getProperty(String key) {
		if (properties != null) {
			String val = properties.getProperty(key);
			if (val != null) {
				return val;
			}
		}
		return null;
	}
	
	/**
	 * プロパティファイルの値を取得します.
	 * <p>
	 * 値に環境変数が指定されている場合は置換して返却します。
	 * </p>
	 * @param key キー
	 * @return 値
	 */
	public String getPropertyConv(String key) {
		String value = getProperty(key);
		return convSystemProp(value);
	}

	/**
	 * 指定された値に環境変数が指定されている場合は置換して返却します。
	 * @param value 値
	 * @return 環境変数が指定されている場合置換した値
	 */
	public String convSystemProp(String value) {
		if (value != null) {
			Matcher matcher = pattern.matcher(value);
			String ret = new String(value);
			boolean isMatch = false;
			while (matcher.find()) {
				isMatch = true;
				String group1 = matcher.group(1);
				String propValue = System.getProperty(group1);
				//if (propValue == null) {
				//	throw new IllegalArgumentException("System Property ${" + group1 + "} is required.");
				//}
				if (!StringUtils.isBlank(propValue)) {
					ret = ret.replaceAll(REGEX_SYSTEM_PARAM_START + group1 + REGEX_SYSTEM_PARAM_END, 
							propValue);
				} else {
					logger.info("System Property ${" + group1 + "} is required.");
					ret = null;
				}
			}
			if (isMatch) {
				return ret;
			}
		}
		return value;
	}
	
	/**
	 * ServletContextとプロパティファイルから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	public Map<String, String> getParamsConv(String prefix) {
		Map<String, String> params = new HashMap<String, String>();
		setMapConv(params, prefix);
		return params;
	}
	
	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	public Map<String, String> getContextParamsConv(String prefix) {
		Map<String, String> params = new HashMap<String, String>();
		setContextMapConv(params, prefix);
		return params;
	}
	
	/**
	 * プロパティファイルから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	public Map<String, String> getPropertyParamsConv(String prefix) {
		Map<String, String> params = new HashMap<String, String>();
		setPropertyMapConv(params, prefix);
		return params;
	}

	/**
	 * ServletContextとプロパティファイルから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 */
	public Map<String, String> getParams(String prefix) {
		Map<String, String> params = new HashMap<String, String>();
		setMap(params, prefix);
		return params;
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 */
	public Map<String, String> getContextParams(String prefix) {
		Map<String, String> params = new HashMap<String, String>();
		setContextMap(params, prefix);
		return params;
	}

	/**
	 * プロパティファイルから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 */
	public Map<String, String> getPropertyParams(String prefix) {
		Map<String, String> params = new HashMap<String, String>();
		setPropertyMap(params, prefix);
		return params;
	}

	/**
	 * ServletContextとプロパティファイルから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	private void setMapConv(Map<String, String> params, String prefix) {
		setContextMapConv(params, prefix);
		setPropertyMapConv(params, prefix);
	}
	
	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	private void setContextMapConv(Map<String, String> params, String prefix) {
		if (params != null && prefix != null && prefix.length() > 0) {
			if (servletContext != null) {
				Enumeration<String> enu = this.servletContext.getInitParameterNames();
				while (enu.hasMoreElements()) {
					String name = enu.nextElement();
					if (name.startsWith(prefix)) {
						String editKey = convSystemProp(name);
						if (!StringUtils.isBlank(editKey)) {
							params.put(editKey, getConv(name));
						}
					}
				}
			}
		}
	}
	
	/**
	 * プロパティファイルから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	private void setPropertyMapConv(Map<String, String> params, String prefix) {
		if (params != null && prefix != null && prefix.length() > 0) {
			if (properties != null) {
				Enumeration enu = this.properties.propertyNames();
				while (enu.hasMoreElements()) {
					String name = (String)enu.nextElement();
					if (name.startsWith(prefix)) {
						params.put(convSystemProp(name), getConv(name));
					}
				}
			}
		}
	}

	/**
	 * ServletContextとプロパティファイルから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 */
	private void setMap(Map<String, String> params, String prefix) {
		setContextMap(params, prefix);
		setPropertyMap(params, prefix);
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 */
	private void setContextMap(Map<String, String> params, String prefix) {
		if (params != null && prefix != null && prefix.length() > 0) {
			if (servletContext != null) {
				Enumeration<String> enu = this.servletContext.getInitParameterNames();
				while (enu.hasMoreElements()) {
					String name = enu.nextElement();
					if (name.startsWith(prefix)) {
						params.put(name, get(name));
					}
				}
			}
		}
	}

	/**
	 * プロパティファイルから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 */
	private void setPropertyMap(Map<String, String> params, String prefix) {
		if (params != null && prefix != null && prefix.length() > 0) {
			if (properties != null) {
				Enumeration enu = this.properties.propertyNames();
				while (enu.hasMoreElements()) {
					String name = (String)enu.nextElement();
					if (name.startsWith(prefix)) {
						params.put(name, get(name));
					}
				}
			}
		}
	}

	/**
	 * ServletContextとプロパティファイルから、
	 * キーの先頭が指定されたprefixの情報をSortedMapにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	public SortedMap<String, String> getSortedParamsConv(String prefix) {
		SortedMap<String, String> params = new TreeMap<String, String>();
		setMapConv(params, prefix);
		return params;
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報をSortedMapにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	public SortedMap<String, String> getContextSortedParamsConv(String prefix) {
		SortedMap<String, String> params = new TreeMap<String, String>();
		setContextMapConv(params, prefix);
		return params;
	}

	/**
	 * プロパティファイルから、キーの先頭が指定されたprefixの情報をSortedMapにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	public SortedMap<String, String> getPropertySortedParamsConv(String prefix) {
		SortedMap<String, String> params = new TreeMap<String, String>();
		setPropertyMapConv(params, prefix);
		return params;
	}

	/**
	 * ServletContextとプロパティファイルから、
	 * キーの先頭が指定されたprefixの情報をSortedMapにして返却します。
	 */
	public SortedMap<String, String> getSortedParams(String prefix) {
		SortedMap<String, String> params = new TreeMap<String, String>();
		setMap(params, prefix);
		return params;
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報をSortedMapにして返却します。
	 */
	public SortedMap<String, String> getContextSortedParams(String prefix) {
		SortedMap<String, String> params = new TreeMap<String, String>();
		setContextMap(params, prefix);
		return params;
	}

	/**
	 * プロパティファイルから、キーの先頭が指定されたprefixの情報をSortedMapにして返却します。
	 */
	public SortedMap<String, String> getPropertySortedParams(String prefix) {
		SortedMap<String, String> params = new TreeMap<String, String>();
		setPropertyMap(params, prefix);
		return params;
	}

	/**
	 * ServletContextとプロパティファイルから、
	 * キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	public Set<String> getSetConv(String prefix) {
		Set<String> params = new HashSet<String>();
		setSetConv(params, prefix);
		return params;
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	public Set<String> getContextSetConv(String prefix) {
		Set<String> params = new HashSet<String>();
		setContextSetConv(params, prefix);
		return params;
	}

	/**
	 * プロパティファイルから、キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	public Set<String> getPropertySetConv(String prefix) {
		Set<String> params = new HashSet<String>();
		setPropertySetConv(params, prefix);
		return params;
	}

	/**
	 * ServletContextとプロパティファイルから、
	 * キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 */
	public Set<String> getSet(String prefix) {
		Set<String> params = new HashSet<String>();
		setSet(params, prefix);
		return params;
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 */
	public Set<String> getContextSet(String prefix) {
		Set<String> params = new HashSet<String>();
		setContextSet(params, prefix);
		return params;
	}

	/**
	 * プロパティファイルから、キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 */
	public Set<String> getPropertySet(String prefix) {
		Set<String> params = new HashSet<String>();
		setPropertySet(params, prefix);
		return params;
	}

	/**
	 * ServletContextとプロパティファイルから、
	 * キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	private void setSetConv(Set<String> params, String prefix) {
		setContextSetConv(params, prefix);
		setPropertySetConv(params, prefix);
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	private void setContextSetConv(Set<String> params, String prefix) {
		if (params != null && prefix != null && prefix.length() > 0) {
			if (servletContext != null) {
				Enumeration<String> enu = this.servletContext.getInitParameterNames();
				while (enu.hasMoreElements()) {
					String name = enu.nextElement();
					if (name.startsWith(prefix)) {
						params.add(getConv(name));
					}
				}
			}
		}
	}

	/**
	 * プロパティファイルから、キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 * 先頭に$の付いた値は、システムプロパティの値を返却します。
	 */
	private void setPropertySetConv(Set<String> params, String prefix) {
		if (params != null && prefix != null && prefix.length() > 0) {
			if (properties != null) {
				Enumeration enu = this.properties.propertyNames();
				while (enu.hasMoreElements()) {
					String name = (String)enu.nextElement();
					if (name.startsWith(prefix)) {
						params.add(getConv(name));
					}
				}
			}
		}
	}

	/**
	 * ServletContextから、キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 */
	private void setSet(Set<String> params, String prefix) {
		setContextSet(params, prefix);
		setPropertySet(params, prefix);
	}

	/**
	 * ServletContextとプロパティファイルから、
	 * キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 */
	private void setContextSet(Set<String> params, String prefix) {
		if (params != null && prefix != null && prefix.length() > 0) {
			if (servletContext != null) {
				Enumeration<String> enu = this.servletContext.getInitParameterNames();
				while (enu.hasMoreElements()) {
					String name = enu.nextElement();
					if (name.startsWith(prefix)) {
						params.add(get(name));
					}
				}
			}
		}
	}

	/**
	 * プロパティファイルから、キーの先頭が指定されたprefixの情報のparam-valueをSetにして返却します。
	 */
	private void setPropertySet(Set<String> params, String prefix) {
		if (params != null && prefix != null && prefix.length() > 0) {
			if (servletContext != null) {
				Enumeration<String> enu = this.servletContext.getInitParameterNames();
				while (enu.hasMoreElements()) {
					String name = enu.nextElement();
					if (name.startsWith(prefix)) {
						params.add(get(name));
					}
				}
			}
		}
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	protected Properties getProperties() {
		return properties;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ServletContextUtil getDefault() {
		if (plugin == null) {
			throw new IllegalStateException("The Web Application has not been activated.");
		}
		return plugin;
	}

}
