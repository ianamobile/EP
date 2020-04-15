package com.iana.api.domain;

import java.util.List;

import lombok.Data;

@Data
public class SearchResult<T> {
	
	private List<T> resultList;
	private Pagination page;
	
	public SearchResult(List<T> resultList, Pagination pagination) {
		super();
		this.resultList = resultList;
		this.page = pagination;
	}
	
	

}
