package de.smarthistory;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import de.smarthistory.data.DataFacade;
import de.smarthistory.data.Mapstop;

public class MapstopPageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retrieve the mapstop id and corresponding mapstop
        long mapstopId = getIntent().getLongExtra(getString(R.string.extra_key_mapstop_id), -1);
        if (mapstopId == -1) {
            Log.e(this.getClass().getName(), "Could not retrieve mapstop id.");
            return;
        }
        Mapstop mapstop = DataFacade.getInstance().getMapstopById(mapstopId);

        // Get the mapstop view
        View mapstopLayout = getLayoutInflater().inflate(R.layout.mapstop, null);

        // Bind mapstop view to a page loader
        MapstopPageView pageView = (MapstopPageView) mapstopLayout.findViewById(R.id.mapstop_page);
        TextView pageIndicatorView = (TextView) mapstopLayout.findViewById(R.id.mapstop_page_indicator);
        MapstopPageLoader pageLoader = new MapstopPageLoader(mapstop, pageView, pageIndicatorView);

        this.setContentView(mapstopLayout);
    }
}
