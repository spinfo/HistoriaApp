package de.smarthistory;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import java.util.logging.Logger;

import de.smarthistory.data.DataFacade;
import de.smarthistory.data.Mapstop;


public class MapstopDialog extends Dialog implements MapstopPageView.PageChangeListener {

    private static final Logger LOGGER = Logger.getLogger(MapstopDialog.class.getName());

    private final Context context;

    private final Mapstop mapstop;

    private MapstopPageView pageView;

    private TextView pageIndicatorTextView;

    int currentPage = -1;

    public MapstopDialog(Context context, Mapstop mapstop) {
        // instantiate with custom theme
        super(context, R.style.MapstopDialogCustom);
        this.mapstop = mapstop;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set custom dialog layout for mapstops
        this.setContentView(R.layout.mapstop);

        // setup views for use int private methods
        this.pageIndicatorTextView = (TextView) findViewById(R.id.mapstop_page_indicator_text);
        this.pageView = (MapstopPageView) findViewById(R.id.mapstop_html_content);
        this.pageView.setPageChangeListener(this);

        // load the page
        loadPage(1);
    }

    public void changePage(int offset) {
        loadPage(currentPage + offset);
    }

    private void loadPage(int pageNo) {
        LOGGER.info("request for page " + pageNo);
        if (mapstop.hasPage(pageNo) && pageNo != currentPage) {
            String url = DataFacade.getInstance().getPageUriForMapstop(mapstop, pageNo);
            pageView.loadUrl(url);
            LOGGER.info("page loaded " + pageNo);
            currentPage = pageNo;
            pageIndicatorTextView.setText(currentPage + "/" + mapstop.getPageAmount());
        }
    }

}
