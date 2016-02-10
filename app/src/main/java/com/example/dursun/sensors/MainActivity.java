package com.example.dursun.sensors;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private String TAG;
    private int mSensorType;
    private LocationListener mLocationListener;
    private NetworkProviderBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getString(R.string.app_name);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();

        Context applicationContext = getApplicationContext();
        mSensorManager = (SensorManager) applicationContext.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        MenuItem camera = menu.getItem(0);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getOutputMediaFileUri(MEDIA_TYPE_IMAGE));
        camera.setIntent(cameraIntent);

        for (Sensor sensor : sensorList) {
            Intent intent = new Intent();
            intent.putExtra(getString(R.string.sensorType), sensor.getType());
            menu.add(sensor.getName()).setIcon(R.drawable.ic_menu_manage).setIntent(intent);
        }
        Intent highAccuracyIntent = new Intent();
        Criteria highAccuracyCriteria = new Criteria();
        highAccuracyCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        highAccuracyCriteria.setSpeedRequired(true);
        highAccuracyCriteria.setAltitudeRequired(true);
        highAccuracyIntent.putExtra("criteria", highAccuracyCriteria);
        menu.add("High Accuracy Location Info").setIcon(R.drawable.ic_menu_send).setIntent(highAccuracyIntent);

        Intent lowPowerIntent = new Intent();
        Criteria lowPowerCriteria = new Criteria();
        lowPowerCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        lowPowerCriteria.setPowerRequirement(Criteria.POWER_LOW);
        lowPowerIntent.putExtra("criteria", lowPowerCriteria);
        menu.add("Low Power Location Info").setIcon(R.drawable.ic_menu_share).setIntent(lowPowerIntent);

        Intent passiveLocationIntent = new Intent();
        passiveLocationIntent.putExtra("passive", true);
        menu.add("Passive Location Info").setIcon(R.drawable.ic_menu_slideshow).setIntent(passiveLocationIntent);

        MenuItem mi = menu.getItem(menu.size() - 1);
        mi.setTitle(mi.getTitle());

    }

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            String message;
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                message = "Image saved.";
//                Toast.makeText(this, "Image saved to:\n" +  data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                message = "Cancelled...";
            } else {
                message = "Image is not saved.";
            }

            Snackbar.make(findViewById(R.id.drawer_layout), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent = item.getIntent();
        mSensorType = intent.getIntExtra(getString(R.string.sensorType), 0);
        Criteria criteria = intent.getParcelableExtra("criteria");
        boolean passive = intent.getBooleanExtra("passive", false);
        cancelLocationSensor();
        cancelSensor();

        if (mSensorType != 0) {
            reclaimSensor();
        } else if (criteria != null) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = lm.getProviders(criteria, false);
            TextView contentText = (TextView) findViewById(R.id.contentText);
            StringBuilder sb = new StringBuilder();
            for (String providerName : providers) {
                enableLocationListener(lm, contentText, sb, providerName);
            }
            contentText.setText(sb.toString());
        } else if (passive) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, new MyLocationListener("passive", (TextView) findViewById(R.id.contentText)));
        } else {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void enableLocationListener(LocationManager lm, TextView contentText, StringBuilder sb, String providerName) {
        sb.append("Location Provider: ").append(providerName);
        lm.requestSingleUpdate(providerName, new MyLocationListener(providerName, contentText), null);
        mLocationListener = new MyLocationListener(providerName + "_Listener", contentText);
        lm.requestLocationUpdates(providerName, 0, 0, mLocationListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            sb.append("NO LOCATION INFO IS AVAILABLE");
        }
        if (confirmLocationProviderEnabled(lm, providerName)) {
            Location lastKnownLocation = lm.getLastKnownLocation(providerName);
            sb.append("Location INFO: ").append(MyLocationListener.locationToString(lastKnownLocation));
            mBroadcastReceiver = new NetworkProviderBroadcastReceiver();
            mBroadcastReceiver.start(this);
        }
    }

    private boolean confirmLocationProviderEnabled(LocationManager lm, String provider) {
        boolean confirmNetworkProviderEnabled = confirmNetworkProviderEnabled(lm, provider);
        boolean confirmAirplaneModeIsOff = confirmAirplaneModeIsOff();
        boolean confirmWifiAvailable = confirmWifiAvailable();
        Log.d(TAG, "confirmNetworkProviderEnabled: " + confirmNetworkProviderEnabled + ", confirmAirplaneModeIsOff:" + confirmAirplaneModeIsOff + ", confirmWifiAvailable:" + confirmWifiAvailable);
        return confirmNetworkProviderEnabled && confirmAirplaneModeIsOff && confirmWifiAvailable;
    }

    private boolean confirmWifiAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean available = false;

        Network[] allNetworks = cm.getAllNetworks();
        for (Network network : allNetworks) {
            NetworkInfo cmNetworkInfo = cm.getNetworkInfo(network);
            Log.d(TAG, "onReceive: NetworkInfo: " + cmNetworkInfo.getTypeName() + ", " + cmNetworkInfo.isAvailable());
            if (cmNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                available = cmNetworkInfo.isAvailable();
            }
        }

        if (!available) {
            showAlertDialog("WIFI is not available", "Please enable wifi");
        }
        return available;
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "DONE....");
            }
        };
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", listener);
        alertDialog.show();
    }

    private boolean confirmAirplaneModeIsOff() {
        boolean isOff = Settings.System.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 0;
        if (!isOff) {
            showAlertDialog("Airplane mode on", "Please turn off airplane mode");
        }
        return isOff;
    }

    private boolean confirmNetworkProviderEnabled(LocationManager lm, String provider) {

        boolean providerEnabled = lm.isProviderEnabled(provider);
        if (!providerEnabled) {
            showAlertDialog("Location Provider(" + provider + ") is not available", "Please enable location provider!");
        }
        return providerEnabled;
    }

    public void cancelLocationSensor() {
        if (mLocationListener != null) {
            Log.d(TAG, "Cancelling Active Location Listener: " + mLocationListener);
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location Listener is not activated.");
                return;
            }
            lm.removeUpdates(mLocationListener);
            mLocationListener = null;
            if (mBroadcastReceiver != null) {
                mBroadcastReceiver.stop(this);
                mBroadcastReceiver = null;
            }
        }
    }

    private void reclaimSensor() {
        if (mSensorType != 0) {
            Log.d(TAG, "Sensor Type:" + mSensorType);
            mSensor = mSensorManager.getDefaultSensor(mSensorType);
            Log.d(TAG, "Default Sensor :" + mSensor);
            if (mSensor == null) {
                List<Sensor> sensorList = mSensorManager.getSensorList(mSensorType);
                Log.d(TAG, "Available Sensors :" + sensorList);
                mSensor = sensorList.get(0);
            }

            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void cancelSensor() {
        Log.d(TAG, "Cancelling Sensor");
        if (mSensor != null) {
            mSensorManager.unregisterListener(this, mSensor);
            mSensor = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reclaimSensor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelSensor();
        cancelLocationSensor();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        TextView contentText = (TextView) findViewById(R.id.contentText);
        float[] values = sensorEvent.values;
        StringBuilder valStr = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            valStr.append(i + ": " + values[i]).append('\n');
        }
        contentText.setText(valStr);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, sensor.getName() + " accuracy change");
    }
}
