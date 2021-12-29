package com.zalexdev.stryker.wifi.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import com.zalexdev.stryker.utils.Core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BruteWps extends AsyncTask<Void, String, ArrayList<String>> {
    public String exec = Core.EXECUTE;
    public String chroot;
    public Activity mActivity;
    public TextView percent;
    public TextView speed;
    public TextView pin;
    public String bssid;
    public int pid;
    public Core core;


    public BruteWps(Activity activity, final TextView per, TextView spe, TextView pi, String bssi,Core c) {
        mActivity = activity;
        percent = per;
        speed = spe;
        pin = pi;
        bssid = bssi;
        core =c;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected ArrayList<String> doInBackground(Void... command) {
            String line;
        ArrayList<String> result = new ArrayList<>();

            try {
                Process process = Runtime.getRuntime().exec("su -mm");
                OutputStream stdin = process.getOutputStream();
                InputStream stderr = process.getErrorStream();
                InputStream stdout = process.getInputStream();
                String cmd = "python3 -u /CORE/PixieWps/pixie.py -i wlan0 --iface-down -B -b "+bssid;
                stdin.write(("svc wifi disable&&sleep 2&&"+exec+"'"+cmd+"'"+" &&echo BRUTEFINISHED&&sleep 2&&svc wifi enable"+ '\n').getBytes());
                stdin.write(("exit\n").getBytes());
                stdin.flush();
                stdin.close();
                pid = getPid(process);
                ArrayList<String> out = new ArrayList<>();
                ArrayList<String> outerror = new ArrayList<>();
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                while ((line = br.readLine()) != null) {
                    out.add(line);
                    if (line.contains("% complete")){
                        String per = getPercent(line);
                        String spe = getSpeed(line);
                        setText(per,percent);
                        setText(spe,speed);
                    }else if(line.contains("Trying PIN")){
                        String pincode = getPincode(line);
                        setText(pincode,pin);
                    }else if(line.contains("BRUTEFINISHED")){
                        result = issuccess(out);
                    }

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

        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {

        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }
public ArrayList<String> issuccess(ArrayList<String> out){
    String pin;
    String pass;
    String stat = "false";
    ArrayList<String> back = new ArrayList<>();
    for (int i = 0;i<out.size();i++){
        String s = out.get(i);
        if (s.contains("[+] WPS PIN:")){
            pin = s.replace("[+] WPS PIN: ","").replaceAll("'","");
            stat = "true";
            back.add(pin);
        } else if (s.contains("[+] WPA PSK:")){
            pass = s.replace("[+] WPA PSK: ","").replaceAll("'","");
            stat = "true";
            back.add(pass);
        } else if (s.contains("Terminated") && !stat.equals("true")){
            stat = "timeout";
        }
    }
    if (out.size() == 0){
        stat = "error";
    }
    back.add(0,stat);
    return back;
}
public void kill() throws IOException {
        Runtime.getRuntime().exec("su -c 'kill "+pid+"'");
        Runtime.getRuntime().exec("su -c 'svc wifi enable'");
}
public void setText(String text,TextView textView){
        mActivity.runOnUiThread(() -> textView.setText(text));
}
    public String getPercent(final String input) {
        String result ="";
        Matcher matcher = Pattern.compile("\\d+\\.\\d+%").matcher(input);
        if (matcher.find()){
            result = matcher.group(0);
        }
        return "Percent done: "+result+"/100%";
    }
    public String getSpeed(final String input) {
        String result ="";
        Matcher matcher = Pattern.compile("\\d+\\.\\d+ seconds").matcher(input);
        if (matcher.find()){
            result = matcher.group(0);
        }
        assert result != null;
        return "Speed: "+result.replace("seconds","sec/1 pin");

    }
    public String getPincode(final String input) {
        String result ="";
        Matcher matcher = Pattern.compile("\\d+").matcher(input);
        if (matcher.find()){
            result = matcher.group(0);
        }
        return "Now: "+result;
    }

    public static int getPid(Process p) {
        int pid;

        try {
            Field f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            pid = f.getInt(p);
            f.setAccessible(false);

        } catch (Throwable ignored) {
            try {
                Matcher m = Pattern.compile("pid=(\\d+)").matcher(p.toString());
                pid = m.find() ? Integer.parseInt(Objects.requireNonNull(m.group(1))) : -1;
            } catch (Throwable ignored2) {
                pid = -1;
            }
        }
        return pid;
    }
}
