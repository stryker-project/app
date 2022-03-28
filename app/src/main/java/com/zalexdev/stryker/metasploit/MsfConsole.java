package com.zalexdev.stryker.metasploit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.searchsploit.SploitAdapter;
import com.zalexdev.stryker.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Objects;

public class MsfConsole extends Fragment {
    public ImageButton run;
    public Core core;
    public Context context;
    public Activity activity;
    public Process msfconsole;
    public OutputStream input;
    public InputStream errors;
    public InputStream output;

    public MsfConsole() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initalizing
        View view = inflater.inflate(R.layout.msfconsole_fragment, container, false);
        context = getContext();
        activity = getActivity();
        run = view.findViewById(R.id.search);
        MaterialTextView console = view.findViewById(R.id.msfoutput);
        console.setMovementMethod(new LinkMovementMethod());
        core = new Core(context);
        TextInputEditText getquery = view.findViewById(R.id.getsearch);
        try {
            msfconsole = Runtime.getRuntime().exec("su -mm");
            input = msfconsole.getOutputStream();
            errors = msfconsole.getErrorStream();
            output = msfconsole.getInputStream();
            initalize();
            new Thread(() -> {
                String line = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(output));
                while (true) {
                    try {
                        line = br.readLine();
                        if (line != null){
                            appendText(console,line+"\n");

                        }else {
                            appendText(console,"waiting...");
                            Thread.sleep(100);
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        run.setOnClickListener(view1 -> {
            String q = String.valueOf(getquery.getText());
            appendText(console,"[Q]"+q+"\n");
            try {
                input.write((q + '\n').getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return view;
    }
    public void appendText(TextView textView, String text) {
        activity.runOnUiThread(() -> textView.append(text));
    }
    public void sendcommand(String cmd){
        new Thread(() -> {
            try {
                input.write((cmd + '\n').getBytes());
                input.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public void initalize(){
        new Thread(() -> {
            try {
                input.write((Core.EXECUTE + " '" + "./metasploit-framework/msfconsole" + "'" + '\n').getBytes());
                input.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
}