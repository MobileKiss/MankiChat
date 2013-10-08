package by.mobilekiss.mankichat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class PostPhoto extends Activity implements OnClickListener{
		
	EditText etFindFriendsByLogin;
	ImageView btnPost, btnFindFriendsByLogin, back;
	SharedPreferences PInfo;
	String SelectedFriends = "";
	DBHelper dbHelper;
	ListView lvFriendsSelection;
	SQLiteDatabase db = null;
	ProgressDialog progressDialog;
	PostAdapter adapter = null;
	TextView bannerName;
	ArrayList<String> AllUIdArr = new ArrayList<String>();	
	
	ArrayList<String> AllContactsArr = new ArrayList<String>();
	ArrayList<Integer> selItems = new ArrayList<Integer>();
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.postphoto);
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
.permitAll().build();
			StrictMode.setThreadPolicy(policy);
			}
		
		/*Display display = getWindowManager().getDefaultDisplay(); 
		int width = display.getWidth();  // deprecated
		int height = display.getHeight();  // deprecated
		*/
		btnFindFriendsByLogin = (ImageView) findViewById(R.id.Post_form_btnFindFriendsByLogin);
		//btnFindFriendsByLogin.getLayoutParams().height = Math.round(height/14);
		//btnFindFriendsByLogin.getLayoutParams().width = Math.round(width/9);
		btnFindFriendsByLogin.setOnClickListener(this);
		
		etFindFriendsByLogin = (EditText) findViewById(R.id.Post_form_etFindFriendsByLogin);
		etFindFriendsByLogin.setTypeface(FontFactory.getUbuntuBold(this));
		
		
		btnPost = (ImageView) findViewById(R.id.btnPost);
		//btnPost.getLayoutParams().height = Math.round(height/9);
		//btnPost.getLayoutParams().width = Math.round(width/5);
		btnPost.setOnClickListener(this);
		
		back = (ImageView) findViewById(R.id.postphoto_form_back);
		//back.getLayoutParams().height = Math.round(height/15);
		//back.getLayoutParams().width = Math.round(width/10);
		back.setOnClickListener(this);
		
		bannerName = (TextView) findViewById(R.id.post_photo_tvBanner);
		bannerName.setTypeface(FontFactory.getUbuntuBold(this));	
		
		lvFriendsSelection = (ListView) findViewById(R.id.lvFriendsSelection);		
		PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);		
        
        lvFriendsSelection.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading ...");
        progressDialog.setCancelable(false);
        
        String str = PInfo.getString("double_click_selId", "");
        if (!str.contentEquals("")){
        
        	FlurryAgent.logEvent("SendPhotoDoubleTap");
        	progressDialog.show();
        	SendTask task = new SendTask();
    		task.progressDialog = progressDialog;
    		task.frm = this;
    		task.execute(str);
     	  	
	        SharedPreferences PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);
		    PInfo.edit().putString("double_click_selId", "").commit();	
		    PInfo = null;
		    
        }
        
		GetAllContactsList();
		for (String s : AllContactsArr)
			selItems.add(0);
		adapter = new PostAdapter(this, AllContactsArr, selItems);
		lvFriendsSelection.setAdapter(adapter);
	}

	public void onClickPost(View v) {
		RelativeLayout l = (RelativeLayout) v.getParent();
		CheckBox chbx = (CheckBox) l.getChildAt(3);
		chbx.setChecked(!chbx.isChecked());
		onClickChecked(chbx);
	}

	public void onClickPost_tvPhone(View v) {
		RelativeLayout l = (RelativeLayout) v.getParent();
		CheckBox chbx = (CheckBox) l.getChildAt(3);
		chbx.setChecked(!chbx.isChecked());
		onClickChecked(chbx);
	}

	public void onClickPost_tvNickname(View v) {
		RelativeLayout l = (RelativeLayout) v.getParent();
		CheckBox chbx = (CheckBox) l.getChildAt(3);
		chbx.setChecked(!chbx.isChecked());
		onClickChecked(chbx);
	}

	public void onClickChecked(View v) {

		CheckBox check = (CheckBox) v;
		
		boolean ischecked = check.isChecked();
		if (ischecked)
			selItems.set(Integer.valueOf(check.getTag().toString()), 1);
		else
			selItems.set(Integer.valueOf(check.getTag().toString()), 0);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		
		GetAllContactsList();
		adapter.notifyDataSetChanged();
	}

	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()){
		case R.id.btnPost:	        
			FlurryAgent.logEvent("SendPhoto");
			progressDialog.show();
     	  	SendPhoto();
			
			break;
		case R.id.Post_form_btnFindFriendsByLogin:
			
			FindFriendsByLogin();
						
						
			break;
		
		case R.id.postphoto_form_back:
			
			finish();
			
			break;
		
		}
	}
	
	public class SendTask extends AsyncTask<String, Void, String>
	{
		public ProgressDialog progressDialog;
		public PostPhoto frm;
		
		
		@Override
		protected void onPostExecute(String result) {
			System.gc();
			progressDialog.dismiss();
			if (result==null)
			{
				Toast.makeText(frm, "Пожалуйста, войдите в приложение еще раз", Toast.LENGTH_SHORT).show();
       		 	Intent Loginintent = new Intent(frm, LoginingForm.class);
				startActivity(Loginintent);
			}
			else if (result.contentEquals("Сообщение успешно отправлено"))
			{
				Toast.makeText(frm, result, Toast.LENGTH_SHORT).show();
   				setResult(RESULT_OK);
				finish();
			}
			else {
				Toast.makeText(frm, result, Toast.LENGTH_SHORT).show();				
			}
		}

		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... SelectedFriends) {
			
			 try{
	             HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost("http://www.mankichat.ru/gate/sendPhoto/");
	             
	             ByteArrayOutputStream bos = new ByteArrayOutputStream();
	             Redraw.getPicture(bos);
	             
	 			 String api_key_access = PInfo.getString("api_key_access", "");
				 String mimeType = "image/png";
	             
	             byte[] data = bos.toByteArray();
	             ByteArrayBody bab = new ByteArrayBody(data, mimeType, 
System.currentTimeMillis() + ".PNG");
	             
	             Intent intent = getIntent();
	             String timer = intent.getStringExtra("timer");
	             
	             MultipartEntity multipartEntity = new MultipartEntity(
HttpMultipartMode.BROWSER_COMPATIBLE);
	             multipartEntity.addPart("to_uids", new StringBody(SelectedFriends[0]));             
	             multipartEntity.addPart("photo", bab);
	             multipartEntity.addPart("time_live", new StringBody(timer));
	             multipartEntity.addPart("api_key_access", new StringBody(api_key_access));
	             post.setEntity(multipartEntity);           
	              
	          // HttpResponse
	             HttpResponse response = client.execute(post);
	             
	             BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
	             String line = reader.readLine();
	             
	             JSONObject jo = new JSONObject(line);
	             
	             if (jo.getString("status").contentEquals("ok")){
	            	 
	            	 //Intent intentf = new Intent();
	            	 setResult(RESULT_OK);
	            	 finish();
	            	 return "Сообщение успешно отправлено";
	            	 ///
	             }
	             
	             if (jo.getString("status").contentEquals("no")){
	            	 if (jo.get("code").toString().contentEquals("5")){
	            		 setResult(RESULT_CANCELED);
	            		 return "Выберите хотя бы одного получателя";
	            		 
	            	 }
	            	 if (jo.get("code").toString().contentEquals("1") || jo.get("code").toString().contentEquals("3")){
						 
						 return null;
						 
					 }else{
				    	  return "Сервер занят попробуйте позже";
					 }
	             }
	             
			 } catch (IOException e) {  
	        	 return "Пожалуйста подключитесь к сети интернет";
		      }
			catch (Exception e) {
	             return "error";
	         }
			 return null;
		}	
	}
	
	public void SendPhoto (){
		
		try
		{
			dbHelper = new DBHelper(this);		
	        db = dbHelper.getReadableDatabase();
			
			Cursor c = db.query("contact_table", null, null, null, null, null, "login");
		     for (int i = 0; i < lvFriendsSelection.getCount(); i++) {
		    	if (selItems.get(i)!=0){
		    		String item = adapter.getItem(i);
		    		item = item.substring(0, item.indexOf("\n"));
		    		
		    		/*if (c.isAfterLast())
		    	    	return;*/
		      
		    		c.moveToFirst();
		    		String arr = c.getString(c.getColumnIndex("login"));
		    		if (arr.contains(item)){
		    			if (SelectedFriends == ""){
		        	
		    				SelectedFriends = c.getString(c.getColumnIndex("user_id"));
		        	
		    			}else{
		        	
		    				SelectedFriends = SelectedFriends + "," + c.getString(c.getColumnIndex("user_id"));
		        	
		    			}
		    		}while (c.moveToNext()){
		    			
		    			if (c.getString(c.getColumnIndex("login")).contains(item)){
	    	    			if (SelectedFriends == ""){
	    	        	
	    	    				SelectedFriends = c.getString(c.getColumnIndex("user_id"));
	    	        	
	    	    			}else{
	    	        	
	    	    				SelectedFriends = SelectedFriends + "," + c.getString(c.getColumnIndex("user_id"));
	    	        	
	    	    			}
		    			}
		    		} 
		    	}    		
		    }
		    c.close();
		    db.close();
		}catch (Exception e) {
	    	  Toast.makeText(this, "UPS Huston we got a problem", Toast.LENGTH_SHORT).show();
        }  	
		
		SendTask task = new SendTask();
		task.progressDialog = progressDialog;
		task.frm = this;
		task.execute(SelectedFriends);
	}

	
