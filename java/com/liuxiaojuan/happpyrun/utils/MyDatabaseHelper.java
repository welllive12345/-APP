package com.liuxiaojuan.happpyrun.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库类
 * 参考这个写你们自己的  跑步的记录的类 的数据库和表
 */
public class MyDatabaseHelper extends SQLiteOpenHelper{

	public static final String CREATE_SPORT_TABLE =
	"create table sportData ("+
			"id Integer," +//要求是整型值--0
			"startPoint text," +//1
			"startTime text," +//2
			"endPoint text," +//3
			"endTime text," +//4
			"duration text," +//5
			"distance text," +//6
			"calories text," +//7
			"speed text," +//8
			"distribution text," +//9
			"pathLinePoints text,"+//10
			"dateTag text)";//11




	private Context mContext;
	
	public MyDatabaseHelper(Context context) {
		super(context,"stepDemoData.db",null,1);
		mContext = context;
	}
	// 如果数据库不存在、则会执行，否者不会执行
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_SPORT_TABLE);
		// create other table
	}

	// 创建数据库不会执行，增大版本号才会执行
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 在这里可以把旧的表drop掉，从而创建新的表
	}
}
