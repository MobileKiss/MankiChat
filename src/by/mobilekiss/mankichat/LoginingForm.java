package by.mobilekiss.mankichat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class LoginingForm extends Activity implements OnClickListener{
	
	Button Login, btnConfirm, btnOpenCodeForm, btnOpenRegForm;
	EditText PhoneNumber, UserName, ConfirmationCode;
	String login = "", phone = "", ConfCode = "", api_key = "", user_id = "", api_key_access = "",status = "", code = "";
	RelativeLayout llConfirm, llLogin;
	SharedPreferences PInfo;
	

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.loginform);
		
		setResult(1);
		
		Display display = getWindowManager().getDefaultDisplay(); 
		int width = display.getWidth();  // deprecated
		int height = display.getHeight();  // deprecated
		
		ImageView login_icon = (ImageView)findViewById(R.id.login_form_icon);
		//login_icon.getLayoutParams().height = Math.round(height/(float)2.5);
		//login_icon.getLayoutParams().width = Math.round(width/(float)1.5);
		
		
		ImageView login_icon2 = (ImageView)findViewById(R.id.login_form_icon2);
		//login_icon2.getLayoutParams().height = Math.round(height/(float)2.5);
		//login_icon2.getLayoutParams().width = Math.round(width/(float)1.5);
		
		
		TextView tv4 = (TextView) findViewById(R.id.tvlogin4);
		tv4.setTypeface(FontFactory.getUbuntuBold(this));
		
		PhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
		PhoneNumber.setTypeface(FontFactory.getUbuntuBold(this));
		PhoneNumber.setHint("ВАШ НОМЕР ТЕЛЕФОНА");
		
		
		UserName = (EditText) findViewById(R.id.etName);
		UserName.setTypeface(FontFactory.getUbuntuBold(this));
		UserName.setHint("НИКНЕЙМ");
		
		TextView tvLicense = (TextView) findViewById(R.id.tvLicense);
		tvLicense.setOnClickListener(this);
		
		ConfirmationCode = (EditText) findViewById(R.id.etConfirmationCode);
		ConfirmationCode.setTypeface(FontFactory.getUbuntuBold(this));
		ConfirmationCode.setHint("ВВЕСТИ КОД ДОСТУПА");
		
		
		btnOpenCodeForm = (Button) findViewById(R.id.btnOpenCodeForm);
		btnOpenCodeForm.setTypeface(FontFactory.getUbuntuBold(this));
		btnOpenCodeForm.setOnClickListener(this);
		
		btnOpenRegForm = (Button) findViewById(R.id.btnOpenRegForm);
		btnOpenRegForm.setTypeface(FontFactory.getUbuntuBold(this));
		btnOpenRegForm.setOnClickListener(this);
		
		Login = (Button) findViewById(R.id.btnLogin);
		Login.setTypeface(FontFactory.getUbuntuBold(this));
		Login.setOnClickListener(this);
		
		llConfirm = (RelativeLayout) findViewById(R.id.llConfirmation);
		llLogin = (RelativeLayout) findViewById(R.id.llLogin);
		
		btnConfirm = (Button) findViewById(R.id.btnConfirm);
		btnConfirm.setTypeface(FontFactory.getUbuntuBold(this));
		btnConfirm.setOnClickListener(this);
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = 
			        new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			}

		

	}

	@Override
	public void onClick(View v) {
			
		login = (UserName.getText()).toString();
		phone = (PhoneNumber.getText()).toString();
		
		switch (v.getId()){
		
		case R.id.tvLicense:
			
			Intent intent = new Intent(this, LicenseActivity.class);
			startActivity(intent);
		
		case R.id.btnOpenCodeForm:
			
			llConfirm.setVisibility(View.VISIBLE);
			llLogin.setVisibility(View.GONE);
			break;
		
		case R.id.btnOpenRegForm:
			
			llConfirm.setVisibility(View.GONE);
			llLogin.setVisibility(View.VISIBLE);
			break;
		
		case R.id.btnConfirm:
			
			ConfCode = (ConfirmationCode.getText()).toString();
			btnConfirm.setClickable(false);
			FlurryAgent.logEvent("SetRegistrationKey");
			Logining("setSmsKey");
			btnConfirm.setClickable(true);
			
			break;
		
		case R.id.btnLogin:
			
			if (login.length() != 0 && phone.length() != 0 ){
				FlurryAgent.logEvent("SetSMSKey");
				Logining("setRegistration");					
				break;
			}
						
			else {
				
				Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_SHORT).show();
			}
			break;
		}
		}
	
	public void Logining(String Function){			
           
           String tz = (TimeZone.getDefault().getID());                 
           
           try{
             HttpClient client = new DefaultHttpClient();
             HttpPost post = new HttpPost("http://www.mankichat.ru/gate/" + Function + "/");
             
             List pairs = new ArrayList();
             if (Function == "setRegistration"){
	             pairs.add(new BasicNameValuePair("phone", phone));
	             pairs.add(new BasicNameValuePair("login", login));
	             pairs.add(new BasicNameValuePair("tz", tz));
             }
             if (Function == "setSmsKey"){            	 
            	 pairs.add(new BasicNameValuePair("sms_key", ConfCode));
             }             
             
             post.setEntity(new UrlEncodedFormEntity(pairs));           
              
          // HttpResponse
             HttpResponse response = client.execute(post);
             
             BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
             StringBuilder sb = new StringBuilder();
             String line = reader.readLine();
             
             JSONObject jo = new JSONObject(line);
             
             status = jo.getString("status");
             
             
             if (status.contentEquals("no")){
            	 code = jo.getString("code");
             }
             
             if (Function == "setSmsKey"){
            	 
            	 if (status.contentEquals("no") && code.contentEquals("8")){
            		 
            		 Toast.makeText(this, "Неверно указан код подтверждения", Toast.LENGTH_SHORT).show(); 
            		 
            	 } 
            	 if (status.contentEquals("ok")) {           		 
            		
            		api_key = jo.getString("api_key");
                	user_id = jo.getString("user_id");
                	
            		api_key_access = md5(api_key + ConfCode);            		
            		saveInfo ("api_key", api_key);
            		saveInfo ("user_id", user_id);
            		saveInfo ("login", login);
            		saveInfo ("api_key_access", api_key_access);
            		saveInfo ("start_id_dialog", "0");
            		
            		/*PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);
            		String FirstStart = PInfo.getString("first_start", "");*/
            		
            		InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(ConfirmationCode.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            		
           		/* if (isMessangerServiceRunning() == false && FirstStart.contentEquals("")){
           				
           				startService(new Intent(this, MessangerService.class));
           				
           			}*/
           		 	PInfo = null;
           		 	Intent intent = new Intent(this, InviteFriends.class);
           		 	intent.putExtra("userName", login);
           		 	intent.putExtra("userPhone", phone);
           		 	startActivityForResult(intent, 6);
           		 	/*setResult(1);
            		finish();*/
            		
            	 }
            	   
             }
             
             if (Function == "setRegistration"){             	
            	       	 
            	 if (status.contentEquals("ok")){  
            		 
            		 llLogin.setVisibility(View.GONE);
            		 llConfirm.setVisibility(View.VISIBLE);
            		             		 
            	}
            	 
            	 if (status.contentEquals("no") && code.contentEquals("7")){            		 
            		 Toast.makeText(this, "Такой логин уже существует", Toast.LENGTH_SHORT).show();            		 
            	 }
            	 if (status.contentEquals("no") && code.contentEquals("6")){            		 
            		 Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_SHORT).show();            		 
            	 }
            	       
             }
             
             } catch (org.apache.http.client.ClientProtocolException e) {
                     
             } catch (IOException e) {
            	 
            	 Toast.makeText(this, "Подключитесь к сети интернет", Toast.LENGTH_SHORT).show();
             
             } catch (Exception e) {
                    Log.e("tag", e.toString());
             }                                        
    }
		
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (resultCode == 1) {
	        	setResult(1);
	            finish();
	        }
	 }
	
	private boolean isMessangerServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (MessangerService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public void saveInfo(String FieldName, String FieldValue) {
	    PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);
	    Editor ed = PInfo.edit();
	    ed.putString(FieldName, FieldValue);
	    ed.commit();
	    PInfo = null;
	    
	}
	
	public static final String md5(final String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest
	                .getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();
	 
	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i = 0; i < messageDigest.length; i++) {
	            String h = Integer.toHexString(0xFF & messageDigest[i]);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        return hexString.toString();
	 
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	 
	 @Override
		public void onBackPressed (){
			
			setResult(0);		
			super.onBackPressed();	
			
		}
}

