package com.zcw.graduationproject.baidumap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zcw.graduationproject.R;
import com.zcw.graduationproject.database.MyDBHelper;

import java.text.DecimalFormat;

public class CoordinateDetailActivity extends Activity {
	private Context context;

	// UI控件
	private EditText et_recordId;
	private EditText et_note;
	private EditText et_address;
	private EditText et_longitude;
	private EditText et_latitude;
	private EditText et_x;
	private EditText et_y;
	private TextView tv_recordId;
	private ImageView iv_back;
	private LinearLayout ll_note;
	// 从上个页面传过来的item id、note
	private String recordId;
	private String note;
	private int type ;
	// 数据库辅助实例
	private MyDBHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coordinatedetail);
		init();
	}

	/**
	 * 初始化函数
	 */
	public void init() {
		context = CoordinateDetailActivity.this;
		et_recordId = (EditText) findViewById(R.id.et_recordId);
		et_note = (EditText) findViewById(R.id.et_note);
		et_address = (EditText) findViewById(R.id.et_address);
		et_longitude = (EditText) findViewById(R.id.et_longitude);
		et_latitude = (EditText) findViewById(R.id.et_latitude);
		et_x = (EditText) findViewById(R.id.et_x);
		et_y = (EditText) findViewById(R.id.et_y);
		tv_recordId = (TextView) findViewById(R.id.tv_recordId);
		iv_back = (ImageView) findViewById(R.id.iv_back);
		ll_note = (LinearLayout) findViewById(R.id.ll_note);
		//先判断传过来 的点类型
		Intent intent = getIntent();
		type = Integer.parseInt(intent.getStringExtra("pointType"));

		if (type==1||type==2 || type == 3) {
			ll_note.setVisibility(View.GONE);
			tv_recordId.setText("点的顺序:");
			String pointIndex = intent.getStringExtra("pointIndex");
			String index = pointIndex.substring(1, 2);

			String address = intent.getStringExtra("address");
			String latitude = intent.getStringExtra("latitude");
			String longitude = intent.getStringExtra("longitude");
			String xPoint = intent.getStringExtra("xPoint");
			String yPoint = intent.getStringExtra("yPoint");

			et_recordId.setEnabled(true);
			et_recordId.setText(index);
			et_recordId.setEnabled(false);

			et_address.setEnabled(true);
			et_address.setText(address);
			et_address.setEnabled(false);

			et_longitude.setEnabled(true);
			et_longitude.setText(latitude);
			et_longitude.setEnabled(false);

			et_latitude.setEnabled(true);
			et_latitude.setText(longitude);
			et_latitude.setEnabled(false);

			et_x.setEnabled(true);
			et_x.setText(xPoint);
			et_x.setEnabled(false);

			et_y.setEnabled(true);
			et_y.setText(yPoint);
			et_y.setEnabled(false);
		}

	}

	public void setEditText() {
		// 数据库
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		et_recordId.setEnabled(true);
		et_recordId.setText(recordId);
		et_recordId.setEnabled(false);
		et_note.setEnabled(true);
		et_note.setText(note);
		et_note.setEnabled(false);
		// 光标
		Cursor cursor = db.query("tab_coordinate", new String[] { "recordId", "latitude",
						"longitude", "xPoint", "yPoint", "address" }, "recordId=?",
				new String[] { recordId }, null, null, null);
		while (cursor.moveToNext()) {
			et_address.setEnabled(true);
			et_address.setText(cursor.getString(cursor
					.getColumnIndex("address")));
			et_address.setEnabled(false);
			et_longitude.setEnabled(true);
			et_longitude.setText(cursor.getString(cursor
					.getColumnIndex("longitude")));
			et_longitude.setEnabled(false);
			et_latitude.setEnabled(true);
			et_latitude.setText(cursor.getString(cursor
					.getColumnIndex("latitude")));
			et_latitude.setEnabled(false);

			DecimalFormat df = new DecimalFormat("0.000#");//最多保留几位小数，就用几个#，最少位就用0来确定
			double x = cursor.getDouble(cursor.getColumnIndex("xPoint"));
			double y = cursor.getDouble(cursor.getColumnIndex("yPoint"));
			String xString=df.format(x);
			String yString=df.format(y);
			et_x.setEnabled(true);
			et_x.setText(xString);
			et_x.setEnabled(false);
			et_y.setEnabled(true);
			et_y.setText(yString);
			et_y.setEnabled(false);
		}
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
}
