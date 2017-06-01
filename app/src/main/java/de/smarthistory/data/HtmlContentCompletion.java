package de.smarthistory.data;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.List;


public class HtmlContentCompletion {

    private static final String LOG_TAG = HtmlContentCompletion.class.getSimpleName();

    // Since our HTML template is really small, we just use a static String for now
    private static final String REPLACE_ME = "to replace";
    private static final String HTML_TEMPLATE =
            "<html>" +
                "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>" +
                        "img { max-width: 100%; height: auto !important; }" +
                        "figure { max-width: 100%; margin: 1em 0 1em; }" +
                    "</style>" +
                "</head>" +
                "<body>" +
                    REPLACE_ME +
                "</body>" +
            "</html>";


    public static String wrapInPage(String innerHtml) {
        if(innerHtml == null) {
            innerHtml = "";
        }
        return HTML_TEMPLATE.replaceFirst(REPLACE_ME, innerHtml);
    }

    public static String replaceMediaitems(String content, List<Mediaitem> media, Context context) {
        if(media == null || media.isEmpty()) {
            return content;
        }

        FileService fileService = new FileService(context);
        for(Mediaitem mediaitem : media) {
            // Treat the mediaitem guid as a file to get the basename
            File guidFile = new File(mediaitem.getGuid());
            String base = guidFile.getName();

            if(base.isEmpty()) {
                Log.w(LOG_TAG, "Could not determine base for guid: " + mediaitem.getGuid());
            } else {
                File file = fileService.getFile(base);
                if(file.exists()) {
                    String replacement = UrlSchemes.FILE + file.getAbsolutePath();
                    Log.d(LOG_TAG, "Replacing: '" + mediaitem.getGuid() + "' with '" + replacement + "'");
                    content = content.replaceAll(mediaitem.getGuid(), replacement);
                } else {
                    Log.w(LOG_TAG, "No file for basename: " + base);
                }
            }
        }
        return content;
    }


}
