package com.zalexdev.stryker.local_network.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.zalexdev.stryker.custom.Device;
import com.zalexdev.stryker.utils.Core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jcifs.netbios.NbtAddress;

public class ScanLocalDevice extends AsyncTask<Void, String, Device> {
    public String exec = Core.EXECUTE;
    public String ip;
    public Core core;

    public ScanLocalDevice(String i, Context context) {
        ip = i;
        try{
        core = new Core(context);}
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("WrongThread")
    @Override
    protected Device doInBackground(Void... command) {
        String line;
        Device d = new Device();
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            if (core.getBoolean("fast_scan")) {
                stdin.write((exec + "'nmap " + ip + " -F --top 100 -n -Pn -O --max-os-tries 1'&&echo LOCALSCANFINISHED" + '\n').getBytes());
            } else {
                stdin.write((exec + "'nmap " + ip + " -n -Pn -O --max-os-tries 1'&&echo LOCALSCANFINISHED" + '\n').getBytes());
            }
            stdin.flush();
            stdin.close();
            ArrayList<String> nmapoutput = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                nmapoutput.add(line);
                if (line.contains("LOCALSCANFINISHED")) {//detect scan finished
                    d = localdevices(nmapoutput);

                }
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
            }
            if (core != null){
            core.writetolog(nmapoutput, false);
            core.writetolog(outerror, true);}
            br.close();
            process.waitFor();
            process.destroy();

        } catch (IOException | InterruptedException e) {
            Log.d(TAG, "An IOException was caught: " + e.getMessage());
        }
        try {
            NbtAddress[] nbts = NbtAddress.getAllByAddress(ip);
            String netbiosname = nbts[nbts.length-1].getHostName();
            d.setSubname(netbiosname);
        } catch (Exception ignored) {
        }
        return d;
    }

    @Override
    protected void onPostExecute(Device result) {
        super.onPostExecute(result);

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }

    public Device localdevices(ArrayList<String> output) throws IOException {
        Device device = new Device();
        for (int i = 0; i < output.size(); i++) {
            String temp = output.get(i).replaceAll("\\s+", " ").replace("*", "");
            device.setIp(ip);
            device.setShim(false);
            if (temp.contains("/tcp")) {
                String r = temp.replace("/tcp", "").replace("open", "").replace("filtered","");
                String port = "";
                String service = "";
                Matcher m = Pattern.compile("[0-9]+").matcher(r);
                if (m.find()) {
                    port = m.group();
                    device.addPort(port);
                    service = r.replaceAll("\\s+", "").replace(port, "");
                    device.addService(service);
                }
            } else if (temp.contains("MAC Address")) {
                Matcher mac = Pattern.compile("((\\w{2}:){5}\\w{2})").matcher(temp);
                if (mac.find()) {
                    device.setMac(mac.group(0).toUpperCase(Locale.ROOT));
                }
                String vendor = temp.replace("MAC Address: ", "").replace(mac + " ", "").replace("(", "").replace(")", "").replace(mac.group() + " ", "");
                device.setVendor(vendor);
            }else if (temp.contains("Running:")){
                device.setOs(temp.replace("Running: ","").replace("Microsoft",""));
            }else if (temp.contains("No exact matches")){
                device.setOs("Unknown");
            }

        }
        return device;
    }

}
