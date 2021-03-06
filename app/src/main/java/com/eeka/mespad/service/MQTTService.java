package com.eeka.mespad.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.eeka.mespad.PadApplication;
import com.eeka.mespad.R;
import com.eeka.mespad.bo.PushJson;
import com.eeka.mespad.http.HttpHelper;
import com.eeka.mespad.manager.Logger;
import com.eeka.mespad.utils.NetUtil;
import com.eeka.mespad.utils.SystemUtils;
import com.eeka.mespad.view.dialog.ErrorDialog;
import com.tencent.bugly.beta.Beta;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.finalteam.okhttpfinal.LogUtil;

/**
 * mqtt 推送控制服务
 * Created by Lenovo on 2017/10/11.
 */

public class MQTTService extends Service {

    private static final String MQTT_PORT = "1883"; // Broker Port
    private static final String MQTT_URL_FORMAT = "tcp://%s:%s"; // URL Format normally don't change
    private String[] myTopic = new String[2];
    private String mClientId = null;
    private ScheduledExecutorService scheduler;
    private MqttAndroidClient mqttClient;
    private String userName = "admin"; // 连接的用户名
    private String passWord = "admin"; //连接的密码
    private ConnectivityManager mConnectivityManager; // To check for connectivity changes
    private MqttConnectOptions options;

    private static Context mContext;

    public static void actionStart(Context ctx) {
        mContext = ctx;
        Intent intent = new Intent(ctx, MQTTService.class);
        ctx.startService(intent);
    }

    public static void actionStop(Context ctx) {
        Intent intent = new Intent(ctx, MQTTService.class);
        ctx.stopService(intent);
    }

    @Override
    public void onDestroy() {
        stopConnect();
        super.onDestroy();
    }

    private void stopConnect() {
        try {
            unregisterReceiver(mConnectivityReceiver);
            mqttClient.unsubscribe(myTopic);
            mqttClient.unregisterResources();
            mqttClient.disconnect();
            Logger.d("MQTT 断开连接");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e("MQTT 断开连接异常");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        registerReceiver(mConnectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 初始化相关数据
     */
    public void init() {
        try {
            myTopic[0] = getString(R.string.app_channel);
            myTopic[1] = HttpHelper.getPadIp();
            //clientId要唯一，不然会挤掉另外相同的clientId的连接
            mClientId = SystemUtils.getIMEI(this);
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            String url = String.format(Locale.US, MQTT_URL_FORMAT, PadApplication.MQTT_BROKER, MQTT_PORT);
            mqttClient = new MqttAndroidClient(this, url, mClientId);
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*value秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(60);
            //设置自动重连
//            options.setAutomaticReconnect(true);
//            mqttAsyncClient.connect(connOpts).waitForCompletion();
            //设置回调
            mqttClient.setCallback(mqttCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
        startReconnect();
    }

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void connectionLost(Throwable cause) {
            //连接丢失后，一般在这里面进行重连
            if (cause != null) {
                Logger.d("mqtt connectionLost,cause:" + cause.getMessage());
                LogUtil.writeToFile(LogUtil.LOGTYPE_MQTT_STATUS, "mqtt connectionLost,cause:" + cause.getMessage());
            } else {
                Logger.d("mqtt connectionLost,cause:null");
                LogUtil.writeToFile(LogUtil.LOGTYPE_MQTT_STATUS, "mqtt connectionLost,cause:null");
            }

            startReconnect();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            //收到消息推送时会回调
            String str1 = new String(message.getPayload());
            Logger.d("MQTT收到推送->" + "message:" + str1);
            LogUtil.writeToFile(LogUtil.LOGTYPE_MQTT, str1);

            if ("update".equals(str1)) {
                //http://10.7.121.40:8161/admin/topics.jsp手动推送，APP更新
                Beta.checkUpgrade();
            } else {
                //ME系统推送
                PushJson pushJson = JSON.parseObject(str1, PushJson.class);
                if (!"0".equals(pushJson.getCode())) {
                    pushJson.setType(PushJson.TYPE_ALERT_AUTOCANCEL);
                }
                EventBus.getDefault().post(pushJson);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //publish后会执行到这里
            long messageId = token.getMessageId();
            Logger.d("messageId=:" + messageId);
        }
    };

    /**
     * Query's the NetworkInfo via ConnectivityManager
     * to return the current connected state
     * 通过ConnectivityManager查询网络连接状态
     *
     * @return boolean true if we are connected false otherwise
     * 如果网络状态正常则返回true反之flase
     */
    private boolean isNetworkAvailable() {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * Checkes the current connectivity
     * and reconnects if it is required.
     * 重新连接如果他是必须的
     */
    public synchronized void reconnectIfNecessary() {
        if (mqttClient != null && !mqttClient.isConnected()) {
            connect();
        }
    }

    private void connect() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    LogUtil.writeToFile(LogUtil.LOGTYPE_MQTT_STATUS, "connect..." + connectCount);
                    mqttClient.connect(options, null, iMqttActionListener);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.writeToFile(LogUtil.LOGTYPE_MQTT_STATUS, "connect exception：" + e.getMessage());
                }
            }
        }).start();
    }

    // MQTT是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken token) {
            Logger.d("MQTT 连接成功");
            scheduler.shutdownNow();
            try {
                // 订阅myTopic话题
                int[] qos = {0, 0};
                mqttClient.subscribe(myTopic, qos);
                LogUtil.writeToFile(LogUtil.LOGTYPE_MQTT_STATUS, "connected and subscribe success");
                Logger.d("MQTT 订阅[" + myTopic[0] + "," + myTopic[1] + "]成功");
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e("MQTT 订阅异常 " + e.getMessage());
                LogUtil.writeToFile(LogUtil.LOGTYPE_MQTT_STATUS, "connected and subscribe exception：" + e.getMessage());
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            String message = null;
            if (exception != null) {
                message = exception.getMessage();
            }
            Logger.e("连接失败,exception:" + message);
            startReconnect();
        }
    };

