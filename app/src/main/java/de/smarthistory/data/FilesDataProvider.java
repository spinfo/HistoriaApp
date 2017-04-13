package de.smarthistory.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FilesDataProvider {

    private static final String LOG_TAG = FilesDataProvider.class.getName();

    private Context context;

    private File mainContentDir;

    protected FilesDataProvider(Context context) {
        this.context = context;

        // initialize the main content directory if it does not exist
        boolean status = initializeMainContentDir();
        if (!status) {
            throw new RuntimeException("Could not create the main content directory.");
        }

        // initialize the example tour if the main content dir is empty
        if (mainContentDir.list() == null || mainContentDir.list().length == 0) {
            status = initializeExampleData();
            if(!status) {
                throw new RuntimeException("Could not initialize the example data.");
            }
        }
    }

    // TODO: Remove this
    public static void testRun(Context context) {
        FilesDataProvider provider = new FilesDataProvider(context);
    }

    private boolean initializeMainContentDir() {
        // initialize the main content directory if it does not exist
        File externalDir = Environment.getExternalStorageDirectory();
        String path = externalDir.getPath() + "/smart-history-tours";
        mainContentDir = new File(path);
        try {
            if(!mainContentDir.exists()) {
                mainContentDir.mkdir();
            }
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Error while creating main content dir: " + e.getMessage());
            return false;
        }
        return (mainContentDir.exists() && mainContentDir.isDirectory());
    }

    private boolean initializeExampleData() {
        try {
            InputStream exampleIs = context.getAssets().open("example-tour.yaml");
            Tour result = TourDeserialiser.parseTour(exampleIs);
        } catch(IOException e){
            Log.e(LOG_TAG, "Error while creating the example tour: " + e.getMessage());
            return false;
        }
        return true;
    }

}
