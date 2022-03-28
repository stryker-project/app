package com.zalexdev.stryker.custom;

/**
 * A Package and version
 */
public class Package {
    public String name;
    public String version;

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setName(String name) {
        this.name = name;
    }
}
