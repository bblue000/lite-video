package pub.androidrubick.widget.litevideo;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

/**
 * A {@link CloneableView} as below:
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
/*package*/ interface CloneableView {

    /**
     * a cloneable view should align a clone view's sides in window
     * or on screen.
     *
     * <p/>
     *
     * may be null, if none clone view is attached
     */
    View getSidesAlignView() ;

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
