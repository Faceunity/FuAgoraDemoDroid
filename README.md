本代码由声网 [视频通话 + 直播 SDK](https://www.agora.io/cn/download/)修改。
# 对接步骤
## 添加module
添加faceunity module到工程中，在app dependencies里添加compile project(':faceunity')
## 修改代码
### 生成与销毁
在WorkerThread的
enablePreProcessor方法中注释原有代码并添加（初始化并加载美颜道具、默认道具）
~~~
FUManager.getInstance(mContext).loadItems();
~~~
disablePreProcessor方法中注释原有代码并添加（销毁道具）
~~~
FUManager.getInstance(mContext).destroyItems();
~~~
### 渲染道具到原始数据上
在video_preprocessing_plugin_jni.cpp里
#### 增加变量
~~~
JavaVM* javaVM;
JNIEnv* env;

jclass renderClass;
jmethodID renderItemsToYUVFrameMethod;
~~~
#### 添加方法renderItemsToYUVFrame方法
使用FUManager将道具渲染到原始数据上
~~~
void renderItemsToYUVFrame(void* yBuffer, void* uBuffer, void* vBuffer, int yStride, int uStride, int vStride, int width, int height, int rotation) {
    javaVM->AttachCurrentThread(&env, NULL);

    renderItemsToYUVFrameMethod = env->GetStaticMethodID(renderClass, "renderItemsToYUVFrame", "(JJJIIIIII)V");

    env->CallStaticVoidMethod(renderClass, renderItemsToYUVFrameMethod, (jlong) yBuffer, (jlong) uBuffer, (jlong) vBuffer, yStride, uStride, vStride, width, height, rotation);

    javaVM->DetachCurrentThread();
}
~~~
#### 修改onCaptureVideoFrame
~~~
renderItemsToYUVFrame(videoFrame.yBuffer, videoFrame.uBuffer, videoFrame.vBuffer, videoFrame.yStride, videoFrame.uStride, videoFrame.vStride, videoFrame.width, videoFrame.height, videoFrame.rotation);
~~~
#### 修改Java_io_agora_propeller_preprocessing_VideoPreProcessing_enablePreProcessing
在方法Java_io_agora_propeller_preprocessing_VideoPreProcessing_enablePreProcessing结尾增加（获取JavaVM和Java类FUManager）
~~~
env->GetJavaVM(&javaVM);

renderClass = env->FindClass("com/faceunity/FUManager");
renderClass = (jclass) env->NewGlobalRef(renderClass);
~~~
## 添加界面（可选）
### 修改activity_live_room
在activity_live_room(layout文件)的末尾LinearLayout中添加（在界面底部显示默认的道具选择控件）
~~~
<com.faceunity.EffectView
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
~~~
## 修改proguard
由于video_preprocessing_plugin_jni.cpp中存在对com.faceunity.FUManager
的引用，故proguard中需添加
~~~
-keep class com.faceunity.FUManager {*;}
~~~
# 定制需求
## 定制界面
修改faceunity中的界面代码
EffectView、EffectAndFilterRecycleViewAdapter和EffectAndFilterItemView或者自己编写。
## 定制道具
faceunity中FUManager ITEM_NAMES指定的是assets里对应的道具的文件名，故如需增删道具只需要在assets增删相应的道具文件并在ITEM_NAMES增删相应的文件名即可。
## 修改默认美颜参数
修改faceunity中FUManager中以下代码
~~~
faceunity.fuItemSetParam(facebeautyItem, "blur_level", 6);
faceunity.fuItemSetParam(facebeautyItem, "color_level", 0.2);
faceunity.fuItemSetParam(facebeautyItem, "red_level", 0.5);
faceunity.fuItemSetParam(facebeautyItem, "face_shape", 3);
faceunity.fuItemSetParam(facebeautyItem, "face_shape_level", 0.5);
faceunity.fuItemSetParam(facebeautyItem, "cheek_thinning", 1);
faceunity.fuItemSetParam(facebeautyItem, "eye_enlarging", 0.5);
~~~
参数含义与取值范围参考[这里](http://www.faceunity.com/technical/android-beauty.html)，如果使用界面，则需要同时修改界面中的初始值。
## 其他需求
nama库的使用参考[这里](http://www.faceunity.com/technical/android-api.html)。
# 2D 3D道具制作
除了使用制作好的道具外，还可以自行制作2D和3D道具，参考[这里](http://www.faceunity.com/technical/fueditor-intro.html)。