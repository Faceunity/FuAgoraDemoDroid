package io.agora.propeller.headset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class HeadsetPlugManager {

    private final static Logger log = LoggerFactory.getLogger(HeadsetPlugManager.class);

    public static final int WIRED = 1;
    public static final int BLUETOOTH = 2;

    private static HeadsetPlugManager manager = null;

    private ArrayList<IHeadsetPlugListener> mObservers = null;

    // private constructor for singleton pattern
    private HeadsetPlugManager() {
        super();
        mObservers = new ArrayList<>();
    }

    public static synchronized HeadsetPlugManager getInstance() {
        if (manager == null) {
            log.debug("HeadsetPlugManager new instance");
            manager = new HeadsetPlugManager();
        }

        return manager;
    }

    // listener management
    public synchronized void registerHeadsetPlugListener(IHeadsetPlugListener listener) {
        if (!mObservers.contains(listener)) {
            mObservers.add(listener);

            log.debug("registerHeadsetPlugListener " + mObservers);
        }
    }

    public synchronized void unregisterHeadsetPlugListener(IHeadsetPlugListener listener) {
        if (mObservers.contains(listener)) {
            mObservers.remove(listener);

            log.debug("unregisterHeadsetPlugListener " + mObservers);
        }
    }

    public synchronized void notifyHeadsetPlugged(boolean plugged, Object... extraData) {
        for (IHeadsetPlugListener listener : mObservers) {
            listener.notifyHeadsetPlugged(plugged, extraData);
        }
    }
}
