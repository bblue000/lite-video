package pub.androidrubick.litevideo.panel.util;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;

/**
 * <p>
 * Created by Yin Yong on 2017/5/4.
 */
public class LiteUtil {

    public static boolean equals(Object obj1, Object obj2) {
        if (null == obj1) {
            return null == obj2;
        } else {
            return obj1.equals(obj2);
        }
    }

    /**
     * 判断当前线程是否是主线程
     */
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * 给出的context是否是Activity实例
     */
    public static boolean isActivityContext(Context context) {
        return context instanceof Activity;
    }
}
