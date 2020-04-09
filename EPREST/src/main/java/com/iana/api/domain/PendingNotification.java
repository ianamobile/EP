package com.iana.api.domain;

import lombok.Data;

@Data
public class PendingNotification {

	private Long notifId;
	private String appName;
	private String appChildName;
	private String notfnMode;
	private String notfnType;
	private String mailTo;
	private String mailCc;
	private String mailBcc;
	private String mailSubject;
	private byte[] mailContent;
	private String mailContentType;
	private byte[] mailAttachments;
	private Boolean mailAttachmentsReq;
	private byte[] mailImagesForBody;
	private String createdDate;
	
}
