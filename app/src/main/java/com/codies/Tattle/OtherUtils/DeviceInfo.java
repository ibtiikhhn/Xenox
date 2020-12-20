package com.codies.Tattle.OtherUtils;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.codies.Tattle.Models.InstalledApps;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.android.material.internal.ContextUtils.getActivity;

public class DeviceInfo {

    public static final String TAG = "DeviceInfo";
    Context context;

    public DeviceInfo(Context context) {
        this.context = context;
    }

    String details = "VERSION.RELEASE : " + Build.VERSION.RELEASE
            + "\nVERSION.INCREMENTAL : " + Build.VERSION.INCREMENTAL
            + "\nVERSION.SDK.NUMBER : " + Build.VERSION.SDK_INT
            + "\nBOARD : " + Build.BOARD
            + "\nBOOTLOADER : " + Build.BOOTLOADER
            + "\nBRAND : " + Build.BRAND
            + "\nCPU_ABI : " + Build.CPU_ABI
            + "\nCPU_ABI2 : " + Build.CPU_ABI2
            + "\nDISPLAY : " + Build.DISPLAY
            + "\nFINGERPRINT : " + Build.FINGERPRINT
            + "\nHARDWARE : " + Build.HARDWARE
            + "\nHOST : " + Build.HOST
            + "\nID : " + Build.ID
            + "\nMANUFACTURER : " + Build.MANUFACTURER
            + "\nMODEL : " + Build.MODEL
            + "\nPRODUCT : " + Build.PRODUCT
            + "\nSERIAL : " + Build.SERIAL
            + "\nTAGS : " + Build.TAGS
            + "\nTIME : " + Build.TIME
            + "\nTYPE : " + Build.TYPE
            + "\nUNKNOWN : " + Build.UNKNOWN
            + "\nUSER : " + Build.USER
            + "\nSSID : " + "null"
            +"\nPRIVATEIP : "+getPrivateIpAddress();

    public String getDetails() {
        return details;
    }

   /* public String getCurrentSsid() {
        String ssid = null;
        Activity activity = (Activity)context;

        ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null && networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }*/

    public String getPrivateIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            Log.i("HELL", "getPrivateIpAddress: "+e.getLocalizedMessage());
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    public static class GetPublicIP extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String publicIP = "";
            try  {
                java.util.Scanner s = new java.util.Scanner(
                        new java.net.URL(
                                "https://api.ipify.org")
                                .openStream(), "UTF-8")
                        .useDelimiter("\\A");
                publicIP = s.next();
                System.out.println("My current IP address is " + publicIP);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            return publicIP;
        }

        @Override
        protected void onPostExecute(String publicIp) {
            super.onPostExecute(publicIp);
            //Here 'publicIp' is your desire public IP
        }
    }

    public List<Account> getAccounts() {
        List<Account> accountList = Arrays.asList(AccountManager.get(context).getAccounts());
        return accountList;
    }

    public List<InstalledApps> getInstalledApps() {
        PackageManager packageManager = context.getPackageManager();

        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        List<PackageInfo> installedPackageInfo = new ArrayList<PackageInfo>();
        List<InstalledApps> installedApps = new ArrayList<>();

        for(PackageInfo pi: packageInfoList){
            if(!isSystemPackage(pi)){
                installedPackageInfo.add(pi);
            }
        }

        for (PackageInfo packageInfo : installedPackageInfo) {
            Timestamp timestamp = new Timestamp(packageInfo.firstInstallTime);
            Log.i(TAG, "getInstalledApps:time "+timestamp.toString());
            installedApps.add(new InstalledApps(packageInfo.packageName, packageInfo.versionName, String.valueOf(packageInfo.firstInstallTime), String.valueOf(packageInfo.lastUpdateTime)));
        }
        return installedApps;
    }

    boolean isSystemPackage(PackageInfo pkgInfo){
        if((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)!=0)
            return false;
        else if((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0)
            return true;
        else if((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED)!=0)
            return false;
        else
            return true;
    }

    public String getIMEIDeviceId(Context context) {

        String deviceId = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "";
                }
            }
            /*assert mTelephony != null;
            if (mTelephony.getDeviceId() != null)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    deviceId = mTelephony.getImei();
                }else {
                    deviceId = mTelephony.getDeviceId();
             *//*   }
            } else {
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        Log.d("deviceId", deviceId);*/
        }
        return deviceId;
    }
}
