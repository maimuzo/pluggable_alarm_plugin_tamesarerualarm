package jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm.util;

import jp.androidapp.apps.pluggablealarm.plugin.plugin.tamesarerualarm.model.WeatherModel;
import net.arnx.jsonic.JSON;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

public class WeatherUtil {
//	private static final String SAMPLE = "http://free.worldweatheronline.com/feed/weather.ashx?q=43.01,141.41&format=json&num_of_days=0&key=2aad2a377b012628130202";
	private static final String ENDPOINT_URL_BASE = "http://free.worldweatheronline.com/feed/weather.ashx?format=json&num_of_days=0&key=";
	private static final String API_KEY = "2aad2a377b012628130202"; // maimuzoが取得したもの
	
	private static String getEndpointUrl(String lat, String lon){
		return ENDPOINT_URL_BASE + API_KEY + "&" + lat + "," + lon;
	}
	
	public static void getCurrent(Context context, String lat, String lon, OnAccessListener listener){
		WeatherAccessTask task = new WeatherAccessTask(context, listener);
		task.execute(getEndpointUrl(lat, lon));
	}
	
	public interface OnAccessListener{
		void onAccess(WeatherModel model);
	}
}
