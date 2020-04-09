package com.iana.api.dao;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.iana.api.domain.PendingNotification;
import com.iana.api.utils.DateTimeFormater;
import com.iana.api.utils.GlobalVariables;

@Repository
public class NotifEmailDaoImpl extends GenericDAO implements NotifEmailDao {
	
	
	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	@Qualifier("notifEmailDataSource")
	private DataSource notifEmailDataSource;
	
	@Override
	public Long insertPendingNotification(PendingNotification pn) {
		
		try {
			
			Map<String,Object> paramMap = new HashMap<String, Object>();
			paramMap.put("APP_NAME", pn.getAppName());
			paramMap.put("APP_CHILD_NAME", pn.getAppChildName());
			paramMap.put("NOTFN_MODE", pn.getNotfnMode());
			paramMap.put("NOTFN_TYPE", pn.getNotfnType());
			paramMap.put("MAIL_TO", pn.getMailTo());
			paramMap.put("MAIL_CC", pn.getMailCc());
			paramMap.put("MAIL_BCC", pn.getMailBcc());
			paramMap.put("MAIL_SUBJECT", pn.getMailSubject());
			paramMap.put("MAIL_CONTENT", pn.getMailContent());
			paramMap.put("MAIL_CONTENT_TYPE", pn.getMailContentType());
			paramMap.put("MAIL_ATTACHMENTS", pn.getMailAttachments());
			paramMap.put("MAIL_ATTACHMENTS_REQ", pn.getMailAttachmentsReq());
			paramMap.put("MAIL_IMAGES_FOR_BODY", pn.getMailImagesForBody());
			paramMap.put("CREATED_DATE", DateTimeFormater.getCurrentDateStr(DateTimeFormater.FORMAT9));
		
			Long notifId = insertAndReturnGeneratedKey(this.notifEmailDataSource, "pending_notification", paramMap, "NOTIF_ID").longValue();
			
			if(pn.getMailAttachmentsReq() == true && null != pn.getMailAttachments()) {
				
				String attachments = new String(pn.getMailAttachments());
				String[] attachmentsArray = attachments.split(GlobalVariables.COMMA);
				for(String attachment : attachmentsArray) {
					File file = new File(attachment);
					if(file.isFile() && file.exists()) {
						paramMap = new HashMap<String, Object>();
						paramMap.put("NOTIF_ID", notifId);
						paramMap.put("NOTFN_FILE", Files.readAllBytes(file.toPath()));
						paramMap.put("CREATED_DATE", DateTimeFormater.getCurrentDateStr(DateTimeFormater.FORMAT9));
					
						insertAndReturnGeneratedKey(this.notifEmailDataSource, "notification_files", paramMap, "NOTFN_FILE_ID").longValue();
					}
				}
			}
			
			return notifId;
			
		} catch(Exception e) {
			log.error("Exception:", e);
			return 0l;
		}
	}
 	
}
