package com.zalexdev.stryker;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.zalexdev.stryker.custom.Router;
import com.zalexdev.stryker.local.exploits.CheckBlueKeep;
import com.zalexdev.stryker.local.exploits.CheckEternalBlue;
import com.zalexdev.stryker.local.exploits.CheckSmbGhost;
import com.zalexdev.stryker.local.exploits.RouterScan;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomChrootCommand;
import com.zalexdev.stryker.utils.CustomCommand;
import com.zalexdev.stryker.utils.RealPath;
import com.zalexdev.stryker.wifi.utils.GetInterfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Settings extends Fragment {



    public TextView scanwlan;
    public TextView deauthwlan;

    public String chroot;
    public Core core;
    public Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.settings2, container, false);
        context = getContext();
        core = new Core(context);
        MaterialCardView speed = viewroot.findViewById(R.id.settings_speed);
        MaterialCardView debug = viewroot.findViewById(R.id.settings_debug);
        MaterialCardView module = viewroot.findViewById(R.id.settings_module);
        MaterialCardView hide = viewroot.findViewById(R.id.settings_hide);
        MaterialCardView auto = viewroot.findViewById(R.id.auto_update);
        MaterialCardView turnoff = viewroot.findViewById(R.id.pixie_off);
        MaterialCardView scan = viewroot.findViewById(R.id.settings_scan);
        MaterialCardView deauth = viewroot.findViewById(R.id.settings_deauth);
        MaterialCardView unmount = viewroot.findViewById(R.id.settings_unmount);
        MaterialCardView delete = viewroot.findViewById(R.id.settings_delete);
        scanwlan = viewroot.findViewById(R.id.scan_text);
        deauthwlan = viewroot.findViewById(R.id.deauth_text);
        speed.setChecked(core.getBoolean("fast_scan"));
        hide.setChecked(core.getBoolean("hide"));
        debug.setChecked(core.getBoolean("debug"));
        turnoff.setChecked(core.getBoolean("pixie_off"));
        auto.setChecked(core.getBoolean("auto_update"));
        scanwlan.setText("Set custom scan interface\nCurrent: "+core.getString("wlan_scan"));
        deauthwlan.setText("Set custom deauth interface\nCurrent: "+core.getString("wlan_deauth"));
        speed.setOnClickListener(view -> {
            speed.toggle();
            core.putBoolean("fast_scan",speed.isChecked());
            core.vibrate(1);
        });
        auto.setOnClickListener(view -> {
            auto.toggle();
            core.putBoolean("auto_update",auto.isChecked());
            core.vibrate(1);
        });
        debug.setOnClickListener(view -> {
            debug.toggle();
            core.putBoolean("debug",debug.isChecked());
            core.vibrate(1);
        });
        turnoff.setOnClickListener(view -> {
            turnoff.toggle();
            core.putBoolean("pixie_off",turnoff.isChecked());
            core.vibrate(1);
        });
        hide.setOnClickListener(view -> {
            hide.toggle();
            core.putBoolean("hide",hide.isChecked());
            core.vibrate(1);
        });
        module.setOnClickListener(view -> {
            install();
        });

        scan.setOnClickListener(view -> {
            try {
                getWlanMonitore(true);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        deauth.setOnClickListener(view -> {
            try {
                getWlanMonitore(false);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        unmount.setOnClickListener(view -> {
            try {
                if (unmount()){
                    core.vibrate(1);
                    getActivity().finish();
                }else{
                    toaster("Failed!");
                    core.vibrate(100);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        });
        delete.setOnClickListener(view -> {
           confirm();
        });


        return viewroot;
    }
    public void confirm(){
        new MaterialAlertDialogBuilder(context)
                .setTitle("Confirm deletion")
                .setMessage("Sorry for bad experience, we are trying our best to solve all bugs! Are you sure you want to delete this app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            if (unmount()){
                                toaster("Sorry for bad experience :( ");
                                new CustomCommand("rm -rf /data/local/stryker&&pm uninstall com.zalexdev.stryker",new Core(context)).execute();
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).show();


    }
    private ArrayList<String> getinterfaces() throws ExecutionException, InterruptedException {
        GetInterfaces airmon = new GetInterfaces(new Core(context));
        return airmon.execute().get();
    }
    public void getWlanMonitore(boolean isscan) throws ExecutionException, InterruptedException {
        ArrayList<String> w = getinterfaces();
        String[] w2 = new String[w.size()];
        for (int i = 0;i<w.size();i++){
            w2[i] = w.get(i);
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle("Pick interface")
                .setItems(w2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (isscan) {core.putString("wlan_scan",w2[i]);}
                        else{core.putString("wlan_deauth",w2[i]);}
                        scanwlan.setText("Set custom scan interface\nCurrent: "+core.getString("wlan_scan"));
                        deauthwlan.setText("Set custom deauth interface\nCurrent: "+core.getString("wlan_deauth"));
                    }
                })
        .show();
    }
    public boolean unmount() throws ExecutionException, InterruptedException {
        return new CustomCommand("/data/data/com.zalexdev.stryker/cache/killroot",new Core(context)).execute().get();
    }
    private void install(){
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a module");
        startActivityForResult(chooseFile, 22);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 22 && resultCode == Activity.RESULT_OK){
            Uri notreal_path = data.getData();
            String path = RealPath.getRealPath(getContext(),notreal_path);
            toaster(path);
            moduledialog(path);

        }
    }
    private void moduledialog(String path) {

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.module_progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        CircularProgressIndicator prog = dialog.findViewById(R.id.module_prog);
        ImageView status_img = dialog.findViewById(R.id.module_img_status);
        TextView title = dialog.findViewById(R.id.module_title);
        TextView progress = dialog.findViewById(R.id.module_progress);
        TextView cancel = dialog.findViewById(R.id.module_cancel);
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
        try{
        ArrayList<String> commands = new ArrayList<>();
        FileInputStream inputstr;
        BufferedReader filereader;
        final File file = new File(path);
        if (file.exists()) {
            inputstr = new FileInputStream(file);
            filereader = new BufferedReader(new InputStreamReader(inputstr));
            String line = " ";
            while(line != null && !line.equals("null") && line.length()>0){
                line = filereader.readLine();
                if (line != null && !line.equals("null") && line.length()>0){
                commands.add(line);}
            }
        }
        dialog.setCanceledOnTouchOutside(false);
            title.setText("Installing module...");
            progress.setText("Preparing...");
            dialog.show();
            Thread t = new Thread(() -> {
                int bar = 100/commands.size();
                for (int i = 0;i<commands.size();i++){
                    String cmd = commands.get(i);
                    setText(progress,"Processing ...("+cmd+")");
                    try {
                        boolean exec = new CustomChrootCommand(cmd,new Core(getContext())).execute().get();
                        setProg(prog,prog.getProgress()+bar);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                setText(progress,"Installed!");
                title.setText("Success!");
                progress.setVisibility(View.INVISIBLE);
                setText(cancel,"Exit");
            });
            t.start();

    } catch (IOException  e) {
        e.printStackTrace();
    }


    }
    public void setText(TextView textView, String text){
        getActivity().runOnUiThread(() -> textView.setText(text));
    }
    public void setProg(CircularProgressIndicator progressIndicator, int prog){
        getActivity().runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);

            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(prog,true);
            }
        });

    }

    public void toaster(String msg){
        getActivity().runOnUiThread(() -> {
            Toast toast = Toast.makeText(context,
                    msg, Toast.LENGTH_SHORT);
            toast.show();
        });

    }
    public Spanned green (String out){
        Spanned formated = Html.fromHtml("<font color='#19D121'>"+out+"</font>");
        return formated;
    }
    public Spanned yellow (String out){
        Spanned formated = Html.fromHtml("<font color='#F9D625'>"+out+"</font>");
        return formated;
    }
    public Spanned red (String out){
        Spanned formated = Html.fromHtml("<font color='#F60B0B'>"+out+"</font>");
        return formated;
    }
}