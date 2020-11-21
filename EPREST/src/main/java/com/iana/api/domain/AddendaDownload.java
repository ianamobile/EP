/**
 * 
 */
package com.iana.api.domain;

import com.iana.api.utils.GlobalVariables;

import lombok.Data;

/**
 * @author 140975
 *
 */
@Data
public class AddendaDownload 
{
	private String epAcctNo;
	private String epName;
	private String nonUIIAEpFlag = GlobalVariables.NO;
	private String epScac;
	private String addendaName;
	private String addendaPath;
	private String addendaEffDate;
	private int addendaId = 0;
}