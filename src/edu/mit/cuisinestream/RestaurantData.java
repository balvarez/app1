package edu.mit.cuisinestream;

import java.io.Serializable;
import java.util.List;

public class RestaurantData implements Serializable, Comparable<RestaurantData>
{
	private static final long serialVersionUID = -6205813922364353421L;
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

	@Override
	public int compareTo(RestaurantData o) {
		return (int)(100*(distance - o.distance));
	}
}
