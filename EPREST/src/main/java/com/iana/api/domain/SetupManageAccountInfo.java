package com.iana.api.domain;

import java.util.List;

import lombok.Data;

@Data
public class SetupManageAccountInfo {

	private List<LabelValueForm> equipProviderType;
	private List<LabelValueForm> uiiaStatus;
	private List<LabelValueForm> serviceLevels;
	private List<LabelValueForm> namePrefixList;
	
}
