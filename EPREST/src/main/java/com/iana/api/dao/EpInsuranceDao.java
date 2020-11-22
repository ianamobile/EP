package com.iana.api.dao;

import java.util.List;

import com.iana.api.domain.EPInsNeeds;
import com.iana.api.domain.EPTemplate;
import com.iana.api.domain.MultipleLimit;

public interface EpInsuranceDao {
	
	List<EPInsNeeds> getEPInsuranceDetails(EPTemplate epTemplate) throws Exception;
	
	List<MultipleLimit> getEPMultipleLim(EPTemplate epTemplate) throws Exception;

}
