package by.mobilekiss.mankichat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MyFriends extends Activity implements OnClickListener{

	ImageView btnFindFriendsByLogin;
	ListView lvContactsList;
	EditText etFindFriendsByLogin;
	SharedPreferences PInfo;
	ImageView inviteFriends, back;
	public static final int RESULT_CODE_OK = 1;
	public static final int RESULT_CODE_ERROR = 0;
	
	ArrayList<String> AllPhonesArr = new ArrayList<String>();
	ArrayList<String> AllUIdArr = new ArrayList<String>();
	My_Friends_Adapter adapter = null;
	public String name = "";
	public String id;
	public String phone;
	DBHelper dbHelper;
	SQLiteDatabase db = null;
	Cursor c = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.my_friends);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = 
			        new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			}
		
		Display display = getWindowManager().getDefaultDisplay(); 
		int width = display.getWidth();  // deprecated
		int height = display.getHeight();  // deprecated

		
		btnFindFriendsByLogin = (ImageView) findViewById(R.id.btnFindFriendsByLogin);
		//btnFindFriendsByLogin.getLayoutParams().height = Math.round(height/14);
		//btnFindFriendsByLogin.getLayoutParams().width = Math.round(width/9);
		btnFindFriendsByLogin.setOnClickListener(this);
		
		etFindFriendsByLogin = (EditText) findViewById(R.id.etFindFriendsByLogin);
		etFindFriendsByLogin.setTypeface(FontFactory.getUbuntuBold(this));
		
		inviteFriends = (ImageView) findViewById(R.id.my_friends_btninvite_friends);
		//inviteFriends.getLayoutParams().height = Math.round(height/15);
		//inviteFriends.getLayoutParams().width = Math.round(width/10);
		inviteFriends.setOnClickListener(this);
		
		back = (ImageView) findViewById(R.id.myfriends_form_back);
		//back.getLayoutParams().height = Math.round(height/15);
		//back.getLayoutParams().width = Math.round(width/10);
		back.setOnClickListener(this);
		
		TextView BannerName = (TextView) findViewById(R.id.my_friends_tvBanner);
		BannerName.setTypeface(FontFactory.getUbuntuBold(this));
				
		ListView lvContactsList = (ListView) findViewById(R.id.lvContactsList);		
		
		PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);   
 
        GetAllFriendsList();
        // создаем адаптер
        adapter = new My_Friends_Adapter(this, AllPhonesArr);        
        lvContactsList.setAdapter(adapter);
        
		
	}

	@Override
    public void onClick(View v) {
		switch (v.getId()){
			
		case R.id.btnFindFriendsByLogin:
			
			FindFriendsByLogin();					
						
			break;
			
		case R.id.my_friends_btninvite_friends:
			Intent intent = new Intent(this, InviteFriends.class);
			startActivity(intent);
			break;
		case R.id.myfriends_form_back:
			
			finish();
			break;
		}
    }
	
	
public void GetAllFriendsList(){
	
	dbHelper = new DBHelper(this);		
    db = dbHelper.getWritableDatabase();	
	AllPhonesArr.clear();
	AllUIdArr.clear();
	Cursor c = db.query("contact_table", null, null, null, null, null, "login");
   
    if (c.moveToFirst()) {     

      do {
    	  AllUIdArr.add(c.getPosition(), (c.getString(c.getColumnIndex("user_id"))));       
    	  AllPhonesArr.add(c.getPosition(), (c.getString(c.getColumnIndex("login"))+ "\n" + c.getString(c.getColumnIndex("phone"))));        
       
      } while (c.moveToNext());
    } else
      
    c.close();
    db.close();
	
	
}	

	public void FindFriendsByLogin(){		
		
		String LoginForSearch = etFindFriendsByLogin.getText().toString();
		LoginForSearch = LoginForSearch.replace("\n", "");
		
		try{
		      HttpClient client = new DefaultHttpClient();
		      HttpPost post = new HttpPost("http://www.mankichat.ru/gate/getFriendByLogin/");
		      
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
		    	  GetAllFriendsList();
		    	  if (AllUIdArr.contains(jo.getString("user_id")) == false){
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
			          
			          GetAllFriendsList();
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
					 
				 }
		    	/*else{
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
	
	/*public void GetAllPhonesFromContacts(){
		
		ArrayList<String> AllPhonesArr = new ArrayList<String>();
		String AllPhones = "";
		int i = 0;
	
		Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
		while (phones.moveToNext()){
						
			String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			AllPhonesArr.add(phones.getPosition(), number);
			
			
			if (phones.isLast() != true){
						
				AllPhones = AllPhones + number + ",";				
				
			}
			if (phones.isLast() == true){
				
				AllPhones = AllPhones + number;
				
			}		
			
		}
		phones.close();
    
	    try{
	      HttpClient client = new DefaultHttpClient();
	      HttpPost post = new HttpPost("http://www.mankichat.ru/gate/getUserFriends/");
	      
	      List pairs = new ArrayList();
	                	 
	     	 pairs.add(new BasicNameValuePair("users_list", AllPhones));
	     	 pairs.add(new BasicNameValuePair("api_key_access", PInfo.getString("api_key_access", "")));
	     	 //
	        
	      
	      post.setEntity(new UrlEncodedFormEntity(pairs));           
	       
	   // HttpResponse
	      HttpResponse response = client.execute(post);
	      
	      pairs.clear();
	      
	      BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
	      StringBuilder sb = new StringBuilder();
	      String line = reader.readLine();
	      
	      JSONObject jo = new JSONObject(line);
	      
	     if (jo.get("status").toString().contentEquals("ok")){
	    	 
	    	 dbHelper = new DBHelper(this);		
	         db = dbHelper.getWritableDatabase();
		     JSONArray contacts = jo.getJSONArray("meta");
		      
		     for(i = 0; i < contacts.length(); i++){
		          JSONObject c = contacts.getJSONObject(i);
		           
		          String phone = c.getString(AllPhonesArr.get(i).toString());
		          
		          if (phone.contentEquals("no") == false){
		        	  JSONObject ContactInfo = c.getJSONObject(AllPhonesArr.get(i).toString());
		        	  
		        	  String user_id = ContactInfo.getString("user_id"); 
		        	  String login = ContactInfo.getString("login");
		        	               
		                	
		              ContentValues  cv = new ContentValues ();
		              cv.put("login", login);
		              cv.put("user_id", user_id); 
		              cv.put("phone", AllPhonesArr.get(i).toString());
		              db.insert("contact_table", null, cv);
		              cv.clear();
		             		              
		          }
		     }
		     db.close();
	     } 
	     if (jo.get("code").toString().contentEquals("1") || jo.get("code").toString().contentEquals("3")){
			 
			 Toast.makeText(this, "Пожалуйста, войдите в приложение еще раз", Toast.LENGTH_SHORT).show();
			 Intent intent = new Intent(this, LoginingForm.class);
			 startActivity(intent);
	     }
		  if (jo.get("status").toString().contentEquals("no")){
		    	  Toast.makeText(this, "Сервер занят попробуйте позже", Toast.LENGTH_SHORT).show();
		      }
	            
	        
	      } catch (org.apache.http.client.ClientProtocolException e) {
	              
	      } catch (IOException e) {  
	    	  Toast.makeText(this, "Пожалуйста подключитесь к сети интернет", Toast.LENGTH_SHORT).show();
	      
	      } catch (Exception e) {
	    	  Toast.makeText(this, "UPS Huston we got problem", Toast.LENGTH_SHORT).show();
	              
	      }                                        
	}	*/
		
}
