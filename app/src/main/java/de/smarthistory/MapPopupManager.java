package de.smarthistory;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import de.smarthistory.data.Area;
import de.smarthistory.data.DataFacade;
import de.smarthistory.data.Mapstop;
import de.smarthistory.data.Tour;

public class MapPopupManager {

    private static final String LOG_TAG = MapPopupManager.class.getSimpleName();

    private enum MapPopupType {
        NONE,
        TOUR_INTRO,
        TOUR_SELECTION,
        MAPSTOP
    }

    // an interface for listening on tour selections triggered by popups
    interface OnTourSelectionListener {
        void onTourSelected(Tour tour);
    }

    // a data provider
    private final DataFacade data;

    // the surface that the popup is shown on
    private final FrameLayout surface;

    // a layout inflater to use
    private final LayoutInflater layoutInflater;

    // values to compute the dimensions of the popup
    private static final float POPUP_WIDTH_RATIO = 0.85f;
    private static final float POPUP_HEIGHT_RATIO = 0.90f;

    // the state needed to save/recreate a popup: The popup's type and the id of the
    // object in question (either Mapstop, Area or Tour)
    private MapPopupType activePopupType;
    private long activeObjId = NO_ACTIVE_OBJ;

    // the currently active popup if there is one
    // TODO: It might be safer to have a promise for a popup here or something deferred
    private PopupWindow activePopup;

    // a check value to indicate that there is no active object
    private static long NO_ACTIVE_OBJ = -1;

    public MapPopupManager(FrameLayout surface) {
        this.data = DataFacade.getInstance(surface.getContext());
        this.surface = surface;
        this.layoutInflater = LayoutInflater.from(surface.getContext());

        // on initalization no popup is shown
        setSaveStateNil();
        this.setBackgroundDimmed(false);
    }

    // this displays a view as a popup over the surface
    private PopupWindow showAsPopup(final View view, final MapPopupType type, final long objId, final boolean asDialog) {
        // if there already is an active popup window, just switch that windows content
        if (activePopupType != MapPopupType.NONE) {
            Log.d(getClass().getName(), "Switching popup: " + activePopupType + " -> " + type);
            final ViewGroup container = (ViewGroup) activePopup.getContentView().findViewById(R.id.map_popup);
            final ViewGroup content = (ViewGroup) container.findViewById(R.id.map_popup_content);
            content.removeAllViews();
            content.addView(view);
            if (asDialog) {
                layoutInflater.inflate(R.layout.map_popup_dialog_buttons_bar, container);
            } else {
                container.removeView(container.findViewById(R.id.map_popup_dialog_buttons_bar));
            }
            setSaveState(type, objId, activePopup);
            return activePopup;
        }
        // add comment explaining null
        final View popupContainer = layoutInflater.inflate(R.layout.map_popup, null);

        // put the supplied view inside the popup view
        ViewGroup popupContent = (ViewGroup) popupContainer.findViewById(R.id.map_popup_content);
        popupContent.addView(view);

        final PopupWindow popup = new PopupWindow(popupContainer);

        // showing the popup has to wait until the surface is ready
        surface.post(new Runnable() {
            @Override
            public void run() {
                final int width = (int) (surface.getWidth() * POPUP_WIDTH_RATIO);
                final int height = (int) (surface.getHeight() * POPUP_HEIGHT_RATIO);
                if (width == 0 || height == 0) {
                    ErrUtil.failInDebug(LOG_TAG, "Bad popup dimensions: " + width + "/" + height);
                    setSaveStateNil();
                }
                popup.setWidth(width);
                popup.setHeight(height);
                popup.setFocusable(true);
                popup.showAtLocation(surface, Gravity.CENTER, 0, 0);

                // this should only be done after the popup is actually shown
                setBackgroundDimmed(true);
                setSaveState(type, objId, popup);
            }
        });

        // get the popup window button and set it to dismiss the popup
        Button button = (Button) popupContainer.findViewById(R.id.map_popup_dismiss);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        // give the popup window a listener to undim the background if dismissed
        popup.setOnDismissListener(new MapPopupOnDismissListener());

        // remove dialog buttons if this is not supposed to be a dialog
        if (!asDialog) {
            View dialogButtonBar = popupContainer.findViewById(R.id.map_popup_dialog_buttons_bar);
            ((ViewGroup)dialogButtonBar.getParent()).removeView(dialogButtonBar);
        }

        return popup;
    }

    public void showMapstop(Mapstop mapstop) {
        // Inflate the mapstop view
        // parent has to be null as it will later be set to the popup container
        View mapstopLayout = layoutInflater.inflate(R.layout.mapstop, null);

        // Bind mapstop view to a page loader
        MapstopPageView pageView = (MapstopPageView) mapstopLayout.findViewById(R.id.mapstop_page);
        TextView pageIndicatorView = (TextView) mapstopLayout.findViewById(R.id.mapstop_page_indicator);
        MapstopPageLoader pageLoader = new MapstopPageLoader(mapstop, pageView, pageIndicatorView);

        // actually display the mapstop as a popup window
        showAsPopup(mapstopLayout, MapPopupType.MAPSTOP, mapstop.getId(), false);
    }

