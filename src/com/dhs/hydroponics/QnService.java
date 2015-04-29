package com.dhs.hydroponics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class QnService extends Service{
	private final IBinder mBinder = new MyBinder();
	private List<Question> _qnlist;
	private Integer _initial_qn,_qn_size;
	private DBHandler dbHandler;
	private ArrayList<Integer> _checkpoints;

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle bundle = intent.getExtras();  
		this._qnlist = (List<Question>) bundle.getSerializable("qnlist");
		this._initial_qn = bundle.getInt("current_qn");
		this._qn_size = this._qnlist.size();
		this._checkpoints = bundle.getIntegerArrayList("checkpoints");
		//initial_qn is set to 0 by default
		//TODO if last time not finished, start from pause position
		startNextQuestion(this._initial_qn);
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	public class MyBinder extends Binder {
		QnService getService() {
			return QnService.this;
		}
	}
	
	public Integer getQnSize(){
		return this._qn_size;
	}
	
	public void startNextQuestion(Integer next_position){
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		Log.d("QnS","NextQnRequest:"+String.valueOf(next_position));
		//check if current station has been finished
		checkStationFinish();
		//next_position: next question order in qnlist
		//If all questions have been answered, goto Upload page
		if(next_position == this._qnlist.size()){
			Intent i = new Intent();
			i.setClass(this, QnTemplateFeedback.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra("student", 1);
			i.putExtra("current_position", next_position);
			Log.d("QnS","Station_PassDownPositionValue:"+String.valueOf(next_position));
			startActivity(i);
		}else{
			Question qn = this._qnlist.get(next_position);
			//Goto station page
			if(qn.getQID() == 0){
				Intent i = new Intent();
				i.setClass(this, StationPage.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("question", (Serializable)qn);
				i.putExtra("current_position", next_position);
				Log.d("QnS","Station_PassDownPositionValue:"+String.valueOf(next_position));
				i.putExtra("size", this._qn_size);
				startActivity(i);
			//Goto question page
			}else {
				//retrive question type
				Integer qid = qn.getQID();
				Log.d("QnService", "QID:"+String.valueOf(qid));
				Intent i = new Intent();
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("question", (Serializable)qn);
				i.putExtra("current_postion", next_position);
				Log.d("QnS","PassDownPositionValue:"+String.valueOf(next_position));
				i.putExtra("size", this._qn_size);
				int type = dbHandler.getQnType(qid);
				Log.d("QnService","Type:"+String.valueOf(type));
				switch (type) {
				//type 0: MCQ 
				case 0:
					i.setClass(this, QnTemplateMCQ.class);
					startActivity(i);
					break;
				//type 1: Photo
				case 1:
					i.setClass(this, QnTemplateCamera.class);
					startActivity(i);
					break;
				//type 2:QA
				case 2:
					i.setClass(this, QnTemplateQA.class);
					startActivity(i);
					break;
				//type 3:Table
				case 3:
					i.setClass(this, QnTemplateTable.class);
					startActivity(i);
					break;
				//type 4:Instruction
				case 4:
					//if no image attached
					if(dbHandler.getOption(qid).equals("0")){
						i.setClass(this,Instruction1.class);
					}
					//else have base-64 image
					else{
						i.setClass(this, Instruction2.class);
					}
					startActivity(i);
					break;
				//type 5: MMCQ(allow multiple checked options)
				case 5:
					i.setClass(this, QnTemplateMMCQ.class);
					startActivity(i);
					break;
					
				//type 6: Comparision (fixed in this version)
				case 6:
					i.setClass(this, QnTemplateComparision.class);
					startActivity(i);
					break;
				
				//type 7: Experiment Setup (fixed in this version)
				case 7:
					i.setClass(this, QnTemplateExpSetup.class);
					startActivity(i);
					break;
				}
			}
		}
		
		
	}
	
	public void checkStationFinish(){
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		/*
		for(int i=1;i<=4;i++){
			Log.d("FINISH","Now:"+String.valueOf(this._qnlist.get(now_position).getQID())+" ck:"+String.valueOf(this._checkpoints.get(i)));
			if(this._qnlist.get(now_position).getQID()==this._checkpoints.get(i)){
				dbHandler.setStationFinished(this._qnlist.get(now_position).getStation());
				Log.d("FINISH","!!!!!!!!!StFinished:"+String.valueOf(this._qnlist.get(now_position).getStation()));
			}
		}
		*/
		for(int i=1;i<=4;i++){
			Log.d("FINISH","Qn in Station"+String.valueOf(i)+":"+dbHandler.getStationQnNum(i));
			Log.d("FINISH","Ans in Station"+String.valueOf(i)+":"+dbHandler.getStationAnswerNum(i));
			if(dbHandler.getStationAnswerNum(i)==dbHandler.getStationQnNum(i)){
				dbHandler.setStationFinished(i);
			}
		}
	}
	
	public void startPrevQuestion(int prev_position){
		
	}
	
	public void gotoStation(Integer station_num){
		checkStationFinish();
		for(int i=0;i<this._qnlist.size();i++){
			Question qn = this._qnlist.get(i);
			if(qn.getStation() == station_num && qn.getQID()==0){
				Intent intent = new Intent();
				intent.setClass(this, StationPage.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("question", (Serializable)qn);
				intent.putExtra("current_position", i);
				Log.d("QnS","gotoStation_position:"+String.valueOf(i));
				intent.putExtra("size", this._qn_size);
				startActivity(intent);
			}
		}
	}

}