package com.zalexdev.stryker.local.utils;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanLocalNetwork extends AsyncTask<Void, String, ArrayList<Device>> {
    public String exec = Core.EXECUTE;
    public String ipmask;
    public Core core;
    public ScanLocalNetwork(String mask, Context context) { ipmask = mask;core = new Core(context); }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("WrongThread")
    @Override
    protected ArrayList<Device> doInBackground(Void... command) {
            String line;
            ArrayList<Device> result = new ArrayList<>();
            try {
                Process process = Runtime.getRuntime().exec("su");
                OutputStream stdin = process.getOutputStream();
                InputStream stderr = process.getErrorStream();
                InputStream stdout = process.getInputStream();
                if (core.getBoolean("fast_scan")){
                stdin.write((exec+"'nmap "+ipmask+" -F --top 100'&&echo LOCALSCANFINISHED" + '\n').getBytes());}
                else {
                stdin.write((exec+"'nmap "+ipmask+" -F'&&echo LOCALSCANFINISHED" + '\n').getBytes());}
                stdin.flush();
                stdin.close();
                ArrayList<String> nmapoutput = new ArrayList<>();
                ArrayList<String> outerror = new ArrayList<>();
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                while ((line = br.readLine()) != null) {
                    nmapoutput.add(line);
                    if (line.contains("LOCALSCANFINISHED")){//detect scan finished
                        result = localdevices(nmapoutput);
                        onPostExecute(result);
                    }
                }
                br.close();
                br = new BufferedReader(new InputStreamReader(stderr));
                while ((line = br.readLine()) != null) {
                    outerror.add(line);
                }
                core.writetolog(nmapoutput,false);
                core.writetolog(outerror,true);
                br.close();
                process.waitFor();
                process.destroy();
            } catch (IOException | InterruptedException e) {
                Log.d(TAG, "An IOException was caught: " + e.getMessage());
            }
        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<Device> result) {
        super.onPostExecute(result);

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }

public ArrayList<Device> localdevices(ArrayList<String> output){
    ArrayList<Device> devices = new ArrayList<>();
    Device device = new Device();
    for (int i = 0;i<output.size();i++){
    String temp = output.get(i).replaceAll("\\s+"," ").replace("*","");
    if (temp.contains("Nmap scan report")){
            String localip = "";
            Matcher m = Pattern.compile("([0-9]+(\\.[0-9]+)+)").matcher(temp);
            if (m.find()) {
                localip = m.group();
                device.setIp(localip);
            }
    }else if (temp.contains("/tcp")){
        String r= temp.replace("/tcp","").replace("open","");
        String port = "";
        String service= "";
        Matcher m = Pattern.compile("[0-9]+").matcher(r);
        if (m.find()) {
            port = m.group();
            device.addPort(port);
            service = r.replaceAll("\\s+","").replace(port,"");
            device.addService(service);
        }
    }else if (temp.contains("MAC Address")){
        String vendor = "";
        String mac = "";

        Matcher m = Pattern.compile("((\\w{2}:){5}\\w{2})").matcher(temp);
        if (m.find()) {
            mac = m.group();
            device.setMac(mac);
        }
        vendor = temp.replace("MAC Address: ","").replace(mac+" ","").replace("(","").replace(")","");
        device.setVendor(vendor);
        devices.add(device);
        device = new Device();

    }
    }
    return devices;
}

}
