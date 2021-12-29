package com.zalexdev.stryker.wifi.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import com.zalexdev.stryker.utils.Core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class EnableMonitor extends AsyncTask<Void, String, Boolean> {
    public String exec = Core.EXECUTE;
    public String wlan;
    public String channel;
    public Core core;
    public EnableMonitor(String wlan1,String ch,Core c) {
        core =c;
        channel = ch;
        wlan = wlan1;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected Boolean doInBackground(Void... command) {
        String line;
        boolean result = false;
        try {
                Process process = Runtime.getRuntime().exec("su -mm");
                OutputStream stdin = process.getOutputStream();
                InputStream stderr = process.getErrorStream();
                InputStream stdout = process.getInputStream();
                if (wlan.equals("wlan0")){
                   stdin.write(("ip link set wlan0 down; echo 4 > /sys/module/wlan/parameters/con_mode;ip link set wlan0 up&&sleep 3&&"+exec+"'iw dev'"+ '\n').getBytes());
                }else {
                    stdin.write((exec+" 'airmon-ng start "+wlan+" "+channel+"'"+ '\n').getBytes());
                }
                stdin.write(("exit\n").getBytes());
                stdin.flush();
                stdin.close();
            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                while ((line = br.readLine()) != null) {
                    out.add(line);
                    if (wlan.equals("wlan0") && line.contains("monitor")){
                        result = true;
                    }else if(!wlan.equals("wlan0")){
                        String temp = line.replaceAll("\\s+","").replace("*","");
                        if (temp.contains("mac80211monitormodevifenabled")){
                            result = true;
                        }
                    }
                }
                br.close();
                br = new BufferedReader(new InputStreamReader(stderr));
                while ((line = br.readLine()) != null) {
                    outerror.add(line);
                    onProgressUpdate(line);
                }
            core.writetolog(out,false);
            core.writetolog(outerror,true);
                br.close();
                process.waitFor();
                process.destroy();
            } catch (IOException e) {
                Log.d(TAG, "An IOException was caught: " + e.getMessage());
            } catch (InterruptedException ex) {
                Log.d(TAG, "An InterruptedException was caught: " + ex.getMessage());
            }

        return result;
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
