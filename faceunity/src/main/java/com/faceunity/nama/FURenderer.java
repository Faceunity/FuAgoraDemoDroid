package com.faceunity.nama;

import android.content.Context;
import android.hardware.Camera;

import com.faceunity.nama.module.BodySlimModule;
import com.faceunity.nama.module.FaceBeautyModule;
import com.faceunity.nama.module.IEffectModule;
import com.faceunity.nama.module.IFaceBeautyModule;
import com.faceunity.nama.module.IMakeupModule;
import com.faceunity.nama.module.IStickerModule;
import com.faceunity.nama.module.MakeupModule;
import com.faceunity.nama.module.StickerModule;
import com.faceunity.nama.utils.BundleUtils;
import com.faceunity.nama.utils.LogUtils;
import com.faceunity.wrapper.faceunity;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 基于 Nama SDK 封装，方便集成，使用步骤：
 * <p>
 * 1. FURenderer.Builder 构造器设置相应的参数
 * 2. 美颜、美妆、贴纸和美体模块化，使用时开关参数设置 true 即可
 * 3. GL 画布创建和销毁时，分别调用 onSurfaceCreated 和 onSurfaceDestroyed
 * 4. 相机朝向和设备方向变化时，分别调用 onCameraChanged 和 onDeviceOrientationChanged
 * 5. 处理图像时调用 onDrawFrame，针对不同数据类型，提供了纹理和 buffer 多种输入方案
 * </p>
 */
public class FURenderer implements IFURenderer, IModuleManager {
    private static final String TAG = "FURenderer";
    /**
     * 输入的 texture 类型，OES 或 2D
     */
    public static final int INPUT_EXTERNAL_OES_TEXTURE = faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE;
    public static final int INPUT_2D_TEXTURE = 0;

    /**
     * 输入的 buffer 格式，NV21、I420 或 RGBA
     */
    public static final int INPUT_FORMAT_NV21 = faceunity.FU_FORMAT_NV21_BUFFER;
    public static final int INPUT_FORMAT_I420 = faceunity.FU_FORMAT_I420_BUFFER;
    public static final int INPUT_FORMAT_RGBA = faceunity.FU_FORMAT_RGBA_BUFFER;

    /**
     * 算法检测类型
     */
    public static final int TRACK_TYPE_FACE = faceunity.FUAITYPE_FACEPROCESSOR;
    public static final int TRACK_TYPE_HUMAN = faceunity.FUAITYPE_HUMAN_PROCESSOR;

    /* 句柄数组下标，分别代表美颜、贴纸、美妆和美体 */
    private static final int ITEMS_ARRAY_FACE_BEAUTY = 0;
    private static final int ITEMS_ARRAY_STICKER = 1;
    private static final int ITEMS_ARRAY_MAKEUP = 2;
    private static final int ITEMS_ARRAY_BODY_SLIM = 3;
    /* 句柄数组长度 4 */
    private static final int ITEMS_ARRAY_LENGTH = 4;
    /* 存放美颜和贴纸句柄的数组 */
    private final int[] mItemsArray = new int[ITEMS_ARRAY_LENGTH];
    private final Context mContext;
    /* 递增的帧 ID */
    private int mFrameId = 0;
    /* 同时识别的最大人脸数，默认 4 */
    private int mMaxFaces = 4;
    /* 是否手动创建 EGLContext，默认不创建 */
    private boolean mIsCreateEglContext = false;
    /* 输入图像的纹理类型，默认 2D */
    private int mInputTextureType = INPUT_2D_TEXTURE;
    /* 输入图像的 buffer 类型，此项一般不用改 */
    private int mInputImageFormat = 0;
    /* 输入图像的方向，默认前置相机 270 */
    private int mInputImageOrientation = 270;
    /* 设备方向，默认竖屏 */
    private int mDeviceOrientation = 90;
    /* 人脸识别方向，默认 1，通过 createRotationMode 方法获得 */
    private int mRotationMode = faceunity.FU_ROTATION_MODE_90;
    /* 相机前后方向，默认前置相机  */
    private int mCameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
    /* 任务队列 */
    private final ArrayList<Runnable> mEventQueue = new ArrayList<>(16);
    /* 任务队列操作锁 */
    private final Object mLock = new Object();
    /* GL 线程 ID */
    private long mGlThreadId;
    /* 是否已经全局初始化，确保只初始化一次 */
    private static boolean sIsInited;

