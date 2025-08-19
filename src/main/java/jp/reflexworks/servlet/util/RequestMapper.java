package jp.reflexworks.servlet.util;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;

import jp.sourceforge.reflex.util.FieldMapper;

public class RequestMapper extends FieldMapper {

	private static Logger logger = Logger.getLogger(RequestMapper.class.getName());

	public RequestMapper(boolean isReflexField) {
		super(isReflexField);
	}

	public void setValue(HttpServletRequest source, Object target) {

		Field[] fields = target.getClass().getDeclaredFields();

		for (Field fld : fields) {

			//String setter = "set" + fld.getName().substring(0, 1).toUpperCase() + fld.getName().substring(1);

			String setter = getSetter(fld, isReflexField);
			
			try {
				String propvalueStr = source.getParameter(fld.getName());

				if (propvalueStr != null) {

					Object propvalue = null;

					Class type = fld.getType();
					if (type.equals(Integer.class)) {
						propvalue = Integer.parseInt(propvalueStr);
					} else if (type.equals(Long.class)) {
						propvalue = Long.parseLong(propvalueStr);
					} else if (type.equals(Float.class)) {
						propvalue = Float.parseFloat(propvalueStr);
					} else if (type.equals(Double.class)) {
						propvalue = Double.parseDouble(propvalueStr);
					} else if (type.equals(int.class)) {
						propvalue = Integer.parseInt(propvalueStr);
					} else if (type.equals(long.class)) {
						propvalue = Long.parseLong(propvalueStr);
					} else if (type.equals(float.class)) {
						propvalue = Float.parseFloat(propvalueStr);
					} else if (type.equals(double.class)) {
						propvalue = Double.parseDouble(propvalueStr);
					} else {
						propvalue = propvalueStr;
					}

					this.setValue(target, type, propvalue, setter);
				}

			} catch (NumberFormatException e) {
				if (logger.isLoggable(Level.FINE)) {
					logger.log(Level.FINE, "NumberFormatException", e);
				}
			} catch (IllegalArgumentException e) {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "IllegalArgumentException", e);
				}
			}

		}
	}


}
