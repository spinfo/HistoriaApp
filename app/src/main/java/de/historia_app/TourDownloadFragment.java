package de.historia_app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import de.historia_app.data.AvailableTours;
import de.historia_app.data.ServerResponseReader;
import de.historia_app.data.DownloadCallback;
import de.historia_app.data.DownloadFileTask;
import de.historia_app.data.DownloadStringTask;
import de.historia_app.data.FileService;
import de.historia_app.data.TourRecord;
import de.historia_app.data.UrlSchemes;

/**
 * This fragment connects to the backend and:
 *      1. Downloads a list of available tours.
 *      2. When asked to, downloads a
 */
public class TourDownloadFragment extends Fragment implements MainActivity.MainActivityFragment {

    private static final String LOG_TAG = TourDownloadFragment.class.getSimpleName();

    private AvailableTours availableTours = null;

    private ListView tourRecordListView;

    // A class to define what happens during and after the download of available tours' info
    // (which is nothing at the moment...)
    private class AvailableToursDownloadCallback implements DownloadCallback<String> {

        // maximum length of the download (would be used in a progress meter, if this was relevant
        // here
        private int maxBytes;

        public AvailableToursDownloadCallback(int maxBytes) {
            this.maxBytes = maxBytes;
        }

        // After the download the result is parsed and set or the user is informed about the failure
        @Override
        public void updateFromDownload(String result) {
            if(result == null) {
                Toast.makeText(getActivity(), getString(R.string.tour_records_unavailable), Toast.LENGTH_LONG).show();
                TourDownloadFragment.this.availableTours = new AvailableTours();
            } else {
                TourDownloadFragment.this.availableTours = ServerResponseReader.parseAvailableTours(result);
            }
        }

        @Override
        public NetworkInfo getActiveNetworkInfo() {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            return connectivityManager.getActiveNetworkInfo();
        }

        @Override
        public void onProgressUpdate(int progressCode, int percentComplete) {
            // do nothing as this should be short
        }

        @Override
        public void finishDownloading() {
            renderDownloadList();
        }
    }

    // A class to define what happens during and after the download of a tour
    private class TourDownloadCallback implements DownloadCallback<File> {

        // A text view to update and it's text during initialization of the callback
        TextView tvTourRecordItem;
        CharSequence baseText;

        // The tour record referenced
        TourRecord record;

        // The maximum of bytes to read (used for displaying progress)
        int maxBytes;

        // get a the download callback with a text view to update
        private TourDownloadCallback(TextView tvTourRecordItem, TourRecord record, int maxBytes) {
            this.tvTourRecordItem = tvTourRecordItem;
            this.baseText = tvTourRecordItem.getText();
            this.record = record;
            this.maxBytes = maxBytes;
        }

        // hand the received File to the File Service for installation
        @Override
        public void updateFromDownload(File result) {
            if(result == null) {
                Toast.makeText(getActivity(), getString(R.string.tour_download_failed), Toast.LENGTH_LONG).show();
                TourDownloadFragment.this.availableTours = new AvailableTours();
            } else {
                FileService fileService = new FileService(getContext());
                fileService.installTour(result, record);
            }
        }

        @Override
        public NetworkInfo getActiveNetworkInfo() {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            return connectivityManager.getActiveNetworkInfo();
        }

        @Override
        public void onProgressUpdate(int progressCode, int percentComplete) {
            final String newText = baseText + " - lädt... (" + percentComplete + " %)";
            updateTextViewOnUI(newText);
        }

        @Override
        public void finishDownloading() {
            final String newText = baseText.toString();
            updateTextViewOnUI(newText);
            Toast.makeText(getContext(), "Tour installiert", Toast.LENGTH_SHORT).show();
        }

        private void updateTextViewOnUI(final String newText) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTourRecordItem.setText(newText);
                }
            });
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // setup the view to be rendered once the list of Downloads is retrieved
        this.tourRecordListView = (ListView) inflater.inflate(R.layout.tour_records_list, container, false);

        // render the list or retrieve the download list if it is not present, then render the list
        // by callback
        if(this.availableTours != null) {
            renderDownloadList();
        } else {
            retrieveAvailableTours();
        }

        return this.tourRecordListView;
    }

    private void renderDownloadList() {
        if(this.availableTours == null) {
            return;
        }
        if(this.tourRecordListView == null) {
            ErrUtil.failInDebug(LOG_TAG, "The tour records view should be present.");
            return;
        }
        TourRecordAdapter adapter = new TourRecordAdapter(this.getContext(), availableTours.getAllRecords());
        tourRecordListView.setAdapter(adapter);

        // on click of a tour record, the download starts
        /* tourRecordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TourRecord record = (TourRecord) parent.getItemAtPosition(position);
                TextView recordTv = (TextView) view.findViewById(R.id.tour_record_essentials);
                int maxSize = record.getDownloadSize();
                DownloadCallback<File> callback = new TourDownloadCallback(recordTv, record, maxSize);
                DownloadFileTask task = new DownloadFileTask(callback, getContext().getCacheDir(), 300000, maxSize);
                task.execute(record.getMediaUrl());
            }
        }); */
    }

    private void retrieveAvailableTours() {
        int maxSize = 10000000;
        AvailableToursDownloadCallback callback = new AvailableToursDownloadCallback(maxSize);
        DownloadStringTask task = new DownloadStringTask(callback, 2000, maxSize);
        task.execute(UrlSchemes.AVAILABLE_TOURS_URL);
    }

    @Override
    public boolean reactToBackButtonPressed() {
        return false;
    }
}
