package com.example.webphotos;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;

public class GalleryActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		String test = intent.getStringExtra("test");
		Log.d("intent extra", "received from main: "+test);
		//		RestaurantData restaurant = intent.get
		//pass something through the intent coming from the main activity
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_gallery);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int h = metrics.heightPixels;
		int w = metrics.widthPixels;

		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setLayoutParams(new LinearLayout.LayoutParams(w, h-80));
		ImagePagerAdapter adapter = new ImagePagerAdapter();
		viewPager.setAdapter(adapter);

		TextView name = (TextView)findViewById(R.id.top_name);
		//name.setText(restaurant.name);
		RatingBar stars = (RatingBar)findViewById(R.id.ratingBar1);
		//stars.setProgress(restaurant.rating*20); //or maybe not *20?

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
		String[] testpics = {"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcRFknsdZDTESSTWImcDDcx_-NR0WXriUbW7VSxLkatioTwm3tWe",
				"http://images.all-free-download.com/images/graphicmedium/fast_food_04_vector_156273.jpg",
				"http://images.all-free-download.com/images/graphicmedium/fast_food_06_vector_156271.jpg",
				"http://www.camillesdish.com/wp-content/uploads/et_temp/IMG_1721-455568_200x200.jpg",
				"http://www.stopfoodborneillness.org/sites/default/files/images/1food.jpg",
			"http://neworleanslocal.com/wp-content/uploads/2012/05/fresh-produce-200x200.jpg"};

		@Override
		public int getCount() {
			return testpics.length;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == ((ImageView) arg1);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int pos) {
			Context context = GalleryActivity.this;
			ImageView imageView = new ImageView(context);
			int padding = 30;
			imageView.setPadding(padding, padding, padding, padding);
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			loadBitmap(testpics[pos], imageView);
			((ViewPager) container).addView(imageView, 0);
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int pos, Object obj) {
			((ViewPager)container).removeView((ImageView)obj);
		}
	}

}
