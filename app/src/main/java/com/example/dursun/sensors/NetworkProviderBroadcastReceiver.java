package com.example.dursun.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class NetworkProviderBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG= "SensorsNetworkProvider";
    public NetworkProviderBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        boolean available=false;
        if(action.equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)){
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network[] allNetworks = cm.getAllNetworks();
            for (Network network: allNetworks) {
                NetworkInfo cmNetworkInfo = cm.getNetworkInfo(network);
                Log.d(TAG, "onReceive: NetworkInfo: "+cmNetworkInfo.getTypeName()+", "+cmNetworkInfo.isAvailable());
                if(cmNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                    available = cmNetworkInfo.isAvailable();
                }
            }

            Log.d(TAG, "onReceive: CONNECTIVITY_ACTION: "+ available);
        }else if(action.equalsIgnoreCase(Intent.ACTION_AIRPLANE_MODE_CHANGED)){
            boolean state = extras.getBoolean("state");
            Log.d(TAG, "onReceive: ACTION_AIRPLANE_MODE_CHANGED: "+ state);
        }
    }

    public void start(Context context){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        context.registerReceiver(this, intentFilter);
        Log.d(TAG, "start: ");
    }

    public void stop(Context context){
        context.unregisterReceiver(this);
        Log.d(TAG, "stop: ");
    }
}
