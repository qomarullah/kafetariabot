package com.mytselbot;

import java.util.Calendar;
import java.util.TimeZone;

public class Testing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String userName="qomar";
		String reply = "";
		Calendar c = Calendar.getInstance();
		int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
		if (timeOfDay >= 0 && timeOfDay < 12) {
			reply = "Pagi bro *" + userName + "* !!";
		} else if (timeOfDay >= 12 && timeOfDay < 16) {
			reply = "Siang bro *" + userName + "* !!";
		} else if (timeOfDay >= 16 && timeOfDay < 18) {
			reply = "Sore bro *" + userName + "* !!";
		} else if (timeOfDay >= 18 && timeOfDay < 24) {
			reply = "Malam bro *" + userName + "* !!";
		}
		
	}

}
