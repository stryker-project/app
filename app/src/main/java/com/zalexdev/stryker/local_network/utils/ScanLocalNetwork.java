package com.zalexdev.stryker.local_network.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.custom.Device;
import com.zalexdev.stryker.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanLocalNetwork extends AsyncTask<Void, String, ArrayList<Device>> {
    public String exec = Core.EXECUTE;
    public String ip;
    public Core core;
    public Activity activity;
    public LinearProgressIndicator progress;
    public boolean finished = false;

    public ScanLocalNetwork(String i, Context context,LinearProgressIndicator l,Activity a) {
        ip = i;
        core = new Core(context);
        progress = l;
        activity = a;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("WrongThread")
    @Override
    protected ArrayList<Device> doInBackground(Void... command) {
        String line;
        ArrayList<Device> d = new ArrayList<>();
        try {

            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'nmap " + ip + " -sP -n --stats-every 1s'&&echo LOCALSCANFINISHED" + '\n').getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> nmapoutput = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                nmapoutput.add(line);
                onProgressUpdate(line);
                if (line.contains("LOCALSCANFINISHED")) {//detect scan finished
                    d = localdevices(nmapoutput);
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
            }
            core.writetolog(nmapoutput, false);
            core.writetolog(outerror, true);
            br.close();
            process.waitFor();
            process.destroy();
            finished = true;
        } catch (IOException | InterruptedException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        }
        return d;
    }

    @Override
    protected void onPostExecute(ArrayList<Device> result) {
        super.onPostExecute(result);

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        activity.runOnUiThread(() -> {
            Matcher per = Pattern.compile("[0-9]*\\.[0-9]+%").matcher(values[0]);
            if (per.find()){
                setProg(progress,(int) Double.parseDouble(per.group().replace("%","")));
            }
        });

    }

    public ArrayList<Device> localdevices(ArrayList<String> output) throws IOException {
        ArrayList<Device> result = new ArrayList<>();
        Device device = new Device();
        for (int i = 0; i < output.size(); i++) {
            String temp = output.get(i).replaceAll("\\s+", " ").replace("*", "");
            if (temp.contains("Nmap scan report for ")) {
                device.setIp(temp.replace("Nmap scan report for ", ""));
            } else if (temp.contains("MAC Address")) {
                Matcher mac = Pattern.compile("((\\w{2}:){5}\\w{2})").matcher(temp);
                if (mac.find()) {
                    device.setMac(mac.group(0).toUpperCase(Locale.ROOT));
                }
                String vendor = temp.replace("MAC Address: ", "").replace(mac + " ", "").replace("(", "").replace(")", "").replace(mac.group() + " ", "");
                device.setVendor(vendor);
                result.add(device);
                device = new Device();
            }
        }
        return result;
    }
    public void setProg(LinearProgressIndicator progressIndicator, int prog) {
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
