package com.example.webphotos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListOfRestaurants implements Serializable
{
	public List<RestaurantData> restaurants;

	public ListOfRestaurants()
	{
		restaurants = new ArrayList<RestaurantData>();
	}

	public ListOfRestaurants(List<RestaurantData> data)
	{
		restaurants = data;
	}

	public RestaurantData[] getRestaurantArray()
	{			
		RestaurantData[] result;
		result = new RestaurantData[restaurants.size()];
		for(int i = 0; i < restaurants.size(); i++)
		{
			result[i] = restaurants.get(i);
		}
		return result;
	}
}