package com.medo.blogirame.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseCategories {
	
    private static final String TABLE_NAME = "categories";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "categories";
 
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SLUG = "slug";
    
    private Context context;
    private SQLiteDatabase db;
    
    public DataBaseCategories(Context context) {
    	this.context = context;
    	OpenHelper openHelper = new OpenHelper(this.context);
    	this.db = openHelper.getWritableDatabase();   	
//        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public void insertInto(String name , String slug) {
    	ContentValues cValues = new ContentValues();
    	cValues.put(COLUMN_NAME, name);
    	cValues.put(COLUMN_SLUG, slug);

    	this.db.insert(TABLE_NAME, "", cValues);
    }
    
	public void deleteAll() {
    	this.db.delete(TABLE_NAME, null, null);
    }
	
    public void closeDB() {
    	this.db.close();
    }
    
    public String[] getCategories() {
    	
    	Cursor cursor = this.db.query(TABLE_NAME, new String[] {COLUMN_NAME}, null, null, null, null, null);
    	String[] toReturn = new String[cursor.getCount()];
    	
    	int i=0;
    	while(cursor.moveToNext()) {
    		toReturn[i++] = cursor.getString(0);
    	}
    	
    	if (cursor != null && !cursor.isClosed()) {
    		cursor.close();
    	}
    	
    	return toReturn;
    }
    
    public String getSlugByName(String name) {
    	
    	Cursor cursor = this.db.query(TABLE_NAME, new String[] {COLUMN_SLUG}, COLUMN_NAME + " like '" + name +"'", null, null, null, null);
    	String toReturn = null;
    	
    	if(cursor.moveToNext()) {
    		toReturn = cursor.getString(0);
    	}
    	
    	if (cursor != null && !cursor.isClosed()) {
    		cursor.close();
    	}
    	
    	return toReturn;
    }
    
    private static class OpenHelper extends SQLiteOpenHelper {

        OpenHelper(Context context) {
           super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
           db.execSQL("CREATE TABLE " + TABLE_NAME + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, " +
           		"slug TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           //Log.w("Example", "Upgrading database, this will drop tables and recreate.");
           db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
           onCreate(db);
        }
     }

}
