package com.moko.beaconxplus.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;

import com.moko.beaconxplus.entity.BeaconXInfo;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.service.DeviceInfoParseable;
import com.moko.support.utils.MokoUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * @Date 2019/5/22
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconxplus.utils.BeaconXInfoParseableImpl
 */
public class BeaconXInfoParseableImpl implements DeviceInfoParseable<BeaconXInfo> {
    private HashMap<String, BeaconXInfo> beaconXInfoHashMap;
    private int battery = -1;
    private int connectState = -1;
    private int lockState = -1;

    public BeaconXInfoParseableImpl() {
        this.beaconXInfoHashMap = new HashMap<>();
    }

    @Override
    public BeaconXInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        ScanResult result = deviceInfo.scanResult;
        ScanRecord record = result.getScanRecord();
        Map<ParcelUuid, byte[]> map = record.getServiceData();
        // filter
        boolean isEddystone = false;
        boolean isBeaconXPlus = false;
        byte[] values = null;
        int type = -1;
        if (map != null && !map.isEmpty()) {
            Iterator iterator = map.keySet().iterator();
            if (iterator.hasNext()) {
                ParcelUuid parcelUuid = (ParcelUuid) iterator.next();
                if (parcelUuid.toString().startsWith("0000feaa")) {
                    isEddystone = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_UID:
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_UID;
                                // 00ee0102030405060708090a0102030405060000
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_URL:
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_URL;
                                // 100c0141424344454609
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM:
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM;
                                // 20000d18158000017eb20002e754
                                break;
                        }
                    }
                    values = bytes;
                } else if (parcelUuid.toString().startsWith("0000feab")) {
                    isBeaconXPlus = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO:
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO;
                                int batteryValue = MokoUtils.toInt(Arrays.copyOfRange(bytes, 3, 5));
                                if (batteryValue > 3000) {
                                    battery = 100;
                                } else if (batteryValue < 2000) {
                                    battery = 0;
                                } else {
                                    battery = (batteryValue - 2000) * 100 / 1000;
                                }
                                lockState = bytes[5] & 0xff;
                                connectState = bytes[6] & 0xff;
                                // 40000a0d0d0001ff02030405063001
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON:
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON;
                                // 50ee0c0102030405060708090a0b0c0d0e0f1000010002
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_AXIS:
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_AXIS;
                                // 60f60e010007f600d5002e00
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_TH:
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_TH;
                                // 700b1000fb02f5
                                break;
                        }
                    }
                    values = bytes;
                }

            }
        }
        if ((!isEddystone && !isBeaconXPlus) || values == null || type == -1) {
            return null;
        }
        // avoid repeat
        BeaconXInfo beaconXInfo;
        if (beaconXInfoHashMap.containsKey(deviceInfo.mac)) {
            beaconXInfo = beaconXInfoHashMap.get(deviceInfo.mac);
            beaconXInfo.rssi = deviceInfo.rssi;
            if (battery >= 0) {
                beaconXInfo.battery = battery;
            }
            if (lockState >= 0) {
                beaconXInfo.lockState = lockState;
            }
            if (connectState >= 0) {
                beaconXInfo.connectState = connectState;
            }
            beaconXInfo.scanRecord = deviceInfo.scanRecord;
            long currentTime = SystemClock.elapsedRealtime();
            long intervalTime = currentTime - beaconXInfo.scanTime;
            beaconXInfo.intervalTime = intervalTime;
            beaconXInfo.scanTime = currentTime;
        } else {
            beaconXInfo = new BeaconXInfo();
            beaconXInfo.name = deviceInfo.name;
            beaconXInfo.mac = deviceInfo.mac;
            beaconXInfo.rssi = deviceInfo.rssi;
            if (battery < 0) {
                beaconXInfo.battery = -1;
            } else {
                beaconXInfo.battery = battery;
            }
            if (lockState < 0) {
                beaconXInfo.lockState = -1;
            } else {
                beaconXInfo.lockState = lockState;
            }
            if (connectState < 0) {
                beaconXInfo.connectState = -1;
            } else {
                beaconXInfo.connectState = connectState;
            }
            beaconXInfo.scanRecord = deviceInfo.scanRecord;
            beaconXInfo.scanTime = SystemClock.elapsedRealtime();
            beaconXInfo.validDataHashMap = new HashMap<>();
            beaconXInfoHashMap.put(deviceInfo.mac, beaconXInfo);
        }
        String data = MokoUtils.bytesToHexString(values);
        if (beaconXInfo.validDataHashMap.containsKey(data)) {
            return beaconXInfo;
        } else {
            BeaconXInfo.ValidData validData = new BeaconXInfo.ValidData();
            validData.data = data;
            validData.type = type;
            validData.txPower = record.getTxPowerLevel();
            if (type == BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM) {
                beaconXInfo.validDataHashMap.put(type + "", validData);
                return beaconXInfo;
            }
            if (type == BeaconXInfo.VALID_DATA_FRAME_TYPE_TH) {
                beaconXInfo.validDataHashMap.put(type + "", validData);
                return beaconXInfo;
            }
            if (type == BeaconXInfo.VALID_DATA_FRAME_TYPE_AXIS) {
                beaconXInfo.validDataHashMap.put(type + "", validData);
                return beaconXInfo;
            }
            beaconXInfo.validDataHashMap.put(data, validData);
        }
        return beaconXInfo;
    }
}
