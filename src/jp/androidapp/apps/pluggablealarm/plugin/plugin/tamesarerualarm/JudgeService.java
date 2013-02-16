package jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm;

import java.util.ArrayList;

import jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm.model.WeatherModel;
import jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm.util.LocationUpdater;
import jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm.util.WeatherUtil;
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
import android.widget.Toast;

public class JudgeService extends IntentService {
	private static final String TAG = "JudgeService";
	/**
	 * 起きる時間のN時間前に呼ばれて、早めに起きるか確認するAction
	 */
	public static final String JUDGE_ACTION = "jp.androidapp.tamesarerualarm.plugin.tenkiyomi.JUDGE_ACTION";
	protected AlarmManager mAlarmManager;
	protected AlarmData mAlarmData;
	protected WeatherModel mModel = null;
	private SharedPreferences mPrefSetting;

    public JudgeService(){
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
        mPrefSetting = getSharedPreferences(prefName, Context.MODE_PRIVATE );

        if(JUDGE_ACTION.equals(actionName)){
        	AlarmUtil.dumpCurrentSharedPrerence(mPrefSetting);
        	// 早起きするか
        	if(!mPrefSetting.getBoolean("need_to_harly_up", false)){
            	// 早起きしないのでそのままアラームを鳴らす
        		kickAlarmActivity();
        	} else {
            	// 呼ばれたタイミングは本来のアラーム時間から早起きする時間を引いたもの
        		
            	// 緯度経度を調べて
        		LocationUpdater locMan = new LocationUpdater(this);
        		Location l = locMan.getLocation();
        		if(null == l){
        			throw new RuntimeException("do not get location still");
        		}
        		String lat = String.valueOf(l.getLatitude());
        		String lon = String.valueOf(l.getLongitude());
        		
            	// 天気を調べて
            	WeatherUtil.getCurrent(this, lat, lon, new WeatherUtil.OnAccessListener() {
    				@Override
    				public void onAccess(WeatherModel model) {
    					mModel = model;
    					judge();
    				}
    			});
            	// TODO: uuidを調べて
            	// TODO: ヤバイよサーバに問い合わせて
        	}
        }
	}
	
	private synchronized void judge(){
		// TODO: ヤバイよサーバの問い合わせ結果もあるならばココに追加
		if(null != mModel){
        	// 今アラームを鳴らすか判断
        	if(judgeByWeather(mModel)){
        		// 早起きする場合は、そのままアラーム画面へ
        		kickAlarmActivity();
        	} else {
        		// 予定通りの場合は再度アラームを設定する
            	String h = mPrefSetting.getString("harlyup_interval", "0");
            	long delay = Long.valueOf(h);
        		AlarmUtil.setNextAlarm(this, mAlarmManager, mAlarmData, delay);
        	}
		}
	}

	/**
	 * AND検索なので1つでも条件を満たさなければfalseにする
	 * @param model
	 * @return
	 */
	private boolean judgeByWeather(WeatherModel model){
		String t;
		int value;
		WeatherModel.CurrentCondition cond = model.getData().getCurrent_condition();
		if(mPrefSetting.getBoolean("need_to_use_windspeed", false)){
			// 風速を考慮(以上)
			t = mPrefSetting.getString("windspeed", "0");
			value = Integer.valueOf(t);
			if(cond.getWindspeedKmph() < value){
				return false;
			}
		}
		if(mPrefSetting.getBoolean("need_to_use_temp_c", false)){
			// 気温を考慮(以下)
			t = mPrefSetting.getString("temp_c", "0");
			value = Integer.valueOf(t);
			if(cond.getTemp_C() > value){
				return false;
			}
		}
		if(mPrefSetting.getBoolean("need_to_use_windspeed", false)){
			// 天気カテゴリを考慮
			String weatherCategory = chooseWeatherCategory(cond.getWeatherCode());
			ArrayList<String> categories = new ArrayList<String>();
			if(mPrefSetting.getBoolean("need_to_use_rain_category", false)){
				categories.add("rain");
			}
			if(mPrefSetting.getBoolean("need_to_use_snow_category", false)){
				categories.add("snow");
			}
			if(!categories.contains(weatherCategory)){
				return false;
			}
		}
		return true;
	}
	
	private String chooseWeatherCategory(int weatherCode){
		return "rain";
	}
	
    private void kickAlarmActivity(){
    	Log.d(TAG, "kickAlarmActivity()");
    	Intent intent = new Intent(this, PluginAlarmActivity.class);
    	mAlarmData.setForAlarmTo(intent);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(intent);
    }
    
    private void showNotificationMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
