package com.zalexdev.stryker.three_wifi;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.WiFINetwork;
import com.zalexdev.stryker.utils.Core;

import java.util.ArrayList;
import java.util.Timer;

public class Search_Adapter extends RecyclerView.Adapter<Search_Adapter.ViewHolder> {
    public ArrayList<WiFINetwork> wifilist;
    public Context context;
    public Activity activity;
    public int tag = 0;
    public Timer deauth;
    public Core core;

    public Search_Adapter(Context context2, Activity mActivity, ArrayList<WiFINetwork> wifi) {
        context = context2;
        wifilist = wifi;
        activity = mActivity;

        core = new Core(context2);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.three_wifi_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        adapter.wifi_name.setText(wifilist.get(position).getSsid());
        adapter.wifi_mac.setText(wifilist.get(position).getMac());
        adapter.wifi_wpspin.setText(core.str("pin") + wifilist.get(position).getPin());
        adapter.wifi_psk.setText(core.str("key") + wifilist.get(position).getPsk());
        if (wifilist.get(position).getLun() !=null) {
            adapter.wifi_lon.setText(wifilist.get(position).getLon() + "/" + wifilist.get(position).getLun() + "\n" + wifilist.get(position).getDate());
        }
        else{ adapter.wifi_lon.setVisibility(View.GONE); }
        if (wifilist.get(position).getPin() == null) { adapter.wifi_wpspin.setVisibility(View.INVISIBLE); }
        if (wifilist.get(position).getPsk() == null) { adapter.wifi_psk.setVisibility(View.INVISIBLE); }
    }

    @Override
    public int getItemCount() {

        return wifilist.size();
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView wifi_name;
        public TextView wifi_mac;
        public TextView wifi_wpspin;
        public TextView wifi_lon;
        public TextView wifi_psk;

        public ViewHolder(View v) {
            super(v);
            wifi_name = v.findViewById(R.id.wifi_name);
            wifi_mac = v.findViewById(R.id.wifi_bssid);
            wifi_wpspin = v.findViewById(R.id.wpspin);
            wifi_lon = v.findViewById(R.id.wifi_desc);
            wifi_psk = v.findViewById(R.id.psk);

        }

    }

}
