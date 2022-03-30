package com.zalexdev.stryker.local_network.utils;

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

public class CutNetwork extends AsyncTask<Void, Void, Void> {


    public Core core;
    public String target;
    public String gateway;
    public Process process;
    public int type;

    public CutNetwork(Core c, String t, String gw, int ty) {
        core = c;
        target = t;
        gateway = gw;
        type = ty;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected Void doInBackground(Void... command) {
        String line;


        try {

            process = Runtime.getRuntime().exec("su -mm");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            if (type == 0) {
                stdin.write((Core.EXECUTE + " 'python3 /CORE/MegaCut/megacut.py " + target + " " + gateway + " -k'" + '\n').getBytes());
            } else if (type == 1) {
                stdin.write((Core.EXECUTE + " 'python3 /CORE/MegaCut/megacut.py " + target + " " + gateway + " -m'" + '\n').getBytes());
            } else if (type == 2) {
                stdin.write((Core.EXECUTE + " 'python3 /CORE/MegaCut/megacut.py " + target + " " + gateway + " -b'" + '\n').getBytes());
            } else if (type == 3) {
                stdin.write((Core.EXECUTE + " 'python3 /CORE/MegaCut/megacut.py " + target + " " + gateway + " -r'" + '\n').getBytes());
            }

            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
            }
            core.writetolog(out, false);
            core.writetolog(outerror, true);
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }

        return null;
    }

    public void kill() {
        process.destroy();
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

    }


}
