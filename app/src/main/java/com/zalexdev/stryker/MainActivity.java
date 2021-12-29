package com.zalexdev.stryker;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.zalexdev.stryker.local.LocalMain;
import com.zalexdev.stryker.router.RouterMain;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;
import com.zalexdev.stryker.wifi.Wifi;
import com.zalexdev.stryker.wifi.utils.DisableMonitor;
import com.zalexdev.stryker.wifi.utils.GetInterfaces;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    public Core core;
    public  String versionName = BuildConfig.VERSION_NAME;
    public  int versionInt = BuildConfig.VERSION_CODE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nvView);
        ImageView disablemon = toolbar.findViewById(R.id.disablemobn);
        disablemon.setOnClickListener(view -> disable());

        setupDrawerContent(nvDrawer);
        core = new Core(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawer =  findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();
        mDrawer.addDrawerListener(drawerToggle);
        mDrawer = findViewById(R.id.drawer_layout);
        copyAssets();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, new Setup()).commit();
        Core core = new Core(this);
        if (!core.getBoolean("first_open")){
        core.putString("chroot_path","/data/local/stryker/beta/");
        core.putString("wlan_scan","wlan0");
        core.putString("wlan_deauth","wlan0");
        core.putBoolean("first_open",true);
        core.putBoolean("auto_update",true);
        core.putInt("threads",100);
        core.putInt("timeout",5000);
        new CustomCommand("su",new Core(this)).execute();
        new CustomCommand("chmod 777 -R /data/data/com.zalexdev.stryker/",new Core(this)).execute();
        new CustomCommand("pm grant com.zalexdev.stryker android.permission.WRITE_EXTERNAL_STORAGE",new Core(this)).execute();
        new CustomCommand("pm grant com.zalexdev.stryker android.permission.MANAGE_EXTERNAL_STORAGE",new Core(this)).execute();
        new CustomCommand("pm grant com.zalexdev.stryker android.permission.READ_EXTERNAL_STORAGE",new Core(this)).execute();
        checkpermission();
        disklaimer();

        }






    }
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    selectDrawerItem(menuItem);
                    return true;
                });
    }
    private ActionBarDrawerToggle setupDrawerToggle() {
      return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }


    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        if (menuItem.getItemId() == R.id.wifi){fragment = Wifi.newInstance();}
        else if (menuItem.getItemId() == R.id.settings){fragment = new Settings();}
        else if (menuItem.getItemId() == R.id.local){fragment = new LocalMain();}
        else if (menuItem.getItemId() == R.id.about){fragment = new About();}
        else if (menuItem.getItemId() == R.id.router){fragment = new RouterMain();}
        else if (menuItem.getItemId() == R.id.dashboard_menu){fragment = new Dashboard();}
        FragmentManager fragmentManager = getSupportFragmentManager();
        assert fragment != null;
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        menuItem.setChecked(true);
        mDrawer.closeDrawers();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File("/data/data/com.zalexdev.stryker/cache/", filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
    public void toaster(String msg){
        Toast toast = Toast.makeText(this,
                msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void disable() {
        ArrayList<String> w = null;
        try {
            w = getinterfaces();

        String[] w2 = new String[w.size()];
        for (int i = 0;i<w.size();i++){
            w2[i] = w.get(i);
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Disable monitore mode")
                .setItems(w2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Thread t = new Thread(() -> new DisableMonitor(w2[i],core).execute());
                        t.start();
                    }
                })
                .show();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    private ArrayList<String> getinterfaces() throws ExecutionException, InterruptedException {
        GetInterfaces airmon = new GetInterfaces(new Core(this));
        return airmon.execute().get();
    }
    public void checkpermission(){
        if(SDK_INT>=Build.VERSION_CODES.M){

            if(checkSelfPermission(WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)){
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{WRITE_EXTERNAL_STORAGE},
                            123
                    );

                }else {
                    // Request permission
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{WRITE_EXTERNAL_STORAGE},
                            123
                    );
                }
            }
        }

        if (SDK_INT >= Build.VERSION_CODES.R) {

            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        }



        else{
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 123);
        }

    }
    public void disklaimer(){
        new MaterialAlertDialogBuilder(this)
                .setTitle("Denial of responsibility")
                .setMessage("Neither the developer nor other community members are responsible for your actions or your device and data. You agree to test only your own devices, or those for which you have explicit permission from the owner. By clicking \"Yes\" you agree with the above statements")
                .setPositiveButton("Yes", (dialogInterface, i) -> {

                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    System.exit(0);
                    dialogInterface.dismiss();
                }).show();

    }
}