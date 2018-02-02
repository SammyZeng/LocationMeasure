package com.zcw.graduationproject.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Line {
	private double length;
	private List<Coordinate> pointList;

	public Line(List<Coordinate> pointList) {
		super();
		this.pointList = pointList;
		this.length = countLength();
	}

	public List<Coordinate> getPointList() {
		return pointList;
	}

	public double getLength() {
		return length;
	}

	/**
	 * 计算长度
	 *
	 * @return
	 */
	private double countLength() {
		length = 0;
		for (int i = 0; i < pointList.size() - 1; i++) {
			double x = pointList.get(i + 1).getxPoint()
					- pointList.get(i).getxPoint();
			double y = pointList.get(i + 1).getyPoint()
					- pointList.get(i).getyPoint();
			length += Math.hypot(x, y);
		}
		BigDecimal bd = new BigDecimal(length);
		BigDecimal result = bd.setScale(2, RoundingMode.DOWN);
		length = result.doubleValue();
		return length;
	}

	/**
	 * 往数据库增加线
	 *
	 * @param dbHelper
	 * @param note
	 */

	public void insertLine(MyDBHelper dbHelper, String note) {
		// 根据时间串来生成记录id
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String recordId = sdf.format(d);

		// 获取数据库对象
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// 往线表添加数据
		ContentValues values = new ContentValues();
		values.put("recordId", recordId);
		values.put("type", 2);
		values.put("note", note);
		values.put("length", length);
		db.insert("tab_line", null, values);
		db.close();
		db = dbHelper.getWritableDatabase();
		// 往坐标表添加数据
		for (int i = 0; i < pointList.size(); i++) {
			ContentValues values2 = new ContentValues();
			values2.put("recordId", recordId);
			values2.put("type", 2);
			values2.put("latitude", pointList.get(i).getLatitude());
			values2.put("longitude", pointList.get(i).getLongitude());
			values2.put("xPoint", pointList.get(i).getxPoint());
			values2.put("yPoint", pointList.get(i).getyPoint());
			values2.put("address", pointList.get(i).getAddress());
			db.insert("tab_coordinate", null, values2);
		}
		db.close();
		System.out.println("添加线成功");
	}

	public static void queryLine(MyDBHelper dbHelper) {
		// 获得数据库对象
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		// type=2是记录线的各点的坐标
		Cursor cursor = db.query("tab_line", new String[] { "recordId", "type",
						"note", "length" }, "type=?", new String[] { "2" }, null, null,
				null);
		while (cursor.moveToNext()) {
			String recordId = cursor.getString(cursor
					.getColumnIndex("recordId"));
			String note = cursor.getString(cursor.getColumnIndex("note"));
			String length = cursor.getString(cursor.getColumnIndex("length"));
			// 通过recordId来查询具体点的信息
			Cursor cursor2 = db.query("tab_coordinate", new String[] {
							"recordId", "type", "latitude", "longitude", "xPoint",
							"yPoint", "address" }, "recordId=?",
					new String[] { recordId }, null, null, null);
			while (cursor2.moveToNext()) {
				double latitude = cursor2.getDouble(cursor2
						.getColumnIndex("latitude"));
				double longitude = cursor2.getDouble(cursor2
						.getColumnIndex("longitude"));
				double xPoint = cursor2.getDouble(cursor2
						.getColumnIndex("xPoint"));
				double yPoint = cursor2.getDouble(cursor2
						.getColumnIndex("yPoint"));
				String address = cursor2.getString(cursor2
						.getColumnIndex("address"));
				System.out.println("查询啦---:" + note + "/" + recordId + "/"
						+ "长度：" + length + latitude + "/" + longitude + "/"
						+ xPoint + "/" + yPoint + "/" + address);
			}
			cursor2.close();
		}
		cursor.close();// 关闭结果集
		db.close();// 关闭数据库对象
		System.out.println("查询成功");
	}


	/**
	 * 删除线
	 * @param dbHelper
	 * @param recordId
	 */
	public static void deleteLine(MyDBHelper dbHelper , String recordId) {
		// 获得数据库对象
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		db.delete("tab_line", "recordId=?", new String[] {recordId});
		db.delete("tab_coordinate", "recordId=?", new String[] {recordId});
	}

}
