package de.historia_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import de.historia_app.data.DataFacade;
import de.historia_app.data.Mapstop;

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

        DataFacade data = new DataFacade(this);
        Mapstop mapstop = data.getMapstopById(mapstopId);

        // Get the mapstop view
        View mapstopLayout = getLayoutInflater().inflate(R.layout.mapstop, null);

        // Bind mapstop view to a page loader
        MapstopPageView pageView = (MapstopPageView) mapstopLayout.findViewById(R.id.mapstop_page);
        TextView pageIndicatorView = (TextView) mapstopLayout.findViewById(R.id.mapstop_page_indicator);
        MapstopPageLoader pageLoader = new MapstopPageLoader(mapstop, pageView, pageIndicatorView);

        this.setContentView(mapstopLayout);
    }
}
