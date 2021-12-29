package com.zalexdev.stryker.router;


import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import com.zalexdev.stryker.utils.Core;

import java.util.ArrayList;

public class RouterAdapter extends RecyclerView.Adapter<RouterAdapter.ViewHolder> {
    public Context context;
    public Activity activity;
    public Core core;
    public ArrayList<Router> routers;
    public ArrayList<Router> all = new ArrayList<>();
    public ArrayList<Router> good = new ArrayList<>();
    public boolean  hide = false;
    RecyclerView.LayoutManager m;
    public RouterAdapter(Context context2, Activity mActivity, ArrayList<Router> tes, RecyclerView.LayoutManager manager) {
        context = context2;
        activity = mActivity;
        core = new Core(context2);
        routers = tes;
        m = manager;
        hide = core.getBoolean("hide");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView ip;
        public TextView status;
        public TextView title;
        public TextView auth;
        public TextView ssid;
        public TextView macadr;
        public TextView psk;
        public ImageView img;

        public ViewHolder(View v) {
            super(v);
           ip = v.findViewById(R.id.router_item_ip);
           title = v.findViewById(R.id.router_item_title);
           auth = v.findViewById(R.id.router_item_auth);
           ssid = v.findViewById(R.id.router_item_ssid);
           macadr = v.findViewById(R.id.router_item_mac);
           psk = v.findViewById(R.id.router_item_password);
           img = v.findViewById(R.id.router_item_img);
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.router_item, parent, false);
        return new ViewHolder(v);

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        if (routers.get(position).getSuccess()){
            adapter.img.setColorFilter(ContextCompat.getColor(context, R.color.green));
        }
        if (hide){
            adapter.ip.setText("Hidden");
            adapter.title.setText(routers.get(position).getTitle());
            adapter.auth.setText(routers.get(position).getAuth());
            adapter.macadr.setText("Hidden");
            adapter.ssid.setText("Hidden");
            adapter.psk.setText(routers.get(position).getPsk());
        }else{
        adapter.ip.setText(String.valueOf(routers.get(position).getIp()));
        adapter.title.setText(routers.get(position).getTitle());
        adapter.auth.setText(routers.get(position).getAuth());
        adapter.macadr.setText(routers.get(position).getBssid());
        adapter.ssid.setText(routers.get(position).getSsid());
        adapter.psk.setText(routers.get(position).getPsk());
    }}

    @Override
    public int getItemCount() {

        return routers.size();
    }
    public void toaster(String msg){
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(context,
                    msg, Toast.LENGTH_SHORT);
            toast.show();
        });

    }
    public void appendtext(String text,TextView output){
        activity.runOnUiThread(() -> output.append(text));
    }
    public int additem (Router item){
        routers.add(item);
        all.add(item);
        notifyItemInserted(routers.size()-1);
        return routers.size() -1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    public ArrayList<Router> getlist(){
        return all;
    }
}
