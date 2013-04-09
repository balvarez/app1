package com.example.webphotos;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

public class GalleryActivity extends Activity {

	@Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.activity_gallery);

	    ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
	    ImagePagerAdapter adapter = new ImagePagerAdapter();
	    viewPager.setAdapter(adapter);
	  }
	
	private class ImagePagerAdapter extends PagerAdapter {
		private int[] imgs = new int[] {
				R.drawable.addpic,
				R.drawable.burger,
				R.drawable.chicken
		};

		@Override
		public int getCount() {
			return imgs.length;
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
			imageView.setImageDrawable(context.getResources().getDrawable(imgs[pos]));
			((ViewPager) container).addView(imageView, 0);
			return imageView;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int pos, Object obj) {
			((ViewPager)container).removeView((ImageView)obj);
		}
	}

}
