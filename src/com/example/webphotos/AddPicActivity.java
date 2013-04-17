package com.example.webphotos;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;

public class AddPicActivity extends Activity {
	//could fairly simply allow for phones without a camera to use the app but not a priority
	
	//SO FAR this should launch the camera and come back and preview the picture taken
	//TODO pick a restaurant
	//TODO upload photo and rating to server
	//TODO make a submit graphic to replace the toMap graphic
	
	static RestaurantData restaurant;
	static ListOfRestaurants restList;
	String currentPhotoPath;
	int pictureActionCode = 42;
	ImageView preview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		if (intent.hasExtra("restaurant")) {restaurant = (RestaurantData) intent.getSerializableExtra("restaurant");}
		else {
			restList = (ListOfRestaurants) intent.getSerializableExtra("restaurantList");
			DialogFragment picker = new RestaurantSelectDialog();
			picker.show(getFragmentManager(), "restPicker");
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_pic);
		preview = (ImageView)findViewById(R.id.newPhoto);
		try {
			createImageFile();
		} catch (IOException e) {
			Log.d("IOException", "failed to create image file");
			e.printStackTrace();
		}
		dispatchTakePictureIntent(pictureActionCode);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//Check receiving correct intent
		if (requestCode == pictureActionCode) {
			//Check for successful request
			if (resultCode == RESULT_OK) {
				galleryAddPic();
				Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
				preview.setImageBitmap(bitmap);
			}
		}
	}
	
	private void dispatchTakePictureIntent(int actionCode) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File picFile = new File(currentPhotoPath);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picFile));
		startActivityForResult(takePictureIntent, actionCode);
	}
	
	private File createImageFile() throws IOException {
		// Create a unique image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "cs" + timeStamp + "_";
		Log.d("imagefile", "made name");
		File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CuisineStream");
		Log.d("imagefile", "created album");
		File image = File.createTempFile(imageFileName, ".jpg",
				directory);
		Log.d("imagefile", "created temp file");
		currentPhotoPath = image.getAbsolutePath();
		Log.d("imagefile", "absPath: "+currentPhotoPath);
		return image;
	}
	
	private void galleryAddPic() {
		//I think this just makes the picture available in the gallery behind the scenes
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(currentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}
	
	public static class RestaurantSelectDialog extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Select Restaurant");
			final RestaurantData[] fullList = restList.getRestaurantArray();
			String[] names = new String[fullList.length];
			for (int i=0; i<names.length; i++) {names[i] = fullList[i].name;}
			builder.setItems(names, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					restaurant = fullList[which];
				}
			});
			return builder.create();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_pic, menu);
		return true;
	}

}
