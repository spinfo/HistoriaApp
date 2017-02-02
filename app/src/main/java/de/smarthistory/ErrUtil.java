package de.smarthistory;

import android.util.Log;

public abstract class ErrUtil {

    public static boolean failInDebug(String message) {
        if (BuildConfig.DEBUG) {
            throw new RuntimeException(message);
        } else {
            Log.e("err", message);
            return false;
        }
    }

}
