package com.example.dursun.sensors;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by dursun on 2/9/16.
 */
public class MyLocationListener implements LocationListener {
    private static final String TAG = "LOC";

    private String desc;
    private TextView tv;

    public MyLocationListener(String desc, TextView contentText) {
        this.desc = desc;
        this.tv = contentText;
    }



    @Override
    public void onLocationChanged(Location location) {
        String s = locationToString(location);

        Log.d(TAG, desc+" - My Location is: "+s);
        tv.append("\n"+ desc+" - My Location is: "+s);
    }

    @NonNull
    public static String locationToString(Location location) {
        if(location == null){
            return "NULL";
        }
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        float accuracy = location.getAccuracy();
        long time = location.getTime();
        StringBuilder s = new StringBuilder();
        s.append("Latitude: ").append(lat).append(", ");
        s.append("Longitude: ").append(lng).append(", ");
        s.append("Accuracy: ").append(accuracy).append(", ");
        s.append("Time: ").append(time).append(", ");
        return s.toString();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, "Location provider enabled : "+ s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, "Location provider disabled : "+ s);
    }
}
