package de.smarthistory;

import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Locale;

public class AboutPageAcitvity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);

        // set the version name in the about page activity
        String versionName;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "X.Y.Z";
        }
        versionName = String.format(Locale.getDefault(),
                getString(R.string.app_version_template), versionName);
        TextView versionInfoView = (TextView) this.findViewById(R.id.about_page_version_info);
        versionInfoView.setText(versionName);
    }
}
