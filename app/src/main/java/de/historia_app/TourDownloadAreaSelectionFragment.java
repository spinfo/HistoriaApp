package de.historia_app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import de.historia_app.data.AreaDownloadStatus;
import de.historia_app.data.AvailableTours;
import de.historia_app.data.AvailableToursDownloadCallback;
import de.historia_app.data.DownloadStringTask;
import de.historia_app.data.UrlSchemes;

public class TourDownloadAreaSelectionFragment extends Fragment implements MainActivity.MainActivityFragment {

    private static final String TAG = TourDownloadAreaSelectionFragment.class.getSimpleName();

    private AvailableTours availableTours = null;

    private ListView tourRecordAreasView = null;

    private AreaDownloadStatusAdapter adapter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.tourRecordAreasView = (ListView) inflater.inflate(R.layout.tour_download_area_list, container, false);

        if(this.availableTours != null) {
            renderAreaList();
        } else {
            retrieveAvailableTours();
        }

        return this.tourRecordAreasView;
    }

    private void retrieveAvailableTours() {
        AvailableToursDownloadCallback callback = new AvailableToursDownloadCallback(getContext(), new AvailableToursDownloadCallback.OnFinishedListener() {
            @Override
            public void onAvailableToursDownloadFinished(AvailableTours availableTours) {
                TourDownloadAreaSelectionFragment.this.availableTours = availableTours;
                renderAreaList();
            }
        });
        DownloadStringTask task = new DownloadStringTask(callback, 2000, 10000000);
        task.execute(UrlSchemes.AVAILABLE_TOURS_URL);
    }


    private void renderAreaList() {
        if (this.availableTours == null || this.tourRecordAreasView == null) {
            return;
        }

        List<AreaDownloadStatus> statusList = availableTours.getAreaDownloadStatus(getContext());
        adapter = new AreaDownloadStatusAdapter(getContext(), statusList);
        tourRecordAreasView.setAdapter(adapter);
    }

    @Override
    public boolean reactToBackButtonPressed() {
        return false;
    }

    private void switchToTourDownload(long areaId) {
        Bundle bundle = new Bundle();
        bundle.putLong(TourDownloadFragment.AREA_ID_BUNDLE_KEY, areaId);

        Fragment tourDownloadFragment = new TourDownloadFragment();
        tourDownloadFragment.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, tourDownloadFragment, "tour-download-fragment")
                .addToBackStack(null)
                .commit();
    }


    private class AreaDownloadStatusAdapter extends ArrayAdapter<AreaDownloadStatus> {

        private AreaDownloadStatusAdapter(Context context, List<AreaDownloadStatus> statusList) {
            super(context, 0, statusList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final AreaDownloadStatus status = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.tour_download_area_meta, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.tour_download_area_meta_title_line);
            TextView descView = convertView.findViewById(R.id.tour_download_area_meta_description);

            titleView.setText(status.getName());
            descView.setText(descriptionLine(status));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TourDownloadAreaSelectionFragment.this.switchToTourDownload(status.getAreaId());
                }
            });

            return convertView;
        }

        private String descriptionLine(AreaDownloadStatus status) {
            String template = "%d/%d installiert (%.2f MB)";
            return String.format(Locale.getDefault(), template,
                    status.getDownloadedToursAmount(),
                    status.getAvailableToursAmount(),
                    (status.getDownloadedToursSize() / 1000000.0));

        }
    }
}