public void FindFriendsByLogin(){
		
		String LoginForSearch = etFindFriendsByLogin.getText().toString();
		LoginForSearch = LoginForSearch.replace("\n", "");
		
		
		try{
		      HttpClient client = new DefaultHttpClient();
		      HttpPost post = new HttpPost("http://gate1.thundersnap.ru/gate/getFriendByLogin/");
		      
		      List pairs = new ArrayList();
		                	 
		      pairs.add(new BasicNameValuePair("api_key_access", PInfo.getString("api_key_access", "")));
		      pairs.add(new BasicNameValuePair("login", LoginForSearch));       
		      
		      post.setEntity(new UrlEncodedFormEntity(pairs));           
		       
		   // HttpResponse
		      HttpResponse response = client.execute(post);
		      
		      pairs.clear();
		      
		      BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
		      String line = reader.readLine();
		      
		      JSONObject jo = new JSONObject(line);
		      
		      if (jo.getString("status").contentEquals("ok")){
		    	  GetAllContactsList();
		    	  if (!AllUIdArr.contains(jo.getString("user_id"))){
			    	  dbHelper = new DBHelper(this);		
			          db = dbHelper.getWritableDatabase();
			    	  		        	 	                	
			          ContentValues  cv = new ContentValues ();
			          cv.put("user_id", jo.getString("user_id"));
			          cv.put("phone", "");
			          cv.put("login", LoginForSearch);
			          cv.put("is_friend", jo.getString("is_friend"));  	    
			          db.insert("contact_table", null, cv);			          
			          db.close();
			          
			          etFindFriendsByLogin.setText("");
			          InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			          if (imm != null)
			        	  imm.hideSoftInputFromWindow(etFindFriendsByLogin.getWindowToken(), 0);
			          
			          GetAllContactsList();
			          selItems.clear();
			          for (String s : AllContactsArr)
			  			selItems.add(0);
			          adapter.notifyDataSetChanged();
			          Toast.makeText(this, "Новый пользователь добавлен", Toast.LENGTH_SHORT).show();
			          
		    	  }else {
		    		  
		    		  Toast.makeText(this, "Пользователь уже у вас в списке друзей", Toast.LENGTH_SHORT).show();
		    	  }     
		          
		          
		          }
		      if (jo.getString("status").contentEquals("no")){
		    	  
		      
		    	if ( jo.getString("code").contentEquals("13")){
		    	  
		    	  Toast.makeText(this, "Пользователь с таким именем не существует", Toast.LENGTH_SHORT).show();
		    	}
		    	
		    	if ( jo.getString("code").contentEquals("130")){
			    	  
			    	  Toast.makeText(this, "Вы не можете добавить в друзья себя", Toast.LENGTH_SHORT).show();
			    	}
		    	
		    	if (jo.get("code").toString().contentEquals("5")){
		    		
		    		Toast.makeText(this, "Введите имя пользователя", Toast.LENGTH_SHORT).show();
		    		
		    	}
		    		
		    	if (jo.get("code").toString().contentEquals("1") || jo.get("code").toString().contentEquals("3")){
		    		
		    		Toast.makeText(this, "Пожалуйста, войдите в приложение еще раз", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(this, LoginingForm.class);
					startActivity(intent);
					 
				 }/*else{
			    	  Toast.makeText(this, "Сервер занят попробуйте позже", Toast.LENGTH_SHORT).show();
				 }*/

		      }
		       
		      } catch (org.apache.http.client.ClientProtocolException e) {
	              
	      } catch (IOException e) {  
	    	  Toast.makeText(this, "Пожалуйста подключитесь к сети интернет", Toast.LENGTH_SHORT).show();
	      
	      } catch (Exception e) {
	    	  Toast.makeText(this, "UPS Huston we got a problem", Toast.LENGTH_SHORT).show();    
	      }
		
	}

	
	
	public void GetAllContactsList(){
		
		dbHelper = new DBHelper(this);		
        db = dbHelper.getWritableDatabase();
        AllUIdArr.clear();
		AllContactsArr.clear();	
		Cursor c = db.query("contact_table", null, null, null, null, null, "login" );
	   
	    if (c.moveToFirst()) {     

	      do {
	    	  AllUIdArr.add(c.getPosition(), (c.getString(c.getColumnIndex("user_id")))); 
	    	  AllContactsArr.add(c.getPosition(), (c.getString(c.getColumnIndex("login"))+ "\n" + c.getString(c.getColumnIndex("phone"))));
	                
	       
	      } while (c.moveToNext());
	    } else
	    
	    c.close();
		db.close();
		
	}	
	
}
