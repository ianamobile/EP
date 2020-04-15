/*
 *  File		: MCOverrideBean.java
 *  Author		: Ashok Soni
 *  Created		: June 09,2006
 *  Description	: This bean will handle MC Overrides which will be the master
 *  			  bean  and will be inherited by JoinRecordBean and OverrideBean
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */
package  com.iana.api.domain;


import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MCOverride {
	
	private String mcName="";
	private String mcScac="";
	private String mcEPStatus="";
	private String mcAcctNo="";
	private String mcEmail="";
	private String ovrUsed="";
	private String epAcctNo="";
	private String company_Name="";
	private List<Object> addReqList = new ArrayList<>();

}	
