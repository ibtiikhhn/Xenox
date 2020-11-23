package com.codies.Tattle.Utils;

import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

public class DeviceInfo {

    public static final String TAG = "DeviceInfo";
    Context context;

    public DeviceInfo(Context context) {
        this.context = context.getApplicationContext();
    }
    String  details =  "VERSION.RELEASE : "+ Build.VERSION.RELEASE
            +"\nVERSION.INCREMENTAL : "+Build.VERSION.INCREMENTAL
            +"\nVERSION.SDK.NUMBER : "+Build.VERSION.SDK_INT
            +"\nBOARD : "+Build.BOARD
            +"\nBOOTLOADER : "+Build.BOOTLOADER
            +"\nBRAND : "+Build.BRAND
            +"\nCPU_ABI : "+Build.CPU_ABI
            +"\nCPU_ABI2 : "+Build.CPU_ABI2
            +"\nDISPLAY : "+Build.DISPLAY
            +"\nFINGERPRINT : "+Build.FINGERPRINT
            +"\nHARDWARE : "+Build.HARDWARE
            +"\nHOST : "+Build.HOST
            +"\nID : "+Build.ID
            +"\nMANUFACTURER : "+Build.MANUFACTURER
            +"\nMODEL : "+Build.MODEL
            +"\nPRODUCT : "+Build.PRODUCT
            +"\nSERIAL : "+Build.SERIAL
            +"\nTAGS : "+Build.TAGS
            +"\nTIME : "+Build.TIME
            +"\nTYPE : "+Build.TYPE
            +"\nUNKNOWN : "+Build.UNKNOWN
            +"\nUSER : "+Build.USER;

    public String getDetails() {
        return details;
    }

    public String getCurrentSsid() {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null && networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }

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

            Log.i("HELL", publicIp+"");
            //Here 'publicIp' is your desire public IP
        }
    }

    public Account[] getAccounts() {
        ArrayList<String> emails = new ArrayList<>();

        Pattern gmailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
           /* if (gmailPattern.matcher(account.name).matches()) {
                emails.add(account.name);
            }*/
            Log.i(TAG, "getAccounts: type " + account.type);
            Log.i(TAG, "getAccounts: name " + account.name);

        }
        return accounts;

        /*TextView viewEmail = (TextView) findViewById(R.id.email_address_view);
        viewEmail.setText("Email From Device: " + emails.size());
        Toast.makeText(this, "Android Device Registered Email Address: " + emails.get(0), Toast
                .LENGTH_LONG).show();*/
    }

    public void getInstalledApps() {
        PackageManager packageManager = context.getPackageManager();

        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        List<PackageInfo> installedPackageInfo = new ArrayList<PackageInfo>();

        for(PackageInfo pi: packageInfoList){
            if(!isSystemPackage(pi)){
                installedPackageInfo.add(pi);
            }
        }

        for (int i = 0; i < installedPackageInfo.size(); i++) {
            Log.i(TAG, "getInstalledApps: " + installedPackageInfo.get(i).packageName);
        }

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

    public void getImei() {

    }

}
