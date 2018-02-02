package com.zcw.graduationproject.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Plane {
	private double perimeter;
	private double area;
	private List<Coordinate> pointList;

	public Plane(List<Coordinate> pointList) {
		super();
		this.pointList = pointList;
		this.perimeter = countPerimeter();
		this.area = polygonArea();
	}


	public List<Coordinate> getPointList() {
		return pointList;
	}



	public double getPerimeter() {
		return perimeter;
	}

	public double getArea() {
		return area;
	}

	/**
	 * 计算多边形的周长
	 *
	 * @return
	 */

	private double countPerimeter() {
		double length = 0;
		int N = pointList.size();
		if (N < 3) {
			System.out.println("不能构成多边形");
			return 0;
		}
		for (int i = 0; i < N; i++) {
			double x = pointList.get((i + 1) % N).getxPoint()
					- pointList.get(i).getxPoint();
			double y = pointList.get((i + 1) % N).getyPoint()
					- pointList.get(i).getyPoint();
			length += Math.hypot(x, y);
		}
		BigDecimal bd = new BigDecimal(length);
		BigDecimal result = bd.setScale(4, RoundingMode.DOWN);
		length = result.doubleValue();
		return length;
	}

	/**
	 * 计算多边形的面积
	 *
	 * @return
	 */

	private double polygonArea() {
		double s = 0;
		int N = pointList.size();
		if (N < 3) {
			System.out.println("不能构成多边形");
			return 0;
		}
		System.out.println("N:" + N);
		for (int i = 0; i < N; i++) {
			s += pointList.get(i).getxPoint()
					* pointList.get((i + 1) % N).getyPoint()
					- pointList.get((i + 1) % N).getxPoint()
					* pointList.get(i).getyPoint();

		}
		BigDecimal bd = new BigDecimal(s);
		BigDecimal result = bd.setScale(4, RoundingMode.DOWN);
		s = result.doubleValue();
		System.out.println("s--finally:" + s);
		return Math.abs(s / 2);
	}

	/**
	 * 往数据库增加面
	 *
	 * @param dbHelper
	 * @param note
	 */

	public void insertPlane(MyDBHelper dbHelper, String note) {
		// 根据时间串来生成记录id
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String recordId = sdf.format(d);

		// 获取数据库对象
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// 往面表添加数据
		ContentValues values = new ContentValues();
		values.put("recordId", recordId);
		values.put("type", 3);
		values.put("note", note);
		values.put("perimeter", perimeter);
		values.put("area", area);
		db.insert("tab_face", null, values);
		db.close();
		db = dbHelper.getWritableDatabase();
		// 往坐标表添加数据
		for (int i = 0; i < pointList.size(); i++) {
			ContentValues values2 = new ContentValues();
			values2.put("recordId", recordId);
			values2.put("type", 3);
			values2.put("latitude", pointList.get(i).getLatitude());
			values2.put("longitude", pointList.get(i).getLongitude());
			values2.put("xPoint", pointList.get(i).getxPoint());
			values2.put("yPoint", pointList.get(i).getyPoint());
			values2.put("address", pointList.get(i).getAddress());
			db.insert("tab_coordinate", null, values2);
		}
		db.close();
		System.out.println("添加面成功");
	}

	public static void queryPlane(MyDBHelper dbHelper) {
		// 获得数据库对象
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		// type=3是记录面的各点的坐标
		Cursor cursor = db.query("tab_face", new String[] { "recordId", "type",
						"note", "perimeter","area" }, "type=?", new String[] { "3" }, null, null,
				null);
		while (cursor.moveToNext()) {
			String recordId = cursor.getString(cursor
					.getColumnIndex("recordId"));
			String note = cursor.getString(cursor.getColumnIndex("note"));
			String perimeter = cursor.getString(cursor.getColumnIndex("perimeter"));
			String area = cursor.getString(cursor.getColumnIndex("area"));

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
				System.out.println(latitude + "/" + longitude + "/"
						+ xPoint + "/" + yPoint + "/" + address);
			}
			cursor2.close();
			System.out.println("查询面啦---:" + note + "/" + recordId + "/"
					+ perimeter+"/"+area);
		}
		cursor.close();// 关闭结果集
		db.close();// 关闭数据库对象
		System.out.println("查询成功");
	}

	/**
	 * 删除面
	 * @param dbHelper
	 * @param recordId
	 */
	public static void deletePoint(MyDBHelper dbHelper ,String recordId) {
		// 获得数据库对象
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		db.delete("tab_face", "recordId=?", new String[] {recordId});
		db.delete("tab_coordinate", "recordId=?", new String[] {recordId});
	}

}
