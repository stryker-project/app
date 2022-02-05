package com.zalexdev.stryker.utils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class CheckMsg extends AsyncTask<Void, String, String> {
    public CheckMsg() {
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected String doInBackground(Void... command) {
        try {
            Document doc = Jsoup.connect("https://raw.githubusercontent.com/stryker-project/updater/main/msg.txt").get();
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
