package jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm;

import java.util.Iterator;
import java.util.Map;

import jp.androidapp.libs.pluggablealarm.AlarmPrefManager;
import jp.androidapp.libs.pluggablealarm.AlarmUtil;
import jp.androidapp.libs.pluggablealarm.IntentParam;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PluginSettingActivity extends PreferenceActivity {
	private static final String TAG = "PluginSettingActivity";
	private static final String PLUGIN_NAME_FOR_HUMAN = "試される目覚まし時計プラグイン";

	private static final String sPackageName;
	static{
		sPackageName = PluginSettingActivity.class.getPackage().getName();		
	}
	private static final String ACTION_PLUGIN_ALARM = sPackageName + ".ACTION_ALARM";
	private static final String ACTION_PLUGIN_EDIT = sPackageName + ".ACTION_OPEN_ALARM_SETTING";
	private static final String ACTION_PLUGIN_NEXT_ALARM = sPackageName + ".ACTION_NEXT_ALARM";
	private static final String ACTION_PLUGIN_NEXT_SNOOZE = sPackageName + ".ACTION_NEXT_SNOOZE";
	private static final String PREF_MANAGE_NAME = "pref_manage";
	
    private Map<String, ?> mPrefMapBeforeModify = null;
	private SharedPreferences mPrefSetting;
	private String mEditMode;
	private Button mButtonCansel;
	private Button mButtonDelete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent data = getIntent();
		
		if(IntentParam.ACTION_DEFUALT_NEXT_ALARM.equals(data.getAction())){
			forNextAlarm(data);
		} else if(IntentParam.ACTION_DEFUALT_NEXT_SNOOZE.equals(data.getAction())){
			forNextSnooze(data);
		} else {
			// see: http://d.hatena.ne.jp/kaw0909/20110403/1301827783
			setContentView(R.layout.activity_plugin_setting);
			addPreferencesFromResource(R.xml.default_setting);
			forEdit(data);
		}
	}
		
	@Override  
	protected void onResume() {  
	    super.onResume();  
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(mListener);  
	}  
	   
	@Override  
	protected void onPause() {  
	    super.onPause();  
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mListener);  
	}  
	  
	// ここで summary を動的に変更  
	private SharedPreferences.OnSharedPreferenceChangeListener mListener =  new SharedPreferences.OnSharedPreferenceChangeListener() {  
	    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {  
	    	dumpCurrentSharedPrerence(mPrefSetting);
	        if("max_volume".equals(key)){
	        	String volume = sharedPreferences.getString(key, "");
		    	findPreference(key).setSummary("現在値: " + volume + "％");  
	        } else if("min_volume".equals(key)){
	        	String volume = sharedPreferences.getString(key, "");
			    findPreference(key).setSummary("現在値: " + volume + "％");  
	        } else if("time_summary".equals(key)){
		    	findPreference("time").setSummary(sharedPreferences.getString(key, ""));  
	        } else if("snooze_interval".equals(key)){
	        	String interval = sharedPreferences.getString(key, "0");
	        	long intervalLong = Long.valueOf(interval) / (1000L * 60L);
	        	String intervalStr = intervalLong + "分";
		    	findPreference(key).setSummary(intervalStr);  
	        } else if("ringtone".equals(key)){
//				// see: Android開発-着信音の選択と再生- - 明日の鍵 http://d.hatena.ne.jp/tomorrowkey/20090826/1251294895
	        	String uriString = sharedPreferences.getString(key, "");
	        	if("".equals(uriString)){
	        		return;
	        	}
	        	Ringtone ringtone = RingtoneManager.getRingtone(PluginSettingActivity.this, Uri.parse(uriString));
	        	String ringtoneName = ringtone.getTitle(PluginSettingActivity.this);
		    	findPreference(key).setSummary("選択中のアラーム: " + ringtoneName); 
		    	Log.d(TAG, "ringtone: " + uriString + ", ringtone name: " + ringtoneName);
	        }
	    }  
	};
	
	private void forEdit(Intent data){
		final int alarmId;
		if(data.hasExtra(IntentParam.EXTRAS_ALARM_ID)){
			alarmId = data.getIntExtra(IntentParam.EXTRAS_ALARM_ID, -1);
		} else {
			alarmId = -1;
		}
		
		findViewById(R.id.plugin_setting_button_submit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {				
				// アラームの内容を入力させて、計算して、保存して、最終的な内容を返す
				dumpCurrentSharedPrerence(mPrefSetting);
				
			    // 入力データを返す
				Intent data = new Intent();
				data.putExtra(IntentParam.EXTRAS_RESULT, mEditMode);
                data.putExtra(IntentParam.EXTRAS_TIME, getTime());
                data.putExtra(IntentParam.EXTRAS_WEEKS, getWeeks());
                data.putExtra(IntentParam.EXTRAS_PLUGIN_NAME, getPluginName());
				data.putExtra(IntentParam.EXTRAS_NEXT_DELAY_IN_MILLIS, getNextDelayInMillisForNextAlarm());
                data.putExtra(IntentParam.EXTRAS_ALARM_TITLE, getAlarmTitle());
				data.putExtra(IntentParam.EXTRAS_PICKED_ALARM_RESOURCE, getSelectedAlarmResource());
				data.putExtra(IntentParam.EXTRAS_ALARM_SPECIAL_ACTION, ACTION_PLUGIN_ALARM);
                data.putExtra(IntentParam.EXTRAS_EDIT_SPECIAL_ACTION, ACTION_PLUGIN_EDIT);
                data.putExtra(IntentParam.EXTRAS_NEXT_ALARM_SPECIAL_ACTION, ACTION_PLUGIN_NEXT_ALARM);
                data.putExtra(IntentParam.EXTRAS_NEXT_SNOOZE_SPECIAL_ACTION, ACTION_PLUGIN_NEXT_SNOOZE);
				data.putExtra(IntentParam.EXTRAS_ALARM_ID, mPrefSetting.getInt(AlarmPrefManager.PREF_ALARM_ID, -1)); // このプラグイン毎にユニークなID
				setResult(RESULT_OK, data);
				finish();
			}
		});
		mButtonCansel = (Button) findViewById(R.id.plugin_setting_button_cansel);
		mButtonCansel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {				
				// 開いた時の情報をSharedPreferenceに書き戻す
				if(IntentParam.EXTRAS_RESULT_UPDATED.equals(mEditMode)){
					restoreSavedState(mPrefSetting);
				}
				
				Intent data = new Intent();
				setResult(RESULT_CANCELED, data);
				finish();
			}
		});
		mButtonDelete = (Button) findViewById(R.id.plugin_setting_button_delete);
		mButtonDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {				
				int a = mPrefSetting.getInt(AlarmPrefManager.PREF_ALARM_ID, -1);
				Intent data = new Intent();
				data.putExtra(IntentParam.EXTRAS_RESULT, IntentParam.EXTRAS_RESULT_DELETED);
                data.putExtra(IntentParam.EXTRAS_PLUGIN_NAME, getPluginName());
				data.putExtra(IntentParam.EXTRAS_ALARM_ID, a); // このプラグイン毎にユニークなID
				setResult(RESULT_OK, data);

				// このSharedPreferenceを消す
				Editor editor = mPrefSetting.edit();
				editor.clear();
				editor.commit();
				
				AlarmPrefManager man = new AlarmPrefManager(PluginSettingActivity.this, PREF_MANAGE_NAME);
				man.removeAlarmId(a);
				
				finish();
			}
		});
		
		

		if(-1 != alarmId){
			// 編集用のコード
			getPreferenceManager().setSharedPreferencesName(AlarmPrefManager.ALARM_NAME_BASE + alarmId);
			mPrefSetting = getPreferenceManager().getSharedPreferences();
			mEditMode = IntentParam.EXTRAS_RESULT_UPDATED;
			
			// 最初の状態を保存しておく
			saveCurrentState(mPrefSetting);
		} else {
			// 新規用のコード
			AlarmPrefManager man = new AlarmPrefManager(this, PREF_MANAGE_NAME);
			getPreferenceManager().setSharedPreferencesName(man.nextPrefName());
			mPrefSetting = getPreferenceManager().getSharedPreferences();
			mEditMode = IntentParam.EXTRAS_RESULT_CREATED;
			mButtonDelete.setVisibility(View.INVISIBLE);
		}

	}
    private void forNextAlarm(Intent intent){
		// alarmidを元に呼び出して計算して、最終的な内容を返す(次回アラーム時間までのdelayを計算)
		int alarmId = 0;
		if(intent.hasExtra(IntentParam.EXTRAS_ALARM_ID)){
			alarmId = intent.getIntExtra(IntentParam.EXTRAS_ALARM_ID, 0);
		} else {
			throw new RuntimeException("need EXTRAS_ALARM_ID");
		}
		getPreferenceManager().setSharedPreferencesName(AlarmPrefManager.ALARM_NAME_BASE + alarmId);
		mPrefSetting = getPreferenceManager().getSharedPreferences();
		dumpCurrentSharedPrerence(mPrefSetting);

		Intent data = new Intent();
		data.putExtra(IntentParam.EXTRAS_RESULT, IntentParam.EXTRAS_RESULT_CALCULATED);
		data.putExtra(IntentParam.EXTRAS_NEXT_DELAY_IN_MILLIS, getNextDelayInMillisForNextAlarm());
		data.putExtra(IntentParam.EXTRAS_PICKED_ALARM_RESOURCE, getSelectedAlarmResource());
		data.putExtra(IntentParam.EXTRAS_ALARM_SPECIAL_ACTION, ACTION_PLUGIN_ALARM);
        data.putExtra(IntentParam.EXTRAS_EDIT_SPECIAL_ACTION, ACTION_PLUGIN_EDIT);
        data.putExtra(IntentParam.EXTRAS_NEXT_ALARM_SPECIAL_ACTION, ACTION_PLUGIN_NEXT_ALARM);
        data.putExtra(IntentParam.EXTRAS_NEXT_SNOOZE_SPECIAL_ACTION, ACTION_PLUGIN_NEXT_SNOOZE);
		data.putExtra(IntentParam.EXTRAS_ALARM_ID, alarmId); // このプラグイン毎にユニークなID
		setResult(RESULT_OK, data);
		finish();		
	}

	private void forNextSnooze(Intent intent){
		// alarmidを元に呼び出して計算して、最終的な内容を返す(次回スヌーズ時間までのdelayを計算)
		int alarmId = 0;
		if(intent.hasExtra(IntentParam.EXTRAS_ALARM_ID)){
			alarmId = intent.getIntExtra(IntentParam.EXTRAS_ALARM_ID, 0);
		} else {
			throw new RuntimeException("need EXTRAS_ALARM_ID");
		}
		getPreferenceManager().setSharedPreferencesName(AlarmPrefManager.ALARM_NAME_BASE + alarmId);
		mPrefSetting = getPreferenceManager().getSharedPreferences();
		dumpCurrentSharedPrerence(mPrefSetting);
		
		Intent data = new Intent();
		data.putExtra(IntentParam.EXTRAS_RESULT, IntentParam.EXTRAS_RESULT_CALCULATED);
		data.putExtra(IntentParam.EXTRAS_NEXT_DELAY_IN_MILLIS, getNextDelayInMillisForNextSnooze());
		data.putExtra(IntentParam.EXTRAS_PICKED_ALARM_RESOURCE, getSelectedAlarmResource());
		data.putExtra(IntentParam.EXTRAS_ALARM_SPECIAL_ACTION, ACTION_PLUGIN_ALARM);
        data.putExtra(IntentParam.EXTRAS_EDIT_SPECIAL_ACTION, ACTION_PLUGIN_EDIT);
        data.putExtra(IntentParam.EXTRAS_NEXT_ALARM_SPECIAL_ACTION, ACTION_PLUGIN_NEXT_ALARM);
        data.putExtra(IntentParam.EXTRAS_NEXT_SNOOZE_SPECIAL_ACTION, ACTION_PLUGIN_NEXT_SNOOZE);
		data.putExtra(IntentParam.EXTRAS_ALARM_ID, alarmId); // このプラグイン毎にユニークなID
		setResult(RESULT_OK, data);
		finish();
	}

    private long getNextDelayInMillisForNextSnooze() {
    	return Long.valueOf(mPrefSetting.getString("snooze_interval", "0"));
    }

    private long getNextDelayInMillisForNextAlarm() {
    	int hour = mPrefSetting.getInt("time_Hour", 0);
    	int minute = mPrefSetting.getInt("time_Minute", 0);
    	// アラーム本来の時間
    	long delay = AlarmUtil.getNextDelayInMillisForNextAlarm(this, hour, minute, getWeeks());
    	if(mPrefSetting.getBoolean("need_to_harly_up", false)){
    		// 早起きを考慮する
    		String h = mPrefSetting.getString("harlyup_interval", "0");
    		delay -= Long.valueOf(h);
    	}
    	return delay; 
    }
	
    private String getWeeks() {
    	StringBuilder sb = new StringBuilder();
    	if(mPrefSetting.getBoolean("sun", false)){
    		sb.append("日");
    	}
    	if(mPrefSetting.getBoolean("mon", false)){
    		if(sb.length() > 0){
    			sb.append(",");
    		}
    		sb.append("月");
    	}
    	if(mPrefSetting.getBoolean("tue", false)){
    		if(sb.length() > 0){
    			sb.append(",");
    		}
    		sb.append("火");
    	}
    	if(mPrefSetting.getBoolean("wed", false)){
    		if(sb.length() > 0){
    			sb.append(",");
    		}
    		sb.append("水");
    	}
    	if(mPrefSetting.getBoolean("thu", false)){
    		if(sb.length() > 0){
    			sb.append(",");
    		}
    		sb.append("木");
    	}
    	if(mPrefSetting.getBoolean("fri", false)){
    		if(sb.length() > 0){
    			sb.append(",");
    		}
    		sb.append("金");
    	}
    	if(mPrefSetting.getBoolean("sat", false)){
    		if(sb.length() > 0){
    			sb.append(",");
    		}
    		sb.append("土");
    	}
        return sb.toString();
    }

    private String getTime() {
        return mPrefSetting.getString("time", "");
    }

    private String getAlarmTitle() {
        return PLUGIN_NAME_FOR_HUMAN;
    }
    
    protected String getPluginName() {
        return getPackageName().toString();
    }

    private String getSelectedAlarmResource() {
        return mPrefSetting.getString("ringtone", "");
	}

    private void dumpCurrentSharedPrerence(SharedPreferences pref){
    	Map<String, ?> map = pref.getAll();
    	Iterator<String> ite = map.keySet().iterator();
    	while(ite.hasNext()){
    		String key = ite.next();
    		Log.d(TAG, key + " : " + String.valueOf(map.get(key)));
    	}
    }
    
    private void saveCurrentState(SharedPreferences pref){
    	mPrefMapBeforeModify = pref.getAll();
    }
    
    private void restoreSavedState(SharedPreferences pref){
    	Editor editor = pref.edit();
    	editor.clear();
    	Iterator<String> ite = mPrefMapBeforeModify.keySet().iterator();
    	while(ite.hasNext()){
    		String key = ite.next();
    		Object value = mPrefMapBeforeModify.get(key);
    		if(value instanceof String){
    			editor.putString(key, (String) value);
    		} else if(value instanceof Integer){
    			editor.putInt(key, (Integer) value);
    		} else {
        		Log.d(TAG, key + " : " + String.valueOf(mPrefMapBeforeModify.get(key)));
    		}
    	}
    	editor.commit();
    }

}
