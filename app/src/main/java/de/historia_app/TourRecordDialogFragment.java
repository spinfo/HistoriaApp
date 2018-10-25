package de.historia_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;

public class TourRecordDialogFragment extends DialogFragment {

    private static final String TAG = TourRecordDialogFragment.class.getSimpleName();

    public interface TourRecordInstallActionListener {
        void install();
        void remove();
    }

    private TourRecordPresenter recordPresenter;
    private TourRecordInstallActionListener installActionListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (recordPresenter == null || installActionListener == null) {
            ErrUtil.failInDebug(TAG, "Preconditions not met for (de-)installing the tour.");
            return createErrorDialog();
        }
        return createDialog();
    }

    private Dialog createErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setMessage(getString(R.string.unable_to_edit_tour));
        return builder.create();
    }

    private Dialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setMessage(recordPresenter.simpleMessage());

        if (recordPresenter.showsDeleteOption()) {
            builder.setNegativeButton(getString(R.string.uninstall_tour), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    installActionListener.remove();
                }
            });
        }
        builder.setPositiveButton(recordPresenter.getInstallButtonText(), new DialogInterface.OnClickListener() {
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

    public void setRecordPresenter(TourRecordPresenter recordPresenter) {
        this.recordPresenter = recordPresenter;
    }

    public void setInstallActionListener(TourRecordInstallActionListener installActionListener) {
        this.installActionListener = installActionListener;
    }
}
