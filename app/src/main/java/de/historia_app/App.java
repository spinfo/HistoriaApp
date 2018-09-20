package de.historia_app;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * The App class sets up general error handling. If an exception is thrown at
 * runtime, the app will be restarted with an error message in an Intent that is able to
 * communicate the failure. (Only if this is a DEBUG build)
 */
public class App extends Application {

    private static final String LOG_TAG = App.class.getName();

    private static Context context;

    // a reference to the default exception handler that we will replace
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;

    // our replacement exception handler will close the application and restart it with an error
    // message put into the intent for the main activity
    private Thread.UncaughtExceptionHandler replacementExceptionHandler =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(final Thread thread, final Throwable ex) {
                    // if we are not in debug mode let the normal handler handle the exception
                    if (!BuildConfig.DEBUG) {
                        defaultExceptionHandler.uncaughtException(thread, ex);
                        return;
                    }

                    // build a short error message, use the throwable's cause if one is present
                    Throwable cause = ex.getCause();
                    StackTraceElement[] trace = (cause == null) ? ex.getStackTrace() : cause.getStackTrace();
                    StringBuilder errMessage = new StringBuilder();
                    errMessage.append(ex.getMessage());
                    for(int i = 0; i < 3 && (i < trace.length); i++) {
                        errMessage.append("\n - ");
                        errMessage.append(trace[i].toString());
                    }
                    // append the version name
                    String version;
                    try {
                        version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        version = "No version found";
                    }
                    errMessage.append("\n\n -- Version: ");
                    errMessage.append(version);

                    // put the message into an intent to restart the app
                    Intent intentToRestartWithError = new Intent(getApplicationContext(), MainActivity.class);
                    intentToRestartWithError.putExtra(
                            getResources().getString(R.string.extra_key_restart_error_message),
                            errMessage.toString());

                    // Output the message to the debug log in any case
                    Log.d(LOG_TAG, errMessage.toString());

                    // wrap into a pending intent and schedule it to start after a second
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                            192837, intentToRestartWithError,
                            PendingIntent.FLAG_ONE_SHOT);
                    AlarmManager alarmManager;
                    alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            1000, pendingIntent );

                    // Signal the the application to exit with error
                    System.exit(1);

                    // re-throw, this is suggested to allow the os further handling of the error
                    // (but honestly it is unclear to me if this code might ever be reached)
                    defaultExceptionHandler.uncaughtException(thread, ex);
                }
            };


    public App() {
        // save a reference to the default exception handler
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        // setup our exception handler
        Thread.setDefaultUncaughtExceptionHandler(replacementExceptionHandler);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }


}
