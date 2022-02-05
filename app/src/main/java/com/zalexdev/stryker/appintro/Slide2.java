package com.zalexdev.stryker.appintro;


import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.utils.Core;

public class Slide2 extends Fragment {


    public String chroot;
    public Core core;
    public Context context;
    public Activity activity;
    public ViewPager mPager;
    public Slide2(ViewPager p){
        mPager = p;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slide2, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        LinearLayout layout = view.findViewById(R.id.slide_layout);
        ImageView img = view.findViewById(R.id.slide_img);
        TextView title = view.findViewById(R.id.slide_title);
        TextView desc = view.findViewById(R.id.slide_description);
        MaterialButton button = view.findViewById(R.id.slide_button);
        checkpermission();
        button.setOnClickListener(view12 -> {
            if (checkpermission()){
                if (core.checkroot()){
                    if (!core.isInstalledOnSdCard()){
                    core.MoveNext(mPager);
                    }
                    else{
                        changecolor(true,layout);
                        title.setText(R.string.error);
                        desc.setText(R.string.sdcard_error);
                        button.setVisibility(View.GONE);
                    }
                }else{
                    changecolor(true,layout);
                    title.setText(R.string.noroot_perm);
                    desc.setText(R.string.nonroot);
                    button.setVisibility(View.GONE);
                }
            }else{
                changecolor(true,layout);
                title.setText(R.string.no_storage);
                desc.setText(R.string.no_storage_desc);
                desc.setOnClickListener(view1 -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                });
            }
        });
        return view;
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
    public void changecolor(boolean red, LinearLayout layout){
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
}