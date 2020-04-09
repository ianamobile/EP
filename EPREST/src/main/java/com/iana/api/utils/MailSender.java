package com.iana.api.utils;

import java.io.File;
import java.util.Date;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iana.api.config.CustomEmailConfig;

@Component
public class MailSender extends Authenticator {

	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	private CustomEmailConfig customEmailConfig;

	/**
	 * @param to[]
	 * @param from
	 *            : this will be a username
	 * @param password
	 * @param subject
	 * @param content
	 *            : will be normal or html form
	 * @param contentType
	 *            : can be blank or html
	 * @param attachment[]
	 * @param attachmentRequired
	 *            : true / false
	 * @param imagesForBody[]
	 *            : value should be absolute file path, in html img tag src value will be "cid:i"
	 *            (i=start from 1....etc)
	 * @param extraMap
	 * @return Map<String, String> : key will be SUCCESS / FAILURE
	 */
	public String sendMail(String to[], String from, String password, String subject, String content, String contentType, String attachment[], Boolean attachmentRequired, String imagesForBody[],
		Map<String, Object> extraMap) {

		final String pass = password;

		String contentTypeHTML = "text/html";

		String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

		Session session = Session.getInstance(customEmailConfig.getEmailProp());

		if (StringUtils.isBlank(pass)) {
			throw new IllegalArgumentException(customEmailConfig.getEmailProp().getProperty("msg_error_invalid_password"));
		}

		// validate from mailid
		if (StringUtils.isBlank(from) || !from.matches(EMAIL_PATTERN)) {
			throw new IllegalArgumentException(customEmailConfig.getEmailProp().getProperty("msg_error_invalid_sender"));
		}

		if (to == null) {
			throw new IllegalArgumentException(customEmailConfig.getEmailProp().getProperty("msg_error_invalid_receiver"));
		}

		if (StringUtils.isBlank(subject)) {
			throw new IllegalArgumentException(customEmailConfig.getEmailProp().getProperty("msg_error_blank_subject"));
		}

		if (content == null) {
			throw new IllegalArgumentException(customEmailConfig.getEmailProp().getProperty("msg_error_invalid_content"));
		}

		if (contentType == null) {
			throw new IllegalArgumentException(customEmailConfig.getEmailProp().getProperty("msg_error_invalid_content_type"));
		}

		InternetAddress[] toAddresses = new InternetAddress[to.length];

		// read to mail-id and store into InternetAddress object
		for (int i = 0; i < to.length; i++) {

			if (StringUtils.isBlank(to[i]) || !to[i].matches(EMAIL_PATTERN)) {
				throw new IllegalArgumentException(customEmailConfig.getEmailProp().getProperty("msg_error_invalid_receiver"));
			}

			try {
				toAddresses[i] = new InternetAddress(to[i]);

			} catch (AddressException e) {
				throw new IllegalArgumentException(customEmailConfig.getEmailProp().getProperty("msg_error_invalid_email_address"));
			}
		}

		// Create a multipart message
		Multipart multipart = new MimeMultipart();

		try {

			// creates a new e-mail message
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO, toAddresses);
			msg.setSubject(subject);
			msg.setSentDate(new Date());

			// Create the message part
			BodyPart messageBodyPart = new MimeBodyPart();

			// Now set the actual message
			if (StringUtils.isBlank(contentType) || !GlobalVariables.EMAIL_TYPE_HTML.equalsIgnoreCase(contentType)) {
				messageBodyPart.setText(content);
			} else {

				// match html string
				messageBodyPart.setContent(content, contentTypeHTML);
			}

			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			// Part two is attachment
			String fileNotFoundName = StringUtils.EMPTY;
			if (attachmentRequired) {

				if (attachment == null) {
					return customEmailConfig.getEmailProp().getProperty("msg_error_missing_attachment");
				}

				for (String attachmentString : attachment) {

					messageBodyPart = new MimeBodyPart();

					String fileName = attachmentString;
					File file = new File(fileName);
					if (file.exists()) {
						DataSource source = new FileDataSource(fileName);
						messageBodyPart.setDataHandler(new DataHandler(source));
						String newFileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
						if ((fileName.lastIndexOf("\\") + 1) <= 0) {
							newFileName = fileName.substring(fileName.lastIndexOf("/") + 1);
						}
						messageBodyPart.setFileName(newFileName);
						multipart.addBodyPart(messageBodyPart);

					} else {
						fileNotFoundName = "," + file.getName();
					}

				}

				if (StringUtils.isNotBlank(fileNotFoundName)) {

					return customEmailConfig.getEmailProp().getProperty("msg_error_invalid_input_files") + fileNotFoundName.substring(1);
				}
			}

			// code to display images in mail body in html starts
			if (null != imagesForBody && imagesForBody.length > 0) {
				Boolean isFileExist = true;
				for (int i = 0; i < imagesForBody.length; i++) {
					if (null != imagesForBody[i]) {
						String filePath = imagesForBody[i];
						if ((new File(filePath)).isFile()) {

							messageBodyPart = new MimeBodyPart();
							DataSource fds = new FileDataSource(filePath);

							messageBodyPart.setDataHandler(new DataHandler(fds));
							messageBodyPart.setHeader("Content-ID", "<" + (i + 1) + ">");
							messageBodyPart.setFileName(new File(filePath).getName());
							multipart.addBodyPart(messageBodyPart);

						} else {
							isFileExist = false;
						}
					}
				}
				if (!isFileExist) {
					return customEmailConfig.getEmailProp().getProperty("msg_error_invalid_files_images");
				}
			}

			// code to display images in mail body in html end

			// Send the complete message parts
			msg.setContent(multipart);

			// sends the e-mail
			Transport.send(msg);

			return StringUtils.EMPTY;

		} catch (AuthenticationFailedException e) {
			log.error("AuthenticationFailedException::", e);
			return customEmailConfig.getEmailProp().getProperty("msg_error_auth_failed_exception");

		} catch (MessagingException e) {
			log.error("MessagingException::", e);
			return customEmailConfig.getEmailProp().getProperty("msg_error_messaging_exception");
		} catch (Exception e) {
			log.error("Exception::", e);
			return customEmailConfig.getEmailProp().getProperty("msg_error_exception");
		}
	}

}
