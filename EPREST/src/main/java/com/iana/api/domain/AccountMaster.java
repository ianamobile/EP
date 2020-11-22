package com.iana.api.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AccountMaster {
	private AccountInfo acctInfo = new AccountInfo();
	private ContactDet cntctInfo = new ContactDet();
	private AddressDet cntctAdd = new AddressDet();
	private ContactDet billInfo = new ContactDet();
	private AddressDet billAdd = new AddressDet();
	private ContactDet disputeInfo = new ContactDet();
	private AddressDet disputeAdd = new AddressDet();
	private String notes = "";
	private String epRmrks = "";
	private List prevNotes = new ArrayList();

}
