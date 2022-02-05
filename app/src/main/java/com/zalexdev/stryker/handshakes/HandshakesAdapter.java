package com.zalexdev.stryker.handshakes;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.WiFiNetwork;
import com.zalexdev.stryker.handshakes.utils.BruteHandshake;
import com.zalexdev.stryker.handshakes.utils.UploadHS;
import com.zalexdev.stryker.utils.Core;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandshakesAdapter extends RecyclerView.Adapter<HandshakesAdapter.ViewHolder> {
    public ArrayList<String> hslist;
    public Context context;
    public Activity activity;
    public Core core;
    public int id = 0;

    public HandshakesAdapter(Context context2, Activity mActivity, ArrayList<String> sploits) {
        context = context2;
        hslist = sploits;
        activity = mActivity;
        core = new Core(context2);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.handshake_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        Matcher m = Pattern.compile("((\\w{2}:){5}\\w{2})").matcher(hslist.get(position));
        String mac = hslist.get(position);
        if (m.find()) {
            mac = m.group(0);
        }
        String stored = core.getString(mac);
        if (stored.length() > 6) {
            adapter.filename.setTextColor(Color.parseColor("#FF1B5E20"));
            adapter.progress.setTextColor(Color.parseColor("#FF1B5E20"));
            adapter.progress.setText(core.str("pass_founded") + stored);
            adapter.time_left.setText(" ");
        }
        adapter.filename.setText(hslist.get(position).replace("/storage/emulated/0/Stryker/captured/", ""));
        String finalMac = mac;
        adapter.brute.setOnClickListener(view -> {
            ArrayList<String> get = core.getListFiles(new File(core.getStorage() + "Stryker/wordlist"));
            String[] w2 = new String[get.size()];
            for (int i = 0; i < get.size(); i++) {
                w2[i] = get.get(i).replace(core.getStorage() + "Stryker/wordlist/", "");
            }
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.select_word)
                    .setItems(w2, (dialogInterface, i) -> {
                        new Thread(() -> {
                            try {
                                id++;
                                BruteHandshake br = new BruteHandshake(hslist.get(position).replace(core.getStorage(), "/sdcard/"), get.get(i).replace(core.getStorage(), "/sdcard/"), core, activity, context, adapter.progress, adapter.time_left, id);
                                activity.runOnUiThread(() -> {
                                    adapter.brute.setVisibility(View.GONE);
                                    adapter.cancel.setVisibility(View.VISIBLE);
                                    adapter.cancel.setOnClickListener(view1 -> {
                                        br.kill();
                                        adapter.cancel.setVisibility(View.GONE);
                                        adapter.brute.setVisibility(View.VISIBLE);
                                    });
                                });
                                WiFiNetwork w = br.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                                activity.runOnUiThread(() -> {
                                    adapter.brute.setVisibility(View.VISIBLE);
                                    adapter.cancel.setVisibility(View.GONE);
                                    if (w.getOK()) {
                                        adapter.time_left.setText(" ");
                                        adapter.progress.setText(core.str("pass_founded") + w.getPsk());
                                        core.putString(finalMac, w.getPsk());
                                    } else {
                                        adapter.time_left.setText(" ");
                                        adapter.progress.setText(R.string.pass_not_found);

                                    }
                                });
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    })
                    .show();

        });
        adapter.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog valuedialog = new Dialog(context);
                valuedialog.setContentView(R.layout.input_dialog);
                valuedialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TextView title = valuedialog.findViewById(R.id.input_title);
                TextInputEditText valueedit = valuedialog.findViewById(R.id.getvalue);
                TextView ok = valuedialog.findViewById(R.id.ok_button);
                title.setText("Enter email");
                ok.setOnClickListener(view1 -> {
                    String email = valueedit.getText().toString();
                    valuedialog.dismiss();
                    adapter.upload.setVisibility(View.GONE);
                    new Thread(() -> {
                        try {
                            Integer upload = new UploadHS(hslist.get(position).replace("/storage/emulated/0/","/sdcard/"),email,context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                            activity.runOnUiThread(() -> {
                                if (upload == 0){
                                    core.toaster(core.str("error_upload"));
                                }else if (upload == 1){
                                    core.toaster(core.str("file_was_uploaded"));
                                }else if (upload == 2){
                                    core.toaster(core.str("upload_success"));
                                }
                                adapter.upload.setVisibility(View.VISIBLE);
                            });
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                });
                valuedialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {

        return hslist.size();
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
        public TextView filename;
        public TextView progress;
        public TextView time_left;
        public ImageView upload;
        public ImageView brute;
        public ImageView cancel;
        public MaterialCardView card;

        public ViewHolder(View v) {
            super(v);
            progress = v.findViewById(R.id.hs_progress);
            filename = v.findViewById(R.id.hs_name);
            time_left = v.findViewById(R.id.hs_time_left);
            upload = v.findViewById(R.id.hs_upload);
            brute = v.findViewById(R.id.hs_brute);
            cancel = v.findViewById(R.id.hs_cancel);
            card = v.findViewById(R.id.item);
        }

    }


}
