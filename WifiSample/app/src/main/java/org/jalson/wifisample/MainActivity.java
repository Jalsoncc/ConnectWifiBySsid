package org.jalson.wifisample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.jalson.lib_wifi.Wifi;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int numOpenNetworksKept;
    private WifiManager mWifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prepareWifi();

    }

    /**
     * wifi准备,如果关闭则打开wifi
     */
    private void prepareWifi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
            numOpenNetworksKept = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.WIFI_NUM_OPEN_NETWORKS_KEPT, 10);
        else
            numOpenNetworksKept = Settings.Global.getInt(this.getContentResolver(), Settings.Global.WIFI_NUM_OPEN_NETWORKS_KEPT, 10);
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(true);
    }

    /**
     * 接收系统wifi扫描结果广播结果
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = mWifiManager.getScanResults();
                if (mScanResults != null && mScanResults.size() > 0) {
                    WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                    ScanResult result = null;
                    for (ScanResult mResult : mScanResults) {
                        if (mResult.SSID.contains("jalson")) {
                            result = mResult;
                        }
                    }
                    if (wifiInfo != null && wifiInfo.getBSSID() != null && result != null && result.BSSID != null) {
                        if (wifiInfo.getBSSID().equals(result.BSSID))
                            return;
                    }

                    if (result != null) {
                        Wifi.connectToNewNetwork(MainActivity.this, mWifiManager, result, "00000000", numOpenNetworksKept);
                    }
                }
            }
        }


    };

    @Override
    protected void onResume() {
        super.onResume();
        final IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);
        if (mWifiManager != null)
            mWifiManager.startScan();
    }
}
