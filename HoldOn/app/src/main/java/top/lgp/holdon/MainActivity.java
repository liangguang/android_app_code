package top.lgp.holdon;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.recog.RecogResult;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import top.lgp.holdon.untils.MyCountDownTimer;

public class MainActivity extends AppCompatActivity  implements EventListener {

    private TextView mTextMessage;

    private MyCountDownTimer timer;
    private final long TIME = 60 * 1000L;
    private final long INTERVAL = 25L;

    public  int clickNum = 0;


    private EventManager asr;

    protected boolean enableOffline = true; // 是否离线命令词

    private TextView audioResult;

    public void changState(View view){

        TextView timeView = (TextView)findViewById(R.id.show_time);
        TextView msgView = (TextView)findViewById(R.id.show_msg);
        msgView.setVisibility(View.GONE);
        msgView.setText("SO GOOD!");
        if(timer == null){
            timer = new MyCountDownTimer(TIME,INTERVAL,timeView,msgView);
        }
        clickNum ++;
        if(clickNum == 1){
            timer.start();
            view.setBackgroundResource(R.drawable.circular_layout_two);
        }else if(clickNum == 2){
            cancelTimer();
            msgView.setVisibility(View.VISIBLE);
            double d = Double.parseDouble(timeView.getText().toString());
            if( d > 10.2 || d < 9.8){
                msgView.setText("Loser!!!");
            }
            view.setBackgroundResource(R.drawable.circular_layout_three);
        }else if(clickNum == 3){
            timeView.setText(R.string.initTime);
            view.setBackgroundResource(R.drawable.circular_layout);
            clickNum = 0;
        }

    }

    /**
     * 取消倒计时
     */
    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        if (enableOffline) {
            unloadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
        }

        // 必须与registerListener成对出现，否则可能造成内存泄露
        asr.unregisterListener(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        Log.i("ActivityMiniRecog","On pause");
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            findViewById(R.id.tenSecond).setVisibility(View.GONE);
            findViewById(R.id.show_time).setVisibility(View.GONE);
            findViewById(R.id.show_msg).setVisibility(View.GONE);
            findViewById(R.id.audio_layout).setVisibility(View.GONE);
            findViewById(R.id.face_layout).setVisibility(View.GONE);
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    findViewById(R.id.tenSecond).setVisibility(View.VISIBLE);
                    findViewById(R.id.show_time).setVisibility(View.VISIBLE);
                    findViewById(R.id.show_msg).setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_dashboard:
                    //startActivity(new Intent(MainActivity.this,AudioRecognitionActivity.class));
                    mTextMessage.setText(R.string.title_dashboard);
                    findViewById(R.id.audio_layout).setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    startActivity(new Intent(MainActivity.this,FaceRecognitionActivity.class));
                    findViewById(R.id.face_layout).setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextMessage = (TextView) findViewById(R.id.message);
        audioResult = (TextView)findViewById(R.id.audio_result);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        asr = EventManagerFactory.create(this, "asr");
        asr.registerListener(this); //  EventListener 中 onEvent方法
        Button startBtn = (Button) findViewById(R.id.btn_start);
        Button stopBtn = (Button)findViewById(R.id.btn_stop);
        startBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                stop();
            }
        });
        if (enableOffline) {
            loadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
        }
    }

    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {

        String logTxt = "name: " + name;

        if (params != null && !params.isEmpty()) {
            //logTxt += " ;params :" + params;
            RecogResult recogResult = RecogResult.parseJson(params);
            //logTxt = recogResult.getOrigalResult();
            if(recogResult.getOrigalResult() != null && !recogResult.getOrigalResult().equals("")){
                Log.i("RecogResutl",recogResult.getOrigalResult());
                try {
                    JSONObject json = new JSONObject(recogResult.getOrigalResult());
                    if(json.optString("best_result") == null ||
                            json.optString("best_result").equals("")){
                        logTxt = json.getString("best_result");
                    }else{
                        logTxt = json.optString("best_result");
                    }

                }catch (JSONException e) {
                   // logTxt = "你说啥？" + e.getMessage();
                    //e.printStackTrace();
                }
            }
        }
        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
            if (params.contains("\"nlu_result\"")) {
                if (length > 0 && data.length > 0) {
                    logTxt += ", 语义解析结果：" + new String(data, offset, length);
                }
            }
        } else if (data != null) {
            logTxt += " ;data length=" + data.length;
        }
        audioResult.setText(logTxt);
    }


    /**
     *
     * 点击开始按钮
     * 测试参数填在这里
     */
    private void start() {
        audioResult.setText("开始识别。。。");
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        String event = null;
        event = SpeechConstant.ASR_START; // 替换成测试的event

        if (enableOffline) {
            params.put(SpeechConstant.DECODER, 2);
        }
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // params.put(SpeechConstant.NLU, "enable");
        params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0); // 长语音
        // params.put(SpeechConstant.IN_FILE, "res:///com/baidu/android/voicedemo/16k_test.pcm");
        // params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        // params.put(SpeechConstant.PROP ,20000);
        // params.put(SpeechConstant.PID, 1537); // 中文输入法模型，有逗号
        // 请先使用如‘在线识别’界面测试和生成识别参数。 params同ActivityRecog类中myRecognizer.start(params);
        // 复制此段可以自动检测错误
        (new AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        Log.w("AutoCheckMessage", message);
                    }
                }
            }
        },enableOffline)).checkAsr(params);
        String json = null; // 可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        asr.send(event, json, null, 0, 0);
        Log.i("AudioParam","输入参数：" + json);
    }

    /**
     * 点击停止按钮
     */
    private void stop() {
        Log.i("AudioStop","停止识别：ASR_STOP");
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
    }


    /**
     * enableOffline设为true时，在onCreate中调用
     */
    private void loadOfflineEngine() {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(SpeechConstant.DECODER, 2);
        params.put(SpeechConstant.ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH, "assets://baidu_speech_grammar.bsg");
        asr.send(SpeechConstant.ASR_KWS_LOAD_ENGINE, new JSONObject(params).toString(), null, 0, 0);
    }

    /**
     * enableOffline为true时，在onDestory中调用，与loadOfflineEngine对应
     */
    private void unloadOfflineEngine() {
        asr.send(SpeechConstant.ASR_KWS_UNLOAD_ENGINE, null, null, 0, 0); //
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        Log.i("AudioResult","权限获取结果：" + requestCode);
    }

}
