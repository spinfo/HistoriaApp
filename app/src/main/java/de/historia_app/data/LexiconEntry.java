package de.historia_app.data;

import com.j256.ormlite.field.DatabaseField;

/**
 * Data object for a lexicon entry (the actual entry is a page of html without mediaitems).
 */
public class LexiconEntry {

    // the server's id value for this page
    @DatabaseField(columnName = "id", id = true)
    private long id;

    // the title of this lexicon page
    @DatabaseField(columnName = "title")
    private String title;

    // the html content of this lexicon page
    @DatabaseField(columnName = "content")
    private String content;

    // Default constructor for YAML parsing, db
    public LexiconEntry() {}

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
        final String content = HtmlContentCompletion.setTitle(this.content, this.title);
        return HtmlContentCompletion.wrapInPage(content);
    }
}
