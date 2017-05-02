package io.agora.openvcall.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.SurfaceView;

import java.util.HashMap;

import io.agora.propeller.UserStatusData;
import io.agora.propeller.VideoInfoData;

public class GridVideoViewContainer extends RecyclerView {
    public GridVideoViewContainer(Context context) {
        super(context);
    }

    public GridVideoViewContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GridVideoViewContainer(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private GridVideoViewContainerAdapter mGridVideoViewContainerAdapter;

    private VideoViewEventListener mEventListener;

    public void setItemEventHandler(VideoViewEventListener listener) {
        this.mEventListener = listener;
    }

    private boolean initAdapter(int localUid, HashMap<Integer, SurfaceView> uids) {
        if (mGridVideoViewContainerAdapter == null) {
            mGridVideoViewContainerAdapter = new GridVideoViewContainerAdapter(getContext(), localUid, uids, mEventListener);
            mGridVideoViewContainerAdapter.setHasStableIds(true);
            return true;
        }
        return false;
    }

    public void initViewContainer(Context context, int localUid, HashMap<Integer, SurfaceView> uids) {
        boolean newCreated = initAdapter(localUid, uids);

        if (!newCreated) {
            mGridVideoViewContainerAdapter.setLocalUid(localUid);
            mGridVideoViewContainerAdapter.customizedInit(uids, true);
        }

        this.setAdapter(mGridVideoViewContainerAdapter);

        int count = uids.size();
        if (count <= 2) { // only local full view or or with one peer
            this.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        } else if (count > 2 && count <= 4) {
            this.setLayoutManager(new GridLayoutManager(context, 2, RecyclerView.VERTICAL, false));
        }

        mGridVideoViewContainerAdapter.notifyDataSetChanged();
    }

    public void notifyUiChanged(HashMap<Integer, SurfaceView> uids, int localUid, HashMap<Integer, Integer> status, HashMap<Integer, Integer> volume) {
        if (mGridVideoViewContainerAdapter == null) {
            return;
        }
        mGridVideoViewContainerAdapter.notifyUiChanged(uids, localUid, status, volume);
    }

    public void addVideoInfo(int uid, VideoInfoData video) {
        if (mGridVideoViewContainerAdapter == null) {
            return;
        }
        mGridVideoViewContainerAdapter.addVideoInfo(uid, video);
    }

    public void cleanVideoInfo() {
        if (mGridVideoViewContainerAdapter == null) {
            return;
        }
        mGridVideoViewContainerAdapter.cleanVideoInfo();
    }

    public UserStatusData getItem(int position) {
        return mGridVideoViewContainerAdapter.getItem(position);
    }

}
