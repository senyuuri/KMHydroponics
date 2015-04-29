package com.dhs.hydroponics;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHandler extends SQLiteOpenHelper {

	//Table1:name list
	public static final String TABLE_NAMELIST = "namelist";
	public static final String NCOLUMN_ID = "_id";
	public static final String NCOLUMN_CLASS = "class";
	public static final String NCOLUMN_NAME = "name";

	//Table2:question set
	public static final String TABLE_QNSET = "qnset";
	public static final String QCOLUMN_QID = "_id";
	//*design rejected, reserved for backup
	//number column used by server only(to indicate individual question)
	//public static final String QCOLUMN_NUM = "number";
	public static final String QCOLUMN_STATION = "station";
	public static final String QCOLUMN_TYPE = "type";
	public static final String QCOLUMN_QUESTION = "question";
	public static final String QCOLUMN_OPTION = "option";
	public static final String QCOLUMN_ANSWER = "answer";
	public static final String QCOLUMN_TITLE= "title";
	

	private static final String DATABASE_NAME = "database.db";

	//Table3:answers
	public static final String TABLE_RECORD = "record";
	public static final String RCOLUMN_QID = "_id";
	public static final String RCOLUMN_ANSWER = "answer";
	//IMG tag in answer field indicates presence of photo in extra field
	public static final String RCOLUMN_EXTRA = "extra";
	public static final String RCOLUMN_EXTRA2 = "extra2";

	//Table4:preferences & students' name
	//key:upload   upload status, "T" by default, "F" indicates previous uploading process not finished
	//key:name1    first students' name
	//key:name2    second students' name
	//key:name_date    datetime of local namelist
	//key:qn_date	   datetime of local question set
	public static final String TABLE_PREF = "pref";
	public static final String PCOLUMN_KEY = "key";
	public static final String PCOLUMN_VALUE = "value";

	//Table5:Upload list
	public static final String TABLE_UPLOAD = "uplist";
	public static final String UCOLUMN_ID = "_id";
	public static final String UCOLUMN_JSON = "json";

	//Table6:Session list
	public static final String TABLE_SESSION = "session";
	public static final String SCOLUMN_ID = "_id";
	public static final String SCOLUMN_JSON = "json";
	public static final String SCOLUMN_POSITION = "position";
	public static final String SCOLUMN_ST1 = "station1";
	public static final String SCOLUMN_ST2 = "station2";
	public static final String SCOLUMN_ST3 = "station3";
	public static final String SCOLUMN_ST4 = "station4";


	private static final int DATABASE_VERSION = 33;

	// Database creation sql statement
	public static final String CREATE_TABLE_NAMELIST = "create table "
			+ TABLE_NAMELIST + "(" + NCOLUMN_ID +" integer primary key autoincrement, " + NCOLUMN_CLASS
			+ " text not null, " + NCOLUMN_NAME
			+ " text not null);";

	public static final String CREATE_TABLE_QNSET = "create table "
			+ TABLE_QNSET + "(" + QCOLUMN_QID  + " integer primary key, "
			+ QCOLUMN_STATION + " integer not null, "
			+ QCOLUMN_TYPE + " integer not null, "
			+ QCOLUMN_QUESTION + " text not null, "
			+ QCOLUMN_OPTION + " text, "
			+ QCOLUMN_ANSWER + " text, " 
			+ QCOLUMN_TITLE + " text);";

	public static final String CREATE_TABLE_RECORD = "create table "
			+ TABLE_RECORD + "(" + RCOLUMN_QID + " integer primary key, " + RCOLUMN_ANSWER +
			" text not null, " + RCOLUMN_EXTRA + " text, "+ RCOLUMN_EXTRA2 + " text);";

	public static final String CREATE_TABLE_PREF = "create table "
			+ TABLE_PREF + "(" + PCOLUMN_KEY + " text primary key, " + PCOLUMN_VALUE + " text not null);";

	public static final String CREATE_TABLE_UPLOAD = "create table "
			+ TABLE_UPLOAD + "(" + UCOLUMN_ID + " integer primary key autoincrement, " + UCOLUMN_JSON + " text not null);";

	public static final String CREATE_TABLE_SESSION = "create table "
			+ TABLE_SESSION + "(" + SCOLUMN_ID + " integer primary key autoincrement, " + SCOLUMN_JSON + " text not null, "
			+SCOLUMN_POSITION +" integer not null, "
			+SCOLUMN_ST1+" text not null, "
			+SCOLUMN_ST2+" text not null, "
			+SCOLUMN_ST3+" text not null, "
			+SCOLUMN_ST4+" text not null);";


	public DBHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_NAMELIST);
		database.execSQL(CREATE_TABLE_QNSET);
		database.execSQL(CREATE_TABLE_RECORD);
		database.execSQL(CREATE_TABLE_PREF);
		database.execSQL(CREATE_TABLE_UPLOAD);
		database.execSQL(CREATE_TABLE_SESSION);
		//set default pref values
		try{
			//Log.d("PrefInitSQL:", "INSERT INTO "+ TABLE_PREF +  " VALUES (\"upload\",\"T\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"upload\",\"T\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"class\",\"N/A\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"name1\",\"N/A\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"name2\",\"N/A\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"name3\",\"N/A\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"name4\",\"N/A\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"name5\",\"N/A\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"name6\",\"N/A\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"name_date\",'2010-01-01,00:00:00')");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"qn_date\",'2010-01-01,00:00:00')");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"current_position\",\"-1\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"st1_finish\",\"false\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"st2_finish\",\"false\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"st3_finish\",\"false\")");
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"st4_finish\",\"false\")");
			/*Session old version(Backup)
			database.execSQL("INSERT INTO "+ TABLE_PREF +  " VALUES (\"current_session\",\"-1\")");
			 */
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHandler.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAMELIST);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_QNSET);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORD);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREF);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_UPLOAD);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
		onCreate(db);
	}

	//Insert new name into table namelist
	public void insertName(String s_class, String s_name){
		try{
			SQLiteDatabase db = this.getWritableDatabase();         
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_NAMELIST + " ("+NCOLUMN_CLASS+","+NCOLUMN_NAME+") VALUES (\"" +
					s_class +"\",\"" +s_name+"\")");
			//db.close(); // Closing database connection
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	//Insert new question into table qnset
	public void insertQuestion(Integer qid,Integer station,Integer type,String question,String option,String answer,String title){
		try{
			SQLiteDatabase db = this.getWritableDatabase();         
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_QNSET +" ("+QCOLUMN_QID+","+QCOLUMN_STATION+","+QCOLUMN_TYPE+","+
					QCOLUMN_QUESTION+","+QCOLUMN_OPTION+","+QCOLUMN_ANSWER+","+QCOLUMN_TITLE+") VALUES (\"" +
					qid +"\",\"" +station+"\",\"" +type+"\",\"" +question+"\",\"" +option+"\",\"" +answer+"\",\"" +title+"\")");
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	public void insertNamePref(String s_class, String s_name1, String s_name2, String s_name3, String s_name4, String s_name5, String s_name6){
		try{
			SQLiteDatabase db = this.getWritableDatabase();         
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"class\",\""+s_class+"\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"name1\",\""+s_name1+"\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"name2\",\""+s_name2+"\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"name3\",\""+s_name3+"\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"name4\",\""+s_name4+"\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"name5\",\""+s_name5+"\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"name6\",\""+s_name6+"\")");
			Log.d("DB","Insert name1:"+s_name1);
			Log.d("DB","Insert name2:"+s_name2);
			Log.d("DB","Insert name3:"+s_name3);
			Log.d("DB","Insert name4:"+s_name4);
			Log.d("DB","Insert name5:"+s_name5);
			Log.d("DB","Insert name6:"+s_name6);
			db.close(); // Closing database connection
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//return timestamp of local namelist
	public String getNamelistVer(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PREF + " WHERE " + PCOLUMN_KEY + "=\"name_date\"", null);
		cursor.moveToFirst();
		db.close();
		return cursor.getString(1);
	}

	//return timestamp of local namelist
	public String getQnsetVer(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PREF + " WHERE " + PCOLUMN_KEY + "=\"qn_date\"", null);
		cursor.moveToFirst();
		db.close();
		return cursor.getString(1);
	}

	//*FOR SPINNER IN CLASS InfoSelection: Return list of classes
	public List<String> getAllClasses(){
		List<String> classes = new ArrayList<String>();
		try{
			String selectQuery = "SELECT DISTINCT "+ NCOLUMN_CLASS + " FROM " + TABLE_NAMELIST;
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);
			//looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					//TODO check 0 or 1
					classes.add(cursor.getString(0));
				} while (cursor.moveToNext());
			}
			// closing connection
			cursor.close();
			db.close();
			// returning lables
		}catch(Exception e){
			e.printStackTrace();}
		return classes;

	}

	//*FOR SPINNER IN CLASS InfoSelection: Return name list from the selected class
	public List<String> getNames(String s_class){
		List<String> names = new ArrayList<String>();
		try{
			String selectQuery = "SELECT * FROM " + TABLE_NAMELIST+" WHERE "+ NCOLUMN_CLASS +"=\""+s_class+"\"";
			SQLiteDatabase db = this.getReadableDatabase();
			Log.v("execSQL",selectQuery);
			Cursor cursor = db.rawQuery(selectQuery, null);
			//looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					//Log.v("result","cursor.get0"+cursor.getString(0)+" get1"+cursor.getString(1));
					names.add(cursor.getString(2));
				} while (cursor.moveToNext());
			}
			// closing connection
			cursor.close();
			db.close();
			// returning lables
		}catch(Exception e){
			e.printStackTrace();}
		return names;
	}

	public String getStudentName1(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PREF + " WHERE " + PCOLUMN_KEY + "=\"name1\"", null);
		cursor.moveToFirst();
		db.close();
		return cursor.getString(1);
	}

	public String getStudentName2(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PREF + " WHERE " + PCOLUMN_KEY + "=\"name2\"", null);
		cursor.moveToFirst();
		db.close();
		return cursor.getString(1);
	}


	public String getStudentName3(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PREF + " WHERE " + PCOLUMN_KEY + "=\"name3\"", null);
		cursor.moveToFirst();
		db.close();
		return cursor.getString(1);
	}


	public String getStudentName4(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PREF + " WHERE " + PCOLUMN_KEY + "=\"name4\"", null);
		cursor.moveToFirst();
		db.close();
		return cursor.getString(1);
	}


	public String getStudentName5(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PREF + " WHERE " + PCOLUMN_KEY + "=\"name5\"", null);
		cursor.moveToFirst();
		db.close();
		return cursor.getString(1);
	}


	public String getStudentName6(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PREF + " WHERE " + PCOLUMN_KEY + "=\"name6\"", null);
		cursor.moveToFirst();
		db.close();
		return cursor.getString(1);
	}

	public String getStudentClass(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PREF + " WHERE " + PCOLUMN_KEY + "=\"class\"", null);
		cursor.moveToFirst();
		db.close();
		return cursor.getString(1);
	}



	public int getQnType(Integer qid){
		//Log.d("DBHandler","Now running getQnType");
		int type=-1;
		if(qid<5000){
			try{
				SQLiteDatabase db = this.getReadableDatabase();
				Cursor cursor = db.rawQuery("SELECT "+ DBHandler.QCOLUMN_TYPE+" FROM "+DBHandler.TABLE_QNSET+
						" WHERE "+ DBHandler.QCOLUMN_QID +"="+qid.toString(),null);
				//Log.d("DBHandler", "SELECT "+ DBHandler.QCOLUMN_TYPE+" FROM "+DBHandler.TABLE_QNSET+" WHERE "+ DBHandler.QCOLUMN_QID +"="+qid.toString());
				cursor.moveToFirst();
				type = cursor.getInt(0);
				db.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return type;
	}

	public String getQuestion(Integer qid){
		String question = "";
		try{
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT "+ DBHandler.QCOLUMN_QUESTION+" FROM "+DBHandler.TABLE_QNSET+
					" WHERE "+ DBHandler.QCOLUMN_QID +"="+qid.toString(),null);
			Log.d("DBHandler", "SELECT "+ DBHandler.QCOLUMN_QUESTION+" FROM "+DBHandler.TABLE_QNSET+
					" WHERE "+ DBHandler.QCOLUMN_QID +"="+qid.toString());
			cursor.moveToFirst();
			question = cursor.getString(0);
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return question;
	}

	public String getOption(Integer qid){
		String option = "";
		try{
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT "+ DBHandler.QCOLUMN_OPTION+" FROM "+DBHandler.TABLE_QNSET+
					" WHERE "+ DBHandler.QCOLUMN_QID +"="+qid.toString(),null);
			Log.d("DBHandler", "SELECT "+ DBHandler.QCOLUMN_OPTION+" FROM "+DBHandler.TABLE_QNSET+
					" WHERE "+ DBHandler.QCOLUMN_QID +"="+qid.toString());
			cursor.moveToFirst();
			option = cursor.getString(0);
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return option;
	}
	
	public String getQnTitle(Integer qid){
		String title = "";
		try{
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT "+ DBHandler.QCOLUMN_TITLE+" FROM "+DBHandler.TABLE_QNSET+
					" WHERE "+ DBHandler.QCOLUMN_QID +"="+qid.toString(),null);
			Log.d("DBHandler", "SELECT "+ DBHandler.QCOLUMN_TITLE+" FROM "+DBHandler.TABLE_QNSET+
					" WHERE "+ DBHandler.QCOLUMN_QID +"="+qid.toString());
			cursor.moveToFirst();
			title = cursor.getString(0);
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return title;
	}
	

	//For question type: instruction2 ONLY
	public String getBase64(Integer qid){
		String baseString = "";
		try{
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT "+ DBHandler.QCOLUMN_ANSWER+" FROM "+DBHandler.TABLE_QNSET+
					" WHERE "+ DBHandler.QCOLUMN_QID +"="+qid.toString(),null);
			Log.d("DBHandler", "SELECT "+ DBHandler.QCOLUMN_ANSWER+" FROM "+DBHandler.TABLE_QNSET+
					" WHERE "+ DBHandler.QCOLUMN_QID +"="+qid.toString());
			cursor.moveToFirst();
			//Original format: (format),(base64 string)
			baseString = cursor.getString(0).split(",")[1];
			Log.d("Base64",baseString);
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return baseString;
	}


	//FOR ACTIVITY QnTemplateMCQ
	public void storeResultMCQ(int qid, String ans){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_RECORD +" ("+RCOLUMN_QID+","+RCOLUMN_ANSWER+
					") VALUES (\"" +qid +"\",\""+ans+"\")");
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//FOR ACTIVITY QnTemplateMMCQ
	public void storeResultMMCQ(int qid, String ans){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_RECORD +" ("+RCOLUMN_QID+","+RCOLUMN_ANSWER+
					") VALUES (\"" +qid +"\",\""+ans+"\")");
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//FOR ACTIVITY QnTemplateComparision
	public void storeResultComparision(int qid, String ans){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_RECORD +" ("+RCOLUMN_QID+","+RCOLUMN_ANSWER+
					") VALUES (\"" +qid +"\",\""+ans+"\")");
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//FOR ACTIVITY QnTemplateComparision
	public void storeResultExpSetup(int qid, String ans){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_RECORD +" ("+RCOLUMN_QID+","+RCOLUMN_ANSWER+
					") VALUES (\"" +qid +"\",\""+ans+"\")");
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

		
	//FOR ACTIVITY QnTemplateCamera
	public void storeResultCam(int qid, String ans,String path1, String path2){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_RECORD +" ("+RCOLUMN_QID+","+RCOLUMN_ANSWER+
					","+RCOLUMN_EXTRA+","+RCOLUMN_EXTRA2+") VALUES (\"" +qid +"\",\""+ans+
					"\",\""+path1+"\",\""+path2+"\")");
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//FOR ACTIVITY QnTemplateQA
	public void storeResultQA(int qid, String ans){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_RECORD +" ("+RCOLUMN_QID+","+RCOLUMN_ANSWER+
					") VALUES (\"" +qid +"\",\""+ans+"\")");
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//For station tag changing function
	//   Add dummy answers to let the number of overall answers in a station
	//   equals to the number of questions
	public void storeResultDummy(int qid){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_RECORD +" ("+RCOLUMN_QID+","+RCOLUMN_ANSWER+
					") VALUES (\"" +qid +"\",\"DUMMY\")");
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//FOR ACTIVITY QnTemplateFeedback
	public void storeResultReflection(Integer qid, String ans){
		String new_qid = "";
		if(String.valueOf(qid).length() == 1){
			new_qid = "500"+ String.valueOf(qid);
		}else{
			new_qid = "50" + String.valueOf(qid);
		}

		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_RECORD +" ("+RCOLUMN_QID+","+RCOLUMN_ANSWER+
					") VALUES (\"" +new_qid +"\",\""+ans+"\")");
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void storeResultTable(int qid, String ans){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_RECORD +" ("+RCOLUMN_QID+","+RCOLUMN_ANSWER+
					") VALUES (\"" +qid +"\",\""+ans+"\")");
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//FOR ACTIVITY Upload
	//"Save Record" button
	public void appendToUpList(String json){
		try{
			SQLiteDatabase db = this.getWritableDatabase();         
			db.execSQL("INSERT INTO "+ TABLE_UPLOAD +"( " + UCOLUMN_JSON + ") VALUES (\"" +json +"\")");
			db.close(); // Closing database connection
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//Drop all answers
	public void clearAnswers(){
		try{
			Log.v("SQL","Drop table "+ TABLE_RECORD);
			SQLiteDatabase db = this.getWritableDatabase();     
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORD);
			Log.v("SQL","Create new table "+TABLE_RECORD);
			db.execSQL(CREATE_TABLE_RECORD);
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"st1_finish\",\"false\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"st2_finish\",\"false\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"st3_finish\",\"false\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"st4_finish\",\"false\")");

			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"current_position\",\"-1\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"name1\",\"N/A\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"name2\",\"N/A\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"name3\",\"N/A\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"name4\",\"N/A\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"name5\",\"N/A\")");
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +  " VALUES (\"name6\",\"N/A\")");

			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void clearSessions(){
		try{
			Log.v("SQL","Drop table "+ TABLE_SESSION);
			SQLiteDatabase db = this.getWritableDatabase();     
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
			Log.v("SQL","Create new table "+TABLE_SESSION);
			db.execSQL(CREATE_TABLE_SESSION);
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}



	//Drop all upload list if success
	public void clearUploadList(){
		try{
			Log.v("SQL","Drop table "+ TABLE_UPLOAD);
			SQLiteDatabase db = this.getWritableDatabase();     
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_UPLOAD);
			Log.v("SQL","Create new table "+TABLE_UPLOAD);
			db.execSQL(CREATE_TABLE_UPLOAD);
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	//Return number of terms in upload list
	public Integer getUploadNum(){
		Integer num = 0;
		SQLiteDatabase db = this.getWritableDatabase();     
		Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_UPLOAD, null);
		num = cursor.getCount();
		db.close();
		return num;
	}

	//Return number of terms in name list
	public Integer getNamelistNum(){
		Integer num = 0;
		SQLiteDatabase db = this.getWritableDatabase();     
		Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_NAMELIST, null);
		num = cursor.getCount();
		db.close();
		return num;
	}

	//Return number of terms in question set
	public Integer getQnSetNum(){
		Integer num = 0;
		SQLiteDatabase db = this.getWritableDatabase();     
		Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_QNSET, null);
		num = cursor.getCount();
		db.close();
		return num;
	}


	public Integer getStationQnNum(Integer station){
		Integer num = 0;
		SQLiteDatabase db = this.getWritableDatabase();     
		Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_QNSET+" WHERE "+QCOLUMN_STATION+"="+String.valueOf(station), null);
		num = cursor.getCount();
		db.close();
		return num;
	}

	//Return number of questions in a station
	public Integer getStationAnswerNum(Integer station){
		Integer num = 0;
		SQLiteDatabase db = this.getWritableDatabase();     
		Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_RECORD+" WHERE "+
				RCOLUMN_QID+">="+String.valueOf(station*1000)+
				" AND "+RCOLUMN_QID+"<"+String.valueOf((station+1)*1000), null);
		num = cursor.getCount();
		db.close();
		return num;
	}



	public String getQnAnswer(Integer qid){
		String answer = "";
		try{
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT "+ QCOLUMN_ANSWER+" FROM "+DBHandler.TABLE_QNSET+
					" WHERE "+ DBHandler.QCOLUMN_QID +"="+qid.toString(),null);
			cursor.moveToFirst();
			answer = cursor.getString(0);
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return answer;
	}

	public String getStudentAnswer(Integer qid){
		String answer = "";
		try{
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT "+ RCOLUMN_ANSWER+" FROM "+DBHandler.TABLE_RECORD+
					" WHERE "+ DBHandler.RCOLUMN_QID +"="+qid.toString(),null);
			if(cursor.getCount()==0){
				db.close();
				return null;
			}else{
				cursor.moveToFirst();
				answer = cursor.getString(0);
				db.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return answer;
	}

	public String getStudentPhoto(Integer qid){
		String answer = "";
		try{
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT * FROM "+DBHandler.TABLE_RECORD+
					" WHERE "+ DBHandler.RCOLUMN_QID +"="+qid.toString(),null);
			if(cursor.getCount()==0){
				db.close();
				return null;
			}
			else{
				cursor.moveToFirst();
				answer = cursor.getString(2)+","+cursor.getString(3);
				db.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return answer;
	}

	public void saveCurrentProgress(Integer current_position){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +" ("+PCOLUMN_KEY+","+PCOLUMN_VALUE+
					") VALUES (\"current_position\",\""+String.valueOf(current_position)+"\")");
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public Integer readCurrentProgress(){
		Integer current_progress = -1;
		try{
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT "+ PCOLUMN_VALUE+" FROM "+DBHandler.TABLE_PREF+
					" WHERE "+ DBHandler.PCOLUMN_KEY +"=\"current_position\"",null);
			cursor.moveToFirst();
			current_progress = Integer.valueOf(cursor.getString(0));
			db.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return current_progress;
	}

	public void setStationFinished(Integer station){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +" ("+PCOLUMN_KEY+","+PCOLUMN_VALUE+
					") VALUES (\"st"+ String.valueOf(station)+ "_finish\",\"true\")");
			Log.d("DB","SetFinish:"+String.valueOf(station));
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public Boolean isStationFinished(Integer station){
		Boolean state = false;
		try{
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT "+ PCOLUMN_VALUE+" FROM "+DBHandler.TABLE_PREF+
					" WHERE "+ DBHandler.PCOLUMN_KEY +"=\"st"+ String.valueOf(station)+ "_finish\"",null);
			cursor.moveToFirst();
			state = Boolean.valueOf(cursor.getString(0));
			//Log.d("DB","ReadFinish:"+String.valueOf(station)+String.valueOf(state));
			db.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return state;
	}


	public void saveSession(String json, Integer current_position,Boolean st1,Boolean st2,Boolean st3,Boolean st4){
		/*Session old version(Backup)
		//Current session has not been saved, create new record
		if(current_session == -1){

		}
		//Delete original record in session table
		else{
			deleteSession(current_session);
		}
		 *
		 *New design: old session record will always be deleted at resume page 
		 *whenever it is selected to be resumed
		 */
		try{
			SQLiteDatabase db = this.getWritableDatabase();         
			db.execSQL("INSERT INTO "+ TABLE_SESSION 
					+"( " + SCOLUMN_JSON +","+SCOLUMN_POSITION +","+SCOLUMN_ST1+","+SCOLUMN_ST2
					+","+SCOLUMN_ST3 +","+SCOLUMN_ST4
					+") VALUES (\""+ json +"\","+String.valueOf(current_position)
					+",\""+String.valueOf(st1)+"\""
					+",\""+String.valueOf(st2)+"\""
					+",\""+String.valueOf(st3)+"\""
					+",\""+String.valueOf(st4)+"\")");
			Log.d("DB","====>Saving session:"+String.valueOf(current_position)+" , "+json);
			db.close(); // Closing database connection
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public void deleteSession(Integer session_id){
		try{
			SQLiteDatabase db = this.getWritableDatabase();         
			db.execSQL("DELETE FROM "+TABLE_SESSION+" WHERE "+SCOLUMN_ID + "=" + String.valueOf(session_id) );
			Log.d("DB","====>Delete session:"+String.valueOf(session_id));
			db.close(); // Closing database connection
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/*Session old version
	public void saveSessionID(Integer current_session){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+ TABLE_PREF +" ("+PCOLUMN_KEY+","+PCOLUMN_VALUE+
					") VALUES (\"current_session\",\""+String.valueOf(current_session)+"\")");
			Log.d("DB","====>SaveSessionID:"+String.valueOf(current_session));
			db.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public Integer readSessionID(){
		Integer session_id = -2;
		try{
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT "+ PCOLUMN_VALUE+" FROM "+DBHandler.TABLE_PREF+
					" WHERE "+ DBHandler.PCOLUMN_KEY +"=\"current_session\"",null);
			cursor.moveToFirst();
			session_id = Integer.valueOf(cursor.getString(0));
			Log.d("DB","====>ReadSessionID:"+String.valueOf(session_id));
			db.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return session_id;
	}
	 */

	public String recordToJson(){
		String rawJson = "";
		JSONObject jObject = new JSONObject();
		try {
			jObject.put("class", this.getStudentClass());
			jObject.put("name1", this.getStudentName1());
			jObject.put("name2", this.getStudentName2());
			jObject.put("name3", this.getStudentName3());
			jObject.put("name4", this.getStudentName4());
			jObject.put("name5", this.getStudentName5());
			jObject.put("name6", this.getStudentName6());
			JSONArray answers = new JSONArray();
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT * FROM "+ DBHandler.TABLE_RECORD+" ORDER BY "+ 
					DBHandler.RCOLUMN_QID,null);	
			if (cursor.moveToFirst()) {
				do {
					Integer qid = cursor.getInt(0);
					//Normal questions
					if(qid<5000){
						//Get question type
						Cursor cursor2 = db.rawQuery("SELECT "+ DBHandler.QCOLUMN_TYPE+" FROM "+DBHandler.TABLE_QNSET+
								" WHERE "+ DBHandler.QCOLUMN_QID +"="+qid.toString(),null);
						cursor2.moveToFirst();
						int type = cursor2.getInt(0);

						//Text/MCQ/table question, directly get answer
						//dummy answer included
						if(type==0 || type == 2 || type==3 || type ==4 || type == 5 || type == 6 || type == 7){
							Cursor cursor3 = db.rawQuery("SELECT "+ DBHandler.RCOLUMN_ANSWER+ " FROM "+DBHandler.TABLE_RECORD+
									" WHERE "+ DBHandler.RCOLUMN_QID +"="+qid.toString(),null);
							cursor3.moveToFirst();
							String answer = cursor3.getString(0);
							if(answer.equals("")){
								answer = "N/A";
							}
							JSONArray temp = new JSONArray();
							temp.put(qid).put(answer);
							answers.put(temp);
						}
						//Photo taking question, get file name, separated by ,
						else if(type==1){
							//Get first img name
							Cursor cursor3 = db.rawQuery("SELECT "+ DBHandler.RCOLUMN_EXTRA+ " FROM "+DBHandler.TABLE_RECORD+
									" WHERE "+ DBHandler.RCOLUMN_QID +"="+qid.toString(),null);
							cursor3.moveToFirst();
							String raw_img1 = cursor3.getString(0);
							String[] sarray_img1 = raw_img1.split("/");
							raw_img1 = sarray_img1[sarray_img1.length-1];
							//Get second img name
							cursor3 = db.rawQuery("SELECT "+ DBHandler.RCOLUMN_EXTRA2+ " FROM "+DBHandler.TABLE_RECORD+
									" WHERE "+ DBHandler.RCOLUMN_QID +"="+qid.toString(),null);
							cursor3.moveToFirst();
							String raw_img2 = cursor3.getString(0);
							String[] sarray_img2 = raw_img2.split("/");
							raw_img2 = sarray_img2[sarray_img2.length-1];
							JSONArray temp = new JSONArray();
							temp.put(qid).put(raw_img1+","+raw_img2);
							answers.put(temp);
						}

					}
					//Reflection questions,  5000<qid<8000
					else{
						Cursor cursor4 = db.rawQuery("SELECT "+ DBHandler.RCOLUMN_ANSWER+ " FROM "+DBHandler.TABLE_RECORD+
								" WHERE "+ DBHandler.RCOLUMN_QID +"="+qid.toString(),null);
						cursor4.moveToFirst();
						String answer = cursor4.getString(0);
						JSONArray temp = new JSONArray();
						temp.put(qid).put(answer);
						answers.put(temp);
					}
				}while (cursor.moveToNext());
				jObject.put("answers", answers);
				rawJson = jObject.toString();
				db.close();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return rawJson;

	}

	/*FOR REFERENCE
	 *Now two classes have been integrated together since the appearance of session object
	 *Session reading operation will be carried out in Resume.java

	 *FOR SPINNER IN CLASS resume: Return list of saved sessions
	public List<String> getAllSessions(){
		List<String> sessions = new ArrayList<String>();
		try{
			String selectQuery = "SELECT "+ SCOLUMN_JSON + " FROM " + TABLE_SESSION +" ORDER BY "+SCOLUMN_ID;
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);
			//looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					//TODO check 0 or 1
					sessions.add(cursor.getString(0));
				} while (cursor.moveToNext());
			}
			// closing connection
			cursor.close();
			db.close();
			// returning session json strings
		}catch(Exception e){
			e.printStackTrace();}
		return sessions;

	}

	public List<Integer> getAllSessionsPositions(){
		List<Integer> positions = new ArrayList<Integer>();
		try{
			String selectQuery = "SELECT "+ SCOLUMN_POSITION + " FROM " + TABLE_SESSION +" ORDER BY "+SCOLUMN_ID;
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);
			//looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					//TODO check 0 or 1
					positions.add(cursor.getInt(0));
				} while (cursor.moveToNext());
			}
			// closing connection
			cursor.close();
			db.close();
			// returning session positions
		}catch(Exception e){
			e.printStackTrace();}
		return positions;
	}
	 */
}


