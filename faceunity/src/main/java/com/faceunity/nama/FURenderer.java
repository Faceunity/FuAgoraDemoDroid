package com.faceunity.nama;

import android.content.Context;

import com.faceunity.core.callback.OperateCallback;
import com.faceunity.core.entity.FURenderInputData;
import com.faceunity.core.entity.FURenderOutputData;
import com.faceunity.core.enumeration.CameraFacingEnum;
import com.faceunity.core.enumeration.FUAIProcessorEnum;
import com.faceunity.core.enumeration.FUAITypeEnum;
import com.faceunity.core.enumeration.FUTransformMatrixEnum;
import com.faceunity.core.faceunity.AICommonData;
import com.faceunity.core.faceunity.FUAIKit;
import com.faceunity.core.faceunity.FURenderConfig;
import com.faceunity.core.faceunity.FURenderKit;
import com.faceunity.core.faceunity.FURenderManager;
import com.faceunity.core.utils.FULogger;
import com.faceunity.nama.listener.FURendererListener;
import com.faceunity.nama.utils.FuDeviceUtils;

import java.io.File;


/**
 * DESC：
 * Created on 2021/4/26
 */
public class FURenderer extends IFURenderer {


    public volatile static FURenderer INSTANCE;

    public static FURenderer getInstance() {
        if (INSTANCE == null) {
            synchronized (FURenderer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FURenderer();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 状态回调监听
     */
    private FURendererListener mFURendererListener;


    /* 特效FURenderKit*/
    private FURenderKit mFURenderKit = FURenderKit.getInstance();
    /* 特效FURenderKit*/
    private FUAIKit mFUAIKit = FUAIKit.getInstance();

    /* AI道具*/
    private String BUNDLE_AI_FACE = "model" + File.separator + "ai_face_processor.bundle";
    private String BUNDLE_AI_HUMAN = "model" + File.separator + "ai_human_processor.bundle";

    /*检测类型*/
    private FUAIProcessorEnum aIProcess = FUAIProcessorEnum.FACE_PROCESSOR;
    /*检测标识*/
    private int aIProcessTrackStatus = -1;

    /**
     * @return version
     */
    public String getVersion() {
        return mFURenderKit.getVersion();
    }

    /**
     * 初始化鉴权
     *
     * @param context
     */
    @Override
    public void setup(Context context) {
        // log等级不要动，会影响性能
        FURenderManager.setKitDebug(FULogger.LogLevel.ERROR);
        FURenderManager.setCoreDebug(FULogger.LogLevel.OFF);
        FURenderManager.registerFURender(context, authpack.A(), new OperateCallback() {
            @Override
            public void onSuccess(int i, String s) {
                if (i == FURenderConfig.OPERATE_SUCCESS_AUTH) {

                    /**
                     * 设置算法人脸模块的加载策略，是否关闭一些模块的加载。可提高加载速度。
                     * FUAIFACE_ENABLE_ALL = 0,
                     *   FUAIFACE_DISABLE_FACE_OCCU = 1 << 0, //关闭全脸遮挡分割
                     *   FUAIFACE_DISABLE_SKIN_SEG = 1 << 1,  //关闭美白皮肤分割
                     *   FUAIFACE_DISABLE_DEL_SPOT = 1 << 2,  //关闭去斑痘
                     *   FUAIFACE_DISABLE_ARMESHV2 = 1 << 3,  //关闭ARMESHV2
                     *   FUAIFACE_DISABLE_RACE = 1 << 4,  //关闭人种分类
                     *   AFUAIFACE_DISABLE_LANDMARK_HP_OCCU = 1 << 5,  //关闭人脸点位
                     */
                    switch (FUConfig.DEVICE_LEVEL) {
                        case FuDeviceUtils.DEVICE_LEVEL_MINUS_ONE:
                        case FuDeviceUtils.DEVICE_LEVEL_ONE:
                            FUAIKit.getInstance().fuSetFaceAlgorithmConfig(AICommonData.FUAIFACE_DISABLE_FACE_OCCU | AICommonData.FUAIFACE_DISABLE_SKIN_SEG | AICommonData.FUAIFACE_DISABLE_DEL_SPOT | AICommonData.FUAIFACE_DISABLE_ARMESHV2 | AICommonData.FUAIFACE_DISABLE_RACE | AICommonData.FUAIFACE_DISABLE_LANDMARK_HP_OCCU);
                            break;
                        case FuDeviceUtils.DEVICE_LEVEL_TWO:
                            FUAIKit.getInstance().fuSetFaceAlgorithmConfig(AICommonData.FUAIFACE_DISABLE_SKIN_SEG | AICommonData.FUAIFACE_DISABLE_DEL_SPOT | AICommonData.FUAIFACE_DISABLE_ARMESHV2 | AICommonData.FUAIFACE_DISABLE_RACE);
                            break;
                        case FuDeviceUtils.DEVICE_LEVEL_THREE:
                            FUAIKit.getInstance().fuSetFaceAlgorithmConfig(AICommonData.FUAIFACE_DISABLE_SKIN_SEG);
                            break;
                        case FuDeviceUtils.DEVICE_LEVEL_FOUR:
                            FUAIKit.getInstance().fuSetFaceAlgorithmConfig(AICommonData.FUAIFACE_ENABLE_ALL);
                            break;
                    }

                    mFUAIKit.loadAIProcessor(BUNDLE_AI_FACE, FUAITypeEnum.FUAITYPE_FACEPROCESSOR);
                    mFUAIKit.loadAIProcessor(BUNDLE_AI_HUMAN, FUAITypeEnum.FUAITYPE_HUMAN_PROCESSOR);
                    if (FUConfig.DEVICE_LEVEL <= FuDeviceUtils.DEVICE_LEVEL_ONE) {
                        mFURenderKit.setUseTexAsync(true);
                    }
                }
            }

            @Override
            public void onFail(int i, String s) {
            }
        });
    }

    /**
     * 开启合成状态
     */
    @Override
    public void bindListener(FURendererListener mFURendererListener) {
        this.mFURendererListener = mFURendererListener;
    }


    /**
     * 双输入接口，输入 buffer 和 texture，必须在具有 GL 环境的线程调用
     * 由于省去数据拷贝，性能相对最优，优先推荐使用。
     * 缺点是无法保证 buffer 和纹理对齐，可能出现点位和效果对不上的情况。
     *
     * @param img    NV21 buffer
     * @param texId  纹理 ID
     * @param width  宽
     * @param height 高
     * @return
     */
    @Override
    public int onDrawFrameDualInput(byte[] img, int texId, int width, int height) {
        prepareDrawFrame();
        FURenderInputData inputData = new FURenderInputData(width, height);
        /*注释掉Buffer配置，启用单纹理模式，防止Buffer跟纹理存在不对齐造成，美妆偏移*/
        //inputData.setImageBuffer(new FURenderInputData.FUImageBuffer(inputBufferType, img));
        inputData.setTexture(new FURenderInputData.FUTexture(inputTextureType, texId));
        FURenderInputData.FURenderConfig config = inputData.getRenderConfig();
        config.setExternalInputType(externalInputType);
        config.setInputOrientation(inputOrientation);
        config.setDeviceOrientation(deviceOrientation);
        config.setInputBufferMatrix(inputBufferMatrix);
        config.setInputTextureMatrix(inputTextureMatrix);
        config.setCameraFacing(cameraFacing);
        config.setOutputMatrix(outputMatrix);
        mCallStartTime = System.nanoTime();
        FURenderOutputData outputData = mFURenderKit.renderWithInput(inputData);
        mSumCallTime += System.nanoTime() - mCallStartTime;
        if (outputData.getTexture() != null && outputData.getTexture().getTexId() > 0) {
            return outputData.getTexture().getTexId();
        }
        return texId;

    }


    /**
     * 释放资源
     */
    @Override
    public void release() {
        mFURenderKit.release();
        aIProcessTrackStatus = -1;
        mFURendererListener = null;
    }


    /**
     * 渲染前置执行
     *
     * @return
     */
    private void prepareDrawFrame() {
        benchmarkFPS();
        // AI检测
        trackStatus();
    }

    //region AI识别

    /**
     * 设置输入数据朝向
     *
     * @param inputOrientation
     * @param isFront
     */
    public void setInputOrientation(int inputOrientation, boolean isFront) {
        setInputOrientation(inputOrientation);
        setCameraFacing(isFront ? CameraFacingEnum.CAMERA_FRONT : CameraFacingEnum.CAMERA_BACK);
        if (isFront) {
            setInputBufferMatrix(FUTransformMatrixEnum.CCROT90_FLIPHORIZONTAL);
            setInputTextureMatrix(FUTransformMatrixEnum.CCROT90_FLIPHORIZONTAL);
            setOutputMatrix(FUTransformMatrixEnum.CCROT270);
        } else {
            setInputBufferMatrix(FUTransformMatrixEnum.CCROT270);
            setInputTextureMatrix(FUTransformMatrixEnum.CCROT270);
            setOutputMatrix(FUTransformMatrixEnum.CCROT90_FLIPVERTICAL);
        }
    }


    /**
     * 设置检测类型
     *
     * @param type
     */
    @Override
    public void setAIProcessTrackType(FUAIProcessorEnum type) {
        aIProcess = type;
        aIProcessTrackStatus = -1;
    }

    /**
     * 设置FPS检测
     *
     * @param enable
     */
    @Override
    public void setMarkFPSEnable(boolean enable) {
        mIsRunBenchmark = enable;
    }


    /**
     * AI识别数目检测
     */
    private void trackStatus() {
        int trackCount;
        if (aIProcess == FUAIProcessorEnum.HAND_GESTURE_PROCESSOR) {
            trackCount = mFURenderKit.getFUAIController().handProcessorGetNumResults();
        } else if (aIProcess == FUAIProcessorEnum.HUMAN_PROCESSOR) {
            trackCount = mFURenderKit.getFUAIController().humanProcessorGetNumResults();
        } else {
            trackCount = mFURenderKit.getFUAIController().isTracking();
        }
        if (trackCount != aIProcessTrackStatus) {
            aIProcessTrackStatus = trackCount;
        } else {
            return;
        }
        if (mFURendererListener != null) {
            mFURendererListener.onTrackStatusChanged(aIProcess, trackCount);
        }
    }
    //endregion AI识别

    //------------------------------FPS 渲染时长回调相关定义------------------------------------

    private static final int NANO_IN_ONE_MILLI_SECOND = 1_000_000;
    private static final int NANO_IN_ONE_SECOND = 1_000_000_000;
    private static final int FRAME_COUNT = 20;
    private boolean mIsRunBenchmark = false;
    private int mCurrentFrameCount;
    private long mLastFrameTimestamp;
    private long mSumCallTime;
    private long mCallStartTime;

    private void benchmarkFPS() {
        if (!mIsRunBenchmark) {
            return;
        }
        if (++mCurrentFrameCount == FRAME_COUNT) {
            long tmp = System.nanoTime();
            double fps = (double) NANO_IN_ONE_SECOND / ((double) (tmp - mLastFrameTimestamp) / FRAME_COUNT);
            double renderTime = (double) mSumCallTime / FRAME_COUNT / NANO_IN_ONE_MILLI_SECOND;
            mLastFrameTimestamp = tmp;
            mSumCallTime = 0;
            mCurrentFrameCount = 0;

            if (mFURendererListener != null) {
                mFURendererListener.onFpsChanged(fps, renderTime);
            }
        }
    }


}
