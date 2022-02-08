package com.zalexdev.stryker;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import com.zalexdev.stryker.appintro.AppIntroActivity;
import com.zalexdev.stryker.coremanger.CoreManager;
import com.zalexdev.stryker.exploit_hub.ExploitScreen;
import com.zalexdev.stryker.geomac.GeoMac;
import com.zalexdev.stryker.handshakes.HandshakeStorage;
import com.zalexdev.stryker.local_network.LocalMain;
import com.zalexdev.stryker.metasploit.MsfConsole;
import com.zalexdev.stryker.modules.ModulesFragment;
import com.zalexdev.stryker.nmap.NmapScanner;

import com.zalexdev.stryker.router_scan.RouterScanMain;
import com.zalexdev.stryker.searchsploit.SearchSploit;
import com.zalexdev.stryker.three_wifi.LoginPage;
import com.zalexdev.stryker.utils.CheckDir;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;
import com.zalexdev.stryker.wifi.Wifi;
import com.zalexdev.stryker.wifi.utils.GetInterfaces;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    public Core core;
    public int versionInt = BuildConfig.VERSION_CODE;
    private DrawerLayout mDrawer;
    public Toolbar toolbar;
    public boolean usbstate = false;
    private ActionBarDrawerToggle drawerToggle;
    public NavigationView nvDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        nvDrawer = findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer);
        core = new Core(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mDrawer = findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();
        mDrawer.addDrawerListener(drawerToggle);
        mDrawer = findViewById(R.id.drawer_layout);

        Core core = new Core(this);
        int night = core.getInt("night");

        core.checkroot();
        checkforusb();
        try {
            if (new CheckDir("/data/local/stryker/beta/usr").execute().get()){
            Intent update = new Intent(this, AppIntroActivity.class);
            update.putExtra("update",true);
            startActivity(update);
        }
            else if (!new CheckDir("/data/local/stryker/release/usr").execute().get()){
                Intent install = new Intent(this, AppIntroActivity.class);
                install.putExtra("update",false);
                startActivity(install);
            }
            else{
                core.mountcore();FragmentManager fragmentManager = getSupportFragmentManager();
                if (!new CheckDir("/data/local/stryker/release/sdcard/Stryker").execute().get()){
                    fragmentManager.beginTransaction().replace(R.id.flContent, new Error()).commit();
                }else{
                fragmentManager.beginTransaction().replace(R.id.flContent, new Dashboard(nvDrawer)).commit();
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        copyAssets();
        checkforusb();
        core.putString("chroot_path", "/data/local/stryker/release/");
        new CustomCommand("chmod 777 -R /data/data/com.zalexdev.stryker/", core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if (!core.checkmod("Searchsploit")){nvDrawer.getMenu().getItem(8).setVisible(false); }
        if (!core.is64Bit() ||!core.checkmod("GeoMac")){nvDrawer.getMenu().getItem(9).setVisible(false); }
        if (!core.is64Bit() || !core.checkmod("Router Scan")){nvDrawer.getMenu().getItem(10).setVisible(false);}
        if (!core.checkmod("Router Scan")){nvDrawer.getMenu().getItem(11).setVisible(false);}
        if (!core.getBoolean("first_open")) {
            core.putString("wlan_scan", "wlan0");
            core.putString("wlan_deauth", "wlan0");
            core.putBoolean("first_open", true);
            core.putBoolean("store_scan", true);
            core.putBoolean("auto_update", true);
            core.putInt("threads", 100);
            core.putInt("night", 2);
           new CustomCommand("dumpsys deviceidle whitelist +com.zalexdev.stryker", core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (night==0){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else if (night==1){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }


    }

    public void usbdialog(){
        final BottomSheetDialog usbdialog = new BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme);
        usbdialog.setContentView(R.layout.usb_dialog);
        TextView info = usbdialog.findViewById(R.id.usb_info);
        Button changelisten = usbdialog.findViewById(R.id.change_listen);
        Button changedeauth = usbdialog.findViewById(R.id.change_deauth);
        info.setText("["+getpid()+"] "+core.getDeviceNameByPid(getpid()));
        assert changelisten != null;
        changelisten.setOnClickListener(view -> getWlanMonitore(true));
        assert changedeauth != null;
        changedeauth.setOnClickListener(view -> getWlanMonitore(false));
        usbdialog.show();
    }
    public void getWlanMonitore(boolean isscan) {
        ArrayList<String> w = null;
        try {
            w = getinterfaces();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        assert w != null;
        String[] w2 = new String[w.size()+1];
        for (int i = 0; i < w.size(); i++) {
            w2[i] = w.get(i);
        }
        w2[w2.length-1] = core.str("customvalue");
        new MaterialAlertDialogBuilder(this)
                .setTitle("Pick interface")
                .setItems(w2, (dialogInterface, i) -> {
                    if (i !=w2.length -1){
                        if (isscan) {
                            core.putString("wlan_scan", w2[i]);
                        } else {
                            core.putString("wlan_deauth", w2[i]);
                        }}else{
                        new Thread(() -> {
                            final String[] temp = {""};
                            runOnUiThread(() -> {
                                final Dialog valuedialog = new Dialog(this);
                                valuedialog.setContentView(R.layout.input_dialog);
                                valuedialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                TextView title = valuedialog.findViewById(R.id.input_title);
                                TextInputEditText valueedit = valuedialog.findViewById(R.id.getvalue);
                                TextView ok = valuedialog.findViewById(R.id.ok_button);
                                title.setText(core.str("customvalue"));
                                ok.setOnClickListener(view1 -> {
                                    temp[0] = Objects.requireNonNull(valueedit.getText()).toString();
                                    valuedialog.dismiss();
                                });
                                valuedialog.show();
                            });
                            while (temp[0].equals("")){
                                Log.d("t","test");
                            }
                            if (isscan) {
                                core.putString("wlan_scan", temp[0]);
                            } else {
                                core.putString("wlan_deauth", temp[0]);
                            }
                        }).start();

                    }
                })
                .show();
    }
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    selectDrawerItem(menuItem);
                    return true;
                });
    }
    public void noroot() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.error)
                .setMessage(core.str("noroot"))
                .setCancelable(false)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    System.exit(0);
                })
                .show();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);

    }
    public boolean isConnected() {
        return getpid() != null;
    }

    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        ImageView disablemon = toolbar.findViewById(R.id.disablemobn);
        disablemon.setVisibility(View.GONE);
        if (menuItem.getItemId() == R.id.wifi) {
            fragment = Wifi.newInstance();
            disablemon.setVisibility(View.VISIBLE);
        } else if (menuItem.getItemId() == R.id.settings) {
            fragment = new Settings();
        } else if (menuItem.getItemId() == R.id.local) {
            fragment = new LocalMain();
      //  } else if (menuItem.getItemId() == R.id.about) {
         //   fragment = new About();
        } else if (menuItem.getItemId() == R.id.core_manager) {
            fragment = new CoreManager();
        }  else if (menuItem.getItemId() == R.id.hs_storage) {
            fragment = new HandshakeStorage();
        } else if (menuItem.getItemId() == R.id.searchsploit) {
            fragment = new SearchSploit();
        } else if (menuItem.getItemId() == R.id.exploit_hub) {
            fragment = new ExploitScreen();
        } else if (menuItem.getItemId() == R.id.msf) {
            fragment = new MsfConsole();
        }
        else if (menuItem.getItemId() == R.id.modules_repo) {
            fragment = new ModulesFragment();
        } else if (menuItem.getItemId() == R.id.three_wifi) {
            fragment = new LoginPage();
        } else if (menuItem.getItemId() == R.id.nmap_scan) {
            fragment = new NmapScanner();
        } else if (menuItem.getItemId() == R.id.geomac) {
            fragment = new GeoMac();
        } else if (menuItem.getItemId() == R.id.router) {
            fragment = new RouterScanMain();
        } else if (menuItem.getItemId() == R.id.dashboard_menu) {
            fragment = new Dashboard(nvDrawer);
        }
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
                File outFile = new File("/data/data/com.zalexdev.stryker/files/", filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            } finally {
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
        new CustomCommand("chmod 777 -R /data/data/com.zalexdev.stryker/", new Core(this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public void toaster(String msg) {
        Toast toast = Toast.makeText(this,
                msg, Toast.LENGTH_SHORT);
        toast.show();
    }



    private ArrayList<String> getinterfaces() throws ExecutionException, InterruptedException {
        GetInterfaces airmon = new GetInterfaces(new Core(this));
        return airmon.execute().get();
    }


    public boolean checkpermission() {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{WRITE_EXTERNAL_STORAGE},
                        123
                );
            }
        return checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    public void checkforusb(){
        Timer usb = new Timer();
        usb.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                boolean temp = usbstate;
                usbstate = isConnected();
                if (temp != usbstate && usbstate){
                    runOnUiThread(() -> usbdialog());
                }
            }
        },0,3000);
    }
    public String getpid(){
        String deviceid = null;
        UsbManager manager = (UsbManager) MainActivity.this.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        for (String deviceName : devices.keySet()) {
            UsbDevice device = devices.get(deviceName);
            assert device != null;
            StringBuilder string2 = new StringBuilder(Integer.toHexString(device.getVendorId()));
            while (string2.length() < 4) {
                string2.insert(0, "0");
            }
            StringBuilder string3 = new StringBuilder(Integer.toHexString(device.getProductId()));
            while (string3.length() < 4) {
                string3.insert(0, "0");
            }
            deviceid = string2 + ":" + string3;
        }
        return deviceid;
    }

    @Override
    public void onBackPressed() {
        mDrawer.openDrawer(GravityCompat.START);
    }

}