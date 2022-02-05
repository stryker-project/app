package com.zalexdev.stryker.coremanger;


import static com.zalexdev.stryker.R.string.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.zalexdev.stryker.R;
import com.zalexdev.stryker.coremanger.utils.InstallPackage;
import com.zalexdev.stryker.custom.Package;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class CoreAdapter extends RecyclerView.Adapter<CoreAdapter.ViewHolder> {
    public ArrayList<Package> pkgs;
    public Context context;
    public Activity activity;
    public Core core;

    public CoreAdapter(Context context2, Activity mActivity, ArrayList<Package> pskss) {
        context = context2;
        pkgs = pskss;
        activity = mActivity;
        core = new Core(context2);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.package_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        Package temp = pkgs.get(position);
        adapter.title.setText(temp.getName());
        adapter.version.setText(temp.getVersion());
        adapter.install.setOnClickListener(view -> {
            adapter.install.setVisibility(View.INVISIBLE);
            core.toaster(core.str("installingg"));
            new Thread(() -> {
                try {
                    Boolean ok = new InstallPackage(temp.getName(),core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                    if (ok){
                        toaster(temp.getName()+core.str("installed"));
                    }else{
                        toaster(core.str("inst_error")+temp.getName());
                        activity.runOnUiThread(() -> adapter.install.setVisibility(View.VISIBLE));
                    }

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    @Override
    public int getItemCount() {

        return pkgs.size();
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
        public TextView version;
        public ImageView install;

        public ViewHolder(View v) {
            super(v);
            version = v.findViewById(R.id.version);
            title = v.findViewById(R.id.title);
            install = v.findViewById(R.id.run_sploit);
        }

    }

}
