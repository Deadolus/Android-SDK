package com.moko.beaconxpro.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moko.beaconxpro.AppConstants;
import com.moko.beaconxpro.R;
import com.moko.beaconxpro.activity.DeviceInfoActivity;
import com.moko.beaconxpro.activity.SlotDataActivity;
import com.moko.beaconxpro.utils.BeaconXParser;
import com.moko.support.MokoSupport;
import com.moko.support.entity.SlotData;
import com.moko.support.entity.SlotEnum;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.utils.MokoUtils;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SlotFragment extends Fragment {

    private static final String TAG = "SlotFragment";
    @Bind(R.id.iv_slot1)
    ImageView ivSlot1;
    @Bind(R.id.tv_slot1)
    TextView tvSlot1;
    @Bind(R.id.rl_slot1)
    RelativeLayout rlSlot1;
    @Bind(R.id.iv_slot2)
    ImageView ivSlot2;
    @Bind(R.id.tv_slot2)
    TextView tvSlot2;
    @Bind(R.id.rl_slot2)
    RelativeLayout rlSlot2;
    @Bind(R.id.iv_slot3)
    ImageView ivSlot3;
    @Bind(R.id.tv_slot3)
    TextView tvSlot3;
    @Bind(R.id.rl_slot3)
    RelativeLayout rlSlot3;
    @Bind(R.id.iv_slot4)
    ImageView ivSlot4;
    @Bind(R.id.tv_slot4)
    TextView tvSlot4;
    @Bind(R.id.rl_slot4)
    RelativeLayout rlSlot4;
    @Bind(R.id.iv_slot5)
    ImageView ivSlot5;
    @Bind(R.id.tv_slot5)
    TextView tvSlot5;
    @Bind(R.id.rl_slot5)
    RelativeLayout rlSlot5;
    @Bind(R.id.iv_slot6)
    ImageView ivSlot6;
    @Bind(R.id.tv_slot6)
    TextView tvSlot6;
    @Bind(R.id.rl_slot6)
    RelativeLayout rlSlot6;

    private DeviceInfoActivity activity;
    private SlotData slotData;
    private int deviceType;
    private int triggerType;
    private String triggerData;

    public SlotFragment() {
    }

    public static SlotFragment newInstance() {
        SlotFragment fragment = new SlotFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_slot, container, false);
        ButterKnife.bind(this, view);
        activity = (DeviceInfoActivity) getActivity();
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @OnClick({R.id.rl_slot1, R.id.rl_slot2, R.id.rl_slot3, R.id.rl_slot4, R.id.rl_slot5, R.id.rl_slot6})
    public void onViewClicked(View view) {
        slotData = new SlotData();
        SlotFrameTypeEnum frameType = (SlotFrameTypeEnum) view.getTag();
        slotData.frameTypeEnum = frameType;
        // NO DATA直接跳转
        switch (view.getId()) {
            case R.id.rl_slot1:
                createData(frameType, SlotEnum.SLOT_1);
                break;
            case R.id.rl_slot2:
                createData(frameType, SlotEnum.SLOT_2);
                break;
            case R.id.rl_slot3:
                createData(frameType, SlotEnum.SLOT_3);
                break;
            case R.id.rl_slot4:
                createData(frameType, SlotEnum.SLOT_4);
                break;
            case R.id.rl_slot5:
                createData(frameType, SlotEnum.SLOT_5);
                break;
            case R.id.rl_slot6:
                createData(frameType, SlotEnum.SLOT_6);
                break;
        }
    }

    private void createData(SlotFrameTypeEnum frameType, SlotEnum slot) {
        slotData.slotEnum = slot;
        switch (frameType) {
            case NO_DATA:
                Intent intent = new Intent(getActivity(), SlotDataActivity.class);
                intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
                intent.putExtra(AppConstants.EXTRA_KEY_DEVICE_TYPE, deviceType);
                startActivityForResult(intent, AppConstants.REQUEST_CODE_SLOT_DATA);
                break;
            case IBEACON:
            case TLM:
            case URL:
            case UID:
            case DEVICE:
            case TH:
            case AXIS:
                getSlotData(slot);
                break;
        }
    }

    private void getSlotData(SlotEnum slotEnum) {
        activity.showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(
                activity.mMokoService.setSlot(slotEnum),
                activity.mMokoService.getSlotData(),
                activity.mMokoService.getTrigger(),
                activity.mMokoService.getAdvTxPower(),
                activity.mMokoService.getRadioTxPower(),
                activity.mMokoService.getAdvInterval()
        );
    }

//    private void getiBeaconData(SlotEnum slotEnum) {
//        activity.showSyncingProgressDialog();
//        MokoSupport.getInstance().sendOrder(
//                activity.mMokoService.setSlot(slotEnum),
//                activity.mMokoService.getiBeaconUUID(),
//                activity.mMokoService.getiBeaconInfo(),
//                activity.mMokoService.getRadioTxPower(),
//                activity.mMokoService.getAdvInterval()
//        );
//    }

    // 10 20 50 40 FF FF
    public void updateSlotType(byte[] value) {
        changeView((int) value[0] & 0xff, tvSlot1, ivSlot1, rlSlot1);
        changeView((int) value[1] & 0xff, tvSlot2, ivSlot2, rlSlot2);
        changeView((int) value[2] & 0xff, tvSlot3, ivSlot3, rlSlot3);
        changeView((int) value[3] & 0xff, tvSlot4, ivSlot4, rlSlot4);
        changeView((int) value[4] & 0xff, tvSlot5, ivSlot5, rlSlot5);
        changeView((int) value[5] & 0xff, tvSlot6, ivSlot6, rlSlot6);

    }

    private void changeView(int frameType, TextView tvSlot, ImageView ivSlot, RelativeLayout rlSlot) {
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum == null) {
            return;
        }
        switch (slotFrameTypeEnum) {
            case UID:
                ivSlot.setImageResource(R.drawable.eddystone_icon);
                break;
            case URL:
                ivSlot.setImageResource(R.drawable.eddystone_icon);
                break;
            case TLM:
                ivSlot.setImageResource(R.drawable.eddystone_icon);
                break;
            case IBEACON:
                ivSlot.setImageResource(R.drawable.ibeacon_icon);
                break;
            case DEVICE:
                ivSlot.setImageResource(R.drawable.device_icon);
                break;
            case NO_DATA:
                ivSlot.setImageResource(R.drawable.no_data_icon);
                break;
            case TH:
                ivSlot.setImageResource(R.drawable.th_icon);
                break;
            case AXIS:
                ivSlot.setImageResource(R.drawable.axis_icon);
                break;

        }
        tvSlot.setText(slotFrameTypeEnum.getShowName());
        rlSlot.setTag(slotFrameTypeEnum);
    }

    //    private String iBeaconUUID;
