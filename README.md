# FuAgoraDemoDroid 快速接入文档

FuAgoraDemoDroid 是集成了 Faceunity 面部跟踪和虚拟道具功能 和 声网视频直播 的 Demo。

**注意：本 demo 支持切换相机**

本文是 FaceUnity SDK 快速对 声网视频直播 的导读说明，关于 `FaceUnity SDK` 的详细说明，请参看 **[FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/tree/dev)**


## 快速集成方法

### 一、导入 SDK

将 FaceUnity 文件夹全部拖入工程中。

- jniLibs 文件夹下 libnama.so 人脸跟踪及道具绘制核心静态库
- libs 文件夹下 nama.jar java层native接口封装
- v3.bundle 初始化必须的二进制文件
- face_beautification.bundle 我司美颜相关的二进制文件
- effects 文件夹下的 *.bundle 文件是我司制作的特效贴纸文件，自定义特效贴纸制作的文档和工具请联系我司获取。

### 二、全局配置

在 FURenderer类 的  `initFURenderer` 静态方法是对 Faceunity SDK 一些全局数据初始化的封装，可以在 Application 中调用，仅需初始化一次即可。

```
public static void initFURenderer(Context context)；
```

### 三、使用 SDK

#### 初始化

在 FURenderer类 的  `onSurfaceCreated` 方法是对 Faceunity SDK 每次使用前数据初始化的封装。

#### 图像处理

在 FURenderer类 的  `onDrawFrame` 方法是对 Faceunity SDK 图像处理方法的封装，该方法有许多重载方法适用于不同的数据类型需求。

#### 销毁

在 FURenderer类 的  `onSurfaceDestroyed` 方法是对 Faceunity SDK 数据销毁的封装。

#### 相机切换

在切换前后相机时，调用FURenderer 类的 `onCameraChange` 方法。

本 demo 是基于声网自采集的摄像头数据 demo 做的集成，使用时可以参考下面的代码。

```
  mCustomizedCameraRenderer.setOnRendererStatusListener(new CustomizedCameraRenderer.OnRendererStatusListener() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                mFURenderer.onSurfaceCreated();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {

            }

            @Override
            public int onDrawFrame(int textureId, EGLContext eglContext, int cameraWidth, int cameraHeight, float[] matrix) {
                int fuTexId = mFURenderer.onDrawFrame(textureId, cameraWidth, cameraHeight);
                AgoraVideoFrame vf = new AgoraVideoFrame();
                vf.format = AgoraVideoFrame.FORMAT_TEXTURE_2D;
                vf.timeStamp = System.currentTimeMillis();
                vf.stride = 1080;
                vf.height = 1920;
                vf.textureID = fuTexId;
                vf.syncMode = true;
                vf.eglContext11 = eglContext;
                vf.transform = matrix;

                boolean result = mRtcEngine.pushExternalVideoFrame(vf);

                if (DBG) {
                    Log.d(TAG, "onFrameAvailable "+ fuTexId + " " + result + " " + Arrays.toString(matrix));
                }
                return fuTexId;
            }

            @Override
            public void onSurfaceDestroy() {
                mFURenderer.onSurfaceDestroyed();
            }

            @Override
            public void onCameraChange(int currentCameraType, int cameraOrientation) {
                Log.d(TAG, "onCameraChange: type:" + currentCameraType + ". orientation:" + cameraOrientation);
                mFURenderer.onCameraChange(currentCameraType, cameraOrientation);
            }
        });

        // 切换相机操作
            public void onChangeCamera() {
                leaveChannel();
                if (mRtcEngine.isTextureEncodeSupported()) {
                    mRtcEngine.setExternalVideoSource(false, true, true);
                }
                mCustomizedCameraRenderer.changeCamera();
                if (mRtcEngine.isTextureEncodeSupported()) {
                    mRtcEngine.setExternalVideoSource(true, true, true);
                }
                joinChannel();
            }

```

### 四、切换道具及调整美颜参数

本例中 FURenderer类 实现了 OnFUControlListener接口，而OnFUControlListener接口是对切换道具及调整美颜参数等一系列操作的封装，demo中使用了BeautyControlView作为切换道具及调整美颜参数的控制view。使用以下代码便可实现view对各种参数的控制。

```
mBeautyControlView.setOnFUControlListener(mFURenderer);
```

**快速集成完毕，关于 FaceUnity SDK 的更多详细说明，请参看 [FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/tree/dev)**