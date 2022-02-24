package com.zalexdev.stryker.three_wifi.utils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.zalexdev.stryker.custom.WiFINetwork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class GetWiFI extends AsyncTask<Void, Void, ArrayList<WiFINetwork>> {
    public String key;
    public String bssid;


    public GetWiFI(String k, String b) {
        key = k;
        bssid = b;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected ArrayList<WiFINetwork> doInBackground(Void... command) {
        ArrayList<WiFINetwork> results = new ArrayList<>();
        Log.e("3wifi", key + bssid);
        try {
            String postUrl = "https://3wifi.stascorp.com/api/apiquery";
            String response = Jsoup.connect(postUrl).timeout(6000).ignoreContentType(true)
                    .method(Connection.Method.POST)
                    .data("key", key)
                    .data("bssid", bssid)
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .userAgent("Mozilla/5.0 (Windows 98; Win 9x 4.90) AppleWebKit/5322 (KHTML, like Gecko) Chrome/40.0.802.0 Mobile Safari/5322")
                    .execute()
                    .body();

            JSONObject answer = new JSONObject(response);
            boolean ok = answer.getBoolean("result");
            if (ok) {
                JSONObject responsee = answer.getJSONObject("data");
                JSONArray keys = responsee.getJSONArray(bssid.toUpperCase(Locale.ROOT));
                for (int i = 0; i < keys.length(); i++) {
                    JSONObject wifi = keys.getJSONObject(i);
                    WiFINetwork temp = new WiFINetwork();
                        if (wifi.has("time")){temp.setDate(wifi.getString("time"));}
                        if (wifi.has("bssid")){temp.setMac(wifi.getString("bssid"));}
                        if (wifi.has("essid")){temp.setSsid(wifi.getString("essid"));}
                        if (wifi.has("key")){temp.setPsk(wifi.getString("key"));}
                        if (wifi.has("wps")){temp.setPin(wifi.getString("wps"));}
                        if (wifi.has("lat") && wifi.has("lon")){ temp.setLon(wifi.getString("lat"));
                        temp.setLun(wifi.getString("lon"));}
                        temp.setOK(true);
                        results.add(temp);

                }
            }
        } catch (IOException ignored) {
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
    }

    @Override
    protected void onPostExecute(ArrayList<WiFINetwork> result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

    }


}
