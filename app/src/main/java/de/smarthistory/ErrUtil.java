package de.smarthistory;

import android.util.Log;

public abstract class ErrUtil {

    /**
     * Crash with RuntimeException in Debug mode. If not in debug, simply log the message and
     * return false.
     *
     * @param logTag The tag to log with, when we are not in Debug mode.
     * @param message The message to log.
     * @return false if not in debug mode, else not at all.
     */
    public static boolean failInDebug(String logTag, String message) {
        if (BuildConfig.DEBUG) {
            throw new RuntimeException(message);
        } else {
            Log.e(logTag, message);
            return false;
        }
    }

    /**
     * Crash with RuntimeException in Debug mode. If not in debug, simply log the exception and
     * return false.
     *
     * @param logTag The tag to log with, when we are not in Debug mode.
     * @param e The exception that will either be re-thrown (debug mode) or logged (other).
     * @return false if not in debug mode, else not at all.
     */
    public static boolean failInDebug(String logTag, Exception e) {
        if(BuildConfig.DEBUG) {
            throw new RuntimeException(e);
        } else {
            Log.e(logTag, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
