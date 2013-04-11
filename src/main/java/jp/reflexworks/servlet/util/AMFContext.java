package jp.reflexworks.servlet.util;

//import java.io.InputStream;
//import java.io.OutputStream;
import java.io.IOException;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;

//import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
import flex.messaging.MessageException;
import flex.messaging.io.MessageDeserializer;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.MessageSerializer;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.SmallMessage;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.endpoints.amf.SerializationFilter;
*/

/**
 * AMF通信で送信されたバイナリデータを、Flashから指定されたオブジェクトに変換します。
 * また、任意のオブジェクトをAMF通信用バイナリデータに変換します。
 * Flash側のチェックに必要な項目をメンバ変数に保持しています。
 * このため、このクラスはメンバ変数に定義せず、スコープを１リクエスト内で完結するようにしてください。
 * @author vtecadmin
 */
public class AMFContext {
	
	/*
	public static final String CONTENT_TYPE_AMF = "application/x-amf";
	//private static final String CLIENT_ID = "my-client";	// 初期設定不要
    private static final int RESPONSE_ERROR = 10308;	// SerializationFilter より

	//private Logger logger = Log.getLogger(LogCategories.ENDPOINT_GENERAL);
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private String messageId;
	private String responseURI;
	private String method;
	
	public Object readMessage(HttpServletRequest req, HttpServletResponse resp) 
	throws IOException, ClassNotFoundException {
		if (!CONTENT_TYPE_AMF.equalsIgnoreCase(req.getContentType())) {
			return null;
		}
		
		ActionContext context = new ActionContext();
		
        // Create an empty ActionMessage object to hold our response
        context.setResponseMessage(new ActionMessage());
        SerializationContext sc = getSerializationContext();

        try {
            // Deserialize the input stream into an "ActionMessage" object.
            MessageDeserializer deserializer = sc.newMessageDeserializer();

            // Set up the deserialization context
            InputStream in = req.getInputStream();
            deserializer.initialize(sc, in, null);

            // record the length of the input stream for performance metrics
            int reqLen = req.getContentLength();
            context.setDeserializedBytes(reqLen);
		
	        ActionMessage m = new ActionMessage();
	        context.setRequestMessage(m);
	        deserializer.readMessage(m, context);
	        
	        MessageBody reqMessageBody = context.getRequestMessageBody();
	        this.responseURI = reqMessageBody.getResponseURI();
	        
	        Object o = reqMessageBody.getData();
	        Message message = null;
	        if (o != null) {
		        if (o instanceof Message) {
		        	message = (Message)o;
		        } else if (o instanceof Object[]) {
		        	Object[] oarr = (Object[])o;
		        	if (oarr.length > 0 && oarr[0] instanceof Message) {
			        	message = (Message)oarr[0];
		        	}
		        }
	        }
	        
	        if (message != null) {
	        	this.messageId = message.getMessageId();
	        }
	        
	        if (message instanceof CommandMessage) {
	        	// 初期アクセス
	        	// 一度クライアントにレスポンスを返し、データを受け取る必要がある。
	        	ActionMessage actionMessage = getInitMessage();
	        	writeResponse(resp, actionMessage, false);
	        	
	        } else if (message instanceof RemotingMessage) {
	        	// データ
	        	RemotingMessage remotingMessage = (RemotingMessage)message;
	        	this.method = remotingMessage.getOperation();
		        List parameters = remotingMessage.getParameters();
		        if (parameters != null && parameters.size() == 1) {
		        	return parameters.get(0);
		        }
			    return parameters;
	        }
	        	
	        return message;

        } catch (Throwable t) {
        	logger.log(Level.SEVERE, t.getMessage(), t);
        	// エラー処理
        	SerializationFilter.handleDeserializationException(context, t, null);
            ActionMessage respMesg = context.getResponseMessage();
        	writeResponse(resp, respMesg, true);
        }
		
        return null;
	}
	*/
    
    public void writeMessage(HttpServletResponse resp, Object obj) 
    throws IOException {
    	/*
    	// ObjectをActionMessageに変換
    	ActionMessage actionMessage = getRespMessage(obj);
    	writeResponse(resp, actionMessage, false);
    	*/
    }

