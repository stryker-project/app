package com.zalexdev.stryker.wifi;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.WiFINetwork;
import com.zalexdev.stryker.utils.CheckFile;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;
import com.zalexdev.stryker.utils.MoveFile;
import com.zalexdev.stryker.wifi.utils.BruteWps;
import com.zalexdev.stryker.wifi.utils.CheckHandshake;
import com.zalexdev.stryker.wifi.utils.DisableMonitor;
import com.zalexdev.stryker.wifi.utils.EnableMonitor;
import com.zalexdev.stryker.wifi.utils.GetInterfaces;
import com.zalexdev.stryker.wifi.utils.LaunchAirodump;
import com.zalexdev.stryker.wifi.utils.PixieDust;
import com.zalexdev.stryker.wifi.utils.StartDeauth;
import net.cachapa.expandablelayout.ExpandableLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class WiFI_Adapter extends RecyclerView.Adapter<WiFI_Adapter.ViewHolder> {
    public ArrayList<WiFINetwork> wifilist;
    public Context context;
    public Activity activity;
    public int tag = 0;
    public Timer deauth;
    public Core core;
    public WiFI_Adapter(Context context2, Activity mActivity, ArrayList<WiFINetwork> wifi) {
        context = context2;
        wifilist = wifi;
        activity = mActivity;
        Collections.sort(wifi,new WiFINetwork.WiFIComporator());
        core = new Core(context2);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView wifi_name;
        public TextView wifi_mac;
        public TextView wifi_model;
        public TextView wifi_power;
        public TextView iswps;
        public MaterialCardView card;


        public ViewHolder(View v) {
            super(v);
            wifi_name = v.findViewById(R.id.wifi_name);
            wifi_mac = v.findViewById(R.id.wifi_bssid);
            wifi_model = v.findViewById(R.id.wifi_model);
            wifi_power = v.findViewById(R.id.wifi_power);
            iswps = v.findViewById(R.id.iswps);
            card = v.findViewById(R.id.item);
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.wifi_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        if (!new Core(context).getBoolean("hide")) {
        adapter.wifi_mac.setText(wifilist.get(position).getMac());}
        else{
            adapter.wifi_mac.setText("XX:XX:XX:XX:XX");
        }
        adapter.wifi_power.setText("  "+ (100 - wifilist.get(position).getPower()) +"%");
        if (wifilist.get(position).getIs5hhz()){
            adapter.wifi_name.setText(wifilist.get(position).getSsid());
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                adapter.wifi_name.setText(Html.fromHtml(wifilist.get(position).getSsid()+"<b> 5 GHz</b> ", Html.FROM_HTML_MODE_COMPACT));
            } else {
                adapter.wifi_name.setText(Html.fromHtml(wifilist.get(position).getSsid()+"<b> 5 GHz</b> "));
            }
        }
        if (wifilist.get(position).getModel() != null){
            String modelka = wifilist.get(position).getModel();
            adapter.wifi_model.setText("Model: "+modelka);
        }
        else{
            adapter.wifi_model.setVisibility(View.INVISIBLE);
        }
        if (wifilist.get(position).getWps()){
            adapter.iswps.setTextColor(context.getColor(R.color.green));
        }else{
            adapter.iswps.setTextColor(context.getColor(R.color.red));
        }
        adapter.card.setOnClickListener(view -> WifiDialog(position,wifilist.get(position).getSsid(),wifilist.get(position).getMac(),wifilist.get(position).getChannel(),wifilist.get(position).getWps()));

    }
    private void WifiDialog(int id,String name,String mac,String channel,boolean wps) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context,R.style.AppBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.wifi_bottom);
        TextView name1 = bottomSheetDialog.findViewById(R.id.wifi_name_bottom);
        TextView mac1 = bottomSheetDialog.findViewById(R.id.wifi_mac_bottom);
        TextView res1 = bottomSheetDialog.findViewById(R.id.getedpass);
        TextView res2 = bottomSheetDialog.findViewById(R.id.getedpin);
        TextView brute_speed = bottomSheetDialog.findViewById(R.id.wps_speed);
        TextView brute_percent = bottomSheetDialog.findViewById(R.id.wps_percent);
        TextView brute_now = bottomSheetDialog.findViewById(R.id.wps_now);
        TextView deauther = bottomSheetDialog.findViewById(R.id.deauther);
        TextView try_handshake = bottomSheetDialog.findViewById(R.id.handshake);
        TextView output = bottomSheetDialog.findViewById(R.id.output);
        output.setMovementMethod(new ScrollingMovementMethod());
        Button back = bottomSheetDialog.findViewById(R.id.back);
        Button back_hs = bottomSheetDialog.findViewById(R.id.cancel_hs);
        Button back_deauth = bottomSheetDialog.findViewById(R.id.cancel_deauth);
        Button cancelBrute = bottomSheetDialog.findViewById(R.id.cancel_brute);
        ExpandableLayout exp_main = bottomSheetDialog.findViewById(R.id.expand);
        ExpandableLayout exp_console = bottomSheetDialog.findViewById(R.id.expand_console);
        ExpandableLayout exp_result = bottomSheetDialog.findViewById(R.id.expand_result);
        ExpandableLayout exp_brute = bottomSheetDialog.findViewById(R.id.expand_brute);
        ExpandableLayout exp_handshake = bottomSheetDialog.findViewById(R.id.expand_handshake);
        ExpandableLayout exp_deauth = bottomSheetDialog.findViewById(R.id.expand_deauth);
        back.setOnClickListener(view -> {
            exp_main.expand();
            exp_console.collapse();
            exp_result.collapse();
        });
        TextView pixiedust = bottomSheetDialog.findViewById(R.id.pixie);
        TextView brutewps = bottomSheetDialog.findViewById(R.id.brute);
        if (wps){
            brutewps.setOnClickListener(view -> {
                exp_main.collapse();
                exp_brute.expand();
                BruteWps brute_wps = new BruteWps(activity,brute_percent,brute_speed,brute_now,mac,new Core(context));
                cancelBrute.setEnabled(true);
                cancelBrute.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        exp_main.expand();
                        exp_brute.collapse();
                        try {
                            brute_wps.kill();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Thread t = new Thread(() -> {
                    try {

                        ArrayList<String> result = brute_wps.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                        if (result.size() >1 && result.size() !=3){
                            activity.runOnUiThread(() -> {
                                exp_brute.collapse();
                                exp_result.expand();
                                res2.setText("Unable to get password");
                                res1.setText("Wps pin:"+result.get(2));
                                back.setEnabled(true);
                            });
                        }else if(result.size() == 3){
                            activity.runOnUiThread(() -> {
                                exp_brute.collapse();
                                exp_result.expand();
                                res1.setText("Password: "+result.get(2));
                                res2.setText("WPS pin: "+result.get(1));
                                back.setEnabled(true);
                            });
                        }else if(result.get(0).equals("error")){
                            activity.runOnUiThread(() -> {
                                exp_brute.collapse();
                                exp_result.expand();
                                res1.setText("Oops..");
                                res2.setText("An error has occurred, try turning on the \"do not turn off wi-fi\" option in the settings, or the network signal is very weak");
                                back.setEnabled(true);
                            });

                        }else{
                            activity.runOnUiThread(() -> {
                                exp_brute.collapse();
                                exp_result.expand();
                                res1.setText("Failed");
                                res2.setText("Sorry, this network is not vulnerable or WPS is blocked");
                                back.setEnabled(true);
                            });
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                t.start();

            });
        }else{
            brutewps.setOnClickListener(view -> toaster("Ooops, this network doesn`t have wps enabled"));
        }
        if (wps){
            back.setOnClickListener(view -> {
                exp_main.expand();
                exp_console.collapse();
                exp_result.collapse();
                new CustomCommand("svc wifi enable",core).execute();
            });
        pixiedust.setOnClickListener(view -> {
            exp_main.toggle();
            exp_console.toggle();
            Button connect = bottomSheetDialog.findViewById(R.id.connect);
            bottomSheetDialog.setCancelable(false);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    PixieDust pixie = new PixieDust(context,activity,output,mac,name,new Core(context));
                    try {
                        ArrayList<String> result = pixie.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                        bottomSheetDialog.setCancelable(true);

                        if (result.size() >1 && result.size() !=3){
                            activity.runOnUiThread(() -> {
                                exp_console.collapse();
                                exp_result.expand();
                                res2.setText("Unable to get password");
                                res1.setText("Wps pin:"+result.get(2));
                                back.setEnabled(true);
                            });
                        }else if(result.size() == 3){
                            activity.runOnUiThread(() -> {
                                exp_console.collapse();
                                exp_result.expand();
                                res1.setText("Password: "+result.get(2));
                                res2.setText("WPS pin: "+result.get(1));
                                back.setEnabled(true);
                            });
                        }else if(result.get(0).equals("error")){
                            activity.runOnUiThread(() -> {
                                exp_console.collapse();
                                exp_result.expand();
                                res1.setText("Oops..");
                                res2.setText("An error has occurred, try turning on the \"do not turn off wi-fi\" option in the settings, or the network signal is very weak");
                                back.setEnabled(true);
                            });

                        }else{
                            activity.runOnUiThread(() -> {
                                exp_console.collapse();
                                exp_result.expand();
                                res1.setText("Failed");
                                res2.setText("Sorry, this network is not vulnerable or WPS is blocked");
                                back.setEnabled(true);
                            });
                        }

                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();

        });}else{
            pixiedust.setOnClickListener(view -> toaster("Ooops, this network doesn`t have wps enabled"));
        }
        try_handshake.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String wlan_listen = core.getString("wlan_scan");
                String wlan_deauth = core.getString("wlan_deauth");


                TextView output_hs = bottomSheetDialog.findViewById(R.id.hs_output);
                exp_main.collapse();
                exp_handshake.expand();
                output_hs.setMovementMethod(new ScrollingMovementMethod());
                back_hs.setEnabled(true);
                back_hs.setOnClickListener(view13 -> {
                    exp_main.expand();
                    exp_handshake.collapse();
                    exp_result.collapse();
                    deauth.cancel();
                    appendtext("Disabling monitore mode, please wait!",output_hs);
                    new DisableMonitor(wlan_listen,core).execute();
                    if (!wlan_listen.equals(wlan_deauth)){
                        new DisableMonitor(wlan_deauth,core).execute();}
                });
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new CustomCommand("rm /storage/emulated/0/Stryker/hs/handshake-01.cap",new Core(context)).execute();
                            if (wlan_listen.equals(wlan_deauth) && wlan_deauth.equals("wlan0")) {
                                final Boolean[] success = {false};
                                appendtext("Trying put wlan0 into monitor mode...\n", output_hs);
                                Boolean listen = new EnableMonitor(wlan_listen, channel,new Core(context)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                if (listen) {
                                    appendtext("Starting capturing handshake...\nMode: Passive\nInterface: wlan0" + "\n", output_hs);
                                    appendtext("Starting airodump-ng" + "\n", output_hs);

                                    LaunchAirodump airodump = new LaunchAirodump(mac,wlan_listen,new Core(context));
                                    airodump.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    back_hs.setOnClickListener(view1 -> {
                                        airodump.kill();
                                        exp_handshake.collapse();
                                        exp_main.expand();
                                    });
                                    Timer cowpatty = new Timer();
                                    cowpatty.scheduleAtFixedRate(new TimerTask() {
                                        @Override
                                        public void run() {
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
                                                            appendtext("Oops.. No handshake...\n", output_hs);
                                                        } else {
                                                            appendtext("Handshake captured!\n", output_hs);
                                                        }
                                                    } else {
                                                        deauth.cancel();


                                                        MoveFile moveFile = new MoveFile("/storage/emulated/0/Stryker/hs/handshake-01.cap", "/storage/emulated/0/Stryker/hs/" + name + "(" + mac + ").cap");
                                                        try {
                                                            Boolean moved = moveFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                                            if (moved) {
                                                                appendtext("Saved to " + "/storage/emulated/0/Stryker/hs/" + name + " (" + mac + ").cap\n", output_hs);

                                                            } else {
                                                                appendtext("Error saving file... please report it!", output_hs);
                                                            }
                                                        } catch (ExecutionException | InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                        airodump.kill();
                                                        cowpatty.cancel();
                                                        new DisableMonitor(wlan_listen,new Core(context)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                                    }
                                                } else {
                                                    appendtext("Oops something went wrong with starting airodump-ng!..", output_hs);
                                                }
                                            } catch (ExecutionException | InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, 4000, 5000);
                                }


                            } else {
                                final Boolean[] success = {false};
                                if (new GetInterfaces(new Core(context)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get().contains(wlan_deauth)) {
                                    appendtext("Trying put interfaces into monitor mode...\n", output_hs);
                                    EnableMonitor monitor = new EnableMonitor(wlan_listen, channel,new Core(context));
                                    Boolean listen = monitor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                    Boolean listen2 = false;
                                   if (listen && wlan_deauth.equals(wlan_listen)){
                                       listen2 = true;
                                   }else{
                                       EnableMonitor monitor2 = new EnableMonitor(wlan_deauth, channel,new Core(context));
                                       listen2 = monitor2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                   }
                                    if (listen && listen2) {
                                        appendtext("Starting capturing handshake...\nMode: Aggressive"  + "\n", output_hs);
                                        LaunchAirodump airodump = new LaunchAirodump(mac,wlan_listen,new Core(context));
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
                                                                appendtext("Oops.. No handshake...\n", output_hs);
                                                            } else {
                                                                appendtext("Handshake captured!\n", output_hs);
                                                            }
                                                        } else {
                                                            deauth.cancel();

                                                            MoveFile moveFile = new MoveFile("/storage/emulated/0/Stryker/hs/handshake-01.cap", "/storage/emulated/0/Stryker/captured/"+name.replaceAll("\\s+","") +"_"+mac+".cap");
                                                            try {
                                                                Boolean moved = moveFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                                                if (moved) {
                                                                    appendtext("Saved to " + "/storage/emulated/0/Stryker/captured/" + name + "_" + mac + ".cap\n", output_hs);

                                                                } else {
                                                                    appendtext("Error saving file... please report it!", output_hs);
                                                                }
                                                            } catch (ExecutionException | InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                            airodump.kill();
                                                            cowpatty.cancel();
                                                            if (wlan_listen.equals("wlan0")){
                                                                DisableMonitor disableMonitor = new DisableMonitor(wlan_listen,new Core(context));
                                                                disableMonitor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                                            }
                                                        }
                                                    } else {
                                                        appendtext("Oops something went wrong with starting airodump-ng!..", output_hs);
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
                                                StartDeauth startDeauth = new StartDeauth(mac,wlan_deauth,true,new Core(context));
                                                try {
                                                    Boolean isok = startDeauth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                                    if (isok){
                                                        appendtext("Deauthing...\n",output_hs);
                                                    }else{
                                                        appendtext("Oops something went wrong with deauth, passive mode now...\n",output_hs);
                                                        deauth.cancel();
                                                    }
                                                } catch (ExecutionException | InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        },10000,20000);
                                    }else {
                                        appendtext("[E] Error puting interface in monitor mode\n",output_hs);
                                    }
                                }else{
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            exp_handshake.collapse();
                                            res1.setText("Error");
                                            res2.setText("No deauth interface found\nChange interface in settings or connect adapter and try again");
                                            back.setEnabled(true);
                                            exp_result.expand();
                                        }
                                    });

                                }

                            }

                            } catch(ExecutionException | InterruptedException e){
                                e.printStackTrace();
                            }

                    }

                });
                t.start();
            }
        });
        deauther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String wlan_deauth = core.getString("wlan_deauth");
                back.setOnClickListener(view13 -> {
                    exp_main.expand();
                    exp_deauth.collapse();
                    exp_result.collapse();
                    new DisableMonitor(wlan_deauth,core).execute();
                });
                exp_deauth.expand();
                exp_main.collapse();
                GetInterfaces getInterfaces = new GetInterfaces(new Core(context));

                try {
                    if (!wlan_deauth.equals("wlan0")){
                    if (getInterfaces.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get().contains(wlan_deauth)){
                        EnableMonitor enableMonitor = new EnableMonitor(wlan_deauth,channel,new Core(context));
                        if (enableMonitor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get()){
                            StartDeauth startDeauth = new StartDeauth(mac,wlan_deauth,false,new Core(context));
                            back_deauth.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    exp_deauth.collapse();
                                    exp_main.expand();
                                    startDeauth.kill();
                                }
                            });
                            back_deauth.setEnabled(true);

                            startDeauth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }else{
                            back.setEnabled(true);
                            exp_deauth.collapse();
                            exp_result.expand();
                            res1.setText("Error");
                            res2.setText("Error enabling monitor mode...");
                        }
                    }else {
                        back.setEnabled(true);
                        exp_deauth.collapse();
                        exp_result.expand();
                        res1.setText("Error");
                        res2.setText("Deauth interface not connected...");

                    }}else {
                        back.setEnabled(true);
                        exp_deauth.collapse();
                        exp_result.expand();
                        res1.setText("Error");
                        res2.setText("Wlan0 can`t be deauth interface...");
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        name1.setText(name);
        if (!new Core(context).getBoolean("hide")) {
        mac1.setText(mac);}
        else {
            mac1.setText("XX:XX:XX:XX:XX");
        }
        bottomSheetDialog.show();
    }

    @Override
    public int getItemCount() {

        return wifilist.size();
    }
    public void toaster(String msg){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context,
                        msg, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }
    public void appendtext(String text,TextView output){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                output.append(text);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}
