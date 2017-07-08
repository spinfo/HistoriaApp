package de.historia_app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.MailTo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

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
        contentView.setWebViewClient(new MailAndHttpRedirectingWebViewClient(this));
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
        try {
            StringBuilder buf = new StringBuilder();
            InputStream html = getAssets().open(getString(R.string.about_page_asset_name));
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(html, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
            return buf.toString();
        } catch (IOException e) {
            Log.e(TAG, "Failed to load the about page content.");
            return getString(R.string.about_page_unreachable);
        }
    }

    private class MailAndHttpRedirectingWebViewClient extends WebViewClient {

        private final Context context;

        MailAndHttpRedirectingWebViewClient(Context context) {
            this.context = context;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("mailto:")) {
                MailTo mt = MailTo.parse(url);
                Intent email = newEmailIntent(mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
                Intent chooseMail = Intent.createChooser(email, getString(R.string.choose_mail_program_title));
                AboutPageAcitvity.this.startActivityForResult(chooseMail, 0);
                return true;
            } else if (url.startsWith("http")) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
            return false;
        }

        private Intent newEmailIntent(String address, String subject, String body, String cc) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
            intent.putExtra(Intent.EXTRA_TEXT, body);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_CC, cc);
            intent.setType("message/rfc822");
            return intent;
        }
    }

}
