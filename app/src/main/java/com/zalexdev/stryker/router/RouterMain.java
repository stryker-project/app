package com.zalexdev.stryker.router;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Router;
import com.zalexdev.stryker.local.exploits.RouterScan;
import com.zalexdev.stryker.router.utils.ScannerRs;
import com.zalexdev.stryker.utils.Core;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;

public class RouterMain extends Fragment {


    public int timeout = 200;
    public String chroot;
    public Core core;
    public Context context;
    public RecyclerView mRecyclerView;
    public RouterAdapter adapter;
    public int maximum = 500;
    public int thread = 10;
    public int now = 2;
    public int athread = 0;
    public boolean scroll;
    public TextView ranges;
    public TextView tcount;
    public TextView progtext;
    public  TextView ipnow;
    public boolean run=false;
    
    public LinearProgressIndicator prog;
    public ArrayList<String> ips = new ArrayList<>();
    ArrayList<Thread> threads = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.routerscan, container, false);
        context = getContext();
        core = new Core(context);

        maximum = core.getInt("threads");
        timeout= core.getInt("timeout");
        Button start = viewroot.findViewById(R.id.router_start);
        tcount = viewroot.findViewById(R.id.tcount);
        progtext = viewroot.findViewById(R.id.status_range);
        ipnow = viewroot.findViewById(R.id.ip_now);
        ImageView settings = viewroot.findViewById(R.id.router_settings_icon);
        ImageView save = viewroot.findViewById(R.id.router_save);
        MaterialCardView ranges_card = viewroot.findViewById(R.id.router_ranges_card);
        ranges = viewroot.findViewById(R.id.router_ranges);
        ranges.setMovementMethod(new ScrollingMovementMethod());
        ExpandableLayout expand = viewroot.findViewById(R.id.router_expand);
        LinearLayout expand_toggle = viewroot.findViewById(R.id.router_toggle);
        ImageView expand_toggle_img =  viewroot.findViewById(R.id.router_toggle_img);
        prog = viewroot.findViewById(R.id.rs_progress);
        mRecyclerView = viewroot.findViewById(R.id.router_adapterok);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        MaterialCheckBox autosc = viewroot.findViewById(R.id.router_autoscroll);
        autosc.setChecked(core.getBoolean("autoscroll"));
        autosc.setOnCheckedChangeListener((compoundButton, b) -> {
            core.putBoolean("autoscroll",b);
            scroll = b;
        });
        adapter = new RouterAdapter(getContext(),getActivity(),new ArrayList<>(),mRecyclerView.getLayoutManager());adapter.setHasStableIds(true);
        save.setOnClickListener(view -> {
            ArrayList<Router> r = adapter.getlist();
            core.saveresult(r);
            core.toaster("Saved!");
        });
        settings.setOnClickListener(view -> settings());
        if (core.getListString("restore_ips").size() >0 || core.getListString("restore_ranges").size() >0){
            restore();
        }
        start.setOnClickListener(view -> {
            if (!run){
                adapter = new RouterAdapter(getContext(),getActivity(),new ArrayList<>(),mRecyclerView.getLayoutManager());adapter.setHasStableIds(true);
                mRecyclerView.setAdapter(adapter);
                start.setText("Stop");
                now = 0;
                run = true;
                update();
                prog.setMax(ips.size());
            Thread t = new Thread(() -> {
                for (int i= 0; i<ips.size();i++){
                    if (run){
                    while (thread>= maximum){
                        try {
                            Thread.sleep(800);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    update();
                    scan(ips.get(i).replaceAll("\\s+",""));}
                    else{
                        break;
                    }


                }
                getActivity().runOnUiThread(this::finished);
            }


            );t.start();
            threads.add(t);}
            else{
                start.setText("Start");
                run = false;
                stop();

            }

        });
        mRecyclerView.setAdapter(adapter);
        expand_toggle.setOnClickListener(view -> {
         if (expand.isExpanded()){
                expand_toggle_img.animate().rotation(0);
         }else{
                expand_toggle_img.animate().rotation(180);
         }
            expand.toggle();
        });
        ranges_card.setOnClickListener(view -> setranges());
        ranges.setOnClickListener(view -> setranges());

        return viewroot;
    }
    public void getipsbyrange(String lowerStr, String upperStr) {
        try { IPAddress lower = new IPAddressString(lowerStr).toAddress();
        IPAddress upper = new IPAddressString(upperStr).toAddress();
        IPAddressSeqRange range = lower.toSequentialRange(upper);
        for(IPAddress addr : range.getIterable()) {
            ips.add(addr+"\n");
        }
        } catch (AddressStringException e) {
            e.printStackTrace();
        }
    }
    private void setranges() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.router_setrange);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView cancel = dialog.findViewById(R.id.rs_cancel);
        TextView ok = dialog.findViewById(R.id.rs_ok);
        TextInputLayout input = dialog.findViewById(R.id.textField);
        ArrayList<String> previous = core.getListString("restore_ranges");
        for (String ip: previous){
            input.getEditText().append(ip);
        }
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
        ok.setOnClickListener(view -> {
            String ipstring = String.valueOf(input.getEditText().getText());
            List<String> iplist = Arrays.asList(ipstring.split("\n"));
            ArrayList<String> r = new ArrayList<>();
            ips = new ArrayList<>();
            for (int i = 0;i<iplist.size();i++){

                if (iplist.get(i).contains("-")){
                    List<String> range = Arrays.asList(iplist.get(i).split("-"));
                    if (validate(range.get(0)) && validate(range.get(1))){
                    getipsbyrange(range.get(0),range.get(1));
                    ranges.append(iplist.get(i)+"\n");
                    r.add(iplist.get(i)+"\n");
                    }
                }else if (iplist.get(i).contains("/")){
                    getipbysubnet(iplist.get(i));
                    ranges.append(iplist.get(i)+"\n");
                    r.add(iplist.get(i)+"\n");
                }
                else if (validate(iplist.get(i))){
                    ips.add(iplist.get(i));
                    ranges.append(iplist.get(i)+"\n");
                    r.add(iplist.get(i)+"\n");
                }
            }
            core.putListString("restore_ips",ips);
            core.putListString("restore_ranges",r);
            dialog.dismiss();
        });

        dialog.show();



    }
    private void settings() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.router_settings);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView ok = dialog.findViewById(R.id.save_router_settings);
        TextInputLayout maxtreads = dialog.findViewById(R.id.setmaxthreads);
        TextInputLayout time = dialog.findViewById(R.id.setmaxtimeout);
        maxtreads.getEditText().setText(String.valueOf(maximum));
        time.getEditText().setText(String.valueOf(timeout));
        ok.setOnClickListener(view -> {
            int threads = Integer.parseInt(String.valueOf(maxtreads.getEditText().getText()));
            int times = Integer.parseInt(String.valueOf(time.getEditText().getText()));
            core.putInt("threads",threads);
            core.putInt("timeout",times);
            maximum = threads;
            timeout = times;
            dialog.dismiss();
        });
        dialog.show();

    }
    public static boolean validate(final String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        return ip.matches(PATTERN);
    }
    public void getipbysubnet(String ipOrCidr) {
        IPAddressString addrString = new IPAddressString(ipOrCidr, IPAddressString.DEFAULT_VALIDATION_OPTIONS);
        IPAddress subnet = null;
        try {
            subnet = addrString.toAddress();
        } catch (AddressStringException e) {
            e.printStackTrace();
        }
        for (IPAddress addr : subnet.getIterable()) {
            Matcher m = Pattern.compile("/[0-9]+").matcher(String.valueOf(addr));
            String newaddr = "";
            if (m.find()) {
                newaddr = m.group();
            }
            ips.add(String.valueOf(addr).replace(newaddr,""));
        }
    }
    public void restore(){
        new MaterialAlertDialogBuilder(context)
                .setTitle("Restore session")
                .setMessage("Hi, do you want to restore previous session?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ips = core.getListString("restore_ips");
                        ArrayList<String> ip = core.getListString("restore_ranges");
                        for (String i2 : ip){
                            ranges.append(i2);
                        }
                        core.toaster("Restored!");
                    }
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).show();


    }
    public void finished(){
        new MaterialAlertDialogBuilder(context)
                .setTitle("Port scan finished!")
                .setMessage("Hi, port scan was finished. But RouterScan threads are still running... Wait as long as you want!")
                .setPositiveButton("OK", (dialogInterface, i) -> {

                }).show();


    }
    public void rsfinish(){
        new MaterialAlertDialogBuilder(context)
                .setTitle("Router Scan finished")
                .setMessage("Hi, router scan threads was finished! You can store results now!")
                .setPositiveButton("OK", (dialogInterface, i) -> {

                }).show();


    }
    public void scan(String ip){
        Thread t = new Thread(() -> {

            thread++;

            if (ping(ip,80)){

                core.writelinetolog("[OK] "+ip);
                thread--;
                athread++;
                try {
                    Router r = new ScannerRs(getContext(),ip,"80").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                    now++;
                    if (now== ips.size()){
                        rsfinish();
                    }
                    getActivity().runOnUiThread(() -> {
                        adapter.additem(r);
                        mRecyclerView.getLayoutManager().scrollToPosition(adapter.getItemCount()-1);
                        athread--;
                        update();
                    });
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }else {update();
                core.writelinetolog("[TIMEOUT] "+ip);
                now++;
                thread--;
            }
        });
        t.start();
        threads.add(t);
    }
    public void settext(String text, TextView output){
        getActivity().runOnUiThread(() -> output.setText(text));
    }
    public void update(){
        try{
        getActivity().runOnUiThread(() -> {
            if (now<ips.size()){
            settext("Threads "+(thread-5)+"/"+maximum,tcount);
            settext(now+"/"+ips.size(),progtext);
            settext(ips.get(now),ipnow);
            setProg(prog,now);
        }});} catch (Exception ignored){

        }

    }
    public void stop(){
        for (int i = 0;i<threads.size();i++){
            try{
                if(threads.get(i).isAlive()){
                    threads.get(i).stop();
                }
            }catch (Exception e){

            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stop();
    }

    public void setProg(LinearProgressIndicator progressIndicator, int prog){
        getActivity().runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);
            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(prog,true);
            }
        });

    }
    public boolean ping(String ip,int port){

        try {
            URI uri = URI.create("http://"+ip+":" + port + "/");
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(timeout);
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(connection::disconnect, timeout+4000);
            return connection.getResponseCode()>=200 && !(connection.getResponseCode() ==404) && !(connection.getResponseCode() ==403);
        }catch (Exception e){
            return false;
        }
    }
}