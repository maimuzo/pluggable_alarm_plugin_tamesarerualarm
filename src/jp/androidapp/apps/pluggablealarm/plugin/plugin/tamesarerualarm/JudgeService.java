package jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm;

import jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm.model.WeatherModel;
import jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm.util.LocationUpdater;
import jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm.util.WeatherUtil;
import jp.androidapp.libs.pluggablealarm.AlarmData;
import jp.androidapp.libs.pluggablealarm.AlarmUtil;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Intent;
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
	protected WeatherModel mModel;

    public JudgeService(){
        super(TAG);
    }
    
	@Override
	protected void onHandleIntent(Intent intent) {
        String actionName = intent.getAction();
        Log.d(TAG, "enter onHandleIntent(). ACTION:" + actionName);
      
		mAlarmData = AlarmData.from(intent);
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if(JUDGE_ACTION.equals(actionName)){
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
	
	private void judge(){
		// TODO: ヤバイよサーバの問い合わせ結果もあるならばココに追加
		if(null != mModel){
        	// 今アラームを鳴らすか判断
        	if(true){
        		// 今鳴らすならまずアラームを解除
        		AlarmUtil.unsetNextAlarm(this, mAlarmManager, mAlarmData);
        		kickAlarmActivity();
        	} else {
        		// TODO: 予定通りの時刻に鳴らすなら、Nミリ秒後に鳴るように再度設定
        		// TODO: 本来の時間はどうやって判断する?
        		AlarmUtil.setNextAlarm(this, mAlarmManager, mAlarmData, 1000 * 10);
        	}
			
		}
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
