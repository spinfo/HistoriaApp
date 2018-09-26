package de.historia_app;

import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import de.historia_app.data.DataFacade;
import de.historia_app.data.LexiconEntry;
import de.historia_app.data.UrlSchemes;

public class UrlSchemeRedirectingWebViewClient extends WebViewClient {

    private static final String LOG_TAG = UrlSchemeRedirectingWebViewClient.class.getSimpleName();

    private final Context context;

    UrlSchemeRedirectingWebViewClient(Context context) {
        this.context = context;
    }
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url == null) {
            Log.w(LOG_TAG, "Request to load url that is null.");
            return false;
        }

        if (url.startsWith(UrlSchemes.LEXICON)) {
            Long id = UrlSchemes.parseLexiconEntryIdFromUrl(url);
            if(id != 0L) {
                LexiconEntry entry = (new DataFacade(context)).getLexiconEntryById(id);
                if(entry != null) {
                    context.startActivity(newLexiconEntryPageViewIntent(entry));
                    return true;
                } else {
                    ErrUtil.failInDebug(LOG_TAG, "No lexicon article for id: " + id);
                }
            } else {
                ErrUtil.failInDebug(LOG_TAG, "Cannot load lexicon article from invalid url: " + url);
            }
            return false;
        } else if (url.startsWith("mailto:")) {
            context.startActivity(newEmailIntent(url));
            return true;
        } else {
            // Redirect everything else to the browser
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }
    }

    private Intent newEmailIntent(String url) {
        MailTo mt = MailTo.parse(url);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { mt.getTo() });
        intent.putExtra(Intent.EXTRA_TEXT, mt.getBody());
        intent.putExtra(Intent.EXTRA_SUBJECT, mt.getSubject());
        intent.putExtra(Intent.EXTRA_CC, mt.getCc());
        intent.setType("message/rfc822");
        return Intent.createChooser(intent, context.getString(R.string.choose_mail_program_title));
    }

    private Intent newLexiconEntryPageViewIntent(LexiconEntry entry) {
        Intent intent = new Intent(context, SimpleWebViewActivity.class);
        intent.putExtra(context.getString(R.string.extra_key_simple_web_view_data), entry.getContent());
        return intent;
    }
}