//    private String major;
//    private String minor;
//    private int rssi_1m;
    private int txPower;
    private int advInterval;

    // eb640010e2c56db5dffb48d2b060d0f5a71096e0
//    public void setiBeaconUUID(byte[] value) {
//        String valueHex = MokoUtils.bytesToHexString(value);
//        iBeaconUUID = valueHex.substring(8);
//        slotData.iBeaconUUID = iBeaconUUID;
//    }

    // eb6600050000000000
//    public void setiBeaconInfo(byte[] value) {
//        String valueHex = MokoUtils.bytesToHexString(value);
//        major = valueHex.substring(8, 12);
//        minor = valueHex.substring(12, 16);
//        rssi_1m = Integer.parseInt(valueHex.substring(16), 16);
//        slotData.major = major;
//        slotData.minor = minor;
//        slotData.rssi_1m = 0 - rssi_1m;
//    }

    // 00
    public void setTxPower(byte[] value) {
        txPower = value[0];
        slotData.txPower = txPower;
    }

    // 00
    public void setAdvTxPower(byte[] value) {
        switch (mSlotFrameTypeEnum) {
            case IBEACON:
                slotData.rssi_1m = value[0];
                break;
            default:
                slotData.rssi_0m = value[0];
                break;
        }
    }

    // 0064
    public void setAdvInterval(byte[] value) {
        advInterval = Integer.parseInt(MokoUtils.bytesToHexString(value), 16);
        slotData.advInterval = advInterval;
        Intent intent = new Intent(getActivity(), SlotDataActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
        intent.putExtra(AppConstants.EXTRA_KEY_DEVICE_TYPE, deviceType);
        intent.putExtra(AppConstants.EXTRA_KEY_TRIGGER_TYPE, triggerType);
        intent.putExtra(AppConstants.EXTRA_KEY_TRIGGER_DATA, triggerData);
        startActivityForResult(intent, AppConstants.REQUEST_CODE_SLOT_DATA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == AppConstants.REQUEST_CODE_SLOT_DATA) {
                Log.i(TAG, "onActivityResult: ");
                activity.getSlotType();
            }
        }
    }

    private SlotFrameTypeEnum mSlotFrameTypeEnum;

    // 不同类型的数据长度不同
    public void setSlotData(byte[] value) {
        int frameType = value[0];
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum != null) {
            mSlotFrameTypeEnum = slotFrameTypeEnum;
            switch (slotFrameTypeEnum) {
                case URL:
                    // URL：10cf014c6f766500
                    BeaconXParser.parseUrlData(slotData, value);
                    break;
                case TLM:
                    break;
                case TH:
                    break;
                case AXIS:
                    break;
                case UID:
                    BeaconXParser.parseUidData(slotData, value);
                    break;
                case DEVICE:
                    byte[] deviceName = Arrays.copyOfRange(value, 1, value.length);
                    slotData.deviceName = new String(deviceName);
                    break;
                case IBEACON:
                    byte[] uuid = Arrays.copyOfRange(value, 1, 17);
                    byte[] major = Arrays.copyOfRange(value, 17, 19);
                    byte[] minor = Arrays.copyOfRange(value, 19, 21);
                    slotData.iBeaconUUID = MokoUtils.bytesToHexString(uuid);
                    slotData.major = MokoUtils.bytesToHexString(major);
                    slotData.minor = MokoUtils.bytesToHexString(minor);
                    break;
            }
        }
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }


    public void setTriggerData(byte[] value) {
        triggerType = value[4] & 0xff;
        if (triggerType != 0) {
            triggerData = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 5, value.length));
        }
    }
}
