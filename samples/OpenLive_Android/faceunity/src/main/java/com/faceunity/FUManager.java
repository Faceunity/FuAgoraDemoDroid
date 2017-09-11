package com.faceunity;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.faceunity.wrapper.faceunity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/4/5.
 */

public class FUManager {

    private final static String[] ITEM_NAMES = {
            "", "lixiaolong.bundle", "chibi_reimu.bundle", "mask_liudehua.bundle", "yuguan.bundle", "Mood.bundle", "gradient.bundle"
    };

    public final static String[] FILTERS = {"nature", "delta", "electric", "slowlived", "tokyo", "warm"};

    private static volatile Context context;

    private static Handler handler;

    private static volatile int effectItem;
    private static int facebeautyItem;

    private static int frameId;

    static volatile boolean creatingItem;

    private static volatile int rotation;

    private static FUManager instance;

    public static FUManager getInstance(Context context) {
        FUManager.context = context;
        if (instance == null) {
            instance = new FUManager(context);
        }
        return instance;
    }

    private FUManager(final Context context) {
        HandlerThread handlerThread = new HandlerThread("FUManager");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        final byte[] authdata = authpack.A();

        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    faceunity.fuCreateEGLContext();

                    InputStream is = context.getAssets().open("v3.bundle");
                    byte[] v3data = new byte[is.available()];
                    is.read(v3data);
                    is.close();
                    faceunity.fuSetup(v3data, null, authdata);
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
                    loadItem("yuguan.bundle");

                    InputStream is = context.getAssets().open("face_beautification.bundle");
                    byte[] itemData = new byte[is.available()];
                    is.read(itemData);
                    is.close();
                    facebeautyItem = faceunity.fuCreateItemFromPackage(itemData);

                    faceunity.fuItemSetParam(facebeautyItem, "blur_level", 6);
                    faceunity.fuItemSetParam(facebeautyItem, "color_level", 0.2);
                    faceunity.fuItemSetParam(facebeautyItem, "red_level", 0.5);
                    faceunity.fuItemSetParam(facebeautyItem, "face_shape", 3);
                    faceunity.fuItemSetParam(facebeautyItem, "face_shape_level", 0.5);
                    faceunity.fuItemSetParam(facebeautyItem, "cheek_thinning", 1);
                    faceunity.fuItemSetParam(facebeautyItem, "eye_enlarging", 0.5);
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
                                faceunity.fuItemSetParam(effectItem, "default_rotation_mode", (rotation == 270) ? 1 : 3);
                            }
                            FUManager.rotation = rotation;

                            faceunity.fuRenderToYUVImage(yBuffer, uBuffer, vBuffer, yStride, uStride, vStride, w, h, frameId++, new int[]{effectItem, facebeautyItem});

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
                destroyEffectItem(effectItem);
                effectItem = 0;
                faceunity.fuDestroyItem(facebeautyItem);
                faceunity.fuOnDeviceLost();
            }
        });
    }

    private static void createEffectItem(String name) {
        try {
            InputStream is = context.getAssets().open(name);
            byte[] itemData = new byte[is.available()];
            is.read(itemData);
            is.close();
            effectItem = faceunity.fuCreateItemFromPackage(itemData);
            faceunity.fuItemSetParam(effectItem, "default_rotation_mode", (rotation == 270) ? 1 : 3);
            faceunity.fuItemSetParam(effectItem, "isAndroid", 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void destroyEffectItem(int effectItem) {
        if (effectItem != 0) {
            faceunity.fuDestroyItem(effectItem);
        }
    }

    public static void loadItem(final String name) {
        int effectItem = FUManager.effectItem;
        if (!TextUtils.isEmpty(name)) {
            createEffectItem(name);
            destroyEffectItem(effectItem);
        } else {
            FUManager.effectItem = 0;
            destroyEffectItem(effectItem);
        }
        creatingItem = false;
    }

    public static void setCurrentItemByPosition(final int itemPosition) {
        creatingItem = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadItem(ITEM_NAMES[itemPosition]);
                creatingItem = false;
            }
        }).start();
    }

    public static void setCurrentFilterByPosition(final int filterPosition) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(facebeautyItem, "filter_name", FILTERS[filterPosition]);
            }
        });
    }

    public static void setBlurLevel(final int level) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(facebeautyItem, "blur_level", level);
            }
        });
    }

    public static void setColorLevel(final int progress, final int max) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(facebeautyItem, "color_level", (float) progress / max);
            }
        });
    }

    public static void setRedLevel(final int progress, final int max) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(facebeautyItem, "red_level", (float) progress / max);
            }
        });
    }

    public static void setFaceShape(final int level) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(facebeautyItem, "face_shape", level);
            }
        });
    }

    public static void setFaceShapeLevel(final int progress, final int max) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(facebeautyItem, "face_shape_level", (float) progress / max);
            }
        });
    }

    public static void setCheekThinning(final int progress, final int max) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(facebeautyItem, "cheek_thinning", (float) progress / max);
            }
        });
    }

    public static void setEyeEnlarging(final int progress, final int max) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(facebeautyItem, "eye_enlarging", (float) progress / max);
            }
        });
    }
}
