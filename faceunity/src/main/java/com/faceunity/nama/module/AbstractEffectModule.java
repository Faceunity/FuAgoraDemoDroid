package com.faceunity.nama.module;

import com.faceunity.nama.utils.LogUtils;
import com.faceunity.wrapper.faceunity;

/**
 * 特效模块基类
 *
 * @author Richie on 2020.07.07
 */
public abstract class AbstractEffectModule implements IEffectModule {
    private static final String TAG = "AbstractEffectModule";
    protected int mItemHandle;
    protected int mRotationMode;
    protected RenderEventQueue mRenderEventQueue;

    @Override
    public void setRotationMode(int rotationMode) {
        mRotationMode = rotationMode;
        faceunity.fuSetDefaultRotationMode(rotationMode);
    }

    @Override
    public void executeEvent() {
        if (mRenderEventQueue != null) {
            mRenderEventQueue.executeAndClear();
        }
    }

    @Override
    public void destroy() {
        if (mItemHandle > 0) {
            LogUtils.debug(TAG, "destroy item %d", mItemHandle);
            faceunity.fuDestroyItem(mItemHandle);
            mItemHandle = 0;
        }
    }

}
