package jp.reflexworks.servlet.model.webxml;

import java.io.Serializable;

public class Session__config implements Serializable {

	public String session__timeout;
	public String tracking__mode;
	public Cookie__config cookie__config;

	public String getSession__timeout() {
		return session__timeout;
	}
	public void setSession__timeout(String session__timeout) {
		this.session__timeout = session__timeout;
	}
	public String getTracking__mode() {
		return tracking__mode;
	}
	public void setTracking__mode(String tracking__mode) {
		this.tracking__mode = tracking__mode;
	}
	public Cookie__config getCookie__config() {
		return cookie__config;
	}
	public void setCookie__config(Cookie__config cookie__config) {
		this.cookie__config = cookie__config;
	}

	@Override
	public String toString() {
		return "Session__config [session__timeout=" + session__timeout
				+ ", tracking__mode=" + tracking__mode + ", cookie__config="
				+ cookie__config + "]";
	}

}
