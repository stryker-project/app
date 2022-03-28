package com.zalexdev.stryker.utils;

import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Logger {
    // These are the tags that are used to identify the type of the log.
    public final static String INPUT = "[I] ";
    public final static String OUTPUT = "[O] ";
    public final static String ERROR = "[E] ";


    public String path = "/storage/emulated/0/Stryker/";
    public File logfile;
    public FileOutputStream fOut;
    public OutputStreamWriter editor;
    public Logger(){
        File folder = new File(path);
        folder.mkdirs();
        logfile = new File(folder, "log.txt");
        try {
            boolean newf = logfile.createNewFile();
            // It checks if the file is new or not. If it is new, it will create a new file and write
            // the header.
             fOut = new FileOutputStream(logfile, true);
            editor = new OutputStreamWriter(fOut);
            if (newf) {
                addDelimetr();
                editor.append("Model:" + android.os.Build.MANUFACTURER + android.os.Build.MODEL + "\n");
                if (Build.SUPPORTED_64_BIT_ABIS.length > 0) {
                    editor.append("Android:" + " " + Build.VERSION.SDK_INT + " (arm64)\n");
                } else {
                    editor.append("Android:" + " " + Build.VERSION.SDK_INT + " (arm32)\n");
                }
                editor.append("AppCodeVersion: 2.1B");
                addDelimetr();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Write a line to the file
     *
     * @param line The line to be written to the file.
     */
    public void write(String line) {
        if (editor !=null){
        try{
            editor.append(line + "\n");
            fOut.flush();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }}}

    /**
     * It writes the contents of the arraylist to the file.
     *
     * @param output The ArrayList of Strings that you want to write to the file.
     */
    public void writeArray(ArrayList<String> output,int type){
        StringBuilder temp = new StringBuilder();
        if (type == 1){
            temp.append(INPUT);
        }else if (type == 2){
            temp.append(OUTPUT);
        }else if (type == 3){
            temp.append(ERROR);
        }
        for (String line : output){
            temp.append(line).append("\n");
        }
        write(temp.toString());
    }

    public void addDelimetr(){
        write("=============================");
    }

    public void writeLine(String line,int type) {
        StringBuilder temp = new StringBuilder();
        if (type == 1){
            temp.append(INPUT);
        }else if (type == 2){
            temp.append(OUTPUT);
        }else if (type == 3){
            temp.append(ERROR);
        }
        write(line);
    }

}
