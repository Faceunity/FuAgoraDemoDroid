package io.agora.sources;

import android.opengl.GLES20;
import android.util.Log;

import com.faceunity.nama.FURenderer;

import java.util.Arrays;

import io.agora.processor.common.connector.SinkConnector;
import io.agora.processor.media.data.CapturedFrame;
import io.agora.processor.media.data.VideoCapturedFrame;
import io.agora.rtcwithfu.gles.ProgramTexture2d;
import io.agora.rtcwithfu.gles.ProgramTextureOES;
import io.agora.rtcwithfu.gles.core.GlUtil;
import io.agora.rtcwithfu.gles.core.Program;

/**
 * Created by lixiaochen on 2020/4/3.
 */

public class EffectHandler implements SinkConnector<CapturedFrame> {
    private static final String TAG = "EffectHandler";
    private static final boolean DEBUG = false;
    private FURenderer mFURenderer;
    private byte[] mRawDataCopy;
    private byte[] mEffectDataCopy;

    public EffectHandler(FURenderer fuRenderer) {
        mFURenderer = fuRenderer;
    }

    @Override
    public void onDataAvailable(CapturedFrame data) {
        VideoCapturedFrame videoCapturedFrame = (VideoCapturedFrame) data;
        int videoHeight = videoCapturedFrame.videoHeight;
        int videoWidth = videoCapturedFrame.videoWidth;
        if (mEffectDataCopy == null) {
            mEffectDataCopy = new byte[data.rawData.length];
        }
        byte[] effectDataCopy = mEffectDataCopy;
        if (mFURenderer != null) {
            if (mRawDataCopy == null) {
                mRawDataCopy = new byte[data.rawData.length];
            }
            // 处理之前，拷贝一次相机 buffer，避免缓冲区重用导致污染
            byte[] rawDataCopy = mRawDataCopy;
            System.arraycopy(data.rawData, 0, rawDataCopy, 0, data.rawData.length);
            // 处理完成后，输入 buffer 底层被修改，带有美颜效果
            int fuTextureId = mFURenderer.onDrawFrameSingleInput(rawDataCopy, videoWidth, videoHeight, FURenderer.INPUT_FORMAT_NV21);
            if (DEBUG) {
                Log.v(TAG, "FU onDataAvailable input:" + data.rawData + ", width:" + videoWidth + ", height:" + videoHeight);
            }
            // 建议优先使用 NV21，可以避免很多问题，谢谢合作，感激不尽！
            videoCapturedFrame.mEffectTextureId = fuTextureId;
            // 送给声网之前，拷贝一次美颜 buffer，避免异步推流导致的数据污染
            System.arraycopy(rawDataCopy, 0, effectDataCopy, 0, rawDataCopy.length);
            videoCapturedFrame.mEffectData = effectDataCopy;

            // 输出 texture 给声网，还要创建 FBO，通过 RTT 再绘一次，声网获取 buffer 还要 glReadPixels，低效不推荐
//            createFBO(videoWidth, videoHeight);
//            renderToTexture(mProgramTexture2d, fuTextureId);
//            videoCapturedFrame.mEffectTextureId = mTexId[0];
        } else {
            createFBO(videoWidth, videoHeight);
            // 这里是为了把 OES 纹理转为 2D 纹理
            renderToTexture(mProgramTextureOes, videoCapturedFrame.mTextureId);
            videoCapturedFrame.mEffectTextureId = mTexId[0];
            System.arraycopy(data.rawData, 0, effectDataCopy, 0, data.rawData.length);
            videoCapturedFrame.mEffectData = effectDataCopy;
        }
    }

    private int[] mFboId;
    private int[] mTexId;
    private int[] mFboOrigin;
    private ProgramTextureOES mProgramTextureOes;
    private ProgramTexture2d mProgramTexture2d;

    private void createFBO(int width, int height) {
        if (mFboId != null) {
            return;
        }
        mFboId = new int[1];
        mTexId = new int[1];
        mFboOrigin = new int[1];
        int[] viewport = new int[4];
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewport, 0);
        GlUtil.createFBO(mTexId, mFboId, viewport[2], viewport[3]);
        mProgramTextureOes = new ProgramTextureOES();
        mProgramTexture2d = new ProgramTexture2d();
        Log.d(TAG, "createFBO fboId: " + mFboId[0] + ", texId: " + mTexId[0] + ", width: "
                + width + ", height: " + height + ", viewport: " + Arrays.toString(viewport));
    }

    private void renderToTexture(Program program, int texture) {
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, mFboOrigin, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId[0]);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        program.drawFrame(texture, GlUtil.IDENTITY_MATRIX, GlUtil.IDENTITY_MATRIX);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboOrigin[0]);
    }

    public void deleteFBO() {
        if (mFboId == null) {
            return;
        }
        Log.d(TAG, "deleteFBO:");
        GlUtil.deleteFBO(mFboId);
        GlUtil.deleteTextureId(mTexId);
        mFboId[0] = 0;
        mFboId = null;
        mTexId[0] = 0;
        mTexId = null;
        mFboOrigin[0] = 0;
        mFboOrigin = null;
        if (mProgramTextureOes != null) {
            mProgramTextureOes.release();
            mProgramTextureOes = null;
        }
        if (mProgramTexture2d != null) {
            mProgramTexture2d.release();
            mProgramTexture2d = null;
        }
    }

}
