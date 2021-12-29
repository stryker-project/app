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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewScan extends AsyncTask<Void, String, Boolean> {
    public String exec = Core.EXECUTE;
    public Core core;
    public String ip;
    public NewScan(String ip1) { ip = ip1; }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("WrongThread")
    @Override
    protected Boolean doInBackground(Void... command) {
            return ping(ip);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }

    public boolean ping(String ip){
        try{
            InetAddress address = InetAddress.getByName(ip);
            return address.isReachable(3000);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
