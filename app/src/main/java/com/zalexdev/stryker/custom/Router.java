package com.zalexdev.stryker.custom;

/**
 * A router is a device that connects to a network
 */
public class Router {
    public String auth = "";
    public String ssid = "";
    public String psk = "";
    public String wps = "";
    public String ip = "0.0.0.0";
    public String title = "Can`t load...";
    public String status = "Failed";
    public String bssid = "";
    public String lon = "N/F";
    public String lun = "N/F";
    public String port = " ";
    public boolean ok = false;
    public boolean scanned;
    public int type = 0;
    public boolean success;

    public Router() {
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String str) {
        auth = str;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String str) {
        ssid = str;
    }

    public String getPsk() {
        return psk;
    }

    public void setPsk(String str) {
        psk = str;
    }

    public String getWps() {
        return wps;
    }

    public void setWps(String str) {
        wps = str;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean res) {
        success = res;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public boolean getOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLun() {
        return lun;
    }

    public void setLun(String lun) {
        this.lun = lun;
    }

    public void setScanned(boolean scanned) {
        this.scanned = scanned;
    }

    public boolean isScanned() {
        return scanned;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }
}
