package edu.mit.cuisinestream;

import com.example.webphotos.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class RestaurantHandler extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant_handler);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.restaurant_handler, menu);
		return true;
	}

}
