package io.agora.tutorials.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

public class CustomizedCameraRenderer extends GLSurfaceView implements
        GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "CustomizedRenderer";

    private static final boolean DEBUG = false;
    private EGLContext mEGLCurrentContext;
    private final Object mLock = new Object();

    private OnRendererStatusListener mOnRendererStatusListener;
    private float[] mMatrix = new float[16];

    public void setOnRendererStatusListener(OnRendererStatusListener onRendererStatusListener) {
        mOnRendererStatusListener = onRendererStatusListener;
    }

    public interface OnRendererStatusListener {

        void onSurfaceCreated(GL10 gl, EGLConfig config);

        void onSurfaceChanged(GL10 gl, int width, int height);

        int onDrawFrame(int textureId, EGLContext eglContext, int cameraWidth, int cameraHeight, float[] matrix);

        void onSurfaceDestroy();

        void onCameraChange(int currentCameraType, int cameraOrientation);
    }

    public interface OnEGLContextListener {
        void onEGLContextReady(EGLContext eglContext);
    }

    private OnEGLContextListener mOnEGLContextHandler;

    private Context mContext;

    private Camera mCamera;

    private volatile int mCameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int mCameraId;
    // mount angle, 270 degrees; back: 90 degrees
    // content: back camera's top row is aligned to left bar; front camera's top row is aligned to right bar
    // counter-clock wise

    private volatile int mCameraRotation = 0;

    private volatile SurfaceTexture mSurfaceTexture;
    private volatile SurfaceTexture mDstSurfaceTexture;

    public final OESTexture mSrcTexture = new OESTexture();
    private final Shader mOffscreenShader = new Shader();
    private int mViewWidth, mViewHeight;
    private volatile boolean mUpdateTexture = false;
    private volatile boolean mIsChangeingCamera = false;

    private float[] mOrientationM = new float[16];
    private float[] mRatio = new float[2];
    private int mCameraPreviewHeight = 720;
    private int mCameraPreviewWidth = 1280;
    private volatile boolean mPreviewing = false;

    public CustomizedCameraRenderer(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CustomizedCameraRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        // Create full scene quad buffer
        final byte FULL_QUAD_COORDS[] = {-1, 1, -1, -1, 1, 1, 1, -1};
        ByteBuffer fullQuadVertices = ByteBuffer.allocateDirect(4 * 2);
        fullQuadVertices.put(FULL_QUAD_COORDS).position(0);

        setEGLContextFactory(new MyContextFactory(this));

        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(2);
        setRenderer(this);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setDebugFlags(DEBUG_LOG_GL_CALLS);
    }

    public void setOnEGLContextHandler(OnEGLContextListener listener) {
        this.mOnEGLContextHandler = listener;
    }

    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (!mIsChangeingCamera) {
            mUpdateTexture = true;
            requestRender();
        }
    }

    public void initCameraTexture() {
        try {
            mOffscreenShader.setProgram(io.agora.tutorials.customizedvideosource.R.raw.vshader, io.agora.tutorials.customizedvideosource.R.raw.fshader, mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mOnEGLContextHandler != null) {
            if (mEGLCurrentContext != null) {
                mOnEGLContextHandler.onEGLContextReady(mEGLCurrentContext);
            }
        }

        // Generate camera texture
        mSrcTexture.init();
    }

    @Override
    public synchronized void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (DEBUG) {
            Log.i(TAG, "onSurfaceCreated " + gl);
        }
        initCameraTexture();
        if (mOnRendererStatusListener != null) {
            mOnRendererStatusListener.onSurfaceCreated(gl, config);
        }
        mCameraToFbo = new TextureRenderer(true);
        mFboToView = new TextureRenderer(false);
        Log.i(TAG, "onSurfaceCreated " + gl + " end");
    }

    @SuppressLint("NewApi")
    @Override
    public synchronized void onSurfaceChanged(GL10 gl, int width, int height) {
        if (DEBUG) {
            Log.i(TAG, "onSurfaceChanged " + gl + " " + width + " " + height);
        }

        mViewWidth = width;
        mViewHeight = height;

        openCamera();

        if (mOnRendererStatusListener != null) {
            mOnRendererStatusListener.onSurfaceChanged(gl, width, height);
        }

        Log.i(TAG, "onSurfaceChanged end " + " " + mCameraRotation + " " + mPreviewing);
    }

    private TextureRenderer mFboToView;
    private TextureRenderer mCameraToFbo;

    public void changeCamera() {
        if (mIsChangeingCamera) {
            return;
        }
        mIsChangeingCamera = true;
        long begin = System.currentTimeMillis();
        releaseCamera();
        synchronized (mLock) {
            mCameraType = mCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT ? Camera.CameraInfo.CAMERA_FACING_BACK :
                    Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        openCamera();
        mIsChangeingCamera = false;
        long duration = System.currentTimeMillis() - begin;
        Log.i(TAG, "changeCamera: duration:" + duration);
    }

    public int getDisplayRotation() {
        int rotation = ((WindowManager) (getContext().getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
        }
        return 0;
    }

    private void openCamera() {
        synchronized (mLock) {
            if (mCamera == null) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                int numCameras = Camera.getNumberOfCameras();
                for (int i = 0; i < numCameras; i++) {
                    Camera.getCameraInfo(i, info);
                    if (info.facing == mCameraType) {
                        mCameraId = i;
                        mCamera = Camera.open(i);
                        mCameraType = info.facing;
                        break;
                    }
                }
            }

            if (mPreviewing) {
                mCamera.stopPreview();
            }

            // Set up SurfaceTexture
            SurfaceTexture oldSurfaceTexture = mSurfaceTexture;
            mSurfaceTexture = new SurfaceTexture(mSrcTexture.getTextureId());
            mSurfaceTexture.setOnFrameAvailableListener(this);
            if (oldSurfaceTexture != null) {
                oldSurfaceTexture.release();
            }

            // set camera paras
            int camera_width = 0;
            int camera_height = 0;

            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
            } catch (IOException ioe) {
                Log.w(TAG, "setPreviewTexture " + Log.getStackTraceString(ioe));
            }

            Camera.Parameters param = mCamera.getParameters();
            List<Size> psize = param.getSupportedPreviewSizes();
            if (psize.size() > 0) {
                boolean supports_1280_720 = false;
                for (int i = 0; i < psize.size(); i++) {
                    if ((psize.get(i).width == 1280) && (psize.get(i).height == 720)) {
                        supports_1280_720 = true;
                    }
                }
                if (supports_1280_720) {
                    mCameraPreviewWidth = 1280;
                    mCameraPreviewHeight = 720;
                } else {
                    mCameraPreviewWidth = param.getSupportedPreviewSizes().get(0).width;
                    mCameraPreviewHeight = param.getSupportedPreviewSizes().get(0).height;
                }

                Log.i(TAG, "setPreviewSize " + mCameraPreviewWidth + " " + mCameraPreviewHeight);
                param.setPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight);
            }

            // get the camera orientation and display dimension
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                Matrix.setRotateM(mOrientationM, 0, 90.0f, 0f, 0f, 1f);
                mRatio[1] = camera_width * 1.0f / mViewHeight;
                mRatio[0] = camera_height * 1.0f / mViewWidth;
            } else {
                Matrix.setRotateM(mOrientationM, 0, 0.0f, 0f, 0f, 1f);
                mRatio[1] = camera_height * 1.0f / mViewHeight;
                mRatio[0] = camera_width * 1.0f / mViewWidth;
            }

            // start camera
            mCamera.setParameters(param);

            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            mCameraRotation = info.orientation;
            mCamera.startPreview();

            mPreviewing = true;

            if (mOnRendererStatusListener != null) {
                mOnRendererStatusListener.onCameraChange(mCameraType, mCameraRotation);
            }
        }
    }

    private int mFbo = 0;
    private int mDstTexture = 0;

    @Override
    public synchronized void onDrawFrame(GL10 gl) {
        if (DEBUG) {
            Log.i(TAG, "onDrawFrame " + mUpdateTexture + " " + mCameraPreviewWidth + " " + mCameraPreviewHeight);
        }
        if (!mUpdateTexture) {
            return;
        }

        if (mFbo == 0) {
            createFbo(1080, 1920);

            if (mDstSurfaceTexture != null) {
                mDstSurfaceTexture.release();
            }
            mDstSurfaceTexture = new SurfaceTexture(mDstTexture);
        }

        // Calculate rotated degrees (camera to view)
        int degrees = getDisplayRotation();

        // render the texture to FBO if new frame is available
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // Latch surface texture
        mSurfaceTexture.updateTexImage();

        // Render to FBO
        GLES20.glFinish();
        GLES20.glViewport(0, 0, 1080, 1920);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFbo);
        mCameraToFbo.rotate(mCameraRotation);
        mCameraToFbo.draw(mSrcTexture.getTextureId());
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        // Render to this view
        int targetWidth = mViewWidth;
        int targetHeight = (int) ((mCameraPreviewWidth * targetWidth * 100.0f / mCameraPreviewHeight) / 100);

        int rotation;
        if (mCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (-degrees + 180) % 360;
        } else {
            rotation = (degrees + 180) % 360;
        }

        if (DEBUG) {
            Log.i(TAG, "glViewport " + targetWidth + " " + targetHeight + " " + mViewWidth + " " + mViewHeight + " " + mCameraPreviewWidth + " " + mCameraPreviewHeight + " " + rotation);
        }
        GLES20.glViewport(0, 0, targetWidth, targetHeight);

        mFboToView.rotate(rotation);
        if (mCameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mFboToView.flip(true, false);
        }

        mDstSurfaceTexture.getTransformMatrix(mMatrix);
        if (mOnRendererStatusListener != null) {
            int i = mOnRendererStatusListener.onDrawFrame(mDstTexture, mEGLCurrentContext,
                    targetWidth, targetHeight, mMatrix);
            mFboToView.draw(i);
        }

        if (DEBUG) {
            Log.i(TAG, "onDrawFrame end orientation " + mCameraRotation + " " + degrees);
        }
    }

    public void onSurfaceDestroyed() {
        deinitCameraTexture();
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mOnRendererStatusListener != null) {
                    mOnRendererStatusListener.onSurfaceDestroy();
                }
            }
        });
    }

    private void releaseCamera() {
        try {
            synchronized (mLock) {
                if (mSurfaceTexture != null) {
                    mSurfaceTexture.setOnFrameAvailableListener(null);
                }
                mUpdateTexture = false;
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.setPreviewTexture(null);
                    mCamera.release();
                    mCamera = null;
                }
                mPreviewing = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deinitCameraTexture() {
        mUpdateTexture = false;

        if (mDstSurfaceTexture != null) {
            mDstSurfaceTexture.release();
        }

        mSurfaceTexture.release();
        mEGLCurrentContext = null;
        releaseCamera();
    }

    private int createFbo(int width, int height) {
        int[] texture = new int[1];
        int[] fbo = new int[1];
        GLES20.glGenFramebuffers(1, fbo, 0);
        GLES20.glGenTextures(1, texture, 0);
        mFbo = fbo[0];
        mDstTexture = texture[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDstTexture);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        // Bind the framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFbo);
        // Specify texture as color attachment
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mDstTexture, 0);

        // Check for framebuffer complete
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "Failed to create framebuffer!!!");
        }

        return 0;
    }

    private static class MyContextFactory implements EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        private CustomizedCameraRenderer mRenderer;

        public MyContextFactory(CustomizedCameraRenderer renderer) {
            Log.i(TAG, "MyContextFactory " + renderer);
            this.mRenderer = renderer;
        }

        private static void checkEglError(String prompt, EGL10 egl) {
            int error;
            while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
                Log.i(TAG, String.format(Locale.US, "%s: EGL error: 0x%x", prompt, error));
            }
        }

        @Override
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            Log.i(TAG, "createContext " + egl + " " + display + " " + eglConfig);
            checkEglError("before createContext", egl);
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};

            EGLContext ctx;

            if (mRenderer.mEGLCurrentContext == null) {
                mRenderer.mEGLCurrentContext = egl.eglCreateContext(display, eglConfig,
                        EGL10.EGL_NO_CONTEXT, attrib_list);
                ctx = mRenderer.mEGLCurrentContext;
            } else {
                ctx = mRenderer.mEGLCurrentContext;
            }
            checkEglError("after createContext", egl);
            return ctx;
        }

        @Override
        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            Log.i(TAG, "destroyContext " + egl + " " + display + " " + context + " " + mRenderer.mEGLCurrentContext);
            if (mRenderer.mEGLCurrentContext == null) {
                egl.eglDestroyContext(display, context);
            }
        }
    }

}
