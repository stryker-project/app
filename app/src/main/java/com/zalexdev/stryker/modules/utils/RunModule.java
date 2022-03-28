package com.zalexdev.stryker.modules.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;

import com.zalexdev.stryker.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * This class is used to run the install and delete scripts for the modules
 */
public class RunModule extends AsyncTask<Void, String, Boolean> {

    public String formatted_name;
    public Core core;
    public TextView log;
    public Activity activity;
    public boolean install;
    public String name;
    public RunModule(String command, Core c, TextView l, Activity a,Boolean i,String n) {
        formatted_name = command;
        core = c;
        log = l;
        activity = a;
        install = i;
        name = n;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected Boolean doInBackground(Void... command) {

        boolean result = false;


        try {
            Process process = Runtime.getRuntime().exec("su -mm");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((Core.EXECUTE+" ash"+"\n").getBytes());
            stdin.flush();
            stdin.write(("cd /modules/"+ formatted_name +"\n").getBytes());
            stdin.flush();
            stdin.write(("chmod 777 *\n").getBytes());
            if (install){
            stdin.write(("/modules/"+formatted_name+"/install.sh"+"\n").getBytes());
            }
            else{
                stdin.write(("/modules/"+formatted_name+"/delete.sh"+"\n").getBytes());
            }
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            new Thread(() -> {
                String line;
                try{
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                while ((line = br.readLine()) != null) {
                    out.add(line);
                    String finalLine = line;
                    activity.runOnUiThread(() -> appendText(finalLine,false));
                }
                br.close();}
             catch (IOException e) {
                Log.d(TAG, "An IOException was caught: " + e.getMessage());
            }
            }).start();
            new Thread(() -> {
                BufferedReader br1 = new BufferedReader(new InputStreamReader(stderr));
                String line1 = null;
                while (true) {
                    try {
                        if ((line1 = br1.readLine()) == null) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    assert line1 != null;
                    if (!line1.contains("%") && line1.contains("exists")){
                        outerror.add(line1);}
                    String finalLine = line1;
                    activity.runOnUiThread(() -> appendText(finalLine,true));
                }
                try {
                    br1.close() ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();


            core.writetolog(out, false);
            core.writetolog(outerror, true);
            process.waitFor();
            process.destroy();
            if (process.exitValue() == 0) {
                result = true;
            }
        } catch (IOException e) {
            Log.d(TAG, "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d(TAG, "An InterruptedException was caught: " + ex.getMessage());
        }
        if (install){core.installmod(name);}else{core.deletemod(name);}
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
    public void appendText(String text, boolean iserror){
        if (!iserror){
            log.append(white(text));
        }else{
            log.append(red(text));
        }
        log.append("\n");
    }
    public Spanned white(String out) {
        return Html.fromHtml("<font color='#FFFFFF'>" + out + "</font>");
    }

    public Spanned red(String out) {
        return Html.fromHtml("<font color='#F60B0B'>" + out + "</font>");
    }

}
