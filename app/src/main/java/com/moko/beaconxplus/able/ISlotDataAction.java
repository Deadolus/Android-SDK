package com.moko.beaconxplus.able;

public interface ISlotDataAction {
    boolean isValid();

    void sendData();

    void upgdateProgress(int viewId, int progress);
}
