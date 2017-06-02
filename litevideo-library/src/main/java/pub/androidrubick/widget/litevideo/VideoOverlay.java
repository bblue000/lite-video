package pub.androidrubick.widget.litevideo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

/**
 * This View Container should be add top of custom view tree
 *
 * <p/>
 *
 * It supports animation between landscape and portrait.
 *
 * <p>
 * Created by Yin Yong on 2017/6/1.
 */
public class VideoOverlay extends FrameLayout {

    public static final int O_PORTRAIT = 0;
    public static final int O_LANDSCAPE = 1;
    public interface OrientationChangeListener {
        /**
         * @see #O_PORTRAIT
         * @see #O_LANDSCAPE
         */
        void onOrientationChanged(int orientation);
    }

    private final boolean NEED_FULLSCREEN = true;
    private int mOrientation = O_PORTRAIT;
    private int mTargetOrientation = O_PORTRAIT;
    private OrientationChangeListener mOrientationChangeListener;

    private Activity mActivity;
    private boolean mDirty = true;
    private boolean mAnimating;
    private final Rect mOverlayNormalRect = new Rect();
    private final Rect mFullscreenRect = new Rect();

    private final Rect mTmpFullRect = new Rect();
    private final Rect mTmpSidesAlignRect = new Rect();

    private CloneableVideoContainer mVideoContainer;
    private CloneableViewDispatcher.CloneStateProvider mCloneStateProvider = new CloneableViewDispatcher.CloneStateProvider() {
        @Override
        public boolean isCloneState() {
            return landscapeOrTransformState();
        }
    };
    /*package*/ VideoOverlay(Context context) {
        this(context, null);
    }

