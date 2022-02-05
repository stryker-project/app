package com.zalexdev.stryker;


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
        MaterialCardView pda = viewroot.findViewById(R.id.about_4pda);
        MaterialCardView web = viewroot.findViewById(R.id.about_site);
        MaterialCardView donate = viewroot.findViewById(R.id.about_donate);

        web.setOnClickListener(view -> openlink("https://zalex.dev/stryker"));
        donate.setOnClickListener(view -> openlink("https://zalex.dev/stryker/donate"));
        tg.setOnClickListener(view -> openlink("https://t.me/strykerapp"));
        pda.setOnClickListener(view -> openlink("https://4pda.to/forum/index.php?showtopic=1037129"));

        info.setText(getDeviceName() + "\n" + core.str("plata") + Build.BOARD + "\n" + "Android SDK: " + Build.VERSION.SDK);
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

    public void openlink(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void setClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(core.str("copied"), text);
        clipboard.setPrimaryClip(clip);
        core.toaster(core.str("copied"));
    }
}