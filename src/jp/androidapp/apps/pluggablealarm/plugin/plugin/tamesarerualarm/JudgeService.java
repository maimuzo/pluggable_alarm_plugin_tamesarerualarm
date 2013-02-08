package jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm;

import jp.androidapp.libs.pluggablealarm.AlarmData;
import jp.androidapp.libs.pluggablealarm.AlarmUtil;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Intent;
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
        	// TODO: 緯度経度を調べて
        	// TODO: uuidを調べて
        	// TODO: 天気を調べて
        	// TODO: ヤバイよサーバに問い合わせて
        	// TODO: 今アラームを鳴らすか判断
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
