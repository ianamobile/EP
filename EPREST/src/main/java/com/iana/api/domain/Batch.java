package com.iana.api.domain;

import lombok.Data;

@Data
public class Batch 
{
	private String batchCode="";
	private String batchDate="";
	private String batchCount="0";
	private String batchAmnt="0";
	private int pymtBatchId=0;
	private String batchDesc="";
		
}
