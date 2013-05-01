package edu.mit.cuisinestream;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class Map extends Activity {
	private ListOfRestaurants listOfRestaurantData;
	private GoogleMap mMap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		setUpMapIfNeeded();
		Intent intent = getIntent();
		Log.d("map", "getting additional data");
		try{
			listOfRestaurantData = (ListOfRestaurants) intent.getSerializableExtra("restaurantList");
		}
		catch(Exception e)
		{
			Log.d("map", "unable to get additional data");
			e.printStackTrace();
		}
		addMarkersToMap();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
	
	private void addMarkersToMap()
	{
		mMap.clear();
		for(RestaurantData restaurant : listOfRestaurantData.restaurants)
		{
			mMap.addMarker(new MarkerOptions().position(new LatLng(restaurant.lat, restaurant.lng)).title(restaurant.name));
		}
	}
	
	private void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (mMap == null) {
	        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
	                            .getMap();
	        // Check if we were successful in obtaining the map.
	        if (mMap != null) {
	            // The Map is verified. It is now safe to manipulate the map.
	        }
	        else
	        {
	        	Log.d("Map", "unable to instantiate map");
	        }
	    }
	}
}
