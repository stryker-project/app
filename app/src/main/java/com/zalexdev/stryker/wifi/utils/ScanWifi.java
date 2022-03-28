package com.zalexdev.stryker.wifi.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

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
 * This class is used to scan for wifi networks and parse the output of the scan
 */
public class ScanWifi extends AsyncTask<Void, String, ArrayList<WiFiNetwork>> {
    public String exec = Core.EXECUTE;
    public String wlan;
    public int count = 0;
    public int count2 = 0;
    public Core core;

    public ScanWifi(String whatwlan, Core c) {
        core = c;
        wlan = whatwlan;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        count = 0;
        count2 = 0;
    }

    @SuppressLint("WrongThread")
    @Override
    protected ArrayList<WiFiNetwork> doInBackground(Void... command) {
        String line;
        ArrayList<WiFiNetwork> result = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'iw " + wlan + " scan'&&echo SCANFINISHED" + '\n').getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out2 = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out2.add(line);
                if (line.contains("SCANFINISHED")) {//detect scan finished
                    result = parsewifi(out2);
                    onPostExecute(result);
                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
            }
            core.writetolog(out2, false);
            core.writetolog(outerror, true);
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            Log.d(TAG, "An IOException was caught: " + e.getMessage());
        }
        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<WiFiNetwork> result) {
        super.onPostExecute(result);

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }

    public ArrayList<WiFiNetwork> parsewifi(ArrayList<String> output) {
        WiFiNetwork wifi = new WiFiNetwork();
        ArrayList<WiFiNetwork> networks = new ArrayList<>();
        count = 0;
        count2 = 0;
        for (int i = 0; i < output.size(); i++) {
            String temp = output.get(i).replaceAll("\\s+", "").replace("*", "");
            if (temp.contains("BSS") && temp.contains("wlan") && !temp.contains("Load") && !temp.contains("width") && !temp.contains("scan")) {
                Matcher m = Pattern.compile("((\\w{2}:){5}\\w{2})").matcher(temp);
                String mac = "";
                if (m.find()) {
                    mac = m.group();
                }
                count = count + 1;
                wifi.setMac(mac);
            } else if (temp.contains("signal:")) {
                String power = temp.replace("signal:", "").replace("dBm", "");
                wifi.setPower(power.substring(0, power.length() - 3).replace("-", ""));
                count = count + 1;//get power
            } else if (temp.contains("SSID:")) {
                String name = temp.replace("SSID:", "");
                if (name.contains("\\x")) {
                    name = "Unsupported name";
                }
                if (name.length() != 0) {
                    wifi.setSsid(name);
                } else {
                    wifi.setSsid("Hidden network");
                }
                count = count + 1;//get wifi name
            } else if (temp.contains("DSParameterset:channel") && count == 3) {
                String ch = temp.replace("DSParameterset:channel", "");
                wifi.setChannel(ch);
                count = count + 1;//wifi channel
            } else if (temp.contains("primarychannel:") && count == 3) {
                String ch = temp.replace("primarychannel:", "");
                wifi.setChannel(ch);
                wifi.setIs5hhz(true);
                count = count + 1;//wifi channel 5g
            }
            if (count == 4) {
                networks.add(wifi);
                count = 0;
                wifi = new WiFiNetwork();

            }
            if (temp.contains("WPS:Version")) {
                networks.get(networks.size() - 1).setWps(true);//detect wps
            } else if (temp.contains("Model:")) {
                String model = temp.replace("Model:", "");
                networks.get(networks.size() - 1).setModel(model);//detect model
            } else if (temp.contains("APsetuplocked:0x01")) {
                networks.get(networks.size() - 1).setBlocked(true);

            }
        }
        return networks;
    }

}
