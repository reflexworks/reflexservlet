package jp.reflexworks.websocket;

import java.util.concurrent.ConcurrentSkipListSet;

public abstract class ReflexWebSocket extends ReflexWebSocketBase {
 
    protected static ConcurrentSkipListSet<ReflexWebSocket> connections = 
    		new ConcurrentSkipListSet<ReflexWebSocket>();
    
    public void onOpen(Connection conn) {
    	super.onOpen(conn);
        connections.add(this);
    }
 
    public void onClose(int code, String str) {
    	super.onClose(code, str);
        connections.remove(this);
    }

	public static ConcurrentSkipListSet<ReflexWebSocket> getConnections() {
		return connections;
	}
	
	// 利用側でonMessageを実装してください。

}
