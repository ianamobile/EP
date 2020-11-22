package com.iana.api.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AddressDet {

	private int driverId = 0;
	private String accountNo = "";
	private String addrStreet1 = "";
	private String addrStreet2 = "";
	private String addrCity = "";
	private String addrState = "";
	private String addrZip = "";
	private String addrCountry = "US";
	private String addrType = "";
	private String sameBillAddr = "";
	private String sameDisputeAddr = "";
	private int addrId = 0;

}
