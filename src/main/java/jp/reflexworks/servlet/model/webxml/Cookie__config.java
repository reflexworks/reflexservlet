package jp.reflexworks.servlet.model.webxml;

import java.io.Serializable;

public class Cookie__config implements Serializable {

	public String name;
	public String path;
	public String comment;
	public String http__only;
	public String secure;
	public String max__age;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getHttp__only() {
		return http__only;
	}
	public void setHttp__only(String http__only) {
		this.http__only = http__only;
	}
	public String getSecure() {
		return secure;
	}
	public void setSecure(String secure) {
		this.secure = secure;
	}
	public String getMax__age() {
		return max__age;
	}
	public void setMax__age(String max__age) {
		this.max__age = max__age;
	}

	@Override
	public String toString() {
		return "Cookie__config [name=" + name + ", path=" + path + ", comment="
				+ comment + ", http__only=" + http__only + ", secure=" + secure
				+ ", max__age=" + max__age + "]";
	}

}
