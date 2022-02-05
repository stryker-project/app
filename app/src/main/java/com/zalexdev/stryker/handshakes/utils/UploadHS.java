package com.zalexdev.stryker.handshakes.utils;

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

public class UploadHS extends AsyncTask<Void, String, Integer> {
    public String exec = Core.EXECUTE;
    public String path;
    public String email;
    public Core core;


    public UploadHS(String p,String e,Context context) {
        core = new Core(context);
        email = e;
        path = p;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("WrongThread")
    @Override
    protected Integer doInBackground(Void... command) {
        String line;
        Integer state = 0;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'curl -X POST -F 'email="+email+"' -F 'file=@"+path+"' https://api.onlinehashcrack.com'&&echo UPLOADFINISHED" + '\n').getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> hsoutput = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                hsoutput.add(line);
                String finalLine = line;
                if (line.contains("UPLOADFINISHED")) {
                    state = checkhs(hsoutput);
                    break;
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
            }
            core.writetolog(hsoutput, false);
            core.writetolog(outerror, true);
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            Log.d(TAG, "An IOException was caught: " + e.getMessage());
        }
        return state;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }

    public Integer checkhs(ArrayList<String> output){
        Integer res = 0;
        for (String line : output){
            if (line.contains("successfully added")){
                res = 2;
            }else if (line.contains("has been already sent")){
                res = 1;
            }
        }
        return res;
    }
}
