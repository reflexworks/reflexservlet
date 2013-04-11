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
	public static final String PARAM_PROPERTY = "context.properties";
	
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
	
	public String get(String key) {
		if (key == null) {
			return null;
		}
		if (properties != null) {
			String val = properties.getProperty(key);
			if (val != null) {
				return val;
			}
		}
		if (servletContext != null) {
			return servletContext.getInitParameter(key);
		}
		return null;
	}
	
	public String getConv(String key) {
		String value = get(key);
		return convSystemProp(value);
	}
	
	public String convSystemProp(String value) {
		if (value != null) {
			Matcher matcher = pattern.matcher(value);
			String ret = new String(value);
			boolean isMatch = false;
			while (matcher.find()) {
				isMatch = true;
				String group1 = matcher.group(1);
				String propValue = System.getProperty(group1);
				if (propValue == null) {
					throw new IllegalArgumentException("System Property ${" + group1 + "} is required.");
				}
				ret = ret.replaceAll(REGEX_SYSTEM_PARAM_START + group1 + REGEX_SYSTEM_PARAM_END, 
						propValue);
			}
			if (isMatch) {
				return ret;
			}
		}
		return value;
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
		if (params != null && prefix != null && prefix.length() > 0) {
			if (servletContext != null) {
				Enumeration<String> enu = this.servletContext.getInitParameterNames();
				while (enu.hasMoreElements()) {
					String name = enu.nextElement();
					if (name.startsWith(prefix)) {
						params.put(convSystemProp(name), getConv(name));
					}
				}
			}
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
	 * ServletContextから、キーの先頭が指定されたprefixの情報をMapにして返却します。
	 */
	private void setMap(Map<String, String> params, String prefix) {
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
			if (properties != null) {
				Enumeration enu = this.properties.propertyNames();
				while (enu.hasMoreElements()) {
					String name = (String)enu.nextElement();
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
