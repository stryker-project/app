package com.zalexdev.stryker;


import static com.airbnb.lottie.L.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomChrootCommand;
import com.zalexdev.stryker.utils.CustomCommand;
import com.zalexdev.stryker.utils.OnSwipeListener;
import com.zalexdev.stryker.wifi.utils.GetInterfaces;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class Settings extends Fragment {


    public TextView scanwlan;
    public TextView deauthwlan;
    public TextView night;
    public Activity activity;
    public String chroot;
    public Core core;
    public Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.settings2, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        MaterialCardView nightmode = viewroot.findViewById(R.id.nightmode);
        MaterialCardView debug = viewroot.findViewById(R.id.settings_debug);
        MaterialCardView module = viewroot.findViewById(R.id.settings_module);
        MaterialCardView hide = viewroot.findViewById(R.id.settings_hide);
        MaterialCardView auto = viewroot.findViewById(R.id.auto_update);
        MaterialCardView turnoff = viewroot.findViewById(R.id.pixie_off);
        MaterialCardView scan = viewroot.findViewById(R.id.settings_scan);
        MaterialCardView deauth = viewroot.findViewById(R.id.settings_deauth);
        MaterialCardView unmount = viewroot.findViewById(R.id.settings_unmount);
        MaterialCardView delete = viewroot.findViewById(R.id.settings_delete);
        MaterialCardView store = viewroot.findViewById(R.id.store_scan);
        MaterialCardView three_wifi = viewroot.findViewById(R.id.three_wifi_scan);
        scanwlan = viewroot.findViewById(R.id.scan_text);
        deauthwlan = viewroot.findViewById(R.id.deauth_text);
        night = viewroot.findViewById(R.id.night_text);
        nightmode.setOnClickListener(view -> setnightmode());
        int nightvalue = core.getInt("night");
        if (nightvalue==0){
            night.setText(R.string.now1);
        }else if (nightvalue ==1){
            night.setText(R.string.niw2);
        }else{
            night.setText(R.string.now3);
        }
        hide.setChecked(core.getBoolean("hide"));
        debug.setChecked(core.getBoolean("debug"));
        turnoff.setChecked(core.getBoolean("pixie_off"));
        three_wifi.setChecked(core.getBoolean("three_wifi"));
        store.setChecked(core.getBoolean("store_scan"));
        auto.setChecked(core.getBoolean("auto_update"));
        scanwlan.setText(getString(R.string.custom_scan) + core.getString("wlan_scan"));
        deauthwlan.setText(getString(R.string.custom_deauth) + core.getString("wlan_deauth"));

        store.setOnClickListener(view -> {
            store.toggle();
            core.putBoolean("store_scan", store.isChecked());
            core.vibrate(1);
        });
        three_wifi.setOnClickListener(view -> {
            three_wifi.toggle();
            core.putBoolean("three_wifi", three_wifi.isChecked());
            core.vibrate(1);
        });
        auto.setOnClickListener(view -> {
            auto.toggle();
            core.putBoolean("auto_update", auto.isChecked());
            core.vibrate(1);
        });
        debug.setOnClickListener(view -> {
            debug.toggle();
            core.putBoolean("debug", debug.isChecked());
            core.vibrate(1);
        });
        turnoff.setOnClickListener(view -> {
            turnoff.toggle();
            core.putBoolean("pixie_off", turnoff.isChecked());
            core.vibrate(1);
        });
        hide.setOnClickListener(view -> {
            hide.toggle();
            core.putBoolean("hide", hide.isChecked());
            core.vibrate(1);
        });
        module.setOnClickListener(view -> {
            moduledialog();
        });

        scan.setOnClickListener(view -> {
            try {
                getWlanMonitore(true);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        deauth.setOnClickListener(view -> {
            try {
                getWlanMonitore(false);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        unmount.setOnClickListener(view -> {
            try {
                if (unmount()) {
                    core.vibrate(1);
                    System.exit(1);
                } else {
                    toaster("Failed!");
                    core.vibrate(100);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        });
        delete.setOnClickListener(view -> confirm());


        return viewroot;
    }

    public void confirm() {
        AlertDialog show = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.confirm)
                .setMessage(R.string.sure)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    try {
                        if (unmount()) {
                            toaster(core.str("sorry"));
                            new CustomCommand("rm -rf /data/local/stryker&&pm uninstall com.zalexdev.stryker", new Core(context)).execute();
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.dismiss()).show();


    }

    private ArrayList<String> getinterfaces() throws ExecutionException, InterruptedException {
        GetInterfaces airmon = new GetInterfaces(new Core(context));
        return airmon.execute().get();
    }

    public void getWlanMonitore(boolean isscan) throws ExecutionException, InterruptedException {
        ArrayList<String> w = getinterfaces();
        String[] w2 = new String[w.size()+1];
        for (int i = 0; i < w.size(); i++) {
            w2[i] = w.get(i);
        }
        w2[w2.length-1] = core.str("customvalue");
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.pick)
                .setItems(w2, (dialogInterface, i) -> {
                    if (i !=w2.length -1){
                    if (isscan) {
                        core.putString("wlan_scan", w2[i]);
                    } else {
                        core.putString("wlan_deauth", w2[i]);
                    }}else{
                        new Thread(() -> {
                            final String[] temp = {""};
                            activity.runOnUiThread(() -> {
                                final Dialog valuedialog = new Dialog(context);
                                valuedialog.setContentView(R.layout.input_dialog);
                                valuedialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                TextView title = valuedialog.findViewById(R.id.input_title);
                                TextInputEditText valueedit = valuedialog.findViewById(R.id.getvalue);
                                TextView ok = valuedialog.findViewById(R.id.ok_button);
                                title.setText(core.str("customvalue"));
                                ok.setOnClickListener(view1 -> {
                                    temp[0] = Objects.requireNonNull(valueedit.getText()).toString();
                                    valuedialog.dismiss();
                                });
                                valuedialog.show();
                            });
                            while (temp[0].equals("")){
                                Log.d(TAG,"test");
                            }
                            if (isscan) {
                                core.putString("wlan_scan", temp[0]);
                            } else {
                                core.putString("wlan_deauth", temp[0]);
                            }
                            activity.runOnUiThread(() -> {
                                scanwlan.setText(core.str("custom_scan") + core.getString("wlan_scan"));
                                deauthwlan.setText(core.str("custom_deauth") + core.getString("wlan_deauth"));
                            });
                        }).start();

                    }
                    scanwlan.setText(core.str("custom_scan") + core.getString("wlan_scan"));
                    deauthwlan.setText(core.str("custom_deauth") + core.getString("wlan_deauth"));

                })
                .show();
    }
    public void setnightmode() {
        String[] w2 = new String[3];
        w2[0] = core.str("now1");
        w2[1] = core.str("niw2");
        w2[2] = core.str("now3");
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.pcik_mode)
                .setItems(w2, (dialogInterface, i) -> {
                    core.putInt("night",i);
                    if (i==0){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }else if (i ==1){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }else{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                    if (i==0){
                        night.setText(R.string.now1);
                    }else if (i ==1){
                        night.setText(R.string.niw2);
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }else{
                        night.setText(R.string.now3);
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                })
                .show();
    }

    public boolean unmount() throws ExecutionException, InterruptedException {
        return new CustomCommand("/data/data/com.zalexdev.stryker/files/killroot", new Core(context)).execute().get();
    }





    private void moduledialog() {

        ArrayList<String> get = core.getListFiles(new File(core.getStorage() + "Stryker/modules"));
        String[] w2 = new String[get.size()];
        for (int i = 0; i < get.size(); i++) {
            w2[i] = get.get(i).replace(core.getStorage() + "Stryker/modules/", "");
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.sel_module)
                .setItems(w2, (dialogInterface, i) -> {
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.module_progress);
                    dialog.setCancelable(false);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    CircularProgressIndicator prog = dialog.findViewById(R.id.module_prog);
                    TextView title = dialog.findViewById(R.id.module_title);
                    TextView progress = dialog.findViewById(R.id.module_progress);
                    TextView cancel = dialog.findViewById(R.id.module_cancel);
                    cancel.setOnClickListener(view -> dialog.dismiss());
                    ArrayList<String> commands = new ArrayList<>();
                    title.setText(core.str("installing")+w2[i]);
                    try (BufferedReader br = new BufferedReader(new FileReader(get.get(i)))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            commands.add(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int pr = 100 / commands.size();
                    final int[] total = {0};
                    new Thread(() -> {
                        for (String cmd: commands){
                            if (cmd.startsWith("#")){
                                setText(progress,cmd.replace("#",""));
                                setProg(prog, total[0]);
                            }else {
                                try {
                                    Boolean bool = new CustomChrootCommand(cmd, core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                } catch (ExecutionException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                                total[0] = total[0] + pr;
                            }
                        }
                        setProg(prog,100);
                        setText(cancel,"OK");
                        setText(title,core.str("finished"));
                        setText(progress,core.str("installed"));
                    }).start();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                })
                .show();




    }

    public void setText(TextView textView, String text) {
        activity.runOnUiThread(() -> textView.setText(text));
    }

    public void setProg(CircularProgressIndicator progressIndicator, int prog) {
        activity.runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);

            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(prog, true);
            }
        });

    }

    public void toaster(String msg) {
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(context,
                    msg, Toast.LENGTH_SHORT);
            toast.show();
        });

    }

    public Spanned green(String out) {
        return Html.fromHtml("<font color='#19D121'>" + out + "</font>");
    }


    public Spanned red(String out) {
        return Html.fromHtml("<font color='#F60B0B'>" + out + "</font>");
    }
}