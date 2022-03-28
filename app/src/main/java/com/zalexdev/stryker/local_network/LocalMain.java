package com.zalexdev.stryker.local_network;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Device;
import com.zalexdev.stryker.local_network.utils.GetNetworkMask;
import com.zalexdev.stryker.local_network.utils.ScanLocalDevice;
import com.zalexdev.stryker.local_network.utils.ScanLocalNetwork;
import com.zalexdev.stryker.utils.CheckDir;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;
import com.zalexdev.stryker.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

/**
 * It scans the local network for devices and displays them in a list
 */
public class LocalMain extends Fragment {
    public SwipeRefreshLayout refresh;
    public LottieAnimationView img;
    public TextView text;
    public Core core;
    private RecyclerView mRecyclerView;
    private LocalAdapter mAdapter;
    public Context context;
    public Activity activity;
    public LinearProgressIndicator progress;
    public LocalMain() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initalizing
        View view = inflater.inflate(R.layout.local_fragment, container, false);
        context = getContext();
        activity = getActivity();
        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        view.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() {core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });
        mRecyclerView = view.findViewById(R.id.local_list);
        refresh = view.findViewById(R.id.local_refresh);
        img = view.findViewById(R.id.local_img);
        text = view.findViewById(R.id.local_text);
        progress = view.findViewById(R.id.nmap_progressbar);
        if (activity !=null){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));}
        core = new Core(context);
        mRecyclerView.setItemViewCacheSize(255);
        //Check is wifi enabled
        if (wificonnected()) {
            scan();
        } else {
            img.setAnimation(R.raw.error);
            img.playAnimation();
            text.setText(R.string.please_connect);
            progress.setVisibility(View.GONE);
        }

        //Pull to refresh

        refresh.setOnRefreshListener(this::scan);

        return view;
    }


    /**
     * It scans the local network for devices and displays them in a list.
     */
    public void scan() {
        //New thread for scan
        Thread scan = new Thread(() -> {
            try {
                //Getting network mask
                String mask = new GetNetworkMask(core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                mask = mask.substring(0, mask.length() - 3) + "/24";
                if (!new CheckDir("/storage/emulated/0/Stryker/exploits").execute().get()){new CustomCommand("cp -R /data/local/stryker/release/exploits/ /storage/emulated/0/Stryker/exploits",core).execute(); }else{
                core.updateexploits();}
                ArrayList<Device> devices = new ScanLocalNetwork(mask, context,progress,activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                //Check is scan is ok

                int i = 0;
                for (Device d : devices) {
                    int finalI = i;
                    new Thread(() -> {
                        try {
                            if(context!=null) {
                                Device temp = new ScanLocalDevice(d.getIp(), context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                if (temp.getMac().equals(core.str("scanning"))) {
                                    temp.setMac(devices.get(finalI).getMac());
                                    temp.setVendor(devices.get(finalI).getVendor());
                                }
                                devices.set(finalI, temp);
                                mAdapter.changeitem(finalI, temp);
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }

                    }).start();
                    i++;
                }
                if (activity !=null) {
                    activity.runOnUiThread(() -> {
                        if (!devices.isEmpty()) {
                            img.setVisibility(View.INVISIBLE);
                            text.setVisibility(View.INVISIBLE);
                            progress.setVisibility(View.GONE);
                            mAdapter = new LocalAdapter(context, activity, devices);
                            mRecyclerView.setAdapter(mAdapter);
                            refresh.setRefreshing(false);
                        } else { //Display error
                            img.setAnimation(R.raw.error);
                            img.playAnimation();
                            text.setText(R.string.oops);
                            progress.setVisibility(View.GONE);
                        }
                    });
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        scan.start();
    }

    //Check is wifi on method
    public boolean wificonnected() {
        boolean ok = false;
        if (context !=null){
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            ok = true;
        }}
        return ok;
    }


}