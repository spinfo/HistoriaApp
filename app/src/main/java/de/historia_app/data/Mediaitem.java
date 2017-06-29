package de.historia_app.data;

import com.j256.ormlite.field.DatabaseField;

/**
 * A mediaitem, i.e. some kind of (audio, video etc.) file which is displayed on a single mapstop
 * page.
 */
public class Mediaitem {

    // a simple generated id for the item
    @DatabaseField(columnName = "id", generatedId = true)
    private long id;

    // a url (wordpress guid) via which the mediaitem may be downloaded
    // notice that this is a unique identifier for the backend, but it may exist multiple times in
    // the app's database (if multiple mapstops use the same mediaitem)
    // It should however only exist once for each page (ensured by the unique index on both
    @DatabaseField(columnName = "guid", uniqueIndexName = "page_mediaguid_index")
    private String guid;

    // the page this mediaitem belongs to
    @DatabaseField(columnName = "page", foreign = true, uniqueIndexName = "page_mediaguid_index")
    private Page page;

    protected Mediaitem() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }
}
