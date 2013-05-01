package edu.mit.cuisinestream.log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

public class LogDataTask extends AsyncTask<LogMessage, Void, Void> {

	@Override
	protected Void doInBackground(LogMessage... params) {
		
		try {
			URL url = new URL(params[0].toString());
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			while(in.readLine()!=null)
			{
				//dont do anything, just make sure we dont close too early
			}
			in.close();
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
}
