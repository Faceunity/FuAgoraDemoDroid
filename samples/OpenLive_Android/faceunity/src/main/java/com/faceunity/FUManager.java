package com.faceunity;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.faceunity.wrapper.faceunity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * 核心类，封装nama底层API
 * Created by Administrator on 2017/4/5.
 */

public class FUManager {

    //道具资源数组
    private final static String[] ITEM_NAMES = {
            "", "lixiaolong.bundle", "chibi_reimu.bundle", "mask_liudehua.bundle", "yuguan.bundle", "Mood.bundle", "gradient.bundle"
    };

    //滤镜名称数组
    final static String[] FILTERS = {"nature", "delta", "electric", "slowlived", "tokyo", "warm"};

    private static volatile WeakReference<Context> context;

    private static Handler handler;

    private static volatile int effectItem;
    private static int faceBeautyItem;

    private static int frameId;

    static volatile boolean creatingItem;

    private static volatile int rotation;

    private static FUManager instance;

    public static FUManager getInstance(Context context) {
        FUManager.context = new WeakReference<>(context);
        if (instance == null) {
            instance = new FUManager(context);
        }
        return instance;
    }

    private FUManager(final Context context) {
        HandlerThread handlerThread = new HandlerThread("FUManager");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        final byte[] authData = authpack.A();

        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    faceunity.fuCreateEGLContext();

                    InputStream is = context.getAssets().open("v3.bundle");
                    byte[] v3data = new byte[is.available()];
                    is.read(v3data);
                    is.close();
                    //TODO 调用fuSetup执行初始化
                    /*
                     类文件 ：faceunity.java

                     函数原型：
                     int fuSetup(byte[] v3data, byte[] ardata, byte[] authData);

                     参数：
                     v3Data : 人脸识别数据库
                     arData : 不需要，传null即可
                     authData : 鉴权证书authpack
                    */
                    faceunity.fuSetup(v3data, null, authData);
                    faceunity.fuSetMaxFaces(4);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadItems() {
        frameId = 0;

        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    //加载默认道具yuguan.bundle
                    loadItem("yuguan.bundle");

                    InputStream is = context.get().getAssets().open("face_beautification.bundle");
                    byte[] itemData = new byte[is.available()];
                    is.read(itemData);
                    is.close();
                    //TODO 调用fuCreateItemFromPackage，加载美颜道具
                    /*
                     类文件 ：faceunity.java

                     函数原型：
                     int fuCreateItemFromPackage(byte[] itemData);

                     参数：
                     itemData : 道具文件加载到内存中的byte[]
                     */
                    faceBeautyItem = faceunity.fuCreateItemFromPackage(itemData);
                    //设置美颜参数
                    faceunity.fuItemSetParam(faceBeautyItem, "blur_level", 6);
                    faceunity.fuItemSetParam(faceBeautyItem, "color_level", 0.2);
                    faceunity.fuItemSetParam(faceBeautyItem, "red_level", 0.5);
                    faceunity.fuItemSetParam(faceBeautyItem, "face_shape", 3);
                    faceunity.fuItemSetParam(faceBeautyItem, "face_shape_level", 0.5);
                    faceunity.fuItemSetParam(faceBeautyItem, "cheek_thinning", 1);
                    faceunity.fuItemSetParam(faceBeautyItem, "eye_enlarging", 0.5);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void renderItemsToYUVFrame(final long yBuffer, final long uBuffer, final long vBuffer, final int yStride, final int uStride, final int vStride, final int w, final int h, final int rotation) {
        if (context == null) {
            return;
        }

        synchronized (FUManager.class) {
            try {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (FUManager.class) {
                            if (FUManager.rotation != rotation) {
                                FUManager.rotation = rotation;
                                setEffectRotation();
                            }

                            faceunity.fuRenderToYUVImage(yBuffer, uBuffer, vBuffer, yStride, uStride, vStride, w, h, frameId++, new int[]{effectItem, faceBeautyItem});

                            FUManager.class.notifyAll();
                        }
                    }
                });

                FUManager.class.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void destroyItems() {
        context = null;

        handler.post(new Runnable() {
            @Override
            public void run() {
                //TODO 调用fuDestroyAllItems，销毁所有道具（特效和美颜）
                /*
                 类文件 ：faceunity.java

                 函数原型：
                 void fuDestroyAllItems();
                 */
                faceunity.fuDestroyAllItems();
                effectItem = 0;
                faceBeautyItem = 0;
                faceunity.fuOnDeviceLost();
            }
        });
    }

    private static void createEffectItem(String name) {
        try {
            InputStream is = context.get().getAssets().open(name);
            byte[] itemData = new byte[is.available()];
            is.read(itemData);
            is.close();
            effectItem = faceunity.fuCreateItemFromPackage(itemData);
            faceunity.fuItemSetParam(effectItem, "isAndroid", 1);
            setEffectRotation();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据图片（yuv数组）朝向设置道具朝向
     */
    private static void setEffectRotation() {
        faceunity.fuItemSetParam(effectItem, "default_rotation_mode", (rotation == 270) ? 1 : 3);
        faceunity.fuItemSetParam(effectItem, "rotationAngle", (rotation == 270) ? 90 : 270);
    }

    private static void destroyEffectItem(int effectItem) {
        if (effectItem != 0) {
            faceunity.fuDestroyItem(effectItem);
        }
    }

    static void loadItem(final String name) {
        int effectItem = FUManager.effectItem;
        if (!TextUtils.isEmpty(name)) {
            createEffectItem(name);
            destroyEffectItem(effectItem);
        } else {
            FUManager.effectItem = 0;
            destroyEffectItem(effectItem);
        }
    }

    static void setCurrentItemByPosition(final int itemPosition) {
        creatingItem = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO 调用FUManager封装的loadItem，加载道具数组ITEM_NAMES中对应位置道具，实现UI交互道具切换
                /*
                  类文件 : FUManager.java

                  函数原型 : void loadItem(final String name)

                  参数 ：assets中的道具名称
                */
                loadItem(ITEM_NAMES[itemPosition]);
                creatingItem = false;
            }
        }).start();
    }

    static void setCurrentFilterByPosition(final int filterPosition) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(faceBeautyItem, "filter_name", FILTERS[filterPosition]);
            }
        });
    }

    static void setBlurLevel(final int level) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(faceBeautyItem, "blur_level", level);
            }
        });
    }

    static void setColorLevel(final int progress, final int max) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(faceBeautyItem, "color_level", (float) progress / max);
            }
        });
    }

    static void setRedLevel(final int progress, final int max) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(faceBeautyItem, "red_level", (float) progress / max);
            }
        });
    }

    static void setFaceShape(final int level) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(faceBeautyItem, "face_shape", level);
            }
        });
    }

    static void setFaceShapeLevel(final int progress, final int max) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(faceBeautyItem, "face_shape_level", (float) progress / max);
            }
        });
    }

    static void setCheekThinning(final int progress, final int max) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(faceBeautyItem, "cheek_thinning", (float) progress / max);
            }
        });
    }

    static void setEyeEnlarging(final int progress, final int max) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(faceBeautyItem, "eye_enlarging", (float) progress / max);
            }
        });
    }
}
