
package de.historia_app;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import de.historia_app.data.DataFacade;
import de.historia_app.data.LexiconEntry;
import de.historia_app.data.UrlSchemes;

public class MapstopPageView extends WebView {

    private static final String LOG_TAG = MapstopPageView.class.getSimpleName();

    interface PageChangeListener {
        void changePage(int offset);
    }

    Context context;

    GestureDetector gestureDetector;

    PageChangeListener pageChangeListener;

    public MapstopPageView(Context context) {
        super(context);
        init(context);
    }

    public MapstopPageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public MapstopPageView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        init(context);
    }

    private void init(final Context context) {
        this.context = context;
        this.gestureDetector = new GestureDetector(context, simpleOnGestureListener);

        // disable hardware acceleration for this view.
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // disable caching as our pages are all local
        this.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        // here we overwrite what happens when a link is clicked, and especially modify the
        // behaviour for links that go to lexicon articles
        final DataFacade data = new DataFacade(context);
        this.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith(UrlSchemes.LEXICON)) {
                    Long id = UrlSchemes.parseLexiconEntryIdFromUrl(url);
                    if(id != 0L) {
                        LexiconEntry entry = data.getLexiconEntryById(id);
                        if(entry != null) {
                            Intent intent = new Intent(getContext(), SimpleWebViewActivity.class);
                            intent.putExtra(getResources().getString(R.string.extra_key_simple_web_view_data), entry.getContent());
                            getContext().startActivity(intent);
                            return true;
                        } else {
                            ErrUtil.failInDebug(LOG_TAG, "No lexicon article for id: " + id);
                        }
                    } else {
                        ErrUtil.failInDebug(LOG_TAG, "Cannot load lexicon article from invalid url: " + url);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return (gestureDetector.onTouchEvent(event) || super.onTouchEvent(event));
    }

    GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;
        private static final int SWIPE_DISTANCE = 50;

        public boolean onDown(MotionEvent event) {
            return false;
        }

        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            float diffY = event2.getY() - event1.getY();
            float diffX = event2.getX() - event1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > SWIPE_DISTANCE) {
                        pageChangeListener.changePage(-1);
                    } else {
                        pageChangeListener.changePage(1);
                    }
                }
            }
            return true;
        }
    };

    public void setPageChangeListener(PageChangeListener pageChangeListener) {
        this.pageChangeListener = pageChangeListener;
    }
}