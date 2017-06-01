package pub.androidrubick.litevideo;

import android.view.View;
import android.view.ViewGroup;

/**
 * a interface indicates a view group as a video view container.
 *
 * <p>
 * Created by Yin Yong on 2017/6/1.
 */
public interface VideoViewContainer {

    ViewGroup asViewGroup() ;

    View getSidesAlignView() ;

}
