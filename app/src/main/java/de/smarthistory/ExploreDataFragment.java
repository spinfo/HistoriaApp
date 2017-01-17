package de.smarthistory;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnExploreDataFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExploreDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExploreDataFragment extends Fragment {

    class TabConfig {
        String name;
        int viewId;

        TabConfig(String name, int viewId) {
            this.name = name;
            this.viewId = viewId;
        }
    }

    private TabConfig[] tabConfigs = {
            new TabConfig("Touren", R.id.tab1),
            new TabConfig("Mapstops A-Z", R.id.tab2),
            new TabConfig("Lexikon", R.id.tab3)
    };

    private OnExploreDataFragmentInteractionListener mListener;

    public ExploreDataFragment() {
        // Required empty public constructor
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

        TabHost tabHost = (TabHost) result.findViewById(R.id.tabHost);
        tabHost.setup();

        for (TabConfig tabConfig : tabConfigs) {
            TabSpec tabSpec = tabHost.newTabSpec(tabConfig.name);
            tabSpec.setContent(tabConfig.viewId);
            tabSpec.setIndicator(tabConfig.name);
            tabHost.addTab(tabSpec);
        }

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
