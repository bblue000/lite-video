package pub.androidrubick.litevideo.panel;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import java.util.Map;

import pub.androidrubick.widget.litevideo.BaseCloneableVideoContainer;
import pub.androidrubick.widget.litevideo.VideoWidgetBase;

/**
 * {@doc}
 *
 * <p/>
 *
 * Created by Yin Yong on 2017/6/1.
 */
public abstract class BaseVideoPanel extends BaseCloneableVideoContainer
    implements VideoWidgetBase, VideoControlCallback {

    private final String DEBUG_TAG = "BaseVideoPanel";

    public BaseVideoPanel(Context context) {
        this(context, null);
    }

    public BaseVideoPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseVideoPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initBase(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);

        initVideoCallbacks();
    }

    protected void initBase(Context context, AttributeSet attrs, int defStyleAttr) {

    }

    protected abstract void initView(Context context, AttributeSet attrs, int defStyleAttr) ;

    private void initVideoCallbacks() {
        VideoView videoView = getVideo();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                postState(STATE_PREPARED, null);
            }
        });
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        postState(STATE_LOADING, new VideoStatus(what, "info").extraCode(extra));
                        return true;
                }
                return false;
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                postState(STATE_COMPLETION, null);
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                VideoStatus status = new VideoStatus(what, "error").extraCode(extra);
                postState(STATE_ERR, status);
                postState(STATE_STOP, status);
                return true;
            }
        });
    }

    protected abstract VideoView getVideo();

    // video state callbacks
    /**
     * state for:
     *
     * 1. uri not set;
     * 2. uri set, but do nothing else
     */
    protected abstract void enterIdleState() ;

    /**
     * state for:
     *
     * 1. uri set, and prepared
     * 2. first enter start state (from idle state or stop)
     */
    protected abstract void enterPlaybackNormalState() ;

    /**
     * state for:
     *
     * start playing
     *
     * @param fromPause whether enter this state from pause state,
     *                  if not we consider it as `first enter start state`
     */
    protected abstract void enterPlayState(boolean fromPause) ;

    /**
     * state for:
     *
     * paused
     */
    protected abstract void enterPauseState() ;

    @Override
    public void onStateChanged(VideoView video, int state, VideoStatus status) {
        switch (state) {
            case STATE_LOADING:
                Log.i(DEBUG_TAG, "onStateChanged loading");
                onStateLoading(status);
                break;
            case STATE_PREPARED:
                Log.i(DEBUG_TAG, "onStateChanged prepared");
                onStatePrepared();
                break;
            case STATE_START:
                Log.i(DEBUG_TAG, "onStateChanged start");
                onStateStart();
                break;
            case STATE_PAUSE:
                Log.i(DEBUG_TAG, "onStateChanged pause");
                onStatePause();
                break;
            case STATE_COMPLETION:
                Log.i(DEBUG_TAG, "onStateChanged completion");
                onStateCompletion();
                break;
            case STATE_STOP:
                Log.i(DEBUG_TAG, "onStateChanged stop");
                onStateStop();
                break;
            case STATE_LOAD_ERR:
                Log.i(DEBUG_TAG, "onStateChanged load err");
                onStateLoadErr(status);
                break;
            case STATE_ERR:
                Log.i(DEBUG_TAG, "onStateChanged err");
                onStateErr(status);
                break;
        }
    }

    @Override
    public void onLoadProgress(VideoView video, String url, long current, long total) {

    }

    protected boolean mInPlaybackStateFlag;
    protected boolean mInPlayingFlag;
    protected final void onStateLoading(VideoStatus status) {
        if (null != status) {
            switch (status.code) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    // FIXME: 是否需要mInPlayingFlag
                    if (getVideo().isInPlaybackState() && mInPlayingFlag) {
                        onStateLoadingStartWhenPlaying();
                    }
                    return;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    // FIXME: 是否需要mInPlayingFlag
                    if (getVideo().isInPlaybackState() && mInPlayingFlag) {
                        onStateLoadingEndWhenPlaying();
                    }
                    return;
            }
        }
        // 其他情况，则是开始时候的加载
        onStateStartLoading();
    }

    protected void onStateStartLoading() {
        enterIdleState();
    }

    /**
     * 指示播放的过程加载数据
     */
    protected void onStateLoadingStartWhenPlaying() {
    }

    /**
     * 指示播放的过程加载数据完成
     */
    protected void onStateLoadingEndWhenPlaying() {
    }

    protected void onStatePrepared() {
        enterPlaybackNormalState();
    }

    protected void onStateStart() {
        if (!mInPlaybackStateFlag && getVideo().isInPlaybackState()) {
            enterPlaybackNormalState();
            enterPlayState(false);
            mInPlaybackStateFlag = true;
        }

        if (mInPlaybackStateFlag && !mInPlayingFlag) {
            enterPlayState(true);
            mInPlayingFlag = true;
        }
    }

    private void checkEnterPausePlayState() {
        if (mInPlayingFlag) {
            mInPlayingFlag = false;
        }
        enterPauseState();
    }

    protected void onStatePause() {
        checkEnterPausePlayState();
    }

    protected void onStateCompletion() {
        checkEnterPausePlayState();

        // 播放结束后，无论如何显示进度条
        enterPlaybackNormalState();
    }

    protected void onStateStop() {
        checkEnterPausePlayState();
        if (mInPlaybackStateFlag) {
            mInPlaybackStateFlag = false;
        }
        /**
         * 退出播放状态（从最近一次开始播放停止时触发）。
         *
         * 暂停不会触发该状态的回调。
         *
         * 该回调之后会立即调用到{@link #enterInPlaybackNormalState}
         */
        enterIdleState();
    }

    protected void onStateLoadErr(VideoStatus status) {
        onStateStop();
    }

    protected void onStateErr(VideoStatus status) {
        onStateStop();
    }

    private final int MSG_STATE = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_STATE:
                    onStateChanged(getVideo(), msg.arg1, (VideoStatus) msg.obj);
                    break;

            }
        }
    };
    private void postState(int state, VideoStatus status) {
        Message msg = Message.obtain(mHandler, MSG_STATE, state, 0, status);
        if (Looper.getMainLooper() == Looper.myLooper()) {
            mHandler.handleMessage(msg);
            msg.recycle();
        } else {
            msg.sendToTarget();
        }
    }

    @Override
    public Uri getUri() {
        return getVideo().getUri();
    }

    @Override
    public void setVideoPath(String url) {
        setVideoURI(Uri.parse(url));
    }

    @Override
    public void setVideoURI(Uri uri) {
        setVideoURICompat(uri, null);
    }

    @Override
    public void setVideoURICompat(Uri uri, Map<String, String> headers) {
        getVideo().setVideoURICompat(uri, headers);
    }

    @Override
    public boolean isPlaying() {
        return getVideo().isPlaying();
    }

    @Override
    public void start() {

    }

    @Override
    public void seekTo(int msec) {
        getVideo().seekTo(msec);
    }

    @Override
    public void pause() {
        getVideo().pause();
        postState(STATE_PAUSE, null);
    }

    @Override
    public void stop() {
        getVideo().stop();
        postState(STATE_STOP, null);
    }

    @Override
    public int getDuration() {
        return getVideo().getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return getVideo().getCurrentPosition();
    }

    @Override
    public int getBufferPercentage() {
        return getVideo().getBufferPercentage();
    }
}
