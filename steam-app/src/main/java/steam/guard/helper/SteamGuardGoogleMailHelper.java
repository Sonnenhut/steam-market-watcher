package steam.guard.helper;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;

import steam.util.Constants;

public class SteamGuardGoogleMailHelper {


	private static final int MAIL_THRESHOLD_IN_MILLIS = 10000; // 10s
	private static final int WAIT_INTERVAL_MILLIS = 1000;
	private static final int PORT = 993;
	private static final String HOST = "imap.gmail.com";
	private static final String PROVIDER = "imaps";
	private static final String USER = Constants.GMAIL_USER;
	private static final String PASSWORD = Constants.GMAIL_PW;

	private static final String STEAM_EMAIL = "noreply@steampowered.com";

	@Inject
	private Logger logger;
	
	private Store store;

	public SteamGuardGoogleMailHelper() {
		try {
			Session session = Session.getDefaultInstance(new Properties(), null);
			store = session.getStore(PROVIDER);
		} catch (NoSuchProviderException e) {
			logger.error("unable to create mail store {}", e);
		}
	}

	public String waitForLatestCode() {
		String res = null;
		try {
			store.connect(HOST, PORT, USER, PASSWORD);
			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_ONLY);
			Date newestMessageDate;
			do {
				logger.trace("checking Mails for new SteamGuard Mail...");
				// get the list of inbox messages
				Message message = inbox.getMessage(inbox.getMessageCount());
				newestMessageDate = message.getSentDate();
				res = getSteamCodeFromMessage(message);
				if (res == null) {
					Thread.sleep(WAIT_INTERVAL_MILLIS);
					logger.info("waiting for new SteamGuard Mail...");
				}
			} while (res == null && !isOlderThanThreshhold(newestMessageDate));
			logger.info("found SteamGuard Code {}", res);
			inbox.close(true);
			store.close();
		} catch (MessagingException | InterruptedException | IOException e) {
			logger.error("error while processing mails {}", e);
			throw new RuntimeException("error while processing mails", e);
		}
		return res;
	}

	private boolean isOlderThanThreshhold(Date date) {
		return null != date && (System.currentTimeMillis() - date.getTime()) > MAIL_THRESHOLD_IN_MILLIS;
	}

	private String getSteamCodeFromMessage(Message message) throws IOException, MessagingException {
		String res = null;
		if (isMessageSentFromSteam(message)) {
			MimeMessage m = (MimeMessage) message;
			Object contentObject = m.getContent();
			if (contentObject instanceof Multipart) {
				Multipart content = (Multipart) contentObject;
				for (int i = 0; i < content.getCount(); i++) {
					BodyPart part = content.getBodyPart(i);
					if (part.isMimeType("text/html")) {
						// pass the html to processing
						res = SteamGuardHtmlHelper.getSteamGuardCodeFromHtml(part.getContent().toString());
						if (res != null) {
							break;
						}
					}
				}
			}
		}
		return res;
	}

	private boolean isMessageSentFromSteam(Message message) throws MessagingException {
		boolean res = false;
		for (Address sender : message.getFrom()) {
			logger.trace("checking sender of Mail: {0}", sender.toString());
			if (sender.toString().contains(STEAM_EMAIL)) {
				res = true;
				break;
			}
		}
		return res;
	}

}
