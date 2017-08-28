package jp.reflexworks.test.servlet;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Part;

/**
 * Partを継承したクラス.
 */
public class TestPart implements Part {
	
	/** payload */
	private byte[] payload;
	/** header */
	private Map<String, List<String>> headers = new HashMap<String, List<String>>();	// TODO コンストラクタで生成
	/** name */
	private String name;
	
	/**
	 * コンストラクタ
	 * @param name name
	 * @param payload payload
	 * @param pHeaders headers
	 */
	public TestPart(String name, byte[] payload, Map<String, String> pHeaders) {
		this.name = name;
		this.payload = payload;
		if (pHeaders != null) {
			for (Map.Entry<String, String> mapEntry : pHeaders.entrySet()) {
				String hKey = mapEntry.getKey();
				List<String> hVal = new ArrayList<String>();
				hVal.add(mapEntry.getValue());
				this.headers.put(hKey, hVal);
			}
		}
	}

	public InputStream getInputStream() throws IOException {
		if (payload != null) {
			return new BufferedInputStream(new ByteArrayInputStream(payload));
		}
		return null;
	}

	public String getContentType() {
		return getHeader("Content-Type");
	}

	public String getName() {
		return name;
	}

	public long getSize() {
		if (payload != null) {
			return payload.length;
		}
		return 0;
	}

	public void write(String fileName) throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void delete() throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public String getHeader(String name) {
		List<String> val = headers.get(name);
		if (val != null && val.size() > 0) {
			return val.get(0);
		}
		return null;
	}

	public Collection<String> getHeaders(String name) {
		return headers.get(name);
	}

	public Collection<String> getHeaderNames() {
		return headers.keySet();
	}

}