package com.zalexdev.stryker.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.WiFINetwork;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.wifi.utils.EnableInterface;
import com.zalexdev.stryker.wifi.utils.GetInterfaces;
import com.zalexdev.stryker.wifi.utils.ScanWifi;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class Wifi extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    public ArrayList<WiFINetwork> list = new ArrayList<>();
    public SwipeRefreshLayout refresh;
    public ImageView img;
    public TextView text1;
    public Core core;
    public String wlan;
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
        mRecyclerView = view.findViewById(R.id.wifilist);
        refresh =  view.findViewById(R.id.refresh);
        img = view.findViewById(R.id.scan_img);
        text1 = view.findViewById(R.id.scan_text);
        core = new Core(getContext());
        wlan = core.getString("wlan_scan");
        Animation fade = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fade.setRepeatCount(Animation.INFINITE);
        img.startAnimation(fade);
        text1.startAnimation(fade);

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scan();

            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        scan();
        return view;
    }
    public void scan(){
        wlan = core.getString("wlan_scan");
        Thread scan = new Thread(() -> {
            try {
                Boolean inter;
                ArrayList<String> wlans = new GetInterfaces(core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                if (wlans.contains(wlan)){
                    if (!wlan.equals("wlan0")){
                        inter = new EnableInterface(wlan,core).execute().get();

                    }else{ inter = wifienabled(); }

                    if (inter){
                    list = new ScanWifi(wlan,core).execute().get();
                    if (list.isEmpty()){
                        list = new ScanWifi(wlan,core).execute().get();
                        if (list.isEmpty()){
                            if (getContext()!=null && getActivity() != null){
                                requireActivity().runOnUiThread(() -> {
                                    img.setImageDrawable(getContext().getDrawable(R.drawable.no_wifi));
                                    img.clearAnimation();
                                    text1.clearAnimation();
                                    text1.setText("Error\nWith interface or wifi turned off");
                                    refresh.setEnabled(true);
                                });}
                        }
                    }
                    if (getContext()!=null && getActivity() != null){
                        mAdapter = new WiFI_Adapter(getContext(),getActivity(),list);
                    requireActivity().runOnUiThread(() -> {
                        img.setVisibility(View.INVISIBLE);
                        text1.setVisibility(View.INVISIBLE);
                        img.clearAnimation();
                        text1.clearAnimation();
                        mRecyclerView.setAdapter(mAdapter);
                        refresh.setRefreshing(false);

                    });}
                    else {
                        if (getContext()!=null && getActivity() != null){
                        requireActivity().runOnUiThread(() -> {
                            img.setImageDrawable(getContext().getDrawable(R.drawable.no_wifi));
                            img.clearAnimation();
                            text1.clearAnimation();
                            text1.setText("Error\nWith interface or wifi turned off");
                            refresh.setEnabled(true);
                        });}
                    }
                }else{
                        if (getContext()!=null && getActivity() != null){
                    requireActivity().runOnUiThread(() -> text1.setText("Error\nInterface "+wlan+" not connected"));}
                }}
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        scan.start();
    }
    public boolean wifienabled(){
        boolean ok = false;
        WifiManager wifi = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled()){
            ok = true;
        }
        return ok;
    }
}