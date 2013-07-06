package jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm;

import java.util.ArrayList;

import jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.model.WeatherModel;
import jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.util.LocationUpdater;
import jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.util.WeatherUtil;
import jp.androidapp.libs.pluggablealarm.AlarmData;
import jp.androidapp.libs.pluggablealarm.AlarmPrefManager;
import jp.androidapp.libs.pluggablealarm.AlarmUtil;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

public class JudgeService
    extends IntentService {
    private static final String TAG = "JudgeService";
    /**
     * 起きる時間のN時間前に呼ばれて、早めに起きるか確認するAction
     */
    public static final String JUDGE_ACTION = "jp.androidapp.tamesarerualarm.plugin.tamesarerualarm.JUDGE_ACTION";
    protected AlarmManager mAlarmManager;
    protected AlarmData mAlarmData;
    protected WeatherModel mModel = null;
    private SharedPreferences mPrefSetting;
    private LocationUpdater mLocMan;

    public JudgeService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String actionName = intent.getAction();
        Log.d(TAG, "enter onHandleIntent(). ACTION:" + actionName);

        mAlarmData = AlarmData.from(intent);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // プリファレンスを読み込む
        String prefName = AlarmPrefManager.ALARM_NAME_BASE + mAlarmData.alarmId;
        mPrefSetting = getSharedPreferences(prefName, Context.MODE_PRIVATE);

        if (JUDGE_ACTION.equals(actionName)) {
            AlarmUtil.dumpCurrentSharedPrerence(mPrefSetting);
            // 早起きするか
            if (!mPrefSetting.getBoolean("need_to_harly_up", false)) {
                // 天候条件では早起きしないので、このタイミングでアラームを鳴らす(呼ばれたタイミングは早起きしないタイミング)
                kickAlarmActivity();
            } else {
                // 呼ばれたタイミングは本来のアラーム時間から早起きする時間を引いたもの(本来よりN分前)なので、ここで天気を調べる

                // 緯度経度を調べて
                mLocMan = new LocationUpdater(this);
                mLocMan.onResume();
                Log.d(TAG, "detecting location...");
                mLocMan.setListener(new LocationUpdater.OnDetactLocation() {
                    @Override
                    public void onDetect(Location location) {
                        // 位置情報が無かったらエラー
                        if (null == location) {
                            throw new RuntimeException("do not get location still");
                        }
                        mLocMan.onPause();
                        String lat = String.valueOf(location.getLatitude());
                        String lon = String.valueOf(location.getLongitude());
                        Log.d(TAG, "detected location lat: " + lat + ", lon: " + lon);

                        // 緯度経度が指す位置の天気を調べて
                        Log.d(TAG, "access to Weather API...");
                        WeatherUtil.getCurrent(JudgeService.this, lat, lon, new WeatherUtil.OnAccessListener() {
                            @Override
                            public void onAccess(WeatherModel model) {
                                Log.d(TAG, "got the weather information...");
                                mModel = model;
                                judge();
                            }
                        });
                        // TODO: uuidを調べて
                        // TODO: ヤバイよサーバに問い合わせて
                    }
                });
            }
        }
    }

    private synchronized void judge() {
        // TODO: ヤバイよサーバの問い合わせ結果もあるならばココに追加
        if (null != mModel) {
            // 今アラームを鳴らすか判断
            if (judgeByWeather(mModel)) {
                // 早起きする場合は、そのままアラーム画面へ
                kickAlarmActivity();
            } else {
                // 予定通りの場合は再度アラームを設定する
                String h = mPrefSetting.getString("harlyup_interval", "0");
                long delay = Long.valueOf(h);
                AlarmUtil.setNextAlarm(this, mAlarmManager, mAlarmData, delay, false);
            }
        }
    }

    /**
     * AND検索なので1つでも条件を満たさなければfalseにする
     * 
     * @param model
     * @return
     */
    private boolean judgeByWeather(WeatherModel model) {
        String t;
        int value;
        WeatherModel.CurrentCondition cond = model.getData().getCurrent_condition().get(0);
        if (mPrefSetting.getBoolean("need_to_use_windspeed", false)) {
            // 風速を考慮(以上)
            t = mPrefSetting.getString("windspeed", "0");
            value = Integer.valueOf(t);
            if (cond.getWindspeedKmph() < value) {
                return false;
            }
        }
        if (mPrefSetting.getBoolean("need_to_use_temp_c", false)) {
            // 気温を考慮(以下)
            t = mPrefSetting.getString("temp_c", "0");
            value = Integer.valueOf(t);
            if (cond.getTemp_C() > value) {
                return false;
            }
        }
        if (mPrefSetting.getBoolean("need_to_use_windspeed", false)) {
            // 天気カテゴリを考慮
            String weatherCategory = chooseWeatherCategory(cond.getWeatherCode());
            ArrayList<String> categories = new ArrayList<String>();
            if (mPrefSetting.getBoolean("need_to_use_rain_category", false)) {
                categories.add("rain");
            }
            if (mPrefSetting.getBoolean("need_to_use_snow_category", false)) {
                categories.add("snow");
            }
            if (!categories.contains(weatherCategory)) {
                return false;
            }
        }
        return true;
    }

    /**
     * weatherCodeが、具体的に雨カテゴリなのか、雪カテゴリなのか、どちらでも無いのかを判定する
     * 
     * @param weatherCode
     * @return
     */
    private String chooseWeatherCategory(int weatherCode) {
        int i;
        for (i = 0; i < rainCodes.length; i++) {
            if (weatherCode == rainCodes[i]) {
                return "rain";
            }
        }
        for (i = 0; i < snowCodes.length; i++) {
            if (weatherCode == snowCodes[i]) {
                return "snow";
            }
        }
        return "other";
    }

    private final int[] rainCodes = new int[] { 389, 386, 359, 356, 353, 314, 311, 308, 305, 302, 299, 296, 293, 284, 281, 266, 263, 185,
                                               176 };
    private final int[] snowCodes = new int[] { 395, 392, 377, 374, 371, 368, 365, 362, 350, 338, 335, 332, 329, 326, 323, 320, 317, 230,
                                               227, 182, 179, };

    private void kickAlarmActivity() {
        Log.d(TAG, "kickAlarmActivity()");
        Intent intent = new Intent(this, PluginAlarmActivity.class);
        mAlarmData.setForAlarmTo(intent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    //
    // private void showNotificationMessage(String message) {
    // Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    // }

}
