package com.ith.project.menu;

public class CustomMenu {
	private String text;
	private String image;

	// public final int actionTag;

	public CustomMenu(String title, String imageStr) {
		text = title;
		image = imageStr;
		// this.actionTag = actionTag;
	}

	public String getText() {
		return text;
	}

	public String getImage() {
		return image;
	}

	public void setValues(String menuString, String menuIcon) {
		this.text = menuString;
		this.image = menuIcon;
	}

}
