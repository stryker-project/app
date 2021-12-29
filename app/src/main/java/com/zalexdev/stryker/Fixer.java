package com.zalexdev.stryker;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.utils.CheckFile;
import com.zalexdev.stryker.utils.CheckUpdates;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Fixer extends Fragment {


    public Context context;
    public Core core;
    public  String versionName = BuildConfig.VERSION_NAME;
    public  int versionInt = BuildConfig.VERSION_CODE;
    public TextView status;
    public LinearProgressIndicator progress;
    @SuppressLint("Range")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.update, container, false);
        context = getContext();
        progress = viewroot.findViewById(R.id.prog_update);
       status = viewroot.findViewById(R.id.update_status);
        core = new Core(context);
        Thread f = new Thread(() -> {
            try {
                List<String> update = Arrays.asList(new CheckUpdates(getContext()).execute().get().split(","));
                new Thread(() -> updater(update.get(2))).start();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        f.start();

        return viewroot;

    }
    public void setText(TextView textView, String text){
        getActivity().runOnUiThread(() -> textView.setText(text));
    }
    public void setProg(LinearProgressIndicator progressIndicator, int prog){
        getActivity().runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);

            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(prog,true);
            }
        });

    }
    public static boolean is64Bit() {
        return (Build.SUPPORTED_64_BIT_ABIS!= null && Build.SUPPORTED_64_BIT_ABIS.length >0);
    }

    @SuppressLint("Range")
    public void updater(String urlapk){
try {
            setText(status,"Downloading apk...");
            setProg(progress,80);
            boolean apk = new CustomCommand(Core.BUSYBOX+"wget "+urlapk+" -O /storage/emulated/0/Download/stryker.apk",core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
            boolean cp = new CustomCommand("cp /storage/emulated/0/Download/stryker.apk /data/local/stryker.apk",core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
            if (apk){
                setProg(progress,100);
                setText(status,"Installing apk...");
                new CustomCommand("pm install /data/local/stryker.apk&&sleep 2&&am start -n com.zalexdev.stryker/.MainActivity",core).execute();
                                    getActivity().runOnUiThread(() -> core.toaster("Updated!"));

                                }else{
                                    getActivity().runOnUiThread(() -> core.toaster("Failed!"));
                                }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }

}