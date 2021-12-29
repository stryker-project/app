package com.zalexdev.stryker.local;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Device;
import com.zalexdev.stryker.custom.Router;
import com.zalexdev.stryker.local.exploits.CheckBlueKeep;
import com.zalexdev.stryker.local.exploits.CheckEternalBlue;
import com.zalexdev.stryker.local.exploits.CheckSmbGhost;
import com.zalexdev.stryker.local.exploits.RouterScan;
import com.zalexdev.stryker.utils.Core;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class LocalAdapter extends RecyclerView.Adapter<LocalAdapter.ViewHolder> {
    public Context context;
    public Activity activity;
    public String port;
    public ArrayList<Device> devices;
    public boolean iscutted = false;
    public LocalAdapter(Context context2, Activity mActivity, ArrayList<Device> devs) {
        context = context2;
        activity = mActivity;
        devices = devs;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //Init
        public TextView local_ip;
        public TextView local_mac;
        public TextView local_manufacture;
        public ImageView local_img;
        public ImageView local_netcut;
        public TextView local_ports;
        public LinearLayout ports;
        public View card;
        public ViewHolder(View v) {
            super(v);
            local_ip = v.findViewById(R.id.local_ip);
            local_mac = v.findViewById(R.id.local_mac);
            local_img = v.findViewById(R.id.local_icon);
            local_netcut = v.findViewById(R.id.local_cutted);
            local_ports = v.findViewById(R.id.local_ports);
            ports = v.findViewById(R.id.port_layout);
            local_manufacture = v.findViewById(R.id.local_manufacture);
            card = v.findViewById(R.id.local_item);

        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.local_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        //Setting up geted info
        String ip = devices.get(position).getIp();
        String mac = devices.get(position).getMac();
        adapter.local_ip.setText(ip);
        if (!new Core(context).getBoolean("hide")) {
            adapter.local_mac.setText(mac);
        }else{
            adapter.local_mac.setText("XX:XX:XX:XX:XX");
        }
        adapter.local_manufacture.setText(devices.get(position).getVendor());
        adapter.local_ports.setText("Ports: "+devices.get(position).getPorts().size());
        adapter.local_img.setImageDrawable(context.getDrawable(devices.get(position).getImage()));
        if (position == 0){
            adapter.local_img.setImageDrawable(context.getDrawable(R.drawable.router));
            //adapter.local_netcut.setVisibility(View.VISIBLE);
            //adapter.local_netcut.setClickable(true);
        }
        adapter.card.setOnClickListener(view -> {

         LocalDialog(context.getDrawable(devices.get(position).getImage()),ip,mac,devices.get(position).getPorts(),devices.get(position).getServices(),position == 0);
        });
        adapter.ports.setOnClickListener(view -> {
            showPorts(devices.get(position).getPorts(),devices.get(position).getServices());
        });

    }
    private void LocalDialog(Drawable img,String ip, String mac, ArrayList<String> port, ArrayList<String> services, boolean isrouter) {
        final BottomSheetDialog localdialog = new BottomSheetDialog(context,R.style.AppBottomSheetDialogTheme);
        localdialog.setContentView(R.layout.local_bottom);
        ImageView device_img = localdialog.findViewById(R.id.device_img);
        TextView ip_view = localdialog.findViewById(R.id.device_ip);
        TextView mac_view = localdialog.findViewById(R.id.device_mac);
        TextView smb = localdialog.findViewById(R.id.check_smb);
        TextView rdp = localdialog.findViewById(R.id.check_rdp);
        TextView admin = localdialog.findViewById(R.id.check_admin_panel);
        smb.setOnClickListener(view -> {getPort("Smb",port,ip); });
        rdp.setOnClickListener(view -> getPort("Rdp",port,ip));
        admin.setOnClickListener(view -> getPort("Admin",port,ip));
        TextView port_count = localdialog.findViewById(R.id.port_count);
        ip_view.setText(ip);

        if (!new Core(context).getBoolean("hide")) {
        mac_view.setText(mac);}
        else{
            mac_view.setText("XX:XX:XX:XX:XX");
        }
        port_count.setText(String.valueOf(port.size()));
        device_img.setImageDrawable(img);
        if (isrouter){
            device_img.setImageDrawable(context.getDrawable(R.drawable.router));
        }
        device_img.setOnClickListener(view -> {
            showPorts(port,services);
        });
        localdialog.show();
    }
    private void showPorts(ArrayList<String> ports,ArrayList<String> services){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.list_of_ports);
        dialog.setTitle("List of ports");
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView text = (TextView) dialog.findViewById(R.id.list_ports);
        for (int i =0;i<ports.size();i++) {
        text.append(ports.get(i)+" ("+services.get(i)+")\n");
        }
        if (ports.size() == 0){
            text.setText("No avaible ports founded");
        }
        dialog.show();
    }
    private void testexploit(String type,String port,String ip) throws ExecutionException, InterruptedException {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.exploit_progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        CircularProgressIndicator prog = dialog.findViewById(R.id.exploit_prog);
        ImageView status_img = dialog.findViewById(R.id.exploit_img_status);
        TextView title = dialog.findViewById(R.id.exploit_title);
        TextView progress = dialog.findViewById(R.id.exploit_progress);
        TextView cancel = dialog.findViewById(R.id.exploit_cancel);
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
        dialog.setCanceledOnTouchOutside(false);
        if (type.equals("Smb")){
                title.setText("SMB check");
                progress.setText("EternalBlue... Checking");
                dialog.show();
                Thread t = new Thread(() -> {
                    boolean eternal = false;
                    try {
                        eternal = new CheckEternalBlue(ip,new Core(context)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                        setProg(prog,50);
                        boolean ghost = false;
                        setText(progress,"SMBGhost... Checking");
                        ghost = new CheckSmbGhost(ip,new Core(context)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                        setProg(prog,100);
                        setText(cancel,"OK");
                        if (!eternal && !ghost){
                            setText(progress,"Exploits failed");
                            setProgColor(prog,status_img,1);}
                        if (!eternal && ghost){
                            setText(progress,"Host seems vuln to smbghost");
                            setProgColor(prog,status_img,3);}
                        if (eternal && !ghost){
                            setText(progress,"Host seems vuln to eternalblue");
                            setProgColor(prog,status_img,3);}
                        if (eternal && ghost){
                            setText(progress,"Host seems vuln to both exploits");
                            setProgColor(prog,status_img,2);
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                t.start();}
        else if (type.equals("Rdp")){
                title.setText("RDP check");
                progress.setText("BlueKeep... checking");
                dialog.show();
                Thread t2 = new Thread(() -> {
                    boolean keep = false;
                    try {
                        keep = new CheckBlueKeep(ip,new Core(context)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    setProg(prog,100);
                    setText(cancel,"OK");
                    if (!keep){
                        setText(progress,"Exploits failed");
                        setProgColor(prog,status_img,1);
                    } else{
                        setText(progress, "Host seems vuln to BlueKeep");
                        setProgColor(prog,status_img,2);
                    }});
                t2.start();

        }else if (type.equals("Admin")){
            title.setText("Admin panel check");
            progress.setText("Starting core...");
            setProg(prog,20);
            dialog.show();
            Thread t3 = new Thread(() -> {

                try {
                    Router router;
                    router = new RouterScan(activity,context,progress,prog,ip,port).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                    setProg(prog,100);
                    setText(cancel,"OK");
                    if (!router.getSuccess()){
                        setText(progress,"Failed to get information");
                        setProgColor(prog,status_img,1);
                    } else {
                        setText(progress,
                                "WebAuth - "+ router.getAuth() +"\nSSID - "+router.getSsid() +"\nPSK - "+router.getPsk()+ "\nWPS - "+router.getWps());
                        setProgColor(prog,status_img,2);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                });
            t3.start();
        }



    }





    public void getPort(String type,ArrayList<String> ports,String ip) {
        port = "";
        if (!ports.isEmpty()){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose port");
        String[] port_list = new String[ports.size()];
        for (int i = 0;i<ports.size();i++){ port_list[i] = ports.get(i); }
        int checkedItem = 0;
        builder.setSingleChoiceItems(port_list, checkedItem, (dialog, which) -> {
            port = port_list[which];
            dialog.dismiss(); });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setOnDismissListener(dialogInterface -> {
            try {
                if (!port.equals("")){
                testexploit(type,port,ip);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });}
    }
    @Override
    public int getItemCount() {

        return devices.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public void setText(TextView textView, String text){
        activity.runOnUiThread(() -> textView.setText(text));
    }
    public void setProg(CircularProgressIndicator progressIndicator, int prog){
        activity.runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);
            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(prog,true);
            }
        });

    }
    public void setProgColor(CircularProgressIndicator progressIndicator,ImageView img, int color){
        activity.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);
            progressIndicator.setVisibility(View.VISIBLE);
            img.setVisibility(View.VISIBLE);
            if (color == 1){
                progressIndicator.setIndicatorColor(context.getColor(R.color.red));
                img.setImageDrawable(context.getDrawable(R.drawable.no));
            } else if (color == 2) {
                progressIndicator.setIndicatorColor(context.getColor(R.color.green));
                img.setImageDrawable(context.getDrawable(R.drawable.ok));
            } else if (color == 3) {
                progressIndicator.setIndicatorColor(context.getColor(R.color.yellow));
                img.setImageDrawable(context.getDrawable(R.drawable.warn));
            }
            }
        });

    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    public void toaster(String msg){
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(context,
                    msg, Toast.LENGTH_SHORT);
            toast.show();
        });

    }

}
