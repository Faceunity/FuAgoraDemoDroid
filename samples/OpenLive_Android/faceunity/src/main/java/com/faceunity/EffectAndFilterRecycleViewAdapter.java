package com.faceunity;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.faceunity.faceunitylibrary.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by lirui on 2016/10/19.
 */

public class EffectAndFilterRecycleViewAdapter extends RecyclerView.Adapter<EffectAndFilterRecycleViewAdapter.ItemViewHolder>{

    private static final int[] EFFECT_ITEM_RES_ARRAY = {
            R.drawable.ic_delete_all, R.drawable.yuguan, R.drawable.lixiaolong, R.drawable.matianyu, R.drawable.yazui, R.drawable.mood, R.drawable.item0204
    };

    private static final int[] FILTER_ITEM_RES_ARRAY = {R.drawable.nature, R.drawable.delta,
            R.drawable.electric, R.drawable.slowlived, R.drawable.tokyo, R.drawable.warm};

    public static final int RECYCLEVIEW_TYPE_EFFECT = 0;
    public static final int RECYCLEVIEW_TYPE_FILTER = 1;
    private int mRecycleViewType;

    private ArrayList<Boolean> mItemClickStateList; //store the click state in a list, restore it in time
    private RecyclerView mRecycleView;
    private int mLastClickPosition;

    public EffectAndFilterRecycleViewAdapter(RecyclerView recyclerView, int recycleViewType) {
        super();

        mRecycleView = recyclerView;
        mRecycleViewType = recycleViewType;

        mItemClickStateList = new ArrayList<>();
        initClickStateList();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(new EffectAndFilterItemView(parent.getContext(), mRecycleViewType));
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        //decide the border by click state
        if (mItemClickStateList.get(position) == null || !mItemClickStateList.get(position)) {
            holder.mItemView.setBackgroundUnSelected();
        } else {
            holder.mItemView.setBackgroundSelected();
        }
        //deal the resource image to present
        if (mRecycleViewType == RECYCLEVIEW_TYPE_EFFECT) {
            holder.mItemView.mItemIcon.setImageResource(EFFECT_ITEM_RES_ARRAY[
                    position % EFFECT_ITEM_RES_ARRAY.length]);
        } else {
            holder.mItemView.mItemText.setVisibility(View.VISIBLE);
            holder.mItemView.mItemIcon.setImageResource(FILTER_ITEM_RES_ARRAY[
                    position % FILTER_ITEM_RES_ARRAY.length]);
            holder.mItemView.mItemText.setText(MRender.FILTERS[
                    position % FILTER_ITEM_RES_ARRAY.length].toUpperCase());
        }

        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MRender.creatingItem) {
                    if (mOnItemSelectedListener != null) {
                        mOnItemSelectedListener.onItemSelected(position, mRecycleViewType);
                    }
                    if (mLastClickPosition != position) {
                        ItemViewHolder lastClickItemViewHolder = (ItemViewHolder) mRecycleView
                                .findViewHolderForAdapterPosition(mLastClickPosition);
                        //restore the image background of last click position
                        if (lastClickItemViewHolder != null) {
                            lastClickItemViewHolder.mItemView.setBackgroundUnSelected();
                        }
                        mItemClickStateList.set(mLastClickPosition, false);
                    }
                    holder.mItemView.setBackgroundSelected();
                    setClickPosition(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRecycleViewType == RECYCLEVIEW_TYPE_EFFECT ?
                EFFECT_ITEM_RES_ARRAY.length : FILTER_ITEM_RES_ARRAY.length;
    }

    /**
     * restore click related state int list
     * @param position
     */
    private void setClickPosition(int position) {
        mItemClickStateList.set(position, true);
        mLastClickPosition = position;
    }

    private void initClickStateList() {
        if (mItemClickStateList == null) {
            return;
        }
        mItemClickStateList.clear();
        if (mRecycleViewType == RECYCLEVIEW_TYPE_EFFECT) {
            mItemClickStateList.addAll(Arrays.asList(
                    new Boolean[EFFECT_ITEM_RES_ARRAY.length]));
            //default effect select item is 1
            setClickPosition(1);
            if (mOnItemSelectedListener != null) {
                mOnItemSelectedListener.onItemSelected(1, mRecycleViewType);
            }
        } else {
            mItemClickStateList.addAll(Arrays.asList(
                    new Boolean[FILTER_ITEM_RES_ARRAY.length]));
            //default filter select item is 0
            setClickPosition(0);
            if (mOnItemSelectedListener != null) {
                mOnItemSelectedListener.onItemSelected(0, mRecycleViewType);
            }
        }

    }

//    public static String getHintText(int effectGroupType, int effectItemId) {
//        int indexRes = EFFECT_ITEM_RES_ARRAY[effectGroupType][effectItemId];
//        switch (indexRes){
//            case R.drawable.mood :
//                return "嘴角向上以及嘴角向下";
//            case R.drawable.item0204 :
//                return "做咀嚼动作";
//            case R.drawable.nangua3 :
//                return "嘴角向上以及嘴角向下";
//            case R.drawable.tears:
//                return "张开嘴巴";
//        }
//        return "";
//    }

    private OnItemSelectedListener mOnItemSelectedListener;

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int itemPosition, int recycleViewType);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        EffectAndFilterItemView mItemView;
        ItemViewHolder(View itemView) {
            super(itemView);
            mItemView = (EffectAndFilterItemView) itemView;
        }
    }
}
