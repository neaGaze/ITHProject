package com.ith.project.sdcard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class LocalConnection {

	public LocalConnection() {
		// createOrUseFile();
	}

	/************************************************************************************
	 * Create a cache file for local storage / reuse otherwise
	 * *************************************************************************************/
	protected void createOrUseFile(String loginInfo, String url) {

		FileWriter fileWriter = null;
		try {
			//String content = loginInfo.toString();
			String content = loginInfo;
			File newTextFile = new File(url);
			fileWriter = new FileWriter(newTextFile);
			fileWriter.write(content);
			fileWriter.close();
		} catch (FileNotFoundException e) {
			Log.e("FILENOTFOUND Exception", "" + e.getMessage());
			e.printStackTrace();
		} catch (IOException ex) {
			Log.e("Failed to write", "" + ex.getMessage());
		} finally {
			try {
				fileWriter.close();
			} catch (IOException ex) {
				Log.e("IOException", "" + ex.getMessage());
			}
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
