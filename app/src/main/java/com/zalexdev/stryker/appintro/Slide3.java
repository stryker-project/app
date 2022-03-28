package com.zalexdev.stryker.appintro;


import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.Dashboard;
import com.zalexdev.stryker.MainActivity;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.utils.CheckDir;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class Slide3 extends Fragment {


    public String chroot;
    public Core core;
    public Context context;
    public Activity activity;
    public ViewPager mPager;
    public int click = 0;
    public Slide3(ViewPager p){
        mPager = p;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slide3, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        Animation fade = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        LinearLayout layout = view.findViewById(R.id.slide_layout);
        ImageView img = view.findViewById(R.id.slide_img);
        TextView title = view.findViewById(R.id.slide_title);
        TextView desc = view.findViewById(R.id.slide_description);
        TextView progress_status = view.findViewById(R.id.slide_progress_text);
        LinearProgressIndicator progress = view.findViewById(R.id.slide_install_progress);
        MaterialButton button = view.findViewById(R.id.slide_button);
        button.setOnClickListener(view1 -> {
            button.setVisibility(View.INVISIBLE);
            progress.setIndeterminate(true);
            progress.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);
            title.setText(core.str("install"));
            title.startAnimation(fade);
            title.setText(core.str("install2"));
            title.startAnimation(fade);
            progress_status.setVisibility(View.VISIBLE);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Downloading sqlite3 and trying to off magisk notif...
                    
                }
            }).start();


            new Thread(() -> {
                clear();
                boolean core_ok;
                if (core.is64Bit()){
                    core_ok = download("https://github.com/stryker-project/stryker-chroot/releases/download/2.0R/Stryker2R.tar.gz", "stryker.tar.gz", progress_status, progress);
                }else {
                    core_ok = download("https://github.com/stryker-project/stryker-chroot/releases/download/2.0R/Stryker2R-32bit.tar.gz", "stryker.tar.gz", progress_status, progress);
                }
                if (core_ok) {
                    setText(title, core.str("install_unpack"), true);
                    setInter(progress, true);
                    setText(title, core.str("install3"), true);
                    if (unTarFile(core.getStorage() + "Download/stryker.tar.gz")){
                        setText(title, core.str("success"), true);
                        new CustomCommand("mkdir /data/local/stryker/release/modules&&mkdir /data/local/stryker/release/exploits&&mkdir /storage/emulated/0/Stryker/modules&&&&mkdir /storage/emulated/0/Stryker/explots&&mkdir /storage/emulated/0/Stryker/wordlists&&mkdir /storage/emulated/0/Stryker/hs&&mkdir /storage/emulated/0/Stryker/captured",core).execute();
                    }else{
                        setText(title, getString(R.string.failed_try), true);
                    }
                    core.mountcore();
                    activity.runOnUiThread(() -> {
                        progress.setVisibility(View.INVISIBLE);
                        progress_status.setVisibility(View.INVISIBLE);
                        core.MoveNext(mPager);
                    });

                } else {
                    setText(title, context.getResources().getString(R.string.fail), true);
                }
            }).start();
        });
        return view;
    }

    public void changecolor(boolean red,LinearLayout layout){
    int colorFrom = Color.parseColor("#2196F3");
    int colorTo = Color.parseColor("#FFFF9800");
    ValueAnimator colorAnimation;
    if (red){
          colorAnimation  = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);}
    else{
        colorAnimation  = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
    }
    colorAnimation.setDuration(250);
    colorAnimation.addUpdateListener(animator -> {
        getActivity().getWindow().setNavigationBarColor((int) animator.getAnimatedValue());
        getActivity().getWindow().setStatusBarColor((int) animator.getAnimatedValue());
        layout.setBackgroundColor((int) animator.getAnimatedValue());
    });
    colorAnimation.start();
}
    @SuppressLint("Range")
    public Boolean download(String url, String name, TextView status, LinearProgressIndicator progress) {
        boolean ok = false;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(context.getResources().getString(R.string.install2));
        request.setTitle(context.getResources().getString(R.string.wait));
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
                ok = true;
            }
            cursor.close();
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

    public void setInter(LinearProgressIndicator progressIndicator, boolean inter) {
        activity.runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(inter);
            progressIndicator.setVisibility(View.VISIBLE);
        });

    }

    public void clear() {
        new CustomCommand("chmod 777 -R /data/data/com.zalexdev.stryker/files/", core).execute();
        new CustomCommand("mkdir " + core.getStorage() + "Stryker", core).execute();
        new CustomCommand("mkdir /data/local/stryker", core).execute();
        new CustomCommand("rm " + core.getStorage() + "Download/stryker.tar.gz", core).execute();
        new CustomCommand("rm -rf " + core.getStorage() + "Stryker/release", core).execute();
        new CustomCommand("rm " + core.getStorage() + "Download/stryker.apk", core).execute();
        new CustomCommand("mkdir " + core.getStorage() + "Stryker/hs", core).execute();
        new CustomCommand("mkdir " + core.getStorage() + "Stryker/captured", core).execute();
    }

    private boolean unTarFile(String tarFile) {
        try {
            return new CustomCommand("/data/data/com.zalexdev.stryker/files/busybox tar -xf " + tarFile + " -C " + "/data/local/stryker/", core).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}