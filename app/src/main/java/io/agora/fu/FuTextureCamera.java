package io.agora.fu;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;

import io.agora.rtc.gl.RendererCommon;
import io.agora.rtc.mediaio.MediaIO;

/**
 * @author Richie on 2019.03.07
 */
public class FuTextureCamera extends FuTextureSource implements Camera.PreviewCallback {
    private static final String TAG = "FuTextureCamera";
    private static final int PREVIEW_BUFFER_COUNT = 3;
    private final Object mCameraLock = new Object();
    private Context mContext;
    private Camera mCamera;
    private byte[][] mPreviewCallbackBuffer;
    private OnCaptureListener mOnCaptureListener;
    private volatile byte[] mCameraNV21Byte;
    private int mCameraOrientation;
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public FuTextureCamera(Context context, int width, int height) {
        super(null, width, height);
        mContext = context;
    }

    public Handler getHandler() {
        return mSurfaceTextureHelper.getHandler();
    }

    public void setOnCaptureListener(OnCaptureListener onCaptureListener) {
        mOnCaptureListener = onCaptureListener;
    }

    public void onResume() {
        openCamera(mCameraFacing);
        previewCamera();
    }

    public void onPause() {
        releaseCamera();
    }

    public void switchCameraFacing() {
        releaseCamera();
        int facing = mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT ? Camera.CameraInfo.CAMERA_FACING_BACK :
                Camera.CameraInfo.CAMERA_FACING_FRONT;
        openCamera(facing);
        reCreateSurfaceTexture();
        previewCamera();
        if (mOnCaptureListener != null) {
            mOnCaptureListener.onCameraSwitched(mCameraFacing, mCameraOrientation);
        }
    }

    @Override
    protected boolean onCapturerOpened() {
        openCamera(mCameraFacing);
        previewCamera();
        return true;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera mCamera) {
        mCameraNV21Byte = data;
        mCamera.addCallbackBuffer(data);
    }

    @Override
    public void onTextureFrameAvailable(int oesTextureId, float[] transformMatrix, long timestampNs) {
        super.onTextureFrameAvailable(oesTextureId, transformMatrix, timestampNs);

        if (mCameraNV21Byte == null) {
            return;
        }

        // 前置相机 270 方向 需要处理
        if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT && mCameraOrientation == 270) {
            transformMatrix = RendererCommon.multiplyMatrices(transformMatrix, RendererCommon.verticalFlipMatrix());
            transformMatrix = RendererCommon.multiplyMatrices(transformMatrix, RendererCommon.horizontalFlipMatrix());
        }

        int fuTexId = 0;
        if (mOnCaptureListener != null && mCameraNV21Byte != null) {
            fuTexId = mOnCaptureListener.onTextureBufferAvailable(oesTextureId, mCameraNV21Byte, mWidth, mHeight);
        }

        if (mConsumer != null && mConsumer.get() != null) {
            if (fuTexId > 0) {
                this.mConsumer.get().consumeTextureFrame(fuTexId, MediaIO.PixelFormat.TEXTURE_2D.intValue(),
                        mWidth, mHeight, mCameraOrientation, System.currentTimeMillis(), transformMatrix);
            }
        }
    }

    @Override
    protected boolean onCapturerStarted() {
        mCamera.startPreview();
        if (mOnCaptureListener != null) {
            mOnCaptureListener.onCapturerStarted();
        }
        return true;
    }

    @Override
    protected void onCapturerStopped() {
        if (mOnCaptureListener != null) {
            mOnCaptureListener.onCapturerStopped();
        }
        mCamera.stopPreview();
    }

    @Override
    protected void onCapturerClosed() {
        releaseCamera();
    }

    private void previewCamera() {
        try {
            mCamera.setPreviewTexture(getSurfaceTexture());
            mCamera.setPreviewCallbackWithBuffer(this);
            if (mPreviewCallbackBuffer == null) {
                mPreviewCallbackBuffer = new byte[PREVIEW_BUFFER_COUNT][mWidth * mHeight * 3 / 2];
            }
            for (int i = 0; i < PREVIEW_BUFFER_COUNT; i++) {
                mCamera.addCallbackBuffer(mPreviewCallbackBuffer[i]);
            }
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "previewCamera: ", e);
        }
    }

    private void openCamera(int cameraFacing) {
        synchronized (mCameraLock) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int numCameras = Camera.getNumberOfCameras();

            for (int i = 0; i < numCameras; ++i) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == cameraFacing) {
                    mCamera = Camera.open(i);
                    mCameraFacing = cameraFacing;
                    break;
                }
            }

            if (mCamera == null) {
                Log.d(TAG, "No front-facing mCamera found; opening default");
                mCamera = Camera.open();
            }

            if (mCamera == null) {
                throw new RuntimeException("Unable to open camera");
            } else {
                Camera.Parameters parms = mCamera.getParameters();
//                List<int[]> frameRates = parms.getSupportedPreviewFpsRange();
//                int minFps = frameRates.get(frameRates.size() - 1)[0];
//                int maxFps = frameRates.get(frameRates.size() - 1)[1];
//                parms.setPreviewFpsRange(minFps, maxFps);
                parms.setPreviewSize(mWidth, mHeight);
//                parms.setRecordingHint(true);
                mCamera.setParameters(parms);
                Camera.Size cameraPreviewSize = parms.getPreviewSize();
                String previewFacts = cameraPreviewSize.width + "x" + cameraPreviewSize.height;
                int deviceOrientation = getDeviceOrientation();
                mCameraOrientation = getFrameOrientation(cameraInfo, deviceOrientation);
                Log.i(TAG, "open Camera config: " + previewFacts + ", orientation:" + mCameraOrientation);
            }
        }
    }

    private int getDeviceOrientation() {
        WindowManager wm = (WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return 0;
        }
        int orientation;
        switch (wm.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                orientation = 0;
                break;
            case Surface.ROTATION_90:
                orientation = 90;
                break;
            case Surface.ROTATION_180:
                orientation = 180;
                break;
            case Surface.ROTATION_270:
                orientation = 270;
                break;
            default:
                orientation = 0;
        }
        return orientation;
    }

    private int getFrameOrientation(Camera.CameraInfo cameraInfo, int rotation) {
        if (cameraInfo.facing == 0) {
            rotation = 360 - rotation;
        }
        return (cameraInfo.orientation + rotation) % 360;
    }

    private void releaseCamera() {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                try {
                    mCamera.stopPreview();
                    mCamera.setPreviewTexture(null);
                    mCamera.setPreviewCallbackWithBuffer(null);
                } catch (Exception var2) {
                    Log.e(TAG, "failed to set Preview Texture");
                }

                mCamera.release();
                mCamera = null;
                Log.d(TAG, "releaseCamera -- done");
            }
        }
        mCameraNV21Byte = null;
    }

    public interface OnCaptureListener {
        /**
         * 取到每帧相机数据时回调
         *
         * @param textureId oesTexture
         * @param buffer    byter array
         * @param width     cameraWidth
         * @param height    cameraHeight
         * @return texture2D
         */
        int onTextureBufferAvailable(int textureId, byte[] buffer, int width, int height);

        /**
         * 开始预览
         */
        void onCapturerStarted();

        /**
         * 结束预览
         */
        void onCapturerStopped();

        /**
         * 相机方向切换
         *
         * @param facing      前后
         * @param orientation 方向
         */
        void onCameraSwitched(int facing, int orientation);
    }

}
