package com.ith.project;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import android.util.Log;

public class HttpConnection /*
							 * extends AsyncTask<JSONObject, Boolean,
							 * JSONObject>
							 */{
	// "http://kathmandu/WcfTestApp/TestWcf.svc/json/Login";
	// private String url = "http://kathmandu/EMSWebService/Service1.svc/json";

	private HttpClient httpclient;
	private HttpPost httppost;
	private static HttpConnection httpConnection;
	private static int ConnCount = 0;

	public HttpConnection() {
		// this.url = url;
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters,timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)in milliseconds which is
		// the timeout for waiting for data.
		int timeoutSocket = 6000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		httpclient = new DefaultHttpClient(httpParameters);
	}

	public static HttpConnection getSingletonConn() {

		if (httpConnection == null)
			httpConnection = new HttpConnection();
		Log.v("Connection count:", "" + ConnCount++);
		return httpConnection;
	}

	/*****************************************************************************************
	 * read json data from the given URL
	 * ************************************************************************************/
	public String getJSONFromUrl(JSONObject jsonForm, String url) {
		// initialize
		InputStream is = null;
		String result = "";
		// JSONArray jArray = null;

		// http post
		try {
			httppost = new HttpPost(url);
			httppost.setHeader(HTTP.CONTENT_TYPE,
					"application/json; charset=utf-8");
			// httppost.setHeader("Accept", "application/json");
			String tempNetworkJson = jsonForm.toString();
			StringEntity se = new StringEntity(tempNetworkJson);
			httppost.setEntity(se);

			HttpResponse response = httpclient.execute(httppost);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.d("Web Service available", "OK to Go");
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
				Log.v("Successful Connection", ":D");
			} else {
				Log.e("Web service unavailable", "Go Local");
			}

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
