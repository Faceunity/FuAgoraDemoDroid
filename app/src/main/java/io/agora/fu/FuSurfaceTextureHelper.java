package io.agora.fu;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.concurrent.Callable;

import io.agora.rtc.gl.EglBase;
import io.agora.rtc.gl.GlUtil;
import io.agora.rtc.utils.ThreadUtils;

/**
 * @author Richie on 2019.03.07
 * from io.agora.rtc.mediaio.SurfaceTextureHelper
 */
public class FuSurfaceTextureHelper {
    private final Handler handler;
    private final EglBase eglBase;
    private final int oesTextureId;
    private final Runnable setListenerRunnable;
    private SurfaceTexture surfaceTexture;
    private FuSurfaceTextureHelper.OnTextureFrameAvailableListener listener;
    private boolean hasPendingTexture;
    private volatile boolean isTextureInUse;
    private boolean isQuitting;
    private FuSurfaceTextureHelper.OnTextureFrameAvailableListener pendingListener;
    private float[] mTransformMatrix = new float[16];

    private FuSurfaceTextureHelper(EglBase.Context sharedContext, Handler handler) {
        this.hasPendingTexture = false;
        this.isTextureInUse = false;
        this.isQuitting = false;
        this.setListenerRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("FuSurfaceTextureHelper", "Setting listener to " + FuSurfaceTextureHelper.this.pendingListener);
                FuSurfaceTextureHelper.this.listener = FuSurfaceTextureHelper.this.pendingListener;
                FuSurfaceTextureHelper.this.pendingListener = null;
                if (FuSurfaceTextureHelper.this.hasPendingTexture) {
                    FuSurfaceTextureHelper.this.updateTexImage();
                    FuSurfaceTextureHelper.this.hasPendingTexture = false;
                }
            }
        };
        if (handler.getLooper().getThread() != Thread.currentThread()) {
            throw new IllegalStateException("FuSurfaceTextureHelper must be created on the handler thread");
        } else {
            this.handler = handler;
            this.eglBase = EglBase.create(sharedContext, EglBase.CONFIG_PIXEL_BUFFER);

            try {
                this.eglBase.createDummyPbufferSurface();
                this.eglBase.makeCurrent();
            } catch (RuntimeException var4) {
                Log.e("FuSurfaceTextureHelper", "FuSurfaceTextureHelper: failed to create pbufferSurface!!");
                this.eglBase.release();
                handler.getLooper().quit();
                throw var4;
            }

            this.oesTextureId = GlUtil.generateTexture(36197);
            createTexture();
        }
    }

    public static FuSurfaceTextureHelper create(final String threadName, final EglBase.Context sharedContext) {
        HandlerThread thread = new HandlerThread(threadName);
        thread.start();
        final Handler handler = new Handler(thread.getLooper());
        return ThreadUtils.invokeAtFrontUninterruptibly(handler, new Callable<FuSurfaceTextureHelper>() {
            @Override
            public FuSurfaceTextureHelper call() {
                try {
                    return new FuSurfaceTextureHelper(sharedContext, handler);
                } catch (RuntimeException var2) {
                    Log.e("FuSurfaceTextureHelper", threadName + " create failure", var2);
                    return null;
                }
            }
        });
    }

    @TargetApi(21)
    private static void setOnFrameAvailableListener(SurfaceTexture surfaceTexture, SurfaceTexture.OnFrameAvailableListener listener, Handler handler) {
        if (Build.VERSION.SDK_INT >= 21) {
            surfaceTexture.setOnFrameAvailableListener(listener, handler);
        } else {
            surfaceTexture.setOnFrameAvailableListener(listener);
        }
    }

    public void createTexture() {
        this.surfaceTexture = new SurfaceTexture(this.oesTextureId);
        setOnFrameAvailableListener(this.surfaceTexture, new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                FuSurfaceTextureHelper.this.hasPendingTexture = true;
                FuSurfaceTextureHelper.this.tryDeliverTextureFrame();
            }
        }, getHandler());
    }

    public EglBase.Context getEglContext() {
        return this.eglBase.getEglBaseContext();
    }

    public void startListening(FuSurfaceTextureHelper.OnTextureFrameAvailableListener listener) {
        if (this.listener == null && this.pendingListener == null) {
            this.pendingListener = listener;
            this.handler.post(this.setListenerRunnable);
        } else {
            throw new IllegalStateException("FuSurfaceTextureHelper listener has already been set.");
        }
    }

    public void stopListening() {
        Log.d("FuSurfaceTextureHelper", "stopListening()");
        this.handler.removeCallbacks(this.setListenerRunnable);
        ThreadUtils.invokeAtFrontUninterruptibly(this.handler, new Runnable() {
            @Override
            public void run() {
                FuSurfaceTextureHelper.this.listener = null;
                FuSurfaceTextureHelper.this.pendingListener = null;
            }
        });
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.surfaceTexture;
    }

    public Handler getHandler() {
        return this.handler;
    }

    public void returnTextureFrame() {
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                FuSurfaceTextureHelper.this.isTextureInUse = false;
                if (FuSurfaceTextureHelper.this.isQuitting) {
                    FuSurfaceTextureHelper.this.release();
                } else {
                    FuSurfaceTextureHelper.this.tryDeliverTextureFrame();
                }

            }
        });
    }

    public void dispose() {
        Log.d("FuSurfaceTextureHelper", "dispose()");
        ThreadUtils.invokeAtFrontUninterruptibly(this.handler, new Runnable() {
            @Override
            public void run() {
                FuSurfaceTextureHelper.this.isQuitting = true;
                if (!FuSurfaceTextureHelper.this.isTextureInUse) {
                    FuSurfaceTextureHelper.this.release();
                }

            }
        });
    }

    private void updateTexImage() {
        try {
            synchronized (EglBase.lock) {
                this.surfaceTexture.updateTexImage();
            }
        } catch (IllegalStateException var4) {
            Log.e("FuSurfaceTextureHelper", "FuSurfaceTextureHelper: failed to updateTexImage!!");
        }

    }

    private void tryDeliverTextureFrame() {
        safelyRunOnGLThread(new Runnable() {
            @Override
            public void run() {
                if (!isQuitting && hasPendingTexture && !isTextureInUse && listener != null) {
                    isTextureInUse = true;
                    hasPendingTexture = false;
                    updateTexImage();
                    surfaceTexture.getTransformMatrix(mTransformMatrix);
                    long timestampNs = surfaceTexture.getTimestamp();
                    listener.onTextureFrameAvailable(oesTextureId, mTransformMatrix, timestampNs);
                }
            }
        });
    }

    private void release() {
        safelyRunOnGLThread(new Runnable() {
            @Override
            public void run() {
                if (!isTextureInUse && isQuitting) {
                    GLES20.glDeleteTextures(1, new int[]{oesTextureId}, 0);
                    surfaceTexture.release();
                    eglBase.release();
                    handler.getLooper().quit();
                } else {
                    throw new IllegalStateException("Unexpected release.");
                }
            }
        });
    }

    private void safelyRunOnGLThread(Runnable runnable) {
        if (Thread.currentThread() == handler.getLooper().getThread()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    public interface OnTextureFrameAvailableListener {
        void onTextureFrameAvailable(int texId, float[] transMatrix, long timestamp);
    }
}
