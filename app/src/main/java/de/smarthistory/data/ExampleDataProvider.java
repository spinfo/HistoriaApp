package de.smarthistory.data;

import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A class to provide example data
 */
class ExampleDataProvider {

    private static final Logger LOGGER = Logger.getLogger(ExampleDataProvider.class.getName());

    private static final String EXAMPLE_DATA_FILE = "res/raw/example_data.json";

    private Lexicon lexicon = new Lexicon();

    private List<LexiconEntry> lexiconEntries = new ArrayList<>();

    private Map<Long, LexiconEntry> lexiconEntriesById = new HashMap<>();

    private boolean assetsPrepared = false;

    ExampleDataProvider() {

        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(EXAMPLE_DATA_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder json = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            Gson gson = new Gson();
            JsonObject data = gson.fromJson(json.toString(), JsonElement.class).getAsJsonObject();

            JsonArray jLexiconEntries = data.getAsJsonArray("lexicon");
            for (int i = 0; i < jLexiconEntries.size(); i++) {
                JsonObject jLexiconEntry = jLexiconEntries.get(i).getAsJsonObject();
                long id = jLexiconEntry.get("id").getAsLong();
                String entryTitle = jLexiconEntry.get("title").getAsString();
                String content = jLexiconEntry.get("content").getAsString();

                LexiconEntry entry = new LexiconEntry(id, entryTitle, content);
                lexiconEntries.add(entry);
                lexicon.addEntry(entry);
                lexiconEntriesById.put(id, entry);
            }

        } catch(Exception e) {
            Log.e("data", "Error while reading example data: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }


    protected Lexicon getLexicon() {
        return lexicon;
    }

    protected List<LexiconEntry> getLexiconEntries() {
        return lexiconEntries;
    }

    protected LexiconEntry getLexiconEntryById(long id) {
        return lexiconEntriesById.get(id);
    }

    // This is a temporary hack
    // moving assets to the external dir to get video/audio to play (uris to external dir are
    // hardcoded in the html of mapstop views, breaks on older devices)
    // TODO replace temporary hack with something robust once we have a better data model
    protected void prepareAssets(AssetManager assetManager, File externalDir) {
        if (!assetsPrepared) {
            Log.d("data", "preparing assets...");
            copyAssets(assetManager, externalDir);
            assetsPrepared = true;
        }
    }

    private void copyAssets(AssetManager assetManager, File externalDir) {
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("data", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(externalDir, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                // NOOP
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
