package jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm;

import java.util.Iterator;
import java.util.Map;

import jp.androidapp.libs.pluggablealarm.AlarmData;
import jp.androidapp.libs.pluggablealarm.AlarmPrefManager;
import jp.androidapp.libs.pluggablealarm.BaseAlarmActivity;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

public class PluginAlarmActivity extends BaseAlarmActivity {
    private Ringtone ringtone = null;
    private Handler mHandler = new Handler();
    private int mMaxVolume = 0;
    private boolean mVolumeInc = false;
    private int mMinVolume = 0;
    private static final String TAG = "DefaultAlarmActivity";
    private AlarmData mAlarmData;
    private Handler mVolumeIncHandler = null;
    private int mCurrentVolume;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plugin_alarm);
        
		findViewById(R.id.alarm_plugin_viewgroup_root).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setNextSnooze(mOnSetAlarm);
				if (ringtone != null) {
                    if (null!=mVolumeIncHandler) {
                        mVolumeIncHandler.removeMessages(0);
                        mVolumeIncHandler = null;
                    }
					ringtone.stop();
				}
			}
		});
		
		findViewById(R.id.alarm_plugin_button_stop).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setNextAlarm(mOnSetAlarm);
//				// 強制終了
//				moveTaskToBack(true);
				if (ringtone != null) {
                    if (null!=mVolumeIncHandler) {
                        mVolumeIncHandler.removeMessages(0);
                        mVolumeIncHandler = null;
                    }
					ringtone.stop();
				}
			}
		});
		
      // Intentパラメータの取得
        
        
        // プリファレンスからアラーム関係の設定値を読み込む
        Intent intent = getIntent();
        mAlarmData = AlarmData.from(intent);
        readPreference(this,mAlarmData.alarmId);
	}

	   private void readPreference(Context context, int alarmId) {
	        // TODO 自動生成されたメソッド・スタブ
	        
	        // プリファレンスを読み込む
	        String name = AlarmPrefManager.ALARM_NAME_BASE + alarmId;
	        SharedPreferences pref = 
	                context.getSharedPreferences( name, Context.MODE_PRIVATE );
	        // データを読み込む
	        dumpCurrentSharedPrerence(pref);
	        
	        // アラームの音量(最大)
	        // mMaxVolume = pref.getInt("max_volume", 70);
	        String maxVolume = pref.getString("max_volume", "70");
	        try {
	            mMaxVolume = Integer.parseInt(maxVolume);
	        } catch (NumberFormatException e) {
	            // TODO 自動生成された catch ブロック
	            e.printStackTrace();
	            mMaxVolume = 70;
	        }
	        
	        // アラーム音を徐々に大きくするか
	        mVolumeInc = pref.getBoolean("need_to_incliment_volume", false);
	        
	        // アラーム音量（最小）
	        // mMinVolume = pref.getInt("min_volume", 0);
	        String minVolume = pref.getString("min_volume", "10");
	        try {
	            mMinVolume = Integer.parseInt(minVolume);
	        } catch (NumberFormatException e) {
	            // TODO 自動生成された catch ブロック
	            e.printStackTrace();
	            mMinVolume = 10;
	        }
	        
	    }

	    private void dumpCurrentSharedPrerence(SharedPreferences pref){
	        Map<String, ?> map = pref.getAll();
	        Iterator<String> ite = map.keySet().iterator();
	        while(ite.hasNext()){
	            String key = ite.next();
	            Log.d(TAG, key + " : " + String.valueOf(map.get(key)));
	        }
	    }

	@Override
	public void onResume(){
		super.onResume();
		if (ringtone == null){
			Uri uri = Uri.parse(mAlarmData.pickedAlarmResource);
			ringtone = RingtoneManager.getRingtone(this, uri);
			ringtone.play();
		}
		
	      // TODO 暫定的に着信音量を上げる
        setAlarmVolume();

	}
    /**
     * 
     */
    private void setAlarmVolume() {
        
        
        if(mVolumeInc){
            // 徐々にあげる有効
            // 現在の音量を最小音量に設定
            AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(AudioManager.STREAM_RING, mMinVolume, 0); //アラーム音量を50に設定
            int now = am.getStreamVolume(AudioManager.STREAM_RING);
            Log.d(TAG,"now:"+now);

            mCurrentVolume = mMinVolume;
            mVolumeIncHandler  = new  Handler();
            mVolumeIncHandler.postDelayed(mVolumeIncTask, 5*1000);;            
        }else{
            // 無効
            AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            int now = am.getStreamVolume(AudioManager.STREAM_RING);
            Log.d(TAG,"now:"+now);
            int max = am.getStreamMaxVolume(AudioManager.STREAM_RING);
            Log.d(TAG,"max:"+max);
            am.setStreamVolume(AudioManager.STREAM_RING, mMaxVolume, 0); //アラーム音量を50に設定
        }
    }
    
    private Runnable mVolumeIncTask = new Runnable() {
        
        @Override
        public void run() {
            // 
            Log.d(TAG,"mCurrentVolume:"+mCurrentVolume);
            Log.d(TAG,"mMinVolume:"+mMinVolume);
            Log.d(TAG,"mMaxVolume:"+mMaxVolume);
            if(mCurrentVolume <= mMaxVolume){
                // ボリュームを足す
                mCurrentVolume += 10;
                // ボリューム設定
                AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                am.setStreamVolume(AudioManager.STREAM_RING, mCurrentVolume, 0); //アラーム音量を50に設定
                int now = am.getStreamVolume(AudioManager.STREAM_RING);
                Log.d(TAG,"now:"+now);

                mVolumeIncHandler.postDelayed(this, 5*1000);
            }else{
                AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                am.setStreamVolume(AudioManager.STREAM_RING, mMaxVolume, 0); //アラーム音量を50に設定
                int now = am.getStreamVolume(AudioManager.STREAM_RING);
                Log.d(TAG,"now:"+now);
            }
            
        }
    };
	private BaseAlarmActivity.OnSetAlarm mOnSetAlarm = new BaseAlarmActivity.OnSetAlarm(){
		@Override
		public void onSetAlarmDone() {
			Intent intent = new Intent(PluginAlarmActivity.this, YabaiyoActivity.class);
			startActivity(intent);
		}

		@Override
		public void onSetSnoozeDone() {
			mHandler.postDelayed(mFinishTask, 2000); // 2sec
		}
	};
	
	private Runnable mFinishTask = new Runnable() {
		@Override
		public void run() {
			finish();
		}
	};
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_plugin_main, menu);
//		return true;
//	}

}

