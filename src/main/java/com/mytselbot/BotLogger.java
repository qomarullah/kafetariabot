package com.mytselbot;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.TelegramApiException;

public class BotLogger {
	//static Logger logger = LogManager.getRootLogger();
	public static Logger logger = LogManager.getLogger(BotLogger.class);

	   
	public BotLogger(){
		
    	logger.trace("Configuration File Defined To Be :: "+System.getProperty("log4j.configurationFile"));
	}
	
	public static void info(String lOGTAG, String msg){
		logger.info(lOGTAG, msg);
	}
	public static void debug(String lOGTAG, String msg){
		logger.debug(lOGTAG, msg);
	}
	public static void trace(String lOGTAG, String TEST){
		logger.trace(lOGTAG, TEST);
	}

	public static void error(String lOGTAG, TelegramApiException e) {
		// TODO Auto-generated method stub
		logger.error(lOGTAG, e);
	}
	public static void error(String lOGTAG, SQLException e) {
		// TODO Auto-generated method stub
		logger.error(lOGTAG, e);
	}
	public static void error(String lOGTAG, Exception e) {
		// TODO Auto-generated method stub
		logger.error(lOGTAG, e);
	}
}
