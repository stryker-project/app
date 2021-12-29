package com.zalexdev.stryker.custom;

import java.util.Comparator;

public class WiFINetwork {
    public String mac;
    public String ssid;
    public Boolean wps = false;
    public Boolean is5hhz = false;
    public String model;
    public String power;
    public String channel;

    public WiFINetwork(){}

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setIs5hhz(Boolean is5hhz) {
        this.is5hhz = is5hhz;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setWps(Boolean wps) {
        this.wps = wps;
    }

    public String getMac() {
        return mac;
    }

    public String getSsid() {
        return ssid;
    }

    public Boolean getIs5hhz() {
        return Integer.parseInt(channel) <20;
    }

    public Boolean getWps() {
        return wps;
    }

    public String getChannel() {
        return channel;
    }

    public String getModel() {
        return model;
    }

    public int getPower() {
        return Integer.parseInt(power);
    }
    public static class WiFIComporator implements Comparator<WiFINetwork> {
        public int compare(WiFINetwork o1, WiFINetwork o2) {
            return o1.getPower() - o2.getPower();
        }
    }
}
