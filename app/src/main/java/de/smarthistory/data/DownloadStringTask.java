package de.smarthistory.data;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import de.smarthistory.ErrUtil;

/**
 * This basically is the reference implementation from:
 *      https://developer.android.com/training/basics/network-ops/connecting.html
 *
 * TODO: This can be greatly simplified
 */
public class DownloadStringTask extends AsyncTask<String, Void, DownloadStringTask.Result> {

    private static final String LOG_TAG = DownloadStringTask.class.getSimpleName();

    private DownloadCallback<String> mCallback;

    private int readTimeoutMilliseconds;

    private int maxBytes;

    public DownloadStringTask(DownloadCallback<String> callback, int readTimeoutMilliseconds, int maxBytes) {
        this.readTimeoutMilliseconds = readTimeoutMilliseconds;
        this.maxBytes = maxBytes;
        setCallback(callback);
    }

    private void setCallback(DownloadCallback<String> callback) {
        mCallback = callback;
    }

    /**
     * Wrapper class that serves as a union of a result value and an exception. When the download
     * task has completed, either the result value or exception can be a non-null value.
     * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
     */
    static class Result {
        String mResultValue;
        Exception mException;
        Result(String resultValue) {
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
    protected DownloadStringTask.Result doInBackground(String... urls) {
        Result result = null;
        if (!isCancelled() && urls != null && urls.length > 0) {
            String urlString = urls[0];
            try {
                String resultString = downloadUrl(urlString, this.readTimeoutMilliseconds, this.maxBytes);
                if (resultString != null && !resultString.isEmpty()) {
                    result = new Result(resultString);
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
    private String downloadUrl(String urlStr, int readTimeoutMilliseconds, int maxBytes)
            throws IOException {
        // do not download anything if it is not below the base url
        if(!urlStr.startsWith(UrlSchemes.SERVER_BASE_URI)) {
            ErrUtil.failInDebug(LOG_TAG, "Attempt to connect to foreign Url: " + urlStr);
            return "";
        }

        HttpsURLConnection connection = null;
        InputStream stream = null;
        String result = "";
        try {
            URL url = new URL(urlStr);
            connection = (HttpsURLConnection) url.openConnection();

            // set timeouts
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(readTimeoutMilliseconds);

            // we do not POST, PUT or DELETE
            connection.setRequestMethod("GET");

            // communicate!
            connection.connect();
            publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS, 0);
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);

            result = saveToString(stream, maxBytes);
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

    // save the input stream to a string updating the callback with a progress, return empty string on error
    public String saveToString(InputStream stream, int maxBytes) throws IOException {
        String result = null;

        if (stream == null) {
            Log.e(LOG_TAG, "Received empty input strem");
            return null;
        }

        // Read InputStream using the UTF-8 charset.
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
        // Create temporary buffer to hold Stream data with specified max length.
        char[] buffer = new char[maxBytes];
        // Populate temporary buffer with Stream data.
        int numChars = 0;
        int readSize = 0;
        while (numChars < maxBytes && readSize != -1) {
            numChars += readSize;
            int percent = (100 * numChars) / maxBytes;
            publishProgress(DownloadCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS, percent);
            readSize = reader.read(buffer, numChars, buffer.length - numChars);
        }
        if (numChars != -1) {
            // The stream was not empty.
            // Create String that is actual length of response body if actual length was less than
            // max length.
            numChars = Math.min(numChars, maxBytes);
            result = new String(buffer, 0, numChars);
        }

        return result;
    }

}
