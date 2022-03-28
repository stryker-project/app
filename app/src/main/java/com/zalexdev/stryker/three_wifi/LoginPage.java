package com.zalexdev.stryker.three_wifi;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Cabinet;
import com.zalexdev.stryker.three_wifi.utils.GetApiKeys;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class LoginPage extends Fragment {


    public Core core;
    public Context context;
    public Activity activity;

    /**
     * This function is called when the user clicks the login button.
     * It checks if the user has entered a username and password.
     * If they have, it calls the GetApiKeys class to get the API keys.
     * If the user has entered valid credentials, it replaces the current fragment with the SearchPage
     * fragment.
     * If the user has entered invalid credentials, it displays an error message
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment,
     * @param container The ViewGroup that the fragment's UI should be attached to. This is typically
     * the Activity's content view.
     * @param savedInstanceState A Bundle that is passed into the onCreate() method.
     * @return Nothing.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.three_wifi_login, container, false);
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
        FragmentManager fragmentManager = getFragmentManager();
        Button loginb = viewroot.findViewById(R.id.login);
        TextInputEditText log = viewroot.findViewById(R.id.getlogin);
        TextInputEditText pass = viewroot.findViewById(R.id.getpassword);
        TextView error = viewroot.findViewById(R.id.error_login);
        Animation animShake = AnimationUtils.loadAnimation(context, R.anim.shake);
        Cabinet temp = new Cabinet(context);
        temp.getStored();
        if (temp.getKeyView().length() > 0) {
            fragmentManager.beginTransaction().replace(R.id.flContent, new SearchPage()).commit();
        } else {
            loginb.setOnClickListener(view -> {
                String login = Objects.requireNonNull(log.getText()).toString();
                String password = Objects.requireNonNull(pass.getText()).toString();
                if (login.length() > 0 && password.length() > 0) {
                    new Thread(() -> {
                        try {
                            Cabinet main = new GetApiKeys(login, password, context).execute().get();
                            if (main.getOk()) {
                                activity.runOnUiThread(() -> {
                                    fragmentManager.beginTransaction().replace(R.id.flContent, new SearchPage()).commit();
                                    core.toaster("OK");
                                });
                            } else {
                                activity.runOnUiThread(() -> {
                                    error.setVisibility(View.VISIBLE);
                                    error.startAnimation(animShake);
                                    error.setText(core.str("invalid_creds"));
                                });

                            }
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            });
        }
        return viewroot;
    }

}