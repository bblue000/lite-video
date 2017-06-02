package pub.androidrubick.litevideo.panel;

import android.app.Activity;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.Map;
import java.util.WeakHashMap;

import pub.androidrubick.litevideo.panel.autoload.AutoLoadable;
import pub.androidrubick.litevideo.panel.autoload.NetDependStrategy;
import pub.androidrubick.litevideo.panel.cache.VideoCache;
import pub.androidrubick.litevideo.panel.cache.VideoNoCache;
import pub.androidrubick.litevideo.panel.util.LiteUtil;

/**
 *
 * 视频加载、播放的逻辑控制器、管理器。
 *
 * The controller to load video sources, control videos(set uri, play, pause, stop etc.)
 *
 * <p/>
 *
 * 视频播放的控件（{@link VideoView}）本身是可以独立使用的，使用控制器，
 * 能够从界面流/布局的角度来控制视频控件的播放顺序、播放-停止状态的切换等。
 *
 * the {@link VideoView} can use without any controller;
 * but with a derived controller, we can define how videos play in our designed order,
 * or switch play-pause state, etc.
 *
 * <p/>
 *
 * 如果需要在当前界面进入后台时保存状态，可以调用{@link #pauseControl()}；
 * 在重新进入前台时恢复状态，可以调用{@link #resumeControl()}。
 *
 * <p/>
 *
 * 视频缓存加载到播放的主要过程(main process from loading cache to playing)：
 * <pre>
 * set uri(with {@link #setVideoPath}, {@link #setVideoURI)
 * ---try load cache
 *        ---(load success)--->set uri to video
 *                                 ---(when prepared)--->try start--->play video
 *                                 ---(fail loading data, unsupported video, etc.)--->post failed message
 *        ---(load failed)--->post failed message
 * </pre>
 *
 * <p/>
 * Created by Yin Yong on 15/12/25.
 *
 * @since 1.0
 */
public abstract class VideoController implements VideoWidgetController {

    protected static final String TAG = VideoDebug.DEBUG_TAG;
    protected static final boolean DEBUG = VideoDebug.DEBUG_CONTROLLER;

    private WeakHashMap<VideoView, Object> mVideoMap = new WeakHashMap<>(4);
    private VideoCache mVideoCache;
    private AutoLoadable mAutoLoadable;
    protected final TargetPlayInfo mPlaying = new TargetPlayInfo();
    private boolean mAutoPlay;
    private long mLeastLoadingDelay = 1 * 1000 / 2;
    // video operation API start
    /**
     * 自动确定播放哪一项，该方法不推荐在界面刚初始化时调用，
     * 因为初始化时还不能确定视频控件在窗口中的位置。
     *
     * @see #findCurrentPlayVideo()
     */
    public void determinePlay() {
        VideoView video = findCurrentPlayVideo();
        VideoToken token = null;
        if (null != video) {
            attachVideo(video);
            token = video.getToken();
        }

        if (DEBUG) Log.d(TAG, "determine playing: " + mPlaying);
        updateCurrentPlayVideo(token, false);
    }

    /**
     * 如{@link Activity#onPause()}时，调用该方法，将保存当前播放的状态并暂停
     */
    public void pauseControl() {
        mPlaying.saveState();
    }

    /**
     * 由外部调用，如{@link Activity#onResume()}时，调用该方法，将相应地重置状态
     */
    public void resumeControl() {
        mPlaying.restoreState();
    }

    /**
     * 由子类实现，查找当前能够播放的项
     */
    protected abstract VideoView findCurrentPlayVideo();

    @Override
    public void setVideoPath(VideoView video, String path) {
        setVideoURI(video, Uri.parse(path));
    }

    @Override
    public void setVideoURI(VideoView video, Uri uri) {
        setVideoURI(video, uri, null);
    }

    @Override
    public void setVideoURI(VideoView video, Uri uri, Map<String, String> headers) {
        attachVideo(video);
        VideoToken token = video.getToken();
        // 重新设置时，检测是否是当前播放项，如果是，则重置其状态
        mPlaying.resetIfMatch(token, uri);
        if (null != token) {
            token.setVideoURI(uri, headers);
            token.seekWhenPrepared = 0; // reset
            token.currentState = VideoToken.STATE_PREPARING;
            dispatchDownload(token, false);
        }

        if (mAutoPlay) {
            start(video);
        }
    }

