package de.smarthistory;


import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.TextView;

import java.util.Map;

import de.smarthistory.data.DataFacade;
import de.smarthistory.data.Mapstop;

/**
 * This class handles loading and changing of pages inside a the mapstop view. It does that by
 * manipulating two views:
 *  1. A MapstopPageView (extends WebView) that holds the content for the mapstop
 *  2. A view indicating what the current view is for.
 */
public class MapstopPageLoader implements MapstopPageView.PageChangeListener {

    Mapstop mapstop;

    private MapstopPageView pageView;

    private TextView pageIndicatorTextView;

    int currentPage = -1;

    public MapstopPageLoader(Mapstop mapstop, MapstopPageView pageView, TextView pageIndicatorTextView) {
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
        if (mapstop.hasPage(pageNo) && pageNo != currentPage) {
            pageView.setWebChromeClient(new WebChromeClient());

            String url = DataFacade.getPageUriForMapstop(mapstop, pageNo);
            pageView.loadUrl(url);

            currentPage = pageNo;
            pageIndicatorTextView.setText(currentPage + "/" + mapstop.getPageAmount());
        }
    }


}
