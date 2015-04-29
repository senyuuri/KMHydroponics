package com.dhs.hydroponics;

import java.io.Serializable;

import android.R.integer;

public class Session implements Serializable{
	/*DB decleration
	*Table6:Session list
	public static final String TABLE_SESSION = "session";
	public static final String SCOLUMN_ID = "_id";
	public static final String SCOLUMN_JSON = "json";
	public static final String SCOLUMN_POSITION = "position";

    */
	private int _id;
	private String _json;
	private int _position;
	private Boolean _st1;
	private Boolean _st2;
	private Boolean _st3;
	private Boolean _st4;
	
	public Session(int id,String json,int position,Boolean st1,Boolean st2,Boolean st3,Boolean st4){
		this._id = id;
		this._json = json;
		this._position = position;
		this._st1 = st1;
		this._st2 = st2;
		this._st3 = st3;
		this._st4 = st4;
	}
	
	

	public int getSessionID(){
		return this._id;
	}
	
	public String getJson(){
		return this._json;
	}

	public int getPosition(){
		return this._position;
	}
	
	public Boolean getSt1State(){
		return this._st1;
	}
	
	public Boolean getSt2State(){
		return this._st2;
	}
	
	public Boolean getSt3State(){
		return this._st3;
	}
	public Boolean getSt4State(){
		return this._st4;
	}


}
