package com.sensoro.beacon.core;

class TimeOutException extends Exception {
	private String error = null;

	TimeOutException(String error){
		this.error = error;
	}
	
	public String toString(){
		return error;
	}
	
	public String getMessage(){
		return error;
	}
}
