package com.dhs.hydroponics;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class InfoSelection extends Activity{

	private static final String TAG = "InfoSelect";
	private Spinner spinner_class, spinner_name1,spinner_name2,spinner_name3,spinner_name4,spinner_name5,spinner_name6;
	private Button btn_next;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_selection);
		spinner_class = (Spinner) findViewById(R.id.spinner_class);
		spinner_name1 = (Spinner) findViewById(R.id.spinner_name1);
		spinner_name2 = (Spinner) findViewById(R.id.spinner_name2);
		spinner_name3 = (Spinner) findViewById(R.id.spinner_name3);
		spinner_name4 = (Spinner) findViewById(R.id.spinner_name4);
		spinner_name5 = (Spinner) findViewById(R.id.spinner_name5);
		spinner_name6 = (Spinner) findViewById(R.id.spinner_name6);
		btn_next = (Button) findViewById(R.id.btn_info_next);

		//load class list from sql database
		loadClassList();


		spinner_class.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {                
			}
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				//Reload name list after choosing a class
				String selected = arg0.getItemAtPosition(arg2).toString();
				loadNames(selected);
			}
		});

		spinner_name1.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {                
			}
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
			}
		});


	}


	private void loadClassList() {
		DBHandler db = new DBHandler(getApplicationContext());
		// Spinner Drop down elements
		List<String> classes = db.getAllClasses();
		// Creating adapter for spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, classes);
		// Drop down layout style - list view with radio button
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// attaching data adapter to spinner
		spinner_class.setAdapter(dataAdapter);
	}


	private void loadNames(String s_class) {
		DBHandler db = new DBHandler(getApplicationContext());
		// Spinner Drop down elements
		List<String> names = db.getNames(s_class);
		names.add(0,"N/A");
		// Creating adapter for spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, names);
		// Drop down layout style - list view with radio button
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// attaching data adapter to spinner
		spinner_name1.setAdapter(dataAdapter);
		spinner_name2.setAdapter(dataAdapter);
		spinner_name3.setAdapter(dataAdapter);
		spinner_name4.setAdapter(dataAdapter);
		spinner_name5.setAdapter(dataAdapter);
		spinner_name6.setAdapter(dataAdapter);
	}


	public void nextStep(View view){
		//must select at least one name
		if((spinner_name1.getSelectedItem().toString() == "N/A")&&
				(spinner_name2.getSelectedItem().toString() == "N/A")&&	
				(spinner_name3.getSelectedItem().toString() == "N/A")&&	
				(spinner_name4.getSelectedItem().toString() == "N/A")&&
				(spinner_name5.getSelectedItem().toString() == "N/A")&&
				(spinner_name6.getSelectedItem().toString() == "N/A")){
			Toast.makeText(getApplicationContext(), "Must select at least one name:(", Toast.LENGTH_SHORT).show();
		}else {


			DBHandler dbHandler = new DBHandler(getApplicationContext());
			dbHandler.clearAnswers();
			//Store selected name in database
			dbHandler.insertNamePref(spinner_class.getSelectedItem().toString(), 
					spinner_name1.getSelectedItem().toString(), 
					spinner_name2.getSelectedItem().toString(),
					spinner_name3.getSelectedItem().toString(),
					spinner_name4.getSelectedItem().toString(),
					spinner_name5.getSelectedItem().toString(),
					spinner_name6.getSelectedItem().toString());
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
				prevQid = qnlist.get(i).getQID();
			}
			checkpoints.set(4,qnlist.get(qnlist.size()-1).getQID());
			/*FOR DEBUG ONLY
		for(int i = 0;i<qnlist.size();i++){
			Log.d(TAG,String.valueOf(qnlist.get(i).getStation())+String.valueOf(qnlist.get(i).getQID()));
		}
			 */

			db.close();
			
			Intent intent = new Intent(getApplicationContext(),QnService.class);
			intent.putExtra("qnlist", (Serializable)qnlist);
			intent.putExtra("checkpoints", checkpoints);
			intent.putExtra("current_qn",0);
			startService(intent);
		}
	}

}
