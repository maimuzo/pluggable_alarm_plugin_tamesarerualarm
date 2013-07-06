package jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.util;

import jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.model.WeatherModel;
import jp.androidapp.libs.pluggablealarm.Log;
import net.arnx.jsonic.JSON;
import android.content.Context;
import android.os.AsyncTask;

public class WeatherAccessTask
    extends AsyncTask<String, Integer, WeatherModel> {
    private static final String TAG = "WeatherAccessTask";
    private final WeatherUtil.OnAccessListener mListener;

    public WeatherAccessTask(Context context,
                             WeatherUtil.OnAccessListener listener) {
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
        if (url == null) {
            Log.d(TAG, "must set URL!");
            return null;
        } else {
            Log.d(TAG, "url: " + url);
            RestfulClient client = new RestfulClient();
            try {
                String jsonstring = client.get(url, null);
                Log.d(TAG, "json string: " + jsonstring);
                WeatherModel model = JSON.decode(jsonstring, WeatherModel.class);
                return model;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                return null;
            }
        }
    }

    /**
     * UIスレッドでの処理用
     */
    @Override
    protected void onPostExecute(WeatherModel result) {
        Log.d(TAG, "enter onPostExecute() user_id:" + result);
        if (null != mListener) {
            mListener.onAccess(result);
        }
    }
}
