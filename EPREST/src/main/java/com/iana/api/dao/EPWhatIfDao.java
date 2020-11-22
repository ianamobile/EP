package com.iana.api.dao;

public interface EPWhatIfDao {

	/**
	 * This method gets the total number of valid MCs for a given EP
	 * 
	 * @param String     acctNo
	 * @return integer count
	 * @throws Exception
	 */
	int getValidMCCount(String acctNo) throws Exception;
	

}
