package jp.androidapp.apps.pluggablealarm.plugin.tamesarerualarm.util;

import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class UrlImageGetTask
    extends AsyncTask<String, Integer, Drawable> {
    private static final String TAG = "UrlImageGetTask";
    private ImageView mImageView = null;

    public UrlImageGetTask(Context context,
                           ImageView imageView) {
        super();
        mImageView = imageView;
    }

    /**
     * UIスレッドでのプログレス表示用
     */
    @Override
    protected void onPreExecute() {
    }

    /**
     * バックグラウンドでのスレッド処理用
     */
    @Override
    protected Drawable doInBackground(String... params) {
        Log.d(TAG, "enter doInBackground()");
        String urlString = params[0];
        if (urlString == null) {
            Log.d(TAG, "must set URL!");
            return null;
        } else {
            try {
                Log.d(TAG, "url: " + urlString);
                // URLクラス
                URL url = new URL(urlString);
                // 入力ストリームを開く
                InputStream istream = url.openStream();

                // 画像をDrawableで取得
                Drawable d = Drawable.createFromStream(istream, "webimg");

                // 入力ストリームを閉じる
                istream.close();

                return d;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                return null;
            }
        }
    }

    /**
     * UIスレッドでの処理用
     */
    @Override
    protected void onPostExecute(Drawable result) {
        Log.d(TAG, "enter onPostExecute() user_id:" + result);
        if (null != mImageView && null != result) {
            mImageView.setImageDrawable(result);
        }
    }
}
