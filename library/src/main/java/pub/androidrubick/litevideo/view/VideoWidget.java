package pub.androidrubick.litevideo.view;

import android.media.MediaPlayer;
import android.view.View;

/**
 * {@doc}
 * <p>
 * Created by Yin Yong on 2017/5/19.
 */
/*package*/ interface VideoWidget extends VideoWidgetBase {

    View asView();

    void setSurfaceCallback(VideoSurfaceCallback callback) ;

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    void setOnPreparedListener(MediaPlayer.OnPreparedListener l) ;

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    void setOnCompletionListener(MediaPlayer.OnCompletionListener l) ;

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    void setOnErrorListener(MediaPlayer.OnErrorListener l) ;

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    void setOnInfoListener(MediaPlayer.OnInfoListener l) ;

    void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener l) ;
}
