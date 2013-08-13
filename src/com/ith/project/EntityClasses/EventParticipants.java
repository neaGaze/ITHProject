package com.ith.project.EntityClasses;

public class EventParticipants {

	private int goingId, eventRealId, toEmployeeId;
	private String goingStatus;

	public void setGoingId(int goingId) {
		this.goingId = goingId;
	}

	public void setEventRealId(int eventRealId) {
		this.eventRealId = eventRealId;
	}

	public void setToEmployeeId(int employeeId) {
		this.toEmployeeId = employeeId;
	}

	public void setGoingStatus(String goingStatus) {
		this.goingStatus = goingStatus;
	}
	
	public int getGoingId(){
		return goingId;
	}
	
	public int getEventRealId(){
		return eventRealId;
	}
	
	public int getToEmployeeId(){
		return toEmployeeId;
	}
	
	public String getGoingStatus(){
		return goingStatus;
	}

}
