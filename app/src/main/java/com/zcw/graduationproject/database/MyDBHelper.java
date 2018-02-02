package com.zcw.graduationproject.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库辅助类
 *
 * @author Jimmy Du
 *
 */

public class MyDBHelper extends SQLiteOpenHelper {

	public static String DB_Name = "db_zhd_map.db";

	// 点--RecordId、Type、Note
	public static String tab_Point = "create table if not exists tab_point("
			+ "recordId Integer primary key,type Integer,sum Integer, note varchar(100))";

	// 线--RecordId、Type、Length、Note
	public static String tab_Line = "create table if not exists tab_line("
			+ "recordId varchar(20) primary key, type Integer,"
			+ "length Real, note varchar(100))";

	// 面--RecordId、Type、perimeter、area、Note
	public static String tab_Face = "create table if not exists tab_face("
			+ "recordId varchar(20) primary key, type Integer,perimeter Real,"
			+ "area Real,note varchar(100))";

	// 坐标--Id、RecordId、Type、Latitude、Longitude、xPoint、yPoint、Address
	public static String tab_Coordinate = "create table if not exists tab_coordinate("
			+ "id Integer primary key,recordId varchar(20),type Integer, "
			+ "latitude text ,longitude text, xPoint Real ,"
			+ "yPoint Real,address varchar(200))";

	/**
	 * 构造方法
	 */
	public MyDBHelper(Context context, int version) {
		super(context, DB_Name, null, 1);
		// TODO Auto-generated constructor stub
	}

	public MyDBHelper(Context context, String name, CursorFactory factory,
					  int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 初次建表在这里执行
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(tab_Point);
		db.execSQL(tab_Line);
		db.execSQL(tab_Face);
		db.execSQL(tab_Coordinate);
		System.out.println("---创建表---");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
