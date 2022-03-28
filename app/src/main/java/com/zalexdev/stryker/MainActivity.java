package com.zalexdev.stryker;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MotionEventCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
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
import com.zalexdev.stryker.utils.GetPersonalID;
import com.zalexdev.stryker.utils.OnSwipeListener;
import com.zalexdev.stryker.wifi.Wifi;
import com.zalexdev.stryker.wifi.utils.GetInterfaces;

import net.cachapa.expandablelayout.ExpandableLayout;

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
    public boolean usbstate = false;
    public int eggcounter = 0;
    public Fragment tempfrag;

    public ExpandableLayout menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        core = new Core(this);
        int night = core.getInt("night");
        ImageView account = findViewById(R.id.account_icon);
        ImageView settings = findViewById(R.id.settings_icon);
         menu = findViewById(R.id.menu_expand);
        account.setOnClickListener(view -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
            if (currentFragment instanceof Settings) {
                menu.collapse();
                settings.setImageDrawable(getDrawable(R.drawable.settings));
            }
            if (currentFragment instanceof Account) {
                account.setImageDrawable(getDrawable(R.drawable.account));
                menu.collapse();
                getSupportFragmentManager().beginTransaction().replace(R.id.flContent, tempfrag).commit();
            }else{

                menu.collapse();
                account.setImageDrawable(getDrawable(R.drawable.close));
                if (!(currentFragment instanceof Settings)) {
                    tempfrag = getSupportFragmentManager().findFragmentById(R.id.flContent);}

                getSupportFragmentManager().beginTransaction().replace(R.id.flContent, new Account()).commit();
            }
        });
        settings.setOnClickListener(view -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
            if (currentFragment instanceof Account) {
                account.setImageDrawable(getDrawable(R.drawable.account));
                menu.collapse();
            }
            if (currentFragment instanceof Settings) {
                settings.setImageDrawable(getDrawable(R.drawable.settings));
                menu.collapse();
                getSupportFragmentManager().beginTransaction().replace(R.id.flContent, tempfrag).commit();
            }else{
                menu.collapse();
                settings.setImageDrawable(getDrawable(R.drawable.close));
                if (!(currentFragment instanceof Account)) {
                tempfrag = getSupportFragmentManager().findFragmentById(R.id.flContent);}
                getSupportFragmentManager().beginTransaction().replace(R.id.flContent, new Settings()).commit();
            }
        });
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
                //
                core.mountcore();FragmentManager fragmentManager = getSupportFragmentManager();
                if (!new CheckDir("/data/local/stryker/release/sdcard/Stryker").execute().get()){
                    fragmentManager.beginTransaction().replace(R.id.flContent, new Error()).commit();

                }else{
                fragmentManager.beginTransaction().replace(R.id.flContent, new Dashboard()).commit();
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        copyAssets();
        checkforusb();
        core.putString("chroot_path", "/data/local/stryker/release/");

        new CustomCommand("chmod 777 -R /data/data/com.zalexdev.stryker/", core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if (night==0 && core.getBoolean("first_open")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else if (night==1){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        TextView logo = findViewById(R.id.stryker_main_logo);

        ImageView menu_toggle = findViewById(R.id.menu_img);
        menu_toggle.setOnClickListener(view -> menu.toggle());
        FragmentManager fragmentManager = getSupportFragmentManager();

        logo.setOnClickListener(view -> {
            eggcounter++;
            if (eggcounter >4){
                logo.setText("Stryker \uD83C\uDDFA\uD83C\uDDE6");
                eggcounter = 0;
            }
        });

        MaterialCardView wifi = findViewById(R.id.menu_wifi);
        MaterialCardView localnetwork = findViewById(R.id.menu_localnetwork);
        MaterialCardView dashboard = findViewById(R.id.menu_dashboard);
        MaterialCardView searchsploit = findViewById(R.id.menu_searchsploit);
        MaterialCardView manager = findViewById(R.id.menu_manager);
        MaterialCardView geo = findViewById(R.id.menu_geomac);
        MaterialCardView metasploit = findViewById(R.id.menu_msf);
        MaterialCardView three = findViewById(R.id.menu_three_wifi);
        MaterialCardView scan = findViewById(R.id.menu_router);
        MaterialCardView repo = findViewById(R.id.menu_repo);
        MaterialCardView site = findViewById(R.id.menu_website);
        MaterialCardView exploits = findViewById(R.id.menu_exloits);
        MaterialCardView nmap = findViewById(R.id.menu_nmap);
        MaterialCardView terminal = findViewById(R.id.menu_terminal);
        wifi.setOnClickListener(view -> {
            settings.setImageDrawable(getDrawable(R.drawable.settings));
            account.setImageDrawable(getDrawable(R.drawable.account));
            fragmentManager.beginTransaction().replace(R.id.flContent, new Wifi()).commit();
            menu.collapse();
        });
        dashboard.setOnClickListener(view -> {
            settings.setImageDrawable(getDrawable(R.drawable.settings));
            account.setImageDrawable(getDrawable(R.drawable.account));
            fragmentManager.beginTransaction().replace(R.id.flContent, new Dashboard()).commit();
            menu.collapse();
        });
        localnetwork.setOnClickListener(view -> {
            settings.setImageDrawable(getDrawable(R.drawable.settings));
            account.setImageDrawable(getDrawable(R.drawable.account));
            fragmentManager.beginTransaction().replace(R.id.flContent, new LocalMain()).commit();
            menu.collapse();
        });
        repo.setOnClickListener(view -> {
            settings.setImageDrawable(getDrawable(R.drawable.settings));
            account.setImageDrawable(getDrawable(R.drawable.account));
            fragmentManager.beginTransaction().replace(R.id.flContent, new ModulesFragment()).commit();
            menu.collapse();
        });
        manager.setOnClickListener(view -> {
            settings.setImageDrawable(getDrawable(R.drawable.settings));
            account.setImageDrawable(getDrawable(R.drawable.account));
            fragmentManager.beginTransaction().replace(R.id.flContent, new CoreManager()).commit();
            menu.collapse();
        });
        if (core.checkmod("Searchsploit")){
            searchsploit.setOnClickListener(view -> {
                settings.setImageDrawable(getDrawable(R.drawable.settings));
                account.setImageDrawable(getDrawable(R.drawable.account));
                fragmentManager.beginTransaction().replace(R.id.flContent, new SearchSploit()).commit();
                menu.collapse();
            });
        }else{
            searchsploit.setOnClickListener(view -> {
                settings.setImageDrawable(getDrawable(R.drawable.settings));
                account.setImageDrawable(getDrawable(R.drawable.account));
                fragmentManager.beginTransaction().replace(R.id.flContent, new PlsInstallModule("Searchsploit")).commit();
                menu.collapse();
            });
        }
        if (core.checkmod("GeoMac")){
            geo.setOnClickListener(view -> {
                settings.setImageDrawable(getDrawable(R.drawable.settings));
                account.setImageDrawable(getDrawable(R.drawable.account));
                fragmentManager.beginTransaction().replace(R.id.flContent, new GeoMac()).commit();
                menu.collapse();
            });
        }else {
            geo.setOnClickListener(view -> {
                settings.setImageDrawable(getDrawable(R.drawable.settings));
                account.setImageDrawable(getDrawable(R.drawable.account));
                fragmentManager.beginTransaction().replace(R.id.flContent, new PlsInstallModule("GeoMac")).commit();
                menu.collapse();
            });
        }
        if (core.checkmod("Router Scan")){
            three.setOnClickListener(view -> {
                settings.setImageDrawable(getDrawable(R.drawable.settings));
                account.setImageDrawable(getDrawable(R.drawable.account));
                fragmentManager.beginTransaction().replace(R.id.flContent, new LoginPage()).commit();
                menu.collapse();
            });
            scan.setOnClickListener(view -> {
                settings.setImageDrawable(getDrawable(R.drawable.settings));
                account.setImageDrawable(getDrawable(R.drawable.account));
                fragmentManager.beginTransaction().replace(R.id.flContent, new RouterScanMain()).commit();
                menu.collapse();
            });
        }else{
            three.setOnClickListener(view -> {
                settings.setImageDrawable(getDrawable(R.drawable.settings));
                account.setImageDrawable(getDrawable(R.drawable.account));
                fragmentManager.beginTransaction().replace(R.id.flContent, new PlsInstallModule("Router Scan")).commit();
                menu.collapse();
            });
            scan.setOnClickListener(view -> {
                settings.setImageDrawable(getDrawable(R.drawable.settings));
                account.setImageDrawable(getDrawable(R.drawable.account));
                fragmentManager.beginTransaction().replace(R.id.flContent, new PlsInstallModule("Router Scan")).commit();
                menu.collapse();
            });
        }
        metasploit.setOnClickListener(view -> {
            settings.setImageDrawable(getDrawable(R.drawable.settings));
            account.setImageDrawable(getDrawable(R.drawable.account));
            fragmentManager.beginTransaction().replace(R.id.flContent, new StillDeveloping()).commit();
            menu.collapse();
        });
        site.setOnClickListener(view -> {
            settings.setImageDrawable(getDrawable(R.drawable.settings));
            account.setImageDrawable(getDrawable(R.drawable.account));
            fragmentManager.beginTransaction().replace(R.id.flContent, new StillDeveloping()).commit();
            menu.collapse();
        });
        nmap.setOnClickListener(view -> {
            settings.setImageDrawable(getDrawable(R.drawable.settings));
            account.setImageDrawable(getDrawable(R.drawable.account));
            fragmentManager.beginTransaction().replace(R.id.flContent, new NmapScanner()).commit();
            menu.collapse();
        });
        exploits.setOnClickListener(view -> {
            settings.setImageDrawable(getDrawable(R.drawable.settings));
            account.setImageDrawable(getDrawable(R.drawable.account));
            fragmentManager.beginTransaction().replace(R.id.flContent, new ExploitScreen()).commit();
            menu.collapse();
        });
        terminal.setOnClickListener(view -> {
            settings.setImageDrawable(getDrawable(R.drawable.settings));
            account.setImageDrawable(getDrawable(R.drawable.account));
            fragmentManager.beginTransaction().replace(R.id.flContent, new StillDeveloping()).commit();
            menu.collapse();
        });


    }



    /**
     * This function is used to show the dialog box when the user clicks on the USB button.
     */
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
    /**
     * It creates a dialog box that allows the user to pick a network interface
     *
     * @param isscan boolean, if true, the user is picking a wlan interface to scan with, if false, the
     * user is picking a wlan interface to deauth with
     */
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

    /**
     * If the user is not rooted, show an error message and exit the app
     */
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


    /**
     * Check if the process is connected to the network.
     *
     * @return A boolean value.
     */
    public boolean isConnected() {
        return getpid() != null;
    }



    /**
     * It copies all the files from the assets folder to the /data/data/com.zalexdev.stryker/files/
     * folder
     */
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


    // Copying a file from one location to another.
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Prints a message on the screen
     *
     * @param msg The message to display in the toast.
     */
    public void toaster(String msg) {
        Toast toast = Toast.makeText(this,
                msg, Toast.LENGTH_SHORT);
        toast.show();
    }




    /**
     * This function returns a list of all the interfaces that are currently up and running
     *
     * @return An ArrayList of Strings.
     */
    private ArrayList<String> getinterfaces() throws ExecutionException, InterruptedException {
        return  core.getInterfacesList();
    }


    /**
     * If the user has not given permission to write to the external storage, then request permission
     *
     * @return Nothing.
     */
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


    /**
     * This function checks if the USB is connected to the device every 3 seconds
     */
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
    /**
     * Get the device id of the connected device
     *
     * @return The device id of the connected device.
     */
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
        if (menu.isExpanded()){
        super.onBackPressed();}else{
            menu.expand();
        }
    }
}