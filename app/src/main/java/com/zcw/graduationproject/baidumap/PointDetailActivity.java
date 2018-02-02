package com.zcw.graduationproject.baidumap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import com.zcw.graduationproject.R;
import com.zcw.graduationproject.database.DataBaseContext;
import com.zcw.graduationproject.database.MyDBHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointDetailActivity extends Activity {
	private Context context;

	// UI控件
	private EditText et_recordId;
	private EditText et_note;
	private EditText et_sum;
	private TextView tv_title;
	private ImageView iv_title;
	private ImageView iv_back;
	private ListView lv;
	// 从上个页面传过来的item id、note、sum
	private String recordId;
	private String note;
	private String sum;
	private int type;

	private List<Map<String, Object>> list ;
	private SimpleAdapter sAdapter;
	// 数据库辅助实例
	private MyDBHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pointdetail);
		init();
		setEditText();
		//设置各点的点击监听,跳转到单点详情
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
									long arg3) {
				// TODO Auto-generated method stub
				System.out.println("1");
				Map<String , Object> map = (Map<String, Object>) arg0.getItemAtPosition(arg2);
				String pointIndex = map.get("pointIndex").toString();
				String address = map.get("address").toString();
				String latitude =map.get("latitude").toString();
				String longitude = map.get("longitude").toString();
				String xPoint = map.get("xPoint").toString();
				String yPoint = map.get("yPoint").toString();

				Intent intent = new Intent();
				String pointType = "1";
				intent.putExtra("pointType", pointType);
				intent.putExtra("pointIndex", pointIndex);
				intent.putExtra("address", address);
				intent.putExtra("latitude", latitude);
				intent.putExtra("longitude", longitude);
				intent.putExtra("xPoint", xPoint);
				intent.putExtra("yPoint", yPoint);
				intent.setClass(PointDetailActivity.this, CoordinateDetailActivity.class);
				startActivity(intent);
				System.out.println("1");
			}
		});
	}

	/**
	 * 初始化函数
	 */
	public void init() {
		context = PointDetailActivity.this;
		et_recordId = (EditText) findViewById(R.id.et_recordId);
		et_note = (EditText) findViewById(R.id.et_note);
		et_sum = (EditText) findViewById(R.id.et_sum);
		tv_title = (TextView) findViewById(R.id.tv_title);
		iv_title = (ImageView) findViewById(R.id.iv_title);
		iv_back = (ImageView) findViewById(R.id.iv_back);
		lv = (ListView) findViewById(R.id.lv);
		list = new ArrayList<Map<String,Object>>();
		Intent intent = getIntent();
		recordId = intent.getStringExtra("recordId");
		note = intent.getStringExtra("note");
		sum = intent.getStringExtra("sum");
		type = Integer.parseInt(intent.getStringExtra("type"));
		if (type == 0) { //type=0，则为轨迹详情页面
			iv_title.setImageResource(R.mipmap.icon_trail);
			tv_title.setText("轨迹详情");
		}
//		dbHelper = new MyDBHelper(context, 1);
		DataBaseContext dbContext = new DataBaseContext(context);
		dbHelper = new MyDBHelper(dbContext, 1);

	}

	/**
	 * 设置EditText
	 */
	public void setEditText() {
		// 数据库
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		et_recordId.setEnabled(true);
		et_recordId.setText(recordId);
		et_recordId.setEnabled(false);
		et_note.setEnabled(true);
		et_note.setText(note);
		et_note.setEnabled(false);
		et_sum.setEnabled(true);
		et_sum.setText(sum+"");
		et_sum.setEnabled(false);
		// 光标
		Cursor cursor = db.query("tab_coordinate", new String[] { "recordId", "latitude",
						"longitude", "xPoint", "yPoint", "address" }, "recordId=?",
				new String[] { recordId }, null, null, null);
		int i =0;
		while (cursor.moveToNext()) {
			i++;
			// 集合
			Map<String, Object> map = new HashMap<String, Object>();
			String address = cursor.getString(cursor
					.getColumnIndex("address"));
			String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
			String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
			DecimalFormat df = new DecimalFormat("0.000#");//最多保留几位小数，就用几个#，最少位就用0来确定
			double x = cursor.getDouble(cursor.getColumnIndex("xPoint"));
			double y = cursor.getDouble(cursor.getColumnIndex("yPoint"));
			String xPoint=df.format(x);
			String yPoint=df.format(y);
			//把点的具体信息都封装起来
			map.put("pointIndex", "点"+i+":");
			map.put("note", note);
			map.put("address", address);
			map.put("latitude", latitude);
			map.put("longitude", longitude);
			map.put("xPoint", xPoint);
			map.put("yPoint", yPoint);

			System.out.println("map:" + map.toString());
			list.add(map);
		}
		cursor.close();// 关闭结果集
		db.close();// 关闭数据库对象
		// 初始化适配器
		sAdapter = new SimpleAdapter(context, list, R.layout.item_pointofline,
				new String[] { "pointIndex", "address" }, new int[] {
				R.id.tv_pointIndex, R.id.tv_address });
		lv.setAdapter(sAdapter);
		cursor.close();
		db.close();
	}

	/**
	 * 返回键
	 * @param view
	 */
	public void back(View view){
		iv_back.setBackgroundColor(Color.parseColor("#AAAAAA"));
		finish();
	}

	/**
	 * 在地图上显示详情
	 * @param view
	 */
	public void showOnMap(View view) {
		Intent intent = new Intent();
		intent.putExtra("recordId", recordId);
		intent.putExtra("type", type+"");
		intent.setClass(context, ShowOnMapActivity.class);
		startActivity(intent);
	}

}
