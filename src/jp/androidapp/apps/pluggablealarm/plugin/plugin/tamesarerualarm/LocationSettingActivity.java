/**
 * 
 */
package jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

/**
 * 位置情報取得Activity.
 */
public class LocationSettingActivity extends Activity implements LocationListener {

    /** TAG. */
    private String TAG = "LocationSetting";
    
    /** EXTRAS_LATITUDE. */
    public static final String EXTRAS_LATITUDE = "Latitude";
    /** EXTRAS_LONGITUDE. */
    public static final String EXTRAS_LONGITUDE = "Longitude";
    /** EXTRAS_ACCURACY. */
    public static final String EXTRAS_ACCURACY = "Accuracy";
    /** EXTRAS_ALTITUDE. */
    public static final String EXTRAS_ALTITUDE = "Altitude";
    /** EXTRAS_TIME. */
    public static final String EXTRAS_TIME = "Time";
    /** EXTRAS_SPEED. */
    public static final String EXTRAS_SPEED = "Speed";
    /** EXTRAS_BEARING. */
    public static final String EXTRAS_BEARING = "Bearing";
    
    /** LocationManager. */
    private LocationManager mLocationManager = null;

    /** 送信済みフラグ. */
    private boolean mSend = false;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.location_setting_main);

        // LocationManagerの取得
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 位置情報を取得していたらやめる
        removeLocationUpdates();
        mLocationManager = null;

    }

    @Override
    protected void onPause() {
        super.onPause();

        // pause状態になったら位置情報の取得を止める
        removeLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 位置情報の更新を取得する
        if (mLocationManager != null) {
            // NETWORK_PROVIDERから、即時に位置情報の取得を始める
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    /* (非 Javadoc)
     * @see android.location.LocationListener#onLocationChanged(android.location.Location)
     */
    @Override
    public void onLocationChanged(Location location) {

        // 情報は一度だけ送信する
        if(mSend){
            return;
        }
        
        Log.d(TAG, "----------");
        Log.d(TAG, "Latitude:" + String.valueOf(location.getLatitude()));
        Log.d(TAG, "Longitude:" + String.valueOf(location.getLongitude()));
        Log.d(TAG, "Accuracy:" + String.valueOf(location.getAccuracy()));
        Log.d(TAG, "Altitude:" + String.valueOf(location.getAltitude()));
        Log.d(TAG, "Time:" + String.valueOf(location.getTime()));
        Log.d(TAG, "Speed:" + String.valueOf(location.getSpeed()));
        Log.d(TAG, "Bearing:" + String.valueOf(location.getBearing()));
        
        // 入力データを返す
        Intent data = new Intent();
        data.putExtra(EXTRAS_LATITUDE, location.getLatitude());
        data.putExtra(EXTRAS_LONGITUDE, location.getLongitude());
        data.putExtra(EXTRAS_ACCURACY, location.getAccuracy());
        data.putExtra(EXTRAS_ALTITUDE, location.getAltitude());
        data.putExtra(EXTRAS_TIME, location.getTime());
        data.putExtra(EXTRAS_SPEED, location.getSpeed());
        data.putExtra(EXTRAS_BEARING, location.getBearing());

        // 一度だけ送信するようにガード
        mSend = true;

        setResult(RESULT_OK, data);
        finish();

    }

    /* (非 Javadoc)
     * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
     */
    @Override
    public void onProviderDisabled(String arg0) {
        // NOP
    }

    /* (非 Javadoc)
     * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled(String arg0) {
        // NOP
    }

    /* (非 Javadoc)
     * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.d("Status", "AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d("Status", "OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d("Status", "TEMPORARILY_UNAVAILABLE");
                break;
        }
    }
    
    /**
     * 位置情報の取得を止める.
     */
    private void removeLocationUpdates() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }



}
