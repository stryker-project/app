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


public class CheckHandshake extends AsyncTask<Void, String, Boolean> {
    public String exec = Core.EXECUTE;
    public String chroot;

    public CheckHandshake() { }


    @Override
    protected void onPreExecute() { super.onPreExecute(); }

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
                stdin.write((exec+"'cowpatty -c -r /sdcard/Stryker/hs/handshake-01.cap'"+ '\n').getBytes());
                stdin.write(("exit\n").getBytes());
                stdin.flush();
                stdin.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                while ((line = br.readLine()) != null) {

                if (line.contains("all")){
                        result = true;
                    }
                }
                br.close();
                br = new BufferedReader(new InputStreamReader(stderr));
                while ((line = br.readLine()) != null) {
                    onProgressUpdate(line);

                }

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
