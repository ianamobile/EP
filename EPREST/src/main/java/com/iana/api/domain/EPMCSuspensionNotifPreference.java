package com.iana.api.domain;

/**
 * @author ianaoffshore
 * Created At 18-Nov-2019 7:22:20 pm
 * 
 */
public class EPMCSuspensionNotifPreference{
	
	private Long snpId;
	private String notifDesc;
	private boolean selected =false;
	
	public Long getSnpId() {
		return snpId;
	}
	
	public void setSnpId(Long snpId) {
		this.snpId = snpId;
	}
	
	public String getNotifDesc() {
		return notifDesc;
	}
	
	public void setNotifDesc(String notifDesc) {
		this.notifDesc = notifDesc;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public String toString() {
		return "EPMCSuspensionNotifPreference [snpId=" + snpId + ", notifDesc=" + notifDesc + "]";
	}
	
	

}
