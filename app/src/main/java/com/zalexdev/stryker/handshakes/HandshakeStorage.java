package com.zalexdev.stryker.handshakes;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.utils.Core;

import java.io.File;

public class HandshakeStorage extends Fragment {
    public Core core;
    public Context context;
    public Activity activity;
    public HandshakeStorage() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initalizing
        View view = inflater.inflate(R.layout.handshakes_fragment, container, false);
        context = getContext();
        activity = getActivity();
        RecyclerView mRecyclerView = view.findViewById(R.id.hs_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        core = new Core(context);
        mRecyclerView.setItemViewCacheSize(255);
        LottieAnimationView img = view.findViewById(R.id.nothing_img);
        TextView txt = view.findViewById(R.id.nothing_text);

        if (!core.getListFiles(new File("/storage/emulated/0/Stryker/captured")).isEmpty()) {
            HandshakesAdapter mAdapter = new HandshakesAdapter(context, activity, core.getListFiles(new File("/storage/emulated/0/Stryker/captured")));
            mRecyclerView.setAdapter(mAdapter);
        }else{
            mRecyclerView.setVisibility(View.GONE);
            img.setVisibility(View.VISIBLE);
            txt.setVisibility(View.VISIBLE);
        }

        return view;
    }


}