package de.smarthistory;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.smarthistory.data.DataFacade;
import de.smarthistory.data.Mapstop;


/**
 * The fragment handling the map view
 */
public class MapFragment extends Fragment {

    private Logger LOGGER = Logger.getLogger(MapFragment.class.getName());

    private OnMapFragmentInteractionListener mListener;

    private DataFacade data = DataFacade.getInstance();

    // the view that this will instantiate, has to be a FrameLayout for us to be able to dim
    // the map on creating a popup
    private FrameLayout mapFragmentView;

    // dimensions for popups over the map
    // TODO put in some config for changeability on different screen types
    private static final float POPUP_WIDTH = 0.85f;
    private static final float POPUP_HEIGHT = 0.90f;

    public MapFragment() {
        // Required empty public constructor
    }

     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        // Inflate the layout for this fragment
        mapFragmentView = (FrameLayout) inflater.inflate(R.layout.fragment_map, container, false);

        // we have a foreground in place to grey out the map if needed. But at first the map should
        // be fully visible
        mapFragmentView.getForeground().setAlpha(0);

        // set up the map to use
        MapView map = (MapView) mapFragmentView.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(17);
        GeoPoint startPoint = new GeoPoint(51.22049, 6.79202);
        // GeoPoint startPoint = new GeoPoint(50.95863, 6.94487);
        mapController.setCenter(startPoint);

        // add Overlay for POIs
        addPOIs(map);

        // add Overlay for current location
        addCurrentLocation(map);

        // return the container for the map view
        return mapFragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onMapFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMapFragmentInteractionListener) {
            mListener = (OnMapFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMapFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void addCurrentLocation(MapView map) {
        GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(getContext());
        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(locationProvider, map);

        map.getOverlays().add(myLocationOverlay);
        myLocationOverlay.enableMyLocation();
    }

    private void addPOIs(MapView map) {
        List<Marker> markers = new ArrayList<>();

        // custom info window for markers
        MarkerInfoWindow window = new MapFragment.MapstopMarkerInfoWindow(R.layout.map_my_bonuspack_bubble, map);

        for (Mapstop mapstop : data.getMapstops()) {
            Marker marker = new Marker(map);
            GeoPoint geoPoint = mapstop.getPlace().getLocation();
            marker.setPosition(geoPoint);
            marker.setTitle(mapstop.getPlace().getName());
            marker.setRelatedObject(mapstop);

            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(ContextCompat.getDrawable(getContext().getApplicationContext(), R.drawable.map_marker_icon_blue_small));

            marker.setInfoWindow(window);

            markers.add(marker);
        }

        map.getOverlays().addAll(markers);
    }

    /**
     * A class controlling the behaviour of the bubble appearing above mapstop markers on the map
     */
    private class MapstopMarkerInfoWindow extends MarkerInfoWindow {

        private class MapstopMarkerInfoWindowOnclickListner implements View.OnClickListener {

            Mapstop mapstop;

            @Override
            public void onClick(View view) {
                showMapstop(this.mapstop);
            }

            public void setMapstop(Mapstop mapstop) {
                this.mapstop = mapstop;
            }
        };

        MapFragment.MapstopMarkerInfoWindow.MapstopMarkerInfoWindowOnclickListner onClickListener;

        MapView map;

        public MapstopMarkerInfoWindow(int layoutResId, final MapView mapView) {
            super(layoutResId, mapView);
            this.map = mapView;
            this.onClickListener = new MapFragment.MapstopMarkerInfoWindow.MapstopMarkerInfoWindowOnclickListner();
        }

        @Override
        public void onOpen(Object item) {
            super.onOpen(item);
            closeAllInfoWindowsOn(this.map);

            // set the right mapstop to use
            this.onClickListener.setMapstop((Mapstop) getMarkerReference().getRelatedObject());

            // set listener for containing view
            LinearLayout layout = (LinearLayout) getView().findViewById(R.id.map_my_bonuspack_bubble);
            layout.setClickable(true);
            layout.setOnClickListener(this.onClickListener);
        }
    }

    private void showMapstop(Mapstop mapstop) {
        // Get the mapstop view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mapstopLayout = inflater.inflate(R.layout.mapstop, null);

        // Bind mapstop view to a page loader
        MapstopPageView pageView = (MapstopPageView) mapstopLayout.findViewById(R.id.mapstop_page);
        TextView pageIndicatorView = (TextView) mapstopLayout.findViewById(R.id.mapstop_page_indicator);
        MapstopPageLoader pageLoader = new MapstopPageLoader(mapstop, pageView, pageIndicatorView);

        // actually display the mapstop as a popup window
        showAsPopup(mapstopLayout);
    }


    // this displays a view as a popup over the map
    private void showAsPopup(View view) {
        // determine size for the popup
        int width = (int) (mapFragmentView.getWidth() * POPUP_WIDTH);
        int height = (int) (mapFragmentView.getHeight() * POPUP_HEIGHT);

        // put the supplied view inside the popup view
        View popupContainer = getActivity().getLayoutInflater().inflate(R.layout.map_popup, null);
        RelativeLayout popupContent = (RelativeLayout) popupContainer.findViewById(R.id.map_popup_content);
        popupContent.addView(view);

        // create the popup window
        final PopupWindow popupWindow = new PopupWindow(popupContainer, width, height, true);
        popupWindow.showAtLocation(popupContainer, Gravity.CENTER, 0, 0);

        // get the popup window button and set it to dismiss the popup
        Button button = (Button) popupContainer.findViewById(R.id.map_popup_dismiss);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        // dim the background
        mapFragmentView.getForeground().setAlpha(200);

        // give the popup window a listener to undim the background if dismissed
        popupWindow.setOnDismissListener(new MapFragmentPopupOnDismissListener());
    }

    // a listener to change un-dim the map on dismissing a popup
    private class MapFragmentPopupOnDismissListener implements PopupWindow.OnDismissListener {
        public void onDismiss() {
            mapFragmentView.getForeground().setAlpha(0);
        }
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnMapFragmentInteractionListener {
        // TODO: Update argument type and make useful
        void onMapFragmentInteraction(Uri uri);
    }
}
