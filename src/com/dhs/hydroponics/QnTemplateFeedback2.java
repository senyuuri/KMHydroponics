package com.dhs.hydroponics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.PrivateCredentialPermission;

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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class QnTemplateFeedback2 extends Activity{
	private QnService qnService;
	private ArrayList<String> fb_qns;
	private Integer current_progress;
	private Button btn_prev,btn_next;
	private CheckBox checkBox1,checkBox2,checkBox3;
	private TextView fb_progress,fb_st_name,fb_text_qn;
	private EditText fb_text_ans;
	private List<CheckBox> checkBoxs;
	private ArrayList<String> default_titles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qn_feedback2);
		//Initialise reflection questions
		fb_qns = new ArrayList<String>();
		fb_qns.add("We enjoyed using the Galaxy Tab in our project work. ");
		fb_qns.add("We have learnt how to set up a fair experiment, taking into considerations the variables that must be kept constant and the variable to change.");
		fb_qns.add("We have learnt to draw conclusions based on the data collected.");
		fb_qns.add("What is one thing your group would like to change to make the project work better?");
		btn_next = (Button) findViewById(R.id.fb_btn_next);
		btn_prev = (Button) findViewById(R.id.fb_btn_prev);
		fb_progress = (TextView) findViewById(R.id.fb_progress);
		fb_st_name = (TextView) findViewById(R.id.fb_student_name);
		fb_text_qn = (TextView) findViewById(R.id.fb_text_qn);
		fb_text_ans = (EditText) findViewById(R.id.fb_text_ans);
		checkBox1 = (CheckBox) findViewById(R.id.fb_checkbox1);
		checkBox2 = (CheckBox) findViewById(R.id.fb_checkbox2);
		checkBox3 = (CheckBox) findViewById(R.id.fb_checkbox3);
		checkBoxs = new ArrayList<CheckBox>();
		checkBoxs.add(checkBox1);
		checkBoxs.add(checkBox2);
		checkBoxs.add(checkBox3);
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		
		
		//Initialise checkboxs
		for(int i=0;i<3;i++){
			CheckBox tempck = checkBoxs.get(i);
			tempck.setText(fb_qns.get(i));
			//TAG
			if(!(dbHandler.getStudentAnswer(5005+i)==null)){
				tempck.setChecked(Boolean.valueOf(dbHandler.getStudentAnswer(5005+i)));
			}
			
		}
		
		fb_progress.setText("2/2");
		
		//Initialise text question
		fb_text_qn.setText(fb_qns.get(3));
		
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		    @Override
		    public void run() {
		    	DBHandler dbHandler = new DBHandler(getApplicationContext());
		    	dbHandler.saveCurrentProgress(qnService.getQnSize());
		    	current_progress = qnService.getQnSize();
		    	Log.d("test","Received:onCreate"+String.valueOf(current_progress));
		    }
		}, 1000);
		
		
		
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
			Intent intent = new Intent(QnTemplateFeedback2.this, ScannerActivity.class);
			QnTemplateFeedback2.this.startActivity(intent);
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


	public void nextStep(View view){
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		for(int i=0;i<3;i++){
			CheckBox tempck = checkBoxs.get(i);
			if(tempck.isChecked()){
				dbHandler.storeResultReflection(i+5, "true");
			}else{
				dbHandler.storeResultReflection(i+5, "false");
			}
		}
		dbHandler.storeResultReflection(8, fb_text_ans.getText().toString());
		//Load new questions
		Intent intent = new Intent(this,Upload.class);
		startActivity(intent);
	}

	public void prevStep(View view){
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		for(int i=0;i<3;i++){
			CheckBox tempck = checkBoxs.get(i);
			if(tempck.isChecked()){
				dbHandler.storeResultReflection(i+5, "true");
			}else{
				dbHandler.storeResultReflection(i+5, "false");
			}
		}
		dbHandler.storeResultReflection(8, fb_text_ans.getText().toString());
		Log.d("test","prevStep"+String.valueOf(current_progress));
		//qnService.startNextQuestion(current_progress-1);
		finish();
	}
	
	
	public void saveCurrentSession(){
		//Append json generated to upload list
		final DBHandler dbHandler = new DBHandler(getApplicationContext());
		// Bug fixed at v2.1.0 beta
		// Important!
		// escape ' in the json string to avoid unterminated array error
		// Using html escape scheme
		String jsonString = dbHandler.recordToJson().replace("'", "&#39;");
		jsonString = jsonString.replace("\"","'");

		Log.v("JSON","NEW:"+ jsonString);
		dbHandler.saveSession(jsonString, dbHandler.readCurrentProgress(),dbHandler.isStationFinished(1),
				dbHandler.isStationFinished(2),dbHandler.isStationFinished(3),dbHandler.isStationFinished(4));
		//Clear records 
		dbHandler.clearAnswers();
		
		//Show dialog
		Builder builder = new AlertDialog.Builder(QnTemplateFeedback2.this);
		builder.setMessage("Session saved:)");
		builder.setCancelable(false);
		builder.setPositiveButton("Start New Lesson", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//clear answers
				Intent intent = new Intent(QnTemplateFeedback2.this, MainActivity.class);
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