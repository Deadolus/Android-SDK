package com.moko.beaconxplus.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.moko.beaconxplus.AppConstants;
import com.moko.beaconxplus.BeaconXListAdapter;
import com.moko.beaconxplus.R;
import com.moko.beaconxplus.dialog.AlertMessageDialog;
import com.moko.beaconxplus.dialog.LoadingDialog;
import com.moko.beaconxplus.dialog.LoadingMessageDialog;
import com.moko.beaconxplus.dialog.PasswordDialog;
import com.moko.beaconxplus.dialog.ScanFilterDialog;
import com.moko.beaconxplus.entity.BeaconXInfo;
import com.moko.beaconxplus.service.MokoService;
import com.moko.beaconxplus.utils.BeaconXInfoParseableImpl;
import com.moko.beaconxplus.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.entity.MokoCharacteristic;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.handler.MokoCharacteristicHandler;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends BaseActivity implements MokoScanDeviceCallback, BaseQuickAdapter.OnItemChildClickListener {

    @Bind(R.id.iv_refresh)
    ImageView ivRefresh;
    @Bind(R.id.rv_devices)
    RecyclerView rvDevices;
    @Bind(R.id.tv_device_num)
    TextView tvDeviceNum;
    @Bind(R.id.rl_edit_filter)
    RelativeLayout rl_edit_filter;
    @Bind(R.id.rl_filter)
    RelativeLayout rl_filter;
    @Bind(R.id.tv_filter)
    TextView tv_filter;
    private MokoService mMokoService;
    private boolean mReceiverTag = false;
    private HashMap<String, BeaconXInfo> beaconXInfoHashMap;
    private ArrayList<BeaconXInfo> beaconXInfos;
    private BeaconXListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Intent intent = new Intent(this, MokoService.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        EventBus.getDefault().register(this);
        beaconXInfoHashMap = new HashMap<>();
        beaconXInfos = new ArrayList<>();
        adapter = new BeaconXListAdapter();
        adapter.replaceData(beaconXInfos);
        adapter.setOnItemChildClickListener(this);
        adapter.openLoadAnimation();
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.shape_recycleview_divider));
        rvDevices.addItemDecoration(itemDecoration);
        rvDevices.setAdapter(adapter);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMokoService = ((MokoService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_ORDER_RESULT);
            filter.addAction(MokoConstants.ACTION_ORDER_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_ORDER_FINISH);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.setPriority(100);
            registerReceiver(mReceiver, filter);
            mReceiverTag = true;
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                // 蓝牙未打开，开启蓝牙
                MokoSupport.getInstance().enableBluetooth();
            } else {
                if (animation == null) {
                    startScan();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private String unLockResponse;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {

                }
                if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {

                }
                if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                    OrderTaskResponse response = (OrderTaskResponse) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK);
                    OrderType orderType = response.orderType;
                    int responseType = response.responseType;
                    byte[] value = response.responseValue;
                    switch (orderType) {
                        case lockState:
                            String valueStr = MokoUtils.bytesToHexString(value);
                            if ("00".equals(valueStr)) {
                                dismissLoadingMessageDialog();
                                if (TextUtils.isEmpty(unLockResponse)) {
                                    MokoSupport.getInstance().disConnectBle();
                                    // 弹出密码框
                                    final PasswordDialog dialog = new PasswordDialog(MainActivity.this);
                                    dialog.setData(mSavedPassword);
                                    dialog.setOnPasswordClicked(new PasswordDialog.PasswordClickListener() {
                                        @Override
                                        public void onEnsureClicked(String password) {
                                            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                                                // 蓝牙未打开，开启蓝牙
                                                MokoSupport.getInstance().enableBluetooth();
                                                return;
                                            }
                                            LogModule.i(password);
                                            mPassword = password;
                                            if (animation != null) {
                                                mMokoService.mHandler.removeMessages(0);
                                                MokoSupport.getInstance().stopScanDevice();
                                            }
                                            mMokoService.connectBluetoothDevice(mSelectedBeaconXMac);
                                            showLoadingProgressDialog();
                                        }

                                        @Override
                                        public void onDismiss() {
                                            unLockResponse = "";
                                            MokoSupport.getInstance().disConnectBle();
                                        }
                                    });
                                    dialog.show();
                                    Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {

                                        @Override
                                        public void run() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    dialog.showKeyboard();
                                                }
                                            });
                                        }
                                    }, 200);
                                } else {
                                    dismissLoadingMessageDialog();
                                    unLockResponse = "";
                                    MokoSupport.getInstance().disConnectBle();
                                    ToastUtils.showToast(MainActivity.this, "Password error");
                                }
                            } else if ("02".equals(valueStr)) {
                                // 不需要密码验证
                                dismissLoadingMessageDialog();
                                Intent deviceInfoIntent = new Intent(MainActivity.this, DeviceInfoActivity.class);
                                deviceInfoIntent.putExtra(AppConstants.EXTRA_KEY_PASSWORD, mPassword);
                                startActivityForResult(deviceInfoIntent, AppConstants.REQUEST_CODE_DEVICE_INFO);
                            } else {
                                // 解锁成功
                                dismissLoadingMessageDialog();
                                LogModule.i("解锁成功");
                                unLockResponse = "";
                                mSavedPassword = mPassword;
                                Intent deviceInfoIntent = new Intent(MainActivity.this, DeviceInfoActivity.class);
                                deviceInfoIntent.putExtra(AppConstants.EXTRA_KEY_PASSWORD, mPassword);
                                startActivityForResult(deviceInfoIntent, AppConstants.REQUEST_CODE_DEVICE_INFO);
                            }
                            break;
                        case unLock:
                            if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                                unLockResponse = MokoUtils.bytesToHexString(value);
                                LogModule.i("返回的随机数：" + unLockResponse);
                                MokoSupport.getInstance().sendOrder(mMokoService.setConfigNotify(), mMokoService.setUnLock(mPassword, value));
                            }
                            if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                                MokoSupport.getInstance().sendOrder(mMokoService.getLockState());
                            }
                            break;
                    }
                }
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            if (animation != null) {
                                mMokoService.mHandler.removeMessages(0);
                                MokoSupport.getInstance().stopScanDevice();
                            }
                            break;
                        case BluetoothAdapter.STATE_ON:
                            if (animation == null) {
                                startScan();
                            }
                            break;

                    }
                }
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
            mPassword = "";
            // 设备断开，通知页面更新
            dismissLoadingProgressDialog();
            dismissLoadingMessageDialog();
            ToastUtils.showToast(MainActivity.this, "Disconnected");
            if (animation == null) {
                startScan();
            }
        }
        if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
            // 设备连接成功，通知页面更新
            dismissLoadingProgressDialog();
            HashMap<OrderType, MokoCharacteristic> map = MokoCharacteristicHandler.getInstance().mokoCharacteristicMap;
            if (map.containsKey(OrderType.deviceType)) {
                showLoadingMessageDialog();
                mMokoService.mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(mPassword)) {
                            MokoSupport.getInstance().sendOrder(mMokoService.getLockState());
                        } else {
                            LogModule.i("锁定状态，获取unLock，解锁");
                            MokoSupport.getInstance().sendOrder(mMokoService.getUnLock());
                        }
                    }
                }, 1000);

            } else {
                MokoSupport.getInstance().disConnectBle();
                AlertMessageDialog dialog = new AlertMessageDialog();
                dialog.setMessage("Oops! Something went wrong. Please check the device version or contact MOKO.");
                dialog.show(getSupportFragmentManager());

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case AppConstants.REQUEST_CODE_DEVICE_INFO:
                    if (animation == null) {
                        startScan();
                    }
                    break;

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        unbindService(mServiceConnection);
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onStartScan() {
        beaconXInfoHashMap.clear();
    }

    private BeaconXInfoParseableImpl beaconXInfoParseable;

    @Override
    public void onScanDevice(DeviceInfo deviceInfo) {
        BeaconXInfo beaconXInfo = beaconXInfoParseable.parseDeviceInfo(deviceInfo);
        if (beaconXInfo == null)
            return;
        beaconXInfoHashMap.put(beaconXInfo.mac, beaconXInfo);
        updateDevices();
    }

    @Override
    public void onStopScan() {
        findViewById(R.id.iv_refresh).clearAnimation();
        animation = null;
        updateDevices();
    }

    private void updateDevices() {
        beaconXInfos.clear();
        if (!TextUtils.isEmpty(filterName) || filterRssi != -127) {
            ArrayList<BeaconXInfo> beaconXInfosFilter = new ArrayList<>(beaconXInfoHashMap.values());
            Iterator<BeaconXInfo> iterator = beaconXInfosFilter.iterator();
            while (iterator.hasNext()) {
                BeaconXInfo beaconXInfo = iterator.next();
                if (beaconXInfo.rssi > filterRssi) {
                    if (TextUtils.isEmpty(filterName)) {
                        continue;
                    } else {
                        if (TextUtils.isEmpty(beaconXInfo.name) && TextUtils.isEmpty(beaconXInfo.mac)) {
                            iterator.remove();
                        } else if (TextUtils.isEmpty(beaconXInfo.name) && beaconXInfo.mac.toLowerCase().replaceAll(":", "").contains(filterName.toLowerCase())) {
                            continue;
                        } else if (TextUtils.isEmpty(beaconXInfo.mac) && beaconXInfo.name.toLowerCase().contains(filterName.toLowerCase())) {
                            continue;
                        } else if (!TextUtils.isEmpty(beaconXInfo.name) && !TextUtils.isEmpty(beaconXInfo.mac) && (beaconXInfo.name.toLowerCase().contains(filterName.toLowerCase()) || beaconXInfo.mac.toLowerCase().replaceAll(":", "").contains(filterName.toLowerCase()))) {
                            continue;
                        } else {
                            iterator.remove();
                        }
                    }
                } else {
                    iterator.remove();
                }
            }
            beaconXInfos.addAll(beaconXInfosFilter);
        } else {
            beaconXInfos.addAll(beaconXInfoHashMap.values());
        }
        Collections.sort(beaconXInfos, new Comparator<BeaconXInfo>() {
            @Override
            public int compare(BeaconXInfo lhs, BeaconXInfo rhs) {
                if (lhs.rssi > rhs.rssi) {
                    return -1;
                } else if (lhs.rssi < rhs.rssi) {
                    return 1;
                }
                return 0;
            }
        });
        adapter.replaceData(beaconXInfos);
        tvDeviceNum.setText(String.format("Devices(%d)", beaconXInfos.size()));
    }

    private Animation animation = null;
    public String filterName;
    public int filterRssi = -127;

    @OnClick({R.id.iv_refresh, R.id.iv_about, R.id.rl_edit_filter, R.id.rl_filter, R.id.iv_filter_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_refresh:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    // 蓝牙未打开，开启蓝牙
                    MokoSupport.getInstance().enableBluetooth();
                    return;
                }
                if (animation == null) {
                    startScan();
                } else {
                    mMokoService.mHandler.removeMessages(0);
                    MokoSupport.getInstance().stopScanDevice();
                }
                break;
            case R.id.iv_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.rl_edit_filter:
            case R.id.rl_filter:
                if (animation != null) {
                    mMokoService.mHandler.removeMessages(0);
                    MokoSupport.getInstance().stopScanDevice();
                }
                ScanFilterDialog scanFilterDialog = new ScanFilterDialog(this);
                scanFilterDialog.setFilterName(filterName);
                scanFilterDialog.setFilterRssi(filterRssi);
                scanFilterDialog.setOnScanFilterListener(new ScanFilterDialog.OnScanFilterListener() {
                    @Override
                    public void onDone(String filterName, int filterRssi) {
                        MainActivity.this.filterName = filterName;
                        MainActivity.this.filterRssi = filterRssi;
                        if (!TextUtils.isEmpty(filterName) || filterRssi != -127) {
                            rl_filter.setVisibility(View.VISIBLE);
                            rl_edit_filter.setVisibility(View.GONE);
                            StringBuilder stringBuilder = new StringBuilder();
                            if (!TextUtils.isEmpty(filterName)) {
                                stringBuilder.append(filterName);
                                stringBuilder.append(";");
                            }
                            if (filterRssi != -127) {
                                stringBuilder.append(String.format("%sdBm", filterRssi + ""));
                                stringBuilder.append(";");
                            }
                            tv_filter.setText(stringBuilder.toString());
                        } else {
                            rl_filter.setVisibility(View.GONE);
                            rl_edit_filter.setVisibility(View.VISIBLE);
                        }
                        if (animation == null) {
                            startScan();
                        }
                    }
                });
                scanFilterDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (animation == null) {
                            startScan();
                        }
                    }
                });
                scanFilterDialog.show();
                break;
            case R.id.iv_filter_delete:
                if (animation != null) {
                    mMokoService.mHandler.removeMessages(0);
                    MokoSupport.getInstance().stopScanDevice();
                }
                rl_filter.setVisibility(View.GONE);
                rl_edit_filter.setVisibility(View.VISIBLE);
                filterName = "";
                filterRssi = -127;
                if (animation == null) {
                    startScan();
                }
                break;
        }
    }

    private void startScan() {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
        findViewById(R.id.iv_refresh).startAnimation(animation);
        beaconXInfoParseable = new BeaconXInfoParseableImpl();
        MokoSupport.getInstance().startScanDevice(this);
        mMokoService.mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MokoSupport.getInstance().stopScanDevice();
            }
        }, 1000 * 60);
    }


    private LoadingDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new LoadingDialog();
        mLoadingDialog.show(getSupportFragmentManager());

    }

    private void dismissLoadingProgressDialog() {
        if (mLoadingDialog != null)
            mLoadingDialog.dismissAllowingStateLoss();
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    private void showLoadingMessageDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Verifying..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    private void dismissLoadingMessageDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage(R.string.main_exit_tips);
        dialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
            @Override
            public void onClick() {
                MainActivity.this.finish();
            }
        });
        dialog.show(getSupportFragmentManager());
    }

    private String mPassword;
    private String mSavedPassword;
    private String mSelectedBeaconXMac;

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        BeaconXInfo beaconXInfo = (BeaconXInfo) adapter.getItem(position);
        if (beaconXInfo != null && !isFinishing()) {
            if (animation != null) {
                mMokoService.mHandler.removeMessages(0);
                MokoSupport.getInstance().stopScanDevice();
            }
            mMokoService.connectBluetoothDevice(beaconXInfo.mac);
            mSelectedBeaconXMac = beaconXInfo.mac;
            showLoadingProgressDialog();
        }
    }
}
