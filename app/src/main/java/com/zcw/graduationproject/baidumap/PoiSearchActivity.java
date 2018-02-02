package com.zcw.graduationproject.baidumap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.zcw.graduationproject.R;
import com.zcw.graduationproject.baidumodefile.PoiOverlay;


import java.util.ArrayList;
import java.util.List;

/**
 * 演示poi搜索功能
 */
public class PoiSearchActivity extends FragmentActivity implements
		OnGetPoiSearchResultListener, OnGetSuggestionResultListener {

	// LocationService locService = null;

	private PoiSearch poiSearch = null;
	private SuggestionSearch suggestionSearch = null;
	private BaiduMap baiduMap = null;
	private MapView mapView; // 地图视图
	private List<String> suggest;

	/**
	 * 搜索关键字输入窗口
	 */
	private EditText editCity = null;
	private AutoCompleteTextView keyWorldsView = null;
	private ArrayAdapter<String> sugAdapter = null;
	private int loadIndex = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poisearch);
		Intent intent = getIntent();
		LatLng latLng = new LatLng(Double.parseDouble(intent
				.getStringExtra("latitude")), Double.parseDouble(intent
				.getStringExtra("longitude")));
		// 初始化搜索模块，注册搜索事件监听
		poiSearch = PoiSearch.newInstance();
		poiSearch.setOnGetPoiSearchResultListener(this);

		// 初始化建议搜索模块，注册建议搜索事件监听
		suggestionSearch = SuggestionSearch.newInstance();
		suggestionSearch.setOnGetSuggestionResultListener(this);

		editCity = (EditText) findViewById(R.id.city);
		keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
		sugAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line);
		keyWorldsView.setAdapter(sugAdapter);
		keyWorldsView.setThreshold(1);

		editCity.setText(intent.getStringExtra("city"));
		// 获取地图控件引用
		mapView = (MapView) findViewById(R.id.map);
		baiduMap = mapView.getMap();

		MapStatus.Builder builder = new MapStatus.Builder();
		builder.target(latLng).zoom(14.0f);// 设置地图中心点及缩放级别
		baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder
				.build()));

		/**
		 * 当输入关键字变化时，动态更新建议列表
		 */
		keyWorldsView.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
										  int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
									  int arg3) {
				if (cs.length() <= 0) {
					return;
				}
				/**
				 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
				 */
				suggestionSearch
						.requestSuggestion((new SuggestionSearchOption())
								.keyword(cs.toString()).city(
										editCity.getText().toString()));
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		poiSearch.destroy();
		suggestionSearch.destroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	/**
	 * 响应城市内搜索按钮点击事件
	 *
	 * @param v
	 */
	public void searchButtonProcess(View v) {
		String citystr = editCity.getText().toString();
		String keystr = keyWorldsView.getText().toString();
		poiSearch.searchInCity((new PoiCitySearchOption()).city(citystr)
				.keyword(keystr).pageNum(loadIndex));
	}

	public void goToNextPage(View v) {
		loadIndex++;
		searchButtonProcess(null);
	}

	/**
	 * 获取POI搜索结果
	 *
	 * @param result
	 */
	public void onGetPoiResult(PoiResult result) {
		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			Toast.makeText(PoiSearchActivity.this, "未找到结果", Toast.LENGTH_LONG)
					.show();
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			baiduMap.clear();
			PoiOverlay overlay = new MyPoiOverlay(baiduMap);
			baiduMap.setOnMarkerClickListener(overlay);
			overlay.setData(result);
			overlay.addToMap();
			overlay.zoomToSpan();
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
			// 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
			String strInfo = "在";
			for (CityInfo cityInfo : result.getSuggestCityList()) {
				strInfo += cityInfo.city;
				strInfo += ",";
			}
			strInfo += "找到结果";
			Toast.makeText(PoiSearchActivity.this, strInfo, Toast.LENGTH_LONG)
					.show();
		}
	}

	/**
	 * 获取POI详情搜索结果，得到searchPoiDetail返回的搜索结果
	 *
	 * @param result
	 */
	public void onGetPoiDetailResult(PoiDetailResult result) {
		if (result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(PoiSearchActivity.this, "抱歉，未找到结果",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(PoiSearchActivity.this,
					result.getName() + ": " + result.getAddress(),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

	}

	/**
	 * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
	 *
	 * @param res
	 */
	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		if (res == null || res.getAllSuggestions() == null) {
			return;
		}
		suggest = new ArrayList<String>();
		for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
			if (info.key != null) {
				suggest.add(info.key);
			}
		}
		sugAdapter = new ArrayAdapter<String>(PoiSearchActivity.this,
				android.R.layout.simple_dropdown_item_1line, suggest);
		keyWorldsView.setAdapter(sugAdapter);
		sugAdapter.notifyDataSetChanged();
	}

	private class MyPoiOverlay extends PoiOverlay {

		public MyPoiOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public boolean onPoiClick(int index) {
			super.onPoiClick(index);
			PoiInfo poi = getPoiResult().getAllPoi().get(index);
			poiSearch.searchPoiDetail((new PoiDetailSearchOption())
					.poiUid(poi.uid));
			return true;
		}
	}

	// 监听物理返回键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			PoiSearchActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
