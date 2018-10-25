package de.historia_app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.MailTo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.Locale;

import de.historia_app.data.AssetHelper;

public class AboutPageAcitvity extends AppCompatActivity {

    private static final String TAG = AboutPageAcitvity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);

        // set the version name in the about page activity
        TextView versionInfoView = (TextView) this.findViewById(R.id.about_page_version_info);
        versionInfoView.setText(getVersionName());

        // set the content of the about page by pointing the web view to the asset file
        WebView contentView = (WebView) this.findViewById(R.id.about_page_content);
        contentView.setWebViewClient(new UrlSchemeRedirectingWebViewClient(this));
        contentView.loadDataWithBaseURL(null, getAboutPageContent(), "text/html", "utf-8", null);
    }


    private String getVersionName() {
        String versionName;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "X.Y.Z";
        }
        return String.format(Locale.getDefault(), getString(R.string.app_version_template), versionName);
    }

    private String getAboutPageContent() {
        return AssetHelper.readAsset(this, getString(R.string.asset_about_page));
    }
}
