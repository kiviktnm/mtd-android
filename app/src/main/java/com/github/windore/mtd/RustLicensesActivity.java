package com.github.windore.mtd;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RustLicensesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String FILENAME = "rust-3rd-party-licenses.html";
        String TAG = "RustLicensesActivity";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rust_licenses);

        TextView licensesTextView = (TextView) findViewById(R.id.rust_licenses_tv);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(getAssets().open(FILENAME));
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line = reader.readLine();
            while (line != null) {
                builder.append(line).append('\n');
                line = reader.readLine();
            }

            reader.close();
            licensesTextView.setText(Html.fromHtml(builder.toString(), Html.FROM_HTML_MODE_COMPACT));
        } catch (IOException e) {
            Log.e(TAG, String.format("Failed to read asset file %s: %s", FILENAME, e.getMessage()));
            licensesTextView.setText(com.google.android.gms.oss.licenses.R.string.license_content_error);
        }
    }
}