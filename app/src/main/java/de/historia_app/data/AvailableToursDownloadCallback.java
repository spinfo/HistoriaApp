package de.historia_app.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import de.historia_app.R;

public class AvailableToursDownloadCallback implements DownloadCallback<String> {

    public interface OnFinishedListener {
        void onAvailableToursDownloadFinished(AvailableTours availableTours);
    }

    private Context context;

    OnFinishedListener listener;

    public AvailableToursDownloadCallback(Context context, OnFinishedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // After the download the result is parsed and set or the user is informed about the failure
    @Override
    public void updateFromDownload(String result) {
        AvailableTours availableTours;
        if(result == null) {
            Toast.makeText(context, context.getString(R.string.tour_records_unavailable), Toast.LENGTH_LONG).show();
            availableTours = new AvailableTours();
        } else {
            availableTours = ServerResponseReader.parseAvailableTours(result);
        }
        listener.onAvailableToursDownloadFinished(availableTours);
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        // intentionally blank
    }

    @Override
    public void finishDownloading() {
        // intentionally blank
    }
}
