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
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.utils.CheckDir;
import com.zalexdev.stryker.utils.CheckFile;
import com.zalexdev.stryker.utils.CheckUpdates;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomChrootCommand;
import com.zalexdev.stryker.utils.CustomCommand;

import com.zalexdev.stryker.utils.DownloadFile;
import com.zalexdev.stryker.wifi.utils.GetInterfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Setup extends Fragment {


    public Context context;
    public Core core;


    @SuppressLint("Range")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.setup, container, false);
        context = getContext();
        TextView status = viewroot.findViewById(R.id.setup_status);
        TextView welcome = viewroot.findViewById(R.id.welcome);
        LinearProgressIndicator progressIndicator = viewroot.findViewById(R.id.prog_setup);
        core = new Core(context);
        boolean installed;
        boolean mounted;


            if (!is64Bit()){
                status.setVisibility(View.INVISIBLE);
                progressIndicator.setVisibility(View.INVISIBLE);
                welcome.setVisibility(View.VISIBLE);
                welcome.setText("Sorry, your device is 32bit and not supported yet!");
            }else {


                try {
                    installed = new CheckDir("/data/local/stryker/beta").execute().get();
                    if (!installed) {
                        new CustomCommand("rm /storage/emulated/0/Download/stryker.tar.gz", core).execute();
                        status.setOnClickListener(view -> {

                            String urlDownload = "https://github.com/stryker-project/stryker-chroot/releases/download/1.2/1.2B.tar.gz";
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlDownload));
                            request.setDescription("Please, wait...");
                            request.setTitle("Downloading core");
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "stryker.tar.gz");

                            final DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);

                            final long downloadId = manager.enqueue(request);
                            new Thread(() -> {

                                boolean downloading = true;

                                while (downloading) {

                                    DownloadManager.Query q = new DownloadManager.Query();
                                    q.setFilterById(downloadId);

                                    Cursor cursor = manager.query(q);
                                    cursor.moveToFirst();
                                    int bytes_downloaded = cursor.getInt(cursor
                                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                    final int dl_progress = (int) ((bytes_downloaded * 100L) / bytes_total);
                                    setText(status, bytes_downloaded / 1024 / 1024 + "MB/" + bytes_total / 1024 / 1024 + "MB (" + dl_progress + "%)");
                                    setProg(progressIndicator, dl_progress);
                                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                                        downloading = false;
                                        new CustomCommand("chmod 777 -R /data/data/com.zalexdev.stryker/cache/", core).execute();
                                        new CustomCommand("mkdir /storage/emulated/0/Stryker", core).execute();
                                        new CustomCommand("mkdir /data/local/stryker", core).execute();
                                        new CustomCommand("rm /storage/emulated/Download/stryker.tar.gz", core).execute();
                                        new CustomCommand("mkdir /storage/emulated/0/Stryker/hs", core).execute();
                                        new CustomCommand("mkdir /storage/emulated/0/Stryker/captured", core).execute();
                                        try {
                                            boolean isok = new CheckFile("/storage/emulated/0/Download/stryker.tar.gz").execute().get();
                                            if (isok) {
                                                setText(status, "Setting up core...");
                                                boolean untar = new CustomCommand("/data/data/com.zalexdev.stryker/cache/busybox tar -xf /storage/emulated/0/Download/stryker.tar.gz -C /data/local/stryker", core).execute().get();
                                                if (untar) {
                                                    setText(status, "Successful installed! Please restart app...");
                                                }
                                            } else {
                                                status.setText("File corrupted!");
                                            }
                                        } catch (ExecutionException | InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                    }


                                    cursor.close();
                                }


                            }).start();
                        });
                    } else {
                        mounted = new CheckDir("/data/local/stryker/beta/sdcard/Stryker").execute().get();
                        progressIndicator.setVisibility(View.INVISIBLE);
                        if (mounted) {
                            status.setVisibility(View.INVISIBLE);
                            welcome.setVisibility(View.VISIBLE);
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.flContent, new Dashboard()).commit();
                        } else {
                            setText(status, "Mounting core...");
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        boolean mount = new CustomCommand("/data/data/com.zalexdev.stryker/cache/bootroot", core).execute().get();
                                        if (mount) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    status.setVisibility(View.INVISIBLE);
                                                    welcome.setVisibility(View.VISIBLE);
                                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                    fragmentManager.beginTransaction().replace(R.id.flContent, new Dashboard()).commit();
                                                }
                                            });

                                        } else {
                                            setText(status, "Error mounting...");
                                        }
                                    } catch (ExecutionException | InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            t.start();

                        }
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }


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
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        core.toaster("Starting background update! 0%");
        new CustomCommand("rm /storage/emulated/0/Download/stryker.apk", core).execute();
        new CustomCommand("rm /data/local/stryker.apk", core).execute();
        new CustomCommand("rm /storage/emulated/0/Download/stryker-chroot.tar.gz", core).execute();
        try {
            core.toaster(urlapk);
            boolean apk = new CustomCommand(Core.BUSYBOX+"wget "+urlapk+" -O /storage/emulated/0/Download/stryker.apk",core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
            if (apk){
                core.toaster("Downloaded apk! 30%");
                boolean chroot = new CustomCommand(Core.BUSYBOX+"wget "+urlchroot+" -O /storage/emulated/0/Download/stryker-chroot.tar.gz",core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                if (chroot){
                    core.toaster("Downloaded chroot! 50%");
                    Boolean un = unmount();
                        core.toaster("Deleting chroot! 60%");
                        boolean del = new CustomCommand("rm -rf /data/local/stryker",new Core(context)).execute().get();
                        if (del){
                        core.toaster("Installing new version! 70%");
                            new CustomCommand("cp /storage/emulated/0/Download/stryker.apk /data/local/stryker.apk", core).execute();
                        new CustomCommand("pm install /data/local/stryker.apk&&sleep 2&&am start -n com.zalexdev.stryker/.MainActivity",core).execute();
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