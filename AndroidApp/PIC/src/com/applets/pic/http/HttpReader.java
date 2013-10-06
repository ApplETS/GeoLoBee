package com.applets.pic.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

 class HttpReader extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... args) {
		String response = "";
		URL url;
		try {
			url = new URL(args[0]);
			URLConnection conn = url.openConnection();
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
	        while ((line = rd.readLine()) != null) {
	            response += line;
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
        return response;
	}

}
