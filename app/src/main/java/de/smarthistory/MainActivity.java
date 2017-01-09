package de.smarthistory;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());

    public final static String EXTRA_MESSAGE = "de.smarthistory.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Called when the user clicks the Send button
     **/
    public void sendMessage(View view) {
        LOGGER.info("starting message activity");

        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString().replace("e", "ü");
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    /**
     * Called when the user clicks the Map button
     */
    public void switchToMap(View view) {
        Intent intent = new Intent(this, MapViewActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user clics the Fragments button
     */
    public void switchToFragmentsExample(View view) {
        LOGGER.info("starting frags activity");

        Intent intent = new Intent(this, FragmentsExampleActivity.class);
        startActivity(intent);
    }
}
