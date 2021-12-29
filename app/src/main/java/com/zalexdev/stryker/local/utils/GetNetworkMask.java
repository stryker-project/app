package com.zalexdev.stryker.local.utils;

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

public class GetNetworkMask extends AsyncTask<Void, String, String> {


    public Core core;
    public GetNetworkMask(Core c) {
        core = c;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected String doInBackground(Void... command) {
            String line;
            String gw = "0.0.0.0";


            try {

                Process process = Runtime.getRuntime().exec("su -mm");
                OutputStream stdin = process.getOutputStream();
                InputStream stderr = process.getErrorStream();
                InputStream stdout = process.getInputStream();
                stdin.write(("ip route show | grep wlan0"+ '\n').getBytes());
                stdin.write(("exit\n").getBytes());
                stdin.flush();
                stdin.close();
                ArrayList<String> out = new ArrayList<>();
                ArrayList<String> outerror = new ArrayList<>();
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                while ((line = br.readLine()) != null) {
                    out.add(line);
                    String[] res = line.split(" ");
                    gw = res[0];

                }
                br.close();
                br = new BufferedReader(new InputStreamReader(stderr));
                while ((line = br.readLine()) != null) {
                    onProgressUpdate(line);
                    outerror.add(line);
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

        return gw;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }


}
