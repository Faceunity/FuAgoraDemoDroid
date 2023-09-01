package io.agora.rtcwithfu.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.faceunity.core.enumeration.FUAIProcessorEnum;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.listener.FURendererListener;
import com.faceunity.nama.ui.FaceUnityView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import io.agora.base.VideoFrame;
import io.agora.framework.PreprocessorFaceUnity;
import io.agora.profile.CSVUtils;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.CameraCapturerConfiguration;
import io.agora.rtc2.video.IVideoFrameObserver;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;
import io.agora.rtcwithfu.MyApplication;
import io.agora.rtcwithfu.R;
import io.agora.rtcwithfu.RtcEngineEventHandler;
import io.agora.rtcwithfu.utils.Constants;

/**
 * This activity demonstrates how to make FU and Agora RTC SDK work together
 * <p>
 * The FU activity which possesses remote video chatting ability.
 */
public class FUChatActivity extends RtcBasedActivity implements RtcEngineEventHandler, SensorEventListener {
    private final static String TAG = FUChatActivity.class.getSimpleName();

    private static final int CAPTURE_WIDTH = 1280;
    private static final int CAPTURE_HEIGHT = 720;
    private static final int CAPTURE_FRAME_RATE = 30;

    private FURenderer mFURenderer = FURenderer.getInstance();
    private FaceUnityDataFactory mFaceUnityDataFactory;
    private PreprocessorFaceUnity preprocessor;

    private FrameLayout mRemoteViewContainer;
    private TextView mTrackingText;
    private int mRemoteUid = -1;
    private SensorManager mSensorManager;

