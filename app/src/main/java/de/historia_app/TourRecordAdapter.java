package de.historia_app;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import de.historia_app.data.AvailableTours;
import de.historia_app.data.DataFacade;
import de.historia_app.data.DownloadCallback;
import de.historia_app.data.DownloadFileTask;
import de.historia_app.data.FileService;
import de.historia_app.data.Tour;
import de.historia_app.data.TourRecord;
import de.historia_app.data.TourRecord.InstallStatus;

public class TourRecordAdapter extends ArrayAdapter<TourRecord> {

    private static final String TAG = TourRecordAdapter.class.getSimpleName();

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
                Toast.makeText(getContext(), getContext().getString(R.string.tour_download_failed), Toast.LENGTH_LONG).show();
            } else {
                FileService fileService = new FileService(getContext());
                fileService.installTour(result, record);
            }
            TourRecordAdapter.this.notifyDataSetChanged();
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
            TourRecordAdapter.this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTourRecordItem.setText(newText);
                }
            });
        }
    }

    private final DataFacade data;

    private final Activity activity;

    public TourRecordAdapter(Activity activity, ArrayList<TourRecord> records) {
        super(activity, 0, records);
        this.activity = activity;
        this.data = new DataFacade(activity);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // the record to be shown
        final TourRecord record = super.getItem(position);
        InstallStatus status = data.determineInstallStatus(record);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tour_record_meta, parent, false);
        }

        ImageButton icon = convertView.findViewById(R.id.tour_record_icon);
        icon.setImageResource(determineIconFor(status));

        final View viewToUpdate = convertView;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTourRecordActionDialog(record, viewToUpdate);
            }
        };
        icon.setOnClickListener(listener);
        convertView.setOnClickListener(listener);

        // set the tour title on the title view
        TextView nameView = (TextView) convertView.findViewById(R.id.tour_record_name);
        nameView.setText(record.getName());

        // set the other record infos on the essentials View
        StringBuilder sb = new StringBuilder();
        sb.append(record.getAreaName());
        sb.append(" - (");
        sb.append(String.format(Locale.getDefault(), "%.2f", (record.getDownloadSize() / 1000000.0)));
        sb.append(" MB)");
        TextView essentialsView = (TextView) convertView.findViewById(R.id.tour_record_essentials);
        essentialsView.setText(sb.toString());

        return convertView;
    }

    private void showTourRecordActionDialog(final TourRecord record, final View viewToUpdate) {
        TourRecordDialogFragment dialog = new TourRecordDialogFragment();
        dialog.setTourRecord(record);
        dialog.setInstallActionListener(new TourRecordDialogFragment.TourRecordInstallActionListener() {
            @Override
            public void install() {
                TourRecordAdapter.this.install(record, viewToUpdate);
            }

            @Override
            public void remove() {
                TourRecordAdapter.this.remove(record, viewToUpdate);
            }
        });
        dialog.show(activity.getFragmentManager(), "tour-dialog");
    }

    private void install(TourRecord record, View viewToUpdate) {
        Log.d(TAG, "Install selected.");
        TextView recordTv = (TextView) viewToUpdate.findViewById(R.id.tour_record_essentials);
        int maxSize = record.getDownloadSize();
        DownloadCallback<File> callback = new TourDownloadCallback(recordTv, record, maxSize);
        DownloadFileTask task = new DownloadFileTask(callback, getContext().getCacheDir(), 300000, maxSize);
        task.execute(record.getMediaUrl());
    }

    private void remove(TourRecord record, View viewToUpdate) {
        Log.d(TAG, "Remove selected.");
        FileService fileService = new FileService(getContext());
        Tour tour = (new DataFacade(getContext())).getTourById(record.getTourId());
        if (tour != null) {
            fileService.removeTour(tour);
            notifyDataSetChanged();
            Toast.makeText(getContext(), "Tour gelöscht.", Toast.LENGTH_LONG).show();
        }
    }

    private int determineIconFor(InstallStatus status) {
        int result;
        switch (status) {
            case NOT_INSTALLED:
                result = R.drawable.download_circular_button_symbol;
                break;
            case UP_TO_DATE:
                result = R.drawable.verification_sign_in_a_circle_outline;
                break;
            case UPDATE_AVAILABLE:
                result = R.drawable.circular_arrow_with_clockwise_rotation;
                break;
            default:
                throw new RuntimeException("Unknown enum value: " + status.name());
        }
        return result;
    }


}