    public VideoOverlay(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        setClipToPadding(false);

        // get full screen rect
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mFullscreenRect.set(0, 0, metrics.widthPixels, metrics.heightPixels);

        // 这边制造一个伪装者
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount != 1) {
            throw new IllegalStateException("`Only` one VideoViewContainer child should be inflated from layout");
        }
        View view = getChildAt(0);
        if (view instanceof BaseCloneableVideoContainer) {
            BaseCloneableVideoContainer container = (BaseCloneableVideoContainer) view;
            CloneableViewDispatcher dispatcher = container.getCloneableViewDispatcher();
            dispatcher.setCloneStateProvider(mCloneStateProvider);
            mVideoContainer = container;
        }
        if (null != mVideoContainer) {
            throw new IllegalStateException("no VideoViewContainer child inflated from layout");
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDirty = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDirty = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (landscapeOrTransformState()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            // clear
            View view = mVideoContainer.asViewGroup();
            View alignView = mVideoContainer.getSidesAlignView();
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = LayoutParams.MATCH_PARENT;
            lp.leftMargin = 0;
            lp.topMargin = 0;

            view.measure(getChildMeasureSpec(widthMeasureSpec, 0, alignView.getMeasuredWidth()),
                    getChildMeasureSpec(heightMeasureSpec, 0, alignView.getMeasuredHeight()));
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (landscapeOrTransformState()) {
            super.onLayout(changed, left, top, right, bottom);
        } else {
            copyLocationWhenNormalScreen();
        }
        if (mDirty) {
            mDirty = false;
            int mw = getWidth();
            int mh = getHeight();

            mOverlayNormalRect.set(0, mFullscreenRect.height() - mh, mw, mFullscreenRect.bottom);
            performTargetOrientation();
        }
    }

    private void copyLocationWhenNormalScreen() {
        getRectOfView(this, mTmpFullRect);
        getNormalScreenRect(mTmpSidesAlignRect);

        int offsetX = mTmpSidesAlignRect.left - mTmpFullRect.left;
        int offsetY = mTmpSidesAlignRect.top - mTmpFullRect.top;
        View view = mVideoContainer.asViewGroup();
        view.layout(offsetX, offsetY, offsetX + mTmpSidesAlignRect.width(), offsetY + mTmpSidesAlignRect.height());

        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        lp.width = mTmpSidesAlignRect.width();
        lp.height = mTmpSidesAlignRect.height();
        lp.leftMargin = offsetX;
        lp.topMargin = offsetY;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return dispatchBackEvent(event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return dispatchBackEvent(event) || super.dispatchKeyEvent(event);
    }

    private boolean dispatchBackEvent(KeyEvent event) {
        if (mAnimating || event.getKeyCode() != KeyEvent.KEYCODE_BACK) {
            return false;
        }
        if (mOrientation == O_LANDSCAPE) {
            toggleOrientation();
            return true;
        }
        return false;
    }

    public final void setOrientationChangeListener(OrientationChangeListener listener) {
        mOrientationChangeListener = listener;
        performOrientationChanged();
    }

    private void performOrientationChanged() {
        if (null != mOrientationChangeListener) {
            mOrientationChangeListener.onOrientationChanged(mOrientation);
        }
    }

    public final void toggleOrientation() {
        switch (mOrientation) {
            case O_LANDSCAPE:
                setTarget(O_PORTRAIT);
                break;
            default:
                setTarget(O_LANDSCAPE);
                break;
        }
    }

    public final boolean portrait() {
        return setTarget(O_PORTRAIT);
    }

    public final boolean landscape() {
        return setTarget(O_LANDSCAPE);
    }

    protected boolean setTarget(int target) {
        if (mAnimating) {
            return false;
        }
        if (mTargetOrientation == target) {
            return false;
        }
        mTargetOrientation = target;
        performTargetOrientation();
        return true;
    }

    private void performTargetOrientation() {
        if (mAnimating || mDirty || mTargetOrientation == mOrientation) {
            return;
        }
        switch (mTargetOrientation) {
            case O_PORTRAIT:
                mAnimating = true;
                startAnimation(new AnimImpl(O_PORTRAIT));
                showSystemUI();
                break;
            case O_LANDSCAPE:
                mAnimating = true;
                copyLocationWhenNormalScreen();
                startAnimation(new AnimImpl(O_LANDSCAPE));
                hideSystemUI();
                break;
        }
    }

    private void hideSystemUI() {
        if (NEED_FULLSCREEN) {
            int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            mActivity.getWindow().setFlags(flags, flags);
        }
    }

    private void showSystemUI() {
        if (NEED_FULLSCREEN) {
            int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            mActivity.getWindow().setFlags(0, flags);
        }
    }

    private boolean landscapeOrTransformState() {
        return mTargetOrientation != mOrientation || mOrientation == O_LANDSCAPE;
    }

    private int getStatusBarHeightIfExist() {
        return mOverlayNormalRect.isEmpty() ? 0 : mOverlayNormalRect.top;
    }

    // 还是正常竖屏的宽高大小
    private Rect getLandscapeTargetRect(Rect rect) {
        if (null == rect) rect = new Rect();
        rect.set(NEED_FULLSCREEN ? mFullscreenRect : mOverlayNormalRect);
        return rect;
    }

    // 还是正常情况下的对外公开的View的大小
    private Rect getNormalScreenRect(Rect rect) {
        if (null == rect) rect = new Rect();
        getRectOfView(mVideoContainer.getSidesAlignView(), rect);
        return rect;
    }

    private static Rect getRectOfView(View view, Rect rect) {
        if (null == rect) rect = new Rect();
        if (null != view) {
            int[] arr = new int[2];
            view.getLocationOnScreen(arr);
            int left = arr[0], top = arr[1];
            rect.set(left, top, left + view.getWidth(), top + view.getHeight());
        }
        return rect;
    }

    private class AnimImpl extends Animation implements Animation.AnimationListener {
        private final float ALPHA_FROM = 0.0f;
        private final int DURATION = 300;
        private int fromW, toW, fromH, toH, fromR, toR;
        private int fromTX, fromTY, toTX, toTY;
        private float fromAlpha, toAlpha;

        private int mTargetOrientation;
        private boolean mInitRectInfo;

        AnimImpl(int target) {
            mTargetOrientation = target;

            AnimImpl.this.setAnimationListener(this);
            AnimImpl.this.setDuration(DURATION);
            setInterpolator(new DecelerateInterpolator());
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            initIfNeeded();

            // 以FakeDecorContentLayout顶点作为坐标
            View child = mVideoContainer.asViewGroup();
            float width = (fromW + (toW - fromW) * interpolatedTime);
            float height = (fromH + (toH - fromH) * interpolatedTime);
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            lp.width = (int) width;
            lp.height = (int) height;
            child.requestLayout();

            child.setRotation(fromR + (toR - fromR) * interpolatedTime);
            child.setTranslationX(fromTX + (toTX - fromTX) * interpolatedTime);
            child.setTranslationY(fromTY + (toTY - fromTY) * interpolatedTime);
//            mAnimateAlpha = fromAlpha + (toAlpha - fromAlpha) * interpolatedTime;
            invalidate();

        }

        private void initIfNeeded() {
            if (!mInitRectInfo) {
                prepareForAnimation();

                getLandscapeTargetRect(mTmpFullRect);
                getNormalScreenRect(mTmpSidesAlignRect);

                switch (mTargetOrientation) {
                    case O_LANDSCAPE:
                        fromW = mTmpSidesAlignRect.width();
                        fromH = mTmpSidesAlignRect.height();
                        toW = mTmpFullRect.height();
                        toH = mTmpFullRect.width();

                        fromTX = fromTY = 0;
                        toTX = (mTmpFullRect.centerX() - (mTmpSidesAlignRect.left + mTmpFullRect.height() / 2));
                        toTY = (mTmpFullRect.centerY() - (mTmpSidesAlignRect.top + mTmpFullRect.width() / 2));
                        fromR = 0; toR = 90;
                        fromAlpha = ALPHA_FROM; toAlpha = 1f;
                        break;
                    default:
                        fromW = mTmpFullRect.height();
                        fromH = mTmpFullRect.width();
                        toW = mTmpSidesAlignRect.width();
                        toH = mTmpSidesAlignRect.height();

                        fromTX = (mTmpFullRect.centerX() - (mTmpSidesAlignRect.left + mTmpFullRect.height() / 2));
                        fromTY = (mTmpFullRect.centerY() - (mTmpSidesAlignRect.top + mTmpFullRect.width() / 2));
                        toTX = toTY = 0;

                        fromR = 90; toR = 0;
                        fromAlpha = 1f; toAlpha = ALPHA_FROM;
                        break;
                }
                mInitRectInfo = true;
            }
        }

        @Override
        public void onAnimationStart(Animation animation) {
            initIfNeeded();
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            endForAnimation();
            mOrientation = mTargetOrientation;
            mAnimating = false;
            performOrientationChanged();
        }

        // use hardware layers to improve animation performance
        private int mRawLayerType;
        private void prepareForAnimation() {
            View view = mVideoContainer.asViewGroup();
            mRawLayerType = view.getLayerType();
            view.setLayerType(LAYER_TYPE_HARDWARE, null);
        }

        private void endForAnimation() {
            View view = mVideoContainer.asViewGroup();
            view.setLayerType(mRawLayerType, null);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
