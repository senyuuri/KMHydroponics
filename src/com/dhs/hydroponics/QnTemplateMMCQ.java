package com.dhs.hydroponics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class QnTemplateMMCQ extends Activity{
	private QnService qnService;
	private Button btn_prev,btn_next,btn_solution;
	private TextView mmcq_progress,mmcq_question,station_name;
	private ImageView correct1,correct2,correct3,correct4,correct5,correct6;
	private RadioGroup radioGroup;
	private CheckBox choice1,choice2,choice3,choice4,choice5,choice6;
	private Question question;
	private Integer current_position,qn_size;
	private String options;
	private String[] option_list;
	private ArrayList<CheckBox> ButtonGroup;
	private ArrayList<ImageView> ImageGroup;
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
		setContentView(R.layout.multiple_selection);
		btn_next = (Button) findViewById(R.id.mmcq_next);
		btn_prev = (Button) findViewById(R.id.mmcq_prev);
		btn_solution = (Button) findViewById(R.id.mmcq_solution);
		mmcq_progress = (TextView) findViewById(R.id.mmcq_progress);
		mmcq_question = (TextView) findViewById(R.id.mmcq_question);
		station_name = (TextView) findViewById(R.id.mmcq_station);
		
		choice1 = (CheckBox) findViewById(R.id.mmcq_checkbox1);
		choice2 = (CheckBox) findViewById(R.id.mmcq_checkbox2);
		choice3 = (CheckBox) findViewById(R.id.mmcq_checkbox3);
		choice4 = (CheckBox) findViewById(R.id.mmcq_checkbox4);
		choice5 = (CheckBox) findViewById(R.id.mmcq_checkbox5);
		choice6 = (CheckBox) findViewById(R.id.mmcq_checkbox6);
		correct1 = (ImageView) findViewById(R.id.mmcq_correct1);
		correct2 = (ImageView) findViewById(R.id.mmcq_correct2);
		correct3 = (ImageView) findViewById(R.id.mmcq_correct3);
		correct4 = (ImageView) findViewById(R.id.mmcq_correct4);
		correct5 = (ImageView) findViewById(R.id.mmcq_correct5);
		correct6 = (ImageView) findViewById(R.id.mmcq_correct6);
		ButtonGroup = new ArrayList<CheckBox>();
		ButtonGroup.add(choice1);
		ButtonGroup.add(choice2);
		ButtonGroup.add(choice3);
		ButtonGroup.add(choice4);
		ButtonGroup.add(choice5);
		ButtonGroup.add(choice6);
		ImageGroup = new ArrayList<ImageView>();
		ImageGroup.add(correct1);
		ImageGroup.add(correct2);
		ImageGroup.add(correct3);
		ImageGroup.add(correct4);
		ImageGroup.add(correct5);
		ImageGroup.add(correct6);
		
		//Load information
		Bundle bundle = getIntent().getExtras();
		question = (Question) bundle.getSerializable("question");
		current_position = bundle.getInt("current_postion");
		qn_size = bundle.getInt("size");
		station_name.setText("Station "+String.valueOf(question.getStation()));
		mmcq_progress.setText(String.valueOf(current_position+1)+"/"+String.valueOf(qn_size));
		Log.d("MMCQ", "ReceivedPositionValue"+String.valueOf(current_position));
		//Set mcq question
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		mmcq_question.setText(dbHandler.getQuestion(question.getQID()));
		//Set mcq options
		options = dbHandler.getOption(question.getQID());
		option_list = options.split(";");
		for(int i=0;i<option_list.length;i++){
			CheckBox tempCkb = ButtonGroup.get(i);
			tempCkb.setText(option_list[i]);
		}
		//Disable checkboxes without text
		for(int i=0;i<ButtonGroup.size();i++){
			CheckBox tempCkb = ButtonGroup.get(i);
			ImageView tempImg = ImageGroup.get(i);
			if(tempCkb.getText().equals("")){
				tempCkb.setVisibility(View.INVISIBLE);
			}
			// hide all ticks
			tempImg.setVisibility(View.INVISIBLE);
		}

		
		//Load answers if the question has been answered
		String st_ans = dbHandler.getStudentAnswer(question.getQID());
		if(st_ans!=null){
			// ans format example: "0,1,0,1"
			String[] newans = st_ans.split("");
			// ans format example: "0,1,0,1"
			Integer qid = question.getQID();
			String answer = dbHandler.getQnAnswer(qid);
			String[] keyans = answer.split(",");
			for(int i=0;i<newans.length;i++){
				if(Integer.valueOf(newans[i]) == 1){
					CheckBox tempCkb = ButtonGroup.get(i);
					tempCkb.setChecked(true);
				}
				
			}
			//show tick if correct
			for(int i=0;i<keyans.length;i++){
				if(Integer.valueOf(keyans[i]) == 1){
					Log.d("KeyAns", String.valueOf(i)+keyans[i]);
					
					//CheckBox tempCkb = ButtonGroup.get(i);
					//show tick if correct
					//Log.d("IsChecked",String.valueOf(tempCkb.isChecked()));
					//if(tempCkb.isChecked()){
					ImageView tempImg = ImageGroup.get(i);
					tempImg.setVisibility(View.VISIBLE);
					//}
				}
			}
			
			
			//Disable button
			btn_solution.setEnabled(false);

			//Disable checkboxes
			for(int i=0;i<ButtonGroup.size();i++){
				CheckBox tempBtn = ButtonGroup.get(i);
				tempBtn.setEnabled(false);
			}
		}

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
			Intent intent = new Intent(QnTemplateMMCQ.this, ScannerActivity.class);
			QnTemplateMMCQ.this.startActivity(intent);
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
		// ans format example: "0,1,0,1"
		Integer qid = question.getQID();
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		String answer = dbHandler.getQnAnswer(qid);
		String[] keyans = answer.split(",");
		Log.d("ShowSolution", "KeyAnswer"+answer);
		for(int i=0;i<keyans.length;i++){
			if(Integer.valueOf(keyans[i]) == 1){
				Log.d("KeyAns", String.valueOf(i)+keyans[i]);
				
				//CheckBox tempCkb = ButtonGroup.get(i);
				//show tick if correct
				//Log.d("IsChecked",String.valueOf(tempCkb.isChecked()));
				//if(tempCkb.isChecked()){
				ImageView tempImg = ImageGroup.get(i);
				tempImg.setVisibility(View.VISIBLE);
				//}
			}
		}
			
		//Disable button
		btn_solution.setEnabled(false);

		String ansString = "";
		for(int i=0;i<ButtonGroup.size();i++){
			CheckBox tempCkb = ButtonGroup.get(i);
			if(tempCkb.getVisibility()==View.VISIBLE){
				//Disable checkboxes
				tempCkb.setEnabled(false);
				//Construct result string
				if(tempCkb.isChecked()){
					ansString += "1,";
				}else{
					ansString += "0,";
				}
			}
		}
		ansString = ansString.substring(0,ansString.length()-1);
		Log.d("MMCQ", "===> MMCQ ansString:" + ansString);
		
		dbHandler.storeResultMMCQ(question.getQID(),ansString);
			
		
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
		Builder builder = new AlertDialog.Builder(QnTemplateMMCQ.this);
		builder.setMessage("Session saved:)");
		builder.setCancelable(false);
		builder.setPositiveButton("Start New Lesson", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//clear answers
				Intent intent = new Intent(QnTemplateMMCQ.this, MainActivity.class);
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
