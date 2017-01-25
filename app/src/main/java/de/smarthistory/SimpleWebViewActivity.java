package de.smarthistory;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import java.util.logging.Logger;

/**
 * A simple activity to show a web view from an url provided by an intent.
 */
public class SimpleWebViewActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logger.getLogger(SimpleWebViewActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url = getIntent().getStringExtra(getResources().getString(R.string.extra_key_url));

        WebView webView = new WebView(this);
        webView.loadUrl(url);
        LOGGER.info("loading web view article from url: " + url);
        this.setContentView(webView);
    }
}
