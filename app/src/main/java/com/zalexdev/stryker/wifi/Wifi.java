package com.zalexdev.stryker.wifi;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Cabinet;
import com.zalexdev.stryker.custom.WiFINetwork;
import com.zalexdev.stryker.three_wifi.utils.GetWiFI;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.wifi.utils.DisableMonitor;
import com.zalexdev.stryker.wifi.utils.EnableInterface;
import com.zalexdev.stryker.wifi.utils.GetInterfaces;
import com.zalexdev.stryker.wifi.utils.ScanWifi;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Wifi extends Fragment {
    public ArrayList<WiFINetwork> list = new ArrayList<>();
    public SwipeRefreshLayout refresh;
    public LottieAnimationView img;
    public TextView text1;
    public Core core;
    public String wlan;
    private RecyclerView mRecyclerView;
    private WiFI_Adapter mAdapter;
    public Activity activity;
    public Context context;
    public Wifi() {

    }


    public static Wifi newInstance() {
        Wifi fragment = new Wifi();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wifi_fragment, container, false);
        activity = getActivity();
        context = getContext();
        mRecyclerView = view.findViewById(R.id.wifilist);
        refresh = view.findViewById(R.id.refresh);
        img = view.findViewById(R.id.scan_img);
        text1 = view.findViewById(R.id.scan_text);
        core = new Core(context);
        wlan = core.getString("wlan_scan");
        refresh.setOnRefreshListener(this::scan);
        if (activity !=null){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));}
        scan();
        return view;
    }

    public void scan() {
        wlan = core.getString("wlan_scan");
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        ImageView disablemon = toolbar.findViewById(R.id.disablemobn);
        disablemon.setOnClickListener(view -> disable());
        Thread scan = new Thread(() -> {
            try {
                boolean inter;
                ArrayList<String> wlans = core.getInterfacesList();
                if (wlans.contains(wlan+"mon")){wlan = wlan+"mon";}
                if (wlans.contains(wlan)) {
                    inter = true;
                    if (!wlan.equals("wlan0") && wlan.contains("mon")){
                        boolean ok = new DisableMonitor(wlan,core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                        wlan = wlan.replace("mon","");
                        new EnableInterface(wlan, core).execute().get();
                        }else if (!wlan.equals("wlan0")){
                        new EnableInterface(wlan, core).execute().get();
                    }
                    else {
                        inter = wifienabled();
                    }
                    }else{
                    inter = false;
                }
                if (inter) {
                        list = new ScanWifi(wlan, core).execute().get();
                        if (list.isEmpty()) {
                            list = new ScanWifi(wlan, core).execute().get();
                            if (list.isEmpty()) {
                                if (activity !=null) {
                                    activity.runOnUiThread(() -> {
                                        img.setAnimation(R.raw.nothing);
                                        img.playAnimation();
                                        text1.setText(R.string.cant_find_netw);
                                        refresh.setEnabled(true);
                                    });
                                }
                            }
                        }

                        for (int i = 0; i < list.size(); i++) {
                            String mac = list.get(i).getMac();
                            if (!core.getnetwork(mac).isEmpty()) {
                                WiFINetwork w = list.get(i);
                                w.setOK(true);
                                w.setPsk(core.getListString(mac).get(0));
                                if (core.getnetwork(mac).size() > 1){
                                w.setPin(core.getListString(mac).get(1));}
                                list.set(i, w);
                            }
                        }
                        if (list.isEmpty()){
                            activity.runOnUiThread(() -> {
                                img.setAnimation(R.raw.nothing);
                                img.playAnimation();
                                text1.setText(R.string.cant_find_netw);
                                refresh.setEnabled(true);
                            });
                        }
                    else{
                        mAdapter = new WiFI_Adapter(context, activity, list);
                        mAdapter.setHasStableIds(true);
                        activity.runOnUiThread(() -> {
                            img.setVisibility(View.INVISIBLE);
                            text1.setVisibility(View.INVISIBLE);
                            img.clearAnimation();
                            text1.clearAnimation();
                            mRecyclerView.setItemViewCacheSize(255);
                            mRecyclerView.setAdapter(mAdapter);
                            refresh.setRefreshing(false);
                            if (core.getBoolean("three_wifi")) {
                                Cabinet cabinet = new Cabinet(context);
                                cabinet.getStored();
                                if (cabinet.getKeyView().length() > 3) {
                                    int i = 0;
                                    for (WiFINetwork temp : list) {
                                        int finalI = i;
                                        new Thread(() -> {
                                            try {
                                                ArrayList<WiFINetwork> three = new GetWiFI(cabinet.getKeyView(), temp.getMac()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                                if (!three.isEmpty()) {
                                                    temp.setPsk(three.get(0).getPsk());
                                                    temp.setPin(three.get(0).getPin());
                                                    temp.setOK(true);
                                                    temp.setThree(true);
                                                    if (activity !=null){
                                                    activity.runOnUiThread(() -> mAdapter.changeitem(temp, finalI));}
                                                }
                                            } catch (ExecutionException | InterruptedException e) {
                                                e.printStackTrace();
                                            }

                                        }).start();
                                        i++;
                                    }
                                }
                            }
                        });}
                    } else {
                    if (activity !=null){
                        activity.runOnUiThread(() -> text1.setText(getString(R.string.error_inter) +" "+ wlan));}
                    }

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        scan.start();
    }
    public void disable() {
        ArrayList<String> w;
        try {
            w = getinterfaces();

            String[] w2 = new String[w.size()];
            for (int i = 0; i < w.size(); i++) {
                w2[i] = w.get(i);
            }
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.disable_mon)
                    .setItems(w2, (dialogInterface, i) -> {
                        Thread t = new Thread(() -> new DisableMonitor(w2[i], core).execute());
                        t.start();
                    })
                    .show();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public boolean wifienabled() {
        boolean ok = false;
        if (context !=null){
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled()) {
            ok = true;
        }
        }
        return ok;
    }
    private ArrayList<String> getinterfaces() throws ExecutionException, InterruptedException {
        return core.getInterfacesList();
    }
}