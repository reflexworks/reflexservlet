package jp.reflexworks.test.servlet;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * ServletInputStreamを継承したクラス.
 */
public class TestInputStream extends ServletInputStream {

	/** InputStream */
	private InputStream in;

	/**
	 * コンストラクタ.
	 * @param payload データ
	 */
	public TestInputStream(byte[] payload) {
		this.in = new BufferedInputStream(
				new ByteArrayInputStream(payload));
	}

	/**
	 * read.
	 * @return int
	 */
	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public boolean isFinished() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean isReady() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public void setReadListener(ReadListener readListener) {
		// TODO 自動生成されたメソッド・スタブ

	}

}
