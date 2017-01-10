package com.faceunity;

import android.content.Context;
import android.util.Log;

import com.faceunity.wrapper.faceunity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2016/12/18.
 */

public class Render {

    private final static String[] m_item_names = {
            "none", "tiara.mp3", "item0208.mp3", "einstein.mp3",
            "YellowEar.mp3", "PrincessCrown.mp3",
            "Mood.mp3", "Deer.mp3", "BeagleDog.mp3", "item0501.mp3", "ColorCrown.mp3",
            "item0210.mp3",  "HappyRabbi.mp3", "item0204.mp3",
            "hartshorn.mp3"
    };

    public final static String[] m_filters = {"nature", "delta", "electric", "slowlived", "tokyo", "warm"};

    static int mFacebeautyItem;
    static int mEffectItem;

    static int mFrameId;

    private volatile static boolean isInit;
    private volatile static boolean isWork;

    private static Context context;

    private volatile static int m_cur_item_id = 1;
    private volatile static int m_cur_filter_id = 0;

    private static float m_faceunity_blur_level = 5.0f;
    private static float m_faceunity_color_level = 1.0f;
    private static float m_faceunity_cheek_thinning = 1.0f;
    private static float m_faceunity_eye_enlarging = 1.0f;

    public static void fuSetUp(Context c) {
        context = c;
        isWork = true;
    }

    public static void fuDestroy() {
        isWork = false;

        if (isInit) {
            isInit = false;

            faceunity.fuDestroyItem(mEffectItem);
            faceunity.fuDestroyItem(mFacebeautyItem);
            faceunity.fuOnDeviceLost();
        }
    }

    /**
     * 初始化GL环境，创建GL Context
     *
     */
    private static void fuInit() {
        /**
         * 如果当前线程没有GL Context，那么可以使用我们的API创建一个
         * 如果已经有GL Context，如在GLSufaceView对应的Renderer,则无需使用
         *
         * 所有FU API 都需要保证在*同一个*具有*GL Context*的线程被调用
         *
         * 建议使用者在*非主线程*完成fu相关操作，这里就不做演示了
         */
        faceunity.fuCreateEGLContext();

        try {
            InputStream is = context.getAssets().open("v3.mp3");
            byte[] v3data = new byte[is.available()];
            is.read(v3data);
            is.close();
            faceunity.fuSetup(v3data, null, authpack.A());
            faceunity.fuSetMaxFaces(1);

            is = context.getAssets().open("face_beautification.mp3");
            byte[] itemData = new byte[is.available()];
            is.read(itemData);
            is.close();
            mFacebeautyItem = faceunity.fuCreateItemFromPackage(itemData);

            faceunity.fuItemSetParam(mEffectItem, "isAndroid", 1.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void fuRenderToNV21Image(byte[] img, int w, int h) {
        if (!isWork) {
            return;
        }

        if (!isInit) {
            fuInit();
            isInit = true;
        }

        synchronized (Render.class) {
            if (m_cur_item_id >= 0) {
                if (mEffectItem != 0) {
                    faceunity.fuDestroyItem(mEffectItem);
                    mEffectItem = 0;
                }

                if (m_cur_item_id > 0) {
                    try {
                        InputStream is = context.getAssets().open(m_item_names[m_cur_item_id]);
                        byte[] itemData = new byte[is.available()];
                        is.read(itemData);
                        is.close();
                        mEffectItem = faceunity.fuCreateItemFromPackage(itemData);
                        m_cur_item_id = -1;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        faceunity.fuItemSetParam(mFacebeautyItem, "filter_name", m_filters[m_cur_filter_id]);
        faceunity.fuItemSetParam(mFacebeautyItem, "blur_level", m_faceunity_blur_level);
        faceunity.fuItemSetParam(mFacebeautyItem, "color_level", m_faceunity_color_level);
        faceunity.fuItemSetParam(mFacebeautyItem, "cheek_thinning", m_faceunity_cheek_thinning);
        faceunity.fuItemSetParam(mFacebeautyItem, "eye_enlarging", m_faceunity_eye_enlarging);

        faceunity.fuRenderToNV21Image(img, w, h, mFrameId++, new int[] { mEffectItem, mFacebeautyItem });
    }

    public static void setCurrentItemByPosition(int itemPosition) {
        synchronized (Render.class) {
            m_cur_item_id = itemPosition;
        }
    }

    public static void setCurrentFilterByPosition(int filterPosition) {
        m_cur_filter_id = filterPosition;
    }

    public static void setFaceunityBlurLevel(int level) {
       m_faceunity_blur_level = level;
    }

    public static void setFaceunityColorLevel(int progress, int max) {
        m_faceunity_color_level = 1.0f * progress / max * 1;
    }

    public static void setFaceunityCheekThinning(int progress, int max) {
        m_faceunity_cheek_thinning = 1.0f * progress / max * 1;
    }

    public static void setFaceunityEyeEnlarging(int progress, int max) {
        m_faceunity_eye_enlarging = 1.0f * progress / max * 1;
    }

}
