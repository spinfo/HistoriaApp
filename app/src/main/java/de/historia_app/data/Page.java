package de.historia_app.data;

import android.content.Context;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple POJO for a page displayed for a mapstop
 */
public class Page {

    private static final String LOG_TAG = Page.class.getSimpleName();

    // the server's id value for this page
    @DatabaseField(columnName = "id", id = true)
    private long id;

    // the unique url (wordpress guid) that identifies this page on the server
    @DatabaseField(columnName = "guid")
    private String guid;

    // the page's position in a series of pages
    @DatabaseField(columnName = "pos")
    private int pos;

    // the page's html content
    @DatabaseField(columnName = "content")
    private String content;

    // a list of guids (server urls) of the page's media items
    @ForeignCollectionField(columnName = "media", eager = true)
    private Collection<Mediaitem> media = new ArrayList<>();

    // the mapstop this page is meant for
    @DatabaseField(columnName = "mapstop", foreign = true)
    private Mapstop mapstop;

    // empty constructor needed for YAML parsing
    protected Page() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Mapstop getMapstop() {
        return mapstop;
    }

    public void setMapstop(Mapstop mapstop) {
        this.mapstop = mapstop;
    }

    public List<Mediaitem> getMedia() {
        return new ArrayList<>(media);
    }

    public void setMedia(List<Mediaitem> media) {
        this.media = media;
    }

    public String getPresentationContent(Context context) {
        String content = HtmlContentCompletion.replaceMediaitems(getContent(), getMedia(), context);
        return HtmlContentCompletion.wrapInPage(content);
    }
}
