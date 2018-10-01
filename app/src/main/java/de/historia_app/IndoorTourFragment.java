package de.historia_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.historia_app.data.DataFacade;
import de.historia_app.data.Tour;

public class IndoorTourFragment extends Fragment implements MainActivity.MainActivityFragment {

    private static final String LOG_TAG = IndoorTourFragment.class.getSimpleName();

    private DataFacade data;

    private FrameLayout indoorFragmentView;
    private HorizontalScrollView scrollView;
    private ImageView sceneView;
    private TextView sceneNo;
    private RelativeLayout coordinateContainer;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private ImageButton closeButton;
    private MapPopupManager popupManager;
    private Tour tour;
    private SceneLoader sceneLoader;
    private RelativeLayout swipeContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        this.data = new DataFacade(this.getContext());
        this.tour = (Tour) getArguments().get("tour");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        indoorFragmentView = (FrameLayout) inflater.inflate(R.layout.fragment_indoor_tour, container, false);
        scrollView = (HorizontalScrollView) indoorFragmentView.findViewById(R.id.scroll_view);
        sceneNo = (TextView) indoorFragmentView.findViewById(R.id.scene_no);
        sceneView = (ImageView) indoorFragmentView.findViewById(R.id.scene);
        coordinateContainer = (RelativeLayout) indoorFragmentView.findViewById(R.id.coordinate_container);
        previousButton = (ImageButton) indoorFragmentView.findViewById(R.id.previous_button);
        nextButton = (ImageButton) indoorFragmentView.findViewById(R.id.next_button);
        closeButton = (ImageButton) indoorFragmentView.findViewById(R.id.close_button);
        swipeContainer = (RelativeLayout) indoorFragmentView.findViewById(R.id.swipe_container);

        popupManager = new MapPopupManager(indoorFragmentView);

        sceneLoader = new SceneLoader(tour, sceneView, scrollView, coordinateContainer, sceneNo, popupManager);

        if (savedInstanceState != null && savedInstanceState.containsKey("current_index")) {
            int currentIndex = (int) savedInstanceState.get("current_index");
            if (currentIndex > 0) {
                sceneLoader.loadScene(currentIndex);

                previousButton.setVisibility(View.VISIBLE);
                if (currentIndex == tour.getScenes().size() - 1) {
                    nextButton.setVisibility(View.GONE);
                }
            }
        }

        if (savedInstanceState == null) {
            swipeContainer.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setVisibility(View.GONE);
                }
            }, 2000);
        }

        /*
         * Causes some trouble
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    //previousButton.setVisibility(View.GONE);
                    //nextButton.setVisibility(View.GONE);
                    //closeButton.setVisibility(View.GONE);
                    final RelativeLayout buttonContainer = (RelativeLayout) indoorFragmentView.findViewById(R.id.button_container);
                    buttonContainer.setVisibility(View.GONE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //previousButton.setVisibility(View.VISIBLE);
                            //nextButton.setVisibility(View.VISIBLE);
                            //closeButton.setVisibility(View.VISIBLE);
                            buttonContainer.setVisibility(View.VISIBLE);
                        }
                    }, 500);
                }
            });
        }
        */

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sceneLoader.changeScene(-1);

                if (sceneLoader.getCurrentIndex() == 0) {
                    previousButton.setVisibility(View.GONE);
                }
                if (sceneLoader.getCurrentIndex() < tour.getScenes().size() - 1) {
                    nextButton.setVisibility(View.VISIBLE);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sceneLoader.changeScene(1);

                if (sceneLoader.getCurrentIndex() > 0) {
                    previousButton.setVisibility(View.VISIBLE);
                }
                if (sceneLoader.getCurrentIndex() == tour.getScenes().size() - 1) {
                    nextButton.setVisibility(View.GONE);
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Wollen Sie die Tour wirklich verlassen?");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        ((MainActivity)getActivity()).switchMainFragmentToMap(false);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return indoorFragmentView;
    }

    @Override
    public boolean reactToBackButtonPressed() {
        return false;
    }

    private SharedPreferences getPrefs() {
        return getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("current_index", sceneLoader.getCurrentIndex());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
