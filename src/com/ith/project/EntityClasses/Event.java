package com.ith.project.EntityClasses;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Event {

	private int eventId, eventRealId, createdBy, sentTo, eventStatus;
	private String eventName, eventDesc, eventDateTime, eventPlace, eventDate,
			eventTime, eventType, participationStatus;
	private boolean isEventRead;
	private String longitude, latitude;

	public Event() {

	}

	public int getEventId() {
		return eventId;
	}

	public int getEventRealId() {
		return eventRealId;
	}

	public String getEventName() {
		return eventName;
	}

	public String getEventDesc() {
		return eventDesc;
	}

	public String getEventDateTime() {
		return eventDateTime;
	}

	public String getEventPlace() {
		return eventPlace;
	}

	public String getEventType() {
		return eventType;
	}

	public int getEventCreator() {
		return createdBy;
	}

	public int getEventReceiver() {
		return sentTo;
	}

	public String getLongitude() {
		return longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getParticipationStatus() {
		return participationStatus;
	}

	public boolean getEventReadStatus() {
		return isEventRead;
	}

	public String getDate() {
		return eventDate;
	}

	public String getTime() {
		return eventTime;
	}

	public int getEventStatus() {
		return eventStatus;
	}

	public void setValues(JSONObject remoteJson) {

		try {

			eventId = remoteJson.getInt("EventId");

			eventName = remoteJson.getString("EventName");
			eventDesc = remoteJson.getString("EventDescription");
			eventDateTime = remoteJson.getString("DateTimeStr");
			eventPlace = remoteJson.getString("eventLocation");
			longitude = remoteJson.getString("Longitude");
			latitude = remoteJson.getString("latitude");
			isEventRead = remoteJson.getBoolean("isEventRead");
			participationStatus = remoteJson.getString("partStatus");
			createdBy = remoteJson.getInt("createdBy");
			sentTo = remoteJson.getInt("sentTo");
			parseDateTime(eventDateTime);

		} catch (JSONException e) {
			Log.e("JSONException", "" + e.getMessage());
			e.printStackTrace();
		}

	}

	/***************************************************************************************
	 * To separate Date From time
	 * ***************************************************************************************/
	public void parseDateTime(String dateTime2) {

		Log.e("dateTime equals", "here it is: " + dateTime2);

		if ((dateTime2 == null) || dateTime2.equals("")) {
			eventDate = null;
			eventTime = null;

		} else {
			this.eventDate = new StringBuilder()
					.append(dateTime2.substring(6, 8)).append("-")
					.append(dateTime2.substring(4, 6)).append("-")
					.append(dateTime2.substring(0, 4)).toString();

			this.eventTime = new StringBuilder()
					.append(dateTime2.substring(9, 11)).append(":")
					.append(dateTime2.substring(11, 13)).toString();
		}

	}

	public void setEventId(int id) {
		eventId = id;
	}

	public void setEventRealId(int id) {
		eventRealId = id;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public void setEventDesc(String eventDesc) {
		this.eventDesc = eventDesc;
	}

	public void setEventDateTime(String eventDateTime) {
		this.eventDateTime = eventDateTime;
		parseDateTime(eventDateTime);
	}

	public void setEventCreator(int creator) {
		this.createdBy = creator;
	}

	public void setEventTo(int sentTo) {
		this.sentTo = sentTo;
	}

	public void setEventPlace(String eventPlace) {
		this.eventPlace = eventPlace;
	}

	public void setEventRead(int eventRead) {
		if (eventRead == 1)
			this.isEventRead = true;
		else
			this.isEventRead = false;
	}

	public void setEventStatus(int eventStatusId) {
		this.eventStatus = eventStatusId;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public void setEventParticipation(String participate) {
		// if (participate == 1)
		this.participationStatus = participate;
		// else
		// this.participationStatus = false;
	}

	public void setEventLongitude(String longitude) {
		this.longitude = longitude;
	}

	public void setEventLatitude(String latitude) {
		this.latitude = latitude;
	}

	/*****************************************************************************************
	 * To make a new Create Event inquiry Json file
	 ******************************************************************************************/
	public JSONObject makeNewEventJSON(int msgFrom, Integer[] msgTo,
			String msgTitle, String msgDesc, String eventVenueStr,
			String eventDateTimeStr, String longitude2, String latitude2) {

		JSONObject tempJsonFile = new JSONObject();
		JSONArray tempMsgTo = new JSONArray();
		StringBuilder msgToString = new StringBuilder();
		try {
			for (int i = 0; i < msgTo.length; i++) {
				tempMsgTo.put(msgTo[i]);
				if (i == msgTo.length - 1)
					msgToString.append(msgTo[i]);
				else
					msgToString.append(msgTo[i]).append(",");
			}

			tempJsonFile.put("organizerEmployeeId",
					new StringBuilder().append(msgFrom));
			tempJsonFile.put("toEmployeeId", msgToString.toString());
			tempJsonFile.put("title", msgTitle);
			tempJsonFile.put("description", msgDesc);
			tempJsonFile.put("venue", eventVenueStr);
			tempJsonFile.put("eventDate", eventDateTimeStr);
			tempJsonFile.put("longitude", longitude2);
			tempJsonFile.put("latitude", latitude2);
			tempJsonFile.put("userLoginId", LoginAuthentication.UserloginId);

		} catch (JSONException e) {
			Log.e("Could not convert to JSONObject", "" + e.getMessage());
			e.printStackTrace();
		}
		return tempJsonFile;

	}
}
