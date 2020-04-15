package com.iana.api.dao;

import java.util.List;

import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.MCDataJsonDTO;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SecurityObject;

public interface EPDao {

	Long countEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount) throws Exception;
	
	List<JoinRecord> getEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount) throws Exception;

	List<MCDataJsonDTO> getMCLookUpForEP(SecurityObject securityObject, SearchAccount searchAccount) throws Exception;

}
