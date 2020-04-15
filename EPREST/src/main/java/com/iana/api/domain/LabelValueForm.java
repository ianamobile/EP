package com.iana.api.domain;

import lombok.Data;

@Data
public class LabelValueForm {
	
	private String label;
	private String value;
	private boolean selected;
	
	public LabelValueForm(){
		
	}
	
	public LabelValueForm(String label, String value) {
		super();
		this.label = label;
		this.value = value;
	}
	
	
	
	
}
