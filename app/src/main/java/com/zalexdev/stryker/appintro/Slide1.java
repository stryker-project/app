package com.zalexdev.stryker.appintro;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.utils.Core;

public class Slide1 extends Fragment {


    public String chroot;
    public Core core;
    public Context context;
    public Activity activity;
    public ViewPager mPager;
    public int click = 0;
    public Slide1(ViewPager p){
        mPager = p;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slide1, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        ImageView img = view.findViewById(R.id.slide_img);
        TextView title = view.findViewById(R.id.slide_title);
        TextView desc = view.findViewById(R.id.slide_description);
        MaterialButton button = view.findViewById(R.id.slide_button);
        img.setOnClickListener(view12 -> {
            click++;
            if (click>5){
                click = 0;
                core.putBoolean("debug",true);
                img.setImageDrawable(context.getDrawable(R.drawable.debug));
                core.toaster(core.str("debug_mode_on"));
                title.setText(R.string.debug_mode_title);
                desc.setText(R.string.debug_desv);
            }
        });
        button.setOnClickListener(view1 -> core.MoveNext(mPager));
        return view;
    }


}