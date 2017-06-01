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
    public interface CloneableView {
        /**
         * @return whether this {@link CloneableView} is in clone state (some methods should be dispatched to {@link CloneableViewDispatcher}) currently
         */
        boolean isCloneState() ;

        /**
         * @return a {@link CloneableViewDispatcher} of this  {@link CloneableView}
         */
        CloneableViewDispatcher getCloneableViewDispatcher();

        /**
         * just invoke {@link View#draw(Canvas) super.draw(Canvas)}
         */
        void superDraw(Canvas canvas) ;

        /**
         * just invoke {@link View#dispatchDraw(Canvas) super.dispatchDraw(Canvas)}
         */
        void superDispatchDraw(Canvas canvas) ;

        /**
         * just invoke {@link android.view.ViewGroup#onInterceptTouchEvent(MotionEvent) super.onInterceptTouchEvent(MotionEvent)}
         */
        boolean superOnInterceptTouchEvent(MotionEvent ev);

        /**
         * just invoke {@link View#dispatchTouchEvent(MotionEvent) super.dispatchTouchEvent(MotionEvent)}
         */
        boolean superDispatchTouchEvent(MotionEvent ev) ;

        /**
         * just invoke {@link android.view.ViewGroup#requestDisallowInterceptTouchEvent(boolean) super.requestDisallowInterceptTouchEvent(boolean)}
         */
        void superRequestDisallowInterceptTouchEvent(boolean disallowIntercept) ;
    }

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

    public void attachCloneableView(CloneableView cloneableView) {
        if (null != mCloneableViewDispatcher) {
            mCloneableViewDispatcher.setCloneView(null);
        }
        mCloneableViewDispatcher = null;

        CloneableViewDispatcher cloneableViewDispatcher = null;
        if (null != cloneableView) {
            cloneableViewDispatcher = cloneableView.getCloneableViewDispatcher();
        }
        if (null != cloneableViewDispatcher) {
            cloneableViewDispatcher.setCloneView(this);
            mCloneableViewDispatcher = cloneableViewDispatcher;
        }
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
