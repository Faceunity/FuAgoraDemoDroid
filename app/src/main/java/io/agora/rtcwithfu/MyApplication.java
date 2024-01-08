package io.agora.rtcwithfu;

import android.app.Application;
import android.content.Context;

import com.faceunity.nama.FUConfig;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.utils.FuDeviceUtils;

public class MyApplication extends Application {
    private RtcEngineEventHandlerProxy mRtcEventHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        FUConfig.DEVICE_LEVEL = FuDeviceUtils.judgeDeviceLevel(true);
        mRtcEventHandler = new RtcEngineEventHandlerProxy();
        initVideoCaptureAsync();
    }


    private void initVideoCaptureAsync() {
        new Thread(() -> {
            Context application = getApplicationContext();
            FURenderer.getInstance().setup(application);
        }).start();
    }


    public void addRtcHandler(RtcEngineEventHandler handler) {
        mRtcEventHandler.addEventHandler(handler);
    }

    public void removeRtcHandler(RtcEngineEventHandler handler) {
        mRtcEventHandler.removeEventHandler(handler);
    }

    public RtcEngineEventHandlerProxy getRtcEventHandler() {
        return mRtcEventHandler;
    }
}
