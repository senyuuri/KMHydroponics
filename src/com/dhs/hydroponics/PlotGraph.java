package com.dhs.hydroponics;

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
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PlotGraph extends Activity{
	private Button btn_prev;
	private String jString;
	private WebView webView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graph);
		btn_prev = (Button) findViewById(R.id.plot_prev);
		webView = (WebView) this.findViewById(R.id.plot_webview); 
		Bundle bundle = getIntent().getExtras();
		jString = bundle.getString("json");
		Log.d("TEST","jsonReceived"+jString);
		//load console message to logcat
		webView.setWebChromeClient(new WebChromeClient() {
			  public void onConsoleMessage(String message, int lineNumber, String sourceID) {
			    Log.d("MyApplication", message + " -- From line "
			                         + lineNumber + " of "
			                         + sourceID);
			  }
			});
		//allow javascript
		webView.getSettings().setJavaScriptEnabled(true);
		//set scalable
		webView.getSettings().setBuiltInZoomControls(true); 
		webView.getSettings().setDomStorageEnabled(true);
		//allow javascript to access android activity
		webView.addJavascriptInterface(this,"PlotGraph");  
		webView.loadUrl("file:///android_asset/chart.html");  
		}
	
	@JavascriptInterface
	public String getJson(){
		return jString;
	}
	
	
	public void nextStep(View view){
		finish();
	}

}
