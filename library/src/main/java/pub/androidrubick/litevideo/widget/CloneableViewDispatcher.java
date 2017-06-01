package pub.androidrubick.litevideo.widget;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * A {@link CloneView.CloneableView} as below:
 *
 * <pre>
 *  public class CustomView extends FrameLayout implements CloneView.CloneableView {
 *
 *      private CloneableViewDispatcher mCloneableViewDispatcher;
 *      public CustomView(Context context) {
 *          this(context, null);
 *      }
 *
 *      public CustomView(Context context, AttributeSet attrs) {
 *          this(context, attrs, 0);
 *      }
 *
 *      public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
 *          super(context, attrs, defStyleAttr);
 *
 *          mCloneableViewDispatcher = new CloneableViewDispatcher(this);
 *      }
 *
 *      @Override
 *      public boolean isCloneState() {
 *          return false;
 *      }
 *
 *      @Override
 *      public CloneableViewDispatcher getCloneableViewDispatcher() {
 *          return mCloneableViewDispatcher;
 *      }
 *
 *      @SuppressLint("MissingSuperCall")
 *      @Override
 *      public void draw(Canvas canvas) {
 *          mCloneableViewDispatcher.draw(canvas);
 *      }
 *
 *      @Override
 *      protected void dispatchDraw(Canvas canvas) {
 *          mCloneableViewDispatcher.dispatchDraw(canvas);
 *      }
 *
 *      @Override
 *      public boolean onInterceptTouchEvent(MotionEvent ev) {
 *          return mCloneableViewDispatcher.onInterceptTouchEvent(ev);
 *      }
 *
 *      @Override
 *      public boolean dispatchTouchEvent(MotionEvent ev) {
 *          return mCloneableViewDispatcher.dispatchTouchEvent(ev);
 *      }
 *
 *      @Override
 *      public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
 *          mCloneableViewDispatcher.requestDisallowInterceptTouchEvent(disallowIntercept);
 *      }
 *
 *      @Override
 *      public void superDraw(Canvas canvas) {
 *          super.draw(canvas);
 *      }
 *
 *      @Override
 *      public void superDispatchDraw(Canvas canvas) {
 *          super.dispatchDraw(canvas);
 *      }
 *
 *      @Override
 *      public boolean superOnInterceptTouchEvent(MotionEvent ev) {
 *          return super.onInterceptTouchEvent(ev);
 *      }
 *
 *      @Override
 *      public boolean superDispatchTouchEvent(MotionEvent ev) {
 *          return super.dispatchTouchEvent(ev);
 *      }
 *
 *      @Override
 *      public void superRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
 *          super.requestDisallowInterceptTouchEvent(disallowIntercept);
 *      }
 *  }
 * </pre>
 *
 *
 * <p>
 * Created by Yin Yong on 2017/5/27.
 */
public class CloneableViewDispatcher {

    private final CloneView.CloneableView mSrcView;
    private View mCloneView;
    private boolean mForceClone;

    public CloneableViewDispatcher(CloneView.CloneableView srcView) {
        if (null == srcView) {
            throw new NullPointerException();
        }
        mSrcView = srcView;
    }

    // 如果本身绘制，会调用该方法，内部会调用dispatchDraw
    /**
     * A {@link CloneView.CloneableView} should dispatch
     * {@link View#draw(Canvas) view's draw(Canvas)} method to this;
     */
    public void draw(Canvas canvas) {
        if (isCloneState() || mForceClone) {
            mSrcView.superDraw(canvas);
        }
    }

    // 如果本身不绘制，会直接调用该方法
    /**
     * A {@link CloneView.CloneableView} should dispatch
     * {@link View#dispatchDraw(Canvas) view's dispatchDraw(Canvas)} method to this;
     */
    public void dispatchDraw(Canvas canvas) {
        if (isCloneState() || mForceClone) {
            mSrcView.superDispatchDraw(canvas);
        }
    }

    /**
     * if {@link CloneView.CloneableView target} is a view, ignore this;
     *
     * if it is a view group, dispatch {@link android.view.ViewGroup#onInterceptTouchEvent(MotionEvent)}
     * to this method.
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isCloneState() || mForceClone) {
            return mSrcView.superOnInterceptTouchEvent(ev);
        }
        return false;
    }

    /**
     * A {@link CloneView.CloneableView} should dispatch
     * {@link View#dispatchTouchEvent(MotionEvent) view's dispatchTouchEvent(MotionEvent)} method to this;
     */
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isCloneState() || mForceClone) {
            return mSrcView.superDispatchTouchEvent(ev);
        }
        return false;
    }

    // 对于外部一些容器，如ScrollView，ViewPager等，需要在一定的条件下，阻止其捕获事件。
    // 如果是竖屏状态，将相应的request传送给mCopyDrawingView
    /**
     * if {@link CloneView.CloneableView} is a view, ignore this;
     *
     * if it is a view group, dispatch {@link ViewGroup#requestDisallowInterceptTouchEvent(boolean)}
     * to this method.
     */
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (isCloneState()) {
            mSrcView.superRequestDisallowInterceptTouchEvent(disallowIntercept);
        } else {
            if (null != mCloneView) {
                ViewParent parent = mCloneView.getParent();
                if (null != parent) {
                    parent.requestDisallowInterceptTouchEvent(disallowIntercept);
                }
            }
        }
    }

    public View getCurrentCloneView() {
        return mCloneView;
    }

    /*package*/ void setCloneView(CloneView cloneView) {
        mCloneView = cloneView;
    }

    /*package*/ boolean isCloneState() {
        return mSrcView.isCloneState();
    }

    /*package*/ void setForceClone(boolean cloneable) {
        mForceClone = cloneable;
    }

}
