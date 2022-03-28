package com.zalexdev.stryker.modules;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Module;
import com.zalexdev.stryker.utils.CheckInet;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ModulesFragment extends Fragment {
    public Core core;
    public Context context;
    public Activity activity;

    public ModulesFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initalizing
        View view = inflater.inflate(R.layout.module_fragment, container, false);
        context = getContext();
        activity = getActivity();
        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        view.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() {core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });
        LinearProgressIndicator progressIndicator = view.findViewById(R.id.loading);
        TextView installed_text = view.findViewById(R.id.installed_text);
        RecyclerView mRecyclerView = view.findViewById(R.id.repo_list);
        RecyclerView mRecyclerView2 = view.findViewById(R.id.installed_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mRecyclerView2.setLayoutManager(new LinearLayoutManager(activity));
        fixinet();
        core = new Core(context);
        mRecyclerView.setItemViewCacheSize(255);
        new Thread(() -> {
            ArrayList<Module> modules = core.getModules();
            ArrayList<Module> modulesnot = new ArrayList<>();
            ArrayList<Module> modulesinstalled = new ArrayList<>();
            for (int i = 0;i<modules.size();i++){
                if (core.checkmod(modules.get(i).getName())){
                    modulesinstalled.add(modules.get(i));
                }else {
                    modulesnot.add(modules.get(i));
                }
            }
            for (Module m :modulesinstalled){
                m.setInstalled(true);
            }
            if (!modulesinstalled.isEmpty()){
                activity.runOnUiThread(() -> installed_text.setVisibility(View.VISIBLE));
            }
            activity.runOnUiThread(() -> progressIndicator.setVisibility(View.GONE));
            if (modules.isEmpty()){
                activity.runOnUiThread(() -> {
                    installed_text.setVisibility(View.VISIBLE);
                    installed_text.setTextColor(context.getColor(R.color.red));
                    installed_text.setText(core.str("no_inet"));
                });
            }
            ModulesAdapter mAdapter = new ModulesAdapter(context,activity, modulesnot);
            ModulesAdapter mAdapter2 = new ModulesAdapter(context,activity, modulesinstalled);
            activity.runOnUiThread(() -> mRecyclerView.setAdapter(mAdapter));
            activity.runOnUiThread(() -> mRecyclerView2.setAdapter(mAdapter2));
        }).start();


        return view;
    }
    public void fixinet(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.exploit_progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        LinearProgressIndicator prog = dialog.findViewById(R.id.exploit_prog);
        LottieAnimationView image = dialog.findViewById(R.id.exploit_img);
        TextView title = dialog.findViewById(R.id.exploit_title);
        TextView progress = dialog.findViewById(R.id.exploit_progress_text);
        TextView cancel = dialog.findViewById(R.id.exploit_cancel);
        title.setText(R.string.checking_inet);
        progress.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        dialog.setCancelable(false);
        image.setAnimation(R.raw.check_net);
        prog.setVisibility(View.GONE);
        cancel.setOnClickListener(view -> dialog.dismiss());
        new Thread(() -> {
            try {
                Boolean inet  = new CheckInet().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                if (inet){
                    activity.runOnUiThread(dialog::dismiss);
                }else{
                    activity.runOnUiThread(dialog::show);
                    boolean o = core.remountcore();
                        inet  = new CheckInet().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                        if (inet){
                            activity.runOnUiThread(dialog::dismiss);
                        }else {
                            activity.runOnUiThread(() -> {
                                cancel.setVisibility(View.VISIBLE);
                                cancel.setText("OK");
                                progress.setVisibility(View.VISIBLE);
                                progress.setText(R.string.no_inet_chroot);
                            });}


                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }
}