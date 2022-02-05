package com.zalexdev.stryker.geomac.utils;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GetGeoByMac extends AsyncTask<Void, String, String> {
    public String mac;
    public Core core;

    public GetGeoByMac(String s,Core c) {
        mac = s;
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
        String result = "";


        try {

            Process process = Runtime.getRuntime().exec("su -mm");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((Core.EXECUTE+ "'./modules/GeoMac/geomac "+ mac + "'" + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                core.writelinetolog(line);
                Matcher coords = Pattern.compile("[0-9]*\\.[0-9]+,\\s[0-9]*\\.[0-9]+").matcher(line);
                Matcher coords1 = Pattern.compile("-[0-9]*\\.[0-9]+,\\s[0-9]*\\.[0-9]+").matcher(line);
                Matcher coords2 = Pattern.compile("-[0-9]*\\.[0-9]+,\\s-[0-9]*\\.[0-9]+").matcher(line);
                Matcher coords3 = Pattern.compile("[0-9]*\\.[0-9]+,\\s-[0-9]*\\.[0-9]+").matcher(line);
                if (coords.find()){ result = coords.group();break; }
                if (coords2.find()){ result = coords2.group();break; }
                if (coords1.find()){ result = coords1.group();break; }
                if (coords3.find()){ result = coords3.group();break; }
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
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }


}
