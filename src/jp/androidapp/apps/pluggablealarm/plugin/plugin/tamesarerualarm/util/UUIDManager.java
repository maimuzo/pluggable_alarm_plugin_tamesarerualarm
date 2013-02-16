package jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm.util;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UUIDManager {
	private static final String PREF_UUID_NAME = "pref_uuid";
	private static final String PREF_KEY_UUID = "pref_key_uuid";
    
    public static String getUUID(Context context) {
    	SharedPreferences pref = context.getSharedPreferences(PREF_UUID_NAME, Context.MODE_PRIVATE);
    	String uuid = pref.getString(PREF_KEY_UUID, "");
    	if("".equals(uuid)){
    		Editor editor = pref.edit();
    		uuid = UUID.randomUUID().toString();
    		editor.putString(PREF_KEY_UUID, uuid);
    		editor.commit();
    	}
        return uuid;
    }    

}
