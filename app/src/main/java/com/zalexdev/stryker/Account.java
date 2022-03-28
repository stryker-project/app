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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;


public class Account extends Fragment {
    public Core core;
    public Activity activity;
    public boolean edit = false;
    public Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.account_fragment, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        EditText user = viewroot.findViewById(R.id.username);
        user.setText(core.getString("username"));
        ImageView editIcon = viewroot.findViewById(R.id.edit_name);
        editIcon.setOnClickListener(view -> {
            if (!edit) {
                edit = true;

                editIcon.setImageDrawable(context.getDrawable(R.drawable.ok));
                user.setFocusable(true);
                user.setFocusableInTouchMode(true);
                user.setClickable(true);
                user.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(user, InputMethodManager.SHOW_IMPLICIT);
            } else {
                // This is saving the username to the shared preferences.
                core.putString("username",user.getText().toString());
                edit = false;
                editIcon.setImageDrawable(context.getDrawable(R.drawable.edit));
                user.setFocusable(false);
                user.setFocusableInTouchMode(false);
                user.setClickable(false);
                if (user.isFocused()){
                    user.clearFocus();
                }
            }
        });
        TextView info = viewroot.findViewById(R.id.device_info);
        MaterialCardView tg = viewroot.findViewById(R.id.about_tg);
        MaterialCardView pda = viewroot.findViewById(R.id.about_4pda);
        MaterialCardView web = viewroot.findViewById(R.id.about_site);
        MaterialCardView ethereum = viewroot.findViewById(R.id.ethereum);
        MaterialCardView bitcoin = viewroot.findViewById(R.id.bitcoin);
        MaterialCardView card = viewroot.findViewById(R.id.creditcard);
        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        web.setOnClickListener(view -> openlink("https://stryker.zalex.dev"));
        tg.setOnClickListener(view -> openlink("https://t.me/stryker"));
        pda.setOnClickListener(view -> openlink("https://4pda.to/forum/index.php?showtopic=1037129"));
        ethereum.setOnClickListener(view -> copy(context,"0x7D91d522244C94a11422C0481c9fdBbFBA01784B"));
        bitcoin.setOnClickListener(view -> copy(context,"bc1q0sewu9cr9rh2uf7qzvxmmvc2z7x7vc9wp7v8f8"));
        card.setOnClickListener(view -> copy(context,"5375411503013075"));
        info.setText(getDeviceName() + "\n" + core.str("plata")+" " + Build.BOARD + "\n" + "Android SDK: " + Build.VERSION.SDK);

        return viewroot;
    }
    /**
     * It hides the keyboard from the screen.
     *
     * @param context The context of the activity that called this method.
     * @param view The view that is currently focused.
     */
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    private void copy(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(core.str("copied"), text);
        clipboard.setPrimaryClip(clip);
        core.toaster(core.str("copied"));
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
}