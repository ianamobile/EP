package com.iana.api.domain;

import lombok.Data;

@Data
public class Pagination {

	private int size;
	private int totalElements;
	private int totalPages;
	private int currentPage;
}
