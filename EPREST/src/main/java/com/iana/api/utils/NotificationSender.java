package com.iana.api.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.iana.api.config.CustomEmailConfig;
import com.iana.api.dao.NotifEmailDao;
import com.iana.api.domain.PendingNotification;

@Component
public class NotificationSender {

	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	private CustomEmailConfig customEmailConfig;

	@Autowired
	private NotifEmailDao notifEmailDao;

	@Autowired
	private Environment env;
	
	public void send(Exception e) {

		StringBuilder exceptionLogs = getIterateStackTraceLog(e);
		sendExceptionEmail(e.getMessage(), exceptionLogs);
	}

	private StringBuilder getIterateStackTraceLog(Exception e) {

		log.error("An Exception Occured:", e);
		StringBuilder exceptionLogs = new StringBuilder(e.toString());
		exceptionLogs.append("<br/>");
		StackTraceElement[] elements = e.getStackTrace();
		/* int displayCount = elements.length >= 4 ? 4 : elements.length; */
		int displayCount = elements.length;
		for (int iterator = 1; iterator <= displayCount; iterator++) {
			exceptionLogs.append(
				"Class Name:" + elements[iterator - 1].getClassName() + " Method Name:" + elements[iterator - 1].getMethodName() + " Line Number:" + elements[iterator - 1].getLineNumber() + "<br/>");

		}
		// log.error(exceptionLogs);
		return exceptionLogs;
	}

	private void sendExceptionEmail(String responseMsg, StringBuilder exceptionLogs) {

		String emailHtmlBody = prepareExceptionEmailBody(responseMsg, exceptionLogs);
		
		PendingNotification pn = new PendingNotification();
		pn.setAppName(env.getProperty("app_name"));
		pn.setAppChildName(env.getProperty("app_child_name"));
		pn.setNotfnMode(env.getProperty("notfn_mode_email"));
		pn.setNotfnType(env.getProperty("notfn_type_error"));
		pn.setMailTo(customEmailConfig.getEmailProp().getProperty("smtp.to.exception.mail"));
		pn.setMailCc(null);
		pn.setMailBcc(null);
		pn.setMailSubject(customEmailConfig.getEmailProp().getProperty("smtp.exception.title"));
		pn.setMailContent(emailHtmlBody.getBytes());
		pn.setMailContentType(GlobalVariables.EMAIL_TYPE_HTML);
		pn.setMailAttachments(null);
		pn.setMailAttachmentsReq(false);
		pn.setMailImagesForBody(null);
		
		Long responseCounter = notifEmailDao.insertPendingNotification(pn);
		log.info("sendExceptionEmail:", responseCounter);

	}

	private String prepareExceptionEmailBody(String responseMsg, StringBuilder exceptionLogs) {

		StringBuilder sb = new StringBuilder("<div>");
		sb.append(" An error has occurred, Kindly look into the details and try to fix it as soon as possible. ");
		sb.append(" <br>");
		sb.append("<br> ");
		sb.append("<div style=\"border: 1px solid #0000ff; width: 100%;font-family:Verdana, Geneva, sans-serif;\"> ");
		sb.append("<table style=\"border-spacing: 0px; width: 100%\" border=\"0\" rowspan=\"0\"> ");
		sb.append("<tbody>");
		sb.append("<tr style=\"color: #fff; background-color: #006aac;\"> ");
		sb.append("<td style=\"padding:5px;font-size:14px;\">Exception Information</td> ");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td style=\"font-size:13px;padding:3px;\"> ");
		sb.append(responseMsg);
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("<tr style=\"color: #fff; background-color: #006aac;\">");
		sb.append("<td style=\"padding:5px;font-size:14px;\">StackTrace Log Details</td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td style=\"font-size:13px;padding:3px;\"> ");
		sb.append("<div>");
		sb.append(exceptionLogs);
		sb.append("</div>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</tbody>");
		sb.append("</table>");
		sb.append("</div>");
		sb.append("</div>");
		return sb.toString();
	}

	public void sendEmail(String subject, String body, String email) {
		
		PendingNotification pn = new PendingNotification();
		
		pn.setAppName(env.getProperty("app_name"));
		pn.setAppChildName(env.getProperty("app_child_name"));
		pn.setNotfnMode(env.getProperty("notfn_mode_email"));
		pn.setNotfnType(env.getProperty("notfn_type_success"));
		pn.setMailTo(email);
		pn.setMailCc(null);
		pn.setMailBcc(null);
		pn.setMailSubject(subject);
		pn.setMailContent(body.getBytes());
		pn.setMailContentType(GlobalVariables.EMAIL_TYPE_HTML);
		pn.setMailAttachments(null);
		pn.setMailAttachmentsReq(false);
		pn.setMailImagesForBody(null);
		
		// don't remove this log.info
		Long responseCounter = notifEmailDao.insertPendingNotification(pn);
		log.info("sendEmail(String subject, String body, String email):", responseCounter);
		
	}
	
	public void sendEmailWithAttachments(String[] emails,String subject,String body,String[] attachments){
		

		PendingNotification pn = new PendingNotification();
		
		pn.setAppName(env.getProperty("app_name"));
		pn.setAppChildName(env.getProperty("app_child_name"));
		pn.setNotfnMode(env.getProperty("notfn_mode_email"));
		pn.setNotfnType(env.getProperty("notfn_type_success"));
		pn.setMailTo(String.join(GlobalVariables.COMMA, emails));
		pn.setMailCc(null);
		pn.setMailBcc(null);
		pn.setMailSubject(subject);
		pn.setMailContent(body.getBytes());
		pn.setMailContentType(GlobalVariables.EMAIL_TYPE_HTML);
		pn.setMailAttachments(String.join(GlobalVariables.COMMA, attachments).getBytes());
		pn.setMailAttachmentsReq(true);
		pn.setMailImagesForBody(null);
		
		Long responseCounter = notifEmailDao.insertPendingNotification(pn);
		log.info("sendEmail(String subject, String body, String email):", responseCounter);
		 
	}
	
	
	public void sendEmailWithImage(String[] emails,String subject,String body,String[] image){

		PendingNotification pn = new PendingNotification();
		pn.setAppName(env.getProperty("app_name"));
		pn.setAppChildName(env.getProperty("app_child_name"));
		pn.setNotfnMode(env.getProperty("notfn_mode_email"));
		pn.setNotfnType(env.getProperty("notfn_type_success"));
		pn.setMailTo(String.join(GlobalVariables.COMMA, emails));
		pn.setMailCc(null);
		pn.setMailBcc(null);
		pn.setMailContent(body.getBytes());
		pn.setMailContentType(GlobalVariables.EMAIL_TYPE_HTML);
		pn.setMailAttachments(null);
		pn.setMailAttachmentsReq(false);
		pn.setMailImagesForBody(String.join(GlobalVariables.COMMA, image).getBytes());
		pn.setMailSubject(subject);
		
		Long responseCounter = notifEmailDao.insertPendingNotification(pn);
		log.info("sendEmailWithImage:", responseCounter);
		
	}

}
