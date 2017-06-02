package pub.androidrubick.litevideo.panel;

import android.net.Uri;

import java.util.Map;

import pub.androidrubick.widget.litevideo.VideoWidgetBase;

/**
 * 视频控件委派执行需要支持的功能的接口，只在此做统一的声明
 *
 * <p/>
 *
 * 方法对应{@link VideoWidgetBase}中的方法
 *
 * <p>
 * Created by Yin Yong on 2017/5/4.
 */
/*package*/ interface VideoWidgetController {
    /**
     * 为指定Video设置播放源
     *
     * <br/>
     *
     * 支持的url格式包括：<sup>[1]</sup>
     * <ul>
     *     <li>http|https|rtsp://{host}/{path}</li>
     *     <li>file://{path}</li>
     *     <li>android.resource://{package}/{res id}</li>
     *     <li>android.resource://{package}/{res dir}/{res file name}<sup>[2]</sup></li>
     * </ul>
     *
     * <p>
     *     <b>[1]</b> 暂不支持file://android_asset/{path}，如需访问assets下文件，可以参见
     *     <a href="http://stackoverflow.com/questions/4820816/how-to-get-uri-from-an-asset-file/4820905#4820905">
     *         How to get URI from an asset File?</a>
     * </p>
     *
     * <p>
     *     <b>[2]</b> 暂不支持file://android_asset/{path}，如需访问assets下文件，可以参见
     *     <a href="http://stackoverflow.com/questions/7976141/get-uri-of-mp3-file-stored-in-res-raw-folder-in-android/7979084#7979084">
     *         Get URI stored in res/raw folder in android</a>
     * </p>
     */
    void setVideoPath(VideoView video, String url);

    /**
     * 为指定Video设置播放源
     *
     * @see #setVideoPath(VideoView, String)
     */
    void setVideoURI(VideoView video, Uri uri);

    /**
     * 为指定Video设置播放源
     *
     * @see #setVideoPath(VideoView, String)
     * @see #setVideoURI(VideoView, Uri)
     */
    void setVideoURI(VideoView video, Uri uri, Map<String, String> headers) ;

    /**
     * 视频是否正在播放
     */
    boolean isPlaying(VideoView video);

    /**
     * 让指定的视频控件开始播放（设置URI之后，并不一定会自动播放，调用该方法开始播放视频；
     * 或者暂停时，调用该方法继续播放）
     */
    void start(VideoView video);

    /**
     * 跳到指定时间点
     */
    void seekTo(VideoView video, int msec);

    /**
     * 让指定的视频控件暂停播放
     */
    void pause(VideoView video);

    // 暂时不提供
    // void suspend(PandoraVideoView video);

    /**
     * 让指定的视频控件停止播放
     */
    void stop(VideoView video);

    /**
     * 给指定的视频控件设置状态回调接口
     */
    void setControlCallback(VideoView video, VideoControlCallback callback);

}
