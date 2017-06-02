package pub.androidrubick.litevideo.panel.controller;

import pub.androidrubick.litevideo.panel.VideoController;
import pub.androidrubick.litevideo.panel.VideoView;

/**
 * 简单的实现类，可以用来支持缓存功能，没有其他复杂的控制逻辑
 *
 * Created by Yin Yong on 15/12/31.
 */
public class SimpleVideoController extends VideoController {

    /**
     * do nothing
     */
    @Override
    public void determinePlay() {

    }

    /**
     * 直接返回null，因为该类只为了对外提供缓存管理功能
     */
    @Override
    protected VideoView findCurrentPlayVideo() {
        return null;
    }
}
