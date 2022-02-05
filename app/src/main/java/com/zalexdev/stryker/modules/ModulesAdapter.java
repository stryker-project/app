package com.zalexdev.stryker.modules;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.zalexdev.stryker.InstalModuleActivity;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.coremanger.utils.InstallPackage;
import com.zalexdev.stryker.custom.Module;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomChrootCommand;
import com.zalexdev.stryker.utils.CustomCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ModulesAdapter extends RecyclerView.Adapter<ModulesAdapter.ViewHolder> {
    public ArrayList<Module> modules;
    public Context context;
    public Activity activity;
    public Core core;
    public int id = 0;

    public ModulesAdapter(Context context2, Activity mActivity, ArrayList<Module> m) {
        context = context2;
        modules = m;
        activity = mActivity;
        core = new Core(context2);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.module_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
       Module m = modules.get(position);
       adapter.name.setText(m.getName());
       adapter.authorver.setText(m.getAuthor()+" ("+m.getVersion()+")");
       adapter.desc.setText(m.getDesc());
       String formated_name = m.getName().replace(" ","_");
       if (!core.checkmod(m.getName())){
           if (!m.isOnly64bit() || core.is64Bit()){
       adapter.install.setOnClickListener(view -> {
           adapter.install.setVisibility(View.GONE);
           adapter.prog.setVisibility(View.VISIBLE);
           new Thread(() -> {
               boolean d = download(m.getSrcinstall(),formated_name+".zip",adapter.prog);
               if (d){
                   new CustomCommand("mkdir storage/emulated/0/Stryker/modules",core).execute();
                   new CustomCommand("mv /storage/emulated/0/Download/"+formated_name+".zip /storage/emulated/0/Stryker/modules/"+formated_name+".zip",core).execute();
                   new CustomCommand("mkdir /data/local/stryker/release/modules",core).execute();
                   try {
                       activity.runOnUiThread(() -> core.toaster("Installing "+m.getPksg()));
                       boolean apk = new InstallPackage(m.getPksg(),core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                       Boolean o1 = new CustomCommand("rm -rf /data/local/stryker/release/modules/"+formated_name,core).execute().get();
                       Boolean o = new CustomCommand("mkdir /storage/emulated/0/Stryker/modules/"+formated_name,core).execute().get();
                       core.unzip(new File("/storage/emulated/0/Stryker/modules/"+formated_name+".zip"), new File("/storage/emulated/0/Stryker/modules/" + formated_name));
                       Boolean move = new CustomCommand("mv /storage/emulated/0/Stryker/modules/"+formated_name+" /data/local/stryker/release/modules/"+formated_name,core).execute().get();
                        core.installmod(m.getName());
                       Intent mouduleinst = new Intent(activity, InstalModuleActivity.class);
                       mouduleinst.putExtra("path",formated_name);
                       mouduleinst.putExtra("install",true);
                       activity.startActivity(mouduleinst);
                   } catch (ExecutionException | InterruptedException e) {
                       e.printStackTrace();
                   }

               }
           }).start();

       });}else{
               adapter.install.setOnClickListener(view -> core.toaster(core.str("only64")));

           }
           }else{
           adapter.install.setImageDrawable(context.getDrawable(R.drawable.delete));
           adapter.install.setOnClickListener(view -> {
               core.deletemod(m.getName());
               new CustomCommand("rm -rf /storage/emulated/0/Stryker/modules/"+formated_name,core).execute();
               new CustomCommand("rm /storage/emulated/0/Stryker/modules/"+formated_name+".zip",core).execute();
               //moduler(m.getName(),"/storage/emulated/0/Stryker/modules/"+formated_name+"/delete.sh",false);
               Intent moduledel = new Intent(activity, InstalModuleActivity.class);
               moduledel.putExtra("path",formated_name);
               moduledel.putExtra("install",false);
               activity.startActivity(moduledel);
           });
       }
    }

    @Override
    public int getItemCount() {

        return modules.size();
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
        public TextView name;
        public TextView authorver;
        public TextView desc;
        public ImageView install;
       public CircularProgressIndicator prog;
        public MaterialCardView card;

        public ViewHolder(View v) {
            super(v);
            authorver = v.findViewById(R.id.module_author_and_ver);
            name = v.findViewById(R.id.module_name);
            desc = v.findViewById(R.id.module_desc);
            install = v.findViewById(R.id.module_install);
            prog = v.findViewById(R.id.module_indicator);
            card = v.findViewById(R.id.item);
        }

    }
    @SuppressLint("Range")
    public Boolean download(String url, String name, CircularProgressIndicator progress) {
        boolean ok = false;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(core.str("install2"));
        request.setTitle(core.str("wait"));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);
        boolean downloading = true;
        while (downloading) {
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(downloadId);
            Cursor cursor = manager.query(q);
            cursor.moveToFirst();
            @SuppressLint("Range") int bytes_downloaded = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            @SuppressLint("Range") int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            if (bytes_total == 0) {
                break;
            }
            final int dl_progress = (int) ((bytes_downloaded * 100L) / bytes_total);
            setProg(progress, dl_progress);
            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                downloading = false;
                ok = true;

            }cursor.close();
        }
        return ok;

    }
    public void setText(TextView textView, String text, boolean animate) {
        activity.runOnUiThread(() -> {
            if (animate) {
                Animation fade = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                textView.startAnimation(fade);
            }
            textView.setText(text);
        });
    }
    public void moduler(String name, String path,boolean install){
        activity.runOnUiThread(() -> {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.module_progress);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            CircularProgressIndicator prog = dialog.findViewById(R.id.module_prog);
            TextView title = dialog.findViewById(R.id.module_title);
            TextView progress = dialog.findViewById(R.id.module_progress);
            TextView cancel = dialog.findViewById(R.id.module_cancel);
            cancel.setVisibility(View.INVISIBLE);
            cancel.setOnClickListener(view -> dialog.dismiss());
            ArrayList<String> commands = new ArrayList<>();
            if (install){
            title.setText(R.string.installing+name);}else{
                title.setText(core.str("deleting")+name);
            }
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                while ((line = br.readLine()) != null) {
                    commands.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            int pr = 100 / commands.size();
            final int[] total = {0};
            new Thread(() -> {
                for (String cmd: commands){
                    if (cmd.startsWith("#")){
                        setText(progress,cmd.replace("#",""),false);
                        setProg(prog, total[0]);
                    }else {
                        try {
                            Boolean bool = new CustomChrootCommand(cmd, core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        total[0] = total[0] + pr;
                    }
                }
                activity.runOnUiThread(() -> cancel.setVisibility(View.VISIBLE));

                setProg(prog,100);
                setText(cancel,"OK",false);
                setText(progress,core.str("finished"),false);
            }).start();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        });


    }


    public void setProg(CircularProgressIndicator progressIndicator, int prog) {
        activity.runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);

            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(prog, true);
            }
        });

    }
}
