package com.zcw.graduationproject.baidumap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.zcw.graduationproject.R;
import com.zcw.graduationproject.baidumodefile.LocationService;
import com.zcw.graduationproject.database.Addpermission;
import com.zcw.graduationproject.database.Coordinate;
import com.zcw.graduationproject.database.DataBaseContext;
import com.zcw.graduationproject.database.Line;
import com.zcw.graduationproject.database.MyDBHelper;
import com.zcw.graduationproject.database.Plane;
import com.zcw.graduationproject.database.Point;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

	MapView mapView; // 地图视图
	BaiduMap baiduMap; // 百度地图实例
	LocationService locService = null;  //定位服务实例
	String address = null; // 地址
	MyDBHelper dbHelper; // 数据库操作对象
	Coordinate drawCoordinate;// 绘制点坐标
	Coordinate trailCoordinate;// 轨迹点坐标
	List<Coordinate> coordinateList; // 点集合、用来线或者面以点的形式保存到坐标表
	List<Coordinate> trailList; // 轨迹点集合
	List<LatLng> drawLPList; // 用来绘制线或者面
	List<LatLng> drawTrialList; // 用来绘制轨迹
	LatLng searchLatLng = new LatLng(39.92235, 116.380338); // 当前点，传过去搜索页面做地图中心点
	String searchCity = "北京";  //搜索的城市，默认为当前所在城市

	// UI控件
	LinearLayout ll_operate;
	LinearLayout ll_search;
	LinearLayout ll_save;
	LinearLayout ll_property1;
	LinearLayout ll_property2;
	TextView tv_longitue;
	TextView tv_latitue;
	TextView tv_address;
	TextView tv_property1;
	TextView tv_property2;
	TextView tv_content1;
	TextView tv_content2;

	// 位置监听器实例
	MyLocationListenner myLocationListenner = new MyLocationListenner();
	LocationMode mCurrentMode; // 最近的位置模式：普通 、跟随、罗盘
	BitmapDescriptor bClickMarker; // 点击标志图标

	boolean isFirstLoc = true; // 是否首次定位
	boolean isContinuteLoc = true; // 是否持续记录轨迹
	boolean isOperating = false; // 是否在采集数据
	boolean isNeedShowTrail = true; // 是否需要显示轨迹信息
	boolean canSave = false; // 是否可以保存
	int clickMode = 0; // 地图点击模式，默认为0--无操作状态的点击，1为点，2为线，3为面

	private Addpermission addpermission ;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addpermission = new Addpermission(this) ;
		addpermission.checkpermission();
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		// SDKInitializer.initialize(getApplicationContext());
		// 已经在MyAplication.java中全局定义好了
		setContentView(R.layout.activity_main);
		// 初始化
		initWidget();

		if(addpermission.isLocationPermission()){
			initMap();
		}


	}

	//初始化控件
	private void initWidget(){
		// UI控件实例化
		tv_longitue = (TextView) findViewById(R.id.tv_longitude);
		tv_latitue = (TextView) findViewById(R.id.tv_latitude);
		tv_address = (TextView) findViewById(R.id.tv_address);
		tv_property1 = (TextView) findViewById(R.id.tv_property1);
		tv_property2 = (TextView) findViewById(R.id.tv_property2);
		tv_content1 = (TextView) findViewById(R.id.tv_content1);
		tv_content2 = (TextView) findViewById(R.id.tv_content2);

		ll_operate = (LinearLayout) findViewById(R.id.ll_operate);
		ll_search = (LinearLayout) findViewById(R.id.ll_search);
		ll_save = (LinearLayout) findViewById(R.id.ll_save);
		ll_property1 = (LinearLayout) findViewById(R.id.ll_property1);
		ll_property2 = (LinearLayout) findViewById(R.id.ll_property2);
		// 先隐藏属性1、2
		ll_property1.setVisibility(View.INVISIBLE);
		ll_property2.setVisibility(View.INVISIBLE);

		// 创建数据库辅助对象
		// dbHelper = new MyDBHelper(MainActivity.this, 1);
		DataBaseContext dbContext = new DataBaseContext(MainActivity.this);
		dbHelper = new MyDBHelper(dbContext, 1);

		coordinateList = new ArrayList<Coordinate>();
		trailList = new ArrayList<Coordinate>();
		drawLPList = new ArrayList<LatLng>();
		drawTrialList = new ArrayList<LatLng>();
	}

	/**
	 * 初始化地图
	 */

	public void initMap() {

		// 获取地图控件引用
		mapView = (MapView) findViewById(R.id.bmapView);
		baiduMap = mapView.getMap();
		// 地图定位模式为普通模式
		mCurrentMode = LocationMode.NORMAL;
		baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				mCurrentMode, true, null));

		locService = ((MyApplication) getApplication()).locationService;
		locService.registerListener(myLocationListenner);
		locService.start();
		baiduMap.setMyLocationEnabled(true);

		// 地图点击监听
		baiduMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			// 此方法就是点击地图监听
			public void onMapClick(LatLng latLng) {
				// TODO Auto-generated method stub
				mapClick(latLng);
			}
		});


	}

	/**
	 * 定位监听器
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// mapView销毁后不再处理新接收的位置
			if (location == null || mapView == null) {
				return;
			}
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			LatLng newLatLng = new LatLng(latitude, longitude);
			if (isNeedShowTrail) { // 没有在操作采集的时候，显示定位位置
				tv_longitue.setText(longitude + "");
				tv_latitue.setText(latitude + "");
			}
			if (isContinuteLoc) { // 记录轨迹的时候，往绘制图表中加数据
				drawTrialList.add(newLatLng);
				drawTrail(drawTrialList);
			}
			trailCoordinate = new Coordinate(latitude, longitude);
			try {
				trailCoordinate.getAddress(handler);
				trailCoordinate.getPlaneCoordinate(handler); // 获得平面坐标
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//定位的位置信息
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			baiduMap.setMyLocationData(locData);

			searchLatLng = newLatLng;
			searchCity = location.getCity();
			if (isFirstLoc) { // 首次定位，设置缩放级别
				isFirstLoc = !isFirstLoc;
				Toast.makeText(MainActivity.this, "记录轨迹", Toast.LENGTH_LONG).show();
				MapStatus.Builder builder = new MapStatus.Builder();
				builder.target(newLatLng).zoom(18.0f);// 设置地图中心点及缩放级别
				baiduMap.animateMapStatus(MapStatusUpdateFactory
						.newMapStatus(builder.build()));
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	/**
	 * 对地图点击之后需实现的功能
	 */
	public void mapClick(LatLng latLng) {
		canSave = false;
		switch (clickMode) {
			case 0: // 定位到当前位置
				LatLng lastPoint = new LatLng(trailCoordinate.getLatitude(),
						trailCoordinate.getLongitude());
				MapStatus.Builder builder = new MapStatus.Builder();
				builder.target(lastPoint);// 设置地图中心点
				baiduMap.animateMapStatus(MapStatusUpdateFactory
						.newMapStatus(builder.build()));
				break;
			case 1: // 点操作
				isOperating = true;
				bClickMarker = BitmapDescriptorFactory
						.fromResource(R.mipmap.icon_gcoding);
				MarkerOptions mo1 = new MarkerOptions().position(latLng)
						.icon(bClickMarker).zIndex(9).draggable(false); // 设置不可拖拽
				baiduMap.addOverlay(mo1);
				System.out.println("----点击分割线----");
				setEditText(latLng);
				drawCoordinate.getPlaneCoordinate(handler); // 获得平面坐标
				System.out.println("----------");
				break;
			case 2: // 线操作
				isOperating = true;
				// baiduMap.clear();
				System.out.println("----线点击分割线----");
				OverlayOptions ooDot = new DotOptions().center(latLng).radius(6)
						.color(0xFF00EE00);
				baiduMap.addOverlay(ooDot);
				// 每画一个点均显示它的坐标，且把它加进coordinateList中
				setEditText(latLng);
				drawCoordinate.getPlaneCoordinate(handler);

				// 添加普通折线绘制
				drawLPList.add(latLng);
				if (drawLPList.size() < 2) {
					return;
				}
				OverlayOptions ooPolyline = new PolylineOptions().width(10)
						.color(0xAAFF0000).points(drawLPList);
				baiduMap.addOverlay(ooPolyline);
				System.out.println("----------");
				break;
			case 3: // 面操作
				isOperating = true;
				// baiduMap.clear();
				System.out.println("----面点击分割线----");

				OverlayOptions ooDot2 = new DotOptions().center(latLng).radius(6)
						.color(0xFF00EE00);
				baiduMap.addOverlay(ooDot2);
				// 每画一个点均显示它的坐标，且把它加进coordinateList中
				setEditText(latLng);
				drawCoordinate.getPlaneCoordinate(handler);

				// 添加多边形
				drawLPList.add(latLng);
				if (drawLPList.size() < 2) {
					return;
				}
				if (drawLPList.size() == 2) {
					OverlayOptions ooPolyline2 = new PolylineOptions().width(10)
							.color(0xAA00FF00).points(drawLPList);
					baiduMap.addOverlay(ooPolyline2);
					return;
				}
				OverlayOptions ooPolygon = new PolygonOptions().points(drawLPList)
						.stroke(new Stroke(5, 0xAA00FF00)).fillColor(0xAAFFFF00);
				baiduMap.addOverlay(ooPolygon);
				System.out.println("----------");
				break;

			default:
				break;
		}
	}

	/**
	 * 多线程处理
	 */
	public final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

				case 101 : //百度地图获取权限成功后
					initMap();
					break;

				case 0: // 百度逆地理编码WebAPI获取地址
					try {
						if (isNeedShowTrail) { // 不操作采集数据的时候显示当前位置
							tv_address.setText(trailCoordinate.getAddress() + "");
							System.out.println(trailCoordinate.getAddress());
						} else { // 不然显示操作地址
							tv_address.setText(drawCoordinate.getAddress() + "");
							System.out.println(drawCoordinate.getAddress());
						}

					} catch (Exception e) {
						// TODO: handle exception
					}
					break;
				case 1: // 获取平面坐标成功
					canSave = true;
					System.out.println("canSave?" + canSave);
					if (isContinuteLoc) { // 如果是记录轨迹,则列表加上记录点
						trailList.add(trailCoordinate);
					}
					if (isOperating) { // 点、线或面操作，可以把坐标加进列表
						isOperating = false;
						coordinateList.add(drawCoordinate);
						System.out.println("coordinateList:"
								+ coordinateList.toString());
						switch (clickMode) {
							case 1:
								tv_content1.setText(coordinateList.size() + ""); // 设置点数
								break;
							case 2:
								Line drawLine = new Line(coordinateList);
								tv_content1.setText(drawLine.getLength() + " 米"); // 设置长度
								break;
							case 3:
								Plane plane = new Plane(coordinateList);
								tv_content1.setText(plane.getPerimeter() + " 米"); // 设置长度
								tv_content2.setText(plane.getArea() + " 平方米"); // 设置长度
								break;

							default:
								break;
						}
					}

					break;

				default:
					break;
			}

		};
	};


	/**
	 * 设置EditText
	 *
	 * @param latLng
	 */
	public void setEditText(LatLng latLng) {
		try {
			drawCoordinate = new Coordinate(latLng);
			drawCoordinate.getAddress(handler);
			// 获取经纬度
			double latitue = latLng.latitude; // 纬度
			double longitue = latLng.longitude; // 经度
			System.out.println("纬度：" + latitue + " 经度：" + longitue);
			tv_longitue.setText(longitue + "");
			tv_latitue.setText(latitue + "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(MainActivity.this, "出现异常", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 实时绘制轨迹
	 *
	 * @param trailPoints
	 */
	public void drawTrail(List<LatLng> trailPoints) {
		if (trailPoints.size() == 0) {
			return;
		}
		if (trailPoints.size() == 1) {
			OverlayOptions dotOpts = new DotOptions()
					.center(trailPoints.get(0)).radius(9).color(0xFF0000FF);
			baiduMap.addOverlay(dotOpts);
		} else {
			OverlayOptions lineOpts = new PolylineOptions().width(10)
					.color(0xFF0000FF).points(trailPoints);
			baiduMap.addOverlay(lineOpts);
		}
	}

	/**
	 * 控件监听
	 *
	 */

	public void doMain(View view) {
		switch (view.getId()) {
			case R.id.ll_operate:// 操作
				showPopupMenu(view);
				break;
			case R.id.ll_search:// 搜索
				Intent intent = new Intent();
				intent.putExtra("latitude", searchLatLng.latitude + "");
				intent.putExtra("longitude", searchLatLng.longitude + "");
				intent.putExtra("city", searchCity);
				intent.setClass(MainActivity.this, PoiSearchActivity.class);
				startActivity(intent);
				break;
			case R.id.ll_save:// 保存

				if(!addpermission.isWritePermission()){
					Toast.makeText(this , "为获取读写权限，保存失败" , Toast.LENGTH_LONG).show();
					return;
				}

				if (!canSave) { // 不能保存
					Toast.makeText(MainActivity.this, "不能保存,请有效操作!", Toast.LENGTH_LONG).show();
					return;
				}
				AlertDialog.Builder adb = new Builder(MainActivity.this);
				adb.setTitle("保存");
				adb.setMessage("请输入备注信息：");
				final EditText etNote = new EditText(MainActivity.this);
				adb.setView(etNote);
				adb.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (clickMode == 0) { // 保存轨迹
							Point point = new Point(trailList);
							point.insertPoint(dbHelper, etNote.getText() + "", 0);
							Point.queryPoint(dbHelper, 0);
							if (!isContinuteLoc) {
								trailList.clear();
								drawTrialList.clear();
								baiduMap.clear();
							}
						}
						if (clickMode == 1) { // 保存点
							Point point = new Point(coordinateList);
							point.insertPoint(dbHelper, etNote.getText() + "", 1);
						}
						if (clickMode == 2) { // 保存线
							Line line = new Line(coordinateList);
							line.insertLine(dbHelper, etNote.getText() + "");
							Line.queryLine(dbHelper);
							System.out.println("线的总长度：" + line.getLength());
						}
						if (clickMode == 3) { // 保存面
							Plane plane = new Plane(coordinateList);
							plane.insertPlane(dbHelper, etNote.getText() + "");
							Plane.queryPlane(dbHelper);
							System.out.println("面的周长：" + plane.getPerimeter());
							System.out.println("面的面积：" + plane.getArea());
						}
						Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_LONG).show();
						coordinateList.clear();
						drawLPList.clear();
					}
				});
				adb.setNegativeButton("取消", null);
				adb.show();
				break;

			default:
				break;
		}
	}

	/**
	 * 操作按键的下拉菜单
	 *
	 * @param view
	 */

	public void showPopupMenu(View view) {
		// View当前PopupMenu显示的相对View的位置
		PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
		// menu布局
		popupMenu.getMenuInflater().inflate(R.menu.operatemap,
				popupMenu.getMenu());
		if (isContinuteLoc) {
			popupMenu.getMenu().getItem(4).setTitle("取消记录轨迹");
		} else {
			popupMenu.getMenu().getItem(4).setTitle("记录轨迹");
		}

		// menu的item点击事件
		popupMenu
				.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						canSave = false;
						switch (item.getItemId()) {
							case R.id.action_point: // 采集点
								clickMode = 1;
								isNeedShowTrail = false;
								tv_property1.setText("点数:");
								tv_content1.setText("0");
								ll_property1.setVisibility(View.VISIBLE);
								ll_property2.setVisibility(View.INVISIBLE);
								baiduMap.clear(); // 清除地图标志
								coordinateList.clear();
								drawLPList.clear();
								break;
							case R.id.action_line: // 采集线
								clickMode = 2;
								isNeedShowTrail = false;
								tv_property1.setText("长度:");
								tv_content1.setText("0");
								ll_property1.setVisibility(View.VISIBLE);
								ll_property2.setVisibility(View.INVISIBLE);
								baiduMap.clear(); // 清除地图标志
								coordinateList.clear();
								drawLPList.clear();
								break;
							case R.id.action_face: // 采集面
								clickMode = 3;
								isNeedShowTrail = false;
								tv_property1.setText("周长:");
								tv_property2.setText("面积:");
								tv_content1.setText("0");
								tv_content2.setText("0");
								ll_property1.setVisibility(View.VISIBLE);
								ll_property2.setVisibility(View.VISIBLE);
								baiduMap.clear(); // 清除地图标志
								coordinateList.clear();
								drawLPList.clear();
								break;
							case R.id.action_check: // 查看

								if(!addpermission.isWritePermission()){
									Toast.makeText(MainActivity.this , "为获取读写权限，保存失败" , Toast.LENGTH_LONG).show();
									break;
								}
								isOperating = false;
								// clickMode = 0;
								Intent intent = new Intent();
								intent.setClass(MainActivity.this,
										CheckActivity.class);
								startActivity(intent);
								break;

							case R.id.action_location: // 记录轨迹
								isOperating = false;
								isContinuteLoc = !isContinuteLoc; // 点击之后更改状态
								// clickMode = 0;
								canSave = true;
								if (isContinuteLoc) {
									Toast.makeText(MainActivity.this, "记录轨迹", Toast.LENGTH_LONG)
											.show();
									clickMode = 0;
									locService
											.registerListener(myLocationListenner);
									locService.start();
								} else {
									Toast.makeText(MainActivity.this, "已取消记录轨迹",
											Toast.LENGTH_LONG).show();

									locService
											.unregisterListener(myLocationListenner);
									locService.stop();
									// 取消记录轨迹之后自动保存
									Date d = new Date();
									SimpleDateFormat sdf = new SimpleDateFormat(
											"yyyy年MM月dd日 HH:mm:ss");
									String note = "于 " + sdf.format(d) + " 结束记录轨迹";
									Point point = new Point(trailList);
									point.insertPoint(dbHelper, note, 0);
									Point.queryPoint(dbHelper, 0);
									trailList.clear();
									drawTrialList.clear();
									baiduMap.clear();
								}

								break;

							default:
								break;
						}
						drawTrail(drawTrialList); // 绘制轨迹
						return false;
					}
				});
		// PopupMenu关闭事件
		popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
			@Override
			public void onDismiss(PopupMenu menu) {
			}
		});
		// 显示下拉框
		popupMenu.show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		addpermission.requestPermission(requestCode , permissions , grantResults);
		handler.sendEmptyMessage(101) ;
	}

	@Override
	protected void onDestroy() {
		// 在activity执行onDestroy时执行mapView.onDestroy()，实现地图生命周期管理
		// 关闭定位图层
		baiduMap.setMyLocationEnabled(false);
		mapView.onDestroy();
		mapView = null;
		locService.unregisterListener(myLocationListenner);  //注销监听器
		locService.stop();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// 在activity执行onResume时执行mapView. onResume ()，实现地图生命周期管理
		mapView.onResume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		// 在activity执行onPause时执行mapView. onPause ()，实现地图生命周期管理
		mapView.onPause();
		super.onPause();
	}

}