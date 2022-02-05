package com.zalexdev.stryker.router_scan.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.custom.Router;
import com.zalexdev.stryker.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RsV2 extends AsyncTask<Void, String, Router> {
    public String exec = Core.EXECUTE;
    public String chroot;
    public Context mContext;
    public Activity mActivity;
    public Core core;
    public String ip;
    public Process proc;


    TextView textprg;


    public RsV2(Activity activity, Context context, TextView text, String ip1) {
        core = new Core(context);
        mContext = context;
        chroot = core.chroot();
        ip = ip1;

        mActivity = activity;
        textprg = text;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected Router doInBackground(Void... command) {
        String line;
        Router r = new Router();

        try {
            proc = Runtime.getRuntime().exec("su -mm");
            OutputStream stdin = proc.getOutputStream();
            InputStream stderr = proc.getErrorStream();
            InputStream stdout = proc.getInputStream();
            String cmd = "rs " + ip +" /sdcard/Stryker/rs/auth_basic.txt /sdcard/Stryker/rs/auth_digest.txt /sdcard/Stryker/rs/auth_form.txt";
            stdin.write((exec + "'" + cmd + "'" + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out2 = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out2.add(line);
                onProgressUpdate(line);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
                onProgressUpdate(line);
            }
            core.writetolog(out2, false);
            core.writetolog(outerror, true);
            br.close();
            proc.waitFor();
            proc.destroy();
            r = rs_result(out2);

        } catch (IOException e) {
            Log.d(TAG, "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d(TAG, "An InterruptedException was caught: " + ex.getMessage());
        }

        return r;
    }

    @Override
    protected void onPostExecute(Router result) {
        super.onPostExecute(result);
    }

    public void kill() {
        proc.destroy();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (values[0].contains("log in")) {
            Matcher m = Pattern.compile("[0-9]+").matcher(values[0]);
            String percent = "";
            if (m.find()) {
                percent = m.group();
                setText(textprg, "Bruting... (" + percent + "%)");
            }
        } else if (values[0].contains("Status")) {
            setText(textprg, values[0].replace("Status: ",""));
        }
    }


    public Router rs_result(ArrayList<String> output) {
        Router result = new Router();
        result.setSuccess(false);

        for (int i = 0; i < output.size(); i++) {
            String temp = output.get(i);
            if (temp.contains("SSID:") && !temp.contains("BSSID:")) {
                String ssid = temp.replace("SSID: ", "");
                result.setSsid(ssid);
                result.setSuccess(true);
            } else if (temp.contains("Auth:")) {
                String auth = temp.replace("Auth: ", "");
                result.setAuth(auth);
                result.setSuccess(true);
            } else if (temp.contains("Key:")) {
                String pswd = temp.replace("Key: ", "");
                result.setPsk(pswd);
                result.setSuccess(true);
            } else if (temp.contains("WPS:")) {
                String wps = temp.replace("WPS: ", "");
                result.setWps(wps);
                result.setSuccess(true);
            } else if (temp.contains("Title:")) {
                String title = temp.replace("Title: ", "");
                result.setTitle(title);
            } else if (temp.contains("BSSID:")){
                String mac = temp.replace("BSSID: ","");
                result.setBssid(mac);
            }
            if (result.getSuccess()) {
                result.setStatus("Success");
                result.setType(1);
            }
        }
        return result;
    }

    public void setText(TextView textView, String text) {
        mActivity.runOnUiThread(() -> textView.setText(text));
    }

    public void setProg(LinearProgressIndicator progressIndicator, int prog) {
        mActivity.runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);

            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(prog, true);
            }
        });

    }

}


