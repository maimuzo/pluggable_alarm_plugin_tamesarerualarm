package jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.model.WeatherModel;
import jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.util.LocationUpdater;
import jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.util.UrlImageGetTask;
import jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.util.WeatherUtil;
import jp.androidapp.libs.pluggablealarm.AlarmPrefManager;
import jp.androidapp.libs.pluggablealarm.AlarmUtil;
import jp.androidapp.libs.pluggablealarm.BaseAlarmActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PluginAlarmActivity
    extends BaseAlarmActivity {
    private static final String TAG = "PluginAlarmActivity";
    private static final long VOLUME_INC_INTERVAL_MSEC = 5 * 1000; // 5sec毎に音量を上げる
    private static final int VOLUME_INC_ONETIME = 1; // VOLUME_INC_INTERVAL_MSEC毎に上げるボリューム量
    private static final int TARGET_STREAM_TYPE = AudioManager.STREAM_RING;
    private static final long[] VIBRATE_PATTERN = { 1000, 1000, 1000, 1000 }; // OFF/ON/OFF/ON...
    private static final int VOLUME_FLAG = AudioManager.FLAG_SHOW_UI;
    // private static final int VOLUME_FLAG = 0;

    private final SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Ringtone mRingtoneManager = null;
    private final Handler mHandler = new Handler();
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private float mMaxVolumePersent;
    private float mMinVolumePersent;
    private int mSystemMaxVolume = 0;
    private int mSavedVolume;
    private int mCurrentVolume;
    private boolean mIsEnabledVolumeInc = false;
    private boolean mPlayingAlarm;
    private boolean mUseSnooze;
    private boolean mIsVibrateOn;
    private boolean mIsWaitingTouch;
    private boolean mCanPlaying;
    private LocationUpdater mLocMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_alarm);

        mPlayingAlarm = false;
        mIsWaitingTouch = true;
        mCanPlaying = true;
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSystemMaxVolume = mAudioManager.getStreamMaxVolume(TARGET_STREAM_TYPE);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        TextView t = (TextView) findViewById(R.id.alarm_text_date);
        t.setText(mFormat.format(new Date()));

        /**
         * 画面全体のリスナー
         */
        findViewById(R.id.alarm_plugin_viewgroup_root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsWaitingTouch) {
                    // mIsWaitingTouch = false;
                    if (mUseSnooze) {
                        // ボタン以外の場合はスヌーズをセット
                        setNextSnooze(mOnSetAlarm);
                    } else {
                        // スヌーズを使わない場合は次回アラームをセット
                        setNextAlarm(mOnSetAlarm);
                    }
                    // stopAlarming();
                    // mOnSetAlarmの中で2秒待ってからfinish()し、onPause()でアラームを止める
                }
            }
        });

        /**
         * 止めるボタン
         */
        findViewById(R.id.alarm_plugin_button_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mIsWaitingTouch) {
                    // mIsWaitingTouch = false;
                    // 次回アラームをセット
                    setNextAlarm(mOnSetAlarm);
                    // stopAlarming();
                    // mOnSetAlarmの中で2秒待ってからfinish()し、onPause()でアラームを止める
                }
            }
        });

        // プリファレンスからアラーム関係の設定値を読み込む(mAlarmDataはBaseAlarmActivityでセットされる)
        readSettingsFromPreference(this, mAlarmData.alarmId);

        // 緯度経度を調べて、天気を調べて、表示する
        mLocMan = new LocationUpdater(this);
        Log.d(TAG, "detecting location...");
        mLocMan.setListener(new LocationUpdater.OnDetactLocation() {
            @Override
            public void onDetect(Location location) {
                // 位置情報が無かったらエラー
                if (null == location) {
                    throw new RuntimeException("do not get location still");
                }
                String lat = String.valueOf(location.getLatitude());
                String lon = String.valueOf(location.getLongitude());
                Log.d(TAG, "detected location lat: " + lat + ", lon: " + lon);

                // 緯度経度が指す位置の天気を調べて
                Log.d(TAG, "access to Weather API...");
                WeatherUtil.getCurrent(PluginAlarmActivity.this, lat, lon, new WeatherUtil.OnAccessListener() {
                    @Override
                    public void onAccess(WeatherModel model) {
                        Log.d(TAG, "got the weather information...");
                        showWhetherInfomation(model);
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        if (mIsVibrateOn && mCanPlaying) {
            mVibrator.vibrate(VIBRATE_PATTERN, 0);
        }
        if (!mPlayingAlarm && mCanPlaying) {
            startAlarming();
        }

        mLocMan.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");

        mLocMan.onPause();
        mVibrator.cancel();
        stopAlarming();
    }

    /**
     * BackキーとHomeキーを無効化し、ちゃんとアラームを止めてもらって、次回のアラームもセットする
     */
    @Override
    public boolean onKeyDown(int keyCode,
                             KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            // 無効化する
            return false;
        } else if (KeyEvent.KEYCODE_HOME == keyCode) {
            // 背景タッチと同じ挙動
            if (mIsWaitingTouch) {
                mIsWaitingTouch = false;
                if (mUseSnooze) {
                    // ボタン以外の場合はスヌーズをセット
                    setNextSnooze(mOnSetAlarm);
                } else {
                    // スヌーズを使わない場合は次回アラームをセット
                    setNextAlarm(mOnSetAlarm);
                }
                // stopAlarming();
                // mOnSetAlarmの中で2秒待ってからfinish()し、onPause()でアラームを止める
            }
            return super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void readSettingsFromPreference(Context context,
                                            int alarmId) {
        // プリファレンスを読み込む
        String name = AlarmPrefManager.ALARM_NAME_BASE + alarmId;
        SharedPreferences pref = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        // TODO: テスト用
        AlarmUtil.dumpCurrentSharedPrerence(pref);

        // スヌーズを使うか
        mUseSnooze = pref.getBoolean("use_snooze", false);

        // バイブを使うか
        mIsVibrateOn = pref.getBoolean("is_viblate_on", false);

        // アラームの音量(最大)
        String maxVolume = pref.getString("max_volume", getResources().getString(R.string.init_max_volume));
        try {
            mMaxVolumePersent = Float.valueOf(maxVolume) / 100f;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            mMaxVolumePersent = Float.valueOf(getResources().getString(R.string.init_max_volume)) / 100f;
        }

        // アラーム音を徐々に大きくするか
        mIsEnabledVolumeInc = pref.getBoolean("need_to_incliment_volume", false);

        // アラーム音量（最小）
        String minVolume = pref.getString("min_volume", getResources().getString(R.string.init_min_volume));
        try {
            mMinVolumePersent = Float.valueOf(minVolume) / 100f;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            mMinVolumePersent = Float.valueOf(getResources().getString(R.string.init_min_volume)) / 100f;
        }
    }

    /**
     * 一定音量、もしくは音量を上げていくための設定を行う
     */
    private void startAlarming() {
        // デバッグ用
        dumpCurrentVolumes();
        // アラーム画面を終了する時に、元々設定してあったボリュームに戻すため、現在値を保存しておく
        int nowVolume = mAudioManager.getStreamVolume(TARGET_STREAM_TYPE);
        mSavedVolume = nowVolume;

        if (mIsEnabledVolumeInc) {
            // 徐々に音量をあげる場合は、まず現在の音量を最小音量に設定
            mCurrentVolume = Math.round(mSystemMaxVolume * mMinVolumePersent);
            mAudioManager.setStreamVolume(TARGET_STREAM_TYPE, mCurrentVolume, VOLUME_FLAG);
            // タスクスタート
            mHandler.postDelayed(mVolumeIncTask, VOLUME_INC_INTERVAL_MSEC);
        } else {
            // 最初から最大音量の場合
            mCurrentVolume = Math.round(mSystemMaxVolume * mMaxVolumePersent);
            mAudioManager.setStreamVolume(TARGET_STREAM_TYPE, mCurrentVolume, VOLUME_FLAG);
        }

        if (mRingtoneManager == null) {
            Uri uri = Uri.parse(mAlarmData.pickedAlarmResource);
            mRingtoneManager = RingtoneManager.getRingtone(this, uri);
        }
        mRingtoneManager.play();
        mPlayingAlarm = true;
    }

    private void dumpCurrentVolumes() {
        int nowVolume = mAudioManager.getStreamVolume(TARGET_STREAM_TYPE);
        Log.d(TAG,
              "now:"
                      + nowVolume
                      + ", maxVolume: "
                      + mSystemMaxVolume
                      + ", minPersent: "
                      + mMinVolumePersent
                      + " -> "
                      + Math.round(mMinVolumePersent * mSystemMaxVolume)
                      + ", maxPersent: "
                      + mMaxVolumePersent
                      + " -> "
                      + Math.round(mMaxVolumePersent * mSystemMaxVolume));
    }

    /**
     * アラームボリュームを上げるために定期的に実行されるタスク
     */
    private final Runnable mVolumeIncTask = new Runnable() {
        @Override
        public void run() {
            // デバッグ用
            dumpCurrentVolumes();
            int nowVolume = mAudioManager.getStreamVolume(TARGET_STREAM_TYPE);

            if ((mCurrentVolume + VOLUME_INC_ONETIME) < Math.round(mSystemMaxVolume * mMaxVolumePersent)) {
                // ボリュームを上げる
                mCurrentVolume += VOLUME_INC_ONETIME;
                mAudioManager.setStreamVolume(TARGET_STREAM_TYPE, mCurrentVolume, VOLUME_FLAG);

                // 今までボリュームが0だったときは、新たに再生をスタートさせる
                if (0 == nowVolume && 0 < mCurrentVolume) {
                    mRingtoneManager.play();
                }

                // 次のタスクをセット
                mHandler.postDelayed(this, VOLUME_INC_INTERVAL_MSEC);
            } else {
                // 最大まで達した時は、最大値をセットしてタスクはセットしない
                mAudioManager.setStreamVolume(TARGET_STREAM_TYPE, Math.round(mSystemMaxVolume * mMaxVolumePersent), VOLUME_FLAG);
            }
        }
    };

    private void stopAlarming() {
        if (mPlayingAlarm) {
            // アラームが鳴っている場合は止める
            mRingtoneManager.stop();
            mPlayingAlarm = false;
        }

        // ボリュームを上げるためのタスクをキャンセル
        mHandler.removeCallbacks(mVolumeIncTask);

        // 目覚まし用の音量ではなく、保存しておいた元々の音量に戻す
        mAudioManager.setStreamVolume(TARGET_STREAM_TYPE, mSavedVolume, 0);
        Log.d(TAG, "restore volume: " + mSavedVolume);
    }

    private final BaseAlarmActivity.OnSetAlarm mOnSetAlarm = new BaseAlarmActivity.OnSetAlarm() {
        @Override
        public void onSetAlarmDone() {
            // 次のアラーム時刻を通知させるために2秒待つ
            mCanPlaying = false; // 戻ってきた時には鳴らさない
            mHandler.postDelayed(mFinishTask, 2000); // 2sec

            // TODO: やばいよサーバに接続してそれを反映させる時は、次の画面を表示させる
            // // 止めた場合は回りのみんながどう反応したか結果画面を表示する
            // Intent intent = new Intent(PluginAlarmActivity.this,
            // YabaiyoActivity.class);
            // startActivity(intent);
        }

        @Override
        public void onSetSnoozeDone() {
            // 次のアラーム時刻を通知させるために2秒待つ
            mCanPlaying = false; // 戻ってきた時には鳴らさない
            mHandler.postDelayed(mFinishTask, 2000); // 2sec
        }
    };

    private final Runnable mFinishTask = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    /**
     * Whether APIからの情報を元に、天気情報を画面に表示する
     * 
     * @param model
     * @return
     */
    private void showWhetherInfomation(WeatherModel model) {
        WeatherModel.CurrentCondition cond = model.getData().getCurrent_condition().get(0);
        if (null == cond) {
            // 取得失敗。緯度経度がおかしい?
            Toast.makeText(this, "天気情報取得失敗", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("気温: ").append(cond.getTemp_C()).append("℃\n");
        sb.append("風速: ").append(cond.getWindspeedKmph()).append("km/h\n");

        TextView t = (TextView) findViewById(R.id.alarm_text_weather);
        t.setText(sb.toString());

        ImageView i = (ImageView) findViewById(R.id.alarm_image_weather);
        UrlImageGetTask task = new UrlImageGetTask(this, i);
        WeatherModel.WeatherIconUrl w = cond.getWeatherIconUrl().get(0);
        task.execute(w.getValue());

        TextView init = (TextView) findViewById(R.id.alarm_text_init);
        init.setVisibility(View.GONE);
        t.setVisibility(View.VISIBLE);
        i.setVisibility(View.VISIBLE);

    }

}
