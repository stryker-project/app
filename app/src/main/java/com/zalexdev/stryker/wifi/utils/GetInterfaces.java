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

public class GetInterfaces extends AsyncTask<Void, String, ArrayList<String>> {
    public String exec = Core.EXECUTE;
    public Core core;

    public GetInterfaces(Core c) {
        core = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected ArrayList<String> doInBackground(Void... command) {
        String line;
        ArrayList<String> inter = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("su -mm");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            String cmd = "airmon-ng";
            stdin.write((exec + "'" + cmd + "'" + " |grep phy &&echo SCANFINISHED" + '\n').getBytes());
            stdin.write(("y\n").getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                if (line.contains("assign")) {
                    stdin.write(("y\n").getBytes());
                    stdin.flush();
                }
                out.add(line);
                if (!line.equals("SCANFINISHED")) {
                    String[] temp = line.trim().replaceAll("\\s+", " ").split(" ");
                    inter.add(temp[1]);
                }
            }
            stdin.close();
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                if (!line.contains("or not found")) {
                    outerror.add(line);
                }
                onProgressUpdate(line);
            }
            core.writetolog(out, false);
            core.writetolog(outerror, true);
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            Log.d(TAG, "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d(TAG, "An InterruptedException was caught: " + ex.getMessage());
        }

        return inter;
    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {

        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }


}