    /* 特效模块，美颜、贴纸、美妆和美体。默认只启用美颜 */
    private FaceBeautyModule mFaceBeautyModule;
    private StickerModule mStickerModule;
    private MakeupModule mMakeupModule;
    private BodySlimModule mBodySlimModule;
    private boolean mIsCreatedSticker;
    private boolean mIsCreatedMakeup;
    private boolean mIsCreatedBodySlim;

    /**
     * 初始化系统环境，加载底层数据，并进行网络鉴权。
     * 应用使用期间只需要初始化一次，无需释放数据。
     * 不需要 GL 环境，但必须在SDK其他接口前调用，否则会引起应用崩溃。
     *
     * @param context
     */
    public static void setup(Context context) {
        if (sIsInited) {
            return;
        }
        // {trace:0, debug:1, info:2, warn:3, error:4, critical:4, off:6}
        int logLevel = 6;
        faceunity.fuSetLogLevel(logLevel);
        LogUtils.setLogLevel(LogUtils.DEBUG);
        // 获取 Nama SDK 版本信息
        LogUtils.info(TAG, "fu sdk version %s", faceunity.fuGetVersion());
        // v3 不再使用，第一个参数传空字节数组即可
        int isSetup = faceunity.fuSetup(new byte[0], authpack.A());
        sIsInited = isInit();
        // 加载人脸检测算法数据模型
        boolean isLoaded = BundleUtils.loadAiModel(context, "model/ai_face_processor.bundle", faceunity.FUAITYPE_FACEPROCESSOR);
        LogUtils.info(TAG, "fuSetup. isSetup: %s, isLibInit: %s, isLoadFaceProcessor: %s", isSetup == 0 ? "no" : "yes",
                sIsInited ? "yes" : "no", isLoaded ? "yes" : "no");
    }

    /**
     * 销毁 SDK，释放资源。如需再次使用，需要调用 setup。
     */
    public static void destroy() {
        BundleUtils.releaseAiModel(faceunity.FUAITYPE_FACEPROCESSOR);
        if (sIsInited) {
            faceunity.fuDestroyLibData();
            sIsInited = isInit();
            LogUtils.debug(TAG, "destroy. isLibraryInit: %s", sIsInited ? "yes" : "no");
        }
    }

    /**
     * SDK 是否初始化
     *
     * @return
     */
    public static boolean isInit() {
        return faceunity.fuIsLibraryInit() == 1;
    }

    /**
     * 获取 Nama SDK 版本号，例如 7_0_0_phy_8b882f6_91a980f
     *
     * @return version
     */
    public static String getVersion() {
        return faceunity.fuGetVersion();
    }

    private FURenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated() {
        LogUtils.info(TAG, "onSurfaceCreated");
        mGlThreadId = Thread.currentThread().getId();
        mFrameId = 0;
        synchronized (mLock) {
            mEventQueue.clear();
        }
        resetTrackStatus();
        /*
         * 创建OpenGL环境，适用于没有 OpenGL 环境时。
         * 如果调用了fuCreateEGLContext，销毁时需要调用fuReleaseEGLContext
         */
        if (mIsCreateEglContext) {
            faceunity.fuCreateEGLContext();
        }
        mRotationMode = createRotationMode();
        if (mFaceBeautyModule != null) {
            mFaceBeautyModule.create(mContext, new IEffectModule.ModuleCallback() {
                @Override
                public void onCreateFinish(int itemHandle) {
                    mItemsArray[ITEMS_ARRAY_FACE_BEAUTY] = itemHandle;
                }
            });
            mFaceBeautyModule.setMaxFaces(mMaxFaces);
            mFaceBeautyModule.setRotationMode(mRotationMode);
        }
        if (mIsCreatedSticker) {
            createStickerModule();
        }
        if (mIsCreatedMakeup) {
            createMakeupModule();
        }
        if (mIsCreatedBodySlim) {
            createBodySlimModule();
        }
    }

