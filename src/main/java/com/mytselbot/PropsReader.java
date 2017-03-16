package com.mytselbot;

import java.io.*;
import java.util.*;

//public class PropsReader implements FileChangeListener
public class PropsReader {

	FileInputStream fStream;
	Properties pFile;
	String fileName;
	long checkPeriod;

	public PropsReader(String fileName) {
		this.fileName = fileName;
		reloadProperties();
	}

	/*
	 * public synchronized void setCheckPeriod(long period) { checkPeriod =
	 * period; try { FileMonitor.getInstance().addFileChangeListener(this,
	 * fileName, checkPeriod); } catch(FileNotFoundException e) { } }
	 */

	private void reloadProperties() {
		try {
			fStream = new FileInputStream(fileName);
			pFile = new Properties();
			pFile.load(fStream);
			fStream.close();
		} catch (IOException e) {
		}

		// setCheckPeriod(Long.parseLong(pFile.getProperty("ConfigCheckPeriod")));
	}

	public void fileChanged(String fileName) {
		reloadProperties();
	}

	public String readProps(String param) {
		return pFile.getProperty(param);
	}
	
	public String readProps(String param, String defaultparam) {
		return pFile.getProperty(param, defaultparam);
	}

	public String readString(String param) {
		return pFile.getProperty(param);
	}

	public int readInt(String param) {
		return Integer.parseInt(pFile.getProperty(param));
	}
	
	public int readInt(String param, String defaultparam) {
		return Integer.parseInt(pFile.getProperty(param,defaultparam));
	}

	public long readLong(String param) {
		return Long.parseLong(pFile.getProperty(param));
	}

	public boolean readBoolean(String param) {
		return Boolean.parseBoolean(pFile.getProperty(param));
	}
}
