package jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm;

import java.util.UUID;

import android.content.Context;

public class UUIDManager {
    
    public static String getUUID(Context context) {
        return UUID.randomUUID().toString();
    }    

}
