package de.smarthistory;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import de.smarthistory.data.DataFacade;
import de.smarthistory.data.Mapstop;

public class MapstopActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logger.getLogger(MapstopActivity.class.getName());

    DataFacade data = DataFacade.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapstop);

        long mapstopId = getIntent().getLongExtra(getResources().getString(R.string.extra_key_mapstop), -1L);
        Mapstop mapstop = data.getMapstopById(mapstopId);

        WebView webView = (WebView) findViewById(R.id.mapstop_page);
        String url = data.getPageUriForMapstop(mapstop, 1);

        webView.loadUrl(url);
    }
}
