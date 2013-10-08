package by.mobilekiss.mankichat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class InviteFriends extends Activity implements OnClickListener {

	ImageView btnInvite;
	boolean isChecked = false;
	ImageView back, ivSelectAll;
	SharedPreferences PInfo;
	JSONArray SelectedFriends = new JSONArray();
	DBHelper dbHelper;
	TextView tvHeader;
	ListView lvFriendInvite;
	SQLiteDatabase db;
	ProgressDialog progressDialog;
	ArrayAdapter<String> adapter;
	ArrayList<String> AllPhonesArr = new ArrayList<String>();
	ArrayList<String> AllNamesArr = new ArrayList<String>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.invite_friend_form);

		setResult(1);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		ivSelectAll = (ImageView) findViewById(R.id.ivSelectAll);
		ivSelectAll.setOnClickListener(this);
		
		btnInvite = (ImageView) findViewById(R.id.btnInviteFriends);
		btnInvite.setOnClickListener(this);

		tvHeader = (TextView) findViewById(R.id.invite_form_tvbanner);
		tvHeader.setTypeface(FontFactory.getUbuntuBold(this));

		back = (ImageView) findViewById(R.id.invite_friends_back);
		back.setOnClickListener(this);

		PInfo = getSharedPreferences("PrivateInfo", MODE_PRIVATE);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Ищем ваших друзей...");
		progressDialog.setCancelable(true);
		progressDialog.show();

		GetAllContacts();

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, AllNamesArr);
		lvFriendInvite = (ListView) findViewById(R.id.lvFriendInvite);
		lvFriendInvite.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		lvFriendInvite.setAdapter(adapter);
		for (int i = 0; i < lvFriendInvite.getCount(); i ++)
			lvFriendInvite.setItemChecked(i, true);

	}

	@Override
	public void onResume() {
		super.onResume();

		btnInvite.setEnabled(true);
		if (PInfo.getString("first_start", "").contentEquals(""))
			GetAllPhonesFromContacts();
		
		if (progressDialog.isShowing())
			progressDialog.cancel();

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.invite_friends_back:

			setResult(1);
			finish();

			break;
			
		case R.id.ivSelectAll:
			isChecked = !isChecked;
			for (int i = 0; i < lvFriendInvite.getCount(); i ++)
				lvFriendInvite.setItemChecked(i, !isChecked);
			
			break;
			
		case R.id.btnInviteFriends:
			// пишем в лог выделенные элементы
			FlurryAgent.logEvent("SendInvite");
			SparseBooleanArray sbArray = lvFriendInvite
					.getCheckedItemPositions();
			for (int i = 0; i < lvFriendInvite.getCount(); i++) {

				if (sbArray.get(i)) {

					JSONObject obj1;
					try {

						obj1 = new JSONObject();
						obj1.put("phone", AllPhonesArr.get(i).toString());
						obj1.put("local", "ru");
						obj1.put("name", (AllNamesArr.get(i).toString()));
						SelectedFriends.put(obj1);

					} catch (JSONException e) {

						e.printStackTrace();
					}

				}
			}
			if (SelectedFriends.length() != 0) {
				
				btnInvite.setClickable(false);
				SendInvitation();
				btnInvite.setClickable(true);

			} else
				Toast.makeText(this, "Выберите хотя бы одного друга",
						Toast.LENGTH_SHORT).show();
			break;
		}

	}

	public void SendInvitation() {

		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://www.mankichat.ru/gate/sendInvate/");

			List pairs = new ArrayList();

			pairs.add(new BasicNameValuePair("api_key_access", PInfo.getString(
					"api_key_access", "")));
			pairs.add(new BasicNameValuePair("friends", SelectedFriends
					.toString()));

			post.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));

			// HttpResponse
			HttpResponse response = client.execute(post);

			pairs.clear();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			String line = reader.readLine();

			JSONObject jo = new JSONObject(line);

			if (jo.get("status").toString().contentEquals("ok")) {

				/*
				 * GetAllContacts(); adapter.notifyDataSetChanged();
				 */
				Toast.makeText(this, "Приглашение отправлено",
						Toast.LENGTH_SHORT).show();
				setResult(1);
				finish();
				btnInvite.setEnabled(true);

			}

			if (jo.get("status").toString().contentEquals("no")) {

				if (jo.get("code").toString().contentEquals("5")) {

					Toast.makeText(this, "Выберите как минимум одного друга",
							Toast.LENGTH_SHORT).show();

				}

				if (jo.get("code").toString().contentEquals("1")
						|| jo.get("code").toString().contentEquals("3")) {

					Toast.makeText(this,
							"Пожалуйста, войдите в приложение еще раз",
							Toast.LENGTH_SHORT).show();
					btnInvite.setEnabled(true);
					Intent intent = new Intent(this, LoginingForm.class);
					startActivity(intent);

				} else {
					Toast.makeText(this, "Сервер занят попробуйте позже",
							Toast.LENGTH_SHORT).show();
				}
			}
		} catch (org.apache.http.client.ClientProtocolException e) {

		} catch (IOException e) {
			Toast.makeText(this, "Пожалуйста подключитесь к сети интернет",
					Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			Toast.makeText(this, "UPS Huston we got problem",
					Toast.LENGTH_SHORT).show();

		}
	}

	public void GetAllContacts() {

		Cursor phones = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
		while (phones.moveToNext()) {

			String name = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String phoneNumber = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			AllPhonesArr.add(phones.getPosition(), phoneNumber);
			AllNamesArr.add(phones.getPosition(), name);

		}

		phones.close();

	}

	public void GetAllPhonesFromContacts() {

		ArrayList<String> AllPhoneArr = new ArrayList<String>();
		String AllPhones = "";
		int i = 0;

		Cursor phones = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, null);

		if (phones.isAfterLast())
			return;

		phones.moveToFirst();
		String number = phones.getString(phones
				.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		number = makeRightNumber(number);
		int k = phones.getPosition();
		AllPhoneArr.add(k, number);
		AllPhones = AllPhones + number + ",";

		while (phones.moveToNext()) {

			number = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			number = makeRightNumber(number);
			AllPhoneArr.add(phones.getPosition(), number);

			if (phones.isLast() != true) {

				AllPhones = AllPhones + number + ",";

			}
			if (phones.isLast() == true) {

				AllPhones = AllPhones + number;

			}

		}
		phones.close();

		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://www.mankichat.ru/gate/getUserFriends2/");

			List pairs = new ArrayList();

			pairs.add(new BasicNameValuePair("users_list", AllPhones));
			pairs.add(new BasicNameValuePair("api_key_access", PInfo.getString(
					"api_key_access", "")));

			post.setEntity(new UrlEncodedFormEntity(pairs));

			// HttpResponse
			HttpResponse response = client.execute(post);

			pairs.clear();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));

			String line = reader.readLine();

			JSONObject jo = new JSONObject(line);
			if (jo.get("status").toString().contentEquals("ok")) {
				 
				
				
				// добавление самого себя в список пользователей 
				ContentValues cv = new ContentValues();
				Bundle bundle = getIntent().getExtras();
				if (bundle != null){
					dbHelper = new DBHelper(this);
					db = dbHelper.getWritableDatabase();								
					cv.put("login", bundle.getString("userName") + " (это вы)");
					cv.put("user_id", PInfo.getString("user_id", ""));
					cv.put("phone", bundle.getString("userPhone"));
					db.insert("contact_table", null, cv);
					cv.clear();
					db.close();
				}
				
				

				//
				
				JSONArray contacts = jo.getJSONArray("meta");
				for (i = 0; i < contacts.length(); i++) {

					JSONObject c = contacts.getJSONObject(i);
					
					String phone = "";
					String login = "";
					
					// добавление имен к никам друзей			
					for (int ii = 0; ii < AllPhonesArr.size(); ii++){
						
						if (phone.contentEquals(makeRightNumber(AllPhonesArr.get(ii))))
							login = c.getString("login") + " (" + AllNamesArr.get(ii) + ")";
							phone = c.getString("phone");					
					}
					//
					
					String user_id = c.getString("user_id");					
					if (login.contentEquals(""))
						login = c.getString("login");
					
					dbHelper = new DBHelper(this);
					db = dbHelper.getWritableDatabase();
					cv.put("login", login);
					cv.put("user_id", user_id);
					cv.put("phone", phone);
					db.insert("contact_table", null, cv);
					cv.clear();
					db.close();
				}

				Editor editor = PInfo.edit();
				editor.putString("first_start", "NO");
				editor.commit();

			}

			if (jo.get("status").toString().contentEquals("no")) {
				Toast.makeText(this, "Сервер занят попробуйте позже",
						Toast.LENGTH_SHORT).show();
			}

		} catch (org.apache.http.client.ClientProtocolException e) {

		} catch (IOException e) {
			Toast.makeText(this, "Пожалуйста подключитесь к сети интернет",
					Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			Toast.makeText(this, "UPS Huston we got problem",
					Toast.LENGTH_SHORT).show();

		}
	}

	public String makeRightNumber(String number) {

		while (number.contains("+")) {

			number = number.replace("+", "");

		}
		while (number.contains(" ")) {

			number = number.replace(" ", "");

		}
		while (number.contains(")")) {

			number = number.replace(")", "");

		}
		while (number.contains("-")) {

			number = number.replace("-", "");

		}
		while (number.contains("(")) {

			number = number.replace("(", "");

		}
		return number;
	}

}
