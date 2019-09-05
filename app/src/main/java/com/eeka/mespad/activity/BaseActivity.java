package com.eeka.mespad.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.eeka.mespad.R;
import com.eeka.mespad.fragment.LoginFragment;
import com.eeka.mespad.http.HttpCallback;
import com.eeka.mespad.http.HttpHelper;
import com.eeka.mespad.utils.ToastUtil;
import com.eeka.mespad.view.dialog.ErrorDialog;
import com.eeka.mespad.view.dialog.LoadingDialog;

/**
 * Activity基类
 * Created by Lenovo on 2017/5/13.
 */

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements View.OnClickListener, HttpCallback, LoginFragment.OnLoginCallback, LoginFragment.OnClockCallback {

    public static int REQUEST_PERMISSION = 998;

    protected Context mContext;

    protected FragmentManager mFragmentManager;
    protected Dialog mLoginDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;
        mFragmentManager = getSupportFragmentManager();

    }

    protected void initView() {
    }

    protected void initData() {
    }

    /**
     * 刷卡获取卡信息
     */
    public void getCardInfo(String orderNum) {
        showLoading();
        HttpHelper.getCardInfo(orderNum, this);
    }

    /**
     * 刷卡上岗
     */
    public void clockIn(String cardNum) {
        HttpHelper.positionLogin(cardNum, this);
    }

    /**
     * 刷卡上岗
     */
    public void clockOut(String cardNum) {
        HttpHelper.positionLogout(cardNum, this);
    }

    protected boolean isEmpty(String str) {
        return TextUtils.isEmpty(str);
    }

    protected void showLoading() {
        showLoading(getString(R.string.loading), true);
    }

    protected void showLoading(String msg, boolean cancelAble) {
        LoadingDialog.show(mContext, msg);
    }

    protected void dismissLoading() {
        LoadingDialog.dismiss();
    }

    protected void showErrorDialog(String msg) {
        ErrorDialog.showAlert(mContext, msg);
    }

    protected void showAlert(String msg) {
        ErrorDialog.showAlert(mContext, msg, ErrorDialog.TYPE.ALERT, null, false);
    }

    protected void toast(String msg) {
        toast(msg, Toast.LENGTH_LONG);
    }

    protected void toast(String msg, int duration) {
        ToastUtil.showToast(this, msg, duration);
    }

    @Override
    public void onClick(View v) {
    }

    protected boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void requestPermission(String[] permissions) {
        requestPermissions(permissions, REQUEST_PERMISSION);
    }

    protected boolean allowAllPermission;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            allowAllPermission = false;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allowAllPermission = false;
                    break;
                }
                allowAllPermission = true;
            }
            if (!allowAllPermission) {
                Toast.makeText(mContext, "该功能需要授权方可使用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 显示登录弹框
     */
    public void showLoginDialog() {
        mLoginDialog = new Dialog(mContext);
        mLoginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoginDialog.setContentView(R.layout.dlg_login);

        final LoginFragment loginFragment = (LoginFragment) mFragmentManager.findFragmentById(R.id.loginFragment);
        assert loginFragment != null;
        loginFragment.setOnClockCallback(this);

        mLoginDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mFragmentManager.beginTransaction().remove(loginFragment).commit();
            }
        });
        mLoginDialog.show();
    }

    @Override
    public void onSuccess(String url, JSONObject resultJSON) {
        dismissLoading();
        if (!HttpHelper.isSuccess(resultJSON)) {
            showErrorDialog(resultJSON.getString("message"));
        }
    }

    @Override
    public void onFailure(String url, int code, String message) {
        dismissLoading();
        ErrorDialog.showAlert(mContext, message);
    }

    @Override
    public void onLogin(boolean success) {
    }

    @Override
    public void onClockIn(boolean success) {
    }

}
