package com.iana.api.dao;

import com.iana.api.domain.PendingNotification;

public interface NotifEmailDao {

	Long insertPendingNotification(PendingNotification pn);
	
}
