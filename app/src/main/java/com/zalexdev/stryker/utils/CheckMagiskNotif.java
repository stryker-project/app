package com.zalexdev.stryker.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * It checks to see if the
 * notification policy is set to 1. If it is, it returns true. If it isn't, it returns false
 */
public class CheckMagiskNotif extends AsyncTask<Void, String, Boolean> {

    // This is the command that is being run on the device.
    public String cmd = "/data/data/com.zalexdev.stryker/files/sqlite3 /data/adb/magisk.db \"SELECT notification FROM policies WHERE package_name='com.zalexdev.stryker';\"";
    public Core core;

    public CheckMagiskNotif(Core c) {core = c;}

    @Override
    protected void onPreExecute() {super.onPreExecute();}

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
            stdin.write((cmd + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                if (line.contains("1")){result = true; }
                out.add(line);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
                onProgressUpdate(line);
            }
            br.close();
            core.writetolog(out, false);
            core.writetolog(outerror, true);
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
    protected void onProgressUpdate(String... values) {super.onProgressUpdate(values);}


}
