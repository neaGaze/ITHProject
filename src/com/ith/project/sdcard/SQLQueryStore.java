package com.ith.project.sdcard;

import android.annotation.SuppressLint;

@SuppressLint("SdCardPath")
public class SQLQueryStore extends LocalConnection {

	private String urlLocal = "/sdcard/EMS";
	private String sqlFolder = "/SQL";
	private String sqlText = "/sql.txt";

	public SQLQueryStore() {
	}

	/*****************************************************************************************
	 * write files to sdcard
	 * ************************************************************************************/
	public void writeFile2Sdcard(String sqlQuery) {
		this.createOrUseFile(sqlQuery, urlLocal + sqlFolder, sqlText);
	}

}
