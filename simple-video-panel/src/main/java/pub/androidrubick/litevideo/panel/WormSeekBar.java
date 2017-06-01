package pub.androidrubick.litevideo.panel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.SeekBar;

/**
 * {@doc}
 * <p>
 * Created by Yin Yong on 2017/5/25.
 */
public class WormSeekBar extends SeekBar {
    public WormSeekBar(Context context) {
        super(context);
    }

    public WormSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WormSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                ViewParent parent = getParent();
                if (null != parent) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                ViewParent parent = getParent();
                if (null != parent) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        return true;
    }
}
