package com.zalexdev.stryker;


import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.utils.CheckDir;
import com.zalexdev.stryker.utils.CheckFile;
import com.zalexdev.stryker.utils.CheckUpdates;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Updater extends Fragment {


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
                new Thread(() -> updater(update.get(2),update.get(3))).start();
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
    public void updater(String urlapk,String urlchroot){

        setText(status,"Deleting cache...");
        new CustomCommand("rm /storage/emulated/0/Download/stryker.apk", core).execute();
        new CustomCommand("rm /data/local/stryker.apk", core).execute();
        new CustomCommand("rm /storage/emulated/0/Download/stryker-chroot.tar.gz", core).execute();
        try {
            setText(status,"Downloading apk...");
            boolean apk = new CustomCommand(Core.BUSYBOX+"wget "+urlapk+" -O /storage/emulated/0/Download/stryker.apk",core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
            if (apk){
                setProg(progress,10);
                setText(status,"Downloading core...");
                boolean chroot = new CustomCommand(Core.BUSYBOX+"wget "+urlchroot+" -O /storage/emulated/0/Download/stryker-chroot.tar.gz",core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                if (chroot){
                    setProg(progress,30);
                    setText(status,"Unmounting core...");
                    Boolean un = unmount();
                    setProg(progress,40);
                    setText(status,"Deleting old core...");
                    setProg(progress,70);
                        boolean del = new CustomCommand("rm -rf /data/local/stryker",new Core(context)).execute().get();
                        if (del){
                            if (new CheckFile("/storage/emulated/0/Download/stryker-chroot.tar.gz").execute().get()){
                                setText(status,"Installing new core...");
                                setProg(progress,90);
                                new CustomCommand("chmod 777 -R /data/data/com.zalexdev.stryker/cache/", core).execute();
                                new CustomCommand("mkdir /storage/emulated/0/Stryker", core).execute();
                                new CustomCommand("mkdir /data/local/stryker", core).execute();
                                boolean untar = new CustomCommand("/data/data/com.zalexdev.stryker/cache/busybox tar -xf /storage/emulated/0/Download/stryker-chroot.tar.gz -C /data/local/stryker", core).execute().get();
                                if (untar){
                                    setProg(progress,100);
                                    new CustomCommand("rm /storage/emulated/0/Download/stryker-chroot.tar.gz",core).execute();
                                    new CustomCommand("cp /storage/emulated/0/Download/stryker.apk /data/local/stryker.apk", core).execute();
                                    new CustomCommand("pm install /data/local/stryker.apk&&sleep 2&&am start -n com.zalexdev.stryker/.MainActivity",core).execute();
                                    getActivity().runOnUiThread(() -> core.toaster("Updated!"));

                                }else{
                                    getActivity().runOnUiThread(() -> core.toaster("Failed!"));
                                }

                            }
                            }

                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }
    public boolean unmount() throws ExecutionException, InterruptedException {
        return new CustomCommand("/data/data/com.zalexdev.stryker/cache/killroot",new Core(context)).execute().get();
    }
}