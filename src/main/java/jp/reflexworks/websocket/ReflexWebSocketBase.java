package jp.reflexworks.websocket;

import org.eclipse.jetty.websocket.WebSocket;

public abstract class ReflexWebSocketBase 
implements WebSocket, WebSocket.OnTextMessage, Comparable<ReflexWebSocketBase> {

	/** WebSocket connection */
    protected Connection conn;
    /** Max idle time (millisecond) */
    protected int ms;
    
    public void onOpen(Connection conn) {
        this.conn = conn;
        if (ms > 0) {
        	this.conn.setMaxIdleTime(ms);
        }
    }
 
    public void onClose(int code, String str) {
    }
    
    public int compareTo(ReflexWebSocketBase rw) {
    	if (rw == null) {
    		return -1;
    	}
    	return rw.hashCode() - this.hashCode();
    }
    
    public void setMaxIdleTime(int ms) {
    	this.ms = ms;
    }
    
    public int getMaxIdleTime() {
    	return ms;
    }

	public WebSocket.Connection getConnection() {
		return conn;
	}

}
