package com.faceunity;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.faceunity.faceunitylibrary.R;

/**
 * An item include a icon and a type hint text which could be invisible,
 * eg. Filter type has type hint text but Effect item doesn't
 * Created by lirui on 2016/11/11.
 */

public class EffectAndFilterItemView extends LinearLayout {

    public ImageView mItemIcon;
    public TextView mItemText;
    private int mItemType;

    public EffectAndFilterItemView(Context context, int itemType) {
        super(context);
        init(context);
        this.mItemType = itemType;
    }

    public EffectAndFilterItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EffectAndFilterItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        if(mItemType == EffectAndFilterRecycleViewAdapter.RECYCLEVIEW_TYPE_FILTER) {
            mItemText.setVisibility(View.VISIBLE);
        }
    }

    private void init(Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        View viewRoot = LayoutInflater.from(context).inflate(
                R.layout.recyle_item_view, this, true);
        mItemIcon = (ImageView) viewRoot.findViewById(R.id.item_icon);
        mItemText = (TextView) viewRoot.findViewById(R.id.item_text);
    }

    public void setBackgroundUnSelected() {
        if (mItemType == EffectAndFilterRecycleViewAdapter.RECYCLEVIEW_TYPE_EFFECT) {
            mItemIcon.setBackgroundResource(R.drawable.effect_item_circle0);
        } else {
            mItemIcon.setBackgroundColor(Color.parseColor("#00000000"));
        }
    }

    public void setBackgroundSelected() {
        if (mItemType == EffectAndFilterRecycleViewAdapter.RECYCLEVIEW_TYPE_EFFECT) {
            mItemIcon.setBackgroundResource(R.drawable.effect_item_circle1);
        } else {
            mItemIcon.setBackgroundResource(R.drawable.effect_item_square1);
        }
    }
}
