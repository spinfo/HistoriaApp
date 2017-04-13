package de.smarthistory.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple POJO for a page displayed for a mapstop
 */
public class Page {

    // the page's position in a series of pages
    private int pos;

    // the unique url that identifies this page on the server
    private String guid;

    // the page's html content
    private String content;

    // a list of guids (server urls) of the page's media items
    private List<String> media = new ArrayList<String>();

    // empty constructor needed for YAML parsing
    protected Page() {}

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

    public List<String> getMedia() {
        return media;
    }

    public void setMedia(List<String> media) {
        this.media = media;
    }
}
