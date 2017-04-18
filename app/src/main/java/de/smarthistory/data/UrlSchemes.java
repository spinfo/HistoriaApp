package de.smarthistory.data;

import android.util.Log;

import de.smarthistory.ErrUtil;

public abstract class UrlSchemes {

    public static final String LEXICON = "lexicon://";

    public static long parseLexiconEntryIdFromUrl(String url) {
        if(url.startsWith(LEXICON)) {
            try {
                String idString = url.replace(LEXICON, "");
                return Long.parseLong(idString);
            } catch (NumberFormatException e) {
                // Do nothing. Handle error below.
            }
        }
        ErrUtil.failInDebug("Invalid lexicon url: " + url);
        return 0L;
    }

}