    public void setAutoPlay(boolean autoPlay) {
        mAutoPlay = autoPlay;
    }

    public boolean isAutoPlay() {
        return mAutoPlay;
    }

    @Override
    public void start(VideoView video) {
        attachVideo(video);
        VideoToken token = video.getToken();
        updateCurrentPlayVideo(token, true);
    }

    @Override
    public boolean isPlaying(VideoView video) {
        attachVideo(video);
        return mPlaying.token == video.getToken() && video.isPlaying();
    }

    @Override
    public void seekTo(VideoView video, int msec) {
        attachVideo(video);
        VideoToken token = video.getToken();
        if (token.isInPlaybackState()) {
            token.seekWhenPrepared = 0;
            doVideoSeekTo(video.getToken(), msec);
        } else {
            token.seekWhenPrepared = msec;
        }
    }

    @Override
    public void pause(VideoView video) {
        attachVideo(video);
        VideoToken token = video.getToken();
        if (token.isInPlaybackState()) {
            token.currentState = VideoToken.STATE_PAUSED;
        }
        token.targetState = VideoToken.STATE_PAUSED;
        doVideoPause(token);
    }

    @Override
    public void stop(VideoView video) {
        attachVideo(video);
        VideoToken token = video.getToken();
        token.currentState = VideoToken.STATE_IDLE;
        token.targetState = VideoToken.STATE_IDLE;
        doVideoStop(video.getToken());
    }

    @Override
    public void setControlCallback(VideoView video, VideoControlCallback callback) {
        attachVideo(video);
        VideoToken token = video.getToken();
        token.stateCb = callback;
        if (null != token.stateCb && token.video.isPlaying()) {
            postState(VideoControlCallback.STATE_START, token, null);
        }
    }
    // video operation API end

    // settings API start
    /**
     * 至少需要加载的时间，为了让已经缓存的视频仍然显得有加载过程。
     */
    public VideoController leastLoadingDelay(long msec) {
        mLeastLoadingDelay = msec;
        return this;
    }

    /**
     * 设置自动播放策略，默认为{@link NetDependStrategy}。
     */
    public VideoController setAutoLoadStrategy(AutoLoadable strategy) {
        mAutoLoadable = strategy;
        return this;
    }

    /**
     * 设置缓存管理
     */
    public VideoController setCache(VideoCache cache) {
        this.mVideoCache = cache;
        return this;
    }

    public AutoLoadable getAutoLoadStrategy() {
        if (null == mAutoLoadable) {
            mAutoLoadable = createDefaultAutoLoadStrategy();
        }
        return mAutoLoadable;
    }

    protected AutoLoadable createDefaultAutoLoadStrategy() {
        return AutoLoadable.DUMMY;
    }

    /**
     * 获取当前使用的缓存管理
     */
    public VideoCache getCache() {
        if (null == mVideoCache) {
            mVideoCache = createDefaultCache();
        }
        return mVideoCache;
    }

    /**
     * 创建默认的
     */
    protected VideoCache createDefaultCache() {
        VideoNoCache cache = new VideoNoCache();
        cache.setAtLeastDelay(mLeastLoadingDelay);
        return cache;
    }

    /**
     * 销毁中间数据，一般是在界面关闭时调用
     */
    public void destroy() {
        mVideoMap.clear();
    }

    /*package*/ void reset() {
        mPlaying.reset();
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }

    // inner implements start
    /**
     * 该方法只用于确定，如果不需要加载播放，则调用相应方法
     */
    protected void updateCurrentPlayVideo(VideoToken token, boolean manual) {
        if (mPlaying.isset()) { // 如果之前有播放项
            // 如果已经设置了，且URL改变了，
            if (mPlaying.match(token)) { // 如果什么都没有改变
                if (mPlaying.prepared) { // 已经处于准备好播放的状态
                    startTargetVideo(mPlaying.token);
                    return;
                }
                // 否则，也是走加载流程
            } else {
                stopPrevious(mPlaying.token);
            }
        }
        mPlaying.set(token, manual);
        if (dispatchDownload(token, manual)) {
            postState(VideoControlCallback.STATE_LOADING, token, null);
        }
    }

