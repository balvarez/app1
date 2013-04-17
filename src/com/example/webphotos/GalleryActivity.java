package com.example.webphotos;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GalleryActivity extends Activity {
	RestaurantData restaurant;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		String test = intent.getStringExtra("tester");
		Log.d("intent extra", "received from main: "+test);
		restaurant = (RestaurantData) intent.getSerializableExtra("data");
		//pass something through the intent coming from the main activity
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_gallery);
		DisplayMetrics metrics = new DisplayMetrics();
		//works to here
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int h = metrics.heightPixels;
		Log.d("view", "height: "+h);
		int w = metrics.widthPixels;

		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setLayoutParams(new RelativeLayout.LayoutParams(w, h-70));
		viewPager.setPageMargin(1);
		ImagePagerAdapter adapter = new ImagePagerAdapter(restaurant, h);
		viewPager.setAdapter(adapter);
		//works to here

		TextView name = (TextView)findViewById(R.id.top_name);
		name.setText(restaurant.name);
		name.setTextAppearance(this, android.R.style.TextAppearance_Large);
		RatingBar stars = (RatingBar)findViewById(R.id.galRestRating);
		stars.setProgress((int)restaurant.rating);

		//setup for the cache
		final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount() / 1024;
			}
		};
		//works to here
	}

	private LruCache<String, Bitmap> mMemoryCache; //instantiate cache

	//cache methods
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	//probably could package bitmapworkertask and cache and just import them. - not a priority

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
			//TO-DO pick a placeholder for images that are downloading slowly
			BitmapWorkerTask task = new BitmapWorkerTask(imgV);
			task.execute(url);
		}
	}

	private class ImagePagerAdapter extends PagerAdapter {
		//		private String[] imgs = restaurant.photos;
		//		String[] testpics = {"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcRFknsdZDTESSTWImcDDcx_-NR0WXriUbW7VSxLkatioTwm3tWe",
		//				"http://images.all-free-download.com/images/graphicmedium/fast_food_04_vector_156273.jpg",
		//				"http://images.all-free-download.com/images/graphicmedium/fast_food_06_vector_156271.jpg",
		//				"http://www.camillesdish.com/wp-content/uploads/et_temp/IMG_1721-455568_200x200.jpg",
		//				"http://www.stopfoodborneillness.org/sites/default/files/images/1food.jpg",
		//		"http://neworleanslocal.com/wp-content/uploads/2012/05/fresh-produce-200x200.jpg"};
		private List<String> pics;

		public ImagePagerAdapter(RestaurantData restaurant, int h) {
			pics=restaurant.photos;
			for (int i=0; i<pics.size(); i++) {
				pics.set(i, pics.get(i).replace("height150", "height"+Integer.toString(h)));
			} Log.d("view", pics.get(0));
		}
		@Override
		public int getCount() {
			return pics.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == ((ImageView) arg1);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int pos) {
			Context context = GalleryActivity.this;
			ImageView imageView = new ImageView(context);
			int padding = 0;
			imageView.setPadding(padding, padding, padding, padding);
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			loadBitmap(pics.get(pos), imageView);
			((ViewPager) container).addView(imageView, 0);
			return imageView;
		}

//		private String ff = "feel";
//
//		public void toggleFF(View v) {
//			Log.d("foodFeel", "hitButton");
//			ImageView toggle = (ImageView)findViewById(R.id.toggle_main);
//			if (ff.equals("feel"))
//			{
//				Log.d("foodFeel", "switch to food");
//				ff = "food";
//				toggle.setImageDrawable(getResources().getDrawable(R.drawable.food));
//			}
//			else if (ff.equals("food"))
//			{
//				Log.d("foodFeel", "switch to feel");
//				ff = "feel";
//				toggle.setImageDrawable(getResources().getDrawable(R.drawable.feel));
//			}
//			else
//			{
//				Log.d("foodFeel", "wrong!");
//				toggle.setImageDrawable(getResources().getDrawable(R.drawable.feel));
//			}
//		}

		@Override
		public void destroyItem(ViewGroup container, int pos, Object obj) {
			((ViewPager)container).removeView((ImageView)obj);
		}
	}

	public void cameraClick(View v) {
		Log.d("cameraclick", "add pic clicked");
		Intent addPic = new Intent(GalleryActivity.this, AddPicActivity.class);
		addPic.putExtra("restaurant", restaurant);
		v.getContext().startActivity(addPic);
	}

	public void goToMapOnePin(View v) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("maps.google.com/maps?q="+restaurant.lat+","+restaurant.lng));
		Log.d("debuf", "intent created");
		v.getContext().startActivity(browserIntent);
	}

}
