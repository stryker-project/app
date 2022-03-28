package com.zalexdev.stryker;


import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.coremanger.CoreManager;
import com.zalexdev.stryker.exploit_hub.ExploitScreen;
import com.zalexdev.stryker.handshakes.HandshakeStorage;
import com.zalexdev.stryker.local_network.LocalMain;
import com.zalexdev.stryker.modules.ModulesFragment;
import com.zalexdev.stryker.router_scan.ThreadInterface;
import com.zalexdev.stryker.utils.CheckMagiskNotif;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;

import com.zalexdev.stryker.utils.OnSwipeListener;
import com.zalexdev.stryker.wifi.Wifi;
import com.zalexdev.stryker.wifi.utils.GetInterfaces;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Dashboard extends Fragment  {


    public Core core;
    public Context context;
    public Activity activity;

    public Dashboard(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.dashboard, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        ImageView img_download = viewroot.findViewById(R.id.img_download);
        ImageView img_close_license = viewroot.findViewById(R.id.license_close);
        ImageView img_close_magisk = viewroot.findViewById(R.id.magisk_close);
        MaterialCardView download_card = viewroot.findViewById(R.id.download_card);
        TextView title_download = viewroot.findViewById(R.id.title_download);
        TextView user_hello = viewroot.findViewById(R.id.user_hello);
        checkpermission();
        ExpandableLayout download_notif = viewroot.findViewById(R.id.expand_download);
        ExpandableLayout license_notif = viewroot.findViewById(R.id.license_notif);
        ExpandableLayout magisk_notif = viewroot.findViewById(R.id.magisk_notif);
        LinearProgressIndicator download_progress = viewroot.findViewById(R.id.progress_download);
        img_close_license.setOnClickListener(view -> license_notif.collapse());
        img_close_magisk.setOnClickListener(view -> magisk_notif.collapse());
        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        // The above code is setting the text of the label to "userhello" and the username of the user.
        user_hello.setText(core.str("userhello")+" "+core.getString("username"));
        try {
            // This code is checking if Magisk notifications about root access is active. If it is, it will
            // show the notification.
            if (new CheckMagiskNotif(core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get()){
                magisk_notif.expand();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        viewroot.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() {core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });
        magisk_notif.setOnClickListener(view -> {
            try {
                magisk_notif.collapse();
                Boolean disablemagisknotif = new CustomCommand("/data/data/com.zalexdev.stryker/files/sqlite3 "
                        + "/data/adb/magisk.db"
                        + " \"UPDATE policies SET logging='0',notification='0' WHERE package_name='"
                        + "com.zalexdev.stryker"
                        + "';\"",core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                if (disablemagisknotif){
                    core.toaster("Уведомления отключены!");
                }else {
                    core.toaster("Ошибка отключения уведомления!");
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        if (!core.getBoolean("first_open")) {
            core.putString("username", "New User");
            core.remove("installed_modules");
            try {
                ArrayList<String> interfaces = new GetInterfaces(core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                if (interfaces.contains("swlan0")){
                    core.putString("wlan_scan", "swlan0");
                    core.putString("wlan_deauth", "swlan0");
                }else{
                    core.putString("wlan_scan", "wlan0");
                    core.putString("wlan_deauth", "wlan0");
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                core.putString("wlan_scan", "wlan0");
                core.putString("wlan_deauth", "wlan0");

            }
            core.putBoolean("first_open", true);
            core.putBoolean("store_scan", true);
            core.putBoolean("auto_update", true);
            core.putInt("night",2);
            core.putInt("threads", 100);
            new CustomCommand("dumpsys deviceidle whitelist +com.zalexdev.stryker", core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }


        if (core.getBoolean("auto_update")){
        // This code is checking for updates.
        new Thread(() -> {
            JSONObject update = core.getjsonbyurl("https://raw.githubusercontent.com/stryker-project/updater/main/update");
            try {
            int version = 23;
            int newversion = update.getInt("version");
            if (newversion>version){
                activity.runOnUiThread(() -> {
                    try {
                        if (!update.getBoolean("isfix")){
                        updatedialog(update.getString("name"),update.getString("srcapk"),update.getString("chroot32"),update.getString("chroot64"));}
                        else{
                            updatefix(update.getString("srcapk"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
                } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
        // This code is checking for a new update message from the server.
        new Thread(() -> {
            JSONObject msg = core.getjsonbyurl("https://raw.githubusercontent.com/stryker-project/updater/main/msg");
            try {
                if (msg.has("msg") && !core.getListString("msgs").contains(msg.getString("title"))){
                    ArrayList<String> msgs = core.getListString("msgs");
                    msgs.add(msg.getString("title"));
                    core.putListString("msgs",msgs);
                    activity.runOnUiThread(() -> {
                        try {
                            newmsg(msg.getString("title"),msg.getString("msg"),msg.getBoolean("enabled"),msg.getString("buttontext"), msg.getString("url"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    });
                     }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();

            }



        return viewroot;
    }

    public boolean checkpermission() {
        if (context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{WRITE_EXTERNAL_STORAGE},
                    123
            );
        }
        return context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    /**
     * This function is used to show the dialog box to the user to ask if he wants to update the app
     *
     * @param name The name of the update
     * @param urlapk The URL of the APK file to download.
     * @param urlchroot32 the url of the 32-bit chroot
     * @param urlchroot64 the url of the 64-bit chroot
     */
    public void updatedialog(String name, String urlapk, String urlchroot32,String urlchroot64) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.new_update)
                .setMessage(getString(R.string.want_update) + "23" + getString(R.string.doo) + name + getString(R.string.rvregre))
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, new Updater(urlapk,urlchroot32,urlchroot64)).commit();
                })
                .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.dismiss()).show();

    }

    /**
     * This function is used to show a dialog box to the user to ask if they want to update to the
     * latest version of the app.
     * If the user clicks yes, the function will replace the current fragment with the fixer fragment.
     * If the user clicks no, the function will dismiss the dialog box
     *
     * @param urlapk The URL of the APK file to download.
     */
    public void updatefix(String urlapk) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.new_fix)
                .setMessage(R.string.recom)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, new Fixer(urlapk)).commit();
                })
                .setNegativeButton(R.string.no, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).show();

    }

    /**
     * This function is used to display a dialog box with a message and a button to dismiss the dialog
     * box
     *
     * @param title The title of the dialog.
     * @param msg The message to be displayed in the dialog.
     * @param a boolean
     * @param action The text to display on the neutral button.
     * @param url The URL to be opened when the user clicks on the "Open" button.
     */
    public void newmsg(String title, String msg,Boolean a, String action, String url) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", (dialogInterface, i) -> {

                })
                .setNeutralButton(action, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (a){
                        openlink(url);
                    }
                }

                ).show();

    }

    /**
     * This function opens a browser with the given url
     *
     * @param url The URL to open.
     */
    public void openlink(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
    @SuppressLint("Range")
    public void updater(String urlapk,TextView status,LinearProgressIndicator progress) {
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
    // This method is downloading the apk file from the internet.
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
            setText(status, bytes_downloaded / 1024 / 1024 + "MB/" + bytes_total / 1024 / 1024 + "MB (" + dl_progress + "%)");
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
    /**
     * It sets the text of a TextView.
     *
     * @param textView The TextView to set the text on.
     * @param text The text to be displayed in the TextView.
     */
    public void setText(TextView textView, String text) {
        activity.runOnUiThread(() -> {
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
}