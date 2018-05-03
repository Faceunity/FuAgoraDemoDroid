package com.faceunity.fulivenativedemo;

import android.content.Context;
import android.content.res.AssetManager;

import com.faceunity.beautycontrolview.OnFaceUnityControlListener;
import com.faceunity.beautycontrolview.entity.Effect;
import com.faceunity.beautycontrolview.entity.Filter;

/**
 * Created by tujh on 2017/8/25.
 */

public class FURenderer implements OnFaceUnityControlListener {

    static {
        System.loadLibrary("faceunity-native");
    }

    public FURenderer() {
        resetStatus();
    }

    private AssetManager mAssetManager;

    public native static void initFURenderer(AssetManager manager);

    /**
     * 需要GL环境，初始化了faceunity相关的数据。
     *
     * @param context
     */
    public void onSurfaceCreated(Context context) {
        mAssetManager = context.getAssets();
        onSurfaceCreated(mAssetManager);
    }

    /**
     * 需要GL环境，初始化了faceunity相关的数据。
     *
     * @param manager 底层需要AssetManager用于从assets资源文件夹中读取v3.mp3、face_beautification.mp3以及道具bundle
     */
    private native void onSurfaceCreated(AssetManager manager);

    /**
     * 需要GL环境,应在SurfaceView大小改变时被调用
     *
     * @param w
     * @param h
     */
    public native void onSurfaceChanged(int w, int h);

    /**
     * 需要GL环境，接收每帧图像纹理与byte[]数据，绘制画面
     *
     * @param img       图像byte[]数据
     * @param textureId 图像纹理
     * @param weight
     * @param height
     * @param mtx       画面方向旋转需要的矩阵
     */
    public native void onDrawFrame(byte[] img, int textureId, int weight, int height, float[] mtx);

    /**
     * 需要GL环境，SurfaceView销毁时被调用，释放faceunity相关的资源
     */
    public native void onSurfaceDestroy();

    /**
     * 需要GL环境，切换摄像头时重置一些native数据
     */
    public native void switchCamera(int cameraType, int inputImageOrientation);

    /**
     * 不需要GL环境，重置美颜、滤镜以及道具数据
     */
    public native void resetStatus();

    @Override
    public native void onMusicFilterTime(long time);

    @Override
    public void onEffectSelected(Effect effectItemName) {
        onEffectSelected(mAssetManager, effectItemName.path());
    }

    private native void onEffectSelected(AssetManager assetManager, String effectItemName);

    @Override
    public native void onFilterLevelSelected(float progress);

    @Override
    public void onFilterSelected(Filter filterName) {
        onFilterSelected(filterName.filterName());
    }

    private native void onFilterSelected(String filterName);

    @Override
    public native void onALLBlurLevelSelected(float isAll);

    @Override
    public native void onBeautySkinTypeSelected(float skinType);

    @Override
    public native void onBlurLevelSelected(float level);

    @Override
    public native void onColorLevelSelected(float progress);

    @Override
    public native void onRedLevelSelected(float progress);

    @Override
    public native void onBrightEyesSelected(float progress);

    @Override
    public native void onBeautyTeethSelected(float progress);

    @Override
    public native void onFaceShapeSelected(float faceShape);

    @Override
    public native void onEnlargeEyeSelected(float progress);

    @Override
    public native void onCheekThinSelected(float progress);

    @Override
    public native void onChinLevelSelected(float progress);

    @Override
    public native void onForeheadLevelSelected(float progress);

    @Override
    public native void onThinNoseLevelSelected(float progress);

    @Override
    public native void onMouthShapeSelected(float progress);

}