    @Override
    public void onSurfaceDestroyed() {
        LogUtils.info(TAG, "onSurfaceDestroyed");
        mGlThreadId = 0;
        mFrameId = 0;
        synchronized (mLock) {
            mEventQueue.clear();
        }
        if (mFaceBeautyModule != null) {
            mFaceBeautyModule.destroy();
        }
        if (mStickerModule != null) {
            mStickerModule.destroy();
        }
        if (mMakeupModule != null) {
            mMakeupModule.destroy();
        }
        if (mBodySlimModule != null) {
            mBodySlimModule.destroy();
        }
        for (int item : mItemsArray) {
            if (item > 0) {
                faceunity.fuDestroyItem(item);
            }
        }
        Arrays.fill(mItemsArray, 0);
        faceunity.fuOnDeviceLost();
        faceunity.fuDone();
        if (mIsCreateEglContext) {
            faceunity.fuReleaseEGLContext();
        }
    }

    @Override
    public int onDrawFrameSingleInput(int tex, int w, int h) {
        if (tex <= 0 || w <= 0 || h <= 0) {
            LogUtils.error(TAG, "onDrawFrame data is invalid");
            return 0;
        }
        prepareDrawFrame();
        int flags = createFlags();
        if (mIsRunBenchmark) {
            mCallStartTime = System.nanoTime();
        }
        int fuTex = faceunity.fuRenderToTexture(tex, w, h, mFrameId++, mItemsArray, flags);
        if (mIsRunBenchmark) {
            mSumRenderTime += System.nanoTime() - mCallStartTime;
        }
        return fuTex;
    }

    @Override
    public int onDrawFrameSingleInput(byte[] img, int w, int h, int format) {
        if (img == null || w <= 0 || h <= 0) {
            LogUtils.error(TAG, "onDrawFrame data is invalid");
            return 0;
        }
        prepareDrawFrame();
        int flags = createFlags();
        flags ^= mInputTextureType;
        if (mIsRunBenchmark) {
            mCallStartTime = System.nanoTime();
        }
        int fuTex;
        switch (format) {
            case INPUT_FORMAT_I420:
                fuTex = faceunity.fuRenderToI420Image(img, w, h, mFrameId++, mItemsArray, flags);
                break;
            case INPUT_FORMAT_RGBA:
                fuTex = faceunity.fuRenderToRgbaImage(img, w, h, mFrameId++, mItemsArray, flags);
                break;
            case INPUT_FORMAT_NV21:
            default:
                fuTex = faceunity.fuRenderToNV21Image(img, w, h, mFrameId++, mItemsArray, flags);
                break;
        }
        if (mIsRunBenchmark) {
            mSumRenderTime += System.nanoTime() - mCallStartTime;
        }
        return fuTex;
    }


    @Override
    public int onDrawFrameSingleInput(byte[] img, int w, int h, int format, byte[] readBackImg, int readBackW, int readBackH) {
        if (img == null || w <= 0 || h <= 0 || readBackImg == null || readBackW <= 0 || readBackH <= 0) {
            LogUtils.error(TAG, "onDrawFrame data is invalid");
            return 0;
        }
        prepareDrawFrame();
        int flags = createFlags();
        flags ^= mInputTextureType;
        if (mIsRunBenchmark) {
            mCallStartTime = System.nanoTime();
        }
        int fuTex;
        switch (format) {
            case INPUT_FORMAT_I420:
                fuTex = faceunity.fuRenderToI420Image(img, w, h, mFrameId++, mItemsArray, flags,
                        readBackW, readBackH, readBackImg);
                break;
            case INPUT_FORMAT_RGBA:
                fuTex = faceunity.fuRenderToRgbaImage(img, w, h, mFrameId++, mItemsArray, flags,
                        readBackW, readBackH, readBackImg);
                break;
            case INPUT_FORMAT_NV21:
            default:
                fuTex = faceunity.fuRenderToNV21Image(img, w, h, mFrameId++, mItemsArray, flags,
                        readBackW, readBackH, readBackImg);
                break;
        }
        if (mIsRunBenchmark) {
            mSumRenderTime += System.nanoTime() - mCallStartTime;
        }
        return fuTex;
    }

