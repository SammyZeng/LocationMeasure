package com.zcw.graduationproject.baidumap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;


import com.zcw.graduationproject.R;
import com.zcw.graduationproject.database.DataBaseContext;
import com.zcw.graduationproject.database.Line;
import com.zcw.graduationproject.database.MyDBHelper;
import com.zcw.graduationproject.database.Plane;
import com.zcw.graduationproject.database.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查看数据库Activity
 *
 * @author Jimmy Du
 *
 */

public class CheckActivity extends Activity {
	// UI控件
	private LinearLayout ll_trail;
	private LinearLayout ll_point;
	private LinearLayout ll_line;
	private LinearLayout ll_pollygon;
	private ListView lv;

	private Context context;

	// 数据源
	private List<Map<String, Object>> list;
	// 数据库辅助实例
	private MyDBHelper dbHelper;
	// 列表适配器
	private SimpleAdapter sAdapter;

	private int kind = 0;
	private String recordId; // 点击item的id
	private String note; // 点击item的备注信息
	private String sum; // 点的点数
	private String length; // 线的长度
	private String perimeter;// 面的周长
	private String area; // 面的面积

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check);

		init();
		// 监听item点击事件
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
									int position, long arg3) {
				// TODO Auto-generated method stub
				Map<String, Object> map = (Map<String, Object>) arg0
						.getItemAtPosition(position);
				recordId = map.get("recordId").toString();
				note = map.get("note").toString();
				if (kind == 0|| kind == 1) { //把点数传过去
					sum = map.get("sum").toString();
				}
				if (kind == 2) { // 把线的长度传过去
					length = map.get("length").toString();
				}
				if (kind == 3) { // 把面的周长和面积传过去
					perimeter = map.get("perimeter").toString();
					area = map.get("area").toString();
				}
				itemClick();

			}
		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			// 长按进行删除
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
										   int position, long arg3) {
				// TODO Auto-generated method stub
				Map<String, Object> map = (Map<String, Object>) arg0
						.getItemAtPosition(position);
				recordId = map.get("recordId").toString();
				// 弹出删除对话框
				AlertDialog.Builder adb = new Builder(CheckActivity.this);
				adb.setTitle("是否删除该项？");
				adb.setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if (kind == 0) {
							Point.deletePoint(dbHelper, recordId);
						}
						if (kind == 1) {
							Point.deletePoint(dbHelper, recordId);
						}
						if (kind == 2) {
							Line.deleteLine(dbHelper, recordId);
						}
						if (kind == 3) {
							Plane.deletePoint(dbHelper, recordId);
						}
						initList();
					}
				});
				adb.setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub

					}
				});
				adb.show();
				return true;
			}
		});

	}

	/**
	 * 初始化函数
	 */
	public void init() {
		context = CheckActivity.this;
		ll_trail = (LinearLayout) findViewById(R.id.ll_trail);
		ll_point = (LinearLayout) findViewById(R.id.ll_point);
		ll_line = (LinearLayout) findViewById(R.id.ll_line);
		ll_pollygon = (LinearLayout) findViewById(R.id.ll_pollygon);
		// dbHelper = new MyDBHelper(context, 1);
		DataBaseContext dbContext = new DataBaseContext(context);
		dbHelper = new MyDBHelper(dbContext, 1);
		lv = (ListView) findViewById(R.id.lv);
		list = new ArrayList<Map<String, Object>>();
		ll_trail.setBackgroundColor(Color.parseColor("#AAAAAA"));
		initList();
	}

	/**
	 * 初始化 列表视图
	 */

	public void initList() {
		lv.setVisibility(View.VISIBLE);
		// 本地数据库
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		// 光标
		Cursor cursor;
		switch (kind) {
			case 0:
				list.clear();
				// type=0才是定位的坐标
				cursor = db.query("tab_point", new String[] { "recordId", "type",
								"note", "sum" }, "type=?", new String[] { "0" }, null,
						null, null);
				while (cursor.moveToNext()) {
					// 集合
					Map<String, Object> map = new HashMap<String, Object>();
					String recordId = cursor.getString(cursor
							.getColumnIndex("recordId"));
					String note = cursor.getString(cursor.getColumnIndex("note"));
					String sum = cursor.getString(cursor.getColumnIndex("sum"));
					map.put("recordId", recordId);
					map.put("note", note);
					map.put("sum", sum);
					list.add(map);
				}
				cursor.close();// 关闭结果集
				db.close();// 关闭数据库对象
				// 初始化适配器
				sAdapter = new SimpleAdapter(context, list, R.layout.item_point,
						new String[] { "recordId", "note" }, new int[] {
						R.id.tv_recordId, R.id.tv_note });
				lv.setAdapter(sAdapter);
				break;
			case 1: // 点表
				list.clear();
				// 初始化光标
				// type=1才是记录点的坐标
				cursor = db.query("tab_point", new String[] { "recordId", "type",
								"note", "sum" }, "type=?", new String[] { "1" }, null,
						null, null);
				while (cursor.moveToNext()) {
					// 集合
					Map<String, Object> map = new HashMap<String, Object>();
					String recordId = cursor.getString(cursor
							.getColumnIndex("recordId"));
					String note = cursor.getString(cursor.getColumnIndex("note"));
					String sum = cursor.getString(cursor.getColumnIndex("sum"));
					map.put("recordId", recordId);
					map.put("note", note);
					map.put("sum", sum);
					list.add(map);
				}
				cursor.close();// 关闭结果集
				db.close();// 关闭数据库对象
				// 初始化适配器
				sAdapter = new SimpleAdapter(context, list, R.layout.item_point,
						new String[] { "recordId", "note" }, new int[] {
						R.id.tv_recordId, R.id.tv_note });
				lv.setAdapter(sAdapter);
				break;
			case 2:// 线表
				list.clear();
				// 初始化光标
				// type=2才是记录线的点坐标
				cursor = db.query("tab_line", new String[] { "recordId", "type",
								"note", "length" }, "type=?", new String[] { "2" }, null,
						null, null);
				while (cursor.moveToNext()) {
					// 集合
					Map<String, Object> map = new HashMap<String, Object>();
					String recordId = cursor.getString(cursor
							.getColumnIndex("recordId"));
					String note = cursor.getString(cursor.getColumnIndex("note"));
					String length = cursor.getString(cursor
							.getColumnIndex("length"));
					map.put("recordId", recordId);
					map.put("note", note);
					map.put("length", length);
					list.add(map);
				}
				cursor.close();// 关闭结果集
				db.close();// 关闭数据库对象
				// 初始化适配器
				sAdapter = new SimpleAdapter(context, list, R.layout.item_point,
						new String[] { "recordId", "note" }, new int[] {
						R.id.tv_recordId, R.id.tv_note });
				lv.setAdapter(sAdapter);
				break;
			case 3: // 面表
				list.clear();
				// 初始化光标
				// type=3才是记录面的点坐标
				cursor = db.query("tab_face", new String[] { "recordId", "type",
								"note", "perimeter", "area" }, "type=?",
						new String[] { "3" }, null, null, null);
				while (cursor.moveToNext()) {
					// 集合
					Map<String, Object> map = new HashMap<String, Object>();
					String recordId = cursor.getString(cursor
							.getColumnIndex("recordId"));
					String note = cursor.getString(cursor.getColumnIndex("note"));
					String perimeter = cursor.getString(cursor
							.getColumnIndex("perimeter"));
					String area = cursor.getString(cursor.getColumnIndex("area"));
					map.put("recordId", recordId);
					map.put("note", note);
					map.put("perimeter", perimeter);
					map.put("area", area);
					System.out.println("map:" + map.toString());
					list.add(map);
				}
				cursor.close();// 关闭结果集
				db.close();// 关闭数据库对象
				// 初始化适配器
				sAdapter = new SimpleAdapter(context, list, R.layout.item_point,
						new String[] { "recordId", "note" }, new int[] {
						R.id.tv_recordId, R.id.tv_note });
				lv.setAdapter(sAdapter);
				break;

			default:
				break;
		}
	}

	/**
	 * 点击查看种类：定位、点线面
	 *
	 * @param view
	 */
	public void checkKind(View view) {
		switch (view.getId()) {
			case R.id.ll_trail:
				kind = 0;
				clearTab();
				ll_trail.setBackgroundColor(Color.parseColor("#AAAAAA"));
				initList();
				break;
			case R.id.ll_point:
				kind = 1;
				clearTab();
				ll_point.setBackgroundColor(Color.parseColor("#AAAAAA"));
				initList();
				break;

			case R.id.ll_line:
				kind = 2;
				clearTab();
				ll_line.setBackgroundColor(Color.parseColor("#AAAAAA"));
				initList();
				break;

			case R.id.ll_pollygon:
				kind = 3;
				clearTab();
				ll_pollygon.setBackgroundColor(Color.parseColor("#AAAAAA"));
				initList();
				break;

			default:
				break;
		}
	}

	/**
	 * 监听item点击
	 */

	public void itemClick() {
		switch (kind) {
			case 0:
				Intent intent0 = new Intent();
				intent0.putExtra("recordId", recordId);
				intent0.putExtra("note", note);
				intent0.putExtra("sum", sum);
				intent0.putExtra("type", "0");
				intent0.setClass(context, PointDetailActivity.class);
				startActivity(intent0);
				System.out.println("recordId:" + recordId + "---note:" + note
						+ "---sum:" + sum);
				break;
			case 1:

				Intent intent1 = new Intent();
				intent1.putExtra("recordId", recordId);
				intent1.putExtra("note", note);
				intent1.putExtra("sum", sum);
				intent1.putExtra("type", "1");
				intent1.setClass(context, PointDetailActivity.class);
				startActivity(intent1);
				System.out.println("recordId:" + recordId + "---note:" + note
						+ "---sum:" + sum);

				break;
			case 2:

				Intent intent2 = new Intent();
				intent2.putExtra("recordId", recordId);
				intent2.putExtra("note", note);
				intent2.putExtra("length", length);
				intent2.setClass(context, LineDetailActivity.class);
				startActivity(intent2);
				System.out.println("recordId:" + recordId + "---note:" + note
						+ "---length:" + length);
				break;
			case 3:
				Intent intent3 = new Intent();
				intent3.putExtra("recordId", recordId);
				intent3.putExtra("note", note);
				intent3.putExtra("perimeter", perimeter);
				intent3.putExtra("area", area);
				intent3.setClass(context, PlaneDetailActivity.class);
				startActivity(intent3);
				System.out.println("recordId:" + recordId + "---note:" + note
						+ "---perimeter:" + perimeter + "---area:" + area);
				break;

			default:
				break;
		}
	}

	/**
	 * 把颜色全设置为黑色
	 */
	public void clearTab() {
		ll_trail.setBackgroundColor(0);
		ll_point.setBackgroundColor(0);
		ll_line.setBackgroundColor(0);
		ll_pollygon.setBackgroundColor(0);
		lv.setVisibility(View.INVISIBLE);
	}

}
