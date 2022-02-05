package com.zalexdev.stryker.custom;

import org.jsoup.nodes.Document;

public class Module {
    public String name;
    public String author;
    public String desc;
    public boolean only64bit = false;
    public boolean proper = false;
    public boolean installed = false;
    public String srcinstall;
    public String pksg;
    public Double version;

    public String getAuthor() {
        return author;
    }

    public String getDesc() {
        return desc;
    }

    public String getName() {
        return name;
    }

    public void setPksg(String pksg) {
        this.pksg = pksg;
    }

    public String getPksg() {
        return pksg;
    }

    public boolean isOnly64bit() {
        return only64bit;
    }

    public boolean isProper() {
        return proper;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOnly64bit(boolean only64bit) {
        this.only64bit = only64bit;
    }

    public void setProper(boolean proper) {
        this.proper = proper;
    }

    public String getSrcinstall() {
        return srcinstall;
    }

    public void setSrcinstall(String srcinstall) {
        this.srcinstall = srcinstall;
    }

    public void setVersion(Double version) {
        this.version = version;
    }

    public Double getVersion() {
        return version;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }
}
