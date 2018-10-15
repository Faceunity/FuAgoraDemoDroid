package io.agora.tutorials.customizedvideosource;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.faceunity.FURenderer;
import com.faceunity.ui.BeautyControlView;
import com.faceunity.utils.EffectEnum;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.AgoraVideoFrame;
import io.agora.rtc.video.VideoCanvas;
import io.agora.tutorials.helper.CustomizedCameraRenderer;

public class VideoChatViewActivity extends AppCompatActivity implements FURenderer.OnTrackingStatusChangedListener {

    private static final String TAG = VideoChatViewActivity.class.getSimpleName();

    private static final boolean DBG = false;

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;

    private CustomizedCameraRenderer mCustomizedCameraRenderer; // Tutorial Step 3
    private RtcEngine mRtcEngine; // Tutorial Step 1
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) { // Tutorial Step 7
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft();
                }
            });
        }
    };

    private FURenderer mFURenderer;
    private TextView mTvNoFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat_view);
        mTvNoFace = findViewById(R.id.tv_no_face);
        BeautyControlView beautyControlView = findViewById(R.id.faceunity_control);
        mFURenderer = new FURenderer
                .Builder(this)
                .inputTextureType(0)
                .defaultEffect(EffectEnum.Effect_fengya_ztt_fu.effect())
                .setOnTrackingStatusChangedListener(this)
                .build();
        beautyControlView.setOnFUControlListener(mFURenderer);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initAgoraEngineAndJoinChannel();
        }
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
        setupVideoProfile();         // Tutorial Step 2
        setupLocalVideo(getApplicationContext()); // Tutorial Step 3
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.CAMERA);
                    finish();
                }
                break;
            }
            default:
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveChannel();
        RtcEngine.destroy();
        mCustomizedCameraRenderer.onSurfaceDestroyed();
        mRtcEngine = null;
    }

    // Tutorial Step 8
    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    // Tutorial Step 6
    public void onEncCallClicked(View view) {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确认要退出吗？")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        VideoChatViewActivity.this.finish();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    // Tutorial Step 1
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
    }

    // Tutorial Step 2
    private void setupVideoProfile() {
        mRtcEngine.enableVideo();

        if (mRtcEngine.isTextureEncodeSupported()) {
            mRtcEngine.setExternalVideoSource(true, true, true);
        } else {
            throw new RuntimeException("Can not work on device do not supporting texture" + mRtcEngine.isTextureEncodeSupported());
        }
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, true);
    }

    private volatile boolean mJoined = false;

    // Tutorial Step 3
    private CustomizedCameraRenderer setupLocalVideo(Context ctx) {
        FrameLayout container = findViewById(R.id.local_video_view_container);
        CustomizedCameraRenderer surfaceV = new CustomizedCameraRenderer(ctx);

        mCustomizedCameraRenderer = surfaceV;
        mCustomizedCameraRenderer.setOnRendererStatusListener(new CustomizedCameraRenderer.OnRendererStatusListener() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                mFURenderer.onSurfaceCreated();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {

            }

            @Override
            public int onDrawFrame(int textureId, EGLContext eglContext, int cameraWidth, int cameraHeight, float[] matrix) {
                int fuTexId = mFURenderer.onDrawFrame(textureId, cameraWidth, cameraHeight);
                AgoraVideoFrame vf = new AgoraVideoFrame();
                vf.format = AgoraVideoFrame.FORMAT_TEXTURE_2D;
                vf.timeStamp = System.currentTimeMillis();
                vf.stride = 1080;
                vf.height = 1920;
                vf.textureID = fuTexId;
                vf.syncMode = true;
                vf.eglContext11 = eglContext;
                vf.transform = matrix;

                boolean result = mRtcEngine.pushExternalVideoFrame(vf);

                if (DBG) {
                    Log.d(TAG, "onFrameAvailable "+ fuTexId + " " + result + " " + Arrays.toString(matrix));
                }
                return fuTexId;
            }

            @Override
            public void onSurfaceDestroy() {
                mFURenderer.onSurfaceDestroyed();
            }

            @Override
            public void onCameraChange(int currentCameraType, int cameraOrientation) {
                Log.d(TAG, "onCameraChange: type:" + currentCameraType + ". orientation:" + cameraOrientation);
                mFURenderer.onCameraChange(currentCameraType, cameraOrientation);
            }
        });

        mCustomizedCameraRenderer.setOnEGLContextHandler(new CustomizedCameraRenderer.OnEGLContextListener() {
            @Override
            public void onEGLContextReady(EGLContext eglContext) {
                Log.d(TAG, "onEGLContextReady " + eglContext + " " + mJoined);

                if (!mJoined) {
                    joinChannel(); // Tutorial Step 4
                    mJoined = true;
                }
            }
        });

        surfaceV.setZOrderMediaOverlay(true);

        container.addView(surfaceV);
        return surfaceV;
    }

    // Tutorial Step 4
    private void joinChannel() {
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        mRtcEngine.joinChannel(null, "CustomizedVideoSourceChannel1", "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
    }

    // Tutorial Step 5
    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);

        if (container.getChildCount() >= 1) {
            return;
        }

        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
        surfaceView.setTag(uid); // for mark purpose
    }

    // Tutorial Step 6
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // Tutorial Step 7
    private void onRemoteUserLeft() {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        container.removeAllViews();
    }

    /**
     * 切换摄像头
     *
     * @param view
     */
    public void onChangeCamera(View view) {
        leaveChannel();
        if (mRtcEngine.isTextureEncodeSupported()) {
            mRtcEngine.setExternalVideoSource(false, true, true);
        }
        mCustomizedCameraRenderer.changeCamera();
        if (mRtcEngine.isTextureEncodeSupported()) {
            mRtcEngine.setExternalVideoSource(true, true, true);
        }
        joinChannel();
    }

    @Override
    public void onTrackingStatusChanged(final int status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvNoFace.setVisibility(status > 0 ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }


    @Override
    public void onBackPressed() {
        onEncCallClicked(null);
    }
}
