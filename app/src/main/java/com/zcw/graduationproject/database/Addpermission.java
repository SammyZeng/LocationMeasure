package com.zcw.graduationproject.database;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.pm.ActivityInfoCompat;
import android.widget.Toast;

/**
 * Created by zcw on 2018/1/24.
 */

public class Addpermission {
    private Activity activity ;
    private boolean isWritePermission = true ;
    private boolean isLocationPermission =true ;
    private String[] LOCATIONPERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION  , Manifest.permission.ACCESS_COARSE_LOCATION} ;
    private String[] WRITEPERMISSION = {Manifest.permission.WRITE_EXTERNAL_STORAGE} ;

    public boolean isWritePermission() {
        return isWritePermission;
    }

    public boolean isLocationPermission() {
        return isLocationPermission;
    }

    public Addpermission(Activity activity){
        this.activity = activity ;
    }

    public void checkpermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int locationcheck = ContextCompat.checkSelfPermission(activity , LOCATIONPERMISSION[0]) ;
            if(locationcheck != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity , LOCATIONPERMISSION , 101);
                isLocationPermission = false ;
            }

            int writecheck = ContextCompat.checkSelfPermission(activity , WRITEPERMISSION[0]) ;
            if(writecheck != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity , WRITEPERMISSION , 102);
                isWritePermission = false ;
            }
        }
    }

    public void requestPermission(int requestCode , @NonNull String[] permission ,  @NonNull int[] grantResults){
        switch (requestCode){
            case 101 :
                if (null != grantResults && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(activity  , "获取位置权限失败" , Toast.LENGTH_LONG).show();
                    activity.finish();
                }else {
                    Toast.makeText(activity  , "获取位置权限成功" , Toast.LENGTH_LONG).show();
                    isLocationPermission = true ;
                }
                break;

            case 102 :
                if (null != grantResults && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(activity  , "获取读写权限失败" , Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(activity  , "获取读写权限成功" , Toast.LENGTH_LONG).show();
                    isWritePermission = true ;
                }
                break;
        }
    }
}
