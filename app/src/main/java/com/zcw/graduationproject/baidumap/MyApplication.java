package com.zcw.graduationproject.baidumap;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.zcw.graduationproject.baidumodefile.LocationService;


/**
 * 一定要在AndroidManifest.xml中声明这个类
 * 
 */
public class MyApplication extends Application {
	
	public LocationService locationService;

	@Override
	public void onCreate() {
		super.onCreate();
		// 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
		SDKInitializer.initialize(this);
		locationService = new LocationService(getApplicationContext());
	}

}