    private RtcEngine rtcEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_base);
        initUI();
        initRoom();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        String sdkVersion = RtcEngine.getSdkVersion();
        Log.i(TAG, "onCreate: agora sdk version " + sdkVersion);
        initCsvUtil(this);
        if (preprocessor != null) {
            preprocessor.setCSVUtils(mCSVUtils);
        }
    }

    private void initUI() {
        initRemoteViewLayout();
    }

    private void initRemoteViewLayout() {
        mRemoteViewContainer = findViewById(R.id.remote_video_view);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mRemoteViewContainer.getLayoutParams();
        params.width = displayMetrics.widthPixels / 3;
        params.height = displayMetrics.heightPixels / 3;
        mRemoteViewContainer.setLayoutParams(params);
    }

    private void initRoom() {
        try {
            rtcEngine = RtcEngine.create(getApplicationContext(), getString(R.string.agora_app_id), application().getRtcEventHandler());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (rtcEngine == null) return;
        initFUModule();
        rtcEngine.enableExtension("agora_video_filters_clear_vision", "clear_vision", true);
        rtcEngine.registerVideoFrameObserver(new IVideoFrameObserver() {
            @Override
            public boolean onCaptureVideoFrame(int sourceType, VideoFrame videoFrame) {
                return preprocessor.processBeauty(videoFrame);
            }

            @Override
            public boolean onPreEncodeVideoFrame(int sourceType, VideoFrame videoFrame) {
                return false;
            }

            @Override
            public boolean onMediaPlayerVideoFrame(VideoFrame videoFrame, int mediaPlayerId) {
                return false;
            }

            @Override
            public boolean onRenderVideoFrame(String channelId, int uid, VideoFrame videoFrame) {
                return false;
            }

            @Override
            public int getVideoFrameProcessMode() {
                return IVideoFrameObserver.PROCESS_MODE_READ_WRITE;
            }

            @Override
            public int getVideoFormatPreference() {
                return IVideoFrameObserver.VIDEO_PIXEL_DEFAULT;
            }

            @Override
            public boolean getRotationApplied() {
                return false;
            }

            @Override
            public boolean getMirrorApplied() {
                return false;
            }

            @Override
            public int getObservedFramePosition() {
                return IVideoFrameObserver.POSITION_POST_CAPTURER;
            }
        });
        rtcEngine.setCameraCapturerConfiguration(new CameraCapturerConfiguration(
                new CameraCapturerConfiguration.CaptureFormat(CAPTURE_WIDTH, CAPTURE_HEIGHT, CAPTURE_FRAME_RATE)));

        rtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
        rtcEngine.enableVideo();
        rtcEngine.disableAudio();
        joinChannel();
    }

    private void initFUModule() {
        preprocessor = new PreprocessorFaceUnity();
        mTrackingText = findViewById(R.id.iv_face_detect);
        FaceUnityView faceUnityView = findViewById(R.id.fu_view);
        mFaceUnityDataFactory = new FaceUnityDataFactory(-1);
        faceUnityView.bindDataFactory(mFaceUnityDataFactory);
        preprocessor.setSurfaceListener(new PreprocessorFaceUnity.SurfaceViewListener() {
            @Override
            public void onSurfaceCreated() {
                mFURenderer.bindListener(mFURendererListener);
                mFaceUnityDataFactory.bindCurrentRenderer();
            }

            @Override
            public void onSurfaceDestroyed() {
                mFURenderer.release();
            }
        });
    }

    private void joinChannel() {
        int uid = new Random(System.currentTimeMillis()).nextInt(1000) + 10000;
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.channelProfile = io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        options.clientRoleType = io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
        String roomName = getIntent().getStringExtra(Constants.ACTION_KEY_ROOM_NAME);
        rtcEngine.joinChannel(null, roomName, uid, options);

        TextureView localVideo = findViewById(R.id.local_video_view);
        VideoCanvas local = new VideoCanvas(localVideo, io.agora.rtc2.Constants.RENDER_MODE_HIDDEN, 0);
        local.mirrorMode = io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_AUTO;
        rtcEngine.setupLocalVideo(local);
        rtcEngine.startPreview();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch_camera:
                rtcEngine.switchCamera();
                break;
        }
    }

    /**
     * FURenderer状态回调
     */
    private FURendererListener mFURendererListener = new FURendererListener() {


        @Override
        public void onTrackStatusChanged(FUAIProcessorEnum type, int status) {
            runOnUiThread(() -> {
                mTrackingText.setText(type == FUAIProcessorEnum.FACE_PROCESSOR ? R.string.toast_not_detect_face : R.string.toast_not_detect_body);
                mTrackingText.setVisibility(status > 0 ? View.INVISIBLE : View.VISIBLE);
            });
        }

        @Override
        public void onFpsChanged(double fps, double callTime) {

        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        preprocessor.setRenderEnable(false);
        rtcEngine.leaveChannel();
        rtcEngine.stopPreview();
        preprocessor.releaseFURender();
        mSensorManager.unregisterListener(this);
        RtcEngine.destroy();
        super.onDestroy();
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.i(TAG, "onJoinChannelSuccess " + channel + " " + (uid & 0xFFFFFFFFL));
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        Log.i(TAG, "onUserJoined " + (uid & 0xFFFFFFFFL));
    }

    @Override
    public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
        Log.i(TAG, "onRemoteVideoStateChanged " + (uid & 0xFFFFFFFFL) + " " + state + " " + reason);
        if (mRemoteUid == -1 && state == io.agora.rtc2.Constants.REMOTE_VIDEO_STATE_STARTING) {
            runOnUiThread(() -> {
                mRemoteUid = uid;
                setRemoteVideoView(uid);
            });
        }
    }

    private void setRemoteVideoView(int uid) {
        TextureView videoView = new TextureView(application());
        rtcEngine.setupRemoteVideo(new VideoCanvas(
                videoView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        mRemoteViewContainer.addView(videoView);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        runOnUiThread(this::onRemoteUserLeft);
    }

    private void onRemoteUserLeft() {
        mRemoteUid = -1;
        removeRemoteView();
    }

    private void removeRemoteView() {
        mRemoteViewContainer.removeAllViews();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            if (Math.abs(x) > 3 || Math.abs(y) > 3) {
                if (Math.abs(x) > Math.abs(y)) {
                    mFURenderer.setDeviceOrientation(x > 0 ? 0 : 180);
                } else {
                    mFURenderer.setDeviceOrientation(y > 0 ? 90 : 270);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private static final int ENCODE_FRAME_WIDTH = 640;
    private static final int ENCODE_FRAME_HEIGHT = 360;
    private static final int ENCODE_FRAME_BITRATE = 1000;
    private static final int ENCODE_FRAME_FPS = 24;

    private CSVUtils mCSVUtils;

    private void initCsvUtil(Context context) {
        mCSVUtils = new CSVUtils(context);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String dateStrDir = format.format(new Date(System.currentTimeMillis()));
        dateStrDir = dateStrDir.replaceAll("-", "").replaceAll("_", "");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        String dateStrFile = df.format(new Date());
        String filePath = io.agora.profile.Constant.filePath + dateStrDir + File.separator + "excel-" + dateStrFile + ".csv";
        Log.d(TAG, "initLog: CSV file path:" + filePath);
        StringBuilder headerInfo = new StringBuilder();
        headerInfo.append("version：").append(FURenderer.getInstance().getVersion()).append(CSVUtils.COMMA)
                .append("机型：").append(android.os.Build.MANUFACTURER).append(android.os.Build.MODEL).append(CSVUtils.COMMA)
                .append("处理方式：双输入纹理输出").append(CSVUtils.COMMA)
                .append("编码方式：硬件编码").append(CSVUtils.COMMA)
                .append("编码分辨率：").append(ENCODE_FRAME_WIDTH).append("x").append(ENCODE_FRAME_HEIGHT).append(CSVUtils.COMMA)
                .append("编码帧率：").append(ENCODE_FRAME_FPS).append(CSVUtils.COMMA)
                .append("编码码率：").append(ENCODE_FRAME_BITRATE).append(CSVUtils.COMMA)
                .append("预览分辨率：").append(CAPTURE_WIDTH).append("x").append(CAPTURE_HEIGHT).append(CSVUtils.COMMA)
                .append("预览帧率：").append(CAPTURE_FRAME_RATE).append(CSVUtils.COMMA);
        mCSVUtils.initHeader(filePath, headerInfo);
        mCSVUtils.setRtcEngineEventHandler(((MyApplication) getApplication()).getRtcEventHandler());
    }
}
