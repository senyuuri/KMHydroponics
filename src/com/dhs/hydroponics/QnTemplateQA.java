package com.dhs.hydroponics;

import java.io.File;
import java.util.ArrayList;

import javax.xml.transform.Templates;

import android.R.anim;
import android.R.string;
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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
public class QnTemplateQA extends Activity{
	private Button btn_prev,btn_next,btn_solution;
	private TextView station_name,qa_progress,qa_question,qa_answer;
	private EditText qa_input;
	private ImageView qa_img,qa_correct,qa_wrong;
	private Question question;
	private Integer current_position,qn_size;
	private QnService qnService;
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
		setContentView(R.layout.qn_qa);
		btn_next = (Button) findViewById(R.id.mcq_next);
		btn_prev = (Button) findViewById(R.id.mcq_prev);
		btn_solution = (Button) findViewById(R.id.qa_solution);
		qa_progress = (TextView) findViewById(R.id.qa_progress);
		qa_question = (TextView) findViewById(R.id.qa_question);
		station_name = (TextView) findViewById(R.id.qa_station_name);
		qa_input = (EditText) findViewById(R.id.qa_input);
		qa_correct = (ImageView) findViewById(R.id.qa_correct);
		qa_wrong = (ImageView) findViewById(R.id.qa_wrong);
		Bundle bundle = getIntent().getExtras();
		question = (Question) bundle.getSerializable("question");
		current_position = bundle.getInt("current_postion");
		qn_size = bundle.getInt("size");
		station_name.setText("Station "+String.valueOf(question.getStation()));
		qa_progress.setText(String.valueOf(current_position+1)+"/"+String.valueOf(qn_size));
		qa_answer = (TextView) findViewById(R.id.qa_solution_text);
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.qa_solution_frame);
		Log.d("QA", "ReceivedPositionValue"+String.valueOf(current_position));
		//Set QA question
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		qa_question.setText(dbHandler.getQuestion(question.getQID()));
		//Hide tick and cross
		qa_correct.setVisibility(View.INVISIBLE);
		qa_wrong.setVisibility(View.INVISIBLE);
		
		//If no answer, change solution button to "Submit" only
		//Hide correct answer area
		if(dbHandler.getQnAnswer(question.getQID()).equals("NA")){
			btn_solution.setText("Submit");
			linearLayout.setVisibility(View.INVISIBLE);
		}
		
		//Load answers if the question has been answered
		String ans = dbHandler.getStudentAnswer(question.getQID());
		if(ans!=null){
			if(dbHandler.getQnAnswer(question.getQID()).equals("NA")){
				//Set answer
				//Still can edit & submit for open question
				qa_input.setText(ans);
				//Disable button
				//qa_input.setKeyListener(null);
				//btn_solution.setEnabled(false);
			}else{
				qa_input.setText(ans);
				qa_input.setKeyListener(null);
				//Set answer
				Integer qid = question.getQID();
				String answer = dbHandler.getQnAnswer(qid);
				qa_answer.setText(answer);
				
				//set tick and cross
				if(ans.toLowerCase().equals(answer.toLowerCase())){
					qa_correct.setVisibility(View.VISIBLE);
				}else{
					qa_wrong.setVisibility(View.VISIBLE);
				}
				
				//Disable button
				btn_solution.setEnabled(false);
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
			Intent intent = new Intent(QnTemplateQA.this, ScannerActivity.class);
			QnTemplateQA.this.startActivity(intent);
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
		try{
			//Store result
			if(qa_input.getText().toString().equals("")){
				Toast.makeText(getApplicationContext(), "Please type something:(", Toast.LENGTH_SHORT).show();
			}else{
				qnService.startNextQuestion(current_position+1);
			}
		}catch(Exception e){
			e.printStackTrace();
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
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		if(qa_input.getText().toString().equals("")){
			Toast.makeText(getApplicationContext(), "Please try the question first!",Toast.LENGTH_SHORT).show();
		}else if(dbHandler.getQnAnswer(question.getQID()).equals("NA")){
			
			
			//Store result
			dbHandler.storeResultQA(question.getQID(), qa_input.getText().toString());
			Toast.makeText(getApplicationContext(), "Submit success!", Toast.LENGTH_SHORT).show();
			//Disable button
			//qa_input.setKeyListener(null);
			//btn_solution.setEnabled(false);
		}else{
			qa_input.setKeyListener(null);
			
			//Set answer
			Integer qid = question.getQID();
			String answer = dbHandler.getQnAnswer(qid);
			qa_answer.setText(answer);
			
			//Store result
			dbHandler.storeResultQA(question.getQID(), qa_input.getText().toString());
			Toast.makeText(getApplicationContext(), "Submit success", Toast.LENGTH_SHORT).show();
			
			//Show tick or cross
			if(qa_input.getText().toString().toLowerCase().equals(answer.toLowerCase())){
					qa_correct.setVisibility(View.VISIBLE);
				}else{
					qa_wrong.setVisibility(View.VISIBLE);
				}
				
			//Disable button
			btn_solution.setEnabled(false);
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
		Builder builder = new AlertDialog.Builder(QnTemplateQA.this);
		builder.setMessage("Session saved:)");
		builder.setCancelable(false);
		builder.setPositiveButton("Start New Lesson", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//clear answers
				Intent intent = new Intent(QnTemplateQA.this, MainActivity.class);
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
