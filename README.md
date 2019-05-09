# FUAgoraOpenLiveDemoDroid 快速接入文档

FUAgoraOpenLiveDemoDroid 是集成了 FaceUnity 面部跟踪和虚拟道具功能 和 **[声网视频直播](https://docs.agora.io/cn/Agora%20Platform/downloads)** 的 Demo。

本 demo 使用 Agora SDK 的新接口 `IVideoSource`，详细说明请阅读[声网的开发文档](https://docs.agora.io/cn/)

**注意：本 demo 支持切换相机**

本文是 FaceUnity SDK 快速对 声网视频直播 的导读说明，关于 `FaceUnity SDK` 的详细说明，请参看 **[FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/tree/dev)**

## 快速集成方法

### 一、导入 SDK

将 faceunity 模块依赖到工程中，demo 使用的是 FaceUnity Nama 5.9 版本。

- jniLibs 文件夹下 libnama.so 人脸跟踪及道具绘制核心静态库
- libs 文件夹下 nama.jar java层native接口封装
- v3.bundle 初始化必须的二进制文件
- face_beautification.bundle 我司美颜相关的二进制文件
- effects 文件夹下的 *.bundle 文件是我司制作的特效贴纸文件，自定义特效贴纸制作的文档和工具请联系我司获取。

### 二、全局配置

在 FURenderer类 的  `initFURenderer` 静态方法是对 FaceUnity SDK 一些全局数据初始化的封装，可以在 Application 中调用，仅需初始化一次即可。

```
public static void initFURenderer(Context context)；
```

### 三、使用 SDK

**FaceUnity 的接入代码在 io.agora.fu 包下面，请作为参考。**

#### 初始化

在 FURenderer类 的  `onSurfaceCreated` 方法是对 FaceUnity SDK 每次使用前数据初始化的封装。

#### 图像处理

在 FURenderer类 的  `onDrawFrame` 方法是对 FaceUnity SDK 图像处理方法的封装，该方法有许多重载方法适用于不同的数据类型需求。

#### 销毁

在 FURenderer类 的  `onSurfaceDestroyed` 方法是对 FaceUnity SDK 数据销毁的封装。

#### 相机切换

在切换前后相机时，调用 FURenderer 类的 `onCameraChange` 方法。


本 demo 是基于声网自采集的摄像头数据 demo 做的集成，使用时可以参考下面的代码，在 LiveRoomActivity 类中。

```
            // IVideoSource 实现类，自己采集视频，并用 FaceUnity SDK 处理
            mFuTextureCamera = new FuTextureCamera(this, 1280, 720);
            mFuTextureCamera.setOnCaptureListener(new FuTextureCamera.OnCaptureListener() {
                // 绘制每帧时，通过 FURenderer 处理
                @Override
                public int onTextureBufferAvailable(int textureId, byte[] buffer, int width, int height) {
                    return mFURenderer.onDrawFrame(buffer, textureId, width, height);
                }

                // 画面创建
                @Override
                public void onCapturerStarted() {
                    Log.d(TAG, "onCapturerStarted() called");
                    mFURenderer.onSurfaceCreated();
                }

                // 画面销毁
                @Override
                public void onCapturerStopped() {
                    Log.d(TAG, "onCapturerStopped() called");
                    mFURenderer.onSurfaceDestroyed();
                }

                // 相机切换
                @Override
                public void onCameraSwitched(int facing, int orientation) {
                    Log.d(TAG, "onCameraSwitched() called with: facing = [" + facing + "], orientation = [" + orientation + "]");
                    mFURenderer.onCameraChange(facing, orientation);
                }
            });
            // 设置视频源
            rtcEngine().setVideoSource(mFuTextureCamera);
            
            // 切换相机时调用
            mFuTextureCamera.switchCameraFacing();

```

显示画面使用声网提供的 AgoraSurfaceView 就可以，下面是初始化渲染器的代码。
```
            AgoraSurfaceView agoraSurfaceView = new AgoraSurfaceView(this);
            agoraSurfaceView.init(mFuTextureCamera.getEglContext());
            agoraSurfaceView.setBufferType(MediaIO.BufferType.TEXTURE);
            agoraSurfaceView.setPixelFormat(MediaIO.PixelFormat.TEXTURE_2D);
            agoraSurfaceView.setZOrderOnTop(true);
            agoraSurfaceView.setZOrderMediaOverlay(true);
            rtcEngine().setLocalVideoRenderer(agoraSurfaceView);
```

下面几个生命周期方法，主要是在应用处于前台、后台、销毁后的相应操作。
```
    @Override
    protected void onResume() {
        super.onResume();
        if (mFuTextureCamera != null) {
            mFuTextureCamera.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFuTextureCamera != null) {
            mFuTextureCamera.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFuTextureCamera != null) {
            mFuTextureCamera.release();
        }
    }
```

### 四、切换道具及调整美颜参数

本例中 FURenderer类 实现了 OnFUControlListener接口，而OnFUControlListener接口是对切换道具及调整美颜参数等一系列操作的封装，demo中使用了BeautyControlView作为切换道具及调整美颜参数的控制view。使用以下代码便可实现view对各种参数的控制。

```
mBeautyControlView.setOnFUControlListener(mFURenderer);
```

**快速集成完毕，关于 FaceUnity SDK 的更多详细说明，请参看 [FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/tree/dev)**