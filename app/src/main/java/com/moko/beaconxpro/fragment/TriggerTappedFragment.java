package com.moko.beaconxpro.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.activity.SlotDataActivity;
import com.moko.beaconxpro.utils.ToastUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TriggerTappedFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "TappedFragment";


    @Bind(R.id.tv_trigger_tips)
    TextView tvTriggerTips;
    @Bind(R.id.rb_always_start)
    RadioButton rbAlwaysStart;
    @Bind(R.id.rb_start_advertising)
    RadioButton rbStartAdvertising;
    @Bind(R.id.rb_stop_advertising)
    RadioButton rbStopAdvertising;
    @Bind(R.id.rg_tapped)
    RadioGroup rgTapped;
    @Bind(R.id.et_start)
    EditText etStart;
    @Bind(R.id.et_stop)
    EditText etStop;


    private SlotDataActivity activity;


    public TriggerTappedFragment() {
    }

    public static TriggerTappedFragment newInstance() {
        TriggerTappedFragment fragment = new TriggerTappedFragment();
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
        View view = inflater.inflate(R.layout.fragment_trigger_tapped, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        if (mDuration == 0) {
            if (mIsStart) {
                rbAlwaysStart.setChecked(true);
            }
        } else {
            if (mIsStart) {
                rbStartAdvertising.setChecked(true);
                etStart.setText(mDuration + "");
                etStart.setSelection((mDuration + "").length());
            } else {
                rbStopAdvertising.setChecked(true);
                etStop.setText(mDuration + "");
                etStop.setSelection((mDuration + "").length());
            }
        }
        rgTapped.setOnCheckedChangeListener(this);
        updateTips();
        etStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String duration = s.toString();
                if (rbStartAdvertising.isChecked() && !TextUtils.isEmpty(duration)) {
                    mDuration = Integer.parseInt(duration);
                    tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_2, "start", String.format("%ds", mDuration), mIsDouble ? "double" : "triple"));
                }
            }
        });
        etStop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String duration = s.toString();
                if (rbStopAdvertising.isChecked() && !TextUtils.isEmpty(duration)) {
                    mDuration = Integer.parseInt(duration);
                    tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_2, "stop", String.format("%ds", mDuration), mIsDouble ? "double" : "triple"));
                }
            }
        });
        return view;
    }

    public void updateTips() {
        if (rbAlwaysStart.isChecked()) {
            tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_1, mIsDouble ? "double" : "triple"));
        } else if (rbStartAdvertising.isChecked()) {
            mDuration = Integer.parseInt(etStart.getText().toString());
            tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_2, "start", String.format("%ds", mDuration), mIsDouble ? "double" : "triple"));
        } else {
            mDuration = Integer.parseInt(etStop.getText().toString());
            tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_2, "stop", String.format("%ds", mDuration), mIsDouble ? "double" : "triple"));
        }
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

    private boolean mIsStart = true;
    private int mDuration = 30;
    private boolean mIsDouble = true;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_always_start:
                mIsStart = true;
                tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_1, mIsDouble ? "double" : "triple"));
                break;
            case R.id.rb_start_advertising:
                mIsStart = true;
                String startDuration = etStart.getText().toString();
                if (TextUtils.isEmpty(startDuration)) {
                    mDuration = 0;
                } else {
                    mDuration = Integer.parseInt(startDuration);
                }
                tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_2, "start", String.format("%ds", mDuration), mIsDouble ? "double" : "triple"));
                break;
            case R.id.rb_stop_advertising:
                mIsStart = false;
                String stopDuration = etStop.getText().toString();
                if (TextUtils.isEmpty(stopDuration)) {
                    mDuration = 0;
                } else {
                    mDuration = Integer.parseInt(stopDuration);
                }
                tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_2, "stop", String.format("%ds", mDuration), mIsDouble ? "double" : "triple"));
                break;
        }
    }

    public void setStart(boolean isStart) {
        mIsStart = isStart;
    }

    public boolean isStart() {
        return mIsStart;
    }


    public void setData(int data) {
        mDuration = data;
    }

    public int getData() {
        String duration = "";
        if (rbStartAdvertising.isChecked()) {
            duration = etStart.getText().toString();
        } else if (rbStopAdvertising.isChecked()) {
            duration = etStop.getText().toString();
        } else {
            duration = "0";
        }
        if (TextUtils.isEmpty(duration)) {
            ToastUtils.showToast(getActivity(), "The advertising can not be empty.");
            return -1;
        }
        mDuration = Integer.parseInt(duration);
        if ((rbStartAdvertising.isChecked() || rbStopAdvertising.isChecked()) && (mDuration < 1 || mDuration > 65535)) {
            ToastUtils.showToast(activity, "The advertising range is 1~65535");
            return -1;
        }
        return mDuration;
    }

    public boolean isDouble() {
        return mIsDouble;
    }

    public void setIsDouble(boolean isDouble) {
        this.mIsDouble = isDouble;
    }
}
