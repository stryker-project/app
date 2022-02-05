package com.zalexdev.stryker.router_scan;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Router;
import com.zalexdev.stryker.utils.CheckInet;
import com.zalexdev.stryker.utils.Core;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.apache.commons.net.util.SubnetUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;

public class RouterScanMain extends Fragment implements ThreadInterface{


    public int timeout = 300;
    public String chroot;
    public Core core;
    public Context context;
    public RecyclerView mRecyclerView;
    public RouterAdapter adapter;
    public Activity activity;
    public TextView ranges_text;
    public TextView ports_text;
    public TextView rs_pinged;
    public TextView rs_ok;
    public TextView threads_text;
    public LinearProgressIndicator prog;
    public ArrayList<String> ipadresses = new ArrayList<>();
    public ArrayList<String> ports = new ArrayList<>();
    public int maximum = 3;
    public int now = 0;
    public int totalping = 0;
    public int totalrs = 0;
    public boolean scanactive = false;
    public LinearProgressIndicator rsbar;
    public LinearProgressIndicator pingbar;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.routerscan_fragment, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        LinearLayout expand_toggle = viewroot.findViewById(R.id.router_toggle);
        MaterialCardView ranges_card = viewroot.findViewById(R.id.router_ranges_card);
        MaterialCardView ports_card = viewroot.findViewById(R.id.router_port_card);
        ranges_text = viewroot.findViewById(R.id.router_ranges_text);
        ports_text = viewroot.findViewById(R.id.router_ports);
        ImageView expand_toggle_img = viewroot.findViewById(R.id.router_toggle_img);
        ImageView save = viewroot.findViewById(R.id.router_save);
        ExpandableLayout routerpanel = viewroot.findViewById(R.id.routerpanel_expand);
        MaterialButton startbutton = viewroot.findViewById(R.id.start_scanner);
        ImageView setting_icon = viewroot.findViewById(R.id.router_settings_icon);
        threads_text = viewroot.findViewById(R.id.rs_threads);
        rs_ok = viewroot.findViewById(R.id.rs_ok);
        rs_pinged = viewroot.findViewById(R.id.rs_pinged);
        setting_icon.setOnClickListener(view -> settings());
        fixinet();
        save.setOnClickListener(view -> {
            core.saveresult(adapter.getGood());
            core.toaster(getString(R.string.saved_to));
        });
        rsbar = viewroot.findViewById(R.id.roterscan_progressbar);
        pingbar = viewroot.findViewById(R.id.ping_progressbar);
        mRecyclerView = viewroot.findViewById(R.id.routerscan_items);
        adapter = new RouterAdapter(context,activity,new ArrayList<>(),this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(adapter);
        restore();
        expand_toggle.setOnClickListener(view -> {
            if (routerpanel.isExpanded()) {
                expand_toggle_img.animate().rotation(0);
            } else {
                expand_toggle_img.animate().rotation(180);
            }
            routerpanel.toggle();
        });
        ranges_text.setMovementMethod(new ScrollingMovementMethod());
        ports_text.setMovementMethod(new ScrollingMovementMethod());
        ranges_card.setOnClickListener(view -> setranges());
        ranges_text.setOnClickListener(view -> setranges());
        ports_text.setOnClickListener(view -> setPorts());
        ports_card.setOnClickListener(view -> setPorts());
        startbutton.setOnClickListener(view -> {
            pingbar.setMax(ipadresses.size()*ports.size());
            rsbar.setMax(ipadresses.size()*ports.size());
            scanactive = !scanactive;
            totalping = 0;
            totalrs = 0;
            if (scanactive){
                adapter = new RouterAdapter(context,activity,new ArrayList<>(),this);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                mRecyclerView.setAdapter(adapter);
                updatebar();
                startbutton.setText(R.string.stop);
                startbutton.setIcon(context.getDrawable(R.drawable.stop));
                new Thread(() -> {
                    for (String ip: ipadresses){
                        for (String port: ports){
                            boolean scanned = false;
                            while (scanactive && !scanned){
                                if (now<maximum){
                                    scanned = scan(ip,port);}
                                else {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    updatebar();
                    scanactive = false;
                    activity.runOnUiThread(() -> {
                        startbutton.setText(R.string.start);
                        startbutton.setIcon(context.getDrawable(R.drawable.run));
                    });

                        activity.runOnUiThread(this::rsfinish);
                }).start();
            }else{
                startbutton.setText(R.string.start);
                startbutton.setIcon(context.getDrawable(R.drawable.run));
            }


        });
        return viewroot;
    }
public boolean scan(String ip, String port){
    new Thread(() -> {
        if (core.ping(ip, Integer.parseInt(port),timeout)){
            Log.e("Ping","OK "+ip+":"+port);
            activity.runOnUiThread(() -> {
                Router temp = new Router();
                temp.setIp(ip+":"+port);
                adapter.additem(temp);
                mRecyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
            });}else{
            now--;
            totalrs++;
        }
        totalping++;

    }).start();
    now++;
    updatebar();
    return true;
}
    public void getipsbyrange(String lowerStr, String upperStr) {
        try {
            IPAddress lower = new IPAddressString(lowerStr).toAddress();
            IPAddress upper = new IPAddressString(upperStr).toAddress();
            IPAddressSeqRange range = lower.toSequentialRange(upper);
            for (IPAddress addr : range.getIterable()) {
                ipadresses.add(String.valueOf(addr));
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
        input.getEditText().setText(ranges_text.getText());
        cancel.setOnClickListener(view -> dialog.dismiss());
        ok.setOnClickListener(view -> {
            String ipstring = String.valueOf(Objects.requireNonNull(input.getEditText()).getText());
            List<String> iplist = Arrays.asList(ipstring.split("\n"));
            ArrayList<String> r = new ArrayList<>();
            ipadresses = new ArrayList<>();
            ranges_text.setText("");
            for (int i = 0; i < iplist.size(); i++) {

                if (iplist.get(i).contains("-")) {
                    List<String> range = Arrays.asList(iplist.get(i).split("-"));
                    if (validate(range.get(0)) && validate(range.get(1))) {
                        getipsbyrange(range.get(0), range.get(1));
                        ranges_text.append(iplist.get(i) + "\n");
                        r.add(iplist.get(i) + "\n");
                    }
                } else if (iplist.get(i).contains("/")) {
                    getipbysubnet(iplist.get(i));
                    ranges_text.append(iplist.get(i) + "\n");
                    r.add(iplist.get(i) + "\n");
                } else if (validate(iplist.get(i))) {
                    ipadresses.add(iplist.get(i));
                    ranges_text.append(iplist.get(i) + "\n");
                    r.add(iplist.get(i) + "\n");
                }
            }
            core.putListString("restore_ips", ipadresses);
            core.putListString("restore_ranges", r);
            dialog.dismiss();
        });
        dialog.show();
    }
    private void setPorts() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.router_setrange);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView cancel = dialog.findViewById(R.id.rs_cancel);
        TextView ok = dialog.findViewById(R.id.rs_ok);
        TextView title = dialog.findViewById(R.id.title);
        title.setText("Set ports");
        TextInputLayout input = dialog.findViewById(R.id.textField);
        input.getEditText().setText(ports_text.getText());
        cancel.setOnClickListener(view -> dialog.dismiss());
        ok.setOnClickListener(view -> {
            ports_text.setText("");
            List<String> tempports =  Arrays.asList(String.valueOf(Objects.requireNonNull(input.getEditText()).getText()).split("\n"));
            ports = new ArrayList<>();
            ports.addAll(tempports);
            core.putListString("restore_ports",ports);
            for (String p : ports){
                ports_text.append(p+"\n");
            }
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
            maximum = Integer.parseInt(String.valueOf(maxtreads.getEditText().getText()));
            timeout = Integer.parseInt(String.valueOf(time.getEditText().getText()));
            core.putInt("restore_maximum", maximum);
            core.putInt("restore_timeout", timeout);
            dialog.dismiss();
        });
        dialog.show();

    }

    public void getipbysubnet(String ipOrCidr) {
        SubnetUtils utils = new SubnetUtils(ipOrCidr.replaceAll("\\s+",""));
        String[] allIps = utils.getInfo().getAllAddresses();
        Collections.addAll(ipadresses, allIps);
    }

    public void restore() {ipadresses = core.getListString("restore_ips");
        ArrayList<String> ip = core.getListString("restore_ranges");
        ports = core.getListString("restore_ports");
        timeout = core.getInt("restore_timeout");
        maximum = core.getInt("restore_maximum");
        if (timeout == 0){
            timeout = 300;
        }
        if (maximum == 0){
            maximum = 50;
        }
        for (String i2 : ip) {
            ranges_text.append(i2);
        }for (String port : ports) {
            ports_text.append(port+"\n");
        }

    }



    public void settext(String text, TextView output) {
        activity.runOnUiThread(() -> output.setText(text));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    public void setProg(LinearProgressIndicator progressIndicator, int prog) {
        activity.runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);
            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {progressIndicator.setProgress(prog, true);}});
    }



    public boolean validate(final String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }

    @Override
    public void minusthread() {
        now--;
        totalrs++;
    }
    public void setText(TextView textView, String text) {
        activity.runOnUiThread(() -> textView.setText(text));
    }

    public void updatebar(){
        new Thread(() -> {
            setText(threads_text,now+"/"+maximum+" ("+totalping/ports.size()+"/"+ipadresses.size()+")");
            setText(rs_pinged, String.valueOf(adapter.getScanned()));
            setText(rs_ok, String.valueOf(adapter.getSuccess()));
                setProg(pingbar,totalping);
                setProg(rsbar,totalrs);
        }).start();
    }

    public void rsfinish() {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.scan_finised)
                .setMessage(R.string.scan_finished_desc)
                .setPositiveButton("OK", (dialogInterface, i) -> {})
                .setNeutralButton(R.string.save, (dialogInterface, i) -> {
                    core.saveresult(adapter.getGood());
                    core.toaster(getString(R.string.saved_to));
                })
                .show();
    }
    public void fixinet(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.exploit_progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        LinearProgressIndicator prog = dialog.findViewById(R.id.exploit_prog);
        LottieAnimationView image = dialog.findViewById(R.id.exploit_img);
        TextView title = dialog.findViewById(R.id.exploit_title);
        TextView progress = dialog.findViewById(R.id.exploit_progress_text);
        TextView cancel = dialog.findViewById(R.id.exploit_cancel);
        title.setText(R.string.checking_inet);
        progress.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        dialog.setCancelable(false);
        image.setAnimation(R.raw.check_net);
        prog.setVisibility(View.GONE);
        cancel.setOnClickListener(view -> dialog.dismiss());
        new Thread(() -> {
            try {
                Boolean inet  = new CheckInet().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                if (inet){
                    activity.runOnUiThread(dialog::dismiss);
                }else{
                    activity.runOnUiThread(dialog::show);

                    boolean o = core.remountcore();
                    inet  = new CheckInet().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                    if (inet){
                        activity.runOnUiThread(dialog::dismiss);
                    }else {
                        activity.runOnUiThread(() -> {
                            cancel.setVisibility(View.VISIBLE);
                            cancel.setText("OK");
                            progress.setVisibility(View.VISIBLE);
                            progress.setText(R.string.no_inet_chroot);
                        });}


                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }
}