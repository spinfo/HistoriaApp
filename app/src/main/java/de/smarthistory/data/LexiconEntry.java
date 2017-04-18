package de.smarthistory.data;

/**
 * Data object for a lexicon entry (the actual entry is one page of html).
 */
public class LexiconEntry {

    private final long id;

    private final String title;

    private final String content;

    public LexiconEntry(long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
