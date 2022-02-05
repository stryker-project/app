package com.zalexdev.stryker;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Fixer extends Fragment {


    public Context context;
    public Core core;
    public String versionName = BuildConfig.VERSION_NAME;
    public int versionInt = BuildConfig.VERSION_CODE;
    public TextView status;
    public LinearProgressIndicator progress;
    public String url;
    public Activity activity;
    public static boolean is64Bit() {
        return (Build.SUPPORTED_64_BIT_ABIS != null && Build.SUPPORTED_64_BIT_ABIS.length > 0);
    }
    public Fixer(String u){
        url = u;

    }
    @SuppressLint("Range")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.update, container, false);
        context = getContext();
        activity = getActivity();
        progress = viewroot.findViewById(R.id.prog_update);
        status = viewroot.findViewById(R.id.update_status);
        core = new Core(context);
        new Thread(() -> updater(url)).start();


        return viewroot;

    }

    public void setText(TextView textView, String text) {
        activity.runOnUiThread(() -> textView.setText(text));
    }

    public void setProg(LinearProgressIndicator progressIndicator, int prog) {
        activity.runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);

            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(prog, true);
            }
        });

    }

    @SuppressLint("Range")
    public void updater(String urlapk) {
        setText(status,  core.str("download_apk"));
        setProg(progress, 80);
        new CustomCommand("rm "+core.getStorage()+"Download/strykerfix.apk",core).execute();
        boolean apk = download(urlapk,"strykerfix.apk",status,progress);
        if (apk){
            core.installApplication(context,"/storage/emulated/0/Download/strykerfix.apk");
        }else{
            setText(status,"Error downloading fix...");
        }

    }
    @SuppressLint("Range")
    public Boolean download(String url, String name, TextView status, LinearProgressIndicator progress) {
        boolean ok = false;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(url);
        request.setTitle(core.str("wait"));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);
        boolean downloading = true;
        while (downloading) {
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(downloadId);
            Cursor cursor = manager.query(q);
            cursor.moveToFirst();
            @SuppressLint("Range") int bytes_downloaded = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            @SuppressLint("Range") int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            if (bytes_total == 0) {
                break;
            }
            final int dl_progress = (int) ((bytes_downloaded * 100L) / bytes_total);
            setText(status, bytes_downloaded / 1024 / 1024 + "MB/" + bytes_total / 1024 / 1024 + "MB (" + dl_progress + "%)", false);
            setProg(progress, dl_progress);
            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                downloading = false;

            }
            cursor.close();
        }
        File f = new File("storage/emulated/0/Download/"+name);
        if(f.exists() && !f.isDirectory()) {
            ok = true;
        }
        return ok;

    }
    public void setText(TextView textView, String text, boolean animate) {
        activity.runOnUiThread(() -> {
            if (animate) {
                Animation fade = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                textView.startAnimation(fade);
            }
            textView.setText(text);
        });
    }
}