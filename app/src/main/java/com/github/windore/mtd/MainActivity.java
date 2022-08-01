package com.github.windore.mtd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.windore.mtd.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MtdApp";
    private static final String FILENAME = "items.json";

    private Mtd mtd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String json = readFile();

        if (json.isEmpty()) {
            mtd = new Mtd();
        } else {
            mtd = new Mtd(json);
        }

        mtd.addObserver((__, ___) -> writeFile(mtd.toJson()));

        com.github.windore.mtd.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        Button licensesBtn = (Button) findViewById(R.id.licenses_btn);
        licensesBtn.setOnClickListener(view -> startActivity(new Intent(this, LicensesActivity.class)));

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_items_list, R.id.navigation_sync)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    public Mtd getMtd() {
        return mtd;
    }

    private String readFile() {
        FileInputStream fis;
        try {
            fis = openFileInput(MainActivity.FILENAME);
        } catch (FileNotFoundException e) {
            return "";
        }

        InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, String.format("Failed to read file %s: %s", MainActivity.FILENAME, e.getMessage()));
        }

        return stringBuilder.toString();
    }

    private void writeFile(String fileContents) {
        try (FileOutputStream fos = openFileOutput(MainActivity.FILENAME, Context.MODE_PRIVATE)) {
            fos.write(fileContents.getBytes());
        } catch (IOException e) {
            Log.e(TAG, String.format("Failed to write file %s: %s", MainActivity.FILENAME, e.getMessage()));
        }
    }
}