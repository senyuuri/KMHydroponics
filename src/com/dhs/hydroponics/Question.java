package com.dhs.hydroponics;

import java.io.Serializable;

import android.R.integer;
import android.R.string;

public final class Question implements Serializable{
	/*DB decleration
	public static final String TABLE_QNSET = "qnset";
	public static final String QCOLUMN_QID = "_id";
	public static final String QCOLUMN_STATION = "station";
	public static final String QCOLUMN_TYPE = "type";
	public static final String QCOLUMN_QUESTION = "question";
	public static final String QCOLUMN_OPTION = "option";
	private static final String DATABASE_NAME = "database.db";
    */
	private int _station;
	private int _qid;
	
	public Question(int station,int qid){
		this._station = station;
		this._qid = qid;
	}

	public int getStation(){
		return this._station;
	}

	public int getQID(){
		return this._qid;
	}


}
