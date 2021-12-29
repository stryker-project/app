package com.zalexdev.stryker.local;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Device;
import com.zalexdev.stryker.local.utils.GetNetworkMask;
import com.zalexdev.stryker.local.utils.ScanLocalNetwork;
import com.zalexdev.stryker.utils.Core;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class LocalMain extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    public SwipeRefreshLayout refresh;
    public ImageView img;
    public TextView text;
    public Core core;
    public LocalMain() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initalizing
        View view = inflater.inflate(R.layout.local_fragment, container, false);
        mRecyclerView = view.findViewById(R.id.local_list);
        refresh =  view.findViewById(R.id.local_refresh);
        img = view.findViewById(R.id.local_img);
        text = view.findViewById(R.id.local_text);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        core = new Core(getContext());

        //Adding animations
        Animation fade = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fade.setRepeatCount(Animation.INFINITE);
        img.startAnimation(fade);
        text.startAnimation(fade);


        //Check is wifi enabled
        if (wificonnected()){ scan();}
        else {
            img.setImageDrawable(getContext().getDrawable(R.drawable.no_wifi));
            text.setText("Please connect to wifi network first!"); }


        //Pull to refresh
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scan();
            }
        });

        return view;
    }

    public void scan(){
        //New thread for scan
        Thread scan = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Getting network mask
                    String mask = new GetNetworkMask(core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();

                    //Scaning mask with nmap
                    ArrayList<Device> devices = new ScanLocalNetwork(mask,getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();

                    //Check is scan is ok
                    if (getActivity()!=null){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!devices.isEmpty()){
                            img.clearAnimation();
                            text.clearAnimation();
                            img.setVisibility(View.INVISIBLE);
                            text.setVisibility(View.INVISIBLE);
                            mAdapter = new LocalAdapter(getContext(),getActivity(),devices);
                            mRecyclerView.setAdapter(mAdapter);
                            refresh.setRefreshing(false);
                            }
                            else{ //Display error
                                img.clearAnimation();
                                text.clearAnimation();
                                img.setImageDrawable(getContext().getDrawable(R.drawable.no_wifi));
                                text.setText("Sorry, something went wrong...");
                            }
                        }
                    });}
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        scan.start();
    }
    //Check is wifi on method
    public boolean wificonnected(){
        boolean ok = false;
        ConnectivityManager connManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            ok = true;
        }
        return ok;
    }
}