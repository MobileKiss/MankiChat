package by.mobilekiss.mankichat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
      // конструктор суперкласса
      super(context, "MalankaDB", null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
      
      // создаем таблицу с полями
    	db.execSQL("create table contact_table ("
                + "id integer primary key autoincrement," 
                + "login text,"
                + "phone text,"
                + "is_friend,"
                + "user_id text CONSTRAINT use_id_index UNIQUE);");
    	
    	db.execSQL("create table messages_table ("
                + "id integer primary key autoincrement," 
                + "id_dialog text,"
                + "from_id text,"
                + "from_login text,"
                + "datesend text,"
                + "was_send text,"
                + "is_view text,"
                + "to_id text,"
                + "to_login text,"
                + "time_live text,"
                + "photo_url text" + ");");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
  }
