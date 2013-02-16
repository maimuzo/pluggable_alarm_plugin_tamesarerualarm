package jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm.util;

import jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm.model.WeatherModel;
import net.arnx.jsonic.JSON;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class WeatherAccessTask extends AsyncTask<String, Integer, WeatherModel> {
    private static final String TAG = "WeatherAccessTask";
    private WeatherUtil.OnAccessListener mListener;
    
    public WeatherAccessTask(Context context, WeatherUtil.OnAccessListener listener){
        super();
        mListener = listener;
    }
    
    /**
     * UIスレッドでのプログレス表示用
     */
    @Override
    protected void onPreExecute() {
    }
    
    /**
     * バックグラウンドでのスレッド処理用
     */
    @Override
    protected WeatherModel doInBackground(String... params) {
        Log.d(TAG, "enter doInBackground()");
        String url = params[0];
        if(url == null){
            Log.d(TAG, "must set URL!");
            RestfulClient client = new RestfulClient();
            try {
				String jsonstring = client.get(url, null);
				WeatherModel model = JSON.decode(jsonstring, WeatherModel.class);
				return model;
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
            return null;
        }
        try {
            Log.d(TAG, "url: " + url);
            
            return null;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    /**
     * UIスレッドでの処理用
     */
    @Override
    protected void onPostExecute(WeatherModel result) {
        Log.d(TAG, "enter onPostExecute() user_id:" + result);
        if(null != mListener){
        	mListener.onAccess(result);
        }
    }
}