    int connectCount;

    /**
     * 调用init() 方法之后，调用此方法。
     */
    public void startReconnect() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        connectCount = 0;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                connectCount++;
                boolean networkStatus = isNetworkAvailable();
                LogUtil.writeToFile(LogUtil.LOGTYPE_MQTT_STATUS, "network status = " + networkStatus);
                boolean pingStatus = SystemUtils.pingIpAddress(PadApplication.MQTT_BROKER);
                LogUtil.writeToFile(LogUtil.LOGTYPE_MQTT_STATUS, "ping " + PadApplication.MQTT_BROKER + " status = " + pingStatus);
                if (!mqttClient.isConnected() && networkStatus) {
                    connect();
                }
            }
        }, 0, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    private long mLastNetworkUnAvailableMills;

    /**
     * Receiver that listens for connectivity chanes
     * via ConnectivityManager
     * 网络状态发生变化接收器
     */
    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isNetworkAvailable()) {
                Logger.d("无网络连接...");
                //处理网络断开时收到两次广播的情况
                long curMills = System.currentTimeMillis();
                if (curMills - mLastNetworkUnAvailableMills > 2000) {
                    mLastNetworkUnAvailableMills = curMills;
                    Toast.makeText(context, "网络不可用", Toast.LENGTH_SHORT).show();
                    LogUtil.writeToFile(LogUtil.LOGTYPE_MQTT_STATUS, "network lost");
                    if (scheduler != null) {
                        scheduler.shutdownNow();
                    }
                }
            } else {
                LogUtil.writeToFile(LogUtil.LOGTYPE_MQTT_STATUS, "network connected");
                Logger.d("网络已连接...");
                if (mqttClient == null) {
                    init();
                } else {
                    startReconnect();
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
