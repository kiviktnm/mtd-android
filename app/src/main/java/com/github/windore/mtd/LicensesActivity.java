package com.github.windore.mtd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

public class LicensesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        Button licensesBtn = (Button) findViewById(R.id.android_licenses_btn);
        licensesBtn.setOnClickListener(view -> startActivity(new Intent(this, OssLicensesMenuActivity.class)));
    }
}