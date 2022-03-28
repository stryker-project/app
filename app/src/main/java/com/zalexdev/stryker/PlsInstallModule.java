package com.zalexdev.stryker;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zalexdev.stryker.geomac.GeoMac;
import com.zalexdev.stryker.modules.ModulesFragment;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

public class PlsInstallModule extends Fragment {


    public String chroot;
    public Core core;
    public Activity activity;
    public Context context;
    public String name = "Error";
    public boolean invalid = false;
    public PlsInstallModule(String n){
        name = n;
    }
    public PlsInstallModule(){}
    public PlsInstallModule(Boolean i,String n){
        invalid = i;
        name = n;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.installmodule, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        TextView t = viewroot.findViewById(R.id.install_text);
        if (!invalid){
        t.setText(core.str("pls_install").replace("{mn}",name));}
        else{
            core.deletemod(name);
            t.setText(core.str("module_invalid").replace("{mn}",name));
        }
        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        TextView repo = viewroot.findViewById(R.id.gorepo);
        repo.setOnClickListener(view -> {
            getParentFragmentManager().beginTransaction().replace(R.id.flContent, new ModulesFragment()).commit();
            menu.collapse();
        });

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