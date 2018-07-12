package de.historia_app;


import android.content.Context;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.widget.ImageView;
import android.widget.LinearLayout;

import de.historia_app.data.Mapstop;
import de.historia_app.data.Page;

/**
 * This class handles loading and changing of pages inside a the mapstop view. It does that by
 * manipulating two views:
 * 1. A MapstopPageView (extends WebView) that holds the content for the mapstop
 * 2. A view indicating the position of the current page in the mapstop's pages.
 */
class MapstopPageLoader implements MapstopPageView.PageChangeListener {

    private static final String LOG_TAG = MapstopPageLoader.class.getSimpleName();

    private Mapstop mapstop;

    private MapstopPageView pageView;

    private LinearLayout pageIndicatorView;

    private int currentPage = -1;

    MapstopPageLoader(Mapstop mapstop, MapstopPageView pageView, LinearLayout pageIndicatorView) {
        this.mapstop = mapstop;
        this.pageView = pageView;
        this.pageIndicatorView = pageIndicatorView;

        // set this up to be the listener for page changing
        this.pageView.setPageChangeListener(this);

        // load the first page
        loadPage(1);
    }

    /**
     * @return A LayoutParams object setting up margins between views in a linear layout.
     */
    private static LinearLayout.LayoutParams makeSinglePageIndicatorViewParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(12, 0, 6, 0);
        return params;
    }

    public void changePage(int offset) {
        loadPage(currentPage + offset);
    }

    private void loadPage(int pageNo) {
        if (mapstop.hasPage(pageNo)) {
            // do not re-load the current page
            if (pageNo == currentPage) {
                Log.w(LOG_TAG, "Attempt to re-open the current mapstop page: " + pageNo);
                return;
            }
            // this somehow needs to be initialized here for audio/video to work
            pageView.setWebChromeClient(new WebChromeClient());

            // load the page content directly from string
            final Page page = this.mapstop.getPages().get(pageNo - 1);
            pageView.loadDataWithBaseURL(null, page.getPresentationContent(pageView.context), "text/html", "utf-8", null);

            // setup the current page and indicators
            currentPage = pageNo;
            drawPageIndicatorView();
        } else {
            Log.e(LOG_TAG, "Request for nonexistent page: " + pageNo);
        }
    }

    /**
     * Draw the view indicating on which page the user is in the mapstop's pages.
     */
    private void drawPageIndicatorView() {
        if (pageIndicatorView == null) {
            Log.e(LOG_TAG, "No view to draw indicators in.");
            return;
        }

        pageIndicatorView.removeAllViews();

        for (int i = 1; i <= mapstop.getPageAmount(); i++) {
            boolean isActivePage = i == currentPage;
            ImageView img = makeSinglePageIndicatorView(isActivePage);
            pageIndicatorView.addView(img, makeSinglePageIndicatorViewParams());
        }
    }

    /**
     * Build an ImageView to represent a single page in the page indicator.
     *
     * @param isActivePage Whether the ImageView should represent an active or inactive page.
     * @return always an ImageView
     */
    private ImageView makeSinglePageIndicatorView(boolean isActivePage) {
        int drawable = R.drawable.indicator_dot_unselected;
        if (isActivePage) {
            drawable = R.drawable.indicator_dot_selected;
        }

        final Context context = pageIndicatorView.getContext();
        ImageView view = new ImageView(context);
        view.setImageDrawable(context.getResources().getDrawable(drawable));

        return view;
    }

}
