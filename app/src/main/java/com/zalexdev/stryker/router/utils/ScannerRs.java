package com.zalexdev.stryker.router.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Router;
import com.zalexdev.stryker.router.RouterAdapter;
import com.zalexdev.stryker.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScannerRs extends AsyncTask<Void, String, Router> {
    public String exec = Core.EXECUTE;

    public Core core;
    public String ip;
    public Process proc;
    public String port;
    public ArrayList<String> out2;
    public RouterAdapter adap;
    public int pos;


    public ScannerRs(Context context, String ip1, String p) {
        ip = ip1;
        port = p;
        core = new Core(context);
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
                String cmd = "rs "+ip+":"+port+" /CORE/RS/auth_basic.txt /CORE/RS/auth_digest.txt /CORE/RS/auth_form.txt";
                stdin.write((exec+"'"+cmd+"'"+ '\n').getBytes());
                stdin.write(("exit\n").getBytes());
                stdin.flush();
                stdin.close();
                 out2 = new ArrayList<>();

                ArrayList<String> outerror = new ArrayList<>();
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                while ((line = br.readLine()) != null) {
                    out2.add(line);
                    onProgressUpdate(line);
                }
                br.close();
                onProgressUpdate("finished");
                br = new BufferedReader(new InputStreamReader(stderr));
                while ((line = br.readLine()) != null) {
                    outerror.add(line);

                }
                core.writetolog(out2,false);
                core.writetolog(outerror,true);
                br.close();
                proc.waitFor();
                proc.destroy();
                r = rs_result(out2);
                r.setIp(ip);

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

    public void kill(){
        proc.destroy();
    }
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);


    }




    public Router rs_result(ArrayList<String> output){

        Router result = new Router();
        result.setSuccess(false);
        result.setOk(true);
        for (int i = 0;i<output.size();i++){
            String temp = output.get(i);
            if (temp.contains("SSID:") && !temp.contains("BSSID:")){
                String ssid = temp.replace("SSID: ","");
                result.setSsid(ssid);
                result.setSuccess(true);
            }else if(temp.contains("BSSID")){
                String bssid = temp.replace("BSSID: ","");
                result.setBssid(bssid);
                result.setSuccess(true);
            }else if (temp.contains("Auth:")){
                String auth = temp.replace("Auth: ","");
                result.setAuth(auth);
                result.setSuccess(true);
            }else if (temp.contains("Key:")){
                String pswd = temp.replace("Key: ","");
                result.setPsk(pswd);
                result.setSuccess(true);
            }else if (temp.contains("WPS:")){
                String wps = temp.replace("WPS: ","");
                result.setWps(wps);
                result.setSuccess(true);
            }else if (temp.contains("Type:")){
                String title = temp.replace("Type: ","").replace("Unknown","").replace(")","").replace("(","");
                result.setTitle(title);
                result.setType(1);
            }
            if (result.getSuccess()){
                result.setStatus("Success");
                result.setType(2);

            }
        }
        return result;
    }


    }


