package com.zalexdev.stryker.coremanger.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.zalexdev.stryker.custom.Package;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetPackage extends AsyncTask<Void, String, ArrayList<Package>> {
    public String exec = Core.EXECUTE;
    public String query;
    public Core core;

    public GetPackage(String q, Core c) {
        core = c;
        query = q;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("WrongThread")
    @Override
    protected ArrayList<Package> doInBackground(Void... command) {
        String line;
        ArrayList<String> out2 = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'apk search " + query + "'" + '\n').getBytes());
            stdin.flush();
            stdin.close();

            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out2.add(line);
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

        return parse(out2);
    }

    @Override
    protected void onPostExecute(ArrayList<Package> result) {
        super.onPostExecute(result);

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }

    public ArrayList<Package> parse(ArrayList<String> out) {
        ArrayList<Package> res = new ArrayList<>();
        for (String pkg : out){
            Package temp = new Package();
            Matcher r = Pattern.compile("-r[0-9]+").matcher(pkg);
            if (r.find()){
                pkg = pkg.replace(r.group(),"");
            }
            String version = pkg.split("-")[pkg.split("-").length-1];
            temp.setVersion(version);
            temp.setName(pkg.replace("-"+version,""));
            res.add(temp);
        }
        return res;
    }


}
