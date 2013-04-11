package jp.reflexworks.test;

import java.util.Properties;
import java.util.Date;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;

public class MailSample {

	static String msgTitle = "JavaMail送信テスト";
	static String msgText = "メール送信テスト。\n２行目です。";

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
		
		boolean debug = Boolean.valueOf(args[5]).booleanValue();

		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.host", host);
		props.put("mail.from", from);
		props.put("mail.smtp.port", port);  // サブミッションポート
		props.put("mail.smtp.auth", "true");   // SMTP 認証を行う
		props.put("mail.smtp.starttls.enable", "true");   // STARTTLS
		if (debug) {
			props.put("mail.debug", args[3]);
		}

		Session session = Session.getInstance(props);
		//SimpleAuthenticator sa = new SimpleAuthenticator(from, password);
		//Session session = Session.getInstance(props, sa);
		session.setDebug(debug);

        Transport transport = null;
		try {
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			//InternetAddress[] address = InternetAddress.parse(to);
			//msg.setRecipients(Message.RecipientType.TO, address);
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			msg.setSubject(msgTitle, "ISO-2022-JP");
			msg.setSentDate(new Date());

			//msg.setText(msgText, "ISO-2022-JP");
			msg.setContent(msgText, "text/plain; charset=ISO-2022-JP");
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
