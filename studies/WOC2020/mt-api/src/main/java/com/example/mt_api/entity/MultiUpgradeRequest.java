package com.example.mt_api.entity;

public class MultiUpgradeRequest {
    private String[] names;
    private String version;

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
