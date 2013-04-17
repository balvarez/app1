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

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainWebActivity extends Activity {
	private String currentLocation = "";
	SeekBar seekbar;
	TextView txt;
	ListOfRestaurants listOfRestaurantData;



	private String getURL(int rad) throws NoLocationException {
		String currentLocation = "";
		final String android_id;
		if(0==(getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))//not debug mode
		{
			Geocoder geocoder;
			String bestProvider;
			List<Address> user = null;
			double lat;
			double lng;
			LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			bestProvider = lm.getBestProvider(criteria, false);
			Location location = lm.getLastKnownLocation(bestProvider);
			
			if (location == null){
				Toast.makeText(this,"Location Not found, please try again shortly",Toast.LENGTH_LONG).show();
				throw new NoLocationException("location not found");
			}else{
				geocoder = new Geocoder(this);
			    try {
			        user = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			    lat=(double)user.get(0).getLatitude();
			    lng=(double)user.get(0).getLongitude();
			    currentLocation = lat + "+" + lng;
			    
			    }catch (Exception e) {
			    	e.printStackTrace();
			    }
			}
			TelephonyManager telephonyManager1 = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			android_id = telephonyManager1.getDeviceId();
		}
		else //we are in debug mode, and should return spoofed values
		{
			android_id = "debug";
			currentLocation = "42.340148+-71.089268";
		}

		
		Log.d("getURL", "android ID: " + android_id);
		Log.d("getURL", "location: " + currentLocation);
		String url = "http://18.238.2.68/cuisinestream/phonedata.cgi?user="+android_id+"&location="+currentLocation+"&radius="+rad;
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
				listOfRestaurantData = restData;
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

		nearbyRestaurants.removeAllViews();
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
		final int PREVIEW_HEIGHT = 180; //height of each restaurant preview in scroll

		//need to differentiate frames. create an ArrayList array. lol
		//number of restaurants defined by testData.length. need that many copies of horizontalscrollview
		RelativeLayout[] imgFramesList = new RelativeLayout[restaurants.size()];
		//TODO remove this limiter
		for (int i=0; i<Math.min(restaurants.size(), 5); i++) {
			HorizontalScrollView restaurant = new HorizontalScrollView(this); //create scroll
			restaurant.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, PREVIEW_HEIGHT)); //set height
			restaurantsFrame.addView(restaurant); //add scroll to scroll container
			imgFramesList[i] = new RelativeLayout(this); //make frame for scroll
			imgFramesList[i].setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, PREVIEW_HEIGHT));
			restaurant.addView(imgFramesList[i]); //add frame to scroll
			TextView info = new TextView(this); //make view for name/distance
			
			
			final RestaurantData currentRestaurant = restaurants.get(i);
			info.setText(currentRestaurant.name+"    "+currentRestaurant.distance/1609d+" miles");
			info.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			imgFramesList[i].addView(info);
			if(currentRestaurant.photos.size()==0)
			{
				//TODO add code to handle if there are no photos 
			}
			//TODO: remove this magic number (the 5 below, for max number of photos to add)
			Resources reso = this.getResources();
			int previousPic = 0;
			for (int j=0; j<Math.min(currentRestaurant.photos.size(), 5); j++) {
				ImageView fillin = new ImageView(this);
				RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, PREVIEW_HEIGHT);
				if (previousPic != 0) {
					imgParams.addRule(RelativeLayout.RIGHT_OF, previousPic);
				}
				fillin.setLayoutParams(imgParams);
				fillin.setClickable(true);
				fillin.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent toPage = new Intent(MainWebActivity.this, GalleryActivity.class);
						toPage.putExtra("data", currentRestaurant); //send RestaurantData object to next activity
						toPage.putExtra("tester", "message");
						startActivity(toPage);
						//this section is not broken
					}
				});
				imgFramesList[i].addView(fillin);
				fillin.setImageDrawable(reso.getDrawable(R.drawable.loading));
				new BitmapWorkerTask(fillin).execute(currentRestaurant.photos.get(j));
				fillin.setId(1000+j);
				previousPic = fillin.getId();
				Log.d("id printer", "fillin id: "+previousPic);
			}
			imgFramesList[i].bringChildToFront(info);
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
				try{
					String URL = getURL(slider_position); //start getting the URL. takes distance in meters
					new RestaurantInfoTask().execute(URL);
				}
				catch(NoLocationException e)
				{
					
				}
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
			//TODO pick a placeholder for images that are downloading slowly
			BitmapWorkerTask task = new BitmapWorkerTask(imgV);
			task.execute(url);
		}
	}
	
	public void cameraClicked(View v) {
		Intent addPhoto = new Intent(MainWebActivity.this, AddPicActivity.class);
		startActivity(addPhoto);
	}
	
	private String ff = "feel";
	
	public void toggleFF(View v) {
		Log.d("foodFeel", "hitButton");
		ImageView toggle = (ImageView)findViewById(R.id.toggle_main);
		if (ff.equals("feel"))
		{
			Log.d("foodFeel", "switch to food");
			ff = "food";
			toggle.setImageDrawable(getResources().getDrawable(R.drawable.food));
		}
		else if (ff.equals("food"))
		{
			Log.d("foodFeel", "switch to feel");
			ff = "feel";
			toggle.setImageDrawable(getResources().getDrawable(R.drawable.feel));
		}
		else
			{
				Log.d("foodFeel", "wrong!");
				toggle.setImageDrawable(getResources().getDrawable(R.drawable.feel));
			}
	}
	
	public void goToMapMain(View v) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+42.340148/1E6 +"," + -71.089268/1E6));
		//Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("maps.google.com/maps?q="+currentLocation.replace("+", ",")));
		startActivity(browserIntent);
		String latitude = String.valueOf(listOfRestaurantData.restaurants.get(0).lat);
		String longitude = String.valueOf(listOfRestaurantData.restaurants.get(1).lng);
		String label = listOfRestaurantData.restaurants.get(0).name;
		String uriBegin = "geo:" + latitude + "," + longitude;
		String query = latitude + "," + longitude + "(" + label + ")";
		String encodedQuery = Uri.encode(query);
		String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
		Uri uri = Uri.parse(uriString);
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
		startActivity(intent);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_web, menu);
		return true;
	}
}
