package com.zalexdev.stryker.three_wifi.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.zalexdev.stryker.custom.Cabinet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class GetApiKeys extends AsyncTask<Void, Void, Cabinet> {
    public String login;
    public String pass;
    public Context context;

    public GetApiKeys(String log, String pas, Context cont) {
        login = log;
        pass = pas;
        context = cont;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected Cabinet doInBackground(Void... command) {
        Cabinet cabinet = new Cabinet(context);
        Log.e("3wifi", login + pass);
        try {
            String postUrl = "https://3wifi.stascorp.com/api/apikeys";
            String response = Jsoup.connect(postUrl).timeout(6000).ignoreContentType(true)
                    .method(Connection.Method.POST)
                    .data("login", login)
                    .data("password", pass)
                    .data("genread", "true")
                    .data("genwrite", "true")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .userAgent("Mozilla/5.0 (Windows 98; Win 9x 4.90) AppleWebKit/5322 (KHTML, like Gecko) Chrome/40.0.802.0 Mobile Safari/5322")
                    .execute()
                    .body();

            JSONObject answer = new JSONObject(response);

            boolean ok = answer.getBoolean("result");
            cabinet.setOk(ok);
            if (ok) {
                JSONArray keys = answer.getJSONArray("data");
                JSONObject key1 = keys.getJSONObject(0);
                JSONObject key2 = keys.getJSONObject(1);
                cabinet.setKeyView(key1.getString("key"));
                cabinet.setKeyWrite(key2.getString("key"));
                Log.e("3wifi", key1.getString("key"));
            }
        } catch (IOException ignored) {
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cabinet;
    }

    @Override
    protected void onPostExecute(Cabinet result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

    }


}
