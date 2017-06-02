package pub.androidrubick.litevideo.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pub.androidrubick.widget.litevideo.BaseCloneableVideoContainer;
import pub.androidrubick.widget.litevideo.CloneView;
import pub.androidrubick.widget.litevideo.VideoOverlay;

/**
 * {@doc}
 * <p>
 * Created by Yin Yong on 2017/5/27.
 */
public class MainActivity extends FragmentActivity {

    private TextView tv;
    private Button btn;
    private BaseCloneableVideoContainer cloneableVideoContainer;
    private CloneView cloneView;
    private VideoOverlay videoOverlay;
    private BaseCloneableVideoContainer videoPanel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        tv = (TextView) findViewById(R.id.tv);
        btn = (Button) findViewById(R.id.btn);
        videoOverlay = (VideoOverlay) findViewById(R.id.video_overlay);
        videoPanel = (BaseCloneableVideoContainer) findViewById(R.id.video_panel);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText(String.valueOf(Math.random() * 100));
                videoOverlay.toggleOrientation();
            }
        });
        cloneableVideoContainer = (BaseCloneableVideoContainer) findViewById(R.id.video_panel);
        cloneView = (CloneView) findViewById(R.id.clone_view);

        cloneableVideoContainer.getCloneableViewDispatcher()
                .attachToCloneView(cloneView);
    }
}
