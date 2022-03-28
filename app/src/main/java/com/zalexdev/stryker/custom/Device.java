package com.zalexdev.stryker.custom;

import com.zalexdev.stryker.R;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A Device object is a single device that is scanned. It contains the IP address, MAC address, vendor,
 * OS, and subname. It also contains a list of ports and services
 */
public class Device {
    public String ip = "Network error...";
    public String mac = "Scanning...";
    public String vendor = "";
    public int image = 0;
    public String os = "Unknown";
    public String subname;
    public ArrayList<String> ports = new ArrayList<>();
    public ArrayList<String> services = new ArrayList<>();
    public ArrayList<String> versions = new ArrayList<>();
    public boolean shim = true;
    boolean iscutted = false;

    public Device() {
    }

    public void addPort(String port) {
        ports.add(port);
    }
    public void guessos(){
        ArrayList<String> ports = getPorts();
        if (ports.contains("21") || ports.contains("22") || ports.contains("23")) {
            setOs("Linux");
            setImage(R.drawable.linux);
        }
        if (ports.contains("554") || ports.contains("37777")) {
            setOs("Secure Camera");
            setImage(R.drawable.camera);
        }
        if (ports.contains("9100")) {
            setOs("Printer");
            setImage(R.drawable.printer);
        }
        if (ports.contains("5555")) {
            setOs("Android");
            setImage(R.drawable.iphone);
        }if (ports.contains("2336") || ports.contains("3004") || ports.contains("3031")) {
            setOs("IOS/MACOS");
            setImage(R.drawable.aplle);
        }
        if (ports.contains("3389") || ports.contains("135") || ports.contains("136") || ports.contains("137") || ports.contains("138") || ports.contains("139") || ports.contains("5357") || ports.contains("445") || ports.contains("903")) {
            setOs("Windows");
            setImage(R.drawable.windows);
        }
        if (ports.contains("1900")){
            setOs("Linux");
            setImage(R.drawable.router);
        }
        
    }
    public void addService(String serv) {
        services.add(serv);
    }

    public ArrayList<String> getServices() {
        return services;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public ArrayList<String> getPorts() {
        return ports;
    }

    public ArrayList<String> getVersions() {
        return versions;
    }

    public void setVersions(ArrayList<String> versions) {
        this.versions = versions;
    }

    public int getImage() {
        if (image == 0) {
            image = R.drawable.devices_local;
        }
        if (os.contains("Windows")){
            image = R.drawable.windows;
        }
        if (os.contains("Linux")){
            image = R.drawable.linux;
        }
        if (os.contains("Android")){
            image = R.drawable.smartphone;
        }
        if (os.contains("IOS")||os.contains("MacOS")||os.contains("Apple")){
            image = R.drawable.apple;
        }
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
        String ven = vendor.toLowerCase(Locale.ROOT);
        if (ven.contains("apple")) {
            setOs("MacOS/IOS");
            setImage(R.drawable.aplle);
        } else if (ven.contains("microsoft")) {
            setOs("Windows");
            setImage(R.drawable.windows);
        } else if (ven.contains("hikvision") || ven.contains("dahua")) {
            setOs("Secure Camera");
            setImage(R.drawable.camera);
        }
    }

    public boolean isShim() {
        return shim;
    }

    public void setShim(boolean shim) {
        this.shim = shim;
    }

    public String getSubname() {
        return subname;
    }

    public void setSubname(String subname) {
        this.subname = subname;
    }

    public boolean isIscutted() {
        return iscutted;
    }

    public void setIscutted(boolean iscutted) {
        this.iscutted = iscutted;
    }
}
