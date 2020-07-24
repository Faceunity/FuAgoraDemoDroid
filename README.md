# FuAgoraDemoDroid 快速接入文档

FuAgoraDemoDroid 集成了 FaceUnity 美颜道具贴纸功能和声网视频通话 **[AgoraIO/FaceUnity](https://github.com/AgoraIO/FaceUnity/tree/master/Agora-Video-With-FaceUnity-Android)**。

本文是 FaceUnity SDK 快速对声网视频通话的导读说明，SDK 版本为 **7.0.1**。关于 SDK 的详细说明，请参看 **[FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/)**。

## 快速集成方法

### 一、导入 SDK

将 faceunity 模块添加到工程中，下面是对库文件的说明。

- jniLibs 文件夹下 libCNamaSDK.so 和 libfuai.so 是道具绘制和人脸跟踪的动态库。
- libs 文件夹下 nama.jar 提供应用层调用的 JNI 接口。
- assets/graphic 文件夹下 face_beautification.bundle 是美颜道具，body_slim.bundle 是美体道具，face_makeup.bundle 是美妆道具。
- assets/model 文件夹下 ai_face_processor.bundle 是人脸识别算法模型，ai_human_processor.bundle 是人体识别算法模型。
- assets/effect 文件夹下 \*.bundle 是特效贴纸文件，自定义特效贴纸制作的文档和工具，请联系技术支持获取。
- assets/makeup 文件夹下 \*.bundle 是美妆素材文件，自定义美妆制作的文档和工具，请联系技术支持获取。
- com/faceunity/nama/authpack.java 是证书文件，必须提供有效的证书才能运行 Demo，请联系技术支持获取。

### 二、使用 SDK

#### 1. 初始化

在 `FURenderer` 类 的  `setup` 方法是对 FaceUnity SDK 全局数据初始化的封装，可以在工作线程调用，仅需初始化一次即可。

#### 2.创建

在 `FURenderer` 类 的  `onSurfaceCreated` 方法是对 FaceUnity SDK 使用前数据初始化的封装。

#### 3. 图像处理

在 `FURenderer` 类 的  `onDrawFrameXXX` 方法是对 FaceUnity SDK 图像处理的封装，该方法有许多重载方法适用于不同数据类型的需求。

#### 4. 销毁

在 `FURenderer` 类 的  `onSurfaceDestroyed` 方法是对 FaceUnity SDK 退出前数据销毁的封装。

#### 5. 切换相机

调用 `FURenderer` 类 的  `onCameraChanged` 方法，用于重新为 SDK 设置参数。

#### 6. 旋转手机

调用 `FURenderer` 类 的  `onDeviceOrientationChanged` 方法，用于重新为 SDK 设置参数。

上面一系列方法的使用，具体在 demo 中的 `EffectHandler` 类和 `FUChatActivity`类，参考该代码示例接入即可。

### 三、接口介绍

- IFURenderer 是核心接口，提供了创建、销毁、渲染等接口。使用时通过 FURenderer.Builder 创建合适的 FURenderer 实例即可。
- IModuleManager 是模块管理接口，用于创建和销毁各个功能模块，FURenderer 是实现类。
- IFaceBeautyModule 是美颜模块的接口，用于调整美颜参数。使用时通过 FURenderer 拿到 FaceBeautyModule 实例，调用里面的接口方法即可。
- IStickerModule 是贴纸模块的接口，用于加载贴纸效果。使用时通过 FURenderer 拿到 StickerModule 实例，调用里面的接口方法即可。
- IMakeModule 是美妆模块的接口，用于加载美妆效果。使用时通过 FURenderer 拿到 MakeupModule 实例，调用里面的接口方法即可。
- IBodySlimModule 是美体模块的接口，用于调整美体参数。使用时通过 FURenderer 拿到 BodySlimModule 实例，调用里面的接口方法即可。

**至此快速集成完毕，关于 FaceUnity SDK 的更多详细说明，请参看 [FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/)** 中的文档说明。