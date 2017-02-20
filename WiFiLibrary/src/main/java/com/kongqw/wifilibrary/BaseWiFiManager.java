package com.kongqw.wifilibrary;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * Created by kongqingwei on 2017/2/17.
 * BaseWiFiManager
 */

public class BaseWiFiManager {

    WifiManager mWifiManager;

    private static ConnectivityManager mConnectivityManager;

    BaseWiFiManager(Context context) {
        // 取得WifiManager对象
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * 添加开放网络配置
     *
     * @param ssid SSID
     * @return NetworkId
     */
    int setOpenNetwork(@NonNull String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return -1;
        }
        WifiConfiguration wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid);
        if (null == wifiConfiguration) {
            // 生成配置
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = addDoubleQuotation(ssid);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedAuthAlgorithms.clear();
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            // 添加配置并返回NetworkID
            return addNetwork(wifiConfig);
        } else {
            // 返回NetworkID
            return wifiConfiguration.networkId;
        }
    }

    /**
     * 添加WEP网络配置
     *
     * @param ssid     SSID
     * @param password 密码
     * @return NetworkId
     */
    int setWEPNetwork(@NonNull String ssid, @NonNull String password) {
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
            return -1;
        }
        WifiConfiguration wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid);
        if (null == wifiConfiguration) {
            // 添加配置
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = addDoubleQuotation(ssid);
            wifiConfig.wepKeys[0] = "\"" + password + "\"";
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            Log.i("kongqw", "setWEPNetwork: addNetwork");
            // 添加配置并返回NetworkID
            return addNetwork(wifiConfig);
        } else {
            Log.i("kongqw", "setWEPNetwork: updateNetwork");
            // 更新配置并返回NetworkID
            wifiConfiguration.wepKeys[0] = "\"" + password + "\"";
            return updateNetwork(wifiConfiguration);
        }

    }

    /**
     * 添加WPA网络配置
     *
     * @param ssid     SSID
     * @param password 密码
     * @return NetworkId
     */
    int setWPA2Network(@NonNull String ssid, @NonNull String password) {
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
            return -1;
        }
        WifiConfiguration wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid);
        if (null == wifiConfiguration) {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = addDoubleQuotation(ssid);
            wifiConfig.preSharedKey = "\"" + password + "\"";
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.status = WifiConfiguration.Status.ENABLED;
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            // 添加配置并返回NetworkID
            Log.i("kongqw", "setWPA2Network: addNetwork");
            return addNetwork(wifiConfig);
        } else {
            Log.i("kongqw", "setWPA2Network: updateNetwork  wifiConfiguration.SSID = " + wifiConfiguration.SSID + "   wifiConfiguration.networkId = " + wifiConfiguration.networkId);
            // 更新配置并返回NetworkID
            wifiConfiguration.preSharedKey = "\"" + password + "\"";
            return updateNetwork(wifiConfiguration);
        }
    }

    /**
     * 通过热点名获取热点配置
     *
     * @param ssid 热点名
     * @return 配置信息
     */
    public WifiConfiguration getConfigFromConfiguredNetworksBySsid(@NonNull String ssid) {
        ssid = addDoubleQuotation(ssid);
        List<WifiConfiguration> existingConfigs = getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            Log.i("kongqw", "getConfigFromConfiguredNetworksBySsid: existingConfig.SSID = " + existingConfig.SSID + "  existingConfig.networkId = " + existingConfig.networkId + "  ssid = " + ssid);
            if (existingConfig.SSID.equals(ssid)) {
                return existingConfig;
            }
        }
        return null;
    }


    /**
     * 获取WIFI的开关状态
     *
     * @return WIFI的可用状态
     */
    boolean isWifiEnabled() {
        return null != mWifiManager && mWifiManager.isWifiEnabled();
    }

    /**
     * 判断WIFI是否连接
     *
     * @return 是否连接
     */
    boolean isWifiConnected() {
        if (null != mConnectivityManager) {
            NetworkInfo mWifi = mConnectivityManager.getActiveNetworkInfo();
            return mWifi.isConnected();
        } else {
            return false;
        }
    }

    /**
     * 获取当前正在连接的WIFI信息
     *
     * @return 当前正在连接的WIFI信息
     */
    public WifiInfo getConnectionInfo() {
        if (null != mWifiManager) {
            return mWifiManager.getConnectionInfo();
        } else {
            return null;
        }
    }

    /**
     * 扫描附近的WIFI
     */
    public void startScan() {
        if (null != mWifiManager) {
            mWifiManager.startScan();
        }
    }

    /**
     * 获取最近扫描的WIFI热点
     *
     * @return WIFI热点列表
     */
    public List<ScanResult> getScanResults() {
        // 得到扫描结果
        if (null != mWifiManager) {
            return mWifiManager.getScanResults();
        } else {
            return null;
        }
    }

    /**
     * 获取配置过的WIFI信息
     *
     * @return 配置信息
     */
    public List<WifiConfiguration> getConfiguredNetworks() {
        if (null != mWifiManager) {
            return mWifiManager.getConfiguredNetworks();
        } else {
            return null;
        }
    }

    /**
     * 保持配置
     *
     * @return 保持是否成功
     */
    boolean saveConfiguration() {
        return null != mWifiManager && mWifiManager.saveConfiguration();
    }

    /**
     * 连接到网络
     *
     * @param networkId NetworkId
     * @return 连接结果
     */
    boolean enableNetwork(int networkId) {
        if (null != mWifiManager) {
            boolean isDisconnect = mWifiManager.disconnect();
            boolean isEnableNetwork = mWifiManager.enableNetwork(networkId, true);
            boolean isReconnect = mWifiManager.reconnect();
            return isDisconnect && isEnableNetwork && isReconnect;
        } else {
            return false;
        }
    }

    /**
     * 添加网络配置
     *
     * @param wifiConfig 配置信息
     * @return NetworkId
     */
    private int addNetwork(WifiConfiguration wifiConfig) {
        int networkId = mWifiManager.addNetwork(wifiConfig);
        if (networkId != -1) {
            boolean isSave = mWifiManager.saveConfiguration();
            Log.i("kongqw", "addNetwork: networkId = " + networkId + "   isSave = " + isSave);
            if (isSave) {
                return networkId;
            }

        }
        Log.i("kongqw", "addNetwork: -1");
        return -1;
    }

    /**
     * 更新网络配置
     *
     * @param wifiConfig 配置信息
     * @return NetworkId
     */
    private int updateNetwork(WifiConfiguration wifiConfig) {
        int networkId = mWifiManager.updateNetwork(wifiConfig);
        if (networkId != -1) {
            boolean isSave = mWifiManager.saveConfiguration();
            Log.i("kongqw", "updateNetwork: networkId = " + networkId + "   isSave = " + isSave);
            if (isSave) {
                return networkId;
            }

        }
        Log.i("kongqw", "updateNetwork: -1  networkId = " + networkId + "   wifiConfig.networkId = " + wifiConfig.networkId);
        return -1;
    }

    /**
     * 断开WIFI
     *
     * @param netId netId
     * @return 是否断开
     */
    public boolean disconnectWifi(int netId) {
        boolean isDisable = mWifiManager.disableNetwork(netId);
        boolean isDisconnect = mWifiManager.disconnect();
        return isDisable && isDisconnect;
    }

    /**
     * 删除配置
     *
     * @param netId netId
     * @return 是否删除成功
     */
    public boolean deleteConfig(int netId) {
        // TODO 正在连接的网络不能删除，要先断开连接，再删除
        boolean isDisable = mWifiManager.disableNetwork(netId);
        boolean isRemove = mWifiManager.removeNetwork(netId);
        boolean isSave = mWifiManager.saveConfiguration();
        Log.i("kongqw", "deleteConfig: isDisable = " + isDisable + "  isRemove = " + isRemove + "  isSave = " + isSave);
        return isDisable && isRemove && isSave;
    }

    public int calculateSignalLevel(int rssi) {
        return WifiManager.calculateSignalLevel(rssi, 5);
    }


    /**
     * 网络加密模式
     */
    public enum SecurityMode {
        OPEN, WEP, WPA, WPA2
    }

    /**
     * 获取WIFI的加密方式
     *
     * @param scanResult WIFI信息
     * @return 加密方式
     */
    public SecurityMode getSecurityMode(@NonNull ScanResult scanResult) {
        String capabilities = scanResult.capabilities;

        if (capabilities.contains("WPA")) {
            return SecurityMode.WPA;
        } else if (capabilities.contains("WEP")) {
            return SecurityMode.WEP;
            //        } else if (capabilities.contains("EAP")) {
            //            return SecurityMode.WEP;
        } else {
            // 没有加密
            return SecurityMode.OPEN;
        }
    }

    /**
     * 添加双引号
     *
     * @param text 待处理字符串
     * @return 处理后字符串
     */
    public String addDoubleQuotation(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        return "\"" + text + "\"";
    }
}
