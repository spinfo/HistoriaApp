package de.historia_app.data;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.historia_app.App;
import de.historia_app.R;


public class HtmlContentCompletion {

    private static final String LOG_TAG = HtmlContentCompletion.class.getSimpleName();

    private static final String CONTENT_GOES_GERE = "43608653-7f0c-4229-853d-3c7b3c0ecf7a";
    private static final String CSS_GOES_HERE = "d79ee39f-9107-44dc-a740-58cc3edc2e7f";
    private static final String HTML_TEMPLATE =
            "<html>" +
                "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>\n" +
                        CSS_GOES_HERE +
                    "</style>\n" +
                "</head>" +
                "<body>" +
                    CONTENT_GOES_GERE +
                "</body>" +
            "</html>";


    public static String wrapInPage(String innerHtml) {
        if(innerHtml == null) {
            innerHtml = "";
        }
        String styles = AssetHelper.readAsset(App.getContext().getString(R.string.asset_app_article_styles));
        String withStyles = HTML_TEMPLATE.replaceFirst(CSS_GOES_HERE, styles);
        return withStyles.replaceFirst(CONTENT_GOES_GERE, innerHtml);
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

    public static String setTitle(String content, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>");
        sb.append(title);
        sb.append("</h1>");
        sb.append(content);
        return sb.toString();
    }

}
