package com.dhs.hydroponics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import com.dhs.hydroponics.R;
import com.dhs.hydroponics.Upload.DownloadImgURL;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Printer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = "SDTrial";
	private Button bt_test,bt_update,bt_start;
	private String raw_namelist,raw_qnset = "";
	private String[] arrary_namelist, array_qnset;
	private String dn_result_0,dn_result_1,dn_result_2;
	private String namelist_ver="";
	private String qnset_ver="";
	private Boolean namelist_state,qnset_state,image_state;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//bt_test = (Button) findViewById(R.id.bt_test);
		bt_start = (Button) findViewById(R.id.bt_start);
		bt_update = (Button) findViewById(R.id.bt_update);
		namelist_state = false;
		qnset_state = false;
		image_state = true;

		//IF local namelist or questionset not found, disable next step button
		File databaseFile = getApplicationContext().getDatabasePath("database.db");
		if(!databaseFile.exists()){
			//TODO 
			bt_start.setEnabled(false);
			Toast.makeText(this, "Database not found. Please update first.",Toast.LENGTH_LONG).show();
		}else{
			//check database table

			//*Active 'next step' button 
		}	



	}

	public void updateCheck(View view){
		//Set cancel button for progress dialog
		//To prevent infinite loop casued by GAE server error
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("Updating");
		progressDialog.setMessage("Please wait...");
		progressDialog.setCancelable(false);
		/*
		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		*/
		progressDialog.show();
		//Disactive 'next step' button
		bt_start.setEnabled(false);
		//check network status
		ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connManager.getActiveNetworkInfo()==null){
			progressDialog.dismiss();
			Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Network not available.");
			builder.setCancelable(true);
			builder.setPositiveButton("Setting", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//TODO show wireless setting
					dialog.dismiss();
				}
			});
			builder.setNegativeButton("Cancel", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();}

		else{
			//TODO
			//Check update(obtain timestamp from server)
			//& download new files if mismatched
			Download dn_timestamp = new Download();
			dn_timestamp.setTarget(0);
			new Thread(dn_timestamp).start();

		}
		//TODO:in case of first time running
		bt_start.setEnabled(true);
	}

	//For resume button
	public void resume(View view){
		Intent intent= new Intent(this,Resume.class);
		startActivity(intent);
	}



	public String md5Checking(String input) {
		try {
			MessageDigest messageDigest =MessageDigest.getInstance("MD5");
			byte[] inputByteArray = input.getBytes();
			messageDigest.update(inputByteArray);
			byte[] resultByteArray = messageDigest.digest();
			return byteArrayToHex(resultByteArray);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	public static String byteArrayToHex(byte[] byteArray) {
		char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9', 'A','B','C','D','E','F' };
		char[] resultCharArray =new char[byteArray.length * 2];
		int index = 0;
		for (byte b : byteArray) {
			resultCharArray[index++] = hexDigits[b>>> 4 & 0xf];
			resultCharArray[index++] = hexDigits[b& 0xf];
		}
		return new String(resultCharArray);
	}	

	public void nextStep(View view){
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		if(dbHandler.getNamelistNum()==0 || dbHandler.getQnSetNum()==0){
			Toast.makeText(this, "Database not found. Please update first.",Toast.LENGTH_LONG).show();
		}else{
			Intent intent = new Intent(getApplicationContext(),InfoSelection.class);
			startActivity(intent);
		}
	}


	

	class Download implements Runnable{
		/*Target url on GAE server
		 * 0: timestamp
		 * 1: namelist
		 * 2: question set
		 * 3:
		 * Refer to Constant class
		 */
		private Integer _target;

		public void setTarget(int target){
			this._target = target;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			BufferedReader in = null;
			try{
				HttpClient client = new DefaultHttpClient();
				String urlnew = "";
				if(this._target==0){
					urlnew = Constant.SERVER_VERSION;
				}else if(this._target==1){
					urlnew = Constant.SERVER_NAMELIST;
				}else if(this._target==2){
					urlnew = Constant.SERVER_QNSET;
				}
				Log.d(TAG,"Download from:"+ urlnew);
				HttpGet request = new HttpGet(urlnew);
				HttpResponse response = client.execute(request);
				Message msg = new Message();
				if(response.getStatusLine().getStatusCode()== HttpStatus.SC_OK){
					HttpEntity resEntity = response.getEntity();
					switch (this._target) {
					case 0:
						dn_result_0 = EntityUtils.toString(resEntity);
						Log.d(TAG, "Download success: " +dn_result_0);
						msg.what = 0;
						break;
					case 1:
						dn_result_1 = EntityUtils.toString(resEntity);
						Log.d(TAG, "Download success: " +dn_result_1);
						msg.what = 1;
						break;
					case 2:
						dn_result_2 = EntityUtils.toString(resEntity);
						msg.what = 2;
						Log.d(TAG, "Download success: " +dn_result_2);
					}
				}

				h.sendMessage(msg);

			}catch (Exception e){Log.d(TAG,"Expection:"+ e.toString());
			}
			finally{
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						Log.d(TAG,"Expection:"+ e.toString());
						e.printStackTrace();
					}
				}
			}

		}
	}

	android.os.Handler h = new android.os.Handler(){
		public void handleMessage (Message msg)
		{
			DBHandler dbHandler = new DBHandler(getApplicationContext());
			SQLiteDatabase db = dbHandler.getWritableDatabase();
			switch(msg.what)
			{
			//timestamp received, check with local 
			case 0:
				//Parse Json
				try{
					JSONObject obj = new JSONObject(dn_result_0);
					namelist_ver = obj.getString("namelist");
					Log.d(TAG, "SERVER NAMELIST:"+namelist_ver);
					Log.d(TAG, "LOCAL:"+dbHandler.getNamelistVer());
					qnset_ver = obj.getString("question");
					Log.d(TAG, "SERVER QNSET:"+qnset_ver);
					Log.d(TAG, "LOCAL:"+dbHandler.getQnsetVer());
				}catch(JSONException e){
					e.printStackTrace();
				}


				//compare with local version, download new if not match
				if(!namelist_ver.equals(dbHandler.getNamelistVer())){
					Download dn_namelist = new Download();
					dn_namelist.setTarget(1);
					new Thread(dn_namelist).start();
				}else if(!qnset_ver.equals(dbHandler.getQnsetVer())){
					Download dn_qnset = new Download();
					dn_qnset.setTarget(2);
					new Thread(dn_qnset).start();

					namelist_state = true;
				}else{
					Toast.makeText(getApplicationContext(), "Current database is the latest!", Toast.LENGTH_LONG).show();
					namelist_state = true;
					qnset_state = true;
					image_state = true;
				}

				if(namelist_state && qnset_state && image_state){
					dbHandler.clearSessions();
					dbHandler.clearUploadList();
					progressDialog.dismiss();
					 bt_start.setEnabled(true);
				}
				db.close();
				break;


				//namelist received, parse json
			case 1:
				//Drop old table
				db.execSQL("DROP TABLE IF EXISTS " + DBHandler.TABLE_NAMELIST);
				db.execSQL(DBHandler.CREATE_TABLE_NAMELIST);
				try{
					//store student info
					JSONObject obj = new JSONObject(dn_result_1);
					JSONArray jarry = obj.getJSONArray("class");
					for(int i  = 0; i < jarry.length(); i++){
						JSONObject jclass = jarry.getJSONObject(i);
						String class_name = jclass.getString("class_name");
						JSONArray name_list = jclass.getJSONArray("name_list");
						for(int j = 0; j< name_list.length();j++){
							dbHandler.insertName(class_name, name_list.getString(j));
						}
					}
					//update namelist version
					//reopen database, since it is closed in previous insertName function
					db = dbHandler.getWritableDatabase();
					db.execSQL("UPDATE "+DBHandler.TABLE_PREF+" SET "+DBHandler.PCOLUMN_VALUE+"=\""+namelist_ver+
							"\" WHERE "+ DBHandler.PCOLUMN_KEY +"=\"name_date\"");
					db.close();
					Toast.makeText(getApplicationContext(), "Class list update success!", Toast.LENGTH_LONG).show();

					//Continue comparing with question set local version, download new if not match
					if(qnset_ver != dbHandler.getQnsetVer()){
						Download dn_qnset = new Download();
						dn_qnset.setTarget(2);
						new Thread(dn_qnset).start();
					}else {
						qnset_state = true;
						image_state = true;
					}

				}catch(JSONException e){
					e.printStackTrace();
				}
				namelist_state = true;
				if(namelist_state && qnset_state && image_state){
					dbHandler.clearSessions();
					dbHandler.clearUploadList();
					progressDialog.dismiss();
					bt_start.setEnabled(true);
				}
				db.close();
				break;

				//question set received, parse json
			case 2:
				//Drop old table
				db.execSQL("DROP TABLE IF EXISTS " + DBHandler.TABLE_QNSET);
				db.execSQL(DBHandler.CREATE_TABLE_QNSET);
				try{
					//store questions
					JSONObject obj = new JSONObject(dn_result_2);
					for(Integer i  = 1; i <= 4; i++){
						JSONArray station_set = obj.getJSONArray(i.toString());
						Log.d(TAG, i.toString()+station_set.toString());
						for(int j = 0; j< station_set.length();j++){
							JSONObject qn  = station_set.getJSONObject(j);

							dbHandler.insertQuestion(qn.getInt("number"),i,qn.getInt("type"),qn.getString("question"),qn.getString("options"),qn.getString("answer"),qn.getString("title"));
						}
					}
					//update namelist version
					db = dbHandler.getWritableDatabase();
					db.execSQL("UPDATE "+DBHandler.TABLE_PREF+" SET "+DBHandler.PCOLUMN_VALUE+"=\""+qnset_ver+
							"\" WHERE "+ DBHandler.PCOLUMN_KEY +"=\"qn_date\"");
					db.close();
					Toast.makeText(getApplicationContext(), "Question set update success!", Toast.LENGTH_LONG).show();
					qnset_state = true;

				}catch(Exception e){
					e.printStackTrace();
				}
				if(namelist_state && qnset_state && image_state){
					dbHandler.clearSessions();
					dbHandler.clearUploadList();
					progressDialog.dismiss();
				}
				db.close();
				break;

			
			}
		}
	};

	public void uploadAll(View view){
		Intent intent = new Intent(this,MainActivityUpload.class);
		startActivity(intent);
	}
}
