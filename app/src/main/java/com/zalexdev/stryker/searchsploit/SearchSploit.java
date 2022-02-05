package com.zalexdev.stryker.searchsploit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Sploit;
import com.zalexdev.stryker.searchsploit.utils.GetSploit;
import com.zalexdev.stryker.utils.Core;

import java.nio.channels.AcceptPendingException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class SearchSploit extends Fragment {
    public ImageButton search;
    public Core core;
    private RecyclerView mRecyclerView;
    private SploitAdapter mAdapter;
    public Context context;
    public Activity activity;
    public SearchSploit() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initalizing
        View view = inflater.inflate(R.layout.searchsploit_fragment, container, false);
        context = getContext();
        activity = getActivity();
        mRecyclerView = view.findViewById(R.id.search_list);
        search = view.findViewById(R.id.search);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        core = new Core(context);
        mRecyclerView.setItemViewCacheSize(255);
        TextInputEditText getquery = view.findViewById(R.id.getsearch);
        search.setOnClickListener(view1 -> {
            String q = String.valueOf(getquery.getText());
            new Thread(() -> {
                try {
                    ArrayList<Sploit> w = new GetSploit(q, core).execute().get();
                    activity.runOnUiThread(() -> {
                        if (w.isEmpty()) {
                            core.toaster(core.str("no_results"));
                        } else {
                            mAdapter = new SploitAdapter(context, activity, w);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    });
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });

        return view;
    }


}