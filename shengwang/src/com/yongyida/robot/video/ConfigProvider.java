package com.yongyida.robot.video;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.yongyida.robot.video.comm.log;

public class ConfigProvider extends ContentProvider {
	private static final String TAG = ConfigProvider.class.getSimpleName();
	
	private static final String AUTHORITY = "com.yongyida.robot.video.provider";
	private static final String DB_NAME = "config.db";
	private static final String TABLE_NAME = "config";
	private static final int DB_VERSION = 1;
	private static final String NAME_COL = "name";
	private static final String VLAUE_COL = "value";
	private static final String TABLE_CREATE = "create table " + TABLE_NAME + " (" + NAME_COL + " text primary key, "
			+ VLAUE_COL + " text not null);";
	private static final int CONFIG_ITEM = 1;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
	private static UriMatcher uriMatcher;
	private DBHelper dbHelper;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, TABLE_NAME, CONFIG_ITEM);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DBHelper(getContext(), DB_NAME, null, DB_VERSION);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (uriMatcher.match(uri) != CONFIG_ITEM) {
			throw new IllegalArgumentException("Error Uri: " + uri);
		}

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (uriMatcher.match(uri) != CONFIG_ITEM) {
			throw new IllegalArgumentException("Error Uri: " + uri);
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.insert(TABLE_NAME, null, values);
		return uri;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		if (uriMatcher.match(uri) != CONFIG_ITEM) {
			throw new IllegalArgumentException("Error Uri: " + uri);
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.update(TABLE_NAME, values, selection, selectionArgs);
	}

	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs) {
		if (uriMatcher.match(uri) != CONFIG_ITEM) {
			throw new IllegalArgumentException("Error Uri: " + uri);
		}
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(TABLE_NAME, whereClause, whereArgs);
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	private static class DBHelper extends SQLiteOpenHelper {
		public DBHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
	
	public static boolean exists(String name) {
		Cursor cursor = RobotApplication.getInstance().getContext().getContentResolver().query(ConfigProvider.CONTENT_URI, null,
				NAME_COL + "=?", new String[] { name }, null);
		return cursor.moveToFirst();
	}
	
	public static String query(String name) {
		String value = null;
		Cursor cursor = RobotApplication.getInstance().getContext().getContentResolver().query(ConfigProvider.CONTENT_URI, null,
				NAME_COL + "=?", new String[] { name }, null);
		try {
			if (cursor.moveToFirst())
				value = cursor.getString(cursor.getColumnIndex(VLAUE_COL));
		}
		catch (Exception e) {
			log.e(TAG, "Query error:" + e.getMessage());
		}
		finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return value;
	}

	public static void insert(String name, String value) {
		ContentValues values = new ContentValues();
		values.put(NAME_COL, name);
		values.put(VLAUE_COL, value);
		try {
			Uri row = RobotApplication.getInstance().getContext().getContentResolver().insert(ConfigProvider.CONTENT_URI,
					values);
			if (row == null)
				log.e(TAG, "ContentResolver insert failed");
		}
		catch (Exception e) {
			log.e(TAG, "insert config failed");
		}
	}
	
	public static int update(String name, String value) {
		ContentValues values = new ContentValues();
		values.put(VLAUE_COL, value);
		return RobotApplication.getInstance().getContext().getContentResolver().update(ConfigProvider.CONTENT_URI, values,
				NAME_COL + "=?", new String[] { name });
	}
	
	/**
	 * save
	 *   存在则更新，不存在则插入
	 * @param name
	 * @param value
	 * @return
	 */
	public static int save(String name, String value) {
		if (exists(name)) {
			return update(name, value);
		}
		else {
			insert(name, value);
			return 0;
		}
	}
}