    /*
	public void serialize(ActionMessage respMesg, OutputStream out, boolean isError) 
	throws IOException {
		SerializationContext sc = null;
		ActionContext context = new ActionContext();
		context.setRequestMessage(respMesg);
        try {
            // overhead calculation is only necessary when MPI is enabled
            sc = getSerializationContext();
            MessageSerializer serializer = sc.newMessageSerializer();
            serializer.initialize(sc, out, null);
            serializer.writeMessage(respMesg);
 
        } catch (Exception e) {
        	if (isError) {
                //Error serializing response
                MessageException ex = new MessageException();
                ex.setMessage(RESPONSE_ERROR);
                ex.setRootCause(e);
                throw ex;
        	}

        	SerializationFilter.handleSerializationException(sc, context, e, null);
            ActionMessage respMesg2 = context.getResponseMessage();
            serialize(respMesg2, out, true);
        }
	}
	
	private SerializationContext getSerializationContext() {
        SerializationContext sc = new SerializationContext();
        sc.setDeserializerClass(AmfMessageDeserializer.class);
        sc.setSerializerClass(AmfMessageSerializer.class);
        return sc;
	}
	
	// 初期アクセス用Message
	private ActionMessage getInitMessage() {
		return getActionMessage(getInitAcknowledgeMessage());
	}
	
	// データ返却用Message
	private ActionMessage getRespMessage(Object obj) {
		AcknowledgeMessage ack = new AcknowledgeMessage();
		ack.setBody(obj);
		return getActionMessage(ack);
	}
	
	private ActionMessage getActionMessage(AcknowledgeMessage ack) {
		//ack.setClientId(CLIENT_ID);
		ack.setCorrelationId(this.messageId);	// Flash側で送信したmessageIdと一致するかチェックしている。
		Message respMessage = convertToSmallMessage((Message)ack);
		
		MessageBody messageBody = new MessageBody();
		messageBody.setTargetURI(this.responseURI);	// Flash側でチェックしている。（次のmethodも必要）
		messageBody.setReplyMethod(MessageIOConstants.RESULT_METHOD);
		messageBody.setData(respMessage);
		
		ActionMessage actionMessage = new ActionMessage();
		actionMessage.setVersion(ActionMessage.CURRENT_VERSION);
		actionMessage.addBody(messageBody);
		
		return actionMessage;
	}
	
	private AcknowledgeMessage getInitAcknowledgeMessage() {
		AcknowledgeMessage ack = new AcknowledgeMessage();
		
		//ack.setTimestamp(System.currentTimeMillis());
		//ack.setHeader(CommandMessage.MESSAGING_VERSION, new Double(1.0));
		//ack.setHeader(Message.FLEX_CLIENT_ID_HEADER, CLIENT_ID);
	
		return ack;
	}

    public Message convertToSmallMessage(Message message) {
        if (message instanceof SmallMessage) {
            Message smallMessage = ((SmallMessage)message).getSmallMessage();
            if (smallMessage != null) {
                message = smallMessage;
            }
        }

        return message;
    }
    
    public void writeResponse(HttpServletResponse resp, ActionMessage respMsg,
    		boolean isError) 
    throws IOException {
    	OutputStream out = resp.getOutputStream();
        try {
        	addNoCacheHeaders(resp);
            resp.setContentType(CONTENT_TYPE_AMF);
            serialize(respMsg, out, isError);
        } finally {
        	out.close();
        }
    }
    
    private void addNoCacheHeaders(HttpServletResponse resp) {
        // For MSIE over HTTPS, set additional Cache-Control values.
        resp.addHeader(AbstractEndpoint.HEADER_NAME_CACHE_CONTROL, "no-cache");

        // Set an expiration date in the past as well.
        resp.setDateHeader(AbstractEndpoint.HEADER_NAME_EXPIRES, 946080000000L); //Approx Jan 1, 2000

        // Set Pragma no-cache header if we're not MSIE over HTTPS
        resp.setHeader(AbstractEndpoint.HEADER_NAME_PRAGMA, "no-cache");
    }

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	*/

}
