package com.iana.api.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iana.api.dao.EPWhatIfDao;
import com.iana.api.dao.EpDao;
import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.EPAddendum;
import com.iana.api.domain.EPInsNeeds;
import com.iana.api.domain.EPWhatIfForm;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.GlobalVariables;

@Service
public class EPWhatIfServiceImpl extends CommonUtils implements EPWhatIfService {

	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	private EpDao epDao;

	@Autowired
	private EPWhatIfDao epWhatIfDao;

	@Autowired
	private EPService epService;

	@Override
	public EPWhatIfForm getWhatIfTemplate(String epAcctNo) throws Exception {
		log.info("Entering method getWhatIfTemplate(" + epAcctNo + ") of EPWhatIfServiceImpl class");

		AccountInfo acctInfo = new AccountInfo();
		// getting the EP basic account details
		acctInfo = epDao.getBasicAcctDtls(epAcctNo);

		// getting valid MC count
		int iValidMCs = epWhatIfDao.getValidMCCount(epAcctNo);

		log.debug("Account Info Details :" + acctInfo.toString());

		String validMC = String.valueOf(iValidMCs);

		log.debug("Getting Addendum Details for what if screen :");
		EPAddendum epAddendum = epService.getActiveTemplate(epAcctNo, "");
		log.debug("Addendum Details :" + epAddendum.toString());

		List<EPInsNeeds> needsList = epAddendum.getEpNeeds();

		EPInsNeeds addAuto = new EPInsNeeds();
		EPInsNeeds addGeneral = new EPInsNeeds();
		EPInsNeeds addCargo = new EPInsNeeds();
		EPInsNeeds addContCargo = new EPInsNeeds();
		EPInsNeeds addTI = new EPInsNeeds();
		EPInsNeeds addRefTI = new EPInsNeeds();
		EPInsNeeds addWC = new EPInsNeeds();
		EPInsNeeds addEL = new EPInsNeeds();
		EPInsNeeds addEDB = new EPInsNeeds();
		EPInsNeeds addUL = new EPInsNeeds();
		for (int i = 0; i < needsList.size(); i++) {
			EPInsNeeds insBean = needsList.get(i);
			insBean.setPolicyReq(GlobalVariables.YES);
			if (insBean.getPolicyType().equals(GlobalVariables.AUTOPOLICY)) {
				addAuto = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.GENPOLICY)) {
				addGeneral = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.CARGOPOLICY)) {
				addCargo = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.CONTCARGO)) {
				addContCargo = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.TRAILERPOLICY)) {
				addTI = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.REFTRAILER)) {
				addRefTI = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.WORKCOMP)) {
				if (insBean.getPolicyReq().equals(GlobalVariables.YES)) {
					insBean.setMinLimit(GlobalVariables.STATUTORY);
					// insBean.setMaxDed(GlobalVariables.STATUTORY);
				}
				addWC = insBean;

			} else if (insBean.getPolicyType().equals(GlobalVariables.EMPLIABILITY)) {
				addEL = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.EMPDISHBOND)) {
				addEDB = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.UMBRELLA)) {
				addUL = insBean;
			}
		}

		log.debug("Setting Policy Types to be shown on what if screen:");
		addAuto.setPolicyType(GlobalVariables.AUTOPOLICY);
		addGeneral.setPolicyType(GlobalVariables.GENPOLICY);
		addCargo.setPolicyType(GlobalVariables.CARGOPOLICY);
		addTI.setPolicyType(GlobalVariables.TRAILERPOLICY);
		addContCargo.setPolicyType(GlobalVariables.CONTCARGO);
		addRefTI.setPolicyType(GlobalVariables.REFTRAILER);
		addWC.setPolicyType(GlobalVariables.WORKCOMP);
		addEL.setPolicyType(GlobalVariables.EMPLIABILITY);
		addEDB.setPolicyType(GlobalVariables.EMPDISHBOND);
		addUL.setPolicyType(GlobalVariables.UMBRELLA);

		List<EPInsNeeds> resultList = new ArrayList<>();

		resultList.add(addAuto);
		resultList.add(addGeneral);
		resultList.add(addCargo);
		resultList.add(addContCargo);
		resultList.add(addTI);
		resultList.add(addRefTI);
		resultList.add(addWC);
		resultList.add(addEL);
		resultList.add(addEDB);
		resultList.add(addUL);

		epAddendum.setEpNeeds(resultList);

		EPWhatIfForm epWhatIfForm = populateFormBean(epAddendum, validMC, resultList);
		epWhatIfForm.setAcctInfo(acctInfo);
		epWhatIfForm.setEpAddendum(epAddendum);
		return epWhatIfForm;

	}

	private EPWhatIfForm populateFormBean(EPAddendum addendum, String validMC, List<EPInsNeeds> resultList) {
		EPWhatIfForm whatIfForm = new EPWhatIfForm();
		whatIfForm.setBlanketAllwd(addendum.getEpSwitches().getBlanketAllwd());
		// whatIfForm.setEffDate(addendumBean.getEffDate());
		// whatIfForm.setEpNeeds(epNeeds);
		whatIfForm.setEpNeeds(resultList);
		whatIfForm.setValidMC(validMC);
		return whatIfForm;

	}
	
	
}
