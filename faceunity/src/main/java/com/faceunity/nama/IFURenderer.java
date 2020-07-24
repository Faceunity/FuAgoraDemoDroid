package com.faceunity.nama;

/**
 * @author Richie on 2020.07.08
 */
public interface IFURenderer {
    /**
     * 初始化 SDK，必须在具有 GL 环境的线程调用。
     * 如果没有 GL 环境，请使用 fuCreateEGLContext 创建 EGL Context。
     */
    void onSurfaceCreated();

    /**
     * 销毁 SDK，必须在具有 GL 环境的线程调用。
     * 如果已经调用 fuCreateEGLContext，请使用 fuReleaseEGLContext 释放 EGL Context。
     */
    void onSurfaceDestroyed();

    /**
     * 双输入接口，输入 buffer 和 texture，必须在具有 GL 环境的线程调用
     * 由于省去数据拷贝，性能相对最优，优先推荐使用。
     * 缺点是数据和纹理不保证对齐，可能出现效果滞后的情况。
     *
     * @param img NV21 数据
     * @param tex 纹理 ID
     * @param w   宽
     * @param h   高
     * @return
     */
    int onDrawFrameDualInput(byte[] img, int tex, int w, int h);

    /**
     * 双输入接口，输入 buffer 和 texture，支持数据回写到 buffer，必须在具有 GL 环境的线程调用
     *
     * @param img         NV21数据
     * @param tex         纹理 ID
     * @param w           宽
     * @param h           高
     * @param readBackImg 数据回写到的 buffer
     * @param readBackW   回写的宽
     * @param readBackH   回写的高
     * @return
     */
    int onDrawFrameDualInput(byte[] img, int tex, int w, int h, byte[] readBackImg, int readBackW, int readBackH);

    /**
     * 单 buffer 输入接口，必须在具有 GL 环境的线程调用
     *
     * @param img    图像
     * @param w      宽
     * @param h      高
     * @param format buffer 格式: nv21, i420, rgba
     * @return
     */
    int onDrawFrameSingleInput(byte[] img, int w, int h, int format);

    /**
     * 单 buffer 输入接口，支持数据回写，必须在具有 GL 环境的线程调用
     *
     * @param img         图像
     * @param w           宽
     * @param h           高
     * @param format      buffer 格式: nv21, i420, rgba
     * @param readBackImg 数据回写到的 buffer
     * @param readBackW   回写的宽
     * @param readBackH   回写的高
     * @return
     */
    int onDrawFrameSingleInput(byte[] img, int w, int h, int format, byte[] readBackImg, int readBackW, int readBackH);

    /**
     * 单 texture 输入接口，必须在具有 GL 环境的线程调用
     *
     * @param tex 纹理 ID
     * @param w   宽
     * @param h   高
     * @return
     */
    int onDrawFrameSingleInput(int tex, int w, int h);

    /**
     * 相机切换时调用
     *
     * @param cameraType        前后置相机 ID
     * @param cameraOrientation 相机方向
     */
    void onCameraChanged(int cameraType, int cameraOrientation);

    /**
     * 设备方向变化时调用
     *
     * @param deviceOrientation 设备方向
     */
    void onDeviceOrientationChanged(int deviceOrientation);

    /**
     * 类似 GLSurfaceView 的 queueEvent 机制，把任务抛到 GL 线程执行。
     *
     * @param r
     */
    void queueEvent(Runnable r);
}
