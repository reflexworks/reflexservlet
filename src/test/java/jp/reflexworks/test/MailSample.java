package jp.reflexworks.test;

import java.io.IOException;
import java.util.Properties;
import java.util.Date;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class MailSample {

	static String msgTitle = "JavaMail送信テスト";
	static String msgText = "メール送信テスト。\n２行目です。\n3行目です。";

	/**
	 * メール送信テスト
	 * @param args
	 * to@example.com from@example.net password smtp.gmail.com 587 true
	 * <ol>
	 * <li>宛先(to)</li>
	 * <li>送信元(from)</li>
	 * <li>送信元パスワード</li>
	 * <li>smtpサーバ</li>
	 * <li>ポート番号</li>
	 * <li>メール送信プロトコル "smtp"か"smtps"を指定する。</li>
	 * <li>STARTTLSを有効にする場合true、有効にしない場合false</li>
	 * <li>(テスト用)デバッグ機能をONにする場合true, OFFにする場合false</li>
	 * </ol>
	 */
	public static void main(String[] args) {

		System.out.println("MailSample start.");
		
		if (args.length != 6) {
			usage();
			System.exit(1);
		}

		System.out.println();

		String to = args[0];
		String from = args[1];
		String password = args[2];
		String host = args[3];
		String port = args[4];
		
		// TODO test
		//String smtpUser = from;
		//from = "=?iso-2022-jp?B?SVRwcm8bJEIlYSE8JWsbKEI=?= <" + from + ">";
		// msg.setFrom(new InternetAddress(msg_from,"お名前","iso-2022-jp"));
		
		boolean debug = Boolean.valueOf(args[5]).booleanValue();

        Transport transport = null;
		try {
			InternetAddress iaFrom = new InternetAddress(from, "", "iso-2022-jp");
			InternetAddress iaTo = new InternetAddress(to, "", "iso-2022-jp");

			Properties props = new Properties();
			props.put("mail.smtp.host", host);
			props.put("mail.host", host);
			props.put("mail.from", from);
			props.put("mail.smtp.port", port);  // サブミッションポート
			props.put("mail.smtp.auth", "true");   // SMTP 認証を行う
			props.put("mail.smtp.starttls.enable", "true");   // STARTTLS
			
			// TODO test
			//props.put("mail.smtp.user", smtpUser);
			
			if (debug) {
				props.put("mail.debug", args[3]);
			}

			Session session = Session.getInstance(props);
			//SimpleAuthenticator sa = new SimpleAuthenticator(from, password);
			//Session session = Session.getInstance(props, sa);
			session.setDebug(debug);
			
			MimeMessage msg = new MimeMessage(session);
			//msg.setFrom(new InternetAddress(from));
			msg.setFrom(iaFrom);
			//InternetAddress[] address = InternetAddress.parse(to);
			//msg.setRecipients(Message.RecipientType.TO, address);
			msg.setRecipient(Message.RecipientType.TO, iaTo);
			msg.setSubject(msgTitle, "ISO-2022-JP");
			msg.setSentDate(new Date());

			//msg.setText(msgText, "ISO-2022-JP");
			//msg.setContent(msgText, "text/plain; charset=ISO-2022-JP");

			MimeMultipart multipart = new MimeMultipart();
			msg.setContent(multipart);
			MimeBodyPart body = new MimeBodyPart();
			body.setContent(msgText, "text/plain; charset=ISO-2022-JP");
			//String encodeText = MimeUtility.encodeText(msgText, "ISO-2022-JP", "Q");	// これだと本文が化ける
			//body.setContent(encodeText, "text/plain; charset=ISO-2022-JP");
			multipart.addBodyPart(body);

			msg.setHeader("Content-Transfer-Encoding", "7bit");

			//Transport.send(msg);
            //transport = session.getTransport("smtp");
            transport = session.getTransport("smtps");
            transport.connect(from, password);
            transport.sendMessage(msg, msg.getAllRecipients());
		
    		System.out.println("MailSample end.");

		} catch (MessagingException mex) {
			System.out.println("¥n--Exception handling in msgsendsample.java");
			mex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
            if (transport != null) {
            	try {
            		transport.close();
        		} catch (MessagingException mex) {
        			mex.printStackTrace();
        		}
            }
		}
	}

	private static void usage() {
		System.out.println(
				"usage: java msgsendsample <to> <from> <password> <smtp> <port> true|false");
	}

}
