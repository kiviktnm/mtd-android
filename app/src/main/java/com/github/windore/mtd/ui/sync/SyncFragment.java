package com.github.windore.mtd.ui.sync;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.windore.mtd.MainActivity;
import com.github.windore.mtd.Mtd;
import com.github.windore.mtd.R;
import com.github.windore.mtd.databinding.FragmentSyncBinding;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SyncFragment extends Fragment {

    private FragmentSyncBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSyncBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext();
        if (context != null) {
            SharedPreferences preferences = context.getSharedPreferences("sync", Context.MODE_PRIVATE);
            binding.editTextPassword.setText(preferences.getString("password", ""));
            binding.editTextServerAddress.setText(preferences.getString("socketAddr", ""));
        }

        binding.editTextPassword.addTextChangedListener(new SyncTextWatcher("password"));
        binding.editTextServerAddress.addTextChangedListener(new SyncTextWatcher("socketAddr"));

        binding.buttonSync.setOnClickListener(view1 -> {
            MainActivity activity = (MainActivity) requireActivity();
            sync(activity, activity.getMtd());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressWarnings("deprecation")
    private void sync(Activity activity, Mtd mtd) {
        // To my knowledge the reason for the deprecation of ProgressDialog is the fact that
        // loading should block user actions. Blocking user actions however, is the easy way to
        // implement the following functionality and for this version of the app. It is perfectly ok.
        ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setTitle(getString(R.string.syncing));
        dialog.setMessage(getString(R.string.wait_for_sync));
        dialog.setCancelable(false);
        dialog.show();

        // Run the task async so the ui doesn't freeze
        Supplier<String> syncTask = () -> mtd.sync(
                binding.editTextPassword.getText().toString(),
                binding.editTextServerAddress.getText().toString()
        );
        CompletableFuture<String> task = CompletableFuture.supplyAsync(syncTask);

        // The UI is blocked because now I don't need to worry about what goes on in the list fragment
        // (no new items are added / removed)

        task.thenAccept(result -> activity.runOnUiThread(() -> {
            dialog.dismiss();
            String title;
            if (!"".equals(result))
                title = getString(R.string.sync_failed);
            else
                title = getString(R.string.sync_successful);

            new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(result)
                    .setPositiveButton(R.string.ok, ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .create()
                    .show();
        }));
    }

    private class SyncTextWatcher implements TextWatcher {
        private final String key;

        public SyncTextWatcher(String key) {
            this.key = key;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            Context context = getContext();
            if (context != null) {
                SharedPreferences preferences = context.getSharedPreferences("sync", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(key, editable.toString());
                editor.apply();
            }
        }
    }
}