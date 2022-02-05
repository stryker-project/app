package com.zalexdev.stryker.handshakes.utils;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.zalexdev.stryker.MainActivity;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.WiFiNetwork;
import com.zalexdev.stryker.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BruteHandshake extends AsyncTask<Void, String, WiFiNetwork> {
    public String exec = Core.EXECUTE;
    public String path;
    public String wordlist;
    public Core core;
    public Activity activity;
    public TextView progress;
    public TextView time;
    public Context context;
    public int id;
    public Process process;

    public BruteHandshake(String p, String w, Core c, Activity a, Context con, TextView pr, TextView t, int i) {
        core = c;
        path = p;
        wordlist = w;
        activity = a;
        progress = pr;
        time = t;
        context = con;
        id = i;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @SuppressLint("WrongThread")
    @Override
    protected WiFiNetwork doInBackground(Void... command) {
        String line;
        WiFiNetwork result = new WiFiNetwork();
        try {
            process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((exec + "'aircrack-ng -w " + wordlist + " " + path + " '" + '\n').getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out2 = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                out2.add(line);
                onProgressUpdate(line);
                if (line.contains("\u001B[11B\u001B[8;28H\u001B[2KKEY FOUND! [ ")) {
                    result.setPsk(line.replace("\u001B[11B\u001B[8;28H\u001B[2KKEY FOUND! [ ", "").replace(" ]", "").replaceAll("\\s+", ""));
                    result.setOK(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CreateNotification("Success", "Password found: " + result.getPsk(), 100, 100);
                    }
                }
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
        if (!result.getOK()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CreateNotification("Failed", "Password Not Found", 100, 100);
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(WiFiNetwork result) {
        super.onPostExecute(result);


    }

    public void kill() {
        if (process != null) {
            process.destroy();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        activity.runOnUiThread(() -> {

            String rem = "";
            Matcher matcher = Pattern.compile("\\d+/\\d+").matcher(values[0]);
            Matcher matcher2 = Pattern.compile("\\d+ hours").matcher(values[0]);
            Matcher matcher3 = Pattern.compile("\\d+ minutes").matcher(values[0]);
            Matcher matcher4 = Pattern.compile("\\d+ seconds").matcher(values[0]);
            if (matcher2.find()) {
                rem = rem + matcher2.group(0) + " ";
            }
            if (matcher3.find()) {
                rem = rem + matcher3.group(0) + " ";
            }
            if (matcher4.find()) {
                rem = rem + matcher4.group(0) + " ";
            }
            int pr = 0;
            int all = 0;
            if (matcher.find()) {
                pr = Integer.parseInt(matcher.group(0).split("/")[0]);
                all = Integer.parseInt(matcher.group(0).split("/")[1]);
                progress.setText("Progress: " + matcher.group(0) + " k/s");
            }
            if (rem.length() != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CreateNotification(matcher.group(0), rem, pr, all);
                }
                time.setText("Time remaining: " + rem);
            }

        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void CreateNotification(String key, String left, int prog, int max) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String CHANNEL_ID = "BruteForce";
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "BruteForce", NotificationManager.IMPORTANCE_LOW);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.iconnotif)
                .setTicker("Brute")
                .setContentTitle(left)
                .setContentText(key)
                .setChannelId(CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setProgress(max, prog, false)
                .setContentInfo("Info");


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(id, b.build());
    }


}
