package com.codies.Tattle.Models;

import android.content.pm.PackageInfo;

import java.io.Serializable;

public class InstalledApps implements Serializable {
    String packageName;
    String packageVersion;
    String firstInstallTime;
    String lastUpdateTime;

    public InstalledApps(String packageName, String packageVersion, String firstInstallTime, String lastUpdateTime) {
        this.packageName = packageName;
        this.packageVersion = packageVersion;
        this.firstInstallTime = firstInstallTime;
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getFirstInstallTime() {
        return firstInstallTime;
    }

    public void setFirstInstallTime(String firstInstallTime) {
        this.firstInstallTime = firstInstallTime;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        return "InstalledApps{" +
                "packageName='" + packageName + '\'' +
                ", packageVersion='" + packageVersion + '\'' +
                ", firstInstallTime='" + firstInstallTime + '\'' +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                '}';
    }
}
