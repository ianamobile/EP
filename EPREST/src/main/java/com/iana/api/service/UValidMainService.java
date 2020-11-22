package com.iana.api.service;

import java.util.List;

import com.iana.api.domain.EPInsOvrWrapper;

public interface UValidMainService {

	/**
	 * Wrapper class for getting Number of MCs that are Valid for any changes made
	 * by EP in WhatIf
	 * 
	 * @param strEPAcctNo Account Number of the EP
	 * @param epDtls      EP Details (having Switches, Additional Requirments,
	 *                    Policy Multiple Limits) <br>
	 *                    But from screen only switches is passed. other parameters
	 *                    are not set
	 * @param arlEPNeeds  ArrayList of EPInsNeeds (all the policy selected as Yes
	 *                    will be passed in this Arraylist <br>
	 *                    against which comparison will be made in UValidPolicyCheck
	 *                    class
	 * @return
	 * @throws Exception Custom Exception
	 */
	int getValidMCCountForWhatIf(String strEPAcctNo, EPInsOvrWrapper epDtls, List arlEPNeeds) throws Exception;

	
}
