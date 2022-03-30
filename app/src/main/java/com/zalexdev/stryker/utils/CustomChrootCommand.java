package com.zalexdev.stryker.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class CustomChrootCommand extends AsyncTask<Void, String, Boolean> {

    public String cmd;
    public Core core;

    public CustomChrootCommand(String command, Core c) {
        cmd = command;
        core = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected Boolean doInBackground(Void... command) {
        String line;
        boolean result = false;
        Logger logger = new Logger();
        try {
            logger.writeLine("Running chroot command "+cmd,1);
            Process process = Runtime.getRuntime().exec("su -mm");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((Core.EXECUTE + " '" + cmd + "'" + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out.add(line);
                logger.writeLine(line,2);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                outerror.add(line);
                onProgressUpdate(line);
                logger.writeLine(line,3);
            }
            br.close();
            core.writetolog(out, false);
            core.writetolog(outerror, true);
            process.waitFor();
            process.destroy();
            if (process.exitValue() == 0) {
                result = true;
            }
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }

        return result;
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
