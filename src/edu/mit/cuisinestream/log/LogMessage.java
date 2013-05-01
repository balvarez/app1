package edu.mit.cuisinestream.log;

import android.net.Uri;

public class LogMessage {
	public enum typeOfLog
	{
		OPEN_MAP,
		OPEN_GALLERY,
		SET_DISTANCE,
		SUBMIT_PHOTO
	}
	public final String URLsuffix;
	
	//go to gallery for restaurant
	//open map from main or gallery
	public LogMessage(typeOfLog type, String data)
	{
		URLsuffix = "?type=" + type.toString() + "&source=" + data;
	}
	
	//for slider updates
	public LogMessage(typeOfLog type, double dist)
	{
		if (type == typeOfLog.SET_DISTANCE)
		{
			URLsuffix = "?type=SET_DISTANCE&source=" + Uri.encode(String.valueOf(dist));
		}
		else {
			throw new IllegalArgumentException("this enum does not corrispond to this constructor");
		}
	}
	
	
	public String toString()
	{
		return URLsuffix;
	}
}
