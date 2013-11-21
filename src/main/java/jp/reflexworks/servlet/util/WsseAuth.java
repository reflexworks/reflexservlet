package jp.reflexworks.servlet.util;

import java.io.Serializable;

public class WsseAuth implements Serializable {

	private static final long serialVersionUID = 1L;
	
	//public static enum AuthFormat {WSSE, RXID, ATTRIBUTE, SESSION};

	/** ユーザ名 */
	public String username;
	/** PasswordDigest */
	public String passwordDigest;
	/** Nonce */
	public String nonce;
	/** Created */
	public String created;
	/** パスワード */
	public String password;
	/** ワンタイムかどうか */
	//public boolean isOnetime;
	/** RXIDかどうか */
	public boolean isRxid;
	/** Cookieに設定されているかどうか */
	//public boolean isCookie;
	/** WsseAuth形式 */
	//public AuthFormat authFormat;
	
	public WsseAuth(String username, String passwordDigest, String nonce, 
			String created) {
		this.username = username;
		this.passwordDigest = passwordDigest;
		this.nonce = nonce;
		this.created = created;
	}
	
	@Override
	public String toString() {
		return "WsseAuth [username=" + username + ", passwordDigest="
				+ passwordDigest + ", nonce=" + nonce + ", created="
				+ created + ", isRxid=" + isRxid
				+ "]";
	}

}
