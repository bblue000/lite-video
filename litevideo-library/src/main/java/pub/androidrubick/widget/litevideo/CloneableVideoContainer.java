package pub.androidrubick.widget.litevideo;

import android.view.View;
import android.view.ViewGroup;

/**
 * A interface indicates a view group as a video view container.
 *
 * <p>
 * Created by Yin Yong on 2017/6/1.
 */
/*package*/ interface CloneableVideoContainer extends CloneView.CloneableView {

    ViewGroup asViewGroup() ;

    View getSidesAlignView() ;

}
