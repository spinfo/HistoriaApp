
package de.smarthistory;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.Toast;

public class MapstopPageView extends WebView {

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

    private void init(Context context) {
        this.context = context;
        this.gestureDetector = new GestureDetector(context, simpleOnGestureListener);
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