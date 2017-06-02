package pub.androidrubick.litevideo.panel;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import pub.androidrubick.widget.litevideo.TextureVideoView;

/**
 * A customized texture video view.
 *
 * <p>
 * Created by Yin Yong on 2017/6/2.
 */
public class VideoView extends TextureVideoView {

    private static final String TAG = VideoDebug.DEBUG_TAG;
    private static final boolean DEBUG = VideoDebug.DEBUG_VIEW;
    
    public VideoView(Context context) {
        this(context, null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        super.setOnPreparedListener(mPreparedListener);
        super.setOnInfoListener(mInfoListener);
        super.setOnCompletionListener(mCompletionListener);
        super.setOnErrorListener(mErrorListener);
        super.setOnSeekCompleteListener(mSeekCompleteListener);
    }

    @Override
    public boolean isInPlaybackState() {
        return super.isInPlaybackState();
    }

    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    @Override
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    private MediaPlayer.OnInfoListener mOnInfoListener;
    @Override
    public void setOnInfoListener(MediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    @Override
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    private MediaPlayer.OnErrorListener mOnErrorListener;
    @Override
    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    // ==================================================
    // ==================================================
    // ==================================================
    // for inner APIs
    private OnPreparedListener mInnerAPIOnPreparedListener;
    private OnCompletionListener mInnerAPIOnCompletionListener;
    private OnErrorListener mInnerAPIOnErrorListener;
    private OnInfoListener mInnerAPIOnInfoListener;
    /*package*/ void setInnerAPIOnPreparedListener(OnPreparedListener l) {
        mInnerAPIOnPreparedListener = l;
    }
    /*package*/ void setInnerAPIOnCompletionListener(OnCompletionListener l) {
        mInnerAPIOnCompletionListener = l;
    }
    /*package*/ void setInnerAPIOnErrorListener(OnErrorListener l) {
        mInnerAPIOnErrorListener = l;
    }
    /*package*/ void setInnerAPIOnInfoListener(OnInfoListener l) {
        mInnerAPIOnInfoListener = l;
    }

    private VideoToken mToken;
    /*package*/ void setToken(VideoToken token) {
        mToken = token;
    }
    /*package*/ VideoToken getToken() {
        return mToken;
    }

    @Override
    protected void onDetachedFromWindow() {
        final VideoToken token = mToken;
        if (null != token) {
            token.controller.detachVideo(this);
        }
        super.onDetachedFromWindow();
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (DEBUG) Log.d(TAG, this + " prepared");

            // hook info here
            // 因为VideoView的setOnInfoListener在API17时才加入
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (null != mp) {
                    mp.setOnInfoListener(mInfoListener);
                }
            }

            if (null != mInnerAPIOnPreparedListener) {
                mInnerAPIOnPreparedListener.onPrepared(VideoView.this, mp);
            }
            if (null != mOnPreparedListener) {
                mOnPreparedListener.onPrepared(mp);
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (null != mInnerAPIOnCompletionListener) {
                mInnerAPIOnCompletionListener.onCompletion(VideoView.this, mp);
            }
            if (null != mOnCompletionListener) {
                mOnCompletionListener.onCompletion(mp);
            }
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            /* If an error handler has been supplied, use it and finish. */
            if (DEBUG) Log.d(TAG, "Error: " + what + "," + extra);
            boolean ret = true;
            if (null != mInnerAPIOnErrorListener) {
                ret = mInnerAPIOnErrorListener.onError(VideoView.this, mp, what, extra);
            }
            if (null != mOnErrorListener) {
                ret = mOnErrorListener.onError(mp, what, extra) || ret;
            }
            return ret;
        }
    };

    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            /* If an error handler has been supplied, use it and finish. */
            if (DEBUG) Log.d(TAG, "Info: " + what + "," + extra);
            if (null != mInnerAPIOnInfoListener) {
                mInnerAPIOnInfoListener.onInfo(VideoView.this, mp, what, extra);
            }
            if (null != mOnInfoListener) {
                mOnInfoListener.onInfo(mp, what, extra);
            }
            return true;
        }
    };

    private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if (DEBUG) Log.d(TAG, "SeekComplete");
        }
    };


    // internal
    /**
     * Interface definition for a callback to be invoked when the media
     * source is ready for playback.
     */
    public interface OnPreparedListener {
        /**
         * Called when the media file is ready for playback.
         *
         * @param video the VIPVideo that is ready for playback
         * @param mp    the MediaPlayer that is ready for playback
         */
        void onPrepared(VideoView video, MediaPlayer mp);
    }

    /**
     * Interface definition for a callback to be invoked when playback of
     * a media source has completed.
     */
    public interface OnCompletionListener {
        /**
         * Called when the end of a media source is reached during playback.
         *
         * @param video the VIPVideo that is ready for playback
         * @param mp    the MediaPlayer that reached the end of the file
         */
        void onCompletion(VideoView video, MediaPlayer mp);
    }

    /**
     * Interface definition of a callback to be invoked when there
     * has been an error during an asynchronous operation (other errors
     * will throw exceptions at method call time).
     */
    public interface OnErrorListener {
        /**
         * Called to indicate an error.
         *
         * @param video   the VIPVideo that is ready for playback
         * @param mp      the MediaPlayer the error pertains to
         * @param what    the type of error that has occurred:
         * <ul>
         * <li>{@link MediaPlayer#MEDIA_ERROR_UNKNOWN}
         * <li>{@link MediaPlayer#MEDIA_ERROR_SERVER_DIED}
         * </ul>
         * @param extra an extra code, specific to the error. Typically
         * implementation dependent.
         * <ul>
         * <li>{@link MediaPlayer#MEDIA_ERROR_IO}
         * <li>{@link MediaPlayer#MEDIA_ERROR_MALFORMED}
         * <li>{@link MediaPlayer#MEDIA_ERROR_UNSUPPORTED}
         * <li>{@link MediaPlayer#MEDIA_ERROR_TIMED_OUT}
         * </ul>
         * @return True if the method handled the error, false if it didn't.
         * Returning false, or not having an OnErrorListener at all, will
         * cause the OnCompletionListener to be called.
         */
        boolean onError(VideoView video, MediaPlayer mp, int what, int extra);
    }

    /**
     * Interface definition of a callback to be invoked to communicate some
     * info and/or warning about the media or its playback.
     */
    public interface OnInfoListener
    {
        /**
         * Called to indicate an info or a warning.
         *
         * @param mp      the MediaPlayer the info pertains to.
         * @param what    the type of info or warning.
         * <ul>
         * <li>{@link MediaPlayer#MEDIA_INFO_UNKNOWN}
         * <li>{@link MediaPlayer#MEDIA_INFO_VIDEO_TRACK_LAGGING}
         * <li>{@link MediaPlayer#MEDIA_INFO_VIDEO_RENDERING_START}
         * <li>{@link MediaPlayer#MEDIA_INFO_BUFFERING_START}
         * <li>{@link MediaPlayer#MEDIA_INFO_BUFFERING_END}
         * <li><code>MEDIA_INFO_NETWORK_BANDWIDTH (703)</code> -
         *     bandwidth information is available (as <code>extra</code> kbps)
         * <li>{@link MediaPlayer#MEDIA_INFO_BAD_INTERLEAVING}
         * <li>{@link MediaPlayer#MEDIA_INFO_NOT_SEEKABLE}
         * <li>{@link MediaPlayer#MEDIA_INFO_METADATA_UPDATE}
         * <li>{@link MediaPlayer#MEDIA_INFO_UNSUPPORTED_SUBTITLE}
         * <li>{@link MediaPlayer#MEDIA_INFO_SUBTITLE_TIMED_OUT}
         * </ul>
         * @param extra an extra code, specific to the info. Typically
         * implementation dependent.
         * @return True if the method handled the info, false if it didn't.
         * Returning false, or not having an OnInfoListener at all, will
         * cause the info to be discarded.
         */
        boolean onInfo(VideoView video, MediaPlayer mp, int what, int extra);
    }
}
