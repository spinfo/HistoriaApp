package de.smarthistory;

import android.util.Log;

public abstract class ErrUtil {

    /**
     * Crash with RuntimeException in Debug mode. If not in debug, simply log the message and
     * return false.
     *
     * @param message The message to log
     * @return false if not in debug mode, else not at all
     */
    public static boolean failInDebug(String message) {
        if (BuildConfig.DEBUG) {
            throw new RuntimeException(message);
        } else {
            Log.e("err", message);
            return false;
        }
    }

}
