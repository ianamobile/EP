package com.iana.api.domain;

import lombok.Data;

@Data
public class CommonFieldsSearch {
	
	private String sortField; //sortBy
	private String sortOrder; // sortDirection
	private int recordFrom;
	private int pageIndex; // offset
	private int pageSize; // limit
	
}
