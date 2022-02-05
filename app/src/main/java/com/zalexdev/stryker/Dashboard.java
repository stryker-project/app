package com.zalexdev.stryker;


import android.app.Activity;
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
import com.google.android.material.navigation.NavigationView;
import com.zalexdev.stryker.coremanger.CoreManager;
import com.zalexdev.stryker.exploit_hub.ExploitScreen;
import com.zalexdev.stryker.handshakes.HandshakeStorage;
import com.zalexdev.stryker.local_network.LocalMain;
import com.zalexdev.stryker.modules.ModulesFragment;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.wifi.Wifi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Dashboard extends Fragment {


    public Core core;
    public Context context;
    public Activity activity;
    public String versionName = BuildConfig.VERSION_NAME;
    public NavigationView menu;
    public int versionInt = BuildConfig.VERSION_CODE;
    public Dashboard(NavigationView d){
        menu = d;
    }
    public Dashboard(){

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.dashboard, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        FragmentManager fragmentManager = getFragmentManager();
        MaterialCardView wifi = viewroot.findViewById(R.id.dashboard_wifi);
        LottieAnimationView anim = viewroot.findViewById(R.id.load_progress);
        MaterialCardView local = viewroot.findViewById(R.id.dashboard_local);
        MaterialCardView settings = viewroot.findViewById(R.id.dashboard_settings);
        MaterialCardView about = viewroot.findViewById(R.id.dashboard_about);
        MaterialCardView hs_folder = viewroot.findViewById(R.id.dashboard_hs);
        MaterialCardView mod = viewroot.findViewById(R.id.dashboard_modules_repo);
        MaterialCardView manager = viewroot.findViewById(R.id.dashboard_core_manager);
        MaterialCardView exploit = viewroot.findViewById(R.id.dashboard_exploit);
        mod.setOnClickListener(view -> {
            fragmentManager.beginTransaction().replace(R.id.flContent, new ModulesFragment()).commit();
            setchecked(7);
        });
        wifi.setOnClickListener(view -> {
            fragmentManager.beginTransaction().replace(R.id.flContent, new Wifi()).commit();
            setchecked(1);
        });
        local.setOnClickListener(view -> {
            fragmentManager.beginTransaction().replace(R.id.flContent, new LocalMain()).commit();
            setchecked(2);
        });
        settings.setOnClickListener(view -> {
            fragmentManager.beginTransaction().replace(R.id.flContent, new Settings()).commit();
            setchecked(12);
        });
        about.setOnClickListener(view -> {
            fragmentManager.beginTransaction().replace(R.id.flContent, new About()).commit();
            setchecked(13);
        });
        manager.setOnClickListener(view -> {
            fragmentManager.beginTransaction().replace(R.id.flContent, new CoreManager()).commit();
            setchecked(6);
        });
        hs_folder.setOnClickListener(view -> {
            fragmentManager.beginTransaction().replace(R.id.flContent, new HandshakeStorage()).commit();
            setchecked(4);
        });
        exploit.setOnClickListener(view -> {
            fragmentManager.beginTransaction().replace(R.id.flContent, new ExploitScreen()).commit();
            setchecked(3);
        });

        if (core.getBoolean("auto_update")){
        new Thread(() -> {
            JSONObject update = core.getjsonbyurl("https://raw.githubusercontent.com/stryker-project/updater/main/update");
            try {
            int version = BuildConfig.VERSION_CODE;
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

    public void updatedialog(String name, String urlapk, String urlchroot32,String urlchroot64) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.new_update)
                .setMessage(getString(R.string.want_update) + versionName + getString(R.string.doo) + name + getString(R.string.rvregre))
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, new Updater(urlapk,urlchroot32,urlchroot64)).commit();
                })
                .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.dismiss()).show();

    }

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
    public void setchecked(int i){
        if (menu !=null){
         menu.getMenu().getItem(i).setChecked(true);}
    }
    public void openlink(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}