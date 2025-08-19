package jp.reflexworks.servlet.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;

/**
 * Mapのキー、CollectionをEnumerationに変換するクラス.
 * Requestをラップして作成する場合に使用。
 */
public class EnumerationConverter implements Enumeration<String> {
	
	/** Iterator */
	private Iterator<String> it;
	
	/**
	 * コンストラクタ.
	 * MapのキーがEnumerationの対象になります。値は無視されます。
	 * @param parameterMap Map
	 */
	public EnumerationConverter(Map<String, ?> parameterMap) {
		if (parameterMap != null) {
			it = parameterMap.keySet().iterator();
		}
	}
	
	/**
	 * コンストラクタ
	 * @param parameter Collection
	 */
	public EnumerationConverter(Collection<String> parameter) {
		if (parameter != null) {
			it = parameter.iterator();
		}
	}
	
	/**
	 * 次のelementがあるかどうか.
	 * @return 次のelementがある場合true
	 */
	public boolean hasMoreElements() {
		if (it == null) {
			return false;
		}
		return it.hasNext();
	}
	
	/**
	 * 次のelementを取得.
	 * @return 次のelement
	 */
	public String nextElement() {
		if (it == null) {
			return null;
		}
		return it.next();
	}

}
