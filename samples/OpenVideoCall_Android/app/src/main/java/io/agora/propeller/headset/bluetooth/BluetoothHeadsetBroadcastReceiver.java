package io.agora.propeller.headset.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import io.agora.propeller.headset.HeadsetPlugManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluetoothHeadsetBroadcastReceiver extends BroadcastReceiver {

    private final static Logger log = LoggerFactory.getLogger(BluetoothHeadsetBroadcastReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {

        BluetoothDevice mConnectedHeadset = null;

        String action = intent.getAction();
        int state = BluetoothHeadset.STATE_DISCONNECTED;
        int previousState = intent.getIntExtra(BluetoothHeadset.EXTRA_PREVIOUS_STATE, BluetoothHeadset.STATE_DISCONNECTED);
        if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
            state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);
            if (state == BluetoothHeadset.STATE_CONNECTED) {
                mConnectedHeadset = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                log.debug("AudioManager ACTION_CONNECTION_STATE_CHANGED " + mConnectedHeadset + " " + state);
                HeadsetPlugManager.getInstance().notifyHeadsetPlugged(true, HeadsetPlugManager.BLUETOOTH, mConnectedHeadset);
            } else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
                mConnectedHeadset = null;
                log.debug("AudioManager ACTION_CONNECTION_STATE_CHANGED " + " " + state);
                HeadsetPlugManager.getInstance().notifyHeadsetPlugged(false, HeadsetPlugManager.BLUETOOTH);
            }
        } else if (BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED.equals(action)) {
            state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
            log.debug("AudioManager ACTION_AUDIO_STATE_CHANGED " + " " + state);
            if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                // bluetooth audio connected. you send audio stream to headset now!!!
            } else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
            }
        } else if (AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED.equals(action)) {
            state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_DISCONNECTED);
            log.debug("AudioManager ACTION_SCO_AUDIO_STATE_UPDATED " + " " + state);
        }
    }

}