    @Override
    public int onDrawFrameDualInput(byte[] img, int tex, int w, int h) {
        if (img == null || tex <= 0 || w <= 0 || h <= 0) {
            LogUtils.error(TAG, "onDrawFrame data is invalid");
            return 0;
        }
        prepareDrawFrame();
        int flags = createFlags();
        if (mIsRunBenchmark) {
            mCallStartTime = System.nanoTime();
        }
        int fuTex = faceunity.fuDualInputToTexture(img, tex, flags, w, h, mFrameId++, mItemsArray);
        if (mIsRunBenchmark) {
            mSumRenderTime += System.nanoTime() - mCallStartTime;
        }
        return fuTex;
    }

    @Override
    public int onDrawFrameDualInput(byte[] img, int tex, int w, int h, byte[] readBackImg, int readBackW, int readBackH) {
        if (img == null || tex <= 0 || w <= 0 || h <= 0 || readBackImg == null || readBackW <= 0 || readBackH <= 0) {
            LogUtils.error(TAG, "onDrawFrame data is invalid");
            return 0;
        }
        prepareDrawFrame();
        int flags = createFlags();
        if (mIsRunBenchmark) {
            mCallStartTime = System.nanoTime();
        }
        int fuTex = faceunity.fuDualInputToTexture(img, tex, flags, w, h, mFrameId++, mItemsArray,
                readBackW, readBackH, readBackImg);
        if (mIsRunBenchmark) {
            mSumRenderTime += System.nanoTime() - mCallStartTime;
        }
        return fuTex;
    }

    @Override
    public void queueEvent(Runnable r) {
        if (r == null) {
            return;
        }
        if (mGlThreadId == Thread.currentThread().getId()) {
            r.run();
        } else {
            synchronized (mLock) {
                mEventQueue.add(r);
            }
        }
    }

    @Override
    public void onDeviceOrientationChanged(int deviceOrientation) {
        if (mDeviceOrientation == deviceOrientation) {
            return;
        }
        LogUtils.debug(TAG, "onDeviceOrientationChanged() deviceOrientation: %d", deviceOrientation);
        mDeviceOrientation = deviceOrientation;
        callWhenDeviceChanged();
    }

    @Override
    public void onCameraChanged(int cameraType, int cameraOrientation) {
        if (mCameraType == cameraType && mInputImageOrientation == cameraOrientation) {
            return;
        }
        LogUtils.debug(TAG, "onCameraChanged() cameraType: %d, cameraOrientation: %d", cameraType, cameraOrientation);
        mCameraType = cameraType;
        mInputImageOrientation = cameraOrientation;
        callWhenDeviceChanged();
    }

    @Override
    public IFaceBeautyModule getFaceBeautyModule() {
        return mFaceBeautyModule;
    }

    @Override
    public void createStickerModule() {
        LogUtils.info(TAG, "createStickerModule: ");
        if (mStickerModule == null) {
            return;
        }
        mIsCreatedSticker = true;
        mStickerModule.create(mContext, new IEffectModule.ModuleCallback() {
            @Override
            public void onCreateFinish(int itemHandle) {
                int oldItem = mItemsArray[ITEMS_ARRAY_STICKER];
                if (oldItem > 0) {
                    faceunity.fuDestroyItem(oldItem);
                }
                mStickerModule.setRotationMode(mRotationMode);
                double isAndroid = mInputTextureType == INPUT_EXTERNAL_OES_TEXTURE ? 1.0 : 0.0;
                // 历史遗留参数，和具体道具有关
                mStickerModule.setItemParam("isAndroid", isAndroid);
                mItemsArray[ITEMS_ARRAY_STICKER] = itemHandle;
            }
        });
    }

