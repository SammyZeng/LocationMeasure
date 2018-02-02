package com.zcw.graduationproject.baidumap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.zcw.graduationproject.R;
import com.zcw.graduationproject.database.DataBaseContext;
import com.zcw.graduationproject.database.MyDBHelper;


import java.util.ArrayList;
import java.util.List;

public class ShowOnMapActivity extends Activity {
	private Context context;
	private BaiduMap baiduMap = null;
	private MapView mapView; // 地图视图
	private MyDBHelper dbHelper;
	private List<LatLng> latlngList; // 从数据库读取数据存到列表里
	private List<String> addressList; // 地址列表
	private String recordId;
	private int type;
	private ImageView iv_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showonmap);
		init();
		show();
	}

	/**
	 * 初始化函数
	 */
	public void init() {
		context = ShowOnMapActivity.this;
		iv_back = (ImageView) findViewById(R.id.iv_back);
		// 获取地图控件引用
		mapView = (MapView) findViewById(R.id.bmapView);
		baiduMap = mapView.getMap();
		// 读取PointActivity传过来的数据
		Intent intent = getIntent();
		recordId = intent.getStringExtra("recordId");
		type = Integer.parseInt(intent.getStringExtra("type"));
		DataBaseContext dbContext = new DataBaseContext(context);
		dbHelper = new MyDBHelper(dbContext, 1);
		latlngList = new ArrayList<LatLng>();
		addressList = new ArrayList<String>();
		System.out.println("recordId:" + recordId + "/type:" + type);
		curryData();
	}

	/**
	 * 遍历数据库得到数据存在latlngList和addressList中
	 */
	public void curryData() {
		// 数据库
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		// 光标
		Cursor cursor = db.query("tab_coordinate", new String[] { "recordId",
						"latitude", "longitude", "xPoint", "yPoint", "address" },
				"recordId=?", new String[] { recordId }, null, null, null);
		try {
			while (cursor.moveToNext()) {
				// 集合
				String address = cursor.getString(cursor
						.getColumnIndex("address"));
				String latitude = cursor.getString(cursor
						.getColumnIndex("latitude"));
				String longitude = cursor.getString(cursor
						.getColumnIndex("longitude"));
				System.out.println(latitude + "/" + longitude);
				// 把点的具体信息都封装起来
				LatLng latLng = new LatLng(Double.parseDouble(latitude),
						Double.parseDouble(longitude));
				latlngList.add(latLng);
				addressList.add(address);
				System.out.println("3");
			}
			cursor.close();// 关闭结果集
			db.close();// 关闭数据库对象
			System.out.println("查询表成功:" + latlngList.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/**
	 * 展示地图
	 */
	public void show() {
		// 已列表的中间顺序的数为中心
		int length = latlngList.size();
		LatLng centerPoint = latlngList.get(length / 2);
		MapStatus.Builder builder = new MapStatus.Builder();
		builder.target(centerPoint).zoom(18.0f);// 设置地图中心点及缩放等级
		baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder
				.build()));

		if (type == 0) { // 绘制轨迹
			if (latlngList.size() == 0) {
				return;
			}
			if (latlngList.size() == 1) {
				OverlayOptions dotOpts = new DotOptions()
						.center(latlngList.get(0)).radius(9).color(0xFF0000FF);
				baiduMap.addOverlay(dotOpts);
			} else {
				OverlayOptions lineOpts = new PolylineOptions().width(10)
						.color(0xFF0000FF).points(latlngList);
				baiduMap.addOverlay(lineOpts);
			}
		}
		if (type == 1) { // 点详情，在地图上添加Marker
			BitmapDescriptor bdIcon = BitmapDescriptorFactory
					.fromResource(R.mipmap.icon_gcoding);
			for (int i = 0; i < latlngList.size(); i++) {
				LatLng latLng = latlngList.get(i);
				MarkerOptions mo1 = new MarkerOptions().position(latLng)
						.icon(bdIcon).zIndex(9).draggable(false); // 设置不可拖拽,设置标题地址
				Marker marker = (Marker) baiduMap.addOverlay(mo1);
				marker.setTitle(addressList.get(i));
			}

			OnMarkerClickListener markerClickListener = new OnMarkerClickListener() {

				@Override
				public boolean onMarkerClick(Marker marker) {
					Toast toast = Toast.makeText(context, marker.getTitle(),
							Toast.LENGTH_LONG);
					toast.show();
					return false;
				}
			};
			baiduMap.setOnMarkerClickListener(markerClickListener);
		}
		if (type == 2) { // 线详情
			if (latlngList.size() == 0) {
				return;
			}
			if (latlngList.size() == 1) {
				OverlayOptions dotOpts = new DotOptions()
						.center(latlngList.get(0)).radius(9).color(0xFF0000FF);
				baiduMap.addOverlay(dotOpts);
			} else {
				OverlayOptions lineOpts = new PolylineOptions().width(10)
						.color(0xAAFF0000).points(latlngList);
				baiduMap.addOverlay(lineOpts);
			}
		}
		if (type == 3) { // 面详情
			if (latlngList.size() < 3) {
				return;
			}
			OverlayOptions ooPolygon = new PolygonOptions().points(latlngList)
					.stroke(new Stroke(5, 0xAA00FF00)).fillColor(0xAAFFFF00);
			baiduMap.addOverlay(ooPolygon);
		}
	}

	/**
	 * 返回上一级键
	 *
	 * @param view
	 */
	public void back(View view) {
		iv_back.setBackgroundColor(Color.parseColor("#AAAAAA"));
		finish();
	}

	/**
	 * 调回到主页面，并且主页面的原状态不变。
	 *
	 * @param view
	 */
	public void backMainMenu(View view) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

}
