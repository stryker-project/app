package com.zalexdev.stryker.custom;

public class Router {
    public String auth = "";
    public String ssid= "";
    public String psk= "";
    public String wps= "";
    public String ip = "0.0.0.0";
    public String title = "Can`t load...";
    public String status = "Failed";
    public String bssid = "";
    public boolean ok = false;
    public int type = 0;
    public boolean success;
    public Router(){}
    public void setAuth(String str){
        auth = str;
    }
    public void setSsid(String str){
        ssid = str;
    }
    public void setPsk(String str){
        psk = str;
    }
    public void setWps(String str){
        wps = str;
    }
    public void setSuccess(boolean res){
        success = res;
    }

    public String getAuth() {
        return auth;
    }

    public String getSsid() {
        return ssid;
    }

    public String getPsk() {
        return psk;
    }

    public String getWps() {
        return wps;
    }
    public boolean getSuccess(){
        return success;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIp() {
        return ip;
    }

    public String getTitle() {
        return title;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public int getType() {
        return type;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }
    public boolean getOk() {
        return ok;
    }
    public void setType(int type) {
        this.type = type;
    }
}
