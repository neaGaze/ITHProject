package com.ith.project;

public class ItemDetails {

	private int itemId;
	private String itemDescription;
	private String DateTime;
	private String date;
	private String time;
	private boolean isNew;

	public ItemDetails(String itemId, String itemDesc, String dateTime,
			String isNew) {
		this.itemId = Integer.parseInt(itemId);
		this.itemDescription = itemDesc;
		this.DateTime = dateTime;
		this.isNew = Boolean.parseBoolean(isNew);
		this.parseDateTime(DateTime);
	}

	/***************************************************************************************
	 * To separate Date From time
	 * ***************************************************************************************/
	private void parseDateTime(String dateTime2) {

		this.date = new StringBuilder().append(dateTime2.substring(6, 8))
				.append("-").append(dateTime2.substring(4, 6)).append("-")
				.append(dateTime2.substring(0, 4)).toString();

		this.time = new StringBuilder().append(dateTime2.substring(9, 11))
				.append(":").append(dateTime2.substring(11, 13)).append(":")
				.append(dateTime2.substring(13)).toString();

	}

	public String getItemDesc() {
		return this.itemDescription;
	}

	public String getItemDateTime() {
		return this.DateTime;
	}

	public String getDate() {
		return this.date;
	}

	public String getTime() {
		return this.time;
	}

	public boolean getIsNew() {
		return this.isNew;
	}

	public int getId() {
		return this.itemId;
	}
}
