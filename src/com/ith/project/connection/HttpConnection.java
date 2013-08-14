package com.ith.project.connection;

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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class HttpConnection {
	private String mainUrl = "http://192.168.100.2/EMSWebService/Service1.svc/json/";

	private HttpClient httpclient;
	private HttpPost httppost;
	private static HttpConnection httpConnection;
	private static int ConnCount = 0;
	private static int timeoutConnection = 3000;
	private static int timeoutSocket = 3000;

	public HttpConnection() {
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)in milliseconds which is
		// the timeout for waiting for data.
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
	public String getJSONFromUrl(JSONObject jsonForm, String serviceName) {
		// initialize
		String url = new StringBuilder().append(mainUrl).append(serviceName)
				.toString();
		InputStream is = null;
		String result = "";

		
		try {
			httppost = new HttpPost(url);
			httppost.setHeader(HTTP.CONTENT_TYPE,
					"application/json; charset=utf-8");
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

		} catch (Exception e) {
			Log.e("Error converting result ", "" + e.toString());
		}
		return result;

	}

	/*****************************************************************************************
	 * get Connection Availability
	 * ************************************************************************************/
	public static boolean getConnectionAvailable(Context context) {
		ConnectivityManager conMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isAvailable() && netInfo.isConnected())
			return true;
		else
			return false;
	}

}
