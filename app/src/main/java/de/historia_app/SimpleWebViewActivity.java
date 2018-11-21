package de.historia_app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

/**
 * A simple activity to show a web view from a string provided by an intent.
 *
 * This mostly shows Lexicon articles.
 */
public class SimpleWebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String data = getIntent().getStringExtra(getResources().getString(R.string.extra_key_simple_web_view_data));

        WebView webView = new WebView(this);
        webView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
        webView.setWebViewClient(new UrlSchemeRedirectingWebViewClient(this));
        this.setContentView(webView);
    }
}
