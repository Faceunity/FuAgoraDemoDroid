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
 * Created by Administrator on 2017/1/6.
 */

public class EffectView extends LinearLayout
    implements RadioGroup.OnCheckedChangeListener,
        DiscreteSeekBar.OnProgressChangeListener {

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

    public EffectView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

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
        mEffectRecycleViewAdapter.setOnItemSelectedListener(new EffectAndFilterRecycleViewAdapter
                .OnItemSelectedListener() {
            @Override
            public void onItemSelected(int itemPosition, int recycleViewType) {
                if (recycleViewType == EffectAndFilterRecycleViewAdapter.RECYCLEVIEW_TYPE_EFFECT) {
                    Render.setCurrentItemByPosition(itemPosition);
                }
            }
        });
        mEffectRecycleView.setAdapter(mEffectRecycleViewAdapter);

        mFilterRecycleView = (RecyclerView) findViewById(R.id.filter_recycle_view);
        mFilterRecycleView.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false));
        EffectAndFilterRecycleViewAdapter mFilterRecycleViewAdapter =
                new EffectAndFilterRecycleViewAdapter(
                        mFilterRecycleView, EffectAndFilterRecycleViewAdapter.RECYCLEVIEW_TYPE_FILTER);
        mFilterRecycleViewAdapter.setOnItemSelectedListener(new EffectAndFilterRecycleViewAdapter
                .OnItemSelectedListener() {
            @Override
            public void onItemSelected(int itemPosition, int recycleViewType) {
                if (recycleViewType == EffectAndFilterRecycleViewAdapter.RECYCLEVIEW_TYPE_FILTER) {
                    Render.setCurrentFilterByPosition(itemPosition);
                }
            }
        });
        mFilterRecycleView.setAdapter(mFilterRecycleViewAdapter);

        viewAnimator = (ViewAnimator) findViewById(R.id.select_animator);

        ((RadioGroup) findViewById(R.id.blur_level_select)).setOnCheckedChangeListener(this);
        ((RadioGroup) findViewById(R.id.select_group)).setOnCheckedChangeListener(this);

        ((DiscreteSeekBar) findViewById(R.id.color_level_seekbar)).setOnProgressChangeListener(this);
        ((DiscreteSeekBar) findViewById(R.id.cheek_thinning_seekbar)).setOnProgressChangeListener(this);
        ((DiscreteSeekBar) findViewById(R.id.eye_enlarging_seekbar)).setOnProgressChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        for (int i = 0; i < group.getChildCount(); i++) {
            if (((RadioButton) group.getChildAt(i)).isChecked()) {
                int groupId = group.getId();
                if (groupId == R.id.select_group) {
                    viewAnimator.setDisplayedChild(i);
                } else if (groupId == R.id.blur_level_select) {
                    Render.setFaceunityBlurLevel(i);
                }
                break;
            }
        }
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        int i = seekBar.getId();
        if (i == R.id.color_level_seekbar) {
            Render.setFaceunityColorLevel(value, seekBar.getMax());
        } else if (i == R.id.cheek_thinning_seekbar) {
            Render.setFaceunityCheekThinning(value, seekBar.getMax());
        } else if (i == R.id.eye_enlarging_seekbar) {
            Render.setFaceunityEyeEnlarging(value, seekBar.getMax());
        }
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

    }
}
