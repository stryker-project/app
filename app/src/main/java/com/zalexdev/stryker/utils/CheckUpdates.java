package com.zalexdev.stryker.utils;

import static android.content.ContentValues.TAG;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * This class checks for updates and returns the result
 */
public class CheckUpdates extends AsyncTask<Void, String, String> {

    public Context context;
    public Core core;
    public CheckUpdates(Context cont) {
        context = cont;
        core = new Core(context);
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected String doInBackground(Void... command) {
        try {
            Document doc = Jsoup.connect("https://raw.githubusercontent.com/stryker-project/updater/main/update.txt").get();
            return doc.text();
        } catch (IOException e) {
            return "Error";
        }


    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }


}
