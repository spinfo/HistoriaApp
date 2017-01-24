package de.smarthistory;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import java.util.ArrayList;
import java.util.List;

import de.smarthistory.data.DataFacade;
import de.smarthistory.data.Tour;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnExploreDataFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ExploreDataFragment extends Fragment {

    private DataFacade data;

    private OnExploreDataFragmentInteractionListener mListener;

    public ExploreDataFragment() {
        this.data = DataFacade.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_explore_data, container, false);

        // setup the tab host
        TabHost tabHost = (TabHost) result.findViewById(R.id.tabHost);
        tabHost.setup();

        // Tab1: Lexikon
        TabSpec tabSpec1 = tabHost.newTabSpec("Lexikon");
        tabSpec1.setContent(R.id.tab1);
        tabSpec1.setIndicator("Lexikon");
        tabHost.addTab(tabSpec1);

        // Tab2: Lesezeichen
        TabSpec tabSpec2 = tabHost.newTabSpec("Lesezeichen");
        tabSpec2.setContent(R.id.tab2);
        tabSpec2.setIndicator("Lesezeichen");
        tabHost.addTab(tabSpec2);

        // Tab3: Touren
        TabSpec tabSpec3 = tabHost.newTabSpec("Touren");
        tabSpec3.setContent(R.id.tab3);
        tabSpec3.setIndicator("Touren");
        tabHost.addTab(tabSpec3);

        List<Tour> tourData = data.getCurrentArea().getTours();
        Tour[] tours = tourData.toArray(new Tour[tourData.size()]);
        TourArrayAdapter toursAdapter = new TourArrayAdapter(getContext(), tours);
        ListView tourList = (ListView) result.findViewById(R.id.tour_list);
        tourList.setAdapter(toursAdapter);

        return result;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onExploreDataFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnExploreDataFragmentInteractionListener) {
            mListener = (OnExploreDataFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnExploreDataFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnExploreDataFragmentInteractionListener {
        // TODO: Update argument type and name
        void onExploreDataFragmentInteraction(Uri uri);
    }
}
