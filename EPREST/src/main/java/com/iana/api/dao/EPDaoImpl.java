package com.iana.api.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.iana.api.domain.EPJoinDet;
import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.MCDataJsonDTO;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SecurityObject;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.DateTimeFormater;
import com.iana.api.utils.GlobalVariables;

@Repository
public class EPDaoImpl extends GenericDAO implements EPDao {
	
	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;
	
	@Autowired
	public Environment env;

	@Override
	public Long countEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount) throws Exception {
		
		List<Object> params = new ArrayList<>();
		StringBuffer sbQuery = new StringBuffer();
		
		sbQuery.append(" SELECT COUNT(*) ");
		sbQuery.append(" FROM account_info a,");
		
		if(StringUtils.isNotBlank(searchAccount.getKnownAs())) {
			sbQuery.append(" (ep_mc_join_details d, mc_ep_join_status j ");
		
		}else {
			sbQuery.append(" (mc_ep_join_status j  LEFT JOIN  ep_mc_join_details d  ON(d.mc_acct_no = j.mc_acct_no AND d.ep_acct_no = ?) ");
			params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		}
		sbQuery.append(" LEFT JOIN (mc_specific_overrides op) ON(op.mc_acct_no = j.mc_acct_no AND op.ep_acct_no = ?) ");
		sbQuery.append(" LEFT JOIN (mc_areq_overrides oa) ON(oa.mc_acct_no = j.mc_acct_no AND oa.ep_acct_no = ? )) ");
		sbQuery.append(" WHERE j.ep_acct_no = ? ");
		
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
			
		filterEPMotorCarriers(searchAccount, params, sbQuery);
				
		return findTotalRecordCount(this.uiiaDataSource, params.toArray(), sbQuery.toString());
	}

	@Override
	public List<JoinRecord> getEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount) throws Exception {
	
		List<Object> params = new ArrayList<>();
		StringBuffer sbQuery = new StringBuffer();
		
		sbQuery.append(" SELECT a.account_no,d.ep_mem_det_id,j.mc_acct_no, j.ep_acct_no, j.mc_ep_status, ");
		sbQuery.append(" j.override_used,ep_cancel,ep_cncl_eff_dt,rsn_cancel,ep_private,ep_house, ");
		sbQuery.append(" d.ep_known_as, d.ep_mem, a.scac_code,a.company_name,MAX(IF(op.epmc_spc_ovr_id IS NOT NULL,'Y',IF(oa.mc_areq_id IS NOT NULL,'Y','N'))) AS ovrused ");
		sbQuery.append(" FROM account_info a, ");
		
		if(StringUtils.isNotBlank(searchAccount.getKnownAs())){
			sbQuery.append(" (ep_mc_join_details d, mc_ep_join_status j ");
		
		} else {
			sbQuery.append(" (mc_ep_join_status j  LEFT JOIN  ep_mc_join_details d  ON(d.mc_acct_no = j.mc_acct_no AND d.ep_acct_no = ?) ");
			params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		}

		sbQuery.append(" LEFT JOIN (mc_specific_overrides op) ON(op.mc_acct_no = j.mc_acct_no AND op.ep_acct_no = ?) ");
		sbQuery.append(" LEFT JOIN (mc_areq_overrides oa) ON(oa.mc_acct_no = j.mc_acct_no AND oa.ep_acct_no = ? )) ");
		sbQuery.append(" WHERE j.ep_acct_no = ? ");
			
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
			
		
		filterEPMotorCarriers(searchAccount, params, sbQuery);
			
		sbQuery.append(" GROUP BY mc_acct_no ORDER BY company_name ");
				
		sbQuery.append(" LIMIT ?, ?");
		
		params.add(searchAccount.getRecordFrom());
		params.add(searchAccount.getPageSize());
		
//		return findAll(this.uiiaDataSource, sbQuery.toString(), params.toArray(), JoinRecord.class);
		
		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbQuery.toString(), params.toArray(), new ResultSetExtractor<List<JoinRecord>>(){
			@Override
			public List<JoinRecord> extractData(ResultSet rsOvrJoin) throws SQLException, DataAccessException {
				List<JoinRecord> arlJoinRecord = new ArrayList<>();
				
				while(rsOvrJoin.next())
				{ 
					JoinRecord joinBean=new JoinRecord();
	 				EPJoinDet memDetails=new EPJoinDet();
	 				//Setting MC Basic details in MC override Bean
	 				log.debug("Account Noo:" + rsOvrJoin.getString("account_no"));	  
	 				if(rsOvrJoin.getString("account_no")!=null)
	 					  joinBean.setMcAcctNo(rsOvrJoin.getString("account_no"));
	 				if(rsOvrJoin.getString("company_name")!=null)
	 					joinBean.setMcName(rsOvrJoin.getString("company_name"));
	 				if(rsOvrJoin.getString("scac_code")!=null)
	 					joinBean.setMcScac(rsOvrJoin.getString("scac_code"));
	 				if(rsOvrJoin.getString("mc_ep_status")!=null)
	 				{
	 					if(GlobalVariables.YES.equals(rsOvrJoin.getString("mc_ep_status")))
	 					{
	 						joinBean.setMcEPStatus(GlobalVariables.APPRVD_STATUS);
	 					}
	 					else
	 					{
	 						joinBean.setMcEPStatus(GlobalVariables.NOT_APPRVD_STATUS);
	 					}
	 				}
	 				else
	 				{
	 					joinBean.setMcEPStatus(GlobalVariables.NOT_APPRVD_STATUS);
	 				}
	 					
	 				if(rsOvrJoin.getString("ovrused")!=null)
	 				{
	 					joinBean.setOvrUsed(rsOvrJoin.getString("ovrused"));
	 				}
	 				else
	 				{
	 					joinBean.setOvrUsed(GlobalVariables.NO);
	 				}
	 				
	 				//Setting Member details in Member Bean
	 				memDetails.setEpMemDtlId(rsOvrJoin.getInt("ep_mem_det_id"));
	 				if(rsOvrJoin.getString("ep_cancel")!=null)
						memDetails.setCancelValue(rsOvrJoin.getString("ep_cancel"));
	 				
	 				if(rsOvrJoin.getDate("ep_cncl_eff_dt")!=null)
				 		memDetails.setCanEffDate(DateTimeFormater.formatSqlDate(rsOvrJoin.getDate("ep_cncl_eff_dt"),DateTimeFormater.FORMAT4));
	 				
	 				if(rsOvrJoin.getString("rsn_cancel")!=null)
					 	memDetails.setRsnCancel(rsOvrJoin.getString("rsn_cancel"));
	 				
	 				if(rsOvrJoin.getString("ep_known_as")!=null)
					 	memDetails.setKnownAs(rsOvrJoin.getString("ep_known_as"));
	 				
	 				if(rsOvrJoin.getString("ep_mem")!=null)
	 					memDetails.setEpMember(rsOvrJoin.getString("ep_mem"));
	 				
	 				if(rsOvrJoin.getString("ep_private")!=null)
	 					memDetails.setEpPrivate(rsOvrJoin.getString("ep_private"));
					
	 				if(rsOvrJoin.getString("ep_house")!=null)
	 					memDetails.setEpHouse(rsOvrJoin.getString("ep_house"));
	 				
	 				//Setting the member bean in MCOverride Bean
	 					joinBean.setJoinBean(memDetails);
	 					joinBean.setEpAcctNo(securityObject.getAccountNumber());//added by piyush to display you need you have on EP MC join status
	 					arlJoinRecord.add(joinBean);
	 				log.debug("Exiting while(rsOvrJoin.next())");
					
				}//while end 
				
				return arlJoinRecord;
			}
		});
		
	}
	
	private void filterEPMotorCarriers(SearchAccount searchAccount, List<Object> params, StringBuffer sbQuery) {
		
		StringBuffer tempCompName=new StringBuffer(CommonUtils.validateObject(searchAccount.getCompanyName()));
		tempCompName.append(GlobalVariables.PERCENTAGE);
		//log.debug("tempCompName:" + tempCompName);
		StringBuffer tempSCAC=new StringBuffer(CommonUtils.validateObject(searchAccount.getScac()));
		tempSCAC.append(GlobalVariables.PERCENTAGE);
		//log.debug("tempSCAC:" + tempSCAC);
		StringBuffer tempKnownAs=new StringBuffer(CommonUtils.validateObject(searchAccount.getKnownAs()));
		tempKnownAs.append(GlobalVariables.PERCENTAGE);

		
		sbQuery.append(" AND a.company_name LIKE ? ");
		params.add(tempCompName.toString());
	
		sbQuery.append(" AND j.mc_acct_no = a.account_no ");
		
		if("EP200035".equalsIgnoreCase(searchAccount.getAccountNumber())) {
			sbQuery.append(" AND a.uiia_status <>'PENDING' ");
			
		} else if (GlobalVariables.TRAC_ACCOUNT_NO.equalsIgnoreCase(searchAccount.getAccountNumber())){
			sbQuery.append("  AND a.mem_type IN ('MC','NON_UIIA_MC') ");
		
		} else {
			sbQuery.append(" AND (a.uiia_status <>'DELETED' AND a.uiia_status <>'PENDING') ");
		}
		
		sbQuery.append(" AND ((a.scac_code LIKE ? ) ");
		params.add(tempSCAC);
		
		if(StringUtils.isNotBlank(searchAccount.getScac())) {
			sbQuery.append(" OR (a.scac_code IN ("+preparedMultipleSQLParamInput(searchAccount.getScac().split(GlobalVariables.COMMA).length)+"))) ");
			preparedMultipleSQLValueInput(params, searchAccount.getScac().split(GlobalVariables.COMMA));
		} else {
			sbQuery.append(" ) ");
		}
		
		if(StringUtils.isNotEmpty(searchAccount.getKnownAs())){
			
			sbQuery.append(" AND d.mc_acct_no = j.mc_acct_no ");
	
			sbQuery.append(" AND ( (d.ep_known_as LIKE ? ) ");
			params.add(tempKnownAs);

			sbQuery.append(" OR ( d.ep_known_as IN ("+preparedMultipleSQLParamInput(searchAccount.getKnownAs().split(GlobalVariables.COMMA).length)+")) ) ");
			preparedMultipleSQLValueInput(params, searchAccount.getKnownAs().split(GlobalVariables.COMMA));
	
			sbQuery.append(" AND d.ep_acct_no = ? ");
			params.add(searchAccount.getAccountNumber());
		}
	}

	@Override
	public List<MCDataJsonDTO> getMCLookUpForEP(SecurityObject securityObject, SearchAccount searchAccount) throws Exception {
		List<Object> params = new ArrayList<>();
		StringBuffer sbQuery = new StringBuffer();
		
		sbQuery.append(" SELECT a.company_name,a.scac_code,j.mc_acct_no, j.mc_ep_status, d.ep_mem  ");
		sbQuery.append(" FROM account_info a,");
		sbQuery.append(" (mc_ep_join_status j  ");
		sbQuery.append(" LEFT JOIN  ep_mc_join_details d  ON (d.mc_acct_no = j.mc_acct_no AND d.ep_acct_no = ?) ");
		sbQuery.append(" LEFT JOIN (mc_specific_overrides op) ON(op.mc_acct_no = j.mc_acct_no AND op.ep_acct_no = ?) ");
		sbQuery.append(" LEFT JOIN (mc_areq_overrides oa) ON(oa.mc_acct_no = j.mc_acct_no AND oa.ep_acct_no = ? )) ");
		sbQuery.append(" WHERE j.ep_acct_no = ? ");
		
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
			
		filterGetMCLookUpForEP(searchAccount, params, sbQuery);
			
		sbQuery.append(" GROUP BY j.mc_acct_no ORDER BY a.company_name ");
				
//		return findAll(this.uiiaDataSource, sbQuery.toString(), params.toArray(), MCDataJsonDTO.class);
		
		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbQuery.toString(), params.toArray(), new ResultSetExtractor<List<MCDataJsonDTO>>(){
			@Override
			public List<MCDataJsonDTO> extractData(ResultSet rsCompList) throws SQLException, DataAccessException {
				List<MCDataJsonDTO> mcDataDtos = new ArrayList<>();
				
				while(rsCompList.next())
				{ 
					MCDataJsonDTO mcDataJsonDTO = new MCDataJsonDTO();
			    	  mcDataJsonDTO.setAccountNumber(rsCompList.getString("mc_acct_no") == null ? StringUtils.EMPTY : rsCompList.getString("mc_acct_no"));
			    	  mcDataJsonDTO.setCompanyName(rsCompList.getString("company_name") == null ? StringUtils.EMPTY : rsCompList.getString("company_name"));
			    	  mcDataJsonDTO.setMcScac(rsCompList.getString("scac_code") == null ? StringUtils.EMPTY : rsCompList.getString("scac_code"));
			    	  String mcEPStatus;
			    	  if(rsCompList.getString("mc_ep_status") == null || rsCompList.getString("mc_ep_status").trim().equalsIgnoreCase("") 
			    			  || rsCompList.getString("mc_ep_status").trim().equalsIgnoreCase("null") 
			    			  || rsCompList.getString("mc_ep_status").trim().equalsIgnoreCase("N") ){
			    		  mcEPStatus ="Not approved";
			    	 }else{
			    		 mcEPStatus ="Approved";
			    	 }
			    	  mcDataJsonDTO.setMcEPStatus(mcEPStatus);
			    	  String epMem="";
			    	  if(rsCompList.getString("ep_mem") == null || rsCompList.getString("ep_mem").trim().equalsIgnoreCase("") 
			    			  || rsCompList.getString("ep_mem").trim().equalsIgnoreCase("null") || rsCompList.getString("ep_mem").trim().equalsIgnoreCase("N")){
			    		  epMem ="Non member";
			    	 }else{
			    		 epMem ="Member";
			    	 }
			    	  mcDataJsonDTO.setEpMemberFlag(epMem);
			    	  
			          mcDataDtos.add(mcDataJsonDTO);
					
				}//while end 
				
				return mcDataDtos;
			}
		});
	}
	
	private void filterGetMCLookUpForEP(SearchAccount searchAccount, List<Object> params, StringBuffer sbQuery) {
		
		sbQuery.append(" AND a.company_name LIKE ? ");
		params.add(CommonUtils.validateObject(searchAccount.getCompanyName()) + GlobalVariables.PERCENTAGE);
	
		sbQuery.append(" AND j.mc_acct_no = a.account_no ");
		sbQuery.append(" AND (a.uiia_status <>'DELETED' AND a.uiia_status <>'PENDING') ");
			
		sbQuery.append(" AND a.scac_code LIKE ? ");
		params.add(CommonUtils.validateObject(searchAccount.getScac()) + GlobalVariables.PERCENTAGE);
	}
	
	
}