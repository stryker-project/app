package com.zalexdev.stryker.router_scan;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Router;
import com.zalexdev.stryker.router_scan.utils.RsV2;
import com.zalexdev.stryker.utils.Core;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class RouterAdapter extends RecyclerView.Adapter<RouterAdapter.ViewHolder> {
    public Context context;
    public Activity activity;
    public Core core;
    public ArrayList<Router> routers;
    public ArrayList<Router> all = new ArrayList<>();
    public ArrayList<Router> good = new ArrayList<>();
    public boolean hide = false;
    private final ThreadInterface listener;
    public int success = 0;
    public  int scanned = 0;


    public RouterAdapter(Context context2, Activity mActivity, ArrayList<Router> tes,ThreadInterface i) {
        context = context2;
        activity = mActivity;
        core = new Core(context2);
        routers = tes;
        hide = core.getBoolean("hide");
        listener = i;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.routerscan_item, parent, false);
        return new ViewHolder(v);

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        Router r = routers.get(position);
        adapter.ip.setText(r.getIp());
        adapter.progress.setColorFilter(ContextCompat.getColor(context, R.color.yellow));
        if (r.isScanned()){
            if (r.getType() == 1){
                adapter.progress.setColorFilter(ContextCompat.getColor(context, R.color.green));
            }else if (r.getType() == 2){
                adapter.progress.setColorFilter(ContextCompat.getColor(context, R.color.red));
            }else{
                adapter.progress.setColorFilter(ContextCompat.getColor(context, R.color.yellow));
            }
        }
        if (!r.isScanned()){
            r.setScanned(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Router res = new RsV2(activity,context,adapter.status,r.getIp()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                    minus();
                    scanned++;
                    if (res.getSuccess()){
                        good.add(res);
                        r.setType(1);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                success++;
                                adapter.progress.setColorFilter(ContextCompat.getColor(context, R.color.green));
                                adapter.ssid.setText(res.getSsid());
                                adapter.psk.setText(res.getPsk());
                                adapter.auth.setText(res.getAuth());
                                adapter.ssid.setVisibility(View.VISIBLE);
                                adapter.psk.setVisibility(View.VISIBLE);
                                adapter.auth.setVisibility(View.VISIBLE);
                            }
                        });
                    }else{
                        r.setType(2);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.progress.setColorFilter(ContextCompat.getColor(context, R.color.red));
                            }
                        });
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();}
    }

    @Override
    public int getItemCount() {

        return routers.size();
    }

    public void toaster(String msg) {
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(context,
                    msg, Toast.LENGTH_SHORT);
            toast.show();
        });

    }

    public void appendtext(String text, TextView output) {
        activity.runOnUiThread(() -> output.append(text));
    }

    public void additem(Router item) {
        routers.add(item);
        notifyItemChanged(getlist().size()-1);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public ArrayList<Router> getlist() {
        return all;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView ip;
        public TextView status;
        public TextView model;
        public TextView auth;
        public TextView ssid;
        public TextView macadr;
        public TextView psk;
        public ImageView progress;


        public ViewHolder(View v) {
            super(v);
            ip = v.findViewById(R.id.router_ip);
            model = v.findViewById(R.id.router_model);
            auth = v.findViewById(R.id.router_auth);
            ssid = v.findViewById(R.id.router_ssid);
            macadr = v.findViewById(R.id.router_mac);
            psk = v.findViewById(R.id.router_password);
            status = v.findViewById(R.id.router_progress);
            progress = v.findViewById(R.id.routerscan_prog);
        }

    }
    public void minus(){
        listener.minusthread();
    }

    public int getSuccess() {
        return success;
    }

    public int getScanned() {
        return scanned;
    }

    public ArrayList<Router> getGood() {
        return good;
    }
}
