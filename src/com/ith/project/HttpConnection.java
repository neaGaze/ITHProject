package com.ith.project;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.util.Log;

public class HttpConnection /*
							 * extends AsyncTask<JSONObject, Boolean,
							 * JSONObject>
							 */{
	// "http://kathmandu/WcfTestApp/TestWcf.svc/json/Login";
	//private String url  = "http://kathmandu/EMSWebService/Service1.svc/json";

	private HttpClient httpclient;
	private HttpPost httppost;
	private static HttpConnection httpConnection;

	public HttpConnection() {
		//this.url = url;
		httpclient = new DefaultHttpClient();
		
	}

	public static HttpConnection getSingletonConn() {
		if (httpConnection == null)
			httpConnection = new HttpConnection();
		return httpConnection;
	}

	/*****************************************************************************************
	 * read json data from the given URL
	 * ************************************************************************************/
	public String getJSONFromUrl(JSONObject jsonForm, String url) {
		// initialize
		InputStream is = null;
		String result = "";
		JSONArray jArray = null;

		// http post
		try {
			httppost = new HttpPost(url);
			httppost.setHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8");
			//httppost.setHeader("Accept", "application/json");
			StringEntity se = new StringEntity(jsonForm.toString());
			httppost.setEntity(se);
			
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
			Log.v("Successful Connection", ":D");

		} catch (Exception e) {
			Log.e("Error in http connection:", "" + e.toString());
		}
		try {
			// convert response to string
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
			// publishProgress(true);

		} catch (Exception e) {

			Log.e("Error converting result ", "" + e.toString());
		}
		return result;
		/*
		 * try { // try parse the string to a JSON object try { // jArray = new
		 * JSONObject(result); jArray = new JSONArray(result); } catch
		 * (JSONException e) { Log.e("log_tag", "Error parsing data " +
		 * e.toString()); }
		 * 
		 * return jArray; // return result;
		 */
	}
	/*
	 * @Override protected void onPreExecute() { super.onPreExecute(); //
	 * displayProgressBar("Downloading..."); }
	 * 
	 * @Override protected void onProgressUpdate(Boolean... values) {
	 * super.onProgressUpdate(values);
	 * 
	 * }
	 * 
	 * @Override protected void onPostExecute(JSONObject jsonObject) {
	 * super.onPostExecute(jsonObject); // dismissProgressBar(); }
	 * 
	 * @Override protected JSONObject doInBackground(JSONObject... params) {
	 * JSONObject tempJson = getJSONFromUrl(params[0]); Log.v("JsonObj Value",
	 * "" + tempJson.toString()); return tempJson; }
	 */
}
