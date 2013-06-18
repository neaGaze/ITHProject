package com.ith.project;

public class GridItemDetails {

	private String menuName, menuIcon;
	private int menuId;

	public GridItemDetails(String menuId, String menuName, String menuIcon) {
		this.menuId = Integer.parseInt(menuId);
		this.menuName = menuName;
		//this.menuIcon = Integer.parseInt(menuIcon);
		this.menuIcon = menuIcon;
	}

	public int getMenuId() {
		return this.menuId;
	}

	public String getMenuName() {
		return this.menuName;
	}
	
	public String getMenuIcon()
	{
		return this.menuIcon;
	}
}
