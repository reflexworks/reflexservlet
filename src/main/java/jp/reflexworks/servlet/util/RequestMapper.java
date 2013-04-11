package jp.reflexworks.servlet.util;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

import jp.sourceforge.reflex.util.FieldMapper;

public class RequestMapper extends FieldMapper{

	public void setValue(HttpServletRequest source, Object target) {

		Field[] fields = target.getClass().getDeclaredFields();

		for (Field fld : fields) {

			String setter = "set" + fld.getName().substring(0, 1).toUpperCase() + fld.getName().substring(1);

			try {
				String propvalueStr = source.getParameter(fld.getName());

				if (propvalueStr != null) {

					Object propvalue = null;

					Class type = fld.getType();
					if (type.equals(Integer.class)) {
						propvalue = new Integer(propvalueStr);
					} else if (type.equals(Long.class)) {
						propvalue = new Long(propvalueStr);
					} else if (type.equals(Float.class)) {
						propvalue = new Float(propvalueStr);
					} else if (type.equals(Double.class)) {
						propvalue = new Double(propvalueStr);
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
				// Do Nothing
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}

		}
	}


}
