package de.historia_app.data;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.List;

import de.historia_app.R;


public class HtmlContentCompletion {

    private static final String LOG_TAG = HtmlContentCompletion.class.getSimpleName();

    private static final String CONTENT_GOES_GERE = "43608653-7f0c-4229-853d-3c7b3c0ecf7a";
    private static final String CSS_GOES_HERE = "d79ee39f-9107-44dc-a740-58cc3edc2e7f";
    private static final String HEADING_GOES_HERE = "b3fc9141-fe1e-41b3-be30-43d1ea91ece8";

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
    private static final String LEXICON_HEADING_TEMPLATE =
            "<div style=\"text-align:center;margin-top:12px;\">" +
                "<hr>" +
                "<h1>" +
                    HEADING_GOES_HERE +
                "</h1>" +
                "<hr>" +
            "</div>";



    public static String wrapInPage(Context context, String innerHtml) {
        if(innerHtml == null) {
            innerHtml = "";
        }
        String styles = AssetHelper.readAsset(context, context.getString(R.string.asset_app_article_styles));
        String withStyles = HTML_TEMPLATE.replaceFirst(CSS_GOES_HERE, styles);
        return withStyles.replaceFirst(CONTENT_GOES_GERE, innerHtml);
    }

    public static String replaceMediaitems(Context context, String content, List<Mediaitem> media) {
        if(media == null || media.isEmpty()) {
            return content;
        }

        FileService fileService = new FileService(context);
        for(Mediaitem mediaitem : media) {
            File file = fileService.determineSaveLocation(mediaitem);
            if(file.exists()) {
                String replacement = UrlSchemes.FILE + file.getAbsolutePath();
                Log.d(LOG_TAG, "Replacing: '" + mediaitem.getGuid() + "' with '" + replacement + "'");
                content = content.replaceAll(mediaitem.getGuid(), replacement);
            } else {
                Log.w(LOG_TAG, "No file for mediaitem: " + mediaitem.getGuid());
            }
        }
        return content;
    }

    public static String prefixTitleHtml(String content, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(LEXICON_HEADING_TEMPLATE.replaceFirst(HEADING_GOES_HERE, title));
        sb.append(content);
        return sb.toString();
    }

}
