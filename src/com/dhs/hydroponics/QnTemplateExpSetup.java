package com.dhs.hydroponics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class QnTemplateExpSetup extends Activity{
	private QnService qnService;
	private Button btn_prev,btn_next,btn_solution;
	private TextView exp_progress,exp_question,station_name;
	private RadioGroup radioGroup1,radioGroup2,radioGroup3,radioGroup4,radioGroup5,radioGroup6,
						radioGroup7,radioGroup8;
	private RadioButton radioButton1,radioButton2,radioButton3,radioButton4,radioButton5,
						radioButton6,radioButton7,radioButton8,radioButton9,radioButton10,
						radioButton11,radioButton12,radioButton13,radioButton14,radioButton15,
						radioButton16;
	private Question question;
	private Integer current_position,qn_size;
	private String options;
	private String[] option_list;
	private ArrayList<RadioGroup> radioGroups;
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
		setContentView(R.layout.experiment_setup);
		btn_next = (Button) findViewById(R.id.com_next);
		btn_prev = (Button) findViewById(R.id.com_prev);
		btn_solution = (Button) findViewById(R.id.exp_solution);
		exp_progress = (TextView) findViewById(R.id.exp_progress);
		station_name = (TextView) findViewById(R.id.exp_title);
		radioGroup1 = (RadioGroup) findViewById(R.id.exp_group1);
		radioGroup2 = (RadioGroup) findViewById(R.id.exp_group2);
		radioGroup3 = (RadioGroup) findViewById(R.id.exp_group3);
		radioGroup4 = (RadioGroup) findViewById(R.id.exp_group4);
		radioGroup5 = (RadioGroup) findViewById(R.id.exp_group5);
		radioGroup6 = (RadioGroup) findViewById(R.id.exp_group6);
		radioGroup7 = (RadioGroup) findViewById(R.id.exp_group7);
		radioGroup8 = (RadioGroup) findViewById(R.id.exp_group8);
		radioButton1 = (RadioButton) findViewById(R.id.exp_btn1);
		radioButton2 = (RadioButton) findViewById(R.id.exp_btn2);
		radioButton3 = (RadioButton) findViewById(R.id.exp_btn3);
		radioButton4 = (RadioButton) findViewById(R.id.exp_btn4);
		radioButton5 = (RadioButton) findViewById(R.id.exp_btn5);
		radioButton6 = (RadioButton) findViewById(R.id.exp_btn6);
		radioButton7 = (RadioButton) findViewById(R.id.exp_btn7);
		radioButton8 = (RadioButton) findViewById(R.id.exp_btn8);
		radioButton9 = (RadioButton) findViewById(R.id.exp_btn9);
		radioButton10 = (RadioButton) findViewById(R.id.exp_btn10);
		radioButton11 = (RadioButton) findViewById(R.id.exp_btn11);
		radioButton12 = (RadioButton) findViewById(R.id.exp_btn12);
		radioButton13 = (RadioButton) findViewById(R.id.exp_btn13);
		radioButton14 = (RadioButton) findViewById(R.id.exp_btn14);
		radioButton15 = (RadioButton) findViewById(R.id.exp_btn15);
		radioButton16 = (RadioButton) findViewById(R.id.exp_btn16);
		
		radioGroups = new ArrayList<RadioGroup>();
		radioGroups.add(radioGroup1);
		radioGroups.add(radioGroup2);
		radioGroups.add(radioGroup3);
		radioGroups.add(radioGroup4);
		radioGroups.add(radioGroup5);
		radioGroups.add(radioGroup6);
		radioGroups.add(radioGroup7);
		radioGroups.add(radioGroup8);

		//Load information
		Bundle bundle = getIntent().getExtras();
		question = (Question) bundle.getSerializable("question");
		current_position = bundle.getInt("current_postion");
		qn_size = bundle.getInt("size");
		station_name.setText("Station "+String.valueOf(question.getStation()));
		exp_progress.setText(String.valueOf(current_position+1)+"/"+String.valueOf(qn_size));
		Log.d("ExpSetupQn", "ReceivedPositionValue"+String.valueOf(current_position));
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		/**Set custome question
		 * reserved for future versions
		exp_question.setText(dbHandler.getQuestion(question.getQID()));
		**/

		//Load answers if the question has been answered
		String ans = dbHandler.getStudentAnswer(question.getQID());
		if(ans!=null){
			String[] newans = ans.split(",");
			for(int  i=0; i<newans.length;i++){
				Integer option = Integer.valueOf(newans[i]);
				RadioGroup tempGroup = radioGroups.get(i);
				if(option != -1){
					RadioButton tempBtn = (RadioButton) tempGroup.getChildAt(option);
					tempBtn.setChecked(true);
				}
			}
			
			// disable submit button
			btn_solution.setEnabled(false);
			

			//Get the list of radiobuttons
			//Disable radio buttons 
			// ArrayList<RadioButton> listOfRadioButtons = new ArrayList<RadioButton>();
			for(int j=0;j<radioGroups.size();j++){
				RadioGroup tempGp = radioGroups.get(j);
				int count = tempGp.getChildCount();
				for (int i=0;i<count;i++) {
					View o = tempGp.getChildAt(i);
					if (o instanceof RadioButton) {
						// listOfRadioButtons.add((RadioButton)o);
						o.setEnabled(false);
					}
				}
			}

		}
		
		
		
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
			Intent intent = new Intent(QnTemplateExpSetup.this, ScannerActivity.class);
			QnTemplateExpSetup.this.startActivity(intent);
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
		qnService.startNextQuestion(current_position+1);
	}

	public void prevStep(View view){
		if(current_position==0){
			finish();
		}else {
			qnService.startNextQuestion(current_position-1);
		}
	}

	public void showSolution(View view){
		String ansString = "";
		for(int  i=0; i<radioGroups.size();i++){
			RadioGroup tempGroup = radioGroups.get(i);
			int radioButtonID = tempGroup.getCheckedRadioButtonId();
			View radioButton = tempGroup.findViewById(radioButtonID);
			int idx = tempGroup.indexOfChild(radioButton);
			ansString += String.valueOf(idx) + ",";
			
		}
		ansString = ansString.substring(0,ansString.length()-1);
		Log.d("ExpSetupQn","===>ansString" + ansString);
		
		//store result
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		dbHandler.storeResultExpSetup(question.getQID(), ansString);

		//Disable button
		btn_solution.setEnabled(false);

		//Get the list of radiobuttons
		//Disable radio buttons 
		// ArrayList<RadioButton> listOfRadioButtons = new ArrayList<RadioButton>();
		for(int j=0;j<radioGroups.size();j++){
			RadioGroup tempGp = radioGroups.get(j);
			int count = tempGp.getChildCount();
			for (int i=0;i<count;i++) {
				View o = tempGp.getChildAt(i);
				if (o instanceof RadioButton) {
					// listOfRadioButtons.add((RadioButton)o);
					o.setEnabled(false);
				}
			}
		}

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
		Builder builder = new AlertDialog.Builder(QnTemplateExpSetup.this);
		builder.setMessage("Session saved:)");
		builder.setCancelable(false);
		builder.setPositiveButton("Start New Lesson", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//clear answers
				Intent intent = new Intent(QnTemplateExpSetup.this, MainActivity.class);
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
