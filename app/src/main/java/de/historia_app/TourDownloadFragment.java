package de.historia_app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import de.historia_app.data.AvailableTours;
import de.historia_app.data.AvailableToursDownloadCallback;
import de.historia_app.data.DownloadStringTask;
import de.historia_app.data.UrlSchemes;

public class TourDownloadFragment extends Fragment implements MainActivity.MainActivityFragment {

    static final String AREA_ID_BUNDLE_KEY = "tour_download_area_id";

    private static final String LOG_TAG = TourDownloadFragment.class.getSimpleName();

    private AvailableTours availableTours = null;

    private ListView tourRecordListView;

    private long areaId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreAreaId(getArguments());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        restoreAreaId(getArguments());

        this.tourRecordListView = (ListView) inflater.inflate(R.layout.tour_records_list, container, false);

        if(this.availableTours != null) {
            renderDownloadList();
        } else {
            retrieveAvailableTours();
        }

        return this.tourRecordListView;
    }

    private void restoreAreaId(Bundle bundle) {
        if (bundle == null) {
            ErrUtil.failInDebug(LOG_TAG, "Bundle is null. Cannot read areaId.");
            return;
        }
        this.areaId = bundle.getLong(AREA_ID_BUNDLE_KEY, -1L);
    }

    private void retrieveAvailableTours() {
        AvailableToursDownloadCallback callback = new AvailableToursDownloadCallback(getContext(), new AvailableToursDownloadCallback.OnFinishedListener() {
            @Override
            public void onAvailableToursDownloadFinished(AvailableTours availableTours) {
                TourDownloadFragment.this.availableTours = availableTours;
                renderDownloadList();
            }
        });
        DownloadStringTask task = new DownloadStringTask(callback, 2000, 10000000);
        task.execute(UrlSchemes.AVAILABLE_TOURS_URL);
    }

    private void renderDownloadList() {
        if(this.availableTours == null || this.tourRecordListView == null) {
            return;
        }
        if (areaId == -1L) {
            ErrUtil.failInDebug(LOG_TAG, "Area id was not properly restored from bundle.");
            return;
        }
        TourRecordAdapter adapter = new TourRecordAdapter(getActivity(), availableTours.getRecordsIn(areaId));
        tourRecordListView.setAdapter(adapter);
    }

    @Override
    public boolean reactToBackButtonPressed() {
        return false;
    }
}
