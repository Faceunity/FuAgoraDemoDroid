package io.agora.rtcwithfu.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.faceunity.nama.FURenderer;

import io.agora.rtcwithfu.R;
import io.agora.rtcwithfu.util.PreferenceUtil;

public class NeedFaceUnityAcct extends Activity {

    private boolean isOn = true;//是否使用FaceUnity

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_faceunity);

        final Button button = (Button) findViewById(R.id.btn_set);
        String isOn = PreferenceUtil.getString(this, PreferenceUtil.KEY_FACEUNITY_IS_ON);
        if (TextUtils.isEmpty(isOn) || PreferenceUtil.VALUE_OFF.equals(isOn)) {
            this.isOn = false;
        } else {
            this.isOn = true;
        }
        button.setText(this.isOn ? "On" : "Off");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NeedFaceUnityAcct.this.isOn = !NeedFaceUnityAcct.this.isOn;
                button.setText(NeedFaceUnityAcct.this.isOn ? "On" : "Off");
            }
        });

        Button btnToMain = (Button) findViewById(R.id.btn_to_main);
        btnToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NeedFaceUnityAcct.this, MainActivity.class);
                PreferenceUtil.persistString(NeedFaceUnityAcct.this, PreferenceUtil.KEY_FACEUNITY_IS_ON,
                        NeedFaceUnityAcct.this.isOn ? PreferenceUtil.VALUE_ON : PreferenceUtil.VALUE_OFF);
                startActivity(intent);
                if (NeedFaceUnityAcct.this.isOn) {
                    FURenderer.setup(NeedFaceUnityAcct.this.getApplicationContext());
                }
                finish();
            }
        });

    }
}
