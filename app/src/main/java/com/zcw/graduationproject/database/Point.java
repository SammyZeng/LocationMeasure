package com.zcw.graduationproject.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 点
 *
 * @author Jimmy Du
 *
 */
public class Point {
	private double sum;
	private List<Coordinate> coordinateList;

	public Point(List<Coordinate> coordinateList) {
		super();
		this.coordinateList = coordinateList;
		this.sum = coordinateList.size();
	}

	public double getSum() {
		return sum;
	}

	public List<Coordinate> getCoordinateList() {
		return coordinateList;
	}

	/**
	 * 往数据库增加点
	 *
	 * @param dbHelper
	 * @param note
	 */

	public void insertPoint(MyDBHelper dbHelper, String note, int type) {
		// 根据时间串来生成记录id
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String recordId = sdf.format(d);
		try {
			// 获取数据库对象
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			// 往点表添加数据
			ContentValues values = new ContentValues();
			values.put("recordId", recordId);
			values.put("sum", sum);
			// type=0是记录定位的坐标,type=1是记录点保存
			if (type == 0) {
				values.put("type", 0);
			} else if (type == 1) {
				values.put("type", 1);
			} else {
				System.out.println("输入类型有误");
				return;
			}
			values.put("note", note);
			db.insert("tab_point", null, values);
			db.close();
			db = dbHelper.getWritableDatabase();
			// 往坐标表添加数据
			for (int i = 0; i < coordinateList.size(); i++) {
				ContentValues values2 = new ContentValues();
				values2.put("recordId", recordId);
				values2.put("type", type);
				values2.put("latitude", coordinateList.get(i).getLatitude());
				values2.put("longitude", coordinateList.get(i).getLongitude());
				values2.put("xPoint", coordinateList.get(i).getxPoint());
				values2.put("yPoint", coordinateList.get(i).getyPoint());
				values2.put("address", coordinateList.get(i).getAddress());
				db.insert("tab_coordinate", null, values2);
			}
			db.close();
			System.out.println("添加点成功");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 查询点
	 *
	 * @param dbHelper
	 * @param type
	 */
	public static void queryPoint(MyDBHelper dbHelper, int type) {
		// 获得数据库对象
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		// type=0是查询定位的坐标,type=1是查询点保存
		Cursor cursor;
		if (type == 0 || type == 1) {
			cursor = db.query("tab_point", new String[] { "recordId", "type",
							"note", "sum" }, "type=?", new String[] { type + "" },
					null, null, null);
		} else {
			System.out.println("输入类型有错");
			return;
		}
		while (cursor.moveToNext()) {
			String recordId = cursor.getString(cursor
					.getColumnIndex("recordId"));
			String sum = cursor.getString(cursor.getColumnIndex("sum"));
			String note = cursor.getString(cursor.getColumnIndex("note"));
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
						+ "点数：" + sum + "/" + latitude + "/" + longitude + "/"
						+ xPoint + "/" + yPoint + "/" + address);
			}
			cursor2.close();
		}
		cursor.close();// 关闭结果集
		db.close();// 关闭数据库对象
		System.out.println("查询成功");
	}

	/**
	 * 删除点
	 *
	 * @param dbHelper
	 * @param recordId
	 */
	public static void deletePoint(MyDBHelper dbHelper, String recordId) {
		// 获得数据库对象
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		db.delete("tab_point", "recordId=?", new String[] { recordId });
		db.delete("tab_coordinate", "recordId=?", new String[] { recordId });
	}

}
