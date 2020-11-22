package com.iana.api.dao;

import java.util.List;

import com.iana.api.domain.EPTemplate;

public interface EpAddendumDao {

	List<EPTemplate> getTemplateList(EPTemplate epTemplate, String accountNo) throws Exception;

}
