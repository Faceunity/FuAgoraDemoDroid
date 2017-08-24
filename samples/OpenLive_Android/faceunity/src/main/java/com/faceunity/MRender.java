package com.faceunity;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.faceunity.wrapper.faceunity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/4/5.
 */

public class MRender {

    private final static String[] ITEM_NAMES = {
            "none", "yuguan.bundle", "lixiaolong.bundle", "mask_matianyu.bundle", "yazui.bundle", "Mood.bundle", "item0204.bundle"
    };

    public final static String[] FILTERS = {"nature", "delta", "electric", "slowlived", "tokyo", "warm"};

    private static volatile Context context;

    private static Handler handler;

    private static volatile int effectItem;
    private static int facebeautyItem;

    private static int frameId;

    private static int w;
    private static int h;

    static volatile boolean creatingItem;

    public static void create(final Context context) {
        MRender.context = context;

        frameId = 0;

        HandlerThread handlerThread = new HandlerThread("MRender");
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
                    faceunity.fuSetMaxFaces(1);

                    createEffectItem(1);

                    is = context.getAssets().open("face_beautification.bundle");
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

    static long startTime;

    public static void renderToI420Image(final byte[] img, final int w, final int h) {
        if (context == null) {
            return;
        }

        if (frameId >= 10) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            } else {
                System.out.println("aaaa "+ w + " " + h +" drawframes " + (frameId - 10) * 1000 / (System.currentTimeMillis() - startTime));
            }
        }

        synchronized (MRender.class) {
            try {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (MRender.class) {
                            if (MRender.w != 0 && (MRender.w != w || MRender.h != h)) {
                                faceunity.fuOnDeviceLost();
                            }
                            MRender.w = w;
                            MRender.h = h;

                            faceunity.fuRenderToI420Image(img, w, h, frameId++, new int[]{effectItem, facebeautyItem});

                            MRender.class.notifyAll();
                        }
                    }
                });

                MRender.class.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void destroy() {
        context = null;

        handler.post(new Runnable() {
            @Override
            public void run() {
                destroyEffectItem(effectItem);
                effectItem = 0;
                faceunity.fuDestroyItem(facebeautyItem);
                faceunity.fuOnDeviceLost();
                faceunity.fuReleaseEGLContext();
                handler.getLooper().quit();
            }
        });
    }

    private static void createEffectItem(int itemPosition) {
        try {
            if (itemPosition > 0) {
                InputStream is = context.getAssets().open(ITEM_NAMES[itemPosition]);
                byte[] itemData = new byte[is.available()];
                is.read(itemData);
                is.close();
                effectItem = faceunity.fuCreateItemFromPackage(itemData);
                faceunity.fuItemSetParam(effectItem, "isAndroid", 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void destroyEffectItem(int effectItem) {
        if (effectItem != 0) {
            faceunity.fuDestroyItem(effectItem);
        }
    }

    public static void setCurrentItemByPosition(final int itemPosition) {
        creatingItem = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int effectItem = MRender.effectItem;
                if (itemPosition > 0) {
                    createEffectItem(itemPosition);
                    destroyEffectItem(effectItem);
                } else {
                    MRender.effectItem = 0;
                    destroyEffectItem(effectItem);
                }
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
