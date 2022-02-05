package com.zalexdev.stryker.nmap;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.nmap.utils.ScanTarget;
import com.zalexdev.stryker.utils.Core;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class NmapScanner extends Fragment {

    public ImageButton search;
    public ImageButton save;
    public Boolean now;
    public Core core;
    public Context context;
    public Activity activity;
    public NmapScanner() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initalizing
        View view = inflater.inflate(R.layout.nmap_fragment, container, false);
        context = getContext();
        activity = getActivity();
        search = view.findViewById(R.id.search);
        save = view.findViewById(R.id.save);
        TextView output = view.findViewById(R.id.nmap_output);
        CheckBox detect_os = view.findViewById(R.id.detect_os);
        CheckBox detect_services = view.findViewById(R.id.detect_services);
        CheckBox fast_scan = view.findViewById(R.id.fast_scan);
        CheckBox mark_online = view.findViewById(R.id.mark_online);
        core = new Core(context);
        TextInputEditText getquery = view.findViewById(R.id.getsearch);
        search.setOnClickListener(view1 -> {
            String q = String.valueOf(getquery.getText());
            output.setText("");
            ArrayList<Boolean> checked = new ArrayList<>();
            checked.add(detect_os.isChecked());
            checked.add(detect_services.isChecked());
            checked.add(fast_scan.isChecked());
            checked.add(mark_online.isChecked());
            new Thread(() -> {
                try {
                    now = new ScanTarget(q, checked, context, activity, output).execute().get();

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });

        return view;
    }


}