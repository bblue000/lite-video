package pub.androidrubick.litevideo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * {@doc}
 * <p>
 * Created by Yin Yong on 2017/5/31.
 */
public class BaseVideoContainer extends FrameLayout
        implements CloneView.CloneableView, VideoViewContainer {

    private final CloneableViewDispatcher mCloneableViewDispatcher;
    public BaseVideoContainer(Context context) {
        this(context, null);
    }

    public BaseVideoContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseVideoContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCloneableViewDispatcher = new CloneableViewDispatcher(this);
    }

    @Override
    public boolean isCloneState() {
        return true;
    }

    @Override
    public CloneableViewDispatcher getCloneableViewDispatcher() {
        return mCloneableViewDispatcher;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void draw(Canvas canvas) {
        mCloneableViewDispatcher.draw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mCloneableViewDispatcher.dispatchDraw(canvas);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mCloneableViewDispatcher.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mCloneableViewDispatcher.dispatchTouchEvent(ev);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        mCloneableViewDispatcher.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    public void superDraw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    public void superDispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    public boolean superOnInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean superDispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void superRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    public ViewGroup asViewGroup() {
        return this;
    }

    @Override
    public View getSidesAlignView() {
        return mCloneableViewDispatcher.getCurrentCloneView();
    }
}
