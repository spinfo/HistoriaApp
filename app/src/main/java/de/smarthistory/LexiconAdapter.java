package de.smarthistory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.smarthistory.data.Lexicon;
import de.smarthistory.data.LexiconEntry;

/**
 * A collection of letters (Character) and lexicon entries in a lexicon can be displayed with
 * this adapter.
 * TODO: Redo as a ExpandableListViewAdapter
 */
public class LexiconAdapter extends ArrayAdapter<Object> {

    private Context context;

    private LayoutInflater inflater;

    /**
     * Public constructor. Call on a lexicon with:
     *  new LexiconAdapter(context, LexiconAdapter.makeData(lexicon))
     *
     * @param context The context to use
     * @param data An array of Objects (Character and LexiconEntry objects in order)
     */
    public LexiconAdapter(Context context, ArrayList<Object> data) {
        super(context, 0, data);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * This will always return a TextView but dynamically decides on which view to use for the
     * object in the lexicon data. (Letters getting a different view than Titles)
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // the object to be displayed
        Object obj = getItem(position);

        if (obj == null) {
            throw new IllegalStateException("null object in lexicon data.");
        }

        String displayString;
        TextView displayTextView;

        if(Character.class.equals(obj.getClass())) {
            if (convertView == null || (convertView.getId() != R.id.lexicon_list_item_letter)) {
                convertView = inflater.inflate(R.layout.lexicon_list_item_letter, parent, false);
            }
            displayTextView = (TextView) convertView;
            displayString = ((Character) obj).toString();

        } else if (LexiconEntry.class.equals(obj.getClass())) {
            if (convertView == null || (convertView.getId() != R.id.lexicon_list_item_title)) {
                convertView = inflater.inflate(R.layout.lexicon_list_item_title, parent, false);
            }
            displayTextView = (TextView) convertView;
            displayString = ((LexiconEntry) obj).getTitle();
        } else {
            throw new IllegalStateException("Object in lexicon data is neither Character nor LexiconEntry.");
        }

        displayTextView.setText(displayString);
        return displayTextView;
    }

    /**
     * Method to be used by client to construct a suitable data array for this adapter from a
     * Lexicon object.
     * @param lexicon The lexicon to use
     * @return An array of Objects of class Character for lexicon's letters and class LexiconEntry
     * for the entries starting with the letter.
     */
    public static ArrayList<Object> makeData(Lexicon lexicon) {
        ArrayList<Object> data = new ArrayList<Object>();

        for (Character letter : lexicon.getLetters()) {
            data.add(letter);
            data.addAll(lexicon.getEntriesForLetter(letter));
        }

        return data;
    }
}
