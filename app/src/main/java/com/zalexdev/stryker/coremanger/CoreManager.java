package com.zalexdev.stryker.coremanger;

import static com.zalexdev.stryker.R.string.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.coremanger.utils.GetPackage;
import com.zalexdev.stryker.coremanger.utils.InstallPackage;
import com.zalexdev.stryker.coremanger.utils.InstallPipPackage;
import com.zalexdev.stryker.custom.Package;
import com.zalexdev.stryker.custom.Sploit;
import com.zalexdev.stryker.searchsploit.SploitAdapter;
import com.zalexdev.stryker.searchsploit.utils.GetSploit;
import com.zalexdev.stryker.utils.CheckInet;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class CoreManager extends Fragment {
    public ImageButton search;
    public Core core;
    private RecyclerView mRecyclerView;
    private CoreAdapter mAdapter;
    public Context context;
    public Activity activity;
    public boolean pkg;
    public CoreManager() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initalizing
        View view = inflater.inflate(R.layout.coremanager_fragment, container, false);
        context = getContext();
        activity = getActivity();
        mRecyclerView = view.findViewById(R.id.package_list);
        search = view.findViewById(R.id.search);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        core = new Core(context);
        mRecyclerView.setItemViewCacheSize(255);
        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        view.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() {core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });
        fixinet();
        TextInputEditText getquery = view.findViewById(R.id.getsearch);
        MaterialRadioButton apk = view.findViewById(R.id.apktoogle);
        MaterialRadioButton pip = view.findViewById(R.id.piptoggle);
        apk.setOnClickListener(view12 -> {
            pip.setChecked(false);
            pkg = true;
            search.setImageDrawable(context.getDrawable(R.drawable.search));
            search.setOnClickListener(view1 -> {
                String q = String.valueOf(getquery.getText());
                new Thread(() -> {
                    try {
                        ArrayList<Package> w = new GetPackage(q, core).execute().get();
                        activity.runOnUiThread(() -> {
                            if (w.isEmpty()) {
                                core.toaster(core.str("no_results"));
                            } else {
                                mAdapter = new CoreAdapter(context, activity, w);
                                mRecyclerView.setAdapter(mAdapter);
                            }
                        });
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            });
        });
        pip.setOnClickListener(view13 -> {

            apk.setChecked(false);
            pkg = false;
            search.setImageDrawable(context.getDrawable(R.drawable.download));
            search.setOnClickListener(view14 -> {
                String q = String.valueOf(getquery.getText());
                core.toaster(getString(installing));
                new Thread(() -> {
                    try {
                        Boolean ok = new InstallPipPackage(q,core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                        if (ok){
                            toaster(q+core.str("installedd"));
                        }else{
                            toaster(core.str("inst_error")+q);
                        }

                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            });
        });
        search.setOnClickListener(view1 -> {
            String q = String.valueOf(getquery.getText());
            new Thread(() -> {
                try {
                    ArrayList<Package> w = new GetPackage(q, core).execute().get();
                    activity.runOnUiThread(() -> {
                        if (w.isEmpty()) {
                            core.toaster(context.getResources().getString(no_results));
                        } else {
                            mAdapter = new CoreAdapter(context, activity, w);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    });
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });

        return view;
    }

    public void toaster(String msg) {
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(context,
                    msg, Toast.LENGTH_SHORT);
            toast.show();
        });

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
        title.setText(checking_inet);
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
                    boolean o = core.remountcore();
                    inet  = new CheckInet().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                    if (inet){
                        activity.runOnUiThread(dialog::dismiss);
                    }else {
                        activity.runOnUiThread(() -> {
                            cancel.setVisibility(View.VISIBLE);
                            cancel.setText("OK");
                            progress.setVisibility(View.VISIBLE);
                            progress.setText(no_inet_chroot);
                        });}


                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        dialog.show();
    }
}