    public void showTourSelection(Area area, final OnTourSelectionListener listener) {
        // Get the list view showing the tours to select from
        // parent has to be null as it will later be set to the popup container
        final ListView listView = (ListView) layoutInflater.inflate(R.layout.tour_or_mapstop_list, null);

        // connect to tour adapter
        final List<Tour> tourData = area.getTours();
        final Tour[] tours = tourData.toArray(new Tour[tourData.size()]);
        final TourArrayAdapter toursAdapter = new TourArrayAdapter(surface.getContext(), tours);
        listView.setAdapter(toursAdapter);

        // open the popup
        final PopupWindow popup = showAsPopup(listView, MapPopupType.TOUR_SELECTION, area.getId(), false);

        // tapping a tour should close tour selection and show the tour intro
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tour tour = (Tour) parent.getItemAtPosition(position);
                showTourIntro(tour, listener);
            }
        });
    }

    public void showTourIntro(final Tour tour, final OnTourSelectionListener listener) {
        // get the tour intro view
        // parent has to be null as it will later be set to the popup container
        final View tourIntroView = layoutInflater.inflate(R.layout.tour_intro, null, false);

        // the tour intro reuses the tour meta view
        final View tourMetaView = tourIntroView.findViewById(R.id.tour_meta);
        TourViewsHelper.injectTourDataIntoTourMetaView(tourMetaView, tour);

        // the other three views are filled as well as found by the helper
        TourViewsHelper.setMapstopsInTourIntro(tourIntroView, tour);
        TourViewsHelper.setFromTextInTourIntro(tourIntroView, tour);
        TourViewsHelper.setIntroductionTextInTourIntro(tourIntroView, tour);

        // the whole introduction is shown as a dialog popup
        final PopupWindow popup = showAsPopup(tourIntroView, MapPopupType.TOUR_INTRO, tour.getId(), true);

        // the ok buttons of the dialog popup switches to the selected tour
        final Button buttonOk = (Button) popup.getContentView().findViewById(R.id.map_popup_dialog_button_ok);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTourSelected(tour);
                popup.dismiss();
            }
        });

        // the cancel button returns to the tour selection
        final Button buttonCancel = (Button) popup.getContentView().findViewById(R.id.map_popup_dialog_button_cancel);
        buttonCancel.setText(getString(R.string.dialog_popup_back));
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTourSelection(tour.getArea(), listener);
            }
        });
    }

    private void setBackgroundDimmed(boolean shallDim) {
        if (shallDim) {
            surface.getForeground().setAlpha(200);
        } else {
            surface.getForeground().setAlpha(0);
        }
    }

    // convenience method to get a string from Resources
    private String getString(int id) {
        return surface.getContext().getResources().getString(id);
    }

    public void dismissActivePopup() {
        if (activePopup != null) {
            activePopup.dismiss();
        }
        setSaveStateNil();
    }

    // set the state that should be saved to recreate the current popup
    private void setSaveState(MapPopupType type, long objId, PopupWindow popup) {
        activePopup = popup;
        activePopupType = type;
        activeObjId = objId;
    }

    private void setSaveStateNil() {
        activePopup = null;
        activePopupType = MapPopupType.NONE;
        activeObjId = NO_ACTIVE_OBJ;
    }

    // save the state the current popup can be recreated on to the bundle
    public void savePopupStateTo(Bundle bundle) {
        if (activePopupType != MapPopupType.NONE) {
            bundle.putString(getString(R.string.extra_key_popup_type), activePopupType.name());
            bundle.putLong(getString(R.string.extra_key_popup_obj_id), activeObjId);
        }
    }

    // restore a popup from a saved state
    public void restorePopupStateFrom(Bundle bundle, OnTourSelectionListener listener) {
        // get the type that was saved to the bundle
        final String savedName = bundle.getString(getString(R.string.extra_key_popup_type), MapPopupType.NONE.name());
        final MapPopupType type = MapPopupType.valueOf(savedName);

        // return immediately if no popup is specified
        if (type == MapPopupType.NONE) {
            activePopupType = type;
            return;
        }

        // get the objects id belonging to the popup to recreate
        final long objId = bundle.getLong(getString(R.string.extra_key_popup_obj_id), -1);
        if (objId == -1) {
            Log.e("map", "Invalid: Found saved type: " + type + " without saved object id.");
            activePopupType = MapPopupType.NONE;
            return;
        }

        // if we get this far, try to retrieve the object in question
        final Object popupObject = retrievePopupObject(type, objId);
        if (popupObject == null) {
            Log.e("map", "Invalid: No object retrievable for popup type: " + type + " and objId: " + objId);
            activePopupType = MapPopupType.NONE;
            return;
        }

        // actually recreate the popup
        switch (type) {
            case MAPSTOP:
                showMapstop((Mapstop) popupObject);
                break;
            case TOUR_INTRO:
                showTourIntro((Tour) popupObject, listener);
                break;
            case TOUR_SELECTION:
                showTourSelection((Area) popupObject, listener);
                break;
            default:
                // this should never happen per the above safeguards, but check anyway in debug
                ErrUtil.failInDebug(LOG_TAG, "Popup type not recognized.");
                activePopupType = MapPopupType.NONE;
                break;
        }
    }

    private Object retrievePopupObject(MapPopupType type, long objId) {
        final Object result;
        switch (type) {
            case MAPSTOP:
                result = data.getMapstopById(objId);
                break;
            case TOUR_INTRO:
                result = data.getTourById(objId);
                break;
            case TOUR_SELECTION:
                result = data.getAreaById(objId);
                break;
            default:
                result = null;
        }
        return result;
    }

    // a basic listener to handle dismissing of a popup on the surface
    private class MapPopupOnDismissListener implements PopupWindow.OnDismissListener {
        public void onDismiss() {
            setBackgroundDimmed(false);
            setSaveStateNil();
        }
    }

}
