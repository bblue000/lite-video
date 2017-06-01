package pub.androidrubick.litevideo.panel;

import android.content.Context;
import android.util.AttributeSet;

import pub.androidrubick.widget.litevideo.BaseCloneableVideoContainer;

/**
 * {@doc}
 * <p>
 * Created by Yin Yong on 2017/6/1.
 */
public class VideoPanel extends BaseCloneableVideoContainer {
    public VideoPanel(Context context) {
        super(context);
    }

    public VideoPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isCloneState() {
        return false;
    }

}
