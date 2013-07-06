package jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.util;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

public class LocationUpdater {
    private final LocationManager mLM;
    private final TextView mTextView;
    private OnDetactLocation mListener = null;

    /**
     * GPSまたは基地局電波やWiFiなどにより、現在地を検測する 現在地確認用TextView無し版
     * 
     * @param context
     */
    public LocationUpdater(Context context) {
        mLM = (LocationManager) context.getSystemService(Activity.LOCATION_SERVICE);
        mTextView = null;
        mListener = null;
    }

    public void setListener(OnDetactLocation listener) {
        mListener = listener;
    }

    /**
     * GPSまたは基地局電波やWiFiなどにより、現在地を検測する 現在地確認用TextViewあり版
     * 
     * @param context
     */
    public LocationUpdater(Context context,
                           TextView targetText) {
        mLM = (LocationManager) context.getSystemService(Activity.LOCATION_SERVICE);
        mTextView = targetText;
    }

    public void onResume() {
        long updateMinTime = 3000l; // 3sec
        float updateMinDist = 1.0f; // 1m
        mLM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, updateMinTime, updateMinDist, mGPSListener);
    }

    public void onPause() {
        mLM.removeUpdates(mGPSListener);
    }

    public Location getLocation() {
        Location gps = mLM.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (null != gps) {
            return gps;
        } else {
            return mLM.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
    }

    /**
     * GPSの状態更新用リスナー
     */
    private final LocationListener mGPSListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider,
                                    int status,
                                    Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            // TODO: 位置情報更新
            if (null != mTextView) {
                String m;
                m = "緯度:" + location.getLatitude() + " 経度:" + location.getLongitude() + " 精度:" + location.getAccuracy() + "m";
                mTextView.setText(m);
            }
            if (null != location && null != mListener) {
                // 位置情報を取得できた
                mListener.onDetect(location);
            }
        }
    };

    public interface OnDetactLocation {
        void onDetect(Location location);
    }
}
