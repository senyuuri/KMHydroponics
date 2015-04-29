package com.dhs.hydroponics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class QnTemplateCamera extends Activity{
	private static final int FLAG_IMAGE_CAPTURE1 = 0;
	private static final int FLAG_IMAGE_CAPTURE2 = 1;
	private static final int FLAG_IMAGE_CAPTURE_CROP1 = 2;
	private static final int FLAG_IMAGE_CAPTURE_CROP2 = 3;
	private static final int FLAG_IMAGE_GALLERY1= 4;
	private static final int FLAG_IMAGE_GALLERY2 = 5;
	private QnService qnService;
	private Button btn_prev,btn_next,cam_img1_btn,cam_img2_btn;
	private TextView cam_progress,cam_question,station_name,cam_img1_text,cam_img2_text;
	private ImageView cam_img1,cam_img2;
	private Question question;
	private Integer current_position,qn_size;
	private Bitmap new_bit1,new_bit2;
	private String abpath1,abpath2,img_name1,img_name2;
	private ArrayList<String> default_titles;

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("current_position", current_position);
		savedInstanceState.putInt("qn_size", qn_size);
		savedInstanceState.putInt("qid", question.getQID());
		savedInstanceState.putInt("station", question.getStation());

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		current_position = savedInstanceState.getInt("current_position");
		qn_size = savedInstanceState.getInt("qn_size");
		question = new Question(savedInstanceState.getInt("station"),savedInstanceState.getInt("qid") );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qn_camera);
		btn_prev = (Button) findViewById(R.id.cam_btn_prev);
		btn_next = (Button) findViewById(R.id.cam_btn_next);
		cam_img1_btn = (Button) findViewById(R.id.cam_img1_btn);
		cam_img2_btn = (Button) findViewById(R.id.cam_img2_btn);
		cam_progress = (TextView) findViewById(R.id.cam_progress);
		cam_question = (TextView) findViewById(R.id.cam_question);
		station_name = (TextView) findViewById(R.id.cam_station_name);
		cam_img1_text = (TextView) findViewById(R.id.cam_img1_text);
		cam_img2_text = (TextView) findViewById(R.id.cam_img2_text);
		cam_img1 = (ImageView) findViewById(R.id.cam_img1);
		cam_img2 = (ImageView) findViewById(R.id.cam_img2);
		//Load information
		Bundle bundle = getIntent().getExtras();
		question = (Question) bundle.getSerializable("question");
		current_position = bundle.getInt("current_postion");
		qn_size = bundle.getInt("size");
		station_name.setText("Station "+String.valueOf(question.getStation()));
		cam_progress.setText(String.valueOf(current_position+1)+"/"+String.valueOf(qn_size));
		//Set photo taking question
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		cam_question.setText(dbHandler.getQuestion(question.getQID()));

		//Load photos if the question has been answered
		String ans = dbHandler.getStudentPhoto(question.getQID());

		//Set default title
		default_titles = new ArrayList<String>();
		default_titles.add("Instruction");
		default_titles.add("Planting and Harvesting");
		default_titles.add("Problem-based Learning");
		default_titles.add("Our Progress");
		station_name.setText(default_titles.get(question.getStation()-1));
		//Load custome title if exists
		String custome_title = dbHandler.getQnTitle(question.getQID());
		if(custome_title != " " && custome_title != "NA"){
			station_name.setText(custome_title);
		}

		if(ans!=null){
			Log.d("CAM","Read from db, ans:"+ ans);
			String[] paths = ans.split(",");
			//If second photo present, set second photo
			if(paths.length == 2){
				img_name2 = paths[1];
				if(!(img_name2.equals("null"))){
					File file = new File(img_name2);
					abpath2 = img_name2;
					Log.d("CAM","Read photo2:"+abpath2);
					if(file.exists()){
						Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
						new_bit2 = Bitmap.createScaledBitmap(bitmap, 480, 360, false);
						bitmap.recycle();
						cam_img2.setImageBitmap(new_bit2);
					}
				}
			}
			img_name1 = paths[0];

			if(!(img_name1.equals("null"))){
				File file = new File(img_name1);
				abpath1 = img_name1;
				Log.d("CAM","Read photo1:"+abpath1);
				if(file.exists()){
					Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
					new_bit1 = Bitmap.createScaledBitmap(bitmap, 480, 360, false);
					bitmap.recycle();
					cam_img1.setImageBitmap(new_bit1);
				}
			}

		}
	}
	
	public void preTakePhoto1(View view){
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		CharSequence items[] = new CharSequence[] {"Choose from gallery", "Take photo"};
		adb.setSingleChoiceItems(items, -1, new OnClickListener() {
		        @Override
		        public void onClick(DialogInterface d, int n) {
		        	if(n==0){
		        		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		        		photoPickerIntent.setType("image/*");
		        		startActivityForResult(photoPickerIntent, FLAG_IMAGE_GALLERY1); 
		        	}else{
		        		takePhoto1();
		        	}
		            d.dismiss();
		        }

		});
		adb.setNegativeButton("Cancel", null);
		adb.setTitle("Add photo from...");
		adb.show();
	}

	public void preTakePhoto2(View view){
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		CharSequence items[] = new CharSequence[] {"Choose from gallery", "Take photo"};
		adb.setSingleChoiceItems(items, -1, new OnClickListener() {
		        @Override
		        public void onClick(DialogInterface d, int n) {
		        	if(n==0){
		        		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		        		photoPickerIntent.setType("image/*");
		        		startActivityForResult(photoPickerIntent, FLAG_IMAGE_GALLERY2); 
		        	}else{
		        		takePhoto2();
		        	}
		            d.dismiss();
		        }

		});
		adb.setNegativeButton("Cancel", null);
		adb.setTitle("Add photo from...");
		adb.show();
	}
	
	public void takePhoto1(){
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri(FLAG_IMAGE_CAPTURE1));
		startActivityForResult(intent,  FLAG_IMAGE_CAPTURE1);
	}

	public void takePhoto2(){
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri(FLAG_IMAGE_CAPTURE2));
		startActivityForResult(intent,  FLAG_IMAGE_CAPTURE2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.menu_scanner:
			Intent intent = new Intent(QnTemplateCamera.this, ScannerActivity.class);
			QnTemplateCamera.this.startActivity(intent);
			return true;
		case R.id.station1:
			qnService.gotoStation(1);
			return true;
		case R.id.station2:
			qnService.gotoStation(2);
			return true;
		case R.id.station3:
			qnService.gotoStation(3);
			return true;
		case R.id.station4:
			qnService.gotoStation(4);
			return true;
		case R.id.menu_reflection:
			Intent i = new Intent(this,QnTemplateFeedback.class);
			startActivity(i);
			return true;
		case R.id.menu_save:
			saveCurrentSession();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu){
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		MenuItem station1 = menu.findItem(R.id.station1);
		MenuItem station2 = menu.findItem(R.id.station2);
		MenuItem station3 = menu.findItem(R.id.station3);
		MenuItem station4 = menu.findItem(R.id.station4);
		if(dbHandler.isStationFinished(1)){
			station1.setIcon(R.drawable.station_complete);
		}
		if(dbHandler.isStationFinished(2)){
			station2.setIcon(R.drawable.station_complete);
		}
		if(dbHandler.isStationFinished(3)){
			station3.setIcon(R.drawable.station_complete);
		}
		if(dbHandler.isStationFinished(4)){
			station4.setIcon(R.drawable.station_complete);
		}
		return true;
	}

	public Uri setImageUri(int img_pos) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
		String date = dateFormat.format(new Date());
		String photoFile = "img_" + date + ".jpg";
		File file = new File(Environment.getExternalStorageDirectory() +  File.separator + photoFile);
		Uri imgUri = Uri.fromFile(file);
		if(img_pos == FLAG_IMAGE_CAPTURE1){
			abpath1 = file.getAbsolutePath();
			img_name1 = photoFile;
		}else if (img_pos == FLAG_IMAGE_CAPTURE2) {
			abpath2 = file.getAbsolutePath();
			img_name2 = photoFile;
		}

		return imgUri;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		InputStream stream = null;
		if (requestCode == FLAG_IMAGE_CAPTURE1 && resultCode == Activity.RESULT_OK) {
			Bitmap bitmap = BitmapFactory.decodeFile(abpath1);

			//Rescale image
			File f_temp = new File(abpath1);
			FileOutputStream out;
			try{
				//Delete original large file and store in smaller size
				if(f_temp.delete()){
					out = new FileOutputStream(f_temp);
					if(bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out)) 
					{
						out.flush();
						out.close();
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}

			new_bit1 = Bitmap.createScaledBitmap(bitmap, 498, 339, false);
			bitmap.recycle();
			//Hide "your image" tag
			cam_img1_text.setVisibility(View.INVISIBLE);
			//show newly taken image
			cam_img1.setImageBitmap(new_bit1);
			return;
		}else if (requestCode == FLAG_IMAGE_CAPTURE2 && resultCode == Activity.RESULT_OK) {
			Bitmap bitmap = BitmapFactory.decodeFile(abpath2);


			//Rescale image
			File f_temp = new File(abpath2);
			FileOutputStream out;
			try{
				//Delete original large file and store in smaller size
				if(f_temp.delete()){
					out = new FileOutputStream(f_temp);
					if(bitmap.compress(Bitmap.CompressFormat.JPEG,30, out)) 
					{
						out.flush();
						out.close();
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}


			new_bit2 = Bitmap.createScaledBitmap(bitmap, 498, 339, false);
			bitmap.recycle();
			//Hide "your image" tag
			cam_img2_text.setVisibility(View.INVISIBLE);
			//show newly taken image
			cam_img2.setImageBitmap(new_bit2);
			return;
		}else if (requestCode == FLAG_IMAGE_GALLERY1 && resultCode == Activity.RESULT_OK) {
			
			Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            
            Cursor cursor = getContentResolver().query(
                               selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            abpath1 = filePath;
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);

			new_bit1 = Bitmap.createScaledBitmap(bitmap, 498, 339, false);
			bitmap.recycle();
			//Hide "your image" tag
			cam_img1_text.setVisibility(View.INVISIBLE);
			//show newly taken image
			cam_img1.setImageBitmap(new_bit1);
			return;
		}else if (requestCode == FLAG_IMAGE_GALLERY2 && resultCode == Activity.RESULT_OK) {
			
			Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            
            Cursor cursor = getContentResolver().query(
                               selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            abpath2 = filePath;
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);

			new_bit2 = Bitmap.createScaledBitmap(bitmap, 498, 339, false);
			bitmap.recycle();
			//Hide "your image" tag
			cam_img2_text.setVisibility(View.INVISIBLE);
			//show newly taken image
			cam_img2.setImageBitmap(new_bit2);
			return;
		}
	}


	@Override
	protected void onResume() {
		super.onResume();
		Intent intent= new Intent(this, QnService.class);
		bindService(intent, mConnection,Context.BIND_AUTO_CREATE);
		//Reload photos if exists
		if(abpath1!=null){
			File f = new File(abpath1); 	
			if(f.exists()){
				Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
				new_bit1 = Bitmap.createScaledBitmap(bitmap, 480, 360, false);
				bitmap.recycle();
				cam_img1.setImageBitmap(new_bit1);
			}
		}
		if(abpath2!=null){
			File f = new File(abpath2); 	
			if(f.exists()){
				Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
				new_bit2 = Bitmap.createScaledBitmap(bitmap, 480, 360, false);
				bitmap.recycle();
				cam_img2.setImageBitmap(new_bit2);
			}
		}

		//Store current progress
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		dbHandler.saveCurrentProgress(current_position);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindService(mConnection);
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, 
				IBinder binder) {
			QnService.MyBinder b = (QnService.MyBinder) binder;
			qnService = b.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			qnService = null;
		}
	};


	public void nextStep(View view){

		//Store result
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		if(abpath1==null && abpath2 == null){
			Toast.makeText(getApplicationContext(), "You must take at least one photo:(", Toast.LENGTH_SHORT).show();
		}else{
			Log.d("CAM","ABSPATH1:"+abpath1);
			Log.d("CAM","ABSPATH2:"+abpath2);
			dbHandler.storeResultCam(question.getQID(), "IMG", abpath1, abpath2);
			qnService.startNextQuestion(current_position+1);
		}




	}

	public void prevStep(View view){
		//Store result
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		if(abpath1==null && abpath2 == null){
			Toast.makeText(getApplicationContext(), "You must take at least one photo:(", Toast.LENGTH_SHORT).show();
		}else{
			Log.d("CAM","ABSPATH1:"+abpath1);
			Log.d("CAM","ABSPATH2:"+abpath2);
			dbHandler.storeResultCam(question.getQID(), "IMG", abpath1, abpath2);
			qnService.startNextQuestion(current_position+1);
			if(current_position==0){
				finish();
			}else {
				qnService.startNextQuestion(current_position-1);
			}
		}

	}


	public void saveCurrentSession(){
		//Append json generated to upload list
		final DBHandler dbHandler = new DBHandler(getApplicationContext());
		String jsonString = dbHandler.recordToJson().replace("\"","'");
		Log.v("JSON","NEW:"+ jsonString);
		dbHandler.saveSession(jsonString, dbHandler.readCurrentProgress(),dbHandler.isStationFinished(1),
				dbHandler.isStationFinished(2),dbHandler.isStationFinished(3),dbHandler.isStationFinished(4));
		//Clear records 
		dbHandler.clearAnswers();

		//Show dialog
		Builder builder = new AlertDialog.Builder(QnTemplateCamera.this);
		builder.setMessage("Session saved:)");
		builder.setCancelable(false);
		builder.setPositiveButton("Start New Lesson", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//clear answers
				Intent intent = new Intent(QnTemplateCamera.this, MainActivity.class);
				startActivity(intent);
			}
		});
		builder.setNegativeButton("Exit", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();

	}

}
