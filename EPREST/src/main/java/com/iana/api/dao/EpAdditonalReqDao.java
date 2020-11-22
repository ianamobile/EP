package com.iana.api.dao;

import java.util.List;

import com.iana.api.domain.AdditionalReq;
import com.iana.api.domain.EPTemplate;

public interface EpAdditonalReqDao {
	List<AdditionalReq> getEPAddlReq(EPTemplate epTemplate, String uvalidFlg) throws Exception;

}
