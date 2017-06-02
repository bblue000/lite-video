package pub.androidrubick.widget.litevideo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * {@doc}
 * <p>
 * Created by Yin Yong on 2017/5/27.
 */
public class CloneView extends View {

    private CloneableViewDispatcher mCloneableViewDispatcher;
    public CloneView(Context context) {
        super(context);
    }

    public CloneView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CloneView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*package*/ void attachDispatcher(CloneableViewDispatcher dispatcher) {
        mCloneableViewDispatcher = dispatcher;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null != mCloneableViewDispatcher) {
            if (mCloneableViewDispatcher.isCloneState()) {
                mCloneableViewDispatcher.setForceClone(true);
                try {
                    mCloneableViewDispatcher.draw(canvas);
                } finally {
                    mCloneableViewDispatcher.setForceClone(false);
                }
            } else {
                super.onDraw(canvas);
            }
            invalidate();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (null != mCloneableViewDispatcher && mCloneableViewDispatcher.isCloneState()) {
            mCloneableViewDispatcher.setForceClone(true);
            try {
                return mCloneableViewDispatcher.dispatchTouchEvent(event);
            } finally {
                mCloneableViewDispatcher.setForceClone(false);
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
