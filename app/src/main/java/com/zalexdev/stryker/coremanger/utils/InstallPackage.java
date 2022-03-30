package com.zalexdev.stryker.coremanger.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.zalexdev.stryker.custom.Package;
import com.zalexdev.stryker.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to install a package
 */
public class InstallPackage extends AsyncTask<Void, String, Boolean> {
    public String exec = Core.EXECUTE;
    public String pkgname;
    public Core core;

    public InstallPackage(String p, Core c) {
        core = c;
        pkgname = p;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("WrongThread")
    @Override
    protected Boolean doInBackground(Void... command) {
        String line;
        ArrayList<String> out2 = new ArrayList<>();
        boolean p = false;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'apk add " + pkgname + "'" + '\n').getBytes());
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
            p = process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        }

        return p;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

    }




}
