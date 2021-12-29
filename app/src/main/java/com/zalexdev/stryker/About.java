package com.zalexdev.stryker;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;
import com.zalexdev.stryker.wifi.utils.GetInterfaces;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class About extends Fragment {



    public String chroot;
    public Core core;
    public Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.about, container, false);
        context = getContext();
        core = new Core(context);
        TextView info = viewroot.findViewById(R.id.device_info);
        MaterialCardView tg = viewroot.findViewById(R.id.about_tg);
        MaterialCardView pda= viewroot.findViewById(R.id.about_4pda);
        MaterialCardView btc = viewroot.findViewById(R.id.about_btc);
        MaterialCardView card = viewroot.findViewById(R.id.about_card);
        tg.setOnClickListener(view -> openlink("https://t.me/strykerapp"));
        pda.setOnClickListener(view -> openlink("https://4pda.to/forum/index.php?showtopic=1037129"));
        btc.setOnClickListener(view -> setClipboard(getContext(),"bc1qclvgmj8pk8ruh7k69zar74gxpq8wdsx20e34wz"));
        card.setOnClickListener(view -> setClipboard(getContext(),"5375411503013075"));
        info.setText(getDeviceName()+"\n"+"CPU: "+Build.BOARD+"\n"+"Android: "+ Build.VERSION.SDK);
        return viewroot;
    }
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
    public void  openlink(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
    private void setClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied!", text);
        clipboard.setPrimaryClip(clip);
        core.toaster("Copied! Thx for your support!");
    }
}