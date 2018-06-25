package de.historia_app.data;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import java.net.HttpURLConnection;

import de.historia_app.ErrUtil;

/**
 * This basically is the reference implementation from:
 *      https://developer.android.com/training/basics/network-ops/connecting.html
 *
 * but extended to use
 *
 * TODO: This can be greatly simplified
 */
public class DownloadFileTask extends AsyncTask<String, Void, DownloadFileTask.Result> {

    private static final String LOG_TAG = DownloadFileTask.class.getSimpleName();

    private DownloadCallback<File> mCallback;

    private int readTimeoutMilliseconds;

    private int maxBytes;

    private File saveDir;

    public DownloadFileTask(DownloadCallback<File> callback, File saveDir, int readTimeoutMilliseconds, int maxBytes) {
        this.readTimeoutMilliseconds = readTimeoutMilliseconds;
        this.maxBytes = maxBytes;
        this.saveDir = saveDir;
        setCallback(callback);
    }

    private void setCallback(DownloadCallback<File> callback) {
        mCallback = callback;
    }

    /**
     * Wrapper class that serves as a union of a result value and an exception. When the download
     * task has completed, either the result value or exception can be a non-null value.
     * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
     */
    static class Result {
        File mResultValue;
        Exception mException;
        Result(File resultValue) {
            mResultValue = resultValue;
        }
        Result(Exception exception) {
            mException = exception;
        }
    }

    /**
     * Cancel background network operation if we do not have network connectivity.
     */
    @Override
    protected void onPreExecute() {
        if (mCallback != null) {
            NetworkInfo networkInfo = mCallback.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected() ||
                    (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                            && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                // If no connectivity, cancel task and update Callback with null data.
                mCallback.updateFromDownload(null);
                cancel(true);
            }
        }
    }

    /**
     * Defines work to perform on the background thread.
     */
    @Override
    protected DownloadFileTask.Result doInBackground(String... urls) {
        Result result = null;
        if (!isCancelled() && urls != null && urls.length > 0) {
            // todo : remove
            String urlString = urls[0].replace("localhost", "10.0.2.2");
            try {
                File resultFile = downloadUrl(urlString, this.readTimeoutMilliseconds, this.maxBytes);
                if (resultFile != null) {
                    result = new Result(resultFile);
                } else {
                    throw new IOException("No response received.");
                }
            } catch(Exception e) {
                result = new Result(e);
            }
        }
        return result;
    }

    /**
     * Updates the DownloadCallback with the result.
     */
    @Override
    protected void onPostExecute(Result result) {
        if (result != null && mCallback != null) {
            if (result.mException != null) {
                // if an Exception was raised, log it and simply update the caller with null
                Log.e(LOG_TAG, "Download failed", result.mException);
                mCallback.updateFromDownload(null);
            } else if (result.mResultValue != null) {
                mCallback.updateFromDownload(result.mResultValue);
            }
            mCallback.finishDownloading();
        }
    }

    /**
     * Override to add special behavior for cancelled AsyncTask.
     */
    @Override
    protected void onCancelled(Result result) {
        super.onCancelled();
    }

    // hand the values for progress etc directly to the callback
    private void publishProgress(int progressCode, int percentComplete) {
        mCallback.onProgressUpdate(progressCode, percentComplete);
    }


    /**
     * Retrieve an uri (if it is on the backend) and return the backend's response as a
     * InputStream.
     *
     * The caller may then hand the stream to one of the readStramAs... methods of this class.
     *
     * @param urlStr The url to query.
     * @param readTimeoutMilliseconds Timeout in Milliseconds for reading the response.
     * @param maxBytes Maximum of bytes that will be read.
     * @return The response as an InputStream or null on error.
     */
    private File downloadUrl(String urlStr, int readTimeoutMilliseconds, int maxBytes)
            throws IOException {
        // do not download anything if it is not below the base url
        if(!urlStr.startsWith(UrlSchemes.SERVER_BASE_URI)) {
            ErrUtil.failInDebug(LOG_TAG, "Attempt to connect to foreign Url: " + urlStr);
            return null;
        }

        // todo : change back to https
        HttpURLConnection connection = null;
        InputStream stream = null;
        File result = null;
        try {
            // todo : back to https
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();

            // set timeouts
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(readTimeoutMilliseconds);

            // we do not POST, PUT or DELETE
            connection.setRequestMethod("GET");

            // communicate!
            connection.connect();
            publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS, 0);
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);

            result = saveToFile(stream);
        } catch (MalformedURLException e) {
            ErrUtil.failInDebug(LOG_TAG, "Bad url given: " + e.getMessage());
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    // save the input stream to a file updating the callback with a progress, return null on error
    private File saveToFile(InputStream stream) throws IOException {

        File result = null;
        OutputStream output = null;

        try {
            result = File.createTempFile("shtm", "", this.saveDir);
            output = new FileOutputStream(result);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = stream.read(data)) != -1) {
                // TODO: allow canceling with back button? , below might be relevant
                /*if (isCancelled()) {
                    input.close();
                    return null;
                }*/
                total += count;
                // publishing the progress....
                if (this.maxBytes > 0) // only if total length is known
                    publishProgress(DownloadCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS, (int) (total * 100 / this.maxBytes));
                output.write(data, 0, count);
            }
        } finally {
            if (output != null) {
                output.close();
            }
        }
        return result;
    }

}
