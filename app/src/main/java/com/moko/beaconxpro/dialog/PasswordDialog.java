package com.moko.beaconxpro.dialog;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.utils.ToastUtils;

import butterknife.Bind;
import butterknife.OnClick;

public class PasswordDialog extends BaseDialog<String> {
    @Bind(R.id.et_password)
    EditText etPassword;
    private final String FILTER_ASCII = "\\A\\p{ASCII}*\\z";

    public PasswordDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_password;
    }

    @Override
    protected void renderConvertView(View convertView, String password) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        etPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), filter});
        if (!TextUtils.isEmpty(password)) {
            etPassword.setText(password);
            etPassword.setSelection(password.length());
        }
    }

    @OnClick({R.id.tv_password_cancel, R.id.tv_password_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_password_cancel:
                dismiss();
                if (passwordClickListener != null) {
                    passwordClickListener.onDismiss();
                }
                break;
            case R.id.tv_password_ensure:
                dismiss();
                if (TextUtils.isEmpty(etPassword.getText().toString())) {
                    ToastUtils.showToast(getContext(), getContext().getString(R.string.password_null));
                    return;
                }
                if (passwordClickListener != null)
                    passwordClickListener.onEnsureClicked(etPassword.getText().toString());
                break;
        }
    }

    private PasswordClickListener passwordClickListener;

    public void setOnPasswordClicked(PasswordClickListener passwordClickListener) {
        this.passwordClickListener = passwordClickListener;
    }

    public interface PasswordClickListener {

        void onEnsureClicked(String password);

        void onDismiss();
    }

    public void showKeyboard() {
        if (etPassword != null) {
            //设置可获得焦点
            etPassword.setFocusable(true);
            etPassword.setFocusableInTouchMode(true);
            //请求获得焦点
            etPassword.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) etPassword
                    .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(etPassword, 0);
        }
    }
}
