package de.historia_app.data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AssetHelper {

    private static final String TAG = AssetHelper.class.getSimpleName();

    public static String readAsset(Context context, String filename) {
        InputStream in = null;
        BufferedReader buffer = null;

        try {
            StringBuilder sb = new StringBuilder();
            in = context.getAssets().open(filename);
            buffer = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String str;
            while ((str = buffer.readLine()) != null) {
                sb.append(str);
            }
            buffer.close();
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "Failed to read the asset: " + filename);
            return "";
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (buffer != null) {
                    buffer.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not close input stream or buffer.");
            }
        }
    }

}
