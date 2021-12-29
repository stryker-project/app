package com.zalexdev.stryker.utils;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zalexdev.stryker.custom.Router;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Core {
    public Context context;
    public final static String EXECUTE = "/data/data/com.zalexdev.stryker/cache/chroot_exec ";
    public final static String BUSYBOX = "/data/data/com.zalexdev.stryker/cache/busybox ";
    private final SharedPreferences preferences;
    public Core(Context context1) {

        context = context1;
        preferences = PreferenceManager.getDefaultSharedPreferences(context1);
    }



    public String chroot(){
        return getString("chroot_path");
    }

    public int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    public float getFloat(String key) {
        return preferences.getFloat(key, 0);
    }

    public String getString(String key) {
        return preferences.getString(key, "");
    }

    public ArrayList<String> getListString(String key) {
        return new ArrayList<String>(Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public void putInt(String key, int value) {
        isNull(key);
        preferences.edit().putInt(key, value).apply();
    }

    public void putString(String key, String value) {
        isNull(key); checkNull(value);
        preferences.edit().putString(key, value).apply();
    }

    public void putListString(String key, ArrayList<String> stringList) {
        isNull(key);
        String[] myStringList = stringList.toArray(new String[stringList.size()]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
    }

    public void putBoolean(String key, boolean value) {
        isNull(key);
        preferences.edit().putBoolean(key, value).apply();
    }

    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

    private void isNull(String key){
        if (key == null){
            throw new NullPointerException();
        }
    }

    private void checkNull(String value){
        if (value == null){
            throw new NullPointerException();
        }
    }
    public void toaster(String msg){
        Toast toast = Toast.makeText(context,
                msg, Toast.LENGTH_SHORT);
        toast.show();
    }
    public void writetolog(ArrayList<String> datas, boolean iserror){
        if (getBoolean("debug")) {
            String path = "/storage/emulated/0/Stryker/";
            File folder = new File(path);
            folder.mkdirs();
            File file = new File(folder, "log.txt");
            try {
                boolean newf = file.createNewFile();

                FileOutputStream fOut = new FileOutputStream(file, true);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                if (newf){
                    myOutWriter.append("Model:"+android.os.Build.MANUFACTURER+ android.os.Build.MODEL+ "\n");
                    if(Build.SUPPORTED_64_BIT_ABIS.length>0)
                    {
                        myOutWriter.append("Android:"+" "+Build.VERSION.SDK_INT+" (arm64)\n");
                    }
                    else
                    {
                        myOutWriter.append("Android:"+" "+Build.VERSION.SDK_INT+" (arm32)\n");
                    }

                }
                for (int i = 0; i < datas.size(); i++) {
                    SimpleDateFormat s = new SimpleDateFormat("[hh:mm:ss] ");
                    String format = s.format(new Date());
                    if (iserror){
                        myOutWriter.append(format +"[ERROR] "+datas.get(i) + "\n");
                    }else{
                    myOutWriter.append(format + datas.get(i) + "\n");}
                }
                myOutWriter.close();
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
        }
    public void writelinetolog(String data){

            String path = "/storage/emulated/0/Stryker/";
            File folder = new File(path);
            folder.mkdirs();
            File file = new File(folder, "ips.txt");
            try {
                boolean newf = file.createNewFile();

                FileOutputStream fOut = new FileOutputStream(file, true);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                if (newf){
                    myOutWriter.append("Model:"+android.os.Build.MANUFACTURER+ android.os.Build.MODEL+ "\n");
                    if(Build.SUPPORTED_64_BIT_ABIS.length>0)
                    {
                        myOutWriter.append("Android:"+" "+Build.VERSION.SDK_INT+" (arm64)\n");
                    }
                    else
                    {
                        myOutWriter.append("Android:"+" "+Build.VERSION.SDK_INT+" (arm32)\n");
                    }

                }

                myOutWriter.append(data + "\n");

                myOutWriter.close();
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }

    }
    public void saveresult(ArrayList<Router> rs){
        String path = "/storage/emulated/0/Stryker/";
        File folder = new File(path);
        folder.mkdirs();
        File file = new File(folder, "routerscan.txt");
        boolean newf = false;
        try {
            newf = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
        if (newf){
            try {
                myOutWriter.append("\"IP Address\";\"Port\";\"Time (ms)\";\"Status\";\"Authorization\";\"Server name / Realm name / Device type\";\"Radio Off\";\"Hidden\";\"BSSID\";\"ESSID\";\"Security\";\"Key\";\"WPS PIN\";\"LAN IP Address\";\"LAN Subnet Mask\";\"WAN IP Address\";\"WAN Subnet Mask\";\"WAN Gateway\";\"Domain Name Servers\";\"Latitude\";\"Longitude\";\"Comments\""+ "\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for(Router r: rs){
        if (r.getSuccess()){
            try {
                StringBuilder sb = new StringBuilder();
                sb.append('"'+r.getIp()+ '"'+';');
                sb.append('"'+"80"+ '"'+';');
                sb.append('"'+"100"+ '"'+';');
                sb.append('"'+"Done"+ '"'+';');
                sb.append('"'+r.getAuth()+ '"'+';');
                sb.append('"'+r.getTitle()+ '"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+r.getBssid()+ '"'+';');
                sb.append('"'+r.getSsid()+ '"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+r.getPsk()+ '"'+';');
                sb.append('"'+r.getWps()+ '"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+" "+'"'+';');
                sb.append('"'+"Stryker!"+'"'+';');
                myOutWriter.append(sb.toString()+"\r\n");

            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }}}
        try {
            myOutWriter.close();
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public void vibrate(int mil) {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) context.getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(mil, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) context.getSystemService(VIBRATOR_SERVICE)).vibrate(mil);
        }
    }
    public void connectToWifi(String ssid, String password)
    {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = "\"" + ssid + "\"";
                wifiConfig.preSharedKey = "\"" + password + "\"";
                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();

            } catch ( Exception e) {
                e.printStackTrace();
            }
        } else {
            WifiNetworkSuggestion suggestion1 =
                    new WifiNetworkSuggestion.Builder()
                            .setSsid(ssid)
                            .setWpa2Passphrase(password)
                            .build();
            final List<WifiNetworkSuggestion> suggestionsList = new ArrayList<>();
            suggestionsList.add(suggestion1);
            wifiManager.addNetworkSuggestions(suggestionsList);
        }
    }
}
