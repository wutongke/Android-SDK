package com.sensoro.beacon.core;

import java.util.Calendar;
import java.util.Date;

class DateUtils {
	
	public static DateBrief	getDate(long timestamp) {
		
		Date date = new Date(timestamp);
		DateBrief dateBrief = new DateBrief();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		dateBrief = getDate(date);
		
		return dateBrief;
	}
	
	public static DateBrief	getDate(Date date) {
		
		DateBrief dateBrief = new DateBrief();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		dateBrief.year = calendar.get(Calendar.YEAR);
		dateBrief.month = calendar.get(Calendar.MONTH) + 1;
		dateBrief.day = calendar.get(Calendar.DAY_OF_MONTH);
		dateBrief.hour = calendar.get(Calendar.HOUR_OF_DAY);
		dateBrief.minute = calendar.get(Calendar.MINUTE);
		dateBrief.second = calendar.get(Calendar.SECOND);
		dateBrief.milliSecond = calendar.get(Calendar.MILLISECOND);
		
		return dateBrief;
	}
}
