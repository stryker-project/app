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
 * This class checks if the device is connected to the internet from chroot
 */
public class CheckInet extends AsyncTask<Void, String, Boolean> {

    public CheckInet() {

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
            InputStream stdout = process.getInputStream();
            stdin.write((Core.EXECUTE+" 'ping -c 1 zalex.dev'" + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
               if (line.contains("1 recieved")){
                   result = true;
               }
            }
            br.close();
            process.waitFor();
            process.destroy();
            if (process.exitValue() == 0) {
                result = true;
            }
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
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
