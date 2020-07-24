package com.faceunity.nama.utils;

import android.util.Log;

/**
 * 日志工具类
 *
 * @author Richie on 2020.07.07
 */
public final class LogUtils {
    private static final String TAG = "LogUtils";
    private static final String GLOBAL_ATG = "[NAMA_LOG] ";
    /**
     * Log level
     */
    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    public static final int OFF = 7;

    private static int sLogLevel = OFF;

    public LogUtils() {
    }

    public static void setLogLevel(int level) {
        sLogLevel = level;
    }

    public static boolean isLoggable(int level) {
        return sLogLevel >= level;
    }

    public static void verbose(String tag, Throwable tr, String... msg) {
        if (Log.VERBOSE >= sLogLevel) {
            Log.v(GLOBAL_ATG + tag, formatString(msg), tr);
        }
    }

    public static void debug(String tag, Throwable tr, String... msg) {
        if (Log.DEBUG >= sLogLevel) {
            Log.d(GLOBAL_ATG + tag, formatString(msg), tr);
        }
    }

    public static void info(String tag, Throwable tr, String... msg) {
        if (Log.INFO >= sLogLevel) {
            Log.i(GLOBAL_ATG + tag, formatString(msg), tr);
        }
    }

    public static void warn(String tag, Throwable tr, String... msg) {
        if (Log.WARN >= sLogLevel) {
            Log.w(GLOBAL_ATG + tag, formatString(msg), tr);
        }
    }

    public static void error(String tag, Throwable tr, String... msg) {
        if (Log.ERROR >= sLogLevel) {
            Log.e(GLOBAL_ATG + tag, formatString(msg), tr);
        }
    }

    public static void verbose(String tag, String msg, Object... obj) {
        if (Log.VERBOSE >= sLogLevel) {
            Log.v(GLOBAL_ATG + tag, String.format(msg, obj));
        }
    }

    public static void debug(String tag, String msg, Object... obj) {
        if (Log.DEBUG >= sLogLevel) {
            Log.d(GLOBAL_ATG + tag, String.format(msg, obj));
        }
    }

    public static void info(String tag, String msg, Object... obj) {
        if (Log.INFO >= sLogLevel) {
            Log.i(GLOBAL_ATG + tag, String.format(msg, obj));
        }
    }

    public static void warn(String tag, String msg, Object... obj) {
        if (Log.WARN >= sLogLevel) {
            Log.w(GLOBAL_ATG + tag, String.format(msg, obj));
        }
    }

    public static void error(String tag, String msg, Object... obj) {
        if (Log.ERROR >= sLogLevel) {
            Log.e(GLOBAL_ATG + tag, String.format(msg, obj));
        }
    }

    public static void error(Throwable throwable) {
        if (Log.ERROR >= sLogLevel) {
            Log.e(GLOBAL_ATG, throwable.getMessage());
        }
    }

    private static String formatString(String... strings) {
        if (strings == null) {
            return null;
        } else if (strings.length == 1) {
            return strings[0];
        } else {
            StringBuilder buffer = new StringBuilder();
            for (String s : strings) {
                buffer.append(s).append(", ");
            }
            return buffer.toString();
        }
    }

}