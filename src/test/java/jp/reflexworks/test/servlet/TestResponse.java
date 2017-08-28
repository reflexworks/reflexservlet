package jp.reflexworks.test.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * テスト用レスポンスクラス
 */
public class TestResponse implements HttpServletResponse {

	public String getCharacterEncoding() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public String getContentType() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public ServletOutputStream getOutputStream() throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public PrintWriter getWriter() throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public void setCharacterEncoding(String charset) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void setContentLength(int len) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void setContentType(String type) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void setBufferSize(int size) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public int getBufferSize() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void flushBuffer() throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void resetBuffer() {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public boolean isCommitted() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	public void reset() {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void setLocale(Locale loc) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public Locale getLocale() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public void addCookie(Cookie cookie) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public boolean containsHeader(String name) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	public String encodeURL(String url) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public String encodeRedirectURL(String url) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public String encodeUrl(String url) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public String encodeRedirectUrl(String url) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public void sendError(int sc, String msg) throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void sendError(int sc) throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void sendRedirect(String location) throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void setDateHeader(String name, long date) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void addDateHeader(String name, long date) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void setHeader(String name, String value) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void addHeader(String name, String value) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void setIntHeader(String name, int value) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void addIntHeader(String name, int value) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void setStatus(int sc) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public void setStatus(int sc, String sm) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	public int getStatus() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public String getHeader(String name) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public Collection<String> getHeaders(String name) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public Collection<String> getHeaderNames() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
