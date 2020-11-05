package com.iana.api.dao;

import com.iana.api.domain.EPSwitches;
import com.iana.api.domain.EPTemplate;

public interface EpSwitchesDao {
	
	EPSwitches getEPSwitches(EPTemplate epTemplate)throws Exception;

}
