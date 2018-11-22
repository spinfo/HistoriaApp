package de.historia_app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;

import de.historia_app.data.Coordinate;
import de.historia_app.data.FileService;
import de.historia_app.data.Mapstop;
import de.historia_app.data.Scene;
import de.historia_app.data.Tour;

public class SceneLoader implements Serializable {

    private static final String LOG_TAG = SceneLoader.class.getSimpleName();

    private Tour tour;
    private ImageView sceneView;
    private HorizontalScrollView scrollView;
    private RelativeLayout coordinateContainer;
    private int currentIndex = -1;
    private TextView sceneNo;
    private MapPopupManager popupManager;

    public SceneLoader(Tour tour, ImageView sceneView, HorizontalScrollView scrollView, RelativeLayout coordinateContainer, TextView sceneNo, MapPopupManager popupManager) {
        this.tour = tour;
        this.sceneView = sceneView;
        this.scrollView = scrollView;
        this.coordinateContainer = coordinateContainer;
        this.sceneNo = sceneNo;
        this.popupManager = popupManager;

        loadScene(0);
    }

    protected void changeScene(int offset) {
        loadScene(this.currentIndex + offset);
    }

    protected void loadScene(int sceneIndex) {
        try {
            Scene scene = this.tour.getScenes().get(sceneIndex);
            loadSrc(scene);

            this.currentIndex = sceneIndex;
            this.sceneNo.setText(String.valueOf(this.currentIndex + 1).concat("\n").concat(String.valueOf(this.tour.getScenes().size())));

            this.sceneNo.setVisibility(View.VISIBLE);
            /*
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sceneNo.setVisibility(View.GONE);
                }
            }, 3000);
            */
        }
        catch (IndexOutOfBoundsException e) {
            Log.e(LOG_TAG, "Request for nonexistent scene: " + sceneIndex);
        }
    }

    private void loadSrc(final Scene scene) {
        FileService fileService = new FileService(sceneView.getContext());
        File sceneFile = new File(scene.getSrc());
        String base = sceneFile.getName();

        if(base.isEmpty()) {
            Log.w(LOG_TAG, "Could not determine base for scene src: " + scene.getSrc());
        } else {
            File file = fileService.getFile(base);
            if(file.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                int inSampleSize = 1;
                Bitmap bm = null;
                while (bm == null) {
                    try {
                        options.inSampleSize = inSampleSize;
                        bm = BitmapFactory.decodeFile(file.getPath(), options);
                    } catch (OutOfMemoryError e) {
                        Log.e(LOG_TAG, e.toString());
                        inSampleSize *= 2;
                    }
                }
                sceneView.setImageBitmap(bm);
            } else {
                Log.w(LOG_TAG, "No file for basename: " + base);
            }
        }

        removeCoordinates();

        sceneView.post(new Runnable() {
            @Override
            public void run() {
                for (Coordinate coordinate : scene.getCoordinates()) {
                    showCoordinate(coordinate);
                }
            }
        });
    }

    private void showCoordinate(Coordinate coordinate) {
        TextView stop = new TextView(sceneView.getContext());
        final Mapstop mapstop = coordinate.getMapstop();

        if (mapstop.getType().equals(Mapstop.Type.Info.getRepresentation())) {
            stop.setBackgroundResource(R.drawable.stop_marker_white);
            stop.setTextColor(Color.BLACK);
        } else {
            stop.setBackgroundResource(R.drawable.stop_marker_blue);
            stop.setTextColor(Color.WHITE);
        }

        stop.setId((int)mapstop.getId());
        stop.setText(String.valueOf(mapstop.getPos()));
        stop.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
        stop.setTypeface(stop.getTypeface(), Typeface.BOLD);
        stop.setPadding(0, calcPixelFromDp(3), calcPixelFromDp(1), 0);
        stop.setGravity(Gravity.CENTER_HORIZONTAL);

        float originalWidth = 960f;
        float originalHeight = 720f;
        int sceneWidth = sceneView.getWidth();
        int sceneHeight = sceneView.getHeight();
        float z;
        if (sceneWidth > sceneHeight) {
            z = originalWidth / sceneWidth;
        } else {
            z = originalHeight / sceneHeight;
        }

        int width = calcPixelFromDp(40);
        int height = calcPixelFromDp(40);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        float x = coordinate.getX();
        float y = coordinate.getY();
        float left = x / z - width;
        float top = y / z - height;
        params.setMargins((int)left, (int)top, 0, 0);
        stop.setLayoutParams(params);

        if (mapstop.isFirstInScene()) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity)coordinateContainer.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            scrollView.scrollTo((int)(x / z) - screenWidth / 2, 0);
        }

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupManager.showMapstop(mapstop);
            }
        });

        coordinateContainer.addView(stop);
    }

    private void removeCoordinates() {
        coordinateContainer.removeAllViews();
    }

    protected int getCurrentIndex() {
        return currentIndex;
    }

    private int calcPixelFromDp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, sceneView.getContext().getResources().getDisplayMetrics());
    }
}
