package com.zalexdev.stryker;


import android.annotation.SuppressLint;
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
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

public class StillDeveloping extends Fragment {


    public String chroot;
    public Core core;
    public Activity activity;
    public Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.developing, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);

        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        viewroot.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() {core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });

        return viewroot;
    }
}