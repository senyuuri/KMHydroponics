package com.dhs.hydroponics;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivityUpload extends Activity{
	private ProgressDialog progressDialog;
	private Boolean ans_finish,img_finish;
	private Integer img_progress,json_size,upload_progress,img_size,upload_item_num;
	private Float bar_now,bar_total;
	private List<String> img_list,json_list;
	private Button btn_upload,btn_back;
	private static final String TAG = "UPLOAD";
	private TextView textView;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_upload);
		btn_upload = (Button) findViewById(R.id.main_upload_btn);
		btn_back = (Button) findViewById(R.id.main_upload_back);
		textView = (TextView) findViewById(R.id.main_upload_text);
		//Upload status
		ans_finish = false;
		img_finish = false;
		upload_progress = 0;
		img_progress = 0;
		img_size = 0;
		img_list = new ArrayList<String>();
		bar_now = 0f;
		bar_total = 0f;
		DBHandler dbHandler = new DBHandler(getApplicationContext());
		upload_item_num = dbHandler.getUploadNum();
		if(upload_item_num == 0){
			btn_upload.setVisibility(View.INVISIBLE);
			textView.setText("All records have been uploaded:)");
		}else{
			textView.setText(String.valueOf(upload_item_num)+" record(s) need to be uploaded.");
		}
		
	}

	public void uploadAll(View view){
		try{
			//Append json generated to upload list
			DBHandler dbHandler = new DBHandler(getApplicationContext());
			//Retrive all from upload list
			SQLiteDatabase db = dbHandler.getReadableDatabase();
			json_list = new ArrayList<String>();
			Cursor cursor = db.rawQuery("SELECT " + DBHandler.UCOLUMN_JSON+" FROM "+ DBHandler.TABLE_UPLOAD, null);
			if (cursor.moveToFirst()) {
				do {
					String tempString = cursor.getString(0).replace("'", "\"");
					// reverse escape '
					tempString = tempString.replace("&#39;", "'");
					Log.d("UpALL", "JSON RECORD:"+tempString);
					json_list.add(tempString);
				}while (cursor.moveToNext());
			}
			json_size = json_list.size();
			
			//Start upload thread for individual json record
			for(int i = 0;i<json_size;i++){
				PreUpload preUpload = new PreUpload();
				preUpload.setJson(json_list.get(i));
				new Thread(preUpload).start();
			}
			
			bar_total += json_size;

			//Set cancel button for progress dialog
			//To prevent infinite loop casued by GAE server error
			progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("Uploading");
			progressDialog.setMessage("Please wait...");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setProgress(0);
			progressDialog.setMessage("Calculating record size...");
			progressDialog.setCancelable(false);
			progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.dismiss();
			    }
			});
			progressDialog.show();
			db.close();
		}catch (Exception e) {
			e.printStackTrace();
		}

	}


	class PreUpload implements Runnable{
		private String _json;

		public void setJson(String json){
			this._json = json;
		}

		@Override
		public void run(){ //Check network status
			ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
			if(connManager.getActiveNetworkInfo()==null){
				Message m = new Message();
				m.what = 4;
				h.sendMessage(m);
			}
			else{
				//If available, start uploading
				//Upload answers in JSON format
				uploadJSON uploadJson = new uploadJSON();
				uploadJson.setJson(this._json);
				new Thread(uploadJson).start();
				
				DBHandler dbHandler = new DBHandler(getApplicationContext());
				//read image path from json, append to img_list
				try{
					//store student info
					JSONObject obj = new JSONObject(this._json);
					JSONArray jarry = obj.getJSONArray("answers");
					for(int i  = 0; i < jarry.length(); i++){
						JSONArray qn = jarry.getJSONArray(i);
						Integer qid = qn.getInt(0);
						//If type is 1, photo taking question
						//retrive file names
						if(dbHandler.getQnType(qid)==1){
							String raw = qn.getString(1);
							String[] raw_ay = raw.split(",");
							String img_path1 = raw_ay[0];
							String img_path2 = raw_ay[1];
							//To get file name from path, for null test
							String[] img_test1 = img_path1.split(File.separator);
							String[] img_test2 = img_path2.split(File.separator);
							Log.d("IMG", "IMG_name:"+img_test1[img_test1.length-1]);
							Log.d("IMG", "IMG_name:"+img_test2[img_test2.length-1]);
							Log.d("IMG", "-----");
							//Add image file name to img_list if not null
							if(!(img_test1[img_test1.length-1].equals("null"))){
								File file = new File(Environment.getExternalStorageDirectory() +  File.separator + img_path1);
								String abspath  = file.getAbsolutePath();
								img_list.add(abspath);
								Log.d("IMG", "IMG_Path:"+abspath);
							}
							if(!(img_test2[img_test2.length-1].equals("null"))){
								File file = new File(Environment.getExternalStorageDirectory() +  File.separator + img_path2);
								String abspath  = file.getAbsolutePath();
								img_list.add(abspath);
								Log.d("IMG", "IMG_Path:"+abspath);
							}
						}
						
					}
					img_size = img_list.size();
					bar_total += img_size;
					progressDialog.setMessage("Record size(including images):"+bar_total.toString());
					Log.d("IMG","IMG_list_size:"+String.valueOf(img_size));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	class uploadJSON implements Runnable{
		private String lineStr,_json;

		public void setJson(String json){
			this._json = json;
		}

		@Override
		public void run() {
			BufferedReader in = null;
			try{
				HttpClient client = new DefaultHttpClient();
				HttpPost request = new HttpPost(Constant.SERVER_UPLOAD_ANS);
				List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				//Add parameter to POST
				postParameters.add(new BasicNameValuePair("answer", this._json));
				UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters);
				request.setEntity(formEntity);
				HttpResponse response = client.execute(request);
				in = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent()));
				lineStr = in.readLine();
				in.close();
				Log.d("Upload", "Response:" +response.getStatusLine().getStatusCode());
				//Check response status
				//If success
				Message msg = new Message();
				if(response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
					msg.what = 0;
				}else{
					msg.what = 2;
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


	class UploadImage implements Runnable{
		private static final String TAG = "uploadFile";   
		private static final int TIME_OUT = 10*10000000; //set timeout   
		private static final String CHARSET = "utf-8"; //set encoding
		public static final String SUCCESS="1"; public static final String FAILURE="0";
		private static final String PREFIX = "--";
		private final String LINE_END =  System.getProperty("line.separator"); 

		private File file;
		private String RequestURL;

		public void setFile(File file){
			this.file = file;
		}

		public void setURL(String URL){
			this.RequestURL = URL;
		}

		@Override
		public void run() {
			String BOUNDARY = UUID.randomUUID().toString(); //Randomly generated boundary, String PREFIX = "--" , LINE_END = "\r\n";   
			String CONTENT_TYPE = "multipart/form-data"; //content type
			try {  
				URL url = new URL(RequestURL);   
				HttpURLConnection conn = (HttpURLConnection) url.openConnection(); conn.setReadTimeout(TIME_OUT); conn.setConnectTimeout(TIME_OUT); 
				conn.setDoInput(true); //allow input stream
				conn.setDoOutput(true); //allow output stream
				conn.setUseCaches(false); //disable cache
				conn.setRequestMethod("POST"); 
				conn.setRequestProperty("Charset", CHARSET);   
				//set encoding
				conn.setRequestProperty("connection", "keep-alive");   
				conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);  
				if(file!=null) {   
					//if file exists
					OutputStream outputSteam=conn.getOutputStream();   
					DataOutputStream dos = new DataOutputStream(outputSteam);   
					StringBuffer sb = new StringBuffer();   
					sb.append(PREFIX);   
					sb.append(BOUNDARY); sb.append(LINE_END);   
					//name = "file"   
					sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""+file.getName()+"\""+LINE_END);  
					sb.append("Content-Type: application/octet-stream; charset="+CHARSET+LINE_END);   
					sb.append(LINE_END);   
					dos.write(sb.toString().getBytes());   
					InputStream is = new FileInputStream(file);
					
					byte[] bytes = new byte[1024];   
					int len = 0;   
					while((len=is.read(bytes))!=-1)   
					{   
						dos.write(bytes, 0, len);   
					}   
					is.close();   
					dos.write(LINE_END.getBytes());   
					byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();   
					dos.write(end_data);   
					dos.flush();  
					//Obtain response code
					int res = conn.getResponseCode();   
					Log.e(TAG, "response code:"+res);   
					if(res==200)   
					{  
						Log.d(TAG, "Image Upload Success!"+file.getAbsolutePath().toString());
						Message m = new Message(); 
						m.what = 1;
						h.sendMessage(m);
					}else{
						Log.d(TAG, "Image Upload Failed!");
						Message m = new Message(); 
						m.what = 3;
						h.sendMessage(m);
					}
				}   
			} catch (MalformedURLException e)   
			{ e.printStackTrace(); }   
			catch (IOException e)   
			{ e.printStackTrace(); }   
		}   

	}


	class DownloadImgURL implements Runnable{
		private String _img_path;

		public void setImagePath(String path){
			this._img_path = path; 
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			BufferedReader in = null;
			try{
				HttpClient client = new DefaultHttpClient();
				Log.d("UPLOAD","Download from:"+ Constant.SERVER_UPLOAD_IMG);
				HttpGet request = new HttpGet(Constant.SERVER_UPLOAD_IMG);
				HttpResponse response = client.execute(request);
				Message msg = new Message();
				if(response.getStatusLine().getStatusCode()== HttpStatus.SC_OK){
					HttpEntity resEntity = response.getEntity();
					String upload_url = EntityUtils.toString(resEntity);
					Log.d("UPLOAD","IMG_UPLOAD_URL:"+upload_url);
					//Start uploading process
					/*
					uploadThread upload = new uploadThread();
					upload.setImagePath(this._img_path);
					upload.setImageUploadURL(upload_url);
					new Thread(upload).start();
					 */
					UploadImage upImg = new UploadImage();
					File file = new File(this._img_path);
					upImg.setFile(file);
					upImg.setURL(upload_url);
					new Thread(upImg).start();
					Log.d("UPLOAD","Start uploading image "+this._img_path);

				}
			}catch (Exception e){
				e.printStackTrace();
			}

		}

	}

	android.os.Handler h = new android.os.Handler(){
		public void handleMessage (Message msg)
		{
			final DBHandler dbHandler = new DBHandler(getApplicationContext());
			SQLiteDatabase db = dbHandler.getWritableDatabase();
			switch(msg.what)
			{
			//Upload json answers finished
			case 0:
				bar_now+=1;
				Float r = bar_now/bar_total*100;
				progressDialog.setProgress(Math.round(r));
				//Mark upload status as finished
				upload_progress +=1;
				if(upload_progress==json_size){
					ans_finish = true;
				}
				//If upload finished,start uploading image
				if(ans_finish){
					//if no image needed to be uploaded
					if(img_size==0){
						img_finish = true;
					}else{
						Log.d("UPLOAD", "====Start uploading images====");
						Log.d("IMG","Progress:"+String.valueOf(img_progress)+"/"+String.valueOf(img_size));
						DownloadImgURL dlURL = new DownloadImgURL();
						dlURL.setImagePath(img_list.get(img_progress));
						new Thread(dlURL).start();
					}
				
				}
				//If upload finished
				if(ans_finish && img_finish){
					//clear upload list
					Log.d("UPLOAD","Clear upload list");
					dbHandler.clearUploadList();
					progressDialog.dismiss();
					Log.d("UPLOAD","UPLOAD FINISH.");
					//Toast.makeText(getApplicationContext(), "Upload finished! Thank you:)", Toast.LENGTH_LONG).show();
					//Show dialog window
					Builder builder = new AlertDialog.Builder(MainActivityUpload.this);
					builder.setMessage("Upload success:)");
					builder.setCancelable(true);
					builder.setPositiveButton("Start New Lesson", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							//clear answers
							dbHandler.clearAnswers();
							Intent intent = new Intent(MainActivityUpload.this, MainActivity.class);
							startActivity(intent);

						}
					});
					builder.setNegativeButton("Exit", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							//clear answers
							dbHandler.clearAnswers();
							Intent intent = new Intent(Intent.ACTION_MAIN);
							intent.addCategory(Intent.CATEGORY_HOME);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					});
					AlertDialog dialog = builder.create();
					dialog.show();
					//Clear image list
					img_list = new ArrayList<String>();
					
					btn_upload.setVisibility(View.INVISIBLE);
					textView.setText("All records have been uploaded:)");

				}
				db.close();
				break;

			//Upload image finished
			case 1:
				img_progress += 1;
				bar_now+=1;
				Float r2 = bar_now/bar_total*100;
				progressDialog.setProgress(Math.round(r2));
				Log.d("IMG","Progress:"+String.valueOf(img_progress)+"/"+String.valueOf(img_size));
				if(img_progress == img_size){
					//All images have been uploaded
					//Mark upload status as finished
					img_finish = true;
				}else{
					DownloadImgURL dlURL = new DownloadImgURL();
					dlURL.setImagePath(img_list.get(img_progress));
					new Thread(dlURL).start();
				}
				//If upload finished
				if(ans_finish && img_finish){
					//clear upload list
					Log.d("UPLOAD","Clear upload list");
					dbHandler.clearUploadList();
					progressDialog.dismiss();
					Log.d("UPLOAD","UPLOAD FINISH.");
					//Toast.makeText(getApplicationContext(), "Upload finished! Thank you:)", Toast.LENGTH_LONG).show();
					//Show dialog window
					Builder builder = new AlertDialog.Builder(MainActivityUpload.this);
					builder.setMessage("Upload success:)");
					builder.setCancelable(true);
					builder.setPositiveButton("Start New Lesson", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							//clear answers
							dbHandler.clearAnswers();
							Intent intent = new Intent(MainActivityUpload.this, MainActivity.class);
							startActivity(intent);

						}
					});
					builder.setNegativeButton("Exit", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							//clear answers
							dbHandler.clearAnswers();
							Intent intent = new Intent(Intent.ACTION_MAIN);
							intent.addCategory(Intent.CATEGORY_HOME);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					});
					AlertDialog dialog = builder.create();
					dialog.show();
					//Clear image list
					img_list = new ArrayList<String>();
					
					btn_upload.setVisibility(View.INVISIBLE);
					textView.setText("All records have been uploaded:)");
				}
				db.close();
				break;

				//Error occured when uploading json
			case 2:
				progressDialog.dismiss();
				//reset upload status
				ans_finish = false;
				img_finish = false;
				Builder builder = new AlertDialog.Builder(MainActivityUpload.this);
				builder.setMessage("Error occured when upload answers. Please try again:(");
				builder.setCancelable(true);
				builder.setNegativeButton("Cancel", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
				//Clear image list
				img_list = new ArrayList<String>();
				
				break;
				
				//Error occured when uploading photos
			case 3:
				progressDialog.dismiss();
				//reset upload status
				ans_finish = false;
				img_finish = false;
				Builder builder2 = new AlertDialog.Builder(MainActivityUpload.this);
				builder2.setMessage("Error occured when upload images. Please try again:(");
				builder2.setCancelable(true);
				builder2.setNegativeButton("Cancel", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				AlertDialog dialog2 = builder2.create();
				dialog2.show();
				//Clear image list
				img_list = new ArrayList<String>();
				
				break;
			
			//network error
			case 4:
				progressDialog.dismiss();
				Builder builder3 = new AlertDialog.Builder(MainActivityUpload.this);
				builder3.setMessage("Network not available.");
				builder3.setCancelable(true);
				builder3.setPositiveButton("Setting", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//TODO show wireless setting
						dialog.dismiss();
					}
				});
				builder3.setNegativeButton("Cancel", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				AlertDialog dialog3 = builder3.create();
				dialog3.show();
				//Clear image list
				img_list = new ArrayList<String>();
				
				break;

			}

		}
	};
	
	public void back(View view){
		finish();
	}

}
