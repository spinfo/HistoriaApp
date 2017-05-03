package de.smarthistory;


import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.TextView;

import java.util.Map;

import de.smarthistory.data.DataFacade;
import de.smarthistory.data.Mapstop;
import de.smarthistory.data.Page;

/**
 * This class handles loading and changing of pages inside a the mapstop view. It does that by
 * manipulating two views:
 *  1. A MapstopPageView (extends WebView) that holds the content for the mapstop
 *  2. A view indicating the position of the current page in the mapstop's pages.
 */
class MapstopPageLoader implements MapstopPageView.PageChangeListener {

    private static final String LOG_TAG = MapstopPageLoader.class.getSimpleName();

    private Mapstop mapstop;

    private MapstopPageView pageView;

    private TextView pageIndicatorTextView;

    private int currentPage = -1;

    MapstopPageLoader(Mapstop mapstop, MapstopPageView pageView, TextView pageIndicatorTextView) {
        this.mapstop = mapstop;
        this.pageView = pageView;
        this.pageIndicatorTextView = pageIndicatorTextView;

        // set this up to be the listener for page changing
        this.pageView.setPageChangeListener(this);

        // load the first page
        loadPage(1);
    }

    public void changePage(int offset) {
        loadPage(currentPage + offset);
    }

    private void loadPage(int pageNo) {
        if (mapstop.hasPage(pageNo)) {
            // do not re-load the current page
            if(pageNo == currentPage) {
                Log.w(LOG_TAG, "Attempt to re-open the current mapstop page: " + pageNo);
                return;
            }
            // this somehow needs to initialized here for audio/video to work
            pageView.setWebChromeClient(new WebChromeClient());

            // load the page content directly from string
            final Page page = this.mapstop.getPages().get(pageNo - 1);
            pageView.loadDataWithBaseURL(null, page.getPresentationContent(pageView.context), "text/html", "utf-8", null);

            // setup the current page and indicators
            currentPage = pageNo;
            pageIndicatorTextView.setText(currentPage + "/" + mapstop.getPageAmount());
        } else {
            Log.e(LOG_TAG, "Request for nonexistent page: " + pageNo);
        }
    }


}
