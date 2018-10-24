package de.historia_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

import de.historia_app.data.DataFacade;
import de.historia_app.data.TourRecord;

public class TourRecordDialogFragment extends DialogFragment {

    private static final String TAG = TourRecordDialogFragment.class.getSimpleName();

    public interface TourRecordInstallActionListener {
        void install();
        void remove();
    }

    private TourRecord tourRecord;
    private TourRecordInstallActionListener installActionListener;

    private static String buildMessage(TourRecord record) {
        String message = "Tour \"%s\" (%.2f MB)";
        return String.format(Locale.getDefault(), message, record.getName(), record.getDownloadSize() / 1000000.0);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (tourRecord == null || installActionListener == null) {
            ErrUtil.failInDebug(TAG, "Preconditions not met for (de-)installing the tour.");
            return createErrorDialog();
        }
        return createDialog();
    }

    private Dialog createErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setMessage("Die Tour kann leider nicht bearbeitet werden.");
        return builder.create();
    }

    private Dialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setMessage(buildMessage(tourRecord));

        builder.setNegativeButton("Löschen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                installActionListener.remove();
            }
        });
        builder.setPositiveButton(installTextFor(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                installActionListener.install();
            }
        });

        final Dialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                negativeButton.setTextColor(getResources().getColor(R.color.danger_zone));
            }
        });

        return dialog;
    }

    private String installTextFor() {
        TourRecord.InstallStatus status = (new DataFacade(getActivity())).determineInstallStatus(tourRecord);
        switch (status) {
            case NOT_INSTALLED:
                return "Installieren";
            case UPDATE_AVAILABLE:
                return "Update";
            case UP_TO_DATE:
                return "Erneut installieren";
            default:
                throw new RuntimeException("No state determinable for tour record.");
        }
    }

    public void setTourRecord(TourRecord tourRecord) {
        this.tourRecord = tourRecord;
    }

    public void setInstallActionListener(TourRecordInstallActionListener installActionListener) {
        this.installActionListener = installActionListener;
    }

    private void showTourRecordAlertDialog(TourRecord record) {
        TourRecordDialogFragment dialog = new TourRecordDialogFragment();
        dialog.setTourRecord(record);
        dialog.setInstallActionListener(this.installActionListener);
        dialog.show(getActivity().getFragmentManager(), "tour-dialog");
    }
}
