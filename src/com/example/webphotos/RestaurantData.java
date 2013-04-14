package com.example.webphotos;

import java.io.Serializable;
import java.util.List;

public class RestaurantData implements Serializable
{
	public String id;
	public String name;
	public double rating;
	public double lat;
	public double lng;
	public double distance;
	public List<String> photos;

	/**
	 * 
	 * @param ID
	 * @param Name
	 * @param Lat
	 * @param Lng
	 * @param Rating
	 * @param Photos
	 */
	public RestaurantData(String ID, String Name, double Lat, double Lng, double Rating, double Distance, List<String> Photos)
	{
		id = ID;
		name = Name;
		lat = Lat;
		lng = Lng;
		rating = Rating;
		distance = Distance;
		photos = Photos;
	}

	@Override
	public String toString()
	{
		return "id: " + id + ", name: " + name + ", #photos: " + String.valueOf(photos.size());
	}
}