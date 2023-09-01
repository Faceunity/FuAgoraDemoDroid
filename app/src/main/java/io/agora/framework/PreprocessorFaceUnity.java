package io.agora.framework;

import android.graphics.Matrix;
import android.util.Log;

import com.faceunity.core.faceunity.FUAIKit;
import com.faceunity.core.faceunity.FURenderKit;
import com.faceunity.core.model.facebeauty.FaceBeautyBlurTypeEnum;
import com.faceunity.nama.FUConfig;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.utils.FuDeviceUtils;

import java.nio.ByteBuffer;

import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.base.internal.video.YuvHelper;
import io.agora.profile.CSVUtils;
import io.agora.rtc2.gl.EglBaseProvider;

public class PreprocessorFaceUnity {
    private final static String TAG = PreprocessorFaceUnity.class.getSimpleName();
    private FURenderer mFURenderer = FURenderer.getInstance();
    private boolean renderSwitch = true;
    private TextureBufferHelper mTextureBufferHelper;
    private ByteBuffer nv21ByteBuffer;
    private byte[] nv21ByteArray;
    private int mFrameRotation;

    public void setCSVUtils(CSVUtils cSVUtils) {
        this.mCSVUtils = cSVUtils;
    }

    private CSVUtils mCSVUtils;

    public PreprocessorFaceUnity() {
    }

    private int originTexId;

    public boolean processBeauty(VideoFrame videoFrame) {
        if (!renderSwitch) {
            return true;
        }

        if (mTextureBufferHelper == null) {
            mTextureBufferHelper = TextureBufferHelper.create("STRender", EglBaseProvider.instance().getRootEglBase().getEglBaseContext());
            if (mTextureBufferHelper != null) {
                mTextureBufferHelper.invoke(() -> {
                    if (mSurfaceViewListener != null) mSurfaceViewListener.onSurfaceCreated();
                    return null;
                });
            }
        }


        VideoFrame.Buffer buffer = videoFrame.getBuffer();
        int width = buffer.getWidth();
        int height = buffer.getHeight();
        int rotation = videoFrame.getRotation();

        boolean skipFrame = false;
        Matrix transformMatrix = new Matrix();
        int nv21Size = (int) (width * height * 3.0f / 2.0f + 0.5f);
        if (nv21ByteBuffer == null || nv21ByteBuffer.capacity() != nv21Size) {
            if (nv21ByteBuffer != null) {
                nv21ByteBuffer.clear();
            }
            nv21ByteBuffer = ByteBuffer.allocateDirect(nv21Size);
            nv21ByteArray = new byte[nv21Size];
            skipFrame = true;
        }

        VideoFrame.I420Buffer i420Buffer = buffer.toI420();
        YuvHelper.I420ToNV12(i420Buffer.getDataY(), i420Buffer.getStrideY(),
                i420Buffer.getDataV(), i420Buffer.getStrideV(),
                i420Buffer.getDataU(), i420Buffer.getStrideU(),
                nv21ByteBuffer, width, height);
        nv21ByteBuffer.position(0);
        nv21ByteBuffer.get(nv21ByteArray);
        i420Buffer.release();

        if (mFrameRotation != rotation) {
            mFrameRotation = rotation;
            skipFrame = true;
        }

        if (skipFrame) {
            return false;
        }

        mFURenderer.setInputOrientation(videoFrame.getRotation(), videoFrame.getSourceType() == VideoFrame.SourceType.kFrontCamera);
        //高性能设备
        if (FUConfig.DEVICE_LEVEL == FuDeviceUtils.DEVICE_LEVEL_HIGH) {
            cheekFaceNum();
        }
        int processTexId = -1;
        originTexId = 0;


        long start = System.nanoTime();
        if (mTextureBufferHelper != null) {
            if (videoFrame.getBuffer() instanceof VideoFrame.TextureBuffer) {
                originTexId = ((VideoFrame.TextureBuffer) videoFrame.getBuffer()).getTextureId();
                if (originTexId == 0) return false;
                processTexId = mTextureBufferHelper.invoke(() ->
                        mFURenderer.onDrawFrameDualInput(nv21ByteArray,
                                originTexId, width,
                                height));
                transformMatrix = ((VideoFrame.TextureBuffer) videoFrame.getBuffer()).getTransformMatrix();
            }
        }

        long renderTime = System.nanoTime() - start;

        if (mCSVUtils != null) {
            mCSVUtils.writeCsv(null, renderTime);
        }

        if (processTexId <= 0) {
            return false;
        }

        if (mTextureBufferHelper != null) {
            VideoFrame.TextureBuffer textureBuffer = mTextureBufferHelper.wrapTextureBuffer(
                    width, height, VideoFrame.TextureBuffer.Type.RGB, processTexId, transformMatrix);
            videoFrame.replaceBuffer(textureBuffer, mFrameRotation, videoFrame.getTimestampNs());
        }
        return true;
    }


    public void setRenderEnable(boolean enabled) {
        renderSwitch = enabled;
    }

    public void releaseFURender() {
        renderSwitch = false;
        if (mTextureBufferHelper != null) {
            mTextureBufferHelper.invoke(() -> {
                if (mSurfaceViewListener != null) mSurfaceViewListener.onSurfaceDestroyed();
                return null;
            });

            boolean disposeSuccess = false;
            while (!disposeSuccess) {
                try {
                    mTextureBufferHelper.dispose();
                    disposeSuccess = true;
                    Log.e("TAG", "releaseFURender");
                } catch (Exception e) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        // do nothing
                    }
                }
            }
            mTextureBufferHelper = null;
        }
    }

    private SurfaceViewListener mSurfaceViewListener;

    public interface SurfaceViewListener {
        void onSurfaceCreated();

        void onSurfaceDestroyed();
    }

    public void setSurfaceListener(SurfaceViewListener surfaceViewListener) {
        this.mSurfaceViewListener = surfaceViewListener;
    }

    /**
     * 检查当前人脸数量
     */
    private void cheekFaceNum() {
        //根据有无人脸 + 设备性能 判断开启的磨皮类型
        float faceProcessorGetConfidenceScore = FUAIKit.getInstance().getFaceProcessorGetConfidenceScore(0);
        if (faceProcessorGetConfidenceScore >= 0.95) {
            //高端手机并且检测到人脸开启均匀磨皮，人脸点位质
            if (FURenderKit.getInstance() != null && FURenderKit.getInstance().getFaceBeauty() != null && FURenderKit.getInstance().getFaceBeauty().getBlurType() != FaceBeautyBlurTypeEnum.EquallySkin) {
                FURenderKit.getInstance().getFaceBeauty().setBlurType(FaceBeautyBlurTypeEnum.EquallySkin);
                FURenderKit.getInstance().getFaceBeauty().setEnableBlurUseMask(true);
            }
        } else {
            if (FURenderKit.getInstance() != null && FURenderKit.getInstance().getFaceBeauty() != null && FURenderKit.getInstance().getFaceBeauty().getBlurType() != FaceBeautyBlurTypeEnum.FineSkin) {
                FURenderKit.getInstance().getFaceBeauty().setBlurType(FaceBeautyBlurTypeEnum.FineSkin);
                FURenderKit.getInstance().getFaceBeauty().setEnableBlurUseMask(false);
            }
        }
    }
}
