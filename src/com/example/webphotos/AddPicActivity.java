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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;

public class AddPicActivity extends Activity {
	//TODO add uses feature android.hardware.camera to manifest
	//could fairly simply allow for phones without a camera to use the app but not a priority
	
	//SO FAR this should launch the camera and come back and preview the picture taken
	//TODO pick a restaurant
	//TODO upload photo and rating to server
	//TODO make a submit graphic to replace the toMap graphic
	
	String currentPhotoPath;
	int pictureActionCode = 42;
	ImageView preview = (ImageView)findViewById(R.id.newPhoto);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_pic);
		try {
			createImageFile();
		} catch (IOException e) {
			Log.d("IOException", "failed to create image file");
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
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(currentPhotoPath));
		startActivityForResult(takePictureIntent, actionCode);
	}
	
	private File createImageFile() throws IOException {
		// Create a unique image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "cs" + timeStamp + "_";
		File image = File.createTempFile(imageFileName, ".jpg",
				new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CuisineStream"));
		currentPhotoPath = image.getAbsolutePath();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_pic, menu);
		return true;
	}

}
