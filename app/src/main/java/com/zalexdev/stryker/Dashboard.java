package com.zalexdev.stryker;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zalexdev.stryker.local.LocalMain;
import com.zalexdev.stryker.utils.CheckMsg;
import com.zalexdev.stryker.utils.CheckUpdates;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.wifi.Wifi;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Dashboard extends Fragment {


    public Core core;
    public Context context;
    public  String versionName = BuildConfig.VERSION_NAME;
    public  int versionInt = BuildConfig.VERSION_CODE;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.dashboard, container, false);
        context = getContext();
        core = new Core(context);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        MaterialCardView wifi = viewroot.findViewById(R.id.dashboard_wifi);
        LottieAnimationView anim = viewroot.findViewById(R.id.load_progress);
        MaterialCardView local = viewroot.findViewById(R.id.dashboard_local);
        MaterialCardView settings = viewroot.findViewById(R.id.dashboard_settings);
        MaterialCardView folder = viewroot.findViewById(R.id.dashboard_folder);
        wifi.setOnClickListener(view -> fragmentManager.beginTransaction().replace(R.id.flContent, new Wifi()).commit());
        local.setOnClickListener(view -> fragmentManager.beginTransaction().replace(R.id.flContent, new LocalMain()).commit());
        settings.setOnClickListener(view -> fragmentManager.beginTransaction().replace(R.id.flContent, new Settings()).commit());
        folder.setOnClickListener(view -> fragmentManager.beginTransaction().replace(R.id.flContent, new About()).commit());
        if (core.getBoolean("auto_update")){
        new Thread(() -> {
            try {
                List<String> update = Arrays.asList(new CheckUpdates(getContext()).execute().get().split(","));

                if (update.size()>3){
                    if (Integer.parseInt(update.get(1))>versionInt){
                        getActivity().runOnUiThread(() -> updatedialog(update.get(0),update.get(2),update.get(3)));

                    }else if (Integer.parseInt(update.get(1))==versionInt && !update.get(0).equals(versionName)){
                        getActivity().runOnUiThread(() -> updatefix(update.get(2)));
                    }
                }

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();}
        new Thread(() -> {
            try {
                List<String> msg = Arrays.asList(new CheckMsg().execute().get().split(";"));

                if (msg.size()>3){
                    if (core.getInt("msgid")<Integer.parseInt(msg.get(0))){
                        getActivity().runOnUiThread(() -> newmsg(msg.get(1),msg.get(2),msg.get(3),msg.get(4)));
                        core.putInt("msgid",Integer.parseInt(msg.get(0)));
                    }
                }

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        return viewroot;
    }
    public void updatedialog(String name,String urlapk,String urlchroot){
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("New update available!")
                .setMessage("Do you want to upgrade your application from version "+versionName+" to "+name+"? This includes the automatic download of the app, chroot and their installation! ")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, new Updater()).commit();
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).show();

    }
    public void updatefix(String urlapk){
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("New fix available!")
                .setMessage("The developer has released a bug fix version, do you want to install it?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, new Fixer()).commit();
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).show();

    }
    public void newmsg(String title,String msg,String action,String url){
        new MaterialAlertDialogBuilder(getContext())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", (dialogInterface, i) -> {

                })
                .setNeutralButton(action, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (action.equals("Donate!")){
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.flContent, new About()).commit();
                    }else if (url.equals("None")){
                        dialogInterface.dismiss();
                    }else{
                        openlink(url);
                    }
                }).show();

    }
    public void  openlink(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}