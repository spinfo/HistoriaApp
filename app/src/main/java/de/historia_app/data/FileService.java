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
import java.util.List;

import de.historia_app.ErrUtil;

public class FileService {

    private static final String LOG_TAG = FileService.class.getSimpleName();

    private static final String TOUR_FILE_PREFIX = "shtm-tour-";

    private static final String TOUR_DELETE_FOLDER_PREFIX = "shtm-tour-delete-";

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
        return createDirectoryIfNotExists(mainContentDir);
    }

    private boolean createDirectoryIfNotExists(File file) {
        try {
            if(!file.exists()) {
                return file.mkdir();
            }
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Error while creating directory at '" + file.getPath() + "': " + e.getMessage());
            return false;
        }
        return (file.exists() && file.isDirectory());
    }

    public boolean initializeExampleDataIfNeeded() {
        DataFacade data = new DataFacade(context);
        if (data.getDefaultTour() == null) {
            return initializeExampleData();
        } else {
            return true;
        }
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

    public File determineSaveLocation(Mediaitem mediaitem) {
        File guidFile = new File(mediaitem.getGuid());
        return getFile(guidFile.getName());
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

        return result;
    }

    public boolean removeTour(Tour tour) {

        DataFacade data = new DataFacade(context);
        DatabaseTourInstaller installer = new DatabaseTourInstaller(context);

        File tmpDir = moveTourFilesToDeletionLocation(tour);
        boolean dbDeleteOK = installer.safeDeleteTour(tour);
        if (!dbDeleteOK) {
            // restore saved files here
            return false;
        }

        // deleteFile(tmpDir);
        initializeExampleDataIfNeeded();
        return true;
    }

    // Recursive delete to easily clean directories before deleting themselves
    private static boolean deleteFile(File element) {
        if (element.listFiles() != null) {
            for (File sub : element.listFiles()) {
                deleteFile(sub);
            }
        }
        return element.delete();
    }

    private File moveTourFilesToDeletionLocation(Tour tour) {
        File tmpDir = null;
        try {
            tmpDir = createTempDir(TOUR_DELETE_FOLDER_PREFIX, ".tmp");
            List<Mediaitem> mediaitems = (new DataFacade(context)).getMediaitemsFor(tour);
            for (Mediaitem mediaitem : mediaitems) {
                File old = determineSaveLocation(mediaitem);
                boolean ok = old.renameTo(new File(tmpDir, old.getName()));
                if (!ok) {
                    Log.w(LOG_TAG, "Unable to move file '" + old.getPath() + "' to temp dir.");
                } else {
                    Log.d(LOG_TAG, "Moved file: " + old.getName());
                }
            }

        } catch (IOException e) {
            Log.d(LOG_TAG, "Failed to move tour files to deletion directory: " + e.getMessage());
        }
        return tmpDir;
    }

    private File createTempDir(String prefix, String suffix) throws  IOException {
        final File temp;
        temp = File.createTempFile(prefix, null, context.getCacheDir());
        if(!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }
        if(!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        return temp;
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
