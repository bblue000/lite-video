package pub.androidrubick.litevideo.panel;

/**
 * 缩放类型
 *
 *
 * <p/>
 * Created by Yin Yong on 2017/5/3.
 */
public enum ScaleType {

    /**
     * 宽高缩放到所属视图的实际宽高
     */
    FIT_XY,

    /**
     * 缩放, 保持原来的比例, 使得宽度或者高度中某一项填充满视图的宽/高
     */
    FIT_CENTER,

    /**
     * Scale the image/subview uniformly (maintain the image/subview's aspect ratio) so
     * that both dimensions (width and height) of the image/subview will be equal
     * to or less than the corresponding dimension of the view
     * (minus padding). The image/subview is then centered in the view.
     */
    CENTER_INSIDE,

    /**
     * Scale the image/subview uniformly (maintain the image's aspect ratio) so
     * that both dimensions (width and height) of the image/subview will be equal
     * to or larger than the corresponding dimension of the view
     * (minus padding). The image/subview is then centered in the view.
     */
    CENTER_CROP;

}
