package com.zalexdev.stryker.wifi;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.WiFiNetwork;
import com.zalexdev.stryker.handshakes.utils.BruteHandshake;
import com.zalexdev.stryker.utils.CheckFile;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;
import com.zalexdev.stryker.utils.MoveFile;
import com.zalexdev.stryker.wifi.utils.BrutePsk;
import com.zalexdev.stryker.wifi.utils.BruteWps;
import com.zalexdev.stryker.wifi.utils.CheckHandshake;
import com.zalexdev.stryker.wifi.utils.CustomPin;
import com.zalexdev.stryker.wifi.utils.DisableMonitor;
import com.zalexdev.stryker.wifi.utils.EnableMonitor;
import com.zalexdev.stryker.wifi.utils.GetInterfaces;
import com.zalexdev.stryker.wifi.utils.LaunchAirodump;
import com.zalexdev.stryker.wifi.utils.PixieDust;
import com.zalexdev.stryker.wifi.utils.StartDeauth;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class WiFI_Adapter extends RecyclerView.Adapter<WiFI_Adapter.ViewHolder> {
    public ArrayList<WiFiNetwork> wifilist;
    public Context context;
    public Activity activity;
    public int tag = 0;
    public Timer deauth;
    public Core core;
    public boolean three_wifi;

    public WiFI_Adapter(Context context2, Activity mActivity, ArrayList<WiFiNetwork> wifi) {
        context = context2;
        wifilist = wifi;
        activity = mActivity;
        try {Collections.sort(wifi, new WiFiNetwork.WiFIComporator());}
        catch (Exception ignored){}
        core = new Core(context2);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.wifi_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        if (!new Core(context).getBoolean("hide")) {
            adapter.wifi_mac.setText(wifilist.get(position).getMac().toUpperCase(Locale.ROOT));
        } else {
            adapter.wifi_mac.setText("XX:XX:XX:XX:XX");
        }
        adapter.wifi_power.setText("  " + (100 - wifilist.get(position).getPower()) + "%");
        if (wifilist.get(position).getIs5hhz()) {
            adapter.wifi_name.setText(wifilist.get(position).getSsid());
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                adapter.wifi_name.setText(Html.fromHtml(wifilist.get(position).getSsid() + " <b>⑤</b>", Html.FROM_HTML_MODE_COMPACT));
            } else {
                adapter.wifi_name.setText(Html.fromHtml(wifilist.get(position).getSsid() + " <b>⑤</b>"));
            }
        }

        if (wifilist.get(position).getWps() && !wifilist.get(position).getBlocked()) {
            adapter.iswps.setTextColor(context.getColor(R.color.green));
        } else if (wifilist.get(position).getBlocked()) {
            adapter.iswps.setTextColor(context.getColor(R.color.red));
            adapter.iswps.setText("Locked");
        } else {
            adapter.iswps.setTextColor(context.getColor(R.color.red));
        }
        if (wifilist.get(position).getOK()) {
            adapter.wifi_name.setTextColor(Color.parseColor("#FF1B5E20"));
            adapter.icon.setVisibility(View.VISIBLE);
            if (wifilist.get(position).isThree()){
                adapter.icon.setImageDrawable(context.getDrawable(R.drawable.three_wifi_database));
            }
        }
        if (wifilist.get(position).getModel() != null) {
            String modelka = wifilist.get(position).getModel();
            adapter.wifi_model.setText("Model: " + modelka);
            if (core.checkmodel(modelka)){adapter.icon.setVisibility(View.VISIBLE);adapter.icon.setImageDrawable(context.getDrawable(R.drawable.star));
            }
        } else {
            adapter.wifi_model.setVisibility(View.INVISIBLE);
        }
        adapter.card.setOnClickListener(view -> WifiDialog(wifilist.get(position)));

    }

    private void WifiDialog(WiFiNetwork selected) {
        String name = selected.getSsid();
        String mac = selected.getMac();
        String channel = selected.getChannel();
        boolean wps = selected.getWps();
        boolean blocked = selected.getBlocked();
        boolean three_wifi = selected.getOK();
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.wifi_bottom);

        TextView name1 = bottomSheetDialog.findViewById(R.id.wifi_name_bottom);
        TextView mac1 = bottomSheetDialog.findViewById(R.id.wifi_mac_bottom);
        TextView res1 = bottomSheetDialog.findViewById(R.id.getedpass);
        TextView res2 = bottomSheetDialog.findViewById(R.id.getedpin);
        TextView output = bottomSheetDialog.findViewById(R.id.output);
        String wlan_listen = core.getString("wlan_scan");
        final String[] wlan_deauth = {core.getString("wlan_deauth")};
        LinearLayout deauther = bottomSheetDialog.findViewById(R.id.deauther);
        LinearLayout try_handshake = bottomSheetDialog.findViewById(R.id.handshake);
        LinearLayout custom_pin = bottomSheetDialog.findViewById(R.id.custom_pin);
        LinearLayout brute_psk = bottomSheetDialog.findViewById(R.id.brute_psk);
        Button back = bottomSheetDialog.findViewById(R.id.back);
        Button main_cancel = bottomSheetDialog.findViewById(R.id.cancel_attack);

        ExpandableLayout exp_main = bottomSheetDialog.findViewById(R.id.expand);
        ExpandableLayout exp_attack = bottomSheetDialog.findViewById(R.id.expand_console);
        ExpandableLayout exp_result = bottomSheetDialog.findViewById(R.id.expand_result);

        brute_psk.setOnClickListener(view -> {
            output.setText(R.string.start_brute);
            final BrutePsk[] brute = {null};
            main_cancel.setOnClickListener(view1 -> {
                if (brute[0] !=null){
                    brute[0].kill();
                    exp_attack.collapse();
                    exp_main.expand();
                }
            });
            ArrayList<String> get = core.getListFiles(new File(core.getStorage() + "Stryker/wordlist"));
            if (!get.isEmpty()){
            String[] w2 = new String[get.size()];
            for (int i = 0; i < get.size(); i++) {
                w2[i] = get.get(i).replace(core.getStorage() + "Stryker/wordlist/", "");
            }
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.select_word2)
                    .setItems(w2, (dialogInterface, i) -> {
                        String path = get.get(i);
                        core.toaster(path);
                        new Thread(() -> {
                            try {
                                ArrayList<String> temp = new ArrayList<>();

                                brute[0] = new BrutePsk(activity,output,name,core,path);
                                WiFiNetwork w = brute[0].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                if (w.getOK()){
                                    settext(core.str("suc_pass")+w.getPsk(), output);
                                    core.savenetwork(mac,w.getPsk(),"-");
                                }else {
                                    settext(core.str("br_failed"),output);
                                }
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    })
                    .show();exp_attack.expand();
                exp_main.collapse();}
            else{
            exp_attack.expand();
            exp_main.collapse();
            output.setText(R.string.error_no_word);
            main_cancel.setOnClickListener(view12 -> {
                exp_attack.collapse();
                exp_main.expand();
            });
            }
        });
        custom_pin.setOnClickListener(view -> {
            output.setText(R.string.trying_connect);
            final Dialog valuedialog = new Dialog(context);
            valuedialog.setContentView(R.layout.input_dialog);
            valuedialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            TextView title = valuedialog.findViewById(R.id.input_title);
            TextInputEditText valueedit = valuedialog.findViewById(R.id.getvalue);
            TextView ok = valuedialog.findViewById(R.id.ok_button);
            title.setText(R.string.enter_pin);
            ok.setOnClickListener(view1 -> {
                String value = valueedit.getText().toString();
                valuedialog.dismiss();
                exp_main.collapse();
                exp_attack.expand();
                new Thread(() -> {
                    settext(core.str("try_connect"), output);
                    try {
                        WiFiNetwork w = new CustomPin(value,activity,output,mac,wlan_listen,core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                        if (w.getOK()){
                            activity.runOnUiThread(() -> {
                                exp_result.expand();
                                exp_attack.collapse();
                                res1.setText(core.str("pass")+w.getPsk());
                                core.savenetwork(mac,w.getPsk(),w.getPin());
                                res2.setText("");
                            });
                        }else{
                            activity.runOnUiThread(() -> {
                                exp_result.expand();
                                exp_attack.collapse();
                                res1.setText(R.string.error);
                                res2.setText(R.string.incorrect);
                            });
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            });
            valuedialog.show();
        });
        assert back != null;
        back.setOnClickListener(view -> {
            exp_main.expand();
            exp_attack.collapse();
            exp_result.collapse();
        });
        if (three_wifi) {
            exp_main.collapse();
            exp_result.expand();
            res1.setText(core.str("pass") + selected.getPsk());
            res2.setText(core.str("pin") + selected.getPin());
            back.setEnabled(true);
        }
        LinearLayout pixiedust = bottomSheetDialog.findViewById(R.id.pixie);
        LinearLayout brutewps = bottomSheetDialog.findViewById(R.id.brute);
        if (wps && !blocked) {
            final BruteWps[] brute_wps = {null};
            brutewps.setOnClickListener(view -> {
                output.setText(R.string.start_brute);
                bottomSheetDialog.setOnDismissListener(dialogInterface -> {
                    new CustomCommand("svc wifi enable", core).execute();
                    if (brute_wps[0] != null) {
                        brute_wps[0].kill();
                    }

                });
                main_cancel.setOnClickListener(view2 -> {
                    exp_main.expand();
                    exp_attack.collapse();
                    exp_result.collapse();
                    new CustomCommand("svc wifi enable", core).execute();
                    if (brute_wps[0] != null) {
                        brute_wps[0].kill();
                    }

                });
                exp_main.collapse();
                exp_attack.expand();
                brute_wps[0] = new BruteWps(activity, output, mac, wlan_listen, new Core(context));
                main_cancel.setEnabled(true);
                Thread t = new Thread(() -> {
                    try {

                        WiFiNetwork result = brute_wps[0].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();

                        if (!result.isCanceled()) {
                            if (result.getOK()) {
                                activity.runOnUiThread(() -> {
                                    exp_attack.collapse();
                                    exp_result.expand();
                                    back.setEnabled(true);
                                    if (result.getPsk() == null) {
                                        res2.setVisibility(View.GONE);
                                        res1.setText(core.str("pin") + result.getPin());
                                        core.savenetwork(mac,"-",result.getPin());
                                    } else {

                                        res1.setText(core.str("pass")+ result.getPsk());
                                        res2.setText(core.str("pin") + result.getPin());
                                        core.savenetwork(mac,result.getPsk(),result.getPin());
                                    }
                                });
                            } else {
                                activity.runOnUiThread(() -> {
                                    exp_attack.collapse();
                                    exp_result.expand();
                                    if (result.getLon() != null) {
                                        res1.setText(R.string.ooops_sh);
                                        res2.setText(core.str("error_interface") + wlan_listen + core.str("dev_issue"));
                                    } else {
                                        res1.setText(R.string.ooops_sh);
                                        res2.setText(R.string.not_vuln_pixie);
                                    }
                                    back.setEnabled(true);
                                });
                            }
                        } else {
                            activity.runOnUiThread(() -> {
                                exp_attack.collapse();
                                exp_result.collapse();
                                exp_main.expand();
                            });
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                t.start();

            });
            final PixieDust[] pixie = {null};
            pixiedust.setOnClickListener(view -> {
                output.setText(R.string.start_pixie);
                bottomSheetDialog.setOnDismissListener(dialogInterface -> {
                    new CustomCommand("svc wifi enable", core).execute();
                    pixie[0].kill();
                });
                main_cancel.setOnClickListener(view2 -> {
                    exp_main.expand();
                    exp_attack.collapse();
                    exp_result.collapse();
                    new CustomCommand("svc wifi enable", core).execute();
                    pixie[0].kill();
                });
                exp_main.collapse();
                exp_attack.expand();
                Button connect = bottomSheetDialog.findViewById(R.id.connect);
                Thread t = new Thread(() -> {
                    pixie[0] = new PixieDust(context, activity, output, mac, name, new Core(context));
                    try {
                        WiFiNetwork result = pixie[0].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                        if (!result.isCanceled()) {
                            if (result.getOK()) {
                                activity.runOnUiThread(() -> {
                                    exp_attack.collapse();
                                    exp_result.expand();
                                    back.setEnabled(true);
                                    if (result.getPsk() == null) {
                                        res2.setVisibility(View.GONE);
                                        res1.setText(core.str("pin") + result.getPin());
                                        core.savenetwork(mac,"-",result.getPin());
                                    } else {
                                        if (!name.equals("Hidden network")){
                                        connect.setVisibility(View.VISIBLE);
                                        connect.setOnClickListener(view15 -> core.connectWiFi2(name,result.getPsk()));}
                                        res1.setText(core.str("pass") + result.getPsk());
                                        res2.setText(core.str("pin") + result.getPin());
                                        core.savenetwork(mac,result.getPsk(),result.getPin());
                                    }
                                });
                            } else {
                                activity.runOnUiThread(() -> {
                                    exp_attack.collapse();
                                    exp_result.expand();
                                    if (result.getLon() != null) {
                                        res1.setText(R.string.ooops_sh);
                                        res2.setText(R.string.error_interface + wlan_listen + R.string.dev_issue);
                                    } else {
                                        res1.setText(R.string.ooops_sh);
                                        res2.setText(R.string.not_vuln_pixie);
                                    }
                                    back.setEnabled(true);
                                });
                            }
                        } else {
                            activity.runOnUiThread(() -> {
                                exp_attack.collapse();
                                exp_result.collapse();
                                exp_main.expand();
                            });
                        }

                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                t.start();

            });
        } else {
            brutewps.setVisibility(View.GONE);
            pixiedust.setVisibility(View.GONE);
            custom_pin.setVisibility(View.GONE);
        }
        try_handshake.setOnClickListener(view -> {
            output.setText(R.string.start_airdump);
            exp_main.collapse();
            exp_attack.expand();
            main_cancel.setEnabled(true);
            main_cancel.setOnClickListener(view13 -> {
                exp_main.expand();
                exp_attack.collapse();
                exp_result.collapse();
                if (deauth != null) {
                    deauth.cancel();
                }
                new DisableMonitor(wlan_listen, core).execute();
                if (!wlan_listen.equals(wlan_deauth[0])) {
                    new DisableMonitor(wlan_deauth[0], core).execute();
                }
            });
            Thread t = new Thread(() -> {
                try {
                    new CustomCommand("rm /storage/emulated/0/Stryker/hs/handshake-01.cap", new Core(context)).execute();
                    final Boolean[] success = {false};
                    if (wlan_listen.equals(wlan_deauth[0]) && wlan_deauth[0].equals("wlan0")) {
                        settext(core.str("try_wlan0"), output);
                        Boolean listen = new EnableMonitor(wlan_listen, channel, new Core(context)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                        if (listen) {
                            settext(core.str("start_dump") + "\n", output);
                            LaunchAirodump airodump = new LaunchAirodump(mac, wlan_listen, new Core(context));
                            airodump.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            bottomSheetDialog.setOnDismissListener(dialogInterface -> airodump.kill());
                            Timer cowpatty = new Timer();
                            final int[] time = {0};

                            cowpatty.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    time[0] += 5;
                                    CheckFile checkFile = new CheckFile("/storage/emulated/0/Stryker/hs/handshake-01.cap");
                                    try {
                                        if (checkFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get()) {
                                            if (!success[0]) {
                                                try {

                                                    success[0] = new CheckHandshake().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();

                                                } catch (ExecutionException | InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                if (!success[0]) {
                                                    settext("[" + time[0] + core.str("wait_hs"), output);
                                                } else {
                                                    settext(core.str("hs_captured"), output);
                                                }
                                            } else {
                                                deauth.cancel();
                                                MoveFile moveFile = new MoveFile("/storage/emulated/0/Stryker/hs/handshake-01.cap", "/storage/emulated/0/Stryker/hs/" + name + "(" + mac + ").cap");
                                                try {
                                                    Boolean moved = moveFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                                    if (moved) {
                                                        settext(core.str("saved_to_hs") + "/storage/emulated/0/Stryker/hs/" + name + " (" + mac + ").cap\n", output);

                                                    } else {
                                                        settext(core.str("error_save_hs"), output);
                                                    }
                                                } catch (ExecutionException | InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                airodump.kill();
                                                cowpatty.cancel();
                                                new DisableMonitor(wlan_listen, new Core(context)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            }
                                        } else {
                                            settext(core.str("cant_airodump"), output);
                                        }
                                    } catch (ExecutionException | InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 4000, 5000);
                        }


                    } else {
                        if (new GetInterfaces(new Core(context)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get().contains(wlan_deauth[0])) {
                            settext(core.str("trying_put_inter"), output);
                            EnableMonitor monitor = new EnableMonitor(wlan_listen, channel, new Core(context));
                            Boolean listen = monitor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                            Boolean listen2;
                            if (listen && wlan_deauth[0].equals(wlan_listen)) {
                                listen2 = true;
                            } else {
                                EnableMonitor monitor2 = new EnableMonitor(wlan_deauth[0], channel, new Core(context));
                                listen2 = monitor2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                            }
                            if (listen && listen2) {
                                settext(core.str("start_airdump") + "\n", output);
                                LaunchAirodump airodump = new LaunchAirodump(mac, wlan_listen, new Core(context));

                                airodump.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                                Timer cowpatty = new Timer();
                                cowpatty.scheduleAtFixedRate(new TimerTask() {
                                    @Override
                                    public void run() {
                                        CheckFile checkFile = new CheckFile("/storage/emulated/0/Stryker/hs/handshake-01.cap");
                                        try {

                                            if (checkFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get()) {
                                                if (!success[0]) {
                                                    try {
                                                        CheckHandshake check_hs = new CheckHandshake();
                                                        success[0] = check_hs.execute().get();

                                                    } catch (ExecutionException | InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                    if (!success[0]) {
                                                        settext(core.str("wait_hs2"), output);
                                                    } else {
                                                        settext(core.str("hs_captured"), output);
                                                        if (deauth != null) {
                                                            deauth.cancel();
                                                        }
                                                    }
                                                } else {


                                                    MoveFile moveFile = new MoveFile("/storage/emulated/0/Stryker/hs/handshake-01.cap", "/storage/emulated/0/Stryker/captured/" + name.replaceAll("\\s+", "") + "_" + mac + ".cap");
                                                    try {
                                                        Boolean moved = moveFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                                        if (moved) {
                                                            settext(core.str("saved_to_hs") + "/storage/emulated/0/Stryker/captured/" + name + "_" + mac + ".cap\n", output);

                                                        } else {
                                                            settext(core.str("error_save_hs"), output);
                                                        }
                                                    } catch (ExecutionException | InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                    airodump.kill();
                                                    cowpatty.cancel();

                                                        DisableMonitor disableMonitor = new DisableMonitor(wlan_listen, new Core(context));
                                                        disableMonitor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                                    if (!wlan_deauth[0].equals(wlan_listen)){
                                                        DisableMonitor disableMonitor2 = new DisableMonitor(wlan_deauth[0], new Core(context));
                                                        disableMonitor2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                                    }
                                                }
                                            } else {
                                                settext(core.str("cant_airodump"), output);
                                                cowpatty.cancel();
                                            }
                                        } catch (ExecutionException | InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 5000, 5000);
                                deauth = new Timer();
                                deauth.scheduleAtFixedRate(new TimerTask() {
                                    @Override
                                    public void run() {
                                        StartDeauth startDeauth = new StartDeauth(mac, wlan_deauth[0], true, new Core(context));
                                        try {
                                            Boolean isok = startDeauth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                            if (isok) {
                                                settext(core.str("deauthing"), output);
                                            } else {
                                                settext(core.str("went_wrog_play"), output);
                                                deauth.cancel();
                                            }
                                        } catch (ExecutionException | InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 10000, 20000);
                            } else {
                                settext(core.str("monit"), output);
                            }
                        } else {
                            activity.runOnUiThread(() -> {
                                exp_attack.collapse();
                                res1.setText(R.string.error);
                                res2.setText(R.string.no_deauth_int);
                                back.setEnabled(true);
                                exp_result.expand();
                            });

                        }

                    }

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

            });
            t.start();
        });
        deauther.setOnClickListener(view -> {
            output.setText(R.string.deauth);
            bottomSheetDialog.setOnDismissListener(dialogInterface -> new DisableMonitor(wlan_deauth[0], core).execute());
            exp_attack.expand();
            exp_main.collapse();
            GetInterfaces getInterfaces = new GetInterfaces(new Core(context));

            try {
                if (!wlan_deauth[0].equals("wlan0")) {
                    ArrayList<String> wlans = getInterfaces.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                    if (wlans.contains(wlan_deauth[0] +"mon")){
                        wlan_deauth[0] = wlan_deauth[0] +"mon";}
                    if (wlans.contains(wlan_deauth[0])) {
                        EnableMonitor enableMonitor = new EnableMonitor(wlan_deauth[0], channel, new Core(context));
                        if (enableMonitor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get()) {
                            StartDeauth startDeauth = new StartDeauth(mac, wlan_deauth[0], false, new Core(context));
                            main_cancel.setOnClickListener(view14 -> {
                                exp_attack.collapse();
                                exp_main.expand();
                                startDeauth.kill();
                                new DisableMonitor(wlan_deauth[0], core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            });
                            main_cancel.setEnabled(true);
                            settext(core.str("deauthing"), output);
                            startDeauth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            back.setEnabled(true);
                            exp_attack.collapse();
                            exp_result.expand();
                            res1.setText(R.string.error);
                            res2.setText(R.string.error_monit);
                        }
                    } else {
                        back.setEnabled(true);
                        exp_attack.collapse();
                        exp_result.expand();
                        res1.setText(R.string.error);
                        res2.setText(R.string.error_interface);

                    }
                } else {
                    back.setEnabled(true);
                    exp_attack.collapse();
                    exp_result.expand();
                    res1.setText(R.string.error);
                    res2.setText(R.string.no_wlan0);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        name1.setText(name);
        if (!new Core(context).getBoolean("hide")) {
            mac1.setText(mac);
        } else {
            mac1.setText("XX:XX:XX:XX:XX");
        }
        bottomSheetDialog.show();

    }

    @Override
    public int getItemCount() {

        return wifilist.size();
    }

    public void toaster(String msg) {
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(context,
                    msg, Toast.LENGTH_SHORT);
            toast.show();
        });

    }

    public void settext(String text, TextView output) {
        activity.runOnUiThread(() -> output.setText(text));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void changeitem(WiFiNetwork temp, int pos) {
        activity.runOnUiThread(() -> {
            wifilist.set(pos, temp);
            notifyItemChanged(pos);

        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView wifi_name;
        public TextView wifi_mac;
        public TextView wifi_model;
        public TextView wifi_power;
        public TextView iswps;
        public MaterialCardView card;
        public ImageView icon;


        public ViewHolder(View v) {
            super(v);
            wifi_name = v.findViewById(R.id.wifi_name);
            wifi_mac = v.findViewById(R.id.wifi_bssid);
            wifi_model = v.findViewById(R.id.wifi_model);
            wifi_power = v.findViewById(R.id.wifi_power);
            iswps = v.findViewById(R.id.iswps);
            card = v.findViewById(R.id.item);
            icon = v.findViewById(R.id.icon_wifi);
        }

    }

}
