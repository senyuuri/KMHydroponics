package com.dhs.hydroponics;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes.Name;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.anim;
import android.R.integer;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Resume extends Activity {


	/*
	 * List objects in Resume.java
	 * All share the same order of indexs, i.e. one index in all lists refers to one record
	 * Arraylist<String> sessions -> session content in json format
	 * Arraylist<Integer> positions -> position info of session
	 * Arraylist<String> info -> string of class+names obtained from json strings
	 * 
	 */
	private static final String TAG = "RESUME";
	private Spinner spinner_session;
	private Button btn_next,btn_back;
	private List<Session> sessions;
	List<String> info;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resume);
		spinner_session = (Spinner) findViewById(R.id.spinner_session);
		btn_back = (Button) findViewById(R.id.resume_btn_back);
		btn_next = (Button) findViewById(R.id.resume_btn_continue);
		btn_back.setVisibility(View.VISIBLE);
		btn_next.setVisibility(View.VISIBLE);


	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("RE","onResume");
		//load session list from sql database
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		SQLiteDatabase db = dbHandler.getWritableDatabase();
		sessions = new ArrayList<Session>();
		info = new ArrayList<String>();
		try{
			String selectQuery = "SELECT * FROM " + DBHandler.TABLE_SESSION +" ORDER BY "+DBHandler.SCOLUMN_ID;

			Cursor cursor = db.rawQuery(selectQuery, null);
			//looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					//Construct new session object
					Session session = new Session(cursor.getInt(0), cursor.getString(1), cursor.getInt(2),
							Boolean.valueOf(cursor.getString(3)),
							Boolean.valueOf(cursor.getString(4)),
							Boolean.valueOf(cursor.getString(5)),
							Boolean.valueOf(cursor.getString(6)));
					sessions.add(session);
				} while (cursor.moveToNext());
			}
			// closing connection
			cursor.close();
			db.close();
			// returning session json strings
		}catch(Exception e){
			e.printStackTrace();}


		loadSessionList();


		spinner_session.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {                
			}
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
			}
		});
	}

	public void back(View view){
		finish();
	}


	//Read class and names from json
	private List<String> parseJsonInfo(List<Session> sessions){
		Log.d("RE","parseJsonINFO");
		DBHandler dbHandler = new DBHandler(getApplicationContext());

		if(sessions.size()==0){
			info.add("N/A");
			btn_next.setVisibility(View.INVISIBLE);
			return info;
		}else if(sessions.size()==1){
			if(sessions.get(0).getJson() == null || (sessions.get(0).getJson().equals(""))){
				info.add("N/A");
				dbHandler.deleteSession(sessions.get(0).getSessionID());
				btn_next.setVisibility(View.INVISIBLE);
				return info;
			}
		}
		try{
			for(int i=0;i<sessions.size();i++){
				String temp = sessions.get(i).getJson().replace("'", "\"");
				Log.d("TEST",">>>JSON:"+temp);
				if(temp != null && !(temp.equals(""))){
					JSONObject obj = new JSONObject(temp);
					String record = obj.getString("class") +":"+obj.getString("name1")+","+obj.getString("name2")
							+","+obj.getString("name3")+","+obj.getString("name4")+","+obj.getString("name5")+","+obj.getString("name6");
					Log.d(TAG,"====>info from json:"+record);
					info.add(record);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return info;
	}
	
	private String convertToName(String json){
		String name = "";
		try{
			if(json != null && !(json.equals(""))){
				JSONObject obj = new JSONObject(json);
				name = obj.getString("class") +":"+obj.getString("name1")+","+obj.getString("name2")
						+","+obj.getString("name3")+","+obj.getString("name4")+","+obj.getString("name5")+","+obj.getString("name6");
			}		
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return name;
	}


	private void loadSessionList() {
		Log.d("RE","loadSessionList");
		//Read student info from sessions(json)
		//format: class:name1,name2,name3
		info = parseJsonInfo(sessions);
		// Creating adapter for spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, info);
		// Drop down layout style - list view with radio button
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// attaching data adapter to spinner
		spinner_session.setAdapter(dataAdapter);
	}


	private void resumeRecordFromJson(Session s){
		Log.d("RE","resumeRecordFromJson");
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		String jsonString = s.getJson().replace("'", "\"");
		jsonString = jsonString.replace("&#39;", "'");
		//Parse json and store in table RECORD
		try {
			JSONObject obj = new JSONObject(jsonString);
			JSONArray jarry = obj.getJSONArray("answers");
			for(int i  = 0; i < jarry.length(); i++){
				JSONArray qn = jarry.getJSONArray(i);
				Integer qid = qn.getInt(0);

				//Normal questions
				if(qid<5000){
					int type = dbHandler.getQnType(qid);
					//Text/MCQ/table/comparision/expsetup/MMCQ question, directly write answer
					//Dummy answers included
					if(type==0 || type == 2 || type == 3 || type == 4 || type == 5 || type == 6 || type == 7){
						dbHandler.storeResultQA(qid, qn.getString(1));
					}
					//Photo taking question
					else if(type==1){
						String raw_paths = qn.getString(1);
						String[] img_path = raw_paths.split(",");
						String abspath1,abspath2;
						File file1 = new File(Environment.getExternalStorageDirectory() +  File.separator + img_path[0]);
						File file2 = new File(Environment.getExternalStorageDirectory() +  File.separator + img_path[1]);
						if(file1.exists()){
							abspath1 = file1.getAbsolutePath();
						}else{
							abspath1 = null;
						}
						if(file2.exists()){
							abspath2 = file2.getAbsolutePath();
						}else{
							abspath2 = null;
						}
						dbHandler.storeResultCam(qid, "IMG",abspath1,abspath2);
					}

				}
				//Reflection questions,  5000<qid<8000
				else{
					dbHandler.storeResultQA(qid, qn.getString(1));
					/*
					if(qid>= 5000 && qid<6000){
						dbHandler.storeResultReflection(0, qid, qn.getString(1));
					}
					else if(qid>=6000 && qid<7000){
						dbHandler.storeResultReflection(1, qid, qn.getString(1));
					}else{
						dbHandler.storeResultReflection(2, qid, qn.getString(1));
					}	
					 */
				}
			}
			//Update student names in table PREF
			dbHandler.insertNamePref(obj.getString("class"),obj.getString("name1"), obj.getString("name2"), obj.getString("name3"),
					obj.getString("name4"), obj.getString("name5"), obj.getString("name6"));


		} catch (Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	public void nextStep(View view){

		DBHandler dbHandler = new DBHandler(getApplicationContext());
		String classname = spinner_session.getSelectedItem().toString();
		Session selectedSession = null;
		//Find corresponding session object
		//Since orders of elements in "sessions" and "info" may be different 
		for(int i=0;i<sessions.size();i++){
			String temp = convertToName(sessions.get(i).getJson().replace("'", "\""));
			Log.d("TEST","temp:"+temp);
			Log.d("TEST","classname:"+classname);
			if(temp.equals(classname)){
				selectedSession = sessions.get(i);
			}
		}

		//Clear old answers, reset values in table PREF
		dbHandler.clearAnswers();
		//Parse json and store in table RECORD
		Log.d(TAG, "Spinner Item:"+String.valueOf(spinner_session.getSelectedItemPosition()));
		Log.d(TAG, "Coressponding json:"+selectedSession.getJson());
		resumeRecordFromJson(selectedSession);

		//Delete selected session record
		dbHandler.deleteSession(selectedSession.getSessionID());

		//Resume station state
		if(selectedSession.getSt1State()){
			dbHandler.setStationFinished(1);
		}
		if(selectedSession.getSt2State()){
			dbHandler.setStationFinished(2);
		}
		if(selectedSession.getSt3State()){
			dbHandler.setStationFinished(3);
		}
		if(selectedSession.getSt4State()){
			dbHandler.setStationFinished(4);
		}

		//Get resume positon
		Integer current_progress = selectedSession.getPosition();
		Log.d("SAVE","====> Read progress:"+String.valueOf(current_progress));
		if(current_progress!=-1){
			//Kill QnService if running
			if(!(isQnServiceRunning())){
				Intent intent = new Intent(getApplicationContext(),QnService.class);
				stopService(intent);
			}
			//Restart QnService
			Log.d(TAG, "starting QNSERVICE");
			//retrive question list and to next activity
			//Ordered by station number & QID
			List<Question> qnlist = new ArrayList<Question>();
			SQLiteDatabase db = dbHandler.getWritableDatabase();
			Cursor cursor = db.rawQuery("SELECT * FROM "+ DBHandler.TABLE_QNSET+" ORDER BY "+ 
					DBHandler.QCOLUMN_STATION+","+DBHandler.QCOLUMN_QID,null);	
			if (cursor.moveToFirst()) {
				do {
					Question qn = new Question(cursor.getInt(1),cursor.getInt(0));
					qnlist.add(qn);
				}while (cursor.moveToNext());
			}
			Integer prevStation = 0;
			Integer prevQid = 0;
			//Checkpoints are pages before station page, to be used to mark station status as completed
			ArrayList<Integer> checkpoints = new ArrayList<Integer>();
			//Initialise checkpoints
			for(int i = 0;i<=4;i++){
				checkpoints.add(0);
			}

			for(int i = 0;i<qnlist.size();i++){
				if(qnlist.get(i).getStation() != prevStation){
					prevStation = qnlist.get(i).getStation();
					qnlist.add(i,new Question(prevStation,0));
					//set as checkpoint
					checkpoints.set(prevStation-1,prevQid);
					Log.d("CP","CheckPoint: st:"+String.valueOf(prevStation-1)+" qid:"+String.valueOf(prevQid));
				}
				checkpoints.set(4,qnlist.get(qnlist.size()-1).getQID());
				prevQid = qnlist.get(i).getQID();
			}
			/*FOR DEBUG ONLY
	for(int i = 0;i<qnlist.size();i++){
		Log.d(TAG,String.valueOf(qnlist.get(i).getStation())+String.valueOf(qnlist.get(i).getQID()));
	}
			 */

			db.close();
			//Resume Progress
			Intent intent = new Intent(getApplicationContext(),QnService.class);
			intent.putExtra("qnlist", (Serializable)qnlist);
			intent.putExtra("checkpoints", checkpoints);
			intent.putExtra("current_qn",current_progress);
			startService(intent);

		}else{
			Toast.makeText(this, "No paused activity:)", Toast.LENGTH_LONG).show();	
		}

	}


	private boolean isQnServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (QnService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}



}
