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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.util.Log;




public class MessangerService extends android.app.Service {
	
	SharedPreferences PInfo;
	DBHelper dbHelper;
	SQLiteDatabase db = null;
	NotificationManager nm;
	String from_login = "", login = "", User_id = "";
	ArrayList<String> AllUIdArr = new ArrayList<String>();
	

	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}
	
	 public void onCreate() {
		 	StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().penaltyDeath().build());
		    super.onCreate();
		    
		    PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);
			User_id = PInfo.getString("user_id", "");
		    nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		    if (android.os.Build.VERSION.SDK_INT > 9) {
				StrictMode.ThreadPolicy policy = 
				        new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy);
				}		    
		  }
		  
		  public int onStartCommand(Intent intent, int flags, int startId) {
			  
			  new Thread(new Runnable() {
			      public void run() {
			        while (true) {
			        	try {
			        		GetNewMessages();
			        		GetUserFriendsUpdate();		        	
			        		TimeUnit.SECONDS.sleep(60);
			        	} catch (InterruptedException e) {
			        		e.printStackTrace();
			          	}
			        }			       
			      }
			    }).start();
			  
			  
		    return super.onStartCommand(intent, flags, startId);
		  }

		  
		  void sendMessageNotification() {
			  
			  Intent intent = new Intent(this, CameraActivity.class);
			  PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
			    			  					
					Notification notif = new NotificationCompat.Builder(this)
				   	.setContentTitle("MankiChat")
				   	.setContentIntent(pIntent)
				   	.addAction(R.drawable.messages_form_new_message, "MankiChat", null)
				   	.setContentText("Ќовое сообщение от " + from_login)
				   	.setSmallIcon(R.drawable.ic_launcher)
				   	.setTicker("MankiChat")
				   	.setAutoCancel(true)
				   	.setDefaults(-1)
				   	.build();
			 
			    nm.notify(1, notif);
			  }
		  
		  /*void sendNewUserNotification(String FriendName) {			  
			  
			  Intent intent = new Intent(this, CameraActivity.class);
			  PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);				  	
					
			  Notification notifyc = new NotificationCompat.Builder(this)					
				   	.setContentTitle("Liloo New Friend")
				   	.addAction(R.drawable.messages_form_new_message, "Liloo", pIntent)
				   	.setContentText("¬аш друг "+ FriendName +" добавилс€ к вам в список контактов")
				   	.setSmallIcon(R.drawable.ic_launcher)
				   	.setTicker("Liloo")
				   	.setAutoCancel(true)
				   	.setDefaults(-1)
				   	.build();		  
			  
			    nm.notify(2, notifyc);
			  }*/
		  
		  public void GetNewMessages(){
				
				try{
					
					int i = 0;
				
				    HttpClient client = new DefaultHttpClient();
				    HttpPost post = new HttpPost("http://www.mankichat.ru/gate/getUpdateList/");
				      
				    List pairs = new ArrayList();
				                	 
		     	 
				    pairs.add(new BasicNameValuePair("api_key_access", PInfo.getString("api_key_access", "")));
				    pairs.add(new BasicNameValuePair("page", "0"));
				    String id = PInfo.getString("start_id_notification", "0");
				    if (id.contentEquals("0"))
				    	id = PInfo.getString("start_id_dialog", "0");
				    pairs.add(new BasicNameValuePair("start_id_dialog", id));
		      
				    post.setEntity(new UrlEncodedFormEntity(pairs));           
		       
			   // HttpResponse
			      HttpResponse response = client.execute(post);
			      
			      pairs.clear();
			      
			      BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
			      String line = reader.readLine();
			      
			      JSONObject jo = new JSONObject(line);
			     if (!jo.get("count").toString().contentEquals("0")){
			    	 
			    	 //dbHelper = new DBHelper(this);		
				     //db = dbHelper.getWritableDatabase();
			    	 int Count = Integer.parseInt(jo.get("count").toString());			    	 
			    	 int mxId = Integer.parseInt(jo.getString("start_id_dialog"));
			    	 			    	 
				     JSONArray messagesArr = jo.getJSONArray("meta");
				     for(i = 0; i < Count; i++){
				    	 JSONObject message = messagesArr.getJSONObject(i);
				        	  
				         String id_dialog = message.getString("id_dialog");
				         int dlgid = Integer.parseInt(id_dialog);
				         if (dlgid>mxId)
				        	 mxId = dlgid;
				       //  String photo_url = message.getString("photo_url");
				         String from_id = message.getString("from_id");
				         from_login = message.getString("from_login");
				         /*String is_view = message.getString("is_view");
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
				             
				         db.insert("messages_table", null, cv);*/
				         //cv.clear();
				         
				         if (!User_id.contentEquals(from_id)){ 
					    	 sendMessageNotification();
					    	 saveInfo("triangle", "1"); 
				         }
			          
				     }
				     saveInfo("start_id_notification", Integer.valueOf(mxId).toString());
				     db.close();
				     
		      } 
			     
			  if (jo.get("status").toString().contentEquals("no")){
			    	  //Toast.makeText(this, "—ервер зан€т попробуйте позже", Toast.LENGTH_SHORT).show();
			  	}
		        
			  
		      } catch (org.apache.http.client.ClientProtocolException e) {
		    	  e.printStackTrace();
		              
		      } catch (IOException e) { 
		    	  e.printStackTrace();
		    	 //Toast.makeText(this, "ѕожалуйста подключитесь к сети интернет", Toast.LENGTH_SHORT).show();
		      
		      } catch (Exception e) {
		    	  e.printStackTrace();
		    	  //Toast.makeText(this, "UPS Huston we got problem", Toast.LENGTH_SHORT).show();
		              
		      }                                        
		}

		  
		  //ƒанные на вывод: {УstatusФ:ФokФ,ФcountФ:Ф1Ф,ФmetaФ:{Уphone2Ф:{УloginФ:ФnameФ,Фuser_idФ:Ф1Ф,Фis_readФ:ФfalseФ}}}
		  public void GetUserFriendsUpdate(){
			  int i;
				
				try{
					PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);
				    HttpClient client = new DefaultHttpClient();
				    HttpPost post = new HttpPost("http://www.mankichat.ru/gate/getUserFriendsUpdate2/");
				      
				    List pairs = new ArrayList();
				                	 
		     	 
				    pairs.add(new BasicNameValuePair("api_key_access", PInfo.getString("api_key_access", "")));        
		      
				    post.setEntity(new UrlEncodedFormEntity(pairs));           
		       
			   // HttpResponse
			      HttpResponse response = client.execute(post);
			      
			      pairs.clear();
			      
			      BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
			      
			      String line = reader.readLine();
			      //line = "\"status\":\"ok\",\"count\":\"1\",\"meta\":{\"phone2\":{\"login\":\"name\",\"user_id\":\"1\",\"is_read\":\"false\"}}}";
			      JSONObject jo = new JSONObject(line);
			      if (jo.get("status").toString().contentEquals("ok")){
			     	 
					    JSONArray contacts = jo.getJSONArray("meta");
					    if (contacts.length() != 0){
						    for(i = 0; i < contacts.length(); i++){
						    	
						    	if (!AllUIdArr.contains(jo.getString("user_id"))){
						    	
						          JSONObject c = contacts.getJSONObject(i);
					              String user_id = c.getString("user_id"); 
						          login = c.getString("login");
						          String phone = c.getString("phone");
						        	  
						          dbHelper = new DBHelper(this);		
						      	  db = dbHelper.getWritableDatabase();
						        	  	                	
						          ContentValues  cv = new ContentValues ();
						          cv.put("login", login);
						          cv.put("user_id", user_id); 
						          //cv.put("phone", phone);
						          db.insert("contact_table", null, cv);
						          cv.clear();
						          db.close();
						    	}
						          
						    }
						   // sendNewUserNotification(login);
						    
					    }
			      }
			 	      
			 	   if (jo.get("status").toString().contentEquals("no")){
			 	    	  //Toast.makeText(this, "—ервер зан€т попробуйте позже", Toast.LENGTH_SHORT).show();
			 	   }			            
			         
			       } catch (org.apache.http.client.ClientProtocolException e) {
			               
			       } catch (IOException e) { 
			    	   Log.e("Notification", e.getMessage());
			     	  //Toast.makeText(this, "ѕожалуйста подключитесь к сети интернет", Toast.LENGTH_SHORT).show();
			       
			       } catch (Exception e) {
			    	   Log.e("Notification", e.getMessage());
			     	  //Toast.makeText(this, "UPS Huston we got problem", Toast.LENGTH_SHORT).show();
			               
			       }                                        
			 }	
		  public void saveInfo(String FieldName, String FieldValue) {
				PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);
			    Editor ed = PInfo.edit();
			    ed.putString(FieldName, FieldValue);
			    ed.commit();
			    PInfo = null;
			    
			}
		  
		  public void GetAllFriendsList(){
				
				dbHelper = new DBHelper(this);		
			    db = dbHelper.getWritableDatabase();	

				AllUIdArr.clear();
				Cursor c = db.query("contact_table", null, null, null, null, null, "login");
			   
			    if (c.moveToFirst()) {     

			      do {
			    	  AllUIdArr.add(c.getPosition(), (c.getString(c.getColumnIndex("user_id"))));        
			       
			      } while (c.moveToNext());
			    } else
			      
			    c.close();
			    db.close();
				
				
			}	


}