    @Override
    public IStickerModule getStickerModule() {
        return mStickerModule;
    }

    @Override
    public void destroyStickerModule() {
        LogUtils.info(TAG, "destroyStickerModule: ");
        mIsCreatedSticker = false;
        if (mStickerModule != null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mStickerModule.destroy();
                    mItemsArray[ITEMS_ARRAY_STICKER] = 0;
                }
            });
        }
    }

    @Override
    public void createMakeupModule() {
        LogUtils.info(TAG, "createMakeupModule: ");
        if (mMakeupModule == null) {
            return;
        }
        mIsCreatedMakeup = true;
        mMakeupModule.create(mContext, new IEffectModule.ModuleCallback() {
            @Override
            public void onCreateFinish(int itemHandle) {
                mItemsArray[ITEMS_ARRAY_MAKEUP] = itemHandle;
            }
        });
    }

    @Override
    public IMakeupModule getMakeupModule() {
        return mMakeupModule;
    }

    @Override
    public void destroyMakeupModule() {
        LogUtils.info(TAG, "destroyMakeupModule: ");
        mIsCreatedMakeup = false;
        if (mMakeupModule != null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mMakeupModule.destroy();
                    mItemsArray[ITEMS_ARRAY_MAKEUP] = 0;
                }
            });
        }
    }

    @Override
    public void createBodySlimModule() {
        LogUtils.info(TAG, "createBodySlimModule: ");
        if (mBodySlimModule == null) {
            return;
        }
        mIsCreatedBodySlim = true;
        mBodySlimModule.create(mContext, new IEffectModule.ModuleCallback() {
            @Override
            public void onCreateFinish(int itemHandle) {
                mItemsArray[ITEMS_ARRAY_BODY_SLIM] = itemHandle;
                mBodySlimModule.setRotationMode(mRotationMode);
                resetTrackStatus();
            }
        });
    }

    @Override
    public BodySlimModule getBodySlimModule() {
        return mBodySlimModule;
    }

    @Override
    public void destroyBodySlimModule() {
        LogUtils.info(TAG, "destroyBodySlimModule: ");
        mIsCreatedBodySlim = false;
        if (mBodySlimModule != null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mBodySlimModule.destroy();
                    mItemsArray[ITEMS_ARRAY_BODY_SLIM] = 0;
                    resetTrackStatus();
                }
            });
        }
    }


    private void prepareDrawFrame() {
        // 计算 FPS 和渲染时长
        benchmarkFPS();
        // 获取人脸是否识别
        int trackFace = faceunity.fuIsTracking();
        // 获取人体是否识别
        int trackHumans = faceunity.fuHumanProcessorGetNumResults();
        if (mItemsArray[ITEMS_ARRAY_BODY_SLIM] > 0) {
            if (mTrackHumanStatus != trackHumans) {
                mTrackHumanStatus = trackHumans;
                if (mOnTrackStatusChangedListener != null) {
                    mOnTrackStatusChangedListener.onTrackStatusChanged(TRACK_TYPE_HUMAN, trackHumans);
                }
            }
        } else {
            if (mTrackFaceStatus != trackFace) {
                mTrackFaceStatus = trackFace;
                if (mOnTrackStatusChangedListener != null) {
                    mOnTrackStatusChangedListener.onTrackStatusChanged(TRACK_TYPE_FACE, trackFace);
                }
            }
        }
        // 获取错误信息，并调用回调接口
        int errorCode = faceunity.fuGetSystemError();
        if (errorCode != 0) {
            String message = faceunity.fuGetSystemErrorString(errorCode);
            LogUtils.error(TAG, "fuGetSystemError. code: %d, message: %s", errorCode, message);
            if (mOnSystemErrorListener != null) {
                mOnSystemErrorListener.onSystemError(errorCode, message);
            }
        }
        // 执行任务队列中的任务
        synchronized (mLock) {
            while (!mEventQueue.isEmpty()) {
                mEventQueue.remove(0).run();
            }
        }
        // 执行各个模块的任务
        if (mFaceBeautyModule != null) {
            mFaceBeautyModule.executeEvent();
        }
        if (mStickerModule != null) {
            mStickerModule.executeEvent();
        }
        if (mMakeupModule != null) {
            mMakeupModule.executeEvent();
        }
        if (mBodySlimModule != null) {
            mBodySlimModule.executeEvent();
        }
    }

    private void callWhenDeviceChanged() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                int rotationMode = createRotationMode();
                LogUtils.debug(TAG, "callWhenDeviceChanged() rotationMode: %d", rotationMode);
                if (mFaceBeautyModule != null) {
                    mFaceBeautyModule.setRotationMode(rotationMode);
                }
                if (mMakeupModule != null) {
                    mMakeupModule.setRotationMode(rotationMode);
                }
                if (mStickerModule != null) {
                    mStickerModule.setRotationMode(rotationMode);
                }
                if (mBodySlimModule != null) {
                    mBodySlimModule.setRotationMode(rotationMode);
                }
                mRotationMode = rotationMode;
                faceunity.fuOnCameraChange();
                faceunity.fuHumanProcessorReset();
            }
        });
    }

    private int createRotationMode() {
        if (mInputTextureType == FURenderer.INPUT_2D_TEXTURE) {
            return faceunity.FU_ROTATION_MODE_0;
        }

        int rotMode = faceunity.FU_ROTATION_MODE_0;
        int deviceOrientation = mDeviceOrientation;
        int cameraType = mCameraType;
        int inputImageOrientation = mInputImageOrientation;
        if (inputImageOrientation == 270) {
            if (cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotMode = deviceOrientation / 90;
            } else {
                if (deviceOrientation == 90) {
                    rotMode = faceunity.FU_ROTATION_MODE_270;
                } else if (deviceOrientation == 270) {
                    rotMode = faceunity.FU_ROTATION_MODE_90;
                } else {
                    rotMode = deviceOrientation / 90;
                }
            }
        } else if (inputImageOrientation == 90) {
            if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                if (deviceOrientation == 90) {
                    rotMode = faceunity.FU_ROTATION_MODE_270;
                } else if (deviceOrientation == 270) {
                    rotMode = faceunity.FU_ROTATION_MODE_90;
                } else {
                    rotMode = deviceOrientation / 90;
                }
            } else {
                if (deviceOrientation == 0) {
                    rotMode = faceunity.FU_ROTATION_MODE_180;
                } else if (deviceOrientation == 90) {
                    rotMode = faceunity.FU_ROTATION_MODE_270;
                } else if (deviceOrientation == 180) {
                    rotMode = faceunity.FU_ROTATION_MODE_0;
                } else {
                    rotMode = faceunity.FU_ROTATION_MODE_90;
                }
            }
        }
        return rotMode;
    }

    private int createFlags() {
        int inputTextureType = mInputTextureType;
        int flags = inputTextureType | mInputImageFormat;
        if (inputTextureType == INPUT_2D_TEXTURE || mCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT) {
            flags |= faceunity.FU_ADM_FLAG_FLIP_X;
        }
        return flags;
    }

    //-----------------------------人脸识别回调相关定义-----------------------------------

    private int mTrackFaceStatus = -1;
    private int mTrackHumanStatus = -1;

    public interface OnTrackStatusChangedListener {
        /**
         * 识别到的人脸或人体数量发生变化
         *
         * @param type   类型
         * @param status 数量
         */
        void onTrackStatusChanged(int type, int status);
    }

    private OnTrackStatusChangedListener mOnTrackStatusChangedListener;

    private void resetTrackStatus() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mTrackFaceStatus = -1;
                mTrackHumanStatus = -1;
            }
        });
    }

    //-------------------------错误信息回调相关定义---------------------------------

    public interface OnSystemErrorListener {
        /**
         * SDK 发生错误时调用
         *
         * @param code    错误码
         * @param message 错误消息
         */
        void onSystemError(int code, String message);
    }

    private OnSystemErrorListener mOnSystemErrorListener;

    //------------------------------FPS 渲染时长回调相关定义------------------------------------

    private static final int NANO_IN_ONE_MILLI_SECOND = 1_000_000;
    private static final int NANO_IN_ONE_SECOND = 1_000_000_000;
    private static final int FRAME_COUNT = 10;
    private boolean mIsRunBenchmark = false;
    private int mCurrentFrameCount;
    private long mLastFrameTimestamp;
    private long mSumRenderTime;
    private long mCallStartTime;
    private OnDebugListener mOnDebugListener;

    public interface OnDebugListener {
        /**
         * 统计每 10 帧的平均数据，FPS 和渲染函数调用时间
         *
         * @param fps      FPS
         * @param callTime 渲染函数调用时间
         */
        void onFpsChanged(double fps, double callTime);
    }

    private void benchmarkFPS() {
        if (!mIsRunBenchmark) {
            return;
        }
        if (++mCurrentFrameCount == FRAME_COUNT) {
            long tmp = System.nanoTime();
            double fps = (double) NANO_IN_ONE_SECOND / ((double) (tmp - mLastFrameTimestamp) / FRAME_COUNT);
            double renderTime = (double) mSumRenderTime / FRAME_COUNT / NANO_IN_ONE_MILLI_SECOND;
            mLastFrameTimestamp = tmp;
            mSumRenderTime = 0;
            mCurrentFrameCount = 0;

            if (mOnDebugListener != null) {
                mOnDebugListener.onFpsChanged(fps, renderTime);
            }
        }
    }

    //--------------------------------------Builder----------------------------------------

    /**
     * FURenderer Builder
     */
    public static class Builder {
        private Context context;
        private boolean isCreateEglContext;
        private int maxFaces = 4;
        private int deviceOrientation = 90;
        private int inputTextureType = INPUT_2D_TEXTURE;
        private int inputImageFormat = 0;
        private int inputImageOrientation = 270;
        private int cameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
        private boolean isRunBenchmark;
        private boolean isCreateFaceBeauty = true;
        private boolean isCreateSticker = true;
        private boolean isCreateMakeup = true;
        private boolean isCreateBodySlim = true;
        private OnDebugListener onDebugListener;
        private OnTrackStatusChangedListener onTrackStatusChangedListener;
        private OnSystemErrorListener onSystemErrorListener;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        /**
         * 是否手动创建 EGLContext
         *
         * @param isCreateEGLContext
         * @return
         */

        public Builder setCreateEglContext(boolean isCreateEGLContext) {
            this.isCreateEglContext = isCreateEGLContext;
            return this;
        }

        /**
         * 同时识别的最大人脸数，默认 4 人，最大 8 人
         *
         * @param maxFaces
         * @return
         */
        public Builder setMaxFaces(int maxFaces) {
            this.maxFaces = maxFaces;
            return this;
        }

        /**
         * 设备方向
         *
         * @param deviceOrientation
         * @return
         */
        public Builder setDeviceOrientation(int deviceOrientation) {
            this.deviceOrientation = deviceOrientation;
            return this;
        }

        /**
         * 输入图像的纹理类型
         *
         * @param inputTextureType OES 或者 2D
         * @return
         */
        public Builder setInputTextureType(int inputTextureType) {
            this.inputTextureType = inputTextureType;
            return this;
        }

        /**
         * 输入图像的 buffer 类型，一般不用修改此项
         *
         * @param inputImageFormat
         * @return
         */
        public Builder setInputImageFormat(int inputImageFormat) {
            this.inputImageFormat = inputImageFormat;
            return this;
        }

        /**
         * 输入图像的方向
         *
         * @param inputImageOrientation
         * @return
         */
        public Builder setInputImageOrientation(int inputImageOrientation) {
            this.inputImageOrientation = inputImageOrientation;
            return this;
        }

        /**
         * 相机前后方向
         *
         * @param cameraType
         * @return
         */
        public Builder setCameraType(int cameraType) {
            this.cameraType = cameraType;
            return this;
        }

        /**
         * 美颜模块
         *
         * @param createFaceBeauty
         * @return
         */
        public Builder setCreateFaceBeauty(boolean createFaceBeauty) {
            this.isCreateFaceBeauty = createFaceBeauty;
            return this;
        }

        /**
         * 贴纸模块
         *
         * @param createSticker
         * @return
         */
        public Builder setCreateSticker(boolean createSticker) {
            this.isCreateSticker = createSticker;
            return this;
        }

        /**
         * 美妆模块
         *
         * @param createMakeup
         * @return
         */
        public Builder setCreateMakeup(boolean createMakeup) {
            this.isCreateMakeup = createMakeup;
            return this;
        }

        /**
         * 美体模块
         *
         * @param createBodySlim
         * @return
         */
        public Builder setCreateBodySlim(boolean createBodySlim) {
            this.isCreateBodySlim = createBodySlim;
            return this;
        }

        /**
         * 是否需要 benchmark 统计数据
         *
         * @param isRunBenchmark
         * @return
         */
        public Builder setRunBenchmark(boolean isRunBenchmark) {
            this.isRunBenchmark = isRunBenchmark;
            return this;
        }

        /**
         * FPS 和函数时长数据回调
         *
         * @param onDebugListener
         * @return
         */
        public Builder setOnDebugListener(OnDebugListener onDebugListener) {
            this.onDebugListener = onDebugListener;
            return this;
        }

        /**
         * 人脸识别状态回调
         *
         * @param onTrackStatusChangedListener
         * @return
         */
        public Builder setOnTrackStatusChangedListener(OnTrackStatusChangedListener onTrackStatusChangedListener) {
            this.onTrackStatusChangedListener = onTrackStatusChangedListener;
            return this;
        }

        /**
         * SDK 错误信息回调
         *
         * @param onSystemErrorListener
         * @return
         */
        public Builder setOnSystemErrorListener(OnSystemErrorListener onSystemErrorListener) {
            this.onSystemErrorListener = onSystemErrorListener;
            return this;
        }

        public FURenderer build() {
            FURenderer fuRenderer = new FURenderer(context);
            fuRenderer.mIsCreateEglContext = isCreateEglContext;
            fuRenderer.mMaxFaces = maxFaces;
            fuRenderer.mDeviceOrientation = deviceOrientation;
            fuRenderer.mInputTextureType = inputTextureType;
            fuRenderer.mInputImageFormat = inputImageFormat;
            fuRenderer.mInputImageOrientation = inputImageOrientation;
            fuRenderer.mCameraType = cameraType;
            fuRenderer.mFaceBeautyModule = isCreateFaceBeauty ? new FaceBeautyModule() : null;
            fuRenderer.mStickerModule = isCreateSticker ? new StickerModule() : null;
            fuRenderer.mMakeupModule = isCreateMakeup ? new MakeupModule() : null;
            fuRenderer.mBodySlimModule = isCreateBodySlim ? new BodySlimModule() : null;
            fuRenderer.mIsRunBenchmark = isRunBenchmark;
            fuRenderer.mOnDebugListener = onDebugListener;
            fuRenderer.mOnTrackStatusChangedListener = onTrackStatusChangedListener;
            fuRenderer.mOnSystemErrorListener = onSystemErrorListener;

            LogUtils.debug(TAG, "FURenderer fields. isCreateEglContext: " + isCreateEglContext + ", maxFaces: "
                    + maxFaces + ", inputTextureType: " + inputTextureType + ", inputImageFormat: "
                    + inputImageFormat + ", inputImageOrientation: " + inputImageOrientation
                    + ", deviceOrientation: " + deviceOrientation + ", cameraType: " + cameraType
                    + ", isRunBenchmark: " + isRunBenchmark + ", isCreateSticker: " + isCreateSticker
                    + ", isCreateMakeup: " + isCreateMakeup + ", isCreateBodySlim: " + isCreateBodySlim);
            return fuRenderer;
        }
    }

}