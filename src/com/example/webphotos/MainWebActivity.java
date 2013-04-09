package com.example.webphotos;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainWebActivity extends Activity {
	private String currentLocation = "";
	SeekBar seekbar;
	TextView txt;



	private String getURL(int rad) {

		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location provider.
				currentLocation = String.valueOf(location.getLatitude()) + "+" + String.valueOf(location.getLongitude());
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};

		Log.d("getURL", "set up locationManager and locationListener");
		// Register the listener with the Location Manager to receive location updates
		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

		Log.d("getURL", "locationManager.requestLocationUpdates");
		//final String android_id = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
		final String android_id = "brian";
		Log.d("getURL", "android ID: " + android_id);
		//final String user = "aaa"; //idk how to deal with making users unique right now
		//String url = "http://18.238.2.68/cuisinestream/phonedata.cgi?user="+android_id+"&location="+currentLocation+"&radius="+rad;
		String url = "http://18.238.2.68/cuisinestream/phonedata.cgi?user="+android_id+"&location="+"42.340148+-71.089268"+"&radius="+rad;
		Log.d("getURL", "result URL: " + url);
		return url;
	}



	private class RestaurantInfoTask extends AsyncTask<String, Void, String> {

		public RestaurantInfoTask() {}

		@Override
		protected String doInBackground(String... arg0) {
			String rawData="";
			try {
				URL url = new URL(arg0[0]);
				//Log.d("reader", "1");
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				//Log.d("reader", "2");
				String inputLine;
				while((inputLine=in.readLine())!=null) {rawData=inputLine;}
				//Log.d("reader", "3");
				in.close();
				//Log.d("reader", "rawData = "+rawData);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return rawData;
		}

		@Override
		protected void onPostExecute(String raw) {
			try {
				JSONTokener tokener = new JSONTokener(raw);
				JSONObject obj1 = (JSONObject) tokener.nextValue();
				JSONArray keys = obj1.names();
				ListOfRestaurants restData = new ListOfRestaurants();
				for(int i = 0; i < keys.length(); i++)
				{
					String id = keys.get(i).toString();
					JSONObject restaurant = (JSONObject)obj1.get(id);
					double lat = restaurant.getDouble("lat");
					double lng = restaurant.getDouble("lng");
					double dist = restaurant.getDouble("distance");
					String name = restaurant.getString("name");
					double rating = Double.NaN;
					if(restaurant.has("rating"))
					{
						rating = restaurant.getDouble("rating");
					}
					JSONArray photosArray = restaurant.getJSONArray("photos");
					List<String> photos = new ArrayList<String>();
					for(int j = 0; j < photosArray.length(); j++)
					{
						photos.add(photosArray.getString(j));
					}
					restData.restaurants.add(new RestaurantData(id, name, lat, lng, rating, dist, photos));
				}
				Log.d("parseJSON", String.valueOf(restData.restaurants.size()));
				for(RestaurantData data : restData.restaurants)
				{
					Log.v("printJSON", data.toString());
				}
				updateDisplay(restData);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}
	}
	
	//be SUPER CAREFUL with calling this. Everything should exist, but this is a hacky way to do it
	private void updateDisplay(ListOfRestaurants restData)
	{
		List<RestaurantData> restaurants = restData.restaurants;

		//vertical scroll in layout containing a vertical linearlayout containing the restaurant scrolls
		ScrollView nearbyRestaurants = (ScrollView)findViewById(R.id.nearbyRestaurants);
		LinearLayout restaurantsFrame = new LinearLayout(this);
		restaurantsFrame.setOrientation(1);
		View parent = (View) restaurantsFrame.getParent();
		Log.d("find error", "resFrame parent: "+parent);
		nearbyRestaurants.addView(restaurantsFrame);

		//made each horizontalscrollview clickable instead
//		//adding a button to test swipe view
//		Button pressme = new Button(this);
//		layout.addView(pressme);
//		pressme.setOnClickListener(new Button.OnClickListener() {
//			public void onClick(View v) {
//				Intent toPage = new Intent(MainWebActivity.this, GalleryActivity.class);
//				startActivity(toPage);
//			}
//		});

		//set up horizontal restaurant scrolls
		final int PREVIEW_HEIGHT = 200; //height of each restaurant preview in scroll
		LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, PREVIEW_HEIGHT);

		//need to differentiate frames. create an ArrayList array. lol
		//number of restaurants defined by testData.length. need that many copies of horizontalscrollview
		LinearLayout[] imgFramesList = new LinearLayout[restaurants.size()];
		for (int i=0; i<restaurants.size(); i++) {
			HorizontalScrollView restaurant = new HorizontalScrollView(this); //create scroll
			restaurant.setLayoutParams(hp); //set height
			restaurant.setClickable(true);
			restaurant.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent toPage = new Intent(MainWebActivity.this, GalleryActivity.class);
//					toPage.putExtra(restaurantData); //send RestaurantData object to next activity
					toPage.putExtra("tester", "message");
					startActivity(toPage);
				}
			});
			restaurantsFrame.addView(restaurant); //add scroll to scroll container
			imgFramesList[i] = new LinearLayout(this); //make frame for scroll
			restaurant.addView(imgFramesList[i]); //add frame to scroll
			
			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200, 200);
			RestaurantData currentRestaurant = restaurants.get(i);
			//TODO: remove this magic number (the 5 below, for max number of photos to add)
			for (int j=0; j<Math.min(currentRestaurant.photos.size(), 5); j++) {
				ImageView fillin = new ImageView(this);
				fillin.setLayoutParams(lp);
				imgFramesList[i].addView(fillin);
				new BitmapWorkerTask(fillin).execute(currentRestaurant.photos.get(j));
			}
		}

		//setup for the cache
		final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount() / 1024;
			}
		};
	}

	private LruCache<String, Bitmap> mMemoryCache; //instantiate cache

	//test data
	/*
	String[][] testData = {{"http://images4.fanpop.com/image/photos/18400000/pickle-delivery-pickles-18401263-373-500.jpg",
		"http://images4.fanpop.com/image/photos/18400000/pickle-delivery-pickles-18401263-373-500.jpg",
		"http://images4.fanpop.com/image/photos/18400000/pickle-delivery-pickles-18401263-373-500.jpg",
		"http://images4.fanpop.com/image/photos/18400000/pickle-delivery-pickles-18401263-373-500.jpg",
		"http://images4.fanpop.com/image/photos/18400000/pickle-delivery-pickles-18401263-373-500.jpg",
	"http://images4.fanpop.com/image/photos/18400000/pickle-delivery-pickles-18401263-373-500.jpg"},
	{"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcRFknsdZDTESSTWImcDDcx_-NR0WXriUbW7VSxLkatioTwm3tWe",
		"http://images.all-free-download.com/images/graphicmedium/fast_food_04_vector_156273.jpg",
		"http://images.all-free-download.com/images/graphicmedium/fast_food_06_vector_156271.jpg",
		"http://www.camillesdish.com/wp-content/uploads/et_temp/IMG_1721-455568_200x200.jpg",
		"http://www.stopfoodborneillness.org/sites/default/files/images/1food.jpg",
	"http://neworleanslocal.com/wp-content/uploads/2012/05/fresh-produce-200x200.jpg"},
	{"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcRFknsdZDTESSTWImcDDcx_-NR0WXriUbW7VSxLkatioTwm3tWe",
		"http://images.all-free-download.com/images/graphicmedium/fast_food_04_vector_156273.jpg",
		"http://images.all-free-download.com/images/graphicmedium/fast_food_06_vector_156271.jpg",
		"http://www.camillesdish.com/wp-content/uploads/et_temp/IMG_1721-455568_200x200.jpg",
		"http://www.stopfoodborneillness.org/sites/default/files/images/1food.jpg",
	"http://neworleanslocal.com/wp-content/uploads/2012/05/fresh-produce-200x200.jpg"}};
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main_web);

		//		jsonTestRead();
		//activity_main_web has a vertical linearlayout base view
		LinearLayout layout = (LinearLayout)findViewById(R.id.layout);

		//set up banner
		Resources res = getResources();
		ImageView banner = (ImageView)findViewById(R.id.bannerSpace);
		banner.setImageDrawable(res.getDrawable(R.drawable.csbanner));

		//listen to the distance slider
		seekbar = (SeekBar)findViewById(R.id.distanceSlide);
		txt = (TextView)findViewById(R.id.radius);
		txt.setText("Set search radius: .1 miles");
		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			int slider_position;
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				String URL = getURL(slider_position); //start getting the URL. takes distance in meters
				new RestaurantInfoTask().execute(URL);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int prog, boolean fromUser) {
				double dist = ((double)prog / 25d) - 1.98; //converts 0 to 100 into about -2 to 2
				dist = Math.exp(dist);
				DecimalFormat formatter = new DecimalFormat("##.#");
				//TODO if we want to, we can convert values < 1 into feet. not priority
				txt.setText("Set search radius: " + formatter.format(dist) + " miles");
				slider_position = (int)(dist*1609d);
			}
		});
		seekbar.incrementProgressBy(1);

	}

	//cache methods
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	//this is the AsyncTask class that handles background download of images
	private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewRef;

		public BitmapWorkerTask(ImageView imgV) {
			// Use a WeakReference to ensure the IV can be garbage collected
			imageViewRef = new WeakReference<ImageView>(imgV);
		}

		// Decode image in background
		@Override
		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			if (mIcon11 != null) addBitmapToMemoryCache(urls[0], mIcon11);
			return mIcon11;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewRef != null && bitmap != null) {
				final ImageView imageView = imageViewRef.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	public void loadBitmap(String url, ImageView imgV) {
		final String imgKey = url;
		final Bitmap bitmap = getBitmapFromMemCache(imgKey);
		if (bitmap != null) imgV.setImageBitmap(bitmap);
		else {
			//			imgV.setImageResource(R.drawable.image_placeholder);
			//TODO pick a placeholder for images that are downloading slowly
			BitmapWorkerTask task = new BitmapWorkerTask(imgV);
			task.execute(url);
		}
	}
	
	private void cameraClicked(View v) {
		Intent addPhoto = new Intent(MainWebActivity.this, AddPicActivity.class);
		startActivity(addPhoto);
	}
	
	private void toggleFF(View v) {
		ImageView toggle = (ImageView)findViewById(R.id.toggle_main);
		if (toggle.getDrawable()==getResources().getDrawable(R.drawable.feel)) {
			toggle.setImageDrawable(getResources().getDrawable(R.drawable.food));
		} else toggle.setImageDrawable(getResources().getDrawable(R.drawable.feel));
	}
	
	private void goToMapMain(View v) {
		String geoString = ""; //placeholder. fill in with restaurant data sent through intent from Main
		//geoString should be like "geo:<lat>,<long>?q=<lat>,<long>(Label+Name)&z=<zoom>"  (& might be ?, not sure)
		//first lat,long is the center. use user location
		//second lat, long is the pin location. (L+N) is the label on the pin. zoom is zoom, something like 17 is probably good? (max 23)
		//add ?q=... for more pins
		Uri geoUri = Uri.parse(geoString);
		Intent toMapMain = new Intent(Intent.ACTION_VIEW, geoUri);
		startActivity(toMapMain);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_web, menu);
		return true;
	}
}
