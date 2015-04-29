package com.dhs.hydroponics;

import java.io.Serializable;

import android.R.string;

public class ChartElement implements Serializable{
	/*DB decleration
	*Table6:Session list
	public static final String TABLE_SESSION = "session";
	public static final String SCOLUMN_ID = "_id";
	public static final String SCOLUMN_JSON = "json";
	public static final String SCOLUMN_POSITION = "position";

    */
	private String _name;
	private Double _value;
	private String _colour;
	
	
	public ChartElement(String name,Double value,String colour) {
		this._name = name;
		this._value = value;
		this._colour = colour;
	}
	

	public void setName(String name){
		this._name = name;
	}
	
	public void setValue(Double value){
		this._value = value;
	}
	
	public void setColour(String colour){
		this._colour = colour;
	}
	
	public String getName(){
		return this._name;
	}
	
	public Double getValue(){
		return this._value;
	}
	
	public String getColour(){
		return this._colour;
	}

}