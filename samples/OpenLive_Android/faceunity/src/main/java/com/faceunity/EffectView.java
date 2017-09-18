package com.faceunity;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ViewAnimator;

import com.faceunity.faceunitylibrary.R;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * 特效选择控件
 * 包括道具选择和美颜选择
 * Created by Administrator on 2017/1/6.
 */

public class EffectView extends LinearLayout
    implements RadioGroup.OnCheckedChangeListener,
        DiscreteSeekBar.OnProgressChangeListener,
        EffectAndFilterRecycleViewAdapter.OnItemSelectedListener {

    private RecyclerView mEffectRecycleView;
    private EffectAndFilterRecycleViewAdapter mEffectRecycleViewAdapter;

    RecyclerView mFilterRecycleView;

    private ViewAnimator viewAnimator;

    public EffectView(Context context) {
        super(context);

        init(context);
    }

    public EffectView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public EffectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.fu_select, this);

        /*deal with effect selected related, such as group or item*/
        mEffectRecycleView = (RecyclerView) findViewById(R.id.effect_recycle_view);
        mEffectRecycleView.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false));
        mEffectRecycleViewAdapter = new EffectAndFilterRecycleViewAdapter(mEffectRecycleView,
                EffectAndFilterRecycleViewAdapter.RECYCLEVIEW_TYPE_EFFECT);
        mEffectRecycleViewAdapter.setOnItemSelectedListener(this);
        mEffectRecycleView.setAdapter(mEffectRecycleViewAdapter);

        mFilterRecycleView = (RecyclerView) findViewById(R.id.filter_recycle_view);
        mFilterRecycleView.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false));
        EffectAndFilterRecycleViewAdapter mFilterRecycleViewAdapter =
                new EffectAndFilterRecycleViewAdapter(
                        mFilterRecycleView, EffectAndFilterRecycleViewAdapter.RECYCLEVIEW_TYPE_FILTER);
        mFilterRecycleViewAdapter.setOnItemSelectedListener(this);
        mFilterRecycleView.setAdapter(mFilterRecycleViewAdapter);

        viewAnimator = (ViewAnimator) findViewById(R.id.select_animator);

        ((RadioGroup) findViewById(R.id.blur_level_select)).setOnCheckedChangeListener(this);
        ((RadioGroup) findViewById(R.id.face_shape)).setOnCheckedChangeListener(this);
        ((RadioGroup) findViewById(R.id.select_group)).setOnCheckedChangeListener(this);

        ((DiscreteSeekBar) findViewById(R.id.color_level_seekbar)).setOnProgressChangeListener(this);
        ((DiscreteSeekBar) findViewById(R.id.red_level_seekbar)).setOnProgressChangeListener(this);
        ((DiscreteSeekBar) findViewById(R.id.face_shape_level_seekbar)).setOnProgressChangeListener(this);
        ((DiscreteSeekBar) findViewById(R.id.eye_enlarging_seekbar)).setOnProgressChangeListener(this);
        ((DiscreteSeekBar) findViewById(R.id.cheek_thinning_seekbar)).setOnProgressChangeListener(this);
    }

    @Override
    public void onItemSelected(int itemPosition, int recycleViewType) {
        if (recycleViewType == EffectAndFilterRecycleViewAdapter.RECYCLEVIEW_TYPE_FILTER) {
            FUManager.setCurrentFilterByPosition(itemPosition);
        } else if (recycleViewType == EffectAndFilterRecycleViewAdapter.RECYCLEVIEW_TYPE_EFFECT) {
            FUManager.setCurrentItemByPosition(itemPosition);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        for (int i = 0; i < group.getChildCount(); i++) {
            if (((RadioButton) group.getChildAt(i)).isChecked()) {
                int groupId = group.getId();
                if (groupId == R.id.select_group) {
                    viewAnimator.setDisplayedChild(i);
                } else if (groupId == R.id.blur_level_select) {
                    FUManager.setBlurLevel(i);
                } else if (groupId == R.id.face_shape) {
                    FUManager.setFaceShape((i + 3) % 4);
                }
                break;
            }
        }
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        int i = seekBar.getId();
        if (i == R.id.color_level_seekbar) {
            FUManager.setColorLevel(value, seekBar.getMax());
        } else if (i == R.id.red_level_seekbar) {
            FUManager.setRedLevel(value, seekBar.getMax());
        } else if (i == R.id.face_shape_level_seekbar) {
            FUManager.setFaceShapeLevel(value, seekBar.getMax());
        } else if (i == R.id.cheek_thinning_seekbar) {
            FUManager.setCheekThinning(value, seekBar.getMax());
        } else if (i == R.id.eye_enlarging_seekbar) {
            FUManager.setEyeEnlarging(value, seekBar.getMax());
        }
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

    }
}
