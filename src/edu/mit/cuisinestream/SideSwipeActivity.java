package edu.mit.cuisinestream;

import java.lang.ref.WeakReference;

import edu.mit.cuisinestream.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.*;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class SideSwipeActivity extends FragmentActivity {
	public static final String EXTRA_IMAGE = "extra_image";
	private ImagePagerAdapter mAdapter;
	private ViewPager mPager;
	private LruCache<String, Bitmap> mMemoryCache;
	
	// A static dataset to back the ViewPager adapter
	// Replace with resIds from bitmaps downloaded
	public final static Integer[] imageResIds = new Integer[] {
		R.drawable.burger, R.drawable.candy, R.drawable.chicken,
		R.drawable.dessert};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_side_swipe); // contains viewpager
		
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), imageResIds.length);
		mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount() / 1024;
			}
		};
	}
	
	public static class ImagePagerAdapter extends FragmentStatePagerAdapter {
		private final int mSize;
		
		public ImagePagerAdapter(FragmentManager fm, int size) {
			super(fm);
			mSize = size;
		}
		
		@Override
		public int getCount() {return mSize;}
		
		@Override
		public Fragment getItem(int position) {
			return ImageDetailFragment.newInstance(position);
		}
	}
	
	public static class ImageDetailFragment extends Fragment {
		private static final String IMAGE_DATA_EXTRA = "resId";
		private int mImageNum;
		private ImageView mImageView;
		
		static ImageDetailFragment newInstance(int imageNum) {
			final ImageDetailFragment f = new ImageDetailFragment();
			final Bundle args = new Bundle();
			args.putInt(IMAGE_DATA_EXTRA, imageNum);
			f.setArguments(args);
			return f;
		}
		
		// Empty constructor, required as per Fragment docs
		public ImageDetailFragment() {}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mImageNum = getArguments() != null ? getArguments().getInt(IMAGE_DATA_EXTRA) : -1;
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			//image_detail_fragment.xml contains just an ImageView
			final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
			mImageView = (ImageView)v.findViewById(R.id.potato);
			return v;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			if (SideSwipeActivity.class.isInstance(getActivity())) {
			final int resId = SideSwipeActivity.imageResIds[mImageNum];
			//Call out to IDA to load bitmap in background thread
			((SideSwipeActivity) getActivity()).loadBitmap(resId, mImageView);
		}
		}
	}
	
	public void loadBitmap(int resId, ImageView imageView) {
		final String imageKey = String.valueOf(resId);
		final Bitmap bitmap = mMemoryCache.get(imageKey);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		} else {
//			mImageView.setImageResource(R.drawable.image_placeholder);
			BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			task.execute(resId);
		}
	}
	
	private class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewRef;
		private int data = 0;
		
		public BitmapWorkerTask(ImageView imgV) {
			// Use a WeakReference to ensure the IV can be garbage collected
			imageViewRef = new WeakReference<ImageView>(imgV);
		}
		
		// Decode image in background
		@Override
		protected Bitmap doInBackground(Integer... params) {
			data = params[0];
			final Bitmap bitmap = decodeSampledBitmapFromResource(getResources(), data);
			addBitmapToMemoryCache(String.valueOf(data), bitmap);
			return bitmap;
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
	
	private static Bitmap decodeSampledBitmapFromResource(Resources res, int resId) {
		return BitmapFactory.decodeResource(res, resId);
	}
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}
	
	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.side_swipe, menu);
		return true;
	}

}
