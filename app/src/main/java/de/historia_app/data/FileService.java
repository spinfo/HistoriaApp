package de.historia_app.data;

import android.content.Context;
import android.util.Log;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import de.historia_app.ErrUtil;

public class FileService {

    private static final String LOG_TAG = FileService.class.getSimpleName();

    private static final String TOUR_FILE_PREFIX = "shtm-tour-";

    private Context context;

    private File mainContentDir;

    public FileService(Context context) {
        this.context = context;

        // initialize the main content directory if it does not exist
        boolean status = initializeMainContentDir();
        if (!status) {
            throw new RuntimeException("Could not create the main content directory.");
        }
    }

    public File getFile(String fileBaseName) {
        return new File(this.mainContentDir, fileBaseName);
    }

    private boolean initializeMainContentDir() {
        File topDir = context.getFilesDir();
        String path = topDir.getPath() + "/smart-history-tours";
        mainContentDir = new File(path);
        Log.i(LOG_TAG, "Using as main content dir: " + mainContentDir.getAbsolutePath());
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

    public boolean initializeExampleData() {
        try {
            // Read the assets file to a temp file
            File temp = File.createTempFile(TOUR_FILE_PREFIX, "", context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(temp);
            InputStream exampleIs = context.getAssets().open("example-tour.zip");
            copy(exampleIs, outputStream);
            outputStream.close();

            // Create a dummy tour record so that the tour file can be found
            TourRecord record = new TourRecord();
            record.setVersion(0L);
            record.setAreaId(0L);

            // hand everything to install
            this.installTour(temp, record);
        } catch(IOException e){
            ErrUtil.failInDebug(LOG_TAG, e);
            return false;
        }
        return true;
    }

    public Tour installTour(File file, TourRecord record) {
        Log.d(LOG_TAG, "received tour to install at: " + file.getAbsolutePath() + " (size: " + file.length() + ")");

        // Extract the zip archive to the main content dir
        try {
            ZipFile zip = new ZipFile(file);
            zip.extractAll(this.mainContentDir.getAbsolutePath());
        } catch (ZipException e) {
            Log.e(LOG_TAG, "Error reading input zip file at: " +  file.getAbsolutePath());
            ErrUtil.failInDebug(LOG_TAG, e);
        } finally {
            if(!file.delete()) {
                Log.w(LOG_TAG, "Failed to delete the temporary tour archive");
            };
        }

        // There should now be a file with the tour's content, given by the tour id and version
        final DataFacade data = new DataFacade(this.context);
        Tour result = null;
        File tourFile = new File(getTourFilePath(record));
        if(tourFile.exists()) {
            try {
                FileInputStream stream = new FileInputStream(tourFile);
                Tour tour = ServerResponseReader.parseTour(stream);
                tour.setVersion(record.getVersion());
                if(data.saveTour(tour)) {
                    result = tour;
                    if(!data.saveLexiconEntries(tour.getLexiconEntries())) {
                        ErrUtil.failInDebug(LOG_TAG, "Failed to save lexicon entries.");
                    }
                } else {
                    ErrUtil.failInDebug(LOG_TAG, "Failed to save tour.");
                }
            } catch (IOException e) {
                ErrUtil.failInDebug(LOG_TAG, "Failed to read tour file.");
            }
        } else {
            ErrUtil.failInDebug(LOG_TAG, "No tour file after unpacking the archive: " );
        }
        // TODO: The deletion of orphanded files should be handled here

        return result;
    }

    private String getTourFilePath(TourRecord record) {
        String filename = TOUR_FILE_PREFIX + record.getTourId() + "-" + record.getVersion() + ".yaml";
        return this.mainContentDir.getAbsolutePath() + '/' + filename;
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
    }

}