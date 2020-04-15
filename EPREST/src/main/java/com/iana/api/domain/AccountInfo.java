package com.iana.api.domain;

import com.iana.api.utils.GlobalVariables;

import lombok.Data;


@Data
public class AccountInfo implements Comparable<AccountInfo> {

	private String accountNumber="";
	private String companyName="";
	private String entitiesName="";
	private String scac="";
	private String uiiaStatus="";
	private String uiiaMember="";
	private String iddMember="";
	private String oldScac="";
	private String ianaMember = GlobalVariables.NO;
	private String nonUIIAEpFlag = GlobalVariables.NO;
	private String uiiaStatusCode="";
	private String memberType="";
	private String companyUrl="";
	private String attr1="";
	private String attr2="";
	private String attr3="";
	private String memEffDate="";
	private String canDate="";
	private String delDate="";
	private String reInsDate="";
	private String lstUpdtDate="";
	private String password="";
	private String oldUiiaStatus="";
	private String secUserName="";
	private String oldSecUserName="";
	private String uiiaMem="";
	private String iddMem="";
	private String iddStatus="";
	private String applyUiiaMem="";
	private String loginAllwd="";
	private String company_Name="";
	private String current_UIIA_Status="";
	private String dt="";
	// attritbute added for identifiying in change password if the password is
	// changed for sec idd user
	private String iddSec = "N";
	// attributes added for IA used to display in IA search by Huda
	private String iaFaxRcvd = GlobalVariables.NO;
	private String contctName="";
	private String iaPassword="";

	// Added by Sumukh on 31st March 2008
	private String city="";
	private String state="";

	// Added by Sumukh on 1st April 2008
	private String addr_street1="";
	private String addr_street2="";
	private String zip="";
	private String country="";

	// prarit added for mc name change pending indicator
	private String status="";
	private String verifiedBy="";
	private String verifiedDate="";
	private String newName="";
	private String iaFax="";
	private String iaEmail="";
	private String iddMemberType="";
	
	private String firstName="";
	private String lastName="";
	private String phone="";
	private String fax="";
	private String email="";
	
	private String epEntities="";
	
	@Override
	public int compareTo(AccountInfo ai) {
		return this.companyName.compareTo(ai.getCompanyName());
	}

}
