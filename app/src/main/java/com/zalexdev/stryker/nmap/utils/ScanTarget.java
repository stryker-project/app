package com.zalexdev.stryker.nmap.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.zalexdev.stryker.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ScanTarget extends AsyncTask<Void, String, Boolean> {
    public String exec = Core.EXECUTE;
    public String ip;
    public Activity activity;
    public TextView output;
    public Core core;
    public ArrayList<Boolean> settings;

    public ScanTarget(String i, ArrayList<Boolean> s, Context context, Activity a, TextView o) {
        ip = i;
        core = new Core(context);
        activity = a;
        output = o;
        settings = s;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("WrongThread")
    @Override
    protected Boolean doInBackground(Void... command) {
        String line;
        Boolean ok = false;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            StringBuilder cmd = new StringBuilder();
            cmd.append("nmap ").append(ip).append(" ");
            if (settings.get(0)) {
                cmd.append(" -O ");
            }
            if (settings.get(1)) {
                cmd.append(" -sV ");
            }
            if (settings.get(2)) {
                cmd.append(" -F --top 100 ");
            }
            if (settings.get(3)) {
                cmd.append(" -Pn ");
            }
            Timer checkprg = new Timer();
            checkprg.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        stdin.write(("" + '\n').getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 200, 1000);
            stdin.write((exec + "'" + cmd.toString() + "'&&echo SCANFINISHED" + '\n').getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> nmapoutput = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                nmapoutput.add(line);
                String finalLine = line;
                if (line.contains("SCANFINISHED")) {
                    ok = true;
                    break;
                }
                activity.runOnUiThread(() -> output.append(finalLine + "\n"));
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
                String finalLine1 = line;
                activity.runOnUiThread(() -> output.append("[E] " + finalLine1 + "\n"));
            }
            core.writetolog(nmapoutput, false);
            core.writetolog(outerror, true);
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            Log.d(TAG, "An IOException was caught: " + e.getMessage());
        }
        return ok;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }


}
