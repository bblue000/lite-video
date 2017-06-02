package pub.androidrubick.litevideo.panel.cache;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.File;

import pub.androidrubick.litevideo.panel.VideoToken;

/**
 *
 * 默认缓存；
 *
 * Created by Yin Yong on 15/12/27.
 */
public class VideoNoCache implements VideoCache {

    private Handler mHandler;
    private long mAtLeastDelay;
    public void setAtLeastDelay(long delayInMillis) {
        mAtLeastDelay = delayInMillis;
    }

    @Override
    public void load(VideoToken video, CacheCallback callback) {
        Uri uri = video.uri;
        if (null == uri) return;

        final String url = String.valueOf(uri);
        final String scheme = uri.getScheme();

        // 过滤掉一些系统本就支持的项
        if (ContentResolver.SCHEME_FILE.equals(scheme)
                || ContentResolver.SCHEME_CONTENT.equals(scheme)
                || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            performSuccess(video, url, uri, callback, true);
            return;
        }

//        callback.onCacheFailed(video, url, new VideoControlCallback.VideoStatus(
//                -1,
//                "not support"
//        ));

        performSuccess(video, url, uri, callback, false);

        // do HTTP/HTTPS \, etc. cache
//        VideoAjaxCallback.download(video, callback);
    }

    private void performSuccess(final VideoToken video, final String url, final Uri uri,
                                final CacheCallback callback, boolean local) {
        if (mAtLeastDelay > 0) {
            long d = local ? (long) (mAtLeastDelay * 0.618) : mAtLeastDelay;
            if (null == mHandler) {
                mHandler = new Handler(Looper.getMainLooper());
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    callback.onCacheSuccess(video, url, uri);
                }
            }, d);
        } else {
            callback.onCacheSuccess(video, url, uri);
        }
    }

    @Override
    public File getCacheDir(Context context) {
        return null;
    }

    @Override
    public void clearCache(Context context) {
    }
}
