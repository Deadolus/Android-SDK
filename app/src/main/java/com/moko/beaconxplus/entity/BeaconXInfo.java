package com.moko.beaconxplus.entity;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @Date 2018/1/16
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconx.entity.BeaconXInfo
 */
public class BeaconXInfo implements Serializable {

    public static final int VALID_DATA_FRAME_TYPE_UID = 0x00;
    public static final int VALID_DATA_FRAME_TYPE_URL = 0x10;
    public static final int VALID_DATA_FRAME_TYPE_TLM = 0x20;
    public static final int VALID_DATA_FRAME_TYPE_IBEACON = 0x50;
    public static final int VALID_DATA_FRAME_TYPE_INFO = 0x40;
    public static final int VALID_DATA_FRAME_TYPE_AXIS = 0x60;
    public static final int VALID_DATA_FRAME_TYPE_TH = 0x70;


    public String name;
    public int rssi;
    public String mac;
    public String scanRecord;
    public int battery;
    public int lockState;
    public int connectState;
    public long intervalTime;
    public long scanTime;
    public HashMap<String, ValidData> validDataHashMap;

    @Override
    public String toString() {
        return "BeaconXInfo{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }


    public static class ValidData {
        public int type;
        public int txPower;
        public byte[] values;
        public String data;

        @Override
        public String toString() {
            return "ValidData{" +
                    "type=" + type +
                    ", data='" + data + '\'' +
                    '}';
        }
    }
}
