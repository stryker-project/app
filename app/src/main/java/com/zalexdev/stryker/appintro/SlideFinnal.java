package com.zalexdev.stryker.appintro;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
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
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;

/**
 * This class is used to display the slide finnal screen
 */
public class SlideFinnal extends Fragment {


    public String chroot;
    public Core core;
    public Context context;
    public Activity activity;
    public ViewPager mPager;
    public SlideFinnal(ViewPager p){
        mPager = p;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slidefinnal, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        ImageView img = view.findViewById(R.id.slide_img);
        TextView title = view.findViewById(R.id.slide_title);
        TextView desc = view.findViewById(R.id.slide_description);
        MaterialButton button = view.findViewById(R.id.slide_button);
        button.setOnClickListener(view1 -> {
            new CustomCommand("sleep 1&&am start -n com.zalexdev.stryker/.MainActivity",new Core(context)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            activity.finishAffinity();
        });
        return view;
    }


}