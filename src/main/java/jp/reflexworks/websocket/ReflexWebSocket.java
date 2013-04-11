package jp.reflexworks.websocket;

import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.jetty.websocket.WebSocket;

public abstract class ReflexWebSocket 
implements WebSocket, WebSocket.OnTextMessage, Comparable<ReflexWebSocket> {

    protected Connection conn;
    protected int ms;
 
    protected static ConcurrentSkipListSet<ReflexWebSocket> connections = 
    		new ConcurrentSkipListSet<ReflexWebSocket>();
    
    public void onOpen(Connection conn) {
        this.conn = conn;
        if (ms > 0) {
        	this.conn.setMaxIdleTime(ms);
        }
        connections.add(this);
    }
 
    public void onClose(int code, String str) {
        connections.remove(this);
    }
    
    public int compareTo(ReflexWebSocket rw) {
    	if (rw == null) {
    		return -1;
    	}
    	return rw.hashCode() - this.hashCode();
    }
    
    public void setMaxIdleTime(int ms) {
    	this.ms = ms;
    }
 
}
