package models.dto;

public enum VipLevel {

	FREE("免费用户"),
	LEVEL1("银卡用户"),
	LEVEL2("金卡用户");

	private final String displayName;

	VipLevel(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
