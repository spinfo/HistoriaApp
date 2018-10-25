package de.historia_app;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import de.historia_app.data.DataFacade;
import de.historia_app.data.TourRecord;

public class TourRecordPresenter {

    private TourRecord record;

    private String installButtonText;

    private int statusIconResource;

    private TourRecord.InstallStatus status;

    private TourRecordPresenter(TourRecord record) {
        this.record = record;
    }

    public static TourRecordPresenter create(Context context, TourRecord record) {
        TourRecordPresenter result = new TourRecordPresenter(record);

        result.status = (new DataFacade(context)).determineInstallStatus(record);
        switch (result.status) {
            case NOT_INSTALLED:
                result.statusIconResource = R.drawable.download_circular_button_symbol;
                result.installButtonText = context.getString(R.string.install_tour);
                break;
            case UP_TO_DATE:
                result.statusIconResource = R.drawable.verification_sign_in_a_circle_outline;
                result.installButtonText = context.getString(R.string.reinstall_tour);
                break;
            case UPDATE_AVAILABLE:
                result.statusIconResource = R.drawable.circular_arrow_with_clockwise_rotation;
                result.installButtonText = context.getString(R.string.update_tour);
                break;
        }

        return result;
    }

    public String title() {
        return record.getName();
    }

    public String essentialsText() {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getAreaName());
        sb.append(" - (");
        sb.append(String.format(Locale.getDefault(), "%.2f", (record.getDownloadSize() / 1000000.0)));
        sb.append(" MB)");
        return sb.toString();
    }

    public void populateTextAreaView(View view) {
        TextView nameView = (TextView) view.findViewById(R.id.tour_record_name);
        nameView.setText(title());

        TextView essentialsView = (TextView) view.findViewById(R.id.tour_record_essentials);
        essentialsView.setText(essentialsText());
    }

    public boolean showsDeleteOption() {
        return status != TourRecord.InstallStatus.NOT_INSTALLED;
    }

    public String getInstallButtonText() {
        return installButtonText;
    }

    public int getStatusIconResource() {
        return statusIconResource;
    }
}
