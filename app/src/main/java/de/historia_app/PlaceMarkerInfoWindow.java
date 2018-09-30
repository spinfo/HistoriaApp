package de.historia_app;

/**
 * Created by david on 06/07/17.
 */

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.List;

import de.historia_app.data.Mapstop;
import de.historia_app.mappables.MapstopOnMap;
import de.historia_app.mappables.PlaceOnMap;

/**
 * A class controlling the behaviour of the bubble appearing above PlaceOnMap markers on the map
 */
public class PlaceMarkerInfoWindow extends MarkerInfoWindow {

    private static final String LOGTAG = PlaceMarkerInfoWindow.class.getSimpleName();

    private class PlaceMarkerInfoWindowOnclickListner implements View.OnClickListener {

        PlaceOnMap placeOnMap;

        @Override
        public void onClick(View view) {
            onModelSelectionListener.onMapstopSelected(this.placeOnMap, pos);
        }

        public void setPlaceOnMap(PlaceOnMap placeOnMap) {
            this.placeOnMap = placeOnMap;
        }
    };

    // this is the object that gets informed, when a place is tapped or a mapstop is switched to
    private OnModelSelectionListener onModelSelectionListener;

    // the on click listener that reacts when a mapstop is selected to be shown
    private PlaceMarkerInfoWindowOnclickListner onClickListener;

    // the map this is being displayed on
    private MapView map;

    int pos = 0;

    public PlaceMarkerInfoWindow(int layoutResId, final MapView mapView, final OnModelSelectionListener listener) {
        super(layoutResId, mapView);
        this.map = mapView;
        this.onClickListener = new PlaceMarkerInfoWindowOnclickListner();
        this.onModelSelectionListener = listener;
    }

    @Override
    public void onOpen(Object item) {
        super.onOpen(item);
        //opening one window closes all others on the map
        InfoWindow.closeAllInfoWindowsOn(this.map);

        final PlaceOnMap placeOnMap = getPlaceOnMap();

        // only one title line is shown depending on whether the view has more than one mapstop
        LinearLayout fullLine = mView.findViewById(R.id.bubble_tour_title_line_full);
        LinearLayout withSwitcher = mView.findViewById(R.id.bubble_tour_title_line_with_switcher);
        if(placeOnMap.getMapstopsOnMap().size() <= 1) {
            withSwitcher.setVisibility(View.GONE);
        } else {
            fullLine.setVisibility(View.GONE);
            Button tourSwitchButton = (Button) withSwitcher.findViewById(R.id.bubble_tour_switch);
            tourSwitchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pos = ((pos + 1) >= placeOnMap.getMapstopsOnMap().size()) ? 0 : (pos + 1);
                    switchToPosition(pos);
                }
            });
        }

        switchToPosition(0);

        // set the right placeOnMap to use for the onclick listener
        this.onClickListener.setPlaceOnMap(placeOnMap);

        // inform the listening activity/fragment that a place was tapped
        this.onModelSelectionListener.onPlaceTapped(placeOnMap);

        // set on click listener for the bubble/infowindow
        LinearLayout layout = (LinearLayout) getView().findViewById(R.id.map_my_bonuspack_bubble);
        layout.setClickable(true);
        layout.setOnClickListener(this.onClickListener);
    }

    public void switchToPosition(int pos) {
        List<MapstopOnMap> stopsOnMap = getPlaceOnMap().getMapstopsOnMap();
        if(pos >= stopsOnMap.size()) {
            Log.w(LOGTAG, "Invalid position argument when switching to mapstop.");
            pos = 0;
        }
        this.pos = pos;
        // display basic information about the mapstop
        populateViews(stopsOnMap.get(pos).getMapstop());
        // tell the listening class, that we just switched to another mapstop
        onModelSelectionListener.onMapstopSwitched(pos);
    }

    // the mapstap this window refers to is saved as a related object of the marker
    private PlaceOnMap getPlaceOnMap() {
        return (PlaceOnMap) getMarkerReference().getRelatedObject();
    }

    private void populateViews(Mapstop mapstop) {
        setTourTitleOn((TextView) mView.findViewById(R.id.bubble_tour_title_before_switch), mapstop);
        setTourTitleOn((TextView) mView.findViewById(R.id.bubble_tour_title_full_width), mapstop);

        ((TextView) mView.findViewById(R.id.bubble_title)).setText(mapstop.getName());

        TextView subdescription = (TextView) mView.findViewById(R.id.bubble_subdescription);
        subdescription.setText(mapstop.getDescription());
        subdescription.setVisibility(View.VISIBLE);
    }

    private void setTourTitleOn(TextView tv, Mapstop mapstop) {
        tv.setText(mapstop.getTour().getName().toUpperCase());
    }

    @Override
    public void onClose() {
        super.onClose();

        // tell the listener that no place is tapped at the moment
        onModelSelectionListener.onPlaceTapped(null);
    }
}
