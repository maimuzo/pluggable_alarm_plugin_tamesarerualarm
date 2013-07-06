package jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class YabaiyoActivity
    extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yabaiyo);

        findViewById(R.id.yabaiyo_button_yabai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO: uuidとlatとlonをヤバイよサーバに届ける
                // 強制終了
                moveTaskToBack(true);
            }
        });
        findViewById(R.id.yabaiyo_button_yabakunai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 強制終了
                moveTaskToBack(true);
            }
        });
    }

    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // // Inflate the menu; this adds items to the action bar if it is present.
    // getMenuInflater().inflate(R.menu.activity_plugin_main, menu);
    // return true;
    // }

}
