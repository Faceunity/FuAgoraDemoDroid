package io.agora.tutorials;

import android.app.Application;

import com.faceunity.FURenderer;

/**
 * @author lq on 2018.09.30
 */
public class AlgoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FURenderer.initFURenderer(this);
    }
}
