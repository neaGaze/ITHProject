package com.ith.project.sdcard;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.util.Log;

public class LocalConnection {

	public LocalConnection() {

	}

	/************************************************************************************
	 * Create a cache file for local storage / reuse otherwise
	 * *************************************************************************************/
	@SuppressLint("SdCardPath")
	protected void createOrUseFile(String loginInfo, String url, String fileName) {

		try {

			String content = loginInfo;
			File newTextFilePath = new File(url);
			if (!newTextFilePath.exists())
				newTextFilePath.mkdirs();

			File newTextFile = new File(url + fileName);
			if (!newTextFile.exists()) {
				newTextFile.createNewFile();
			}

			String pastString = getStringFromLocal("/sdcard/EMS/SQL/sql.txt");

			FileWriter fileWriter = new FileWriter(newTextFile);
			/*
			 * fileWriter.write(content); fileWriter.close();
			 */
			BufferedWriter bufferWritter = new BufferedWriter(fileWriter);
			bufferWritter.write(content + "; \n" + pastString);
			bufferWritter.close();

			/*
			 * else{ FileWriter fileWritter = new
			 * FileWriter(newTextFile.getName(),true); BufferedWriter
			 * bufferWritter = new BufferedWriter(fileWritter);
			 * bufferWritter.write(content); bufferWritter.close(); }
			 */
			// fileWriter.close();
		} catch (FileNotFoundException e) {
			Log.e("FILENOTFOUND Exception", "" + e.getMessage());
			e.printStackTrace();
		} catch (IOException ex) {
			Log.e("Failed to write", "" + ex.getMessage());
		} finally {

		}
	}

	/*****************************************************************************************
	 * convert the file in localUrl to string and returns it
	 * ************************************************************************************/
	public String getStringFromLocal(String localUrl) {

		File localFile = new File(localUrl);
		StringBuffer fileData = new StringBuffer();
		try {
			FileReader localFileReader = new FileReader(localFile);
			BufferedReader reader = new BufferedReader(localFileReader);
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
			}
			reader.close();
			return fileData.toString();

		} catch (FileNotFoundException e) {
			Log.e("FileNotFoundException", "" + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("ioEXception", "" + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

}
