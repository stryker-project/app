package com.zalexdev.stryker.custom;

import com.zalexdev.stryker.R;

import java.util.ArrayList;
import java.util.Locale;

public class Device {
    public String ip;
    public String mac;
    public String vendor;
    public int image = 0;
    public String os = "Unknown";
    public ArrayList<String> ports = new ArrayList<>();
    public ArrayList<String> services = new ArrayList<>();
    public ArrayList<String> versions = new ArrayList<>();
    public Device(){}

    public void setImage(int image) {
        this.image = image;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setOs(String os) {
        this.os = os;
    }



    public void setVendor(String vendor) {
        this.vendor = vendor;
        String ven = vendor.toLowerCase(Locale.ROOT);
        if (ven.contains("apple")){
            setOs("MacOS/IOS");
            setImage(R.drawable.aplle);
        }else if (ven.contains("microsoft")){
            setOs("Windows");
            setImage(R.drawable.windows);
        }else if(ven.contains("hikvision")||ven.contains("dahua")){
            setOs("Secure Camera");
            setImage(R.drawable.camera);
        }
    }

    public void setVersions(ArrayList<String> versions) {
        this.versions = versions;
    }
    public void addPort(String port){
        ports.add(port);
            if (port.equals("21")||port.equals("22")||port.equals("23")){
                setOs("Linux");
                setImage(R.drawable.linux);
            }else if (port.equals("3389")||port.equals("135")||port.equals("136")||port.equals("137")||port.equals("138")||port.equals("139")){
                setOs("Windows");
                setImage(R.drawable.windows);
            } else if (port.equals("554")||port.equals("37777")){
                setOs("Secure Camera");
                setImage(R.drawable.camera);
            }else if (port.equals("9100")){
                setOs("Printer");
                setImage(R.drawable.printer);
            }else if (port.equals("5555")){
                setOs("Android");
                setImage(R.drawable.iphone);
            }

    }
    public void addService(String serv){
        services.add(serv);
    }
    public ArrayList<String> getServices() {
        return services;
    }

    public String getIp() {
        return ip;
    }

    public ArrayList<String> getPorts() {
        return ports;
    }

    public ArrayList<String> getVersions() {
        return versions;
    }

    public int getImage() {
        if (image == 0){
            image = R.drawable.devices_local;
        }
        return image;
    }

    public String getMac() {
        return mac;
    }

    public String getOs() {
        return os;
    }

    public String getVendor() {
        return vendor;
    }

}
