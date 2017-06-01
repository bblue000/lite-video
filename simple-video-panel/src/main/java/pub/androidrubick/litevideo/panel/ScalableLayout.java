package pub.androidrubick.litevideo.panel;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * {@doc}
 * <p>
 * Created by Yin Yong on 2017/5/4.
 */
public class ScalableLayout extends FrameLayout {

    private ScaleType mScaleType = ScaleType.FIT_CENTER;
    private Rect mChildRect = new Rect();

    public ScalableLayout(Context context) {
        this(context, null);
    }

    public ScalableLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScalableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

    }

    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            throw new NullPointerException();
        }
        //FIXME: 暂时不支持，因为如果大小缩放的不好，会出现崩溃（JNI）
        if (scaleType == ScaleType.CENTER_CROP) {
            scaleType = ScaleType.FIT_CENTER;
        }
        if (mScaleType != scaleType) {
            mScaleType = scaleType;
            requestLayout();
            invalidate();
        }
    }

    @Override
    public void addView(View child) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScalableLayout can host only one direct child");
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScalableLayout can host only one direct child");
        }
        super.addView(child, index);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScalableLayout can host only one direct child");
        }
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScalableLayout can host only one direct child");
        }
        super.addView(child, index, params);
    }

//    @Override
//    protected LayoutParams generateDefaultLayoutParams() {
//        return wrapLayoutParams(super.generateDefaultLayoutParams());
//    }
//
//    @Override
//    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
//        return wrapLayoutParams(super.generateLayoutParams(lp));
//    }
//
//    @Override
//    public LayoutParams generateLayoutParams(AttributeSet attrs) {
//        return wrapLayoutParams(super.generateLayoutParams(attrs));
//    }
//
//    private LayoutParams wrapLayoutParams(LayoutParams layoutParams) {
//        layoutParams.width = layoutParams.WRAP_CONTENT;
//        layoutParams.height = layoutParams.WRAP_CONTENT;
//        return layoutParams;
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mChildRect.set(0, 0, 0, 0);
        if (getChildCount() == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthSize, heightSize;
        int w, h, cw, ch;

        final int pleft = getPaddingLeft();
        final int pright = getPaddingRight();
        final int ptop = getPaddingTop();
        final int pbottom = getPaddingBottom();

        final View child = getChildAt(0);

        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        cw = child.getMeasuredWidth();
        ch = child.getMeasuredHeight();

        if (cw <= 0) cw = 1;
        if (ch <= 0) ch = 1;

        widthSize = resolveSizeAndState0(cw, widthMeasureSpec, 0);
        heightSize = resolveSizeAndState0(ch, heightMeasureSpec, 0);

        w = MEASURED_SIZE_MASK & widthSize;
        h = MEASURED_SIZE_MASK & heightSize;

        if (w == 0 || h == 0) {
            setMeasuredDimension(widthSize, heightSize);
            return;
        }

        // 在此进行比较
        boolean changed = false;
        boolean updateBounds = false;
        switch (mScaleType) {
            case FIT_XY:
                mChildRect.set(0, 0, w, h);
                break;
            case FIT_CENTER:
                float rw = (float) w / (float) cw;
                float rh = (float) h / (float) ch;
                if (rw < rh) {
                    cw = w;
                    ch = (int) (ch * rw);
                } else {
                    ch = h;
                    cw = (int) (cw * rh);
                }
                changed = true;
                updateBounds = true;
                break;
            case CENTER_INSIDE:
                if (w < cw) {
                    // ch/cw = x/w
                    ch = (int) ((float) w * (float) ch / (float) cw);
                    cw = w;
                    changed = true;
                }
                if (h < ch) {
                    // cw/ch = x/h
                    cw = (int) ((float) h * (float) cw / (float) ch);
                    ch = h;
                    changed = true;
                }
                updateBounds = true;
                break;
        }

        if (changed) {
            widthSize = resolveSizeAndState0(cw, widthMeasureSpec, 0);
            heightSize = resolveSizeAndState0(ch, heightMeasureSpec, 0);

            w = MEASURED_SIZE_MASK & widthSize;
            h = MEASURED_SIZE_MASK & heightSize;
        }

        if (updateBounds) {
            configureBounds(w, h, cw, ch);
            measureChild(child, MeasureSpec.makeMeasureSpec(cw, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(ch, MeasureSpec.EXACTLY));
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    private void configureBounds(int w, int h, int cw, int ch) {
        mChildRect.set(0, 0, cw, ch);
        mChildRect.offset((w - cw) / 2, (h - ch) / 2);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() == 0) {
            return;
        }
        final View child = getChildAt(0);
        child.layout(mChildRect.left, mChildRect.top, mChildRect.right, mChildRect.bottom);
    }

    private int resolveAdjustedSize(int desiredSize, int maxSize,
                                    int measureSpec) {
        int result = desiredSize;
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize =  MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                /* Parent says we can be as big as we want. Just don't be larger
                   than max size imposed on ourselves.
                */
                result = Math.min(desiredSize, maxSize);
                break;
            case MeasureSpec.AT_MOST:
                // Parent says we can be as big as we want, up to specSize.
                // Don't be larger than specSize, and don't be larger than
                // the max size imposed on ourselves.
                result = Math.min(Math.min(desiredSize, specSize), maxSize);
                break;
            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }

    private int resolveSizeAndState0(int size, int measureSpec, int childMeasuredState) {
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);
        final int result;
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                } else {
                    result = size;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                result = size;
        }
        return result | (childMeasuredState & MEASURED_STATE_MASK);
    }

}
