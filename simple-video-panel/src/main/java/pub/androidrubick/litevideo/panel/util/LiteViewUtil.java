package pub.androidrubick.litevideo.panel.util;

import android.view.View;

/**
 * <p>
 * Created by Yin Yong on 2017/5/4.
 */
public class LiteViewUtil {

    public static void setViewVisibility(View view, int visibility) {
        if (null == view) {
            return;
        }
        if (view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }

    public static void setViewVisibility(View view, boolean show) {
        if (null == view) {
            return;
        }
        if (show) {
            setViewVisible(view);
        } else {
            setViewGone(view);
        }
    }

    public static boolean isVisibilityEqual(View view, int visibility) {
        if (null == view) {
            return false;
        }
        return view.getVisibility() == visibility;
    }

    public static boolean isVisible(View view) {
        return isVisibilityEqual(view, View.VISIBLE);
    }

    public static boolean isInvisible(View view) {
        return isVisibilityEqual(view, View.INVISIBLE);
    }

    public static boolean isGone(View view) {
        return isVisibilityEqual(view, View.GONE);
    }

    public static void setViewVisible(View view) {
        setViewVisibility(view, View.VISIBLE);
    }

    public static void setViewInvisible(View view) {
        setViewVisibility(view, View.INVISIBLE);
    }

    public static void setViewGone(View view) {
        setViewVisibility(view, View.GONE);
    }

    public static void setOnClickListener(View view, View.OnClickListener listener) {
        if (null == view) {
            return ;
        }
        view.setOnClickListener(listener);
    }
}
