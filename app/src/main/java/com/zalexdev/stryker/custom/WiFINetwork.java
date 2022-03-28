package com.zalexdev.stryker.custom;
import java.util.Comparator;

/**
 * A WiFiNetwork is a WiFi network that has been detected by the app
 */
public class WiFINetwork {
    public String mac;
    public String ssid;
    public Boolean wps = false;
    public Boolean is5hhz = false;
    public Boolean isOK = false;
    public Boolean isBlocked = false;
    public String model;
    public String power = "40";
    public String channel = "1";
    public String lon;
    public String lun;
    public String date;
    public String psk;
    public String pin;
    public boolean canceled = false;
    public boolean three = false;

    public WiFINetwork() {
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public Boolean getIs5hhz() {
        return Integer.parseInt(channel) < 20;
    }

    public void setIs5hhz(Boolean is5hhz) {
        this.is5hhz = is5hhz;
    }

    public Boolean getWps() {
        return wps;
    }

    public void setWps(Boolean wps) {
        this.wps = wps;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getPower() {
        return Integer.parseInt(power);
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getLun() {
        return lun;
    }

    public void setLun(String lun) {
        this.lun = lun;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPsk() {
        return psk;
    }

    public void setPsk(String psk) {
        this.psk = psk;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Boolean getOK() {
        return isOK;
    }

    public void setOK(Boolean OK) {
        isOK = OK;
    }

    public Boolean getBlocked() {
        return isBlocked;
    }

    public void setBlocked(Boolean blocked) {
        isBlocked = blocked;
    }

    public static class WiFIComporator implements Comparator<WiFINetwork> {
        public int compare(WiFINetwork o1, WiFINetwork o2) {
            return o1.getPower() - o2.getPower();
        }
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isThree() {
        return three;
    }

    public void setThree(boolean three) {
        this.three = three;
    }
}
