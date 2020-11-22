package com.iana.api.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.iana.api.domain.EPSwitches;
import com.iana.api.domain.EPTemplate;
import com.iana.api.utils.GlobalVariables;

@Repository
public class EpSwitchesDaoImpl extends GenericDAO implements EpSwitchesDao {

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;

	/*
	 * this method gets all the EP switches
	 * 
	 * @param int templateId
	 * 
	 * @return EPSwitchesBean
	 * 
	 * @throws Exception
	 */
	public EPSwitches getEPSwitches(EPTemplate epTemplate) throws Exception {

		StringBuffer sbQry = new StringBuffer();

		if (GlobalVariables.NO.equalsIgnoreCase(epTemplate.getDbTemplateStatus())) {
			sbQry.append(
					"SELECT  EP_ADDNDM_ID as addendumId,EP_MEM_SPCFC_CARRIER as memberSpecific,EP_KNOWN_AS_RQD as knownAs,BLNKT_ALLWD as blanketAllwd,RAMP_DTLS_RQD as rampDetReq,ATTR1,ATTR2 FROM arch_ep_specific_addendum WHERE");
			sbQry.append(" EP_TEMPLATE_ID = '" + epTemplate.getTemplateID() + "'");
		} else if (GlobalVariables.YES.equalsIgnoreCase(epTemplate.getDbTemplateStatus())
				|| GlobalVariables.PENDINGTEMPLATES.equalsIgnoreCase(epTemplate.getDbTemplateStatus())) {
			sbQry.append(
					"SELECT  EP_ADDNDM_ID as addendumId,EP_MEM_SPCFC_CARRIER as memberSpecific,EP_KNOWN_AS_RQD as knownAs,BLNKT_ALLWD as blanketAllwd,RAMP_DTLS_RQD as rampDetReq,ATTR1,ATTR2 FROM ep_specific_addendum WHERE");
			sbQry.append(" EP_TEMPLATE_ID = '" + epTemplate.getTemplateID() + "'");
		}

		EPSwitches epSwitches = findBean(this.uiiaDataSource, sbQry.toString(), EPSwitches.class);
		return epSwitches;

	}

}
