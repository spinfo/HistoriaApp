package de.historia_app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
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

import de.historia_app.data.Coordinate;
import de.historia_app.data.Mapstop;
import de.historia_app.data.Scene;
import de.historia_app.data.Tour;

public class SceneLoader {

    private static final String LOG_TAG = SceneLoader.class.getSimpleName();

    private Tour tour;
    private ImageView sceneView;
    private HorizontalScrollView scrollView;
    private RelativeLayout coordinateContainer;
    private int currentIndex = -1;
    private MapPopupManager popupManager;

    public SceneLoader(Tour tour, ImageView sceneView, HorizontalScrollView scrollView, RelativeLayout coordinateContainer, MapPopupManager popupManager) {
        this.tour = tour;
        this.sceneView = sceneView;
        this.scrollView = scrollView;
        this.coordinateContainer = coordinateContainer;
        this.popupManager = popupManager;

        loadScene(0);
    }

    protected void changeScene(int offset) {
        loadScene(this.currentIndex + offset);
    }

    private void loadScene(int sceneIndex) {
        try {
            Scene scene = this.tour.getScenes().get(sceneIndex);
            loadSrc(scene);

            this.currentIndex = sceneIndex;
        }
        catch (IndexOutOfBoundsException e) {
            Log.e(LOG_TAG, "Request for nonexistent scene: " + sceneIndex);
        }
    }

    private void loadSrc(final Scene scene) {
        //File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "scenes");
        File topDir = coordinateContainer.getContext().getFilesDir();
        String path = topDir.getPath() + "/smart-history-tours/scenes";
        File dir = new File(path);
        Bitmap bm = BitmapFactory.decodeFile(dir.toString() + File.separator + scene.getName() + ".png");
        sceneView.setImageBitmap(bm);

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
        stop.setBackgroundResource(R.drawable.stop_marker);

        final Mapstop mapstop = coordinate.getMapstop();
        stop.setId((int)mapstop.getId());
        int stopNo = 0;
        for (Mapstop tMapstop : coordinate.getScene().getMapstops()) {
            stopNo++;
            if (tMapstop.getId() == mapstop.getId()) {
                break;
            }
        }

        stop.setText(String.valueOf(stopNo));
        stop.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        stop.setTypeface(stop.getTypeface(), Typeface.BOLD);
        stop.setPadding(0, 0, 0, 0);
        stop.setGravity(Gravity.CENTER_HORIZONTAL);
        stop.setTextColor(Color.BLACK);

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

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(120, 120);
        float x = coordinate.getX();
        float y = coordinate.getY();
        float left = x / z - 60;
        float top = y / z - 120;
        params.setMargins((int)left, (int)top, 0, 0);
        stop.setLayoutParams(params);

        if (stopNo == 1) {
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
}
