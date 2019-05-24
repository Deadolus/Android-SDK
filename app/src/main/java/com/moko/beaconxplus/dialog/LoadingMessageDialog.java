package com.moko.beaconxplus.dialog;

import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.moko.beaconxplus.R;
import com.moko.beaconxplus.view.ProgressDrawable;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoadingMessageDialog extends MokoBaseDialog {
    private static final int DIALOG_DISMISS_DELAY_TIME = 5000;
    public static final String TAG = LoadingMessageDialog.class.getSimpleName();
    @Bind(R.id.iv_loading)
    ImageView ivLoading;
    @Bind(R.id.tv_loading_message)
    TextView tvLoadingMessage;

    private String message;

    private int messageId = -1;

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_loading_message;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);
        ProgressDrawable progressDrawable = new ProgressDrawable();
        progressDrawable.setColor(ContextCompat.getColor(getContext(), R.color.text_black_4d4d4d));
        ivLoading.setImageDrawable(progressDrawable);
        progressDrawable.start();
        if (messageId > 0) {
            message = getString(messageId);
        }
        if (TextUtils.isEmpty(message)) {
            message  = getString(R.string.setting_syncing);
        }
        tvLoadingMessage.setText(message);
        tvLoadingMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isVisible()) {
                    dismissAllowingStateLoss();
                    if (callback != null) {
                        callback.onOvertimeDismiss();
                    }
                }
            }
        }, DIALOG_DISMISS_DELAY_TIME);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (callback != null) {
            callback.onDismiss();
        }
    }

    @Override
    public int getDialogStyle() {
        return R.style.CenterDialog;
    }

    @Override
    public int getGravity() {
        return Gravity.CENTER;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @Override
    public boolean getCancelOutside() {
        return false;
    }

    @Override
    public boolean getCancellable() {
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ProgressDrawable) ivLoading.getDrawable()).stop();
        ButterKnife.unbind(this);
    }



    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessage(@StringRes int messageId) {
        this.messageId = messageId;
    }

    private DialogDissmissCallback callback;

    public void setDialogDismissCallback(final DialogDissmissCallback callback) {
        this.callback = callback;
    }

    public interface DialogDissmissCallback {
        void onOvertimeDismiss();

        void onDismiss();
    }
}
