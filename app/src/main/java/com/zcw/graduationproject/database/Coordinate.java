package com.zcw.graduationproject.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 坐标
 *
 * @author Jimmy Du
 *
 */
public class Coordinate {
	private double latitude;
	private double longitude;
	private double xPoint;
	private double yPoint;
	private String address;

	public static boolean canSave = false; // 平面坐标转换成功之后会变成true
	public static String key = "q0MzyjZBm0HSTFTGDVPr9pvBz6mwm0jY"; // ak -->  根据当前的SHA1值和包名到百度地图开发官网申请
	// 安全码
	public static String mcode = "ED:5D:BD:B3:9F:90:6D:77:C0:CA:37:60:36:3A:66:04:0D:CD:39:64;com.zcw.graduationproject";

	public Coordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Coordinate(LatLng latLng) {
		this.latitude = latLng.latitude;
		this.longitude = latLng.longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getxPoint() {
		return xPoint;
	}

	public void setxPoint(double xPoint) {
		this.xPoint = xPoint;
	}

	public double getyPoint() {
		return yPoint;
	}

	public void setyPoint(double yPoint) {
		this.yPoint = yPoint;
	}

	public String getAddress() {
		if (address==null) {
			return "null";
		}
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}


	public static void queryCoordinate(MyDBHelper dbHelper) {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		// 获得数据库对象
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query("tab_coordinate", new String[] { "recordId",
				"address" }, null, null, null, null, null);
		while (cursor.moveToNext()) {
			String recordId = cursor.getString(cursor
					.getColumnIndex("recordId"));
			String address = cursor.getString(cursor
					.getColumnIndex("address"));
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("recordId", recordId);
			map.put("address", address);
			list.add(map);
		}
		System.out.println(list.toString());
	}

	@Override
	public String toString() {
		return "Coordinate [latitude=" + latitude + ", longitude=" + longitude
				+ ", xPoint=" + xPoint + ", yPoint=" + yPoint + ", address="
				+ address + "]";
	}



	/**
	 * 获取平面坐标系
	 */
	public void getPlaneCoordinate(final Handler handler) {
		new Thread() {
			public void run() {

				String url = "http://api.map.baidu.com/geoconv/v1/"
						+ "?coords=" + longitude + "," + latitude
						+ "&from=5&to=6" + "&mcode=" + mcode + "&ak=" + key;
//				System.out.println("获取平面坐标系的url:" + url);
				try {
					URL getUrl = new java.net.URL(url);
					HttpURLConnection urlConn2 = (HttpURLConnection) getUrl
							.openConnection();
					urlConn2.setDoOutput(true);
					urlConn2.setDoInput(true);
					urlConn2.disconnect();
					// System.out.println("getPlaneCoordinate()1");
					StringBuilder json = new StringBuilder();
					InputStreamReader in = new InputStreamReader(
							urlConn2.getInputStream());
					BufferedReader buffer = new BufferedReader(in);
					String inputLine = null;
					while (((inputLine = buffer.readLine()) != null)) {
						json.append(inputLine);
						// System.out.println("--" + json + "--");
						JSONObject res = new JSONObject(json + "");
						JSONArray result = res.getJSONArray("result");
						// 获取平面坐标x,y
						xPoint = result.getJSONObject(0).getDouble("x");
						yPoint = result.getJSONObject(0).getDouble("y");
//						System.out.println("百度坐标转换API获取平面坐标：" + xPoint + "/"
//								+ yPoint);
					}
					in.close();

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("百度坐标转换API获取平面坐标失败");
				} finally {
					// 转换成功之后进入多线程
					handler.obtainMessage(1).sendToTarget();
//					System.out.println("进入多线程1");
				}
			};
		}.start();
	}

	/**
	 * 获取地址
	 *
	 */
	public void getAddress(final Handler handler) throws IOException {
		new Thread() {
			public void run() {

				String url = "http://api.map.baidu.com/geocoder/v2/" + "?ak="
						+ key + "&mcode=" + mcode + "&callback=renderReverse"
						+ "&location=" + latitude + "," + longitude
						+ "&output=json";
//				System.out.println("获取百度逆地理编码WebAPI的url:" + url);
				try {
					URL getUrl = new java.net.URL(url);
					HttpURLConnection urlConn = (HttpURLConnection) getUrl
							.openConnection();
					urlConn.setDoOutput(true);
					urlConn.setDoInput(true);
					urlConn.disconnect();
					InputStreamReader in = new InputStreamReader(
							urlConn.getInputStream());
					BufferedReader buffer = new BufferedReader(in);
					String inputLine = null;
					while (((inputLine = buffer.readLine()) != null)) {
						String NewStr = inputLine.substring(
								inputLine.indexOf("(") + 1,
								inputLine.lastIndexOf(")"));
						// System.out.println("--" + NewStr + "--");
						JSONObject res = new JSONObject(NewStr);
						JSONObject result = res.getJSONObject("result");
						address = result.getString("formatted_address")
								+ result.getString("sematic_description")
								.toString();
					}
//					System.out.println("百度逆地理编码WebAPI获取地址：" + address);
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("百度逆地理编码WebAPI获取地址异常");
				} finally {
					handler.obtainMessage(0).sendToTarget();
				}
			};
		}.start();
	}

}
