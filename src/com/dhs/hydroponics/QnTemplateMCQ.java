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

public class QnTemplateMCQ extends Activity{
	private QnService qnService;
	private Button btn_prev,btn_next,btn_solution;
	private TextView mcq_progress,mcq_question,station_name,mcq_answer;
	private ImageView mcq_img,mcq_correct,mcq_wrong;
	private RadioGroup radioGroup;
	private RadioButton choice1,choice2,choice3,choice4;
	private Question question;
	private Integer current_position,qn_size;
	private String options;
	private String[] option_list;
	private List<RadioButton> choiceButtons;
	private Bitmap bitmap,new_bit;
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
		setContentView(R.layout.qn_mcq);
		btn_next = (Button) findViewById(R.id.mcq_next);
		btn_prev = (Button) findViewById(R.id.mcq_prev);
		btn_solution = (Button) findViewById(R.id.mcq_solution);
		mcq_progress = (TextView) findViewById(R.id.mcq_progress);
		mcq_question = (TextView) findViewById(R.id.mcq_question);
		station_name = (TextView) findViewById(R.id.mcq_station_name);
		mcq_answer = (TextView) findViewById(R.id.mcq_solution_text);
		mcq_correct = (ImageView) findViewById(R.id.mcq_correct);
		mcq_wrong = (ImageView) findViewById(R.id.mcq_wrong);
		radioGroup = (RadioGroup) findViewById(R.id.mcq_group);
		choice1 = (RadioButton) findViewById(R.id.mcq_choice1);
		choice2 = (RadioButton) findViewById(R.id.mcq_choice2);
		choice3 = (RadioButton) findViewById(R.id.mcq_choice3);
		choice4 = (RadioButton) findViewById(R.id.mcq_choice4);
		choiceButtons = new ArrayList<RadioButton>();
		choiceButtons.add(choice1);
		choiceButtons.add(choice2);
		choiceButtons.add(choice3);
		choiceButtons.add(choice4);
		//Load information
		Bundle bundle = getIntent().getExtras();
		question = (Question) bundle.getSerializable("question");
		current_position = bundle.getInt("current_postion");
		qn_size = bundle.getInt("size");
		station_name.setText("Station "+String.valueOf(question.getStation()));
		mcq_progress.setText(String.valueOf(current_position+1)+"/"+String.valueOf(qn_size));
		Log.d("MCQ", "ReceivedPositionValue"+String.valueOf(current_position));
		//Set mcq question
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		mcq_question.setText(dbHandler.getQuestion(question.getQID()));
		//Set mcq options
		options = dbHandler.getOption(question.getQID());
		option_list = options.split(";");
		for(int i=0;i<option_list.length;i++){
			RadioButton tempBtn = choiceButtons.get(i);
			tempBtn.setText(option_list[i]);
		}
		//Disable radio buttons without text
		for(int i=0;i<choiceButtons.size();i++){
			RadioButton tempBtn = choiceButtons.get(i);
			if(tempBtn.getText().equals("")){
				tempBtn.setVisibility(View.INVISIBLE);
			}
		}

		//hide tick and cross
		mcq_correct.setVisibility(View.INVISIBLE);
		mcq_wrong.setVisibility(View.INVISIBLE);


		//Load answers if the question has been answered
		String ans = dbHandler.getStudentAnswer(question.getQID());
		if(ans!=null){
			if(ans.equals(choice1.getText().toString())){
				choice1.setChecked(true);
			}
			else if(ans.equals(choice2.getText().toString())){
				choice2.setChecked(true);
			}
			else if(ans.equals(choice3.getText().toString())){
				choice3.setChecked(true);
			}
			else if(ans.equals(choice4.getText().toString())){
				choice4.setChecked(true);
			}

			Integer qid = question.getQID();
			String answer = dbHandler.getQnAnswer(qid);
			mcq_answer.setText(answer);

			//show tick if correct
			if(ans.equals(answer)){
				mcq_correct.setVisibility(View.VISIBLE);
			}else{
				mcq_wrong.setVisibility(View.VISIBLE);
			}

			//Disable button
			btn_solution.setEnabled(false);

			//Disable radio buttons 
			for(int i=0;i<choiceButtons.size();i++){
				RadioButton tempBtn = choiceButtons.get(i);
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
		if(!custome_title.equals(" ") && !custome_title.equals("NA")){
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
			Intent intent = new Intent(QnTemplateMCQ.this, ScannerActivity.class);
			QnTemplateMCQ.this.startActivity(intent);
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

		//Store result
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		RadioButton btn_temp = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
		if(btn_temp == null){
			Toast.makeText(getApplicationContext(), "You must select an option to continue:(", Toast.LENGTH_SHORT).show();
		}else{

			qnService.startNextQuestion(current_position+1);
		}

	}

	public void prevStep(View view){
		if(current_position==0){
			finish();
		}else {
			qnService.startNextQuestion(current_position-1);
		}
	}

	public void showSolution(View view){
		Boolean choose = false;
		for(int i=0;i<=3;i++){
			RadioButton temp = choiceButtons.get(i);
			if(temp.isChecked()){
				choose = true;
			}
		}
		if(!choose){
			Toast.makeText(getApplicationContext(), "Try the question first!",Toast.LENGTH_SHORT).show();
		}

		else{
			//set answer
			Integer qid = question.getQID();
			DBHandler dbHandler = new DBHandler(getApplicationContext());
			String answer = dbHandler.getQnAnswer(qid);
			mcq_answer.setText(answer);

			//store result
			RadioButton btn_temp = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
			dbHandler.storeResultMCQ(question.getQID(), (String) btn_temp.getText());

			//Disable button
			btn_solution.setEnabled(false);


			//show tick if correct
			if(btn_temp.getText().toString().equals(answer)){
				mcq_correct.setVisibility(View.VISIBLE);
			}else{
				mcq_wrong.setVisibility(View.VISIBLE);
			}

			//Disable radio buttons 
			for(int i=0;i<choiceButtons.size();i++){
				RadioButton tempBtn = choiceButtons.get(i);
				tempBtn.setEnabled(false);
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
		Builder builder = new AlertDialog.Builder(QnTemplateMCQ.this);
		builder.setMessage("Session saved:)");
		builder.setCancelable(false);
		builder.setPositiveButton("Start New Lesson", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//clear answers
				Intent intent = new Intent(QnTemplateMCQ.this, MainActivity.class);
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
