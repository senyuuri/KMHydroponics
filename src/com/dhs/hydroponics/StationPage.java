package com.dhs.hydroponics;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.R.menu;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class StationPage extends Activity{
	private ImageView station_img;
	private Button btn_prev,btn_next;
	private TextView station_progress,station_name,station_title;
	private Question question;
	private Integer current_position,qn_size;
	private QnService qnService;
	private Bitmap bitmap,new_bit;
	private ArrayList<String> default_titles;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.station_page);
		btn_prev = (Button) findViewById(R.id.station_prev);
		btn_next = (Button) findViewById(R.id.station_next);
		station_name = (TextView) findViewById(R.id.station_name);
		station_progress = (TextView) findViewById(R.id.station_progress);
		station_title = (TextView) findViewById(R.id.station_title);
		//Get current progress
		Bundle bundle = getIntent().getExtras();
		question = (Question) bundle.getSerializable("question");
		current_position = bundle.getInt("current_position");
		Log.d("Station", "ReceivedPositionValue"+String.valueOf(current_position));
		DBHandler dbHandler = new DBHandler(getApplicationContext());

		qn_size = bundle.getInt("size");
		station_title.setText("Let's move to:");
		station_progress.setText(String.valueOf(current_position+1)+"/"+String.valueOf(qn_size));
		
		//Set default title
		default_titles = new ArrayList<String>();
		default_titles.add("Instruction");
		default_titles.add("Planting and Harvesting");
		default_titles.add("Problem-based Learning");
		default_titles.add("Our Progress");
		station_name.setText(default_titles.get(question.getStation()-1));
		
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
			Intent intent = new Intent(StationPage.this, ScannerActivity.class);
			StationPage.this.startActivity(intent);
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

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent= new Intent(this, QnService.class);
		bindService(intent, mConnection,Context.BIND_AUTO_CREATE);

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
		
		qnService.startNextQuestion(current_position+1);
	}

	public void prevStep(View view){
		if(current_position==0){
			finish();
		}else {
			qnService.startNextQuestion(current_position-1);
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
		Builder builder = new AlertDialog.Builder(StationPage.this);
		builder.setMessage("Session saved:)");
		builder.setCancelable(false);
		builder.setPositiveButton("Start New Lesson", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//clear answers
				Intent intent = new Intent(StationPage.this, MainActivity.class);
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
