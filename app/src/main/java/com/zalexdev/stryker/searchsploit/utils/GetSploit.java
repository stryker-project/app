package com.zalexdev.stryker.searchsploit.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.zalexdev.stryker.custom.Sploit;
import com.zalexdev.stryker.utils.Core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class GetSploit extends AsyncTask<Void, String, ArrayList<Sploit>> {
    public String exec = Core.EXECUTE;
    public String query;
    public Core core;

    public GetSploit(String q, Core c) {
        core = c;
        query = q;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("WrongThread")
    @Override
    protected ArrayList<Sploit> doInBackground(Void... command) {
        String line;
        StringBuilder json = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'/modules/Searchsploit/exploitdb/searchsploit " + query + "  --json'" + '\n').getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out2 = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out2.add(line);
                json.append(line).append("\n");
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

        return parse(json.toString());
    }

    @Override
    protected void onPostExecute(ArrayList<Sploit> result) {
        super.onPostExecute(result);

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }

    public ArrayList<Sploit> parse(String out) {
        ArrayList<Sploit> res = new ArrayList<>();
        try {
            JSONObject all = new JSONObject(out);
            JSONArray exploits = all.getJSONArray("RESULTS_EXPLOIT");
            for (int i = 0; i < exploits.length(); i++) {
                JSONObject exploit = exploits.getJSONObject(i);
                Sploit temp = new Sploit();
                temp.setTitle(exploit.getString("Title"));
                temp.setDate(exploit.getString("Date"));
                temp.setAuthor(exploit.getString("Author"));
                temp.setType(exploit.getString("Type"));
                temp.setPlatform(exploit.getString("Platform"));
                temp.setPath(exploit.getString("Path"));
                res.add(temp);
            }
            return res;
        } catch (JSONException e) {
            e.printStackTrace();
            return res;
        }
    }


}
