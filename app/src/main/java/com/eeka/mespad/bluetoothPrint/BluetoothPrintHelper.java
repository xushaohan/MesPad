package com.eeka.mespad.bluetoothPrint;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.widget.Toast;

import com.eeka.mespad.utils.SystemUtils;
import com.eeka.mespad.utils.ToastUtil;
import com.eeka.mespad.view.dialog.ErrorDialog;

import java.util.Set;

import zpSDK.zpSDK.zpSDK;

public class BluetoothPrintHelper {

    public static void Print(Activity context, String Pdata) {
        if (TextUtils.isEmpty(Pdata)) {
            ErrorDialog.showAlert(context, "请输入要打印的内容");
            return;
        }
        if (!OpenPrinter(context)) {
            return;
        }

        zpSDK.zp_page_clear();
        if (!zpSDK.zp_page_create(70, 10)) {
            ToastUtil.showToast(context, "无法创建打印纸，请更换打印纸", Toast.LENGTH_LONG);
            return;
        }
//        int mid = Pdata.length() / 2;
//        String str1 = Pdata.substring(0, mid);
//        String str2 = Pdata.substring(mid, Pdata.length());
        zpSDK.zp_draw_text_ex(1, 3, Pdata, "宋体", 3, 0, false, false, false);
        zpSDK.zp_draw_text_ex(25, 3, Pdata, "宋体", 3, 0, false, false, false);
        zpSDK.zp_draw_text_ex(49, 3, Pdata, "宋体", 3, 0, false, false, false);
//        zpSDK.zp_draw_barcode(5, 15, Pdata, zpSDK.BARCODE_TYPE.BARCODE_CODE128, 8, 2, 0);

        zpSDK.zp_page_print(false);
        zpSDK.zp_goto_mark_label(4);//走纸

        zpSDK.zp_close();
    }

    private static boolean OpenPrinter(Activity context) {
        BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            ToastUtil.showToast(context, "该设备不支持蓝牙功能", Toast.LENGTH_LONG);
            return false;
        }

        if (!myBluetoothAdapter.isEnabled()) {
            ToastUtil.showToast(context, "请打开蓝牙开关,并配对蓝牙打印机K319...", Toast.LENGTH_LONG);
            SystemUtils.startBluetoothSettingView(context);
            return false;
        }

        Set<BluetoothDevice> bondedDevices = myBluetoothAdapter.getBondedDevices();
        if (bondedDevices.size() <= 0) {
            ToastUtil.showToast(context, "未配对蓝牙打印机,请配对K319...", Toast.LENGTH_LONG);
//            SystemUtils.startBluetoothSettingView(context);
            return false;
        }
        BluetoothDevice myDevice = null;
        for (BluetoothDevice device : bondedDevices) {
            if (device.getName().contains("K319")) {
                myDevice = device;
                break;
            }
        }
        if (myDevice == null) {
            ToastUtil.showToast(context, "未配对蓝牙打印机,请配对K319...", Toast.LENGTH_LONG);
//            SystemUtils.startBluetoothSettingView(context);
            return false;
        }
        if (!zpSDK.zp_open(myBluetoothAdapter, myDevice)) {
            if (!zpSDK.zp_open(myBluetoothAdapter, myDevice)) {
                ToastUtil.showToast(context, zpSDK.ErrorMessage, Toast.LENGTH_LONG);
                return false;
            }
        }
        return true;
    }

}
