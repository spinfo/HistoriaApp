package de.historia_app.data;

import de.historia_app.ErrUtil;

public abstract class UrlSchemes {

    private static final String LOG_TAG = UrlSchemes.class.getSimpleName();

    // todo : change back
    public static final String SERVER_BASE_URI =
            //"https://historia-app.de/wp-content/uploads/smart-history-tours";
            //"http://historia.ililil.co/wp-content/uploads/smart-history-tours";
            "http://10.0.2.2:8000/wp-content/uploads/smart-history-tours";

    public static final String AVAILABLE_TOURS_URL = SERVER_BASE_URI + "/tours.v2.yaml";

    public static final String LEXICON = "lexicon://";

    public static final String FILE = "file://";

    public static long parseLexiconEntryIdFromUrl(String url) {
        if(url.startsWith(LEXICON)) {
            try {
                String idString = url.replace(LEXICON, "");
                return Long.parseLong(idString);
            } catch (NumberFormatException e) {
                // Do nothing. Handle error below.
            }
        }
        ErrUtil.failInDebug(LOG_TAG, "Invalid lexicon url: " + url);
        return 0L;
    }

}
