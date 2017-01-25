package de.smarthistory.data;

/**
 * Data object for a lexicon entry (the actual entry is one page of html).
 */
public class LexiconEntry {

    private final long id;

    private final String title;

    public LexiconEntry(long id, String title) {
        this.id = id;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
