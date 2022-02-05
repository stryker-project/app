package com.zalexdev.stryker.searchsploit;


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
import androidx.recyclerview.widget.RecyclerView;

import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Sploit;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomChrootCommand;
import com.zalexdev.stryker.utils.CustomCommand;

import java.util.ArrayList;

public class SploitAdapter extends RecyclerView.Adapter<SploitAdapter.ViewHolder> {
    public ArrayList<Sploit> exploits;
    public Context context;
    public Activity activity;
    public Core core;

    public SploitAdapter(Context context2, Activity mActivity, ArrayList<Sploit> sploits) {
        context = context2;
        exploits = sploits;
        activity = mActivity;
        core = new Core(context2);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.sploit_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        Sploit temp = exploits.get(position);
        adapter.title.setText(temp.getTitle());
        adapter.date.setText(temp.getAuthor());
        adapter.platform.setText(temp.getPlatform() + " (" + temp.getType() + ")");
        adapter.run.setOnClickListener(view -> core.toaster(temp.getPath()));
        adapter.run.setOnClickListener(view -> {
            new CustomCommand("cp /data/local/stryker/release"+temp.getPath()+" /storage/emulated/0/Stryker/exploits/",core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            core.toaster(core.str("saved_to_exp")+temp.getPath().split("/")[temp.getPath().split("/").length-1]);
        });
    }

    @Override
    public int getItemCount() {

        return exploits.size();
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
        public TextView title;
        public TextView date;
        public TextView platform;
        public ImageView run;

        public ViewHolder(View v) {
            super(v);
            date = v.findViewById(R.id.sploit_date);
            title = v.findViewById(R.id.sploit_title);
            platform = v.findViewById(R.id.sploit_platform);
            run = v.findViewById(R.id.run_sploit);
        }

    }

}
