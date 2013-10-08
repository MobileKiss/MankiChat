package by.mobilekiss.mankichat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import by.mobilekiss.mankichat.OverscrolledListView.OnItemDoubleTapLister;

public class MessagesForm extends Activity implements OnItemDoubleTapLister {
	

    
	OverscrolledListView lvMessages;
	TextView tvHeader;
	String SenderName = "";
	MessangerAdapter adapter = null;
	ArrayList<String> AllMessagesArr = new ArrayList<String>();
	DBHelper dbHelper;
	SQLiteDatabase db = null;
	SharedPreferences PInfo;
	Timer myTimer = new Timer();
	ImageView backToCamera, btnInfo;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.messages_form);
		
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = 
			        new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			}

		Display display = getWindowManager().getDefaultDisplay(); 
		int width = display.getWidth();  // deprecated
		int height = display.getHeight();  // deprecated
		
		PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);
		
		tvHeader = (TextView) findViewById(R.id.message_form_tvbanner);
		tvHeader.setTypeface(FontFactory.getUbuntuBold(this));
		
		lvMessages = (OverscrolledListView) findViewById(R.id.lvMessages);
		
		backToCamera = (ImageView) findViewById(R.id.messages_form_backToCamera);
		//backToCamera.getLayoutParams().height = Math.round(height/13);
		//backToCamera.getLayoutParams().width = Math.round(width/8);
					
		lvMessages.showView = getLayoutInflater().inflate(R.layout.list_header, null);
		lvMessages.form = this;
		lvMessages.setOnItemDoubleClickListener(this);
		
        //View headView = getLayoutInflater().inflate(R.layout.list_header, null);
        //lvMessages.addHeaderView(headView, null, false);        
		 
        GetAllMessagesList();
        adapter = new MessangerAdapter(this, AllMessagesArr);        
        adapter.setNotifyOnChange(false);
        lvMessages.setAdapter(adapter);

                
        
        myTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				runOnUiThread(timerTick);							
			}
		}, 0, 60000);
	}
	
	private Runnable timerTick = new Runnable() {
		@Override
		public void run() {
			InvokeGetNewMessages();			
		}
	};
		
	public class UpdateTask extends AsyncTask<Void, Void, JSONObject>
	{
		private String error;
		private MessagesForm frm;
		
		@Override
		protected JSONObject doInBackground(Void... arg0) {
	    	SharedPreferences PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);
			try{
			    HttpClient client = new DefaultHttpClient();
			    HttpPost post = new HttpPost("http://www.mankichat.ru/gate/getUpdateList/");
			      
			    List pairs = new ArrayList();
			    	     	 
			    pairs.add(new BasicNameValuePair("api_key_access", PInfo.getString("api_key_access", "")));
			    pairs.add(new BasicNameValuePair("page", "0"));
			    pairs.add(new BasicNameValuePair("start_id_dialog", PInfo.getString("start_id_dialog", "0")));
			    String str =  PInfo.getString("start_id_dialog", "0");
			    post.setEntity(new UrlEncodedFormEntity(pairs));           
	       
			    HttpResponse response = client.execute(post);
			      
			    pairs.clear();
			      
			    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
			    String line = reader.readLine();
			      
			    JSONObject jo = new JSONObject(line);
			    return jo;
			    
	      } catch (org.apache.http.client.ClientProtocolException e) {
             error = "Ошибка обмена";
             return null;
	      } catch (IOException e) {  
	    	 error = "Пожалуйста подключитесь к сети интернет";
	    	 return null;
	      } catch (Exception e) {
	    	  return null;
	      }                                        
		}
		
		@Override
		protected void onPostExecute(JSONObject o)
		{
			boolean res = false;
			if (o==null)
			{
		    	 ToastOnce.makeText(frm, error, Toast.LENGTH_SHORT);
		    	 return;
			}
			else
				res = frm.GetNewMessages(o);
			frm.lvMessages.hideUpdater();
			if (res)
			{
				Toast.makeText(frm, "События обновлены", Toast.LENGTH_SHORT).show();
				GetAllMessagesList();
				adapter.notifyDataSetChanged();
			}
		}
		
		public UpdateTask(MessagesForm frm)
		{
			this.frm = frm;
		}
	}
	
	private UpdateTask asyncTask = null;
	public void InvokeGetNewMessages()
	{
		if ((asyncTask==null)||(asyncTask.getStatus()==AsyncTask.Status.FINISHED))
		{
			asyncTask = new UpdateTask(this);
			asyncTask.execute();
		}
	}
	
	 
	 public void onClickPhoto(View v) {
	      //
		 finish();
		 //
	    }
	 
	 public void onClickInfo(View v) {
	      //
		 Toast.makeText(this, "Чтоб просмотреть сообщение нажмите и удерживайте палец на строке.", Toast.LENGTH_LONG).show();
		 //
	    }
	  
	
	public void GetAllMessagesList(){
		
		dbHelper = new DBHelper(this);		
        db = dbHelper.getReadableDatabase(); 
				
		AllMessagesArr.clear();
		
		Cursor c = db.rawQuery("SELECT * FROM messages_table  ORDER BY id DESC LIMIT 50", null);
		
		int i = c.getCount();
		int from_id = c.getColumnIndex("from_id");
		int to_id = c.getColumnIndex("to_id");
	    int is_view = c.getColumnIndex("is_view");
	    int from_login = c.getColumnIndex("from_login");
	    int datesend = c.getColumnIndex("datesend");
	    
	    int was_send = c.getColumnIndex("was_send");
	    
	    if (c.isAfterLast())
	    	return;
	    
			 c.moveToFirst();
	    	 String isViewwed = "";
	    	 String StringToAdd = "";

	    String h = PInfo.getString("user_id", "");
	    int hhh = 0;
    	try
    	{
    		hhh = Integer.valueOf(h);
    	}
    	catch (Exception e)
    	{}
	    	 
	    	 
	      do {
	    	  int strr = c.getInt(is_view);
	    	  String isViewed = "";
	    	  int cur_from_id = c.getInt(from_id);
	    	  int cur_to_id = c.getInt(to_id);
	    	  String cur_from_login = c.getString(from_login);
	    	  String cur_datesend = c.getString(datesend);
	    	  String dateTime = "";
	    	  Date tempDate = null;
	    	  SimpleDateFormat StandartFormat = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
	    	  SimpleDateFormat DesiredFormat = new SimpleDateFormat("dd.MM HH:mm");
	    	  try {
				tempDate = StandartFormat.parse(cur_datesend);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	  dateTime = DesiredFormat.format(tempDate);
	    	  
	    	  if ( cur_from_id != hhh || cur_from_id == cur_to_id ){	    		  
	    		  
	    		  if (strr == 0){
	    		  
		    		  isViewwed = " - новое сообщение";
		    		  StringToAdd = cur_from_login + isViewwed + "\n" + dateTime;
		    		  
		    	  }
		    	  
		    	  if (strr == 1 || strr == 2) {
		    		  
		    		  isViewwed = " - просмотрено";
		    		  StringToAdd = cur_from_login + isViewwed + "\n" + dateTime + "  нажмите 2 раза для ответа";
		    	  
		    	  }
	    	  }else{ 
	    		  
	    		  if (strr == 0)
	    			  isViewed = "не открыто";
	    		  else
	    			  isViewed = "открыто";
	    			  
	    		  
	    		  StringToAdd =  "отправлено - " + c.getString(c.getColumnIndex("to_login")) + "\n" + dateTime /*+ "  " + isViewed*/;
	    	  }
	    	AllMessagesArr.add(StringToAdd);  
	      } while (c.moveToNext());
     
	    c.close();
	    db.close();

	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		GetAllMessagesList();
		adapter.notifyDataSetChanged();
		saveInfo("double_click_selId", "");
		
	}
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		myTimer.cancel();		
	}
	
	@Override
	public void onPause(){
		super.onPause();
		myTimer.cancel();
	}
	
	@Override
	public void onStop(){
		super.onStop();
		ToastOnce.reset();
		myTimer.cancel();		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == RESULT_OK ){
	    	
	    	GetAllMessagesList(); 
	    	adapter.notifyDataSetChanged();
	    	
	    }
	    
	    else if (resultCode == RESULT_CANCELED){
	    	ToastOnce.reset();
	    	ToastOnce.makeText(this, "Пожалуйста подключитесь к сети интернет", Toast.LENGTH_SHORT);
	    }
	    
	}
	 
	 public boolean GetNewMessages(JSONObject jo){
		 try {
		     if (jo.get("status").toString().contentEquals("ok")){
		    	 
		    	 saveInfo("triangle",  "0");
		    	 dbHelper = new DBHelper(this);		
			     db = dbHelper.getWritableDatabase();
		    	 int Count = Integer.parseInt(jo.get("count").toString());			    	 
		    	 
		    	 
			     JSONArray messagesArr = jo.getJSONArray("meta");
			     for (int i = 0; i < Count; i++){
			    	 JSONObject message = messagesArr.getJSONObject(i);
			        	  
			    	 String id_dialog = message.getString("id_dialog"); 
			         String photo_url = message.getString("photo_url");			         
			         String from_id = message.getString("from_id");
			         String from_login = message.getString("from_login");
			         String is_view = message.getString("is_view");
			         String to_id = message.getString("to_id");
			         String time_live = message.getString("time_live");
			         String to_login = message.getString("to_login");
			         String datesend = "";
			         String was_send = "";
			         if (message.has("datesend"))
			        	 datesend = message.getString("datesend");
			         
			         if (message.has("was_send"))
			        	 was_send = message.getString("was_send");
			         
			        			                	
			         ContentValues  cv = new ContentValues ();
			         if (!datesend.isEmpty())
			        	 cv.put("datesend", datesend);
			         
			         if (!was_send.isEmpty())
			        	 cv.put("was_send", was_send);
			         
			         cv.put("id_dialog", id_dialog);
			         cv.put("photo_url", photo_url);
			         cv.put("from_id", from_id);
			         cv.put("from_login", from_login);
			         cv.put("is_view", is_view);
			         cv.put("to_id", to_id);
			         cv.put("time_live", time_live);
			         cv.put("to_login", to_login);
			             
			         db.insert("messages_table", null, cv);
			         saveInfo("start_id_dialog", id_dialog);
			         cv.clear();	          		             
		          
			      }   
	 		     db.close();
	 		     return true;
		      } 
			     
			  if (jo.get("status").toString().contentEquals("no")){
						 					 
						 if (jo.get("code").toString().contentEquals("1") || jo.get("code").toString().contentEquals("3")){
							 
							 Toast.makeText(this, "Пожалуйста, войдите в приложение еще раз", Toast.LENGTH_SHORT).show();
							 Intent intent = new Intent(this, LoginingForm.class);
							 startActivity(intent);
							 
						 }else{
					    	  ToastOnce.makeText(this, "Сервер занят попробуйте позже", Toast.LENGTH_SHORT);
						 }
						 return false;
			  }
			  GetAllMessagesList();
		 }
		 catch (JSONException e)
		 {
	    	 ToastOnce.makeText(this, "Неверный ответ от сервера", Toast.LENGTH_SHORT);
	    	 return false;
		 }
		 return false;
	}

	    public void saveInfo(String FieldName, String FieldValue) {
	    	SharedPreferences PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);
		    PInfo.edit().putString(FieldName, FieldValue).commit();	
		    PInfo = null;
		    
		}


		
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}


		
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			return false;
		}


		
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}


		
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			return false;
		}


		
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}


		
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}


		@Override
		public void OnDoubleTap(AdapterView<?> parent, View view, int position,
				long id) {
			dbHelper = new DBHelper(this);		
	        db = dbHelper.getReadableDatabase(); 
			
			Cursor c = db.rawQuery("SELECT * FROM messages_table  ORDER BY id DESC LIMIT 50", null);
			c.moveToPosition(position);
			String a = c.getString(c.getColumnIndex("is_view"));
			String b = c.getString(c.getColumnIndex("from_id"));
			String ca = PInfo.getString("user_id", "");
			if ((c.getString(c.getColumnIndex("is_view")).contentEquals("1") || c.getString(c.getColumnIndex("is_view")).contentEquals("2")) && (c.getString(c.getColumnIndex("from_id")).contentEquals(PInfo.getString("user_id", "")) == false || c.getString(c.getColumnIndex("from_id")).contentEquals(c.getString(c.getColumnIndex("to_id"))))){
							
				saveInfo("double_click_selId", b);
				finish();
				
			}
			c.close();
			db.close();
			
			
		}


		@Override
		public void OnSingleTap(AdapterView<?> parent, View view, int position,
				long id) {
			dbHelper = new DBHelper(this);		
	        db = dbHelper.getReadableDatabase(); 
			
			Cursor c = db.rawQuery("SELECT * FROM messages_table  ORDER BY id DESC LIMIT 50", null);
			c.moveToPosition(position);
			String a = c.getString(c.getColumnIndex("is_view"));
			String b = c.getString(c.getColumnIndex("from_id"));
			String ca = PInfo.getString("user_id", "");
			
			
			
			if (c.getString(c.getColumnIndex("is_view")).contentEquals("0") && (c.getString(c.getColumnIndex("from_id")).contentEquals(PInfo.getString("user_id", "")) == false || c.getString(c.getColumnIndex("from_id")).contentEquals(c.getString(c.getColumnIndex("to_id"))))){
				//String stn = c.getString(c.getColumnIndex("time_live"));
				Intent intent = new Intent(this, WebViewer.class);
				intent.putExtra("pos", Integer.toString(position));
				intent.putExtra("URL", c.getString(c.getColumnIndex("photo_url")));
				intent.putExtra("time_live", c.getString(c.getColumnIndex("time_live")));
				startActivityForResult(intent, 4);
				
			}
			c.close();
			db.close();
			
		}
}



class ToastOnce {
	private static String last = "";

	public static void reset(){
		last = "";
	}
	
	public static void makeText(Context context, CharSequence text, int duration) {
		if (!last.contentEquals(text))
			Toast.makeText(context, text, duration).show();
		last = (String)text;
	}
}
