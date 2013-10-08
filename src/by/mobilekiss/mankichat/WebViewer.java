package by.mobilekiss.mankichat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebViewer extends Activity {
    private WebView webview;
    ProgressDialog progressDialog;
    DBHelper dbHelper;
	SQLiteDatabase db = null;
	String Position = "";
	int LiveTime = 5;
	boolean TimeOut = false;
    

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_OK);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.web_wiew);
        
        if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = 
			        new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			}
        
        progressDialog = new ProgressDialog(WebViewer.this);
        progressDialog.setMessage("Loading ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        webview = (WebView)findViewById(R.id.wvPicture);       
        
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        
        //webview.getSettings().setUseWideViewPort(true);
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        //webview.setScrollbarFadingEnabled(false);
        
        Intent intent = getIntent();
        String URL = intent.getStringExtra("URL");
        Position = intent.getStringExtra("pos");
        LiveTime = Integer.parseInt(intent.getStringExtra("time_live"));
        
        
        webview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
            	super.onPageFinished(view, url);
            	progressDialog.cancel();
	   	    	 new Thread(new Runnable() {
				      public void run() {
				        try {
				        	TimeUnit.SECONDS.sleep(LiveTime);
				        	setResult(RESULT_OK);
				        	finish();
				        	TimeOut = true;
				        } catch (InterruptedException e) {
				        		e.printStackTrace();
				        		finish();
				        }			       
				      }
				    }).start();
	            	SendConfirmation();
            }

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				setResult(RESULT_CANCELED);	
				finish();
			}
        });
        
        webview.loadUrl(URL);
        
    }
    
    public void onResume(){
    	super.onResume();
    	
    	TimeOut = false;
    	
    }
    
    public void onBackPressed(){
    	super.onBackPressed();
    	
    	SendConfirmation();
    	setResult(RESULT_OK);
    	finish();    	
    	
    }
    
    public void SendConfirmation(){
    	
		try{
			SharedPreferences PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);
		
		    HttpClient client = new DefaultHttpClient();
		    HttpPost post = new HttpPost("http://www.mankichat.ru/gate/setPhotoView/");
		    
		    /*Intent intent = getIntent();
		    String getpos = intent.getStringExtra("position");*/
        	int position = Integer.parseInt(Position);
        	 
        	dbHelper = new DBHelper(this);		
            db = dbHelper.getWritableDatabase();
        	
		    Cursor c = db.rawQuery("SELECT * FROM messages_table  ORDER BY id DESC LIMIT 50", null);
			c.moveToPosition(position);	
			
			String messageId = c.getString(c.getColumnIndex("id"));
		
		    		
		    List pairs = new ArrayList();    	 
		    pairs.add(new BasicNameValuePair("api_key_access", PInfo.getString("api_key_access", "")));
		    pairs.add(new BasicNameValuePair("id_dialog", c.getString(c.getColumnIndex("id_dialog"))));
      
		    post.setEntity(new UrlEncodedFormEntity(pairs));           
       
	   // HttpResponse
	      HttpResponse response = client.execute(post);
	      
	      pairs.clear();
	      
	      BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
	      
	      String line = reader.readLine();
	      
	      JSONObject jo = new JSONObject(line);
	     if (jo.get("status").toString().contentEquals("ok")){
	    	 
	    	 ContentValues cv = new ContentValues();	    	 
	    	 cv.put("is_view", "1");	    	 
	    	 db.update("messages_table", cv, "id=" + messageId, null );
	    	 c.close();
	    	 db.close();
	     }
	     
	     
	      
	     if (jo.get("status").toString().contentEquals("no")){
	    	 
	    	 if (jo.get("code").toString().contentEquals("1") || jo.get("code").toString().contentEquals("3")){
				 
		    		Toast.makeText(this, "Пожалуйста, войдите в приложение еще раз", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(this, LoginingForm.class);
					startActivity(intent);
					 
			 }else	    	 
				 Toast.makeText(this, "Сервер занят попробуйте позже", Toast.LENGTH_SHORT).show();
	     }
	     
            
        
      } catch (org.apache.http.client.ClientProtocolException e) {
              
      } catch (IOException e) {  
    	  ToastOnce.makeText(this, "Пожалуйста подключитесь к сети интернет", Toast.LENGTH_SHORT);
      
      } catch (Exception e) {
    	  Toast.makeText(this, "UPS Huston we got problem", Toast.LENGTH_SHORT).show();
              
      }                                        
}
	
}
