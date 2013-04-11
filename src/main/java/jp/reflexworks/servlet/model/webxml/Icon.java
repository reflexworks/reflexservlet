package jp.reflexworks.servlet.model.webxml;

import java.io.Serializable;

public class Icon implements Serializable {
	
	public String small__icon;
	public String large__icon;

	public String getSmall__icon() {
		return small__icon;
	}
	public void setSmall__icon(String small__icon) {
		this.small__icon = small__icon;
	}
	public String getLarge__icon() {
		return large__icon;
	}
	public void setLarge__icon(String large__icon) {
		this.large__icon = large__icon;
	}

	@Override
	public String toString() {
		return "Icon [small__icon=" + small__icon + ", large__icon="
				+ large__icon + "]";
	}

}
