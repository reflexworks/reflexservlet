package jp.reflexworks.servlet.util;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;

public class MailUtil {
	
	public static final String SMTP = "smtp";
	public static final String SMTPS = "smtps";

	public static final String CHARSET = "ISO-2022-JP";
	public static final String CONTENT_TYPE = "text/plain; charset=" + CHARSET;
	public static final String ENCODING = "B";	// B or Q
	public static final Map<String, String> HEADERS = new HashMap<String, String>();
	static {
		HEADERS.put("Content-Transfer-Encoding", "7bit");
	}
	
	private static Logger logger = Logger.getLogger(MailUtil.class.getName());

	public void send(String title, String message, String to, String from, 
			String password, String host, String port, String protocol,
			boolean isStarttls, boolean debug) 
	throws IOException {
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.host", host);
		props.put("mail.from", from);
		props.put("mail.smtp.port", port);  // サブミッションポート
		props.put("mail.smtp.auth", "true");   // SMTP 認証を行う
		props.put("mail.smtp.starttls.enable", "true");   // STARTTLS
		if (debug) {
			props.put("mail.debug", "true");
		}
		
		send(title, message, to, from, password, props, protocol, debug);
	}
		
	public void send(String title, String message, String to, String from, 
			String password, Properties props, String protocol, boolean debug) 
	throws IOException {
		Session session = Session.getInstance(props);
		session.setDebug(debug);

        Transport transport = null;
		try {
			MimeMessage msg = new MimeMessage(session);
			
			// From
			msg.setFrom(new InternetAddress(from));
			// To
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			// Subject
			msg.setSubject(title, CHARSET);
			//String encodeTitle = MimeUtility.encodeText(title, CHARSET, ENCODING);
			//msg.setSubject(encodeTitle, CHARSET);
			// Date
			msg.setSentDate(new Date());
			// 本文
			//msg.setContent(message, CONTENT_TYPE);
			/*
			String encodeMessage = MimeUtility.encodeText(message, CHARSET, ENCODING);
			msg.setContent(encodeMessage, CONTENT_TYPE);
			*/
			MimeMultipart multipart = new MimeMultipart();
			msg.setContent(multipart);
			MimeBodyPart body = new MimeBodyPart();
			body.setContent(message, CONTENT_TYPE);
			multipart.addBodyPart(body);

			// ヘッダ
			for (Map.Entry<String, String> mapEntry : HEADERS.entrySet()) {
				msg.setHeader(mapEntry.getKey(), mapEntry.getValue());
			}

            transport = session.getTransport(protocol);
            transport.connect(from, password);
            transport.sendMessage(msg, msg.getAllRecipients());
		
    		//logger.info("send mail.");

		} catch (MessagingException e) {
			IOException ie = new IOException();
			ie.initCause(e);
			throw ie;

		} finally {
            if (transport != null) {
            	try {
            		transport.close();
        		} catch (MessagingException mex) {
        			logger.warning("MessagingException: " + mex.getMessage());
        		}
            }
		}
	}

}
