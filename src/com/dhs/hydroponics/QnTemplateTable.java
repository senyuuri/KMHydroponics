package com.dhs.hydroponics;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.util.Printer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class QnTemplateTable extends Activity{
	private Button btn_prev,btn_next,btn_solution;
	private TextView table_progress,table_title;
	private EditText qa_input;
	private Question question;
	private Integer current_position,qn_size;
	private QnService qnService;
	private FrameLayout LinearL;
	private ArrayList<EditText> cellList;
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
		setContentView(R.layout.qn_table);
		btn_next = (Button) findViewById(R.id.table_next);
		btn_prev = (Button) findViewById(R.id.table_prev);
		table_progress = (TextView) findViewById(R.id.table_progress);
		table_title = (TextView) findViewById(R.id.table_title);
		LinearL = (FrameLayout) findViewById(R.id.table_layout);
		Bundle bundle = getIntent().getExtras();
		question = (Question) bundle.getSerializable("question");
		current_position = bundle.getInt("current_postion");
		qn_size = bundle.getInt("size");

		table_progress.setText(String.valueOf(current_position+1)+"/"+String.valueOf(qn_size));

		Log.d("QA", "ReceivedPositionValue"+String.valueOf(current_position));
		//Set QA question
		final DBHandler dbHandler = new DBHandler(getApplicationContext());

		//Set default title
		default_titles = new ArrayList<String>();
		default_titles.add("Instruction");
		default_titles.add("Planting and Harvesting");
		default_titles.add("Problem-based Learning");
		default_titles.add("Our Progress");
		table_title.setText(default_titles.get(question.getStation()-1));
		//Load custome title if exists
		String custome_title = dbHandler.getQnTitle(question.getQID());
		if(custome_title != " " && custome_title != "NA"){
			table_title.setText(custome_title);
		}
		
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		    @Override
		    public void run() {
		    	//wait for onResume() to finish
		    	//restore student answer if present
				String raw_json = dbHandler.getStudentAnswer(question.getQID());
				if(raw_json!=null && !(raw_json.equals(""))){
					jsonToCell(raw_json.replace("'","\""));
				}
		    }
		}, 1000);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public List<View> getAllChildViews() {
		View view = this.getWindow().getDecorView();
		return getAllChildViews(view);
	}

	private List<View> getAllChildViews(View view) {
		List<View> allchildren = new ArrayList<View>();
		if (view instanceof ViewGroup) {
			ViewGroup vp = (ViewGroup) view;
			for (int i = 0; i < vp.getChildCount(); i++) {
				View viewchild = vp.getChildAt(i);
				allchildren.add(viewchild);
				allchildren.addAll(getAllChildViews(viewchild));
			}
		}
		return allchildren;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.menu_scanner:
			Intent intent = new Intent(QnTemplateTable.this, ScannerActivity.class);
			QnTemplateTable.this.startActivity(intent);
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

		//Load all cell into a list
		cellList = new ArrayList<EditText>();
		List<View> views = getAllChildViews();
		for(int i=0;i<views.size();i++){

			if(views.get(i) instanceof EditText){
				Log.d("CELL", views.get(i).toString());
				cellList.add((EditText)views.get(i));
			}
		}
		Log.d("CELL", "CellList size:"+cellList.size());

	
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



	private String prepareSaveJson(){
		JSONArray jArray = new JSONArray();
		for(int j=1;j<=3;j++){
			for(int i=0;i<cellList.size();i++){

				EditText temp = (EditText) cellList.get(i);
				if(temp!=null){
					String[] tag = temp.getTag().toString().split(",");
					Integer row = Integer.valueOf(tag[0]);
					Integer colomn = Integer.valueOf(tag[1]);

					if(colomn==j){
						JSONArray tempArray = new JSONArray();
						tempArray.put(row);
						tempArray.put(colomn);
						if(temp.getText().toString().equals("")){
							//Use 0 to replace empty entry
							tempArray.put(0);
						}else{
							tempArray.put(Integer.valueOf(temp.getText().toString()));
						}
						jArray.put(tempArray);
						Log.d("TABLE","***prepare***"+tempArray.toString());
					}
				}

			}

		}
		Log.d("TABLE","***prepareSaveJson***"+jArray.toString());
		return jArray.toString();
	}



	private String prepareGraphJson(){
		String[] color_set = {"#0080c9","#2b7f39","#b10058"};
		JSONArray jArray = new JSONArray();
		try{
			for(int j=1;j<=3;j++){
				JSONObject item = new JSONObject();
				item.put("name","Set-up "+String.valueOf(j));
				JSONArray tempArray = new JSONArray();
				for(int i=0;i<cellList.size();i++){
					EditText temp = (EditText) cellList.get(i);
					if(temp!=null){
						String[] tag = temp.getTag().toString().split(",");
						Integer colomn = Integer.valueOf(tag[1]);
						//TODO-check order
						Log.d("TABLE","***prepare***j:"+String.valueOf(j)+"i:"+String.valueOf(i));
						if(colomn==j){
							if(temp.getText().toString().equals("")){
								//Use 0 to replace empty entry
								tempArray.put(0);
							}else{
								tempArray.put(Integer.valueOf(temp.getText().toString()));
							}
							Log.d("TABLE","***prepare***"+tempArray.toString());
						}
					}
				}
				item.put("value", tempArray);
				item.put("color", color_set[j-1]);
				jArray.put(item);
			}
			Log.d("TABLE","***prepareGraphJson***"+jArray.toString());
		}catch(JSONException e){
			e.printStackTrace();
		}
		return jArray.toString();
	}

	private void jsonToCell(String jString){
		try {
			JSONArray jsonArray = new JSONArray(jString);
			for(int j=0;j<jsonArray.length();j++){
				JSONArray tempArray = (JSONArray) jsonArray.get(j);
				for(int i=0;i<cellList.size();i++){
					EditText temp = (EditText) cellList.get(i);
					if(temp!=null){
						if(tempArray.getString(0).equals(temp.getTag().toString().split(",")[0])){
							if(tempArray.getString(1).equals(temp.getTag().toString().split(",")[1])){
								temp.setText(String.valueOf(tempArray.getInt(2)));
							}
						}

					}
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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

	public void plotGraph(View view){

		try{
			//Store result
			String raw_json = prepareSaveJson();
			raw_json = raw_json.replace("\"","'");
			DBHandler dbHandler = new DBHandler(getApplicationContext());
			dbHandler.storeResultTable(question.getQID(),raw_json);
		}catch(Exception e){
			e.printStackTrace();
		}
		Intent intent = new Intent(this,PlotGraph.class);
		String jString = prepareGraphJson();
		intent.putExtra("json", jString);
		startActivity(intent);

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
		Builder builder = new AlertDialog.Builder(QnTemplateTable.this);
		builder.setMessage("Session saved:)");
		builder.setCancelable(false);
		builder.setPositiveButton("Start New Lesson", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//clear answers
				Intent intent = new Intent(QnTemplateTable.this, MainActivity.class);
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



