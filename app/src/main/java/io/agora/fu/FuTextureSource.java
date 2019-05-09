package io.agora.fu;

import android.graphics.SurfaceTexture;

import java.lang.ref.WeakReference;

import io.agora.rtc.gl.EglBase;
import io.agora.rtc.mediaio.IVideoFrameConsumer;
import io.agora.rtc.mediaio.IVideoSource;
import io.agora.rtc.mediaio.MediaIO;

/**
 * @author Richie on 2019.03.07
 * from io.agora.rtc.mediaio.TextureSource
 */
public abstract class FuTextureSource implements IVideoSource, FuSurfaceTextureHelper.OnTextureFrameAvailableListener {
    protected WeakReference<IVideoFrameConsumer> mConsumer;
    protected FuSurfaceTextureHelper mSurfaceTextureHelper;
    protected int mWidth;
    protected int mHeight;
    protected int mPixelFormat;

    public FuTextureSource(EglBase.Context sharedContext, int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        this.mPixelFormat = MediaIO.PixelFormat.TEXTURE_OES.intValue();
        this.mSurfaceTextureHelper = FuSurfaceTextureHelper.create("TexCam", sharedContext);
        this.mSurfaceTextureHelper.getSurfaceTexture().setDefaultBufferSize(width, height);
        this.mSurfaceTextureHelper.startListening(this);
    }

    @Override
    public boolean onInitialize(IVideoFrameConsumer observer) {
        this.mConsumer = new WeakReference<>(observer);
        return this.onCapturerOpened();
    }

    @Override
    public boolean onStart() {
        return this.onCapturerStarted();
    }

    @Override
    public void onStop() {
        this.onCapturerStopped();
    }

    @Override
    public void onDispose() {
        this.mConsumer = null;
        this.onCapturerClosed();
    }

    @Override
    public int getBufferType() {
        return MediaIO.BufferType.TEXTURE.intValue();
    }

    @Override
    public void onTextureFrameAvailable(int oesTextureId, float[] transformMatrix, long timestampNs) {
        this.mSurfaceTextureHelper.returnTextureFrame();
    }

    public void reCreateSurfaceTexture() {
        mSurfaceTextureHelper.createTexture();
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.mSurfaceTextureHelper.getSurfaceTexture();
    }

    public EglBase.Context getEglContext() {
        return this.mSurfaceTextureHelper.getEglContext();
    }

    public void release() {
        this.mSurfaceTextureHelper.stopListening();
        this.mSurfaceTextureHelper.dispose();
    }

    protected abstract boolean onCapturerOpened();

    protected abstract boolean onCapturerStarted();

    protected abstract void onCapturerStopped();

    protected abstract void onCapturerClosed();
}