    protected void startTargetVideo(VideoToken token) {
        if (token.isInPlaybackState()) {
            token.currentState = VideoToken.STATE_PLAYING;
        }
        token.targetState = VideoToken.STATE_PLAYING;
        doVideoStart(token); // FIXME 是否在这里做，还是确定控件没有开始播放时才start
    }

    /**
     * 在加载完成后调用，看是否能够播放当前项
     */
    protected void tryPlayCurrent() {
        if (!mPlaying.isset()) return; // 如果没有可播放项，则直接返回

        final VideoToken current = mPlaying.token;
        if (null == current.playUri) return;// 如果当前的播放Uri尚未加载完成，直接返回

        if (DEBUG) Log.e(TAG, current.video + " start uri : " + current.uri);
        if (DEBUG) Log.w(TAG, current.video + " start play uri: " + getPlayUriFileName(current.playUri));

        doSetVideoUri(current, current.playUri, current.headers);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_DELAY_PLAY, current), mLeastLoadingDelay);
    }

    protected void playCurrentDelayed(VideoToken current) {
        if (!mPlaying.match(current)) return; // 再次验证

        if (mPlaying.prepared && null != mPlaying.token.playUri) {
            if (DEBUG) Log.d(TAG, current.video + " start...");
            if (current.seekWhenPrepared != 0) {
                doVideoSeekTo(current, current.seekWhenPrepared);
                current.seekWhenPrepared = 0;
            }
            startTargetVideo(current);
        } else {
            if (DEBUG) Log.d(TAG, current.video + " no play uri");
            // 这种情况不执行播放
        }
    }

    protected void stopPrevious(VideoToken previous) {
        mHandler.removeMessages(MSG_DELAY_PLAY); // 不能忘记移除所有的播放message
        stop(previous.video);
    }

    // 直接调用video的方法，并相应地处理各自的回调（如果有的话）
    /**
     * 此处根据设置的Uri和headers尝试下载
     */
    protected void doSetVideoUri(VideoToken token, Uri uri, Map<String, String> headers) {
        token.video.setVideoURICompat(uri, headers);
    }

    protected void doVideoStart(VideoToken token) {
        token.video.start();
        postState(VideoControlCallback.STATE_START, token, null);
    }

    protected void doVideoSeekTo(VideoToken token, int msec) {
        token.video.seekTo(msec);
    }

    protected void doVideoPause(VideoToken token) {
        boolean isPlaying = token.video.isPlaying();
        token.video.pause(); // just do pause
        // FIXME: 暂时先不验证
//        if (isPlaying ^ token.video.isPlaying()) {
            postState(VideoControlCallback.STATE_PAUSE, token, null);
//        }
    }

    protected void doVideoStop(VideoToken token) {
        token.video.stop();
        // 无论如何都发送一个停止的回调
        postState(VideoControlCallback.STATE_STOP, token, null);
    }
    // end

    /**
     * {@link #doSetVideoUri(VideoToken, Uri, Map)}操作中，发现相应的uri还没有加载到playUri时将调用该方法
     *
     * @param force 如果视频资源尚未加载成功，是否强制加载（即使{@link #getAutoLoadStrategy()#autoLoad(VideoView)}返回false）
     *
     * @return 返回true表明需要下载，后续将会通过{@link #onVideoDownloaded(VideoToken, String, Uri)}
     * 或者{@link #onVideoCacheFailed(VideoToken, String, VideoControlCallback.VideoStatus)}
     * 等相关方法返回结果。
     */
    protected boolean dispatchDownload(VideoToken token, boolean force) {
        if (null != token) {
            if (null == token.playUri) { // 如果还没有下载资源
                if (force || getAutoLoadStrategy().autoLoad(token.video)) { // 如果允许自动加载播放，则往下执行
                    getCache().load(token, mCacheCallback);
                    return true;
                }
            } else {
                onVideoDownloaded(token, String.valueOf(token.uri), token.playUri);
                return true;
            }
        }
        return false;
    }

    /**
     * 当设置了本地播放资源，视频控件反馈可以播放时调用
     */
    protected void onVideoPrepared(VideoToken token) {
        token.currentState = VideoToken.STATE_PREPARED;
        postState(VideoControlCallback.STATE_PREPARED, token, null);
        // 看看是否是当前项加载好了
        if (mPlaying.match(token) && null != mPlaying.token.playUri) {
            mPlaying.prepared = true;
            // try play current
            mHandler.removeMessages(MSG_DELAY_PLAY);
            // 特别处理一下预设，如果目标的状态是暂停或者停止，特别处理一下
            if (token.targetState > VideoToken.STATE_PLAYING ) {
//                switch (token.targetState) {
//                    case VideoViewToken.STATE_PAUSED:
//                        pause(token.video);
//                        break;
//                    case VideoViewToken.STATE_PLAYBACK_COMPLETED:
//                        stop(token.video);
//                        break;
//                }
            } else {
                playCurrentDelayed(token);
            }
        }
    }

    /**
     * 当设置了本地播放资源，视频控件反馈播放完成时调用
     */
    protected void onVideoPlayCompleted(VideoToken token) {
        token.currentState = VideoToken.STATE_PLAYBACK_COMPLETED;
        token.targetState = VideoToken.STATE_PLAYBACK_COMPLETED;
        postState(VideoControlCallback.STATE_COMPLETION, token, null);
    }

    /**
     * 当设置了本地播放资源，视频控件反馈播放错误时调用。
     *
     * 出现错误也应当告诉外界视频播放结束了（即后续会立即触发进入{@link VideoControlCallback#STATE_STOP}）
     *
     * @return 如果做了一定的处理，则返回true，否则返回false
     */
    protected boolean onVideoPlayError(VideoToken token, VideoControlCallback.VideoStatus state) {
        if (DEBUG) Log.e(TAG, String.valueOf(state));
        token.currentState = VideoToken.STATE_ERROR;
        token.targetState = VideoToken.STATE_ERROR;
        postState(VideoControlCallback.STATE_ERR, token, state);
        postState(VideoControlCallback.STATE_STOP, token, state);
        return true;
    }

    // 设置必要的token信息
    protected void attachVideo(VideoView video) {
        if (!videoAttached(video)) {
            // mVideoInfoMap.put(video, new VideoViewToken(this, video));
            video.setToken(new VideoToken(this, video));
            mVideoMap.put(video, this);
            video.setInnerAPIOnPreparedListener(mOnPreparedListener);
            video.setInnerAPIOnCompletionListener(mOnCompletionListener);
            video.setInnerAPIOnErrorListener(mOnErrorListener);
            video.setInnerAPIOnInfoListener(mOnInfoListener);
        }
    }

    // 从管理器中删除制定的控件及其相应的token，不再对其进行管理
    protected void detachVideo(VideoView video) {
        mVideoMap.remove(video);
        video.setToken(null);
        video.setInnerAPIOnPreparedListener(null);
        video.setInnerAPIOnCompletionListener(null);
        video.setInnerAPIOnErrorListener(null);
        video.setInnerAPIOnInfoListener(null);
    }

    protected boolean videoAttached(VideoView video) {
        return null != video && null != video.getToken() && this == video.getToken().controller;
    }

    // =============================================
    // 下载的统一管理
    // =============================================
    protected void onVideoCacheProgress(VideoToken token, String uri, long current, long total) {
        if (mPlaying.match(token, uri) && null == mPlaying.token.playUri) {
            if (DEBUG) Log.d(TAG, "onVideoCacheProgress");
            postCacheProgress(token, uri, current, total);
        }
    }

    /**
     * 视频缓存好时调用
     */
    protected void onVideoDownloaded(VideoToken token, String uri, Uri target) {
        if (token.matchUri(uri)) {
            token.playUri = target;
        }
        if (mPlaying.match(token, uri)) {
            // 如果是当前播放项，且URL相同，则播放
            tryPlayCurrent();
        }
    }

    /**
     * 视频缓存出错时调用
     */
    protected void onVideoCacheFailed(VideoToken token, String uri, VideoControlCallback.VideoStatus status) {
        if (mPlaying.match(token, uri) && null == mPlaying.token.playUri) {
            // 如果是当前播放项没有改变起始的URL，且没有下载完成（playUri is null）
            if (DEBUG) Log.e(TAG, "onVideoCacheFailed");
            // send message
            postState(VideoControlCallback.STATE_LOAD_ERR, token, status);
        }
    }

    private VideoCache.CacheCallback mCacheCallback = new VideoCache.CacheCallback() {

        @Override
        public void onCacheProgress(VideoToken token, String uri, long current, long total) {
            onVideoCacheProgress(token, uri, current, total);
        }

        @Override
        public void onCacheSuccess(VideoToken token, String uri, Uri target) {
            if (DEBUG) Log.d(TAG, uri.substring(uri.lastIndexOf("/") + 1) + " downloaded ");
            onVideoDownloaded(token, uri, target);
        }

        @Override
        public void onCacheFailed(VideoToken token, String uri, VideoControlCallback.VideoStatus status) {
            if (DEBUG) Log.d(TAG, uri.substring(uri.lastIndexOf("/") + 1) + " download failed");
            onVideoCacheFailed(token, uri, status);
        }

    };

    private VideoView.OnPreparedListener mOnPreparedListener = new VideoView.OnPreparedListener() {
        @Override
        public void onPrepared(VideoView video, MediaPlayer mp) {
            if (videoAttached(video)) {
                onVideoPrepared(video.getToken());
            }
        }
    };

    private VideoView.OnCompletionListener mOnCompletionListener = new VideoView.OnCompletionListener() {
        @Override
        public void onCompletion(VideoView video, MediaPlayer mp) {
            if (videoAttached(video)) {
                onVideoPlayCompleted(video.getToken());
            }
        }
    };

    private VideoView.OnErrorListener mOnErrorListener = new VideoView.OnErrorListener() {

        @Override
        public boolean onError(VideoView video, MediaPlayer mp, int what, int extra) {
            if (videoAttached(video)) {
                return onVideoPlayError(video.getToken(), new VideoControlCallback.VideoStatus(what, "error").extraCode(extra));
            }
            return false;
        }

    };

    private VideoView.OnInfoListener mOnInfoListener = new VideoView.OnInfoListener() {

        @Override
        public boolean onInfo(VideoView video, MediaPlayer mp, int what, int extra) {
            // 在此特殊处理一下
            if (videoAttached(video)) {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        postState(VideoControlCallback.STATE_LOADING, video.getToken(), new VideoControlCallback.VideoStatus(what, "info").extraCode(extra));
                        break;
                }
                return true;
            }
            return false;
        }
    };

    protected void postState(int what, VideoToken token, VideoControlCallback.VideoStatus status) {
        Message msg;
        if (null == status) {
            msg = Message.obtain(mHandler, MSG_STATE, what, 0, token);
        } else {
            msg = Message.obtain(mHandler, MSG_STATE, what, 0, new MergeToken(token, status));
        }
        if (LiteUtil.isMainThread()) {
            mHandler.handleMessage(msg);
            msg.recycle();
        } else {
            msg.sendToTarget();
        }
    }

    protected void postCacheProgress(VideoToken token, String uri, long current, long total) {
        Message msg = Message.obtain(mHandler, MSG_CACHE_PROGRESS, token);
        Bundle data = new Bundle(3);
        data.putString(DATA_URI, uri);
        data.putLong(DATA_CURRENT_SIZE, current);
        data.putLong(DATA_TOTAL_SIZE, total);
        msg.setData(data);
        if (LiteUtil.isMainThread()) {
            mHandler.handleMessage(msg);
            msg.recycle();
        } else {
            msg.sendToTarget();
        }
    }

    private class MergeToken {
        public final VideoToken target;
        public final VideoControlCallback.VideoStatus param;

        private MergeToken(VideoToken target, VideoControlCallback.VideoStatus param) {
            this.target = target;
            this.param = param;
        }
    }

    private final int MSG_DELAY_PLAY = -1;
    private final int MSG_STATE = 0;
    private final int MSG_CACHE_PROGRESS = 1;
    private final String DATA_URI = "uri";
    private final String DATA_CURRENT_SIZE = "current";
    private final String DATA_TOTAL_SIZE = "total";
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_STATE: {
                    VideoToken token;
                    VideoControlCallback.VideoStatus status = null;
                    if (msg.obj instanceof MergeToken) {
                        MergeToken mergeToken = (MergeToken) msg.obj;
                        token = mergeToken.target;
                        status = mergeToken.param;
                    } else {
                        token = (VideoToken) msg.obj;
                    }
                    final VideoControlCallback controlCallback = token.stateCb;
                    if (null != controlCallback) {
                        controlCallback.onStateChanged(token.video, msg.arg1, status);
                    }
                }
                    break;
                case MSG_CACHE_PROGRESS: {
                    VideoToken token = (VideoToken) msg.obj;
                    final VideoControlCallback controlCallback = token.stateCb;
                    if (null != controlCallback) {
                        controlCallback.onLoadProgress(token.video, msg.getData().getString(DATA_URI),
                                msg.getData().getLong(DATA_CURRENT_SIZE), msg.getData().getLong(DATA_TOTAL_SIZE));
                    }
                }
                    break;
                case MSG_DELAY_PLAY:
                    playCurrentDelayed((VideoToken) msg.obj);
                    break;
            }
        }
    };

    /**
     * 当前准备/正在播放的项
     */
    public class TargetPlayInfo {
        public VideoToken token; // 当前的视频项
        public String url; // 指定播放时的URL
        public boolean prepared; // playUri有没有设置过，如果设置过，之后的播放暂停等操作我们不再需要过多地涉及
        public boolean manual; // 是否是用户手动触发的

        /**
         * 是否设置有当前播放项
         */
        public boolean isset() {
            return null != token;
        }

        public boolean match(VideoToken token, String uri) {
            return isset() && this.token == token && LiteUtil.equals(uri, this.url);
        }

        public boolean match(VideoToken token) {
            return isset() && this.token == token && token.matchUri(this.url);
        }

        public void resetIfMatch(VideoToken token, Uri uri) {
            if (isset() && this.token == token/* && !ObjectUtils.equals(String.valueOf(uri), this.url)*/) {
                if (prepared) {
                    stopPrevious(this.token);
                }
                reset();
            }
        }

        public void reset() {
            token = null;
            url = null;
            prepared = false;
            manual = false;
            isPlayingWhenPauseControl = false;
        }

        public void set(VideoToken token, boolean manual) {
            reset();
            if (null != token) {
                this.token = token;
                this.url = String.valueOf(this.token.uri); // 即时地保存url到临时字段
                this.manual = manual;
            }
        }

        // 保存/恢复状态 start
        private boolean isPlayingWhenPauseControl;
        private int posWhenPauseControl;
        public void saveState() {
            if (isset()) {
                isPlayingWhenPauseControl = token.video.isPlaying();
                posWhenPauseControl = token.video.getCurrentPosition();
            }
        }

        public void restoreState() {
            if (isset()) {
                if (posWhenPauseControl != 0) {
                    seekTo(token.video, posWhenPauseControl);
                    if (isPlayingWhenPauseControl) {
                        start(token.video);
                    } else {
                        pause(token.video);
                    }
                } else {
                    if (isPlayingWhenPauseControl) {
                        start(token.video);
                    }
                }
            }
            isPlayingWhenPauseControl = false;
            posWhenPauseControl = 0;
        }
        // end

        @Override
        public String toString() {
            return "token = " + token + ", url = " + url;
        }
    }

    // =============================================
    // util
    // =============================================
    protected Rect mTempRect = new Rect();
    protected int[] mTempLoc = new int[2];
    /**
     * 是否在指定父控件的可视范围之内
     */
    protected boolean isInViewport(VideoView video, ViewGroup parent) {
        if (null == video || null == parent || !parent.getGlobalVisibleRect(mTempRect)) { // 没有可显示的区域
            return false;
        }
        final int top = mTempRect.top;
        video.getLocationOnScreen(mTempLoc);
        return mTempLoc[1] + video.getHeight() > top;
    }

    /**
     * 判断是否在其直接父控件的可是范围内
     */
    protected boolean isInParentViewPort(VideoView video) {
        if (null == video || null == video.getParent()) {
            return false;
        }
        ViewParent parent = video.getParent();
        if (parent instanceof ViewGroup) {
            return isInViewport(video, (ViewGroup) parent);
        }
        return false;
    }

    /**
     * 获取uri中的文件名
     */
    public static String getPlayUriFileName(Uri uri) {
        return null == uri ? null : uri.getLastPathSegment();
    }
}
