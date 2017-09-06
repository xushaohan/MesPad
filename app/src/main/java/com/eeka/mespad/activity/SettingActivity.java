package com.eeka.mespad.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.eeka.mespad.R;
import com.eeka.mespad.bo.PushJson;
import com.eeka.mespad.manager.UpdateManager;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Lenovo on 2017/9/4.
 */

public class SettingActivity extends BaseActivity {

    private static final int REQUEST_LOGIN = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_setting);

        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        findViewById(R.id.tv_setLoginUser).setOnClickListener(this);
        findViewById(R.id.tv_checkUpdate).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_setLoginUser:
                startActivityForResult(new Intent(mContext, LoginActivity.class), REQUEST_LOGIN);
                break;
            case R.id.tv_checkUpdate:
                UpdateManager.downloadApk(mContext);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_LOGIN) {
                toast("设置成功，正刷新数据");
                PushJson push = new PushJson();
                push.setType(PushJson.TYPE_RELOGIN);
                EventBus.getDefault().post(push);
                startActivity(new Intent(mContext, MainActivity.class));
                finish();
            }
        }
    }
}