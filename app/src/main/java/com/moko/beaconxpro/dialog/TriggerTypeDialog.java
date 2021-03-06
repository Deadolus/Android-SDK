package com.moko.beaconxpro.dialog;

import android.text.TextUtils;
import android.view.View;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.view.WheelView;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TriggerTypeDialog extends MokoBaseDialog {
    @Bind(R.id.wv_trigger_type)
    WheelView wvTriggerType;
    private int selected;
    private String[] datas;

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_trigger_type;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);
        wvTriggerType.setData(new ArrayList<>(Arrays.asList(datas)));
        wvTriggerType.setDefault(selected);
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.tv_cancel, R.id.tv_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_confirm:
                dismiss();
                if (TextUtils.isEmpty(wvTriggerType.getSelectedText())) {
                    return;
                }
                if (wvTriggerType.getSelected() < 0) {
                    return;
                }
                if (listener != null) {
                    listener.onDataSelected(wvTriggerType.getSelected());
                }
                break;
        }
    }

    private OnDataSelectedListener listener;

    public void setListener(OnDataSelectedListener listener) {
        this.listener = listener;
    }

    public void setTriggerArray(String[] triggerArray) {
        datas = triggerArray;
    }

    public interface OnDataSelectedListener {
        void onDataSelected(int data);
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }
}
