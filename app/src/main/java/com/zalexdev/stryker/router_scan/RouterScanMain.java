package com.zalexdev.stryker.router_scan;


import android.annotation.SuppressLint;
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
import com.zalexdev.stryker.PlsInstallModule;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Router;
import com.zalexdev.stryker.searchsploit.SearchSploit;
import com.zalexdev.stryker.utils.CheckFile;
import com.zalexdev.stryker.utils.CheckInet;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.OnSwipeListener;

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



    // The main code for the routerscanner. It is the code that is run when the fragment is created.
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.routerscan_fragment, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        viewroot.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() {core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });
        try {
            if (!new CheckFile("/data/local/stryker/release/usr/bin/rs").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get()){
              getParentFragmentManager().beginTransaction().replace(R.id.flContent, new PlsInstallModule(true,"Router Scan")).commit();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
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
        // The above code is a thread that scans the ip addresses and ports and updates the progress
        // bar.
        startbutton.setOnClickListener(view -> {
            if (ports_text.getText().toString().length()>0&&ranges_text.getText().toString().length()>0) {
                pingbar.setMax(ipadresses.size() * ports.size());
                rsbar.setMax(ipadresses.size() * ports.size());
                scanactive = !scanactive;
                totalping = 0;
                totalrs = 0;
                if (scanactive) {
                    adapter = new RouterAdapter(context, activity, new ArrayList<>(), this);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    mRecyclerView.setAdapter(adapter);
                    updatebar();
                    startbutton.setText(R.string.stop);
                    startbutton.setIcon(context.getDrawable(R.drawable.stop));
                    // The above code is a thread that scans the ipadresses and ports in the ipadresses and
                    // ports arrays.
                    new Thread(() -> {
                        for (String ip : ipadresses) {
                            for (String port : ports) {

                                boolean scanned = false;
                                while (scanactive && !scanned) {
                                    if (now < maximum) {
                                        scanned = scan(ip, port);
                                    } else {
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
                } else {
                    startbutton.setText(R.string.start);
                    startbutton.setIcon(context.getDrawable(R.drawable.run));
                }
            }else{
                core.toaster(core.str("fill_rs"));
            }
        });
        return viewroot;
    }
/**
 * This function is used to scan the ip+port combo
 *
 * @param ip The IP address of the router you want to ping.
 * @param port The port to ping.
 * @return Nothing.
 */
public boolean scan(String ip, String port){
    new Thread(() -> {
        if (core.ping(ip, Integer.parseInt(port),timeout)){
            Log.e("Ping","OK "+ip+":"+port);
            activity.runOnUiThread(() -> {
                Router temp = new Router();
                temp.setIp(ip+":"+port);
                adapter.additem(temp);
                // Scrolling to the bottom of the list.
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
    /**
     * Given a lower and upper bound, return a list of all IP addresses in that range
     *
     * @param lowerStr The lower bound of the range.
     * @param upperStr The upper bound of the range.
     */
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

    /**
     * The function takes a string of IP addresses and/or subnets and splits them into a list of
     * strings.
     * It then iterates through the list and checks if the IP address is valid. If it is, it adds it to
     * the ipadresses list.
     * If it is a range, it splits the range into two strings and checks if both are valid. If they
     * are, it adds the range to the ranges_text and ipadresses lists.
     * If it is a subnet, it adds the subnet to the ipadresses list and the subnet to the ranges_text
     * list
     */
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
    /**
     * This function is used to set the ports that can be restored
     */
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
    /**
     * This function is used to set the maximum number of threads and the maximum time to wait for a
     * response from the router
     */
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

    /**
     * Given a subnet, return all the IP addresses in that subnet
     *
     * @param ipOrCidr The IP address or CIDR range to be parsed.
     */
    public void getipbysubnet(String ipOrCidr) {
        SubnetUtils utils = new SubnetUtils(ipOrCidr.replaceAll("\\s+",""));
        String[] allIps = utils.getInfo().getAllAddresses();
        Collections.addAll(ipadresses, allIps);
    }

    /**
     * It reads the values from the config file and restores them in the variables
     */
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

    /**
     * It sets the progress indicator to be invisible, sets the progress indicator to be indeterminate,
     * and sets the progress indicator to be visible.
     *
     * @param progressIndicator The progress indicator to be updated.
     * @param prog The progress indicator to be updated.
     */
    public void setProg(LinearProgressIndicator progressIndicator, int prog) {
        activity.runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);
            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {progressIndicator.setProgress(prog, true);}});
    }



    /**
     * Given an IP address, return true if it is a valid IP address, false otherwise
     *
     * @param ip The IP address to validate.
     * @return The method returns a boolean value.
     */
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

    /**
     * It updates the text of the labels and the progress bars.
     */
    public void updatebar(){
        new Thread(() -> {
            setText(threads_text,now+"/"+maximum+" ("+totalping/ports.size()+"/"+ipadresses.size()+")");
            setText(rs_pinged, String.valueOf(adapter.getScanned()));
            setText(rs_ok, String.valueOf(adapter.getSuccess()));
                setProg(pingbar,totalping);
                setProg(rsbar,totalrs);
        }).start();
    }

    /**
     * This function is called when the scan is finished. It shows a dialog box with the scan results
     */
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
    /**
     * This function checks if the device is connected to the internet. If it is, it returns true. If
     * it isn't, it attempts to remount the system partition and check again. If it still isn't
     * connected, it shows a dialog that says "No internet connection. Please connect to the internet
     * and try again."
     */
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