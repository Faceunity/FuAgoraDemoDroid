package io.agora.propeller.headset;

public interface IHeadsetPlugListener {
    public void notifyHeadsetPlugged(boolean plugged, Object... extraData);
